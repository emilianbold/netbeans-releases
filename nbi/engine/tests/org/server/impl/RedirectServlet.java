package org.server.impl;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Danila_Dugurov
 */
public class RedirectServlet extends HttpServlet {
  
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final String fileName = req.getPathInfo().substring(req.getPathInfo().lastIndexOf("/") + 1);
    final String path = req.getPathInfo().substring(0,req.getPathInfo().lastIndexOf("/"));
    resp.sendRedirect("/" + fileName);
  }
}