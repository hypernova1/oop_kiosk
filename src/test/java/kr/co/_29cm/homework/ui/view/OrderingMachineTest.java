package kr.co._29cm.homework.ui.view;

import kr.co._29cm.homework.ui.input.Input;
import kr.co._29cm.homework.ui.input.command.Command;
import kr.co._29cm.homework.ui.output.Output;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderingMachineTest {

    @Mock
    private Input input;

    @Mock
    private Output output;

    @Mock
    private OrderProcessHandler orderProcessHandler;

    @InjectMocks
    private OrderingMachine orderingMachine;

    private final String USER_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        this.orderingMachine = new OrderingMachine(orderProcessHandler, input, output);
    }

    @DisplayName("상품 추가 케이스")
    @Test
    void test_continue_add_cart() {
        //given
        String PRODUCT_ID = "10000";

        Command productNoCommand = mock(Command.class);
        Command quantityCommand = mock(Command.class);

        when(productNoCommand.isCompleteOrder()).thenReturn(false);
        when(productNoCommand.toString()).thenReturn(PRODUCT_ID);
        when(input.inputProductNoOrIsCompleteOrder()).thenReturn(productNoCommand);

        when(quantityCommand.toInt()).thenReturn(1);
        when(input.inputQuantity()).thenReturn(quantityCommand);

        doNothing().when(orderProcessHandler).addProductToCart(USER_ID, PRODUCT_ID, 1);

        //when
        OrderProcess orderProcess = orderingMachine.addProduct(USER_ID);

        //then
        assertThat(orderProcess).isEqualTo(OrderProcess.ADD_PRODUCT);
    }

    @DisplayName("상품 추가 완료 (장바구니에 상품 존재)")
    @Test
    void test_complete_add_cart() {
        //given
        Command command = mock(Command.class);

        when(command.isCompleteOrder()).thenReturn(true);
        when(input.inputProductNoOrIsCompleteOrder()).thenReturn(command);
        when(orderProcessHandler.existCartItems(USER_ID)).thenReturn(true);

        //when
        OrderProcess orderProcess = orderingMachine.addProduct(USER_ID);

        //then
        assertThat(orderProcess).isEqualTo(OrderProcess.COMPLETE_ORDER);
    }

    @DisplayName("장바구니에 상품 존재하지 않음")
    @Test
    void test_not_exists_cart_item() {
        //given
        Command command = mock(Command.class);

        when(command.isCompleteOrder()).thenReturn(true);
        when(input.inputProductNoOrIsCompleteOrder()).thenReturn(command);
        when(orderProcessHandler.existCartItems(USER_ID)).thenReturn(false);

        //when
        OrderProcess orderProcess = orderingMachine.addProduct(USER_ID);

        //then
        assertThat(orderProcess).isEqualTo(OrderProcess.CONTINUE_OR_QUIT);
    }

}