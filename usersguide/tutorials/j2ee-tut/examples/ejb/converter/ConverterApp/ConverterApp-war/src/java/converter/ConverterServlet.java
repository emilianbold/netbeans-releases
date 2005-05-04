/*
 * ConverterServlet.java
 *
 * Created on 04 May 2005, 18:38
 */

package converter;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Administrator
 * @version
 */
public class ConverterServlet extends HttpServlet {
    
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
        out.println("<title>Servlet ConverterServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1><b><center>Converter</center></b></h1>");
        out.println("<hr>");
        out.println("<p>Enter an amount to convert:</p>");
        out.println("<form method=\"get\">");
        out.println("<input type=\"text\" name=\"amount\" size=\"25\">");
        out.println("<br>");
        out.println("<p>");
        out.println("<input type=\"submit\" value=\"Submit\">");
        out.println("<input type=\"reset\" value=\"Reset\">");
        out.println("</form>");
        String amount = request.getParameter("amount");
        if ( amount != null && amount.length() > 0 ) {
            try {
                converter.ConverterRemote converter;
                converter = lookupConverterBean();
                
                java.math.BigDecimal d = new java.math.BigDecimal(amount);
                out.println("<p>");
                out.println("<p>");
                out.println(amount + " Dollars are  " + converter.dollarToYen(d) + " Yen.");
                out.println("<p>");
                out.println(amount + " Yen are " + converter.yenToEuro(d) + " Euro.");
                
                converter.remove();
            } catch (Exception e){
                out.println("Cannot lookup or execute EJB!");
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
    
    private converter.ConverterRemote lookupConverterBean() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            Object remote = c.lookup("java:comp/env/ejb/ConverterBean");
            converter.ConverterRemoteHome rv = (converter.ConverterRemoteHome) javax.rmi.PortableRemoteObject.narrow(remote, converter.ConverterRemoteHome.class);
            return rv.create();
        } catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        } catch(javax.ejb.CreateException ce) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ce);
            throw new RuntimeException(ce);
        } catch(java.rmi.RemoteException re) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,re);
            throw new RuntimeException(re);
        }
    }
}
