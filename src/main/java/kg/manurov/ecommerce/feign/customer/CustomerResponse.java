package kg.manurov.ecommerce.feign.customer;

public record CustomerResponse(
        String id,
        String firstname,
        String lastname,
        String email
) {

}
