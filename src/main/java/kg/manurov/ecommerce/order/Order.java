package kg.manurov.ecommerce.order;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Accessors(chain = true)
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Validated
@Table(name = "customer_order")
public class Order {

    @Id
    @GeneratedValue
    Integer id;

    @Column(unique = true,  nullable = false)
    String reference;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

    String customerId;

    @OneToMany(mappedBy = "order")
    List<OrderLine> orderLines;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    LocalDateTime lastModifiedDate;
}
