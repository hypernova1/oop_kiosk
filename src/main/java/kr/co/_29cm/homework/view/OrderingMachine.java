package kr.co._29cm.homework.view;

import kr.co._29cm.homework.cart.domain.CartEmptyException;
import kr.co._29cm.homework.order.domain.NoOrderItemException;
import kr.co._29cm.homework.order.payload.OrderResponse;
import kr.co._29cm.homework.product.domain.ProductNotFoundException;
import kr.co._29cm.homework.product.domain.SoldOutException;
import kr.co._29cm.homework.product.payload.ProductDto;
import kr.co._29cm.homework.view.input.BadCommandException;
import kr.co._29cm.homework.view.input.Command;
import kr.co._29cm.homework.view.input.InputView;
import kr.co._29cm.homework.view.output.OutputView;

import java.util.List;

public class OrderingMachine {

    private final OrderProcessHandler orderProcessHandler;

    public OrderingMachine(OrderProcessHandler orderProcessHandler) {
        this.orderProcessHandler = orderProcessHandler;
    }

    /**
     * 주문 프로세스를 시작한다.
     */
    public void process() {
        List<ProductDto> products = orderProcessHandler.getAllProducts();
        OutputView.printProducts(products);

        while (true) {
            try {
                Command productNoInput = InputView.inputProductNoOrIsCompleteOrder();
                if (!productNoInput.isCompleteOrder()) {
                    Command quantityInput = InputView.inputQuantity();
                    orderProcessHandler.addProductToCart(productNoInput.toString(), quantityInput.toInt());
                    continue;
                }

                OrderResponse orderResponse = orderProcessHandler.createOrder();
                OutputView.printOrderDetail(orderResponse);

                boolean isProgramTerminated = InputView.inputPogramTerminatedOrOrderContinue().isProgramTerminated();
                if (isProgramTerminated) {
                    break;
                }
            } catch (BadCommandException | ProductNotFoundException | CartEmptyException | NoOrderItemException e) {
                OutputView.printException(e);
            } catch (SoldOutException e) {
                OutputView.printException(e);
                orderProcessHandler.clearCart();
            }
        }

        OutputView.printThanksToCustomer();
    }

}
