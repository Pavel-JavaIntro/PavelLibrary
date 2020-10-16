package by.pavka.library.controller.command.impl;

import by.pavka.library.ConfigurationManager;
import by.pavka.library.controller.command.ActionCommand;
import by.pavka.library.entity.impl.Edition;
import by.pavka.library.model.service.ServiceException;
import by.pavka.library.model.service.WelcomeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

public class FindEditionCommand implements ActionCommand {
  @Override
  public void execute(HttpServletRequest request) {
    String title = request.getParameter("title");
    String author = request.getParameter("author");
    WelcomeService welcomeService = WelcomeService.getInstance();
    HttpSession session = request.getSession();
    String page = (String)session.getAttribute("page");
    List<Edition> editions = null;
    try {
      editions = welcomeService.findEditions(title, author);
    } catch (ServiceException e) {
      page = ConfigurationManager.getProperty("error");
      logger.error("FindEditionCommand hasn't completed");
    }
    Map<Edition, String> editionInfo = null;
    try {
      editionInfo = welcomeService.authorsByEdition(editions);
      session.setAttribute("editions", editionInfo);
    } catch (ServiceException e) {
      page = ConfigurationManager.getProperty("error");
      logger.error("FindEditionCommand hasn't completed");
    }
    session.setAttribute("page", page);
  }
}
