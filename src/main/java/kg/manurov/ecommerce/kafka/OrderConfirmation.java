package kg.manurov.ecommerce.kafka;

import kg.manurov.ecommerce.feign.customer.CustomerResponse;
import kg.manurov.ecommerce.feign.product.PurchaseResponse;
import kg.manurov.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation (
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products

) {
}