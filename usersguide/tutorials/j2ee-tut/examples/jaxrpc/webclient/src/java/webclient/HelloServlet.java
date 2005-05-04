package webclient;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelloServlet extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String username = request.getParameter("username");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>HelloServlet</title>");
        out.println("</head>");
        out.println("<body>");
        if (username != null && username.length() > 0) {
            try {
                out.println("<img src=\"duke.waving.gif\">");
                out.println("<h2><font color=\"black\">");
                out.println(getHelloSEIPort().sayHello(username));
                out.println("</font></h2>");
            } catch(java.rmi.RemoteException ex) {
                ex.printStackTrace(out);
            }
        } else {
            out.println("You didn't specify your name.<br/>");
        }
        out.println("<a href=\"index.jsp\">back</a>");
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
    
    private webclient.Hello getHello() {
        webclient.Hello hello = null;
        try {
            javax.naming.InitialContext ic = new javax.naming.InitialContext();
            hello = (webclient.Hello) ic.lookup("java:comp/env/service/Hello");
        } catch(javax.naming.NamingException ex) {
            
        }
        return hello;
    }
    
    private webclient.HelloSEI getHelloSEIPort() {
        webclient.HelloSEI helloSEIPort = null;
        try {
            helloSEIPort = getHello().getHelloSEIPort();
        } catch(javax.xml.rpc.ServiceException ex) {
            
        }
        return helloSEIPort;
    }
}
