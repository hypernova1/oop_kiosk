package kr.co._29cm.homework.view.input.command;

public interface Command {

    boolean isCompleteOrder();
    boolean isProgramTerminated();
    int toInt();

}
