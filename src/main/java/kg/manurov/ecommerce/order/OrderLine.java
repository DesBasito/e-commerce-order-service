package kg.manurov.ecommerce.order;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "customer_line")
public class OrderLine {

    @Id
    @GeneratedValue
    Integer id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;
    Integer productId;
    double quantity;
}
