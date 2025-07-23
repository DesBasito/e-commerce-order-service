package kg.manurov.ecommerce.services;


import jakarta.persistence.EntityNotFoundException;
import kg.manurov.ecommerce.dto.order.OrderRequest;
import kg.manurov.ecommerce.dto.order.OrderResponse;
import kg.manurov.ecommerce.dto.orderLine.OrderLineRequest;
import kg.manurov.ecommerce.errors.BusinessException;
import kg.manurov.ecommerce.feign.customer.CustomerClient;
import kg.manurov.ecommerce.feign.customer.CustomerResponse;
import kg.manurov.ecommerce.feign.payment.PaymentClient;
import kg.manurov.ecommerce.feign.payment.PaymentRequest;
import kg.manurov.ecommerce.feign.product.ProductClient;
import kg.manurov.ecommerce.feign.product.PurchaseRequest;
import kg.manurov.ecommerce.feign.product.PurchaseResponse;
import kg.manurov.ecommerce.kafka.OrderConfirmation;
import kg.manurov.ecommerce.kafka.OrderProducer;
import kg.manurov.ecommerce.mapper.OrderMapper;
import kg.manurov.ecommerce.order.Order;
import kg.manurov.ecommerce.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;
    private final ProductClient productClient;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;

    @Transactional
    public Integer createOrder(OrderRequest request) {
        CustomerResponse customer = this.customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided ID"));

        List<PurchaseResponse> purchasedProducts = productClient.purchaseProducts(request.products());

        Order order = this.repository.save(mapper.toOrder(request));

        for (PurchaseRequest purchaseRequest : request.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }
        PaymentRequest paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        request.reference(),
                        request.amount(),
                        request.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );

        return order.getId();
    }

    public List<OrderResponse> findAllOrders() {
        return this.repository.findAll()
                .stream()
                .map(this.mapper::fromOrder)
                .toList();
    }

    public OrderResponse findById(Integer id) {
        return this.repository.findById(id)
                .map(this.mapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", id)));
    }
}
