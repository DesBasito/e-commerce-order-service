package kg.manurov.ecommerce.mapper;

import kg.manurov.ecommerce.dto.orderLine.OrderLineRequest;
import kg.manurov.ecommerce.dto.orderLine.OrderLineResponse;
import kg.manurov.ecommerce.order.Order;
import kg.manurov.ecommerce.order.OrderLine;
import org.springframework.stereotype.Service;

@Service
public class OrderLineMapper {
    public OrderLine toOrderLine(OrderLineRequest request) {
        return OrderLine.builder()
                .id(request.id())
                .productId(request.productId())
                .order(
                        Order.builder()
                                .id(request.orderId())
                                .build()
                )
                .quantity(request.quantity())
                .build();
    }

    public OrderLineResponse toOrderLineResponse(OrderLine orderLine) {
        return new OrderLineResponse(
                orderLine.getId(),
                orderLine.getQuantity()
        );
    }
}