package by.pavka.library.controller.command.impl;

import by.pavka.library.ConfigurationManager;
import by.pavka.library.controller.command.ActionCommand;
import by.pavka.library.entity.client.AppClient;
import by.pavka.library.model.mapper.ConstantManager;
import by.pavka.library.model.service.ServiceException;
import by.pavka.library.model.service.WelcomeService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class WelcomeCommand implements ActionCommand {
  @Override
  public void execute(HttpServletRequest request) {
    //TODO
    WelcomeService service = WelcomeService.getInstance();
    //LibraryService service = LibraryService.getInstance();
    HttpSession session = request.getSession();
    try {
      int books = service.countBooks();
      int users = service.countUsers();
      ServletContext servletContext = request.getServletContext();
      servletContext.setAttribute("books", books);
      servletContext.setAttribute("users", users);
      if (session.isNew()) {
        AppClient client = new AppClient() {
          @Override
          public String getRole() {
            return ConstantManager.GUEST;
          }
        };
        session.setAttribute("client", client);
      }
      session.setAttribute(PAGE, ConfigurationManager.getProperty("welcome"));
    } catch (ServiceException e) {
      session.setAttribute("page", ConfigurationManager.getProperty("error"));
      LOGGER.error("WelcomeCommand hasn't completed");
    }
  }
}
