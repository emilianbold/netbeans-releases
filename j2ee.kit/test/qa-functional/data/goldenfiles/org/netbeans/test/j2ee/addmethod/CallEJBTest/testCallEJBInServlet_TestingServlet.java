/*
 * TestingServlet.java
 *
 * Created on 26. kveten 2005, 14:03
 */

package test;

import java.io.*;
import java.net.*;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author lm97939
 * @version
 */
public class TestingServlet extends HttpServlet {
    
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
        out.println("<title>Servlet TestingServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet TestingServlet at " + request.getContextPath () + "</h1>");
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

    private TestingSessionRemote lookupTestingSessionBean() {
        try {
            Context c = new InitialContext();
            Object remote = c.lookup("java:comp/env/TestingSessionBean");
            TestingSessionRemoteHome rv = (TestingSessionRemoteHome) javax.rmi.PortableRemoteObject.narrow(remote, test.TestingSessionRemoteHome.class);
            return rv.create();
        } catch (NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        } catch (CreateException ce) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, "exception caught", ce);
            throw new RuntimeException(ce);
        } catch (RemoteException re) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, "exception caught", re);
            throw new RuntimeException(re);
        }
    }
    // </editor-fold>
}
