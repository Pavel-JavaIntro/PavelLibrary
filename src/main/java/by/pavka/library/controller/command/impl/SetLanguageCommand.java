package by.pavka.library.controller.command.impl;

import by.pavka.library.controller.command.ActionCommand;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SetLanguageCommand implements ActionCommand {
  @Override
  public void execute(HttpServletRequest request) {
    String language = request.getParameter("lan");
    HttpSession session = request.getSession();
    session.setAttribute("lan", language);
  }
}
