package kg.manurov.ecommerce.feign.payment;

import kg.manurov.ecommerce.feign.customer.CustomerResponse;
import kg.manurov.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}
