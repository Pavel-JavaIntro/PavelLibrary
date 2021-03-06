package by.pavka.library.controller;

import by.pavka.library.ConfigurationManager;
import by.pavka.library.MessageManager;
import by.pavka.library.controller.command.ActionCommand;
import by.pavka.library.controller.command.ActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class LibraryServlet extends HttpServlet {
  private static final Logger LOGGER = LogManager.getLogger(LibraryServlet.class);

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    process(request, response);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (request.getParameter("command").equals("welcome")) {
      process(request, response);
    } else {
      response.sendError(403);
    }
  }

  private void process(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Old version
    ActionFactory client = new ActionFactory();
    ActionCommand command = client.defineCommand(request);
    LOGGER.info("Execution: " + command.getClass().getSimpleName());
    command.execute(request);
    String page = (String) request.getSession().getAttribute("page");
    if (page != null) {
      RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher(page);
      requestDispatcher.forward(request, response);
    } else {
      page = ConfigurationManager.getProperty("index");
      request.getSession().setAttribute("nullPage", MessageManager.getProperty("message.nullpage"));
      response.sendRedirect(request.getContextPath() + page);
    }

  }
}
