package kg.manurov.ecommerce.services;

import kg.manurov.ecommerce.dto.orderLine.OrderLineRequest;
import kg.manurov.ecommerce.dto.orderLine.OrderLineResponse;
import kg.manurov.ecommerce.mapper.OrderLineMapper;
import kg.manurov.ecommerce.order.OrderLine;
import kg.manurov.ecommerce.repositories.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderLineService {

    private final OrderLineRepository repository;
    private final OrderLineMapper mapper;

    public Integer saveOrderLine(OrderLineRequest request) {
        OrderLine order = mapper.toOrderLine(request);
        return repository.save(order).getId();
    }

    public List<OrderLineResponse> findAllByOrderId(Integer orderId) {
        return repository.findAllByOrder_Id(orderId)
                .stream()
                .map(mapper::toOrderLineResponse)
                .toList();
    }
}
