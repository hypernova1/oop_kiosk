package kr.co._29cm.homework.order.application;

import kr.co._29cm.homework.order.domain.NoOrderItemException;
import kr.co._29cm.homework.order.domain.Order;
import kr.co._29cm.homework.order.domain.OrderRepository;
import kr.co._29cm.homework.order.payload.OrderRequest;
import kr.co._29cm.homework.payment.application.PaymentService;
import kr.co._29cm.homework.payment.payload.PaymentRequest;
import kr.co._29cm.homework.product.application.ProductService;
import kr.co._29cm.homework.product.application.StockRollbackEvent;
import kr.co._29cm.homework.product.payload.ProductPriceDto;
import kr.co._29cm.homework.product.payload.ProductQuantityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 주문을 생성한다.
     *
     * @param orderRequest 주문 정보
     * @return 주문 번호
     */
    @Transactional
    public String create(OrderRequest orderRequest) {
        if (orderRequest.products().isEmpty()) {
            throw new NoOrderItemException();
        }

        List<ProductQuantityDto> productQuantityDtoList = orderRequest.products().stream()
                .map((product) -> new ProductQuantityDto(product.productNo(), product.quantity()))
                .toList();
        productService.decreaseStock(productQuantityDtoList);

        try {
            return createOrder(orderRequest, productQuantityDtoList);
        } catch (RuntimeException e) {
            this.eventPublisher.publishEvent(new StockRollbackEvent(productQuantityDtoList));
            throw e;
        }
    }

    /**
     * 주문을 생성한다.
     *
     * @param orderRequest 주문 요청 데이터
     * @param productQuantities 주문할 상품 재고 데이터
     * @return 주문 번호
     * */
    private String createOrder(OrderRequest orderRequest, List<ProductQuantityDto> productQuantities) {
        List<String> productNoList = productQuantities.stream().map(ProductQuantityDto::productNo).toList();
        List<ProductPriceDto> productPrices = productService.getProductPrices(productNoList);

        Order order = Order.of(orderRequest, productPrices);

        this.orderRepository.save(order);
        this.paymentService.create(
                new PaymentRequest(
                        order.getOrderNo(),
                        order.getTotalProductPrice(),
                        order.getShippingPrice(),
                        order.getUserId()
                )
        );
        return order.getOrderNo();
    }

}
