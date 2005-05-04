/*
 * HelloWebServlet.java
 *
 */

package helloweb;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @version
 */
public class HelloWebServlet extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet HelloWebServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<p>Enter your name:");
        out.println("<form method=\"get\">");
        out.println("<input type=\"text\" name=\"name\" size=\"25\">");
        out.println("<br>");
        out.println("<p>");
        out.println("<input type=\"submit\" value=\"Submit\">");
        out.println("</form>");
        String name = request.getParameter("name");
        if ( name != null ) {
            try {
                out.println(getHelloServiceSEIPort().sayHello(name));
            } catch(java.rmi.RemoteException ex) {
                out.println("<p>Caught an exception <p>" + ex);
            }
        }
        out.println("</body>");
        out.println("</html>");
        
        out.close();
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
    
    private helloweb.HelloService getHelloService() {
        helloweb.HelloService helloService = null;
        try {
            javax.naming.InitialContext ic = new javax.naming.InitialContext();
            helloService = (helloweb.HelloService) ic.lookup("java:comp/env/service/HelloService");
        } catch(javax.naming.NamingException ex) {
            // TODO handle JNDI naming exception
        }
        return helloService;
    }
    
    private helloweb.HelloServiceSEI getHelloServiceSEIPort() {
        helloweb.HelloServiceSEI helloServiceSEIPort = null;
        try {
            helloServiceSEIPort = getHelloService().getHelloServiceSEIPort();
        } catch(javax.xml.rpc.ServiceException ex) {
            // TODO handle service exception
        }
        return helloServiceSEIPort;
    }
}
