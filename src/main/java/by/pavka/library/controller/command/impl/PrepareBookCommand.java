package by.pavka.library.controller.command.impl;

import by.pavka.library.BookOrder;
import by.pavka.library.OrderHolder;
import by.pavka.library.controller.command.ActionCommand;
import by.pavka.library.entity.EditionInfo;
import by.pavka.library.entity.client.AppClient;
import com.sun.scenario.effect.impl.prism.PrDrawable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Queue;

public class PrepareBookCommand implements ActionCommand {
  public static final String DENY = "deny";
  public static final String PREPARE = "prepare";

  @Override
  public void execute(HttpServletRequest request) {
    String action = request.getParameter("preparation");
    OrderHolder orderHolder = OrderHolder.getInstance();
    Queue<BookOrder> placedOrders = orderHolder.getPlacedOrders();
    int bookId = Integer.parseInt(request.getParameter("book"));
    for (BookOrder bookOrder : placedOrders) {
      for (EditionInfo editionInfo : bookOrder.getEditionInfoSet()) {
        if (editionInfo.getBook() != null && editionInfo.getBook().getId() == bookId) {
          BookOrder passedOrder = bookOrder.passBook(editionInfo);
          if (action.equals(PREPARE)) {
            orderHolder.prepareOrder(passedOrder);
          }
        }
      }
    }
  }
}
