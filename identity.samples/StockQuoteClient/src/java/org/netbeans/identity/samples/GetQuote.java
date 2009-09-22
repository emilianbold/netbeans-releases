/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
*
* The contents of this file are subject to the terms of either the GNU
* General Public License Version 2 only ("GPL") or the Common
* Development and Distribution License("CDDL") (collectively, the
* "License"). You may not use this file except in compliance with the
* License. You can obtain a copy of the License at
* http://www.netbeans.org/cddl-gplv2.html
* or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
* specific language governing permissions and limitations under the
* License.  When distributing the software, include this License Header
* Notice in each file and include the License file at
* nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
* particular file as subject to the "Classpath" exception as provided
* by Sun in the GPL Version 2 section of the License file that
* accompanied this code. If applicable, add the following below the
* License Header, with the fields enclosed by brackets [] replaced by
* your own identifying information:
* "Portions Copyrighted [year] [name of copyright owner]"
*
* Contributor(s):
*
* The Original Software is NetBeans. The Initial Developer of the Original
* Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
* Microsystems, Inc. All Rights Reserved.
*
* If you wish your version of this file to be governed by only the CDDL
* or only the GPL Version 2, indicate your decision by adding
* "[Contributor] elects to include this software in this distribution
* under the [CDDL or GPL Version 2] license." If you do not indicate a
* single choice of license, a recipient has the option to distribute
* your version of this file under either the CDDL, the GPL Version 2 or
* to extend the choice of license to its licensees as provided above.
* However, if you add GPL Version 2 code and therefore, elected the GPL
* Version 2 license, then the option applies only if the new code is
* made subject to such option by the copyright holder.
*/

package org.netbeans.identity.samples;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.ws.WebServiceRef;

/**
 *
 * @author Administrator
 * 
 */
public class GetQuote extends HttpServlet {
  
    @WebServiceRef(wsdlLocation =
        "http://localhost:8080/StockQuoteService/StockService?wsdl")
    private org.netbeans.identity.samples.StockService service;
    
    /**
     * Get Stock quote from WSP
     */
    public QuoteResponseType getStockQuote(String symbol) {
        StockQuotePortType port = service.getStockQuotePortTypePort();
        QuoteRequestType body = new QuoteRequestType();
        body.setSymbol(symbol);
        return(port.getStockQuote(body));
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String symbol = request.getParameter("symbol");
        if ((symbol == null) || (symbol.length() == 0)) {
            out.println("<h1>Invalid Stock Symbol</h1>");
            out.close();
            return;
        }
        try {
            // Get StockQuote
            QuoteResponseType result = getStockQuote(symbol);
            PriceType price = result.getPrice();
            
            // Display the page
            out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
            out.write("\"http://www.w3.org/TR/html4/loose.dtd\">\n");
            out.write("<html>\n");
            out.write("<head>\n");
            out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
            out.write("<title>Display Quote</title>\n");
            addJavaScript(out);
            out.write("</head>\n");
            out.write("<body>\n");
            out.write("<h1>Stock Quote Display</h1>\n");
            out.write("<hr/>\n");
            out.write("<table border=\"1\" width=\"400\" cellspacing=\"1\" cellpadding=\"1\" bgcolor=\"#BBDDFF\">\n");
            out.write("<thead>\n");
            out.write("<tr>\n<th>");
            out.print(result.getCompany());
            out.write("(");
            out.print( result.getSymbol());
            out.write(")</th>\n");
            out.write("<th>");
            out.print(result.getMessage());
            out.write("</th>\n");
            out.write("</tr>\n");
            out.write("</thead>\n");
            out.write("<tbody>\n");
            out.write("<tr>\n");
            out.write("<td width=\"60%\">Last Trade:</td>\n");
            out.write("<td width=\"40%\">");
            out.print( (price != null) ? price.getLast() : "N/A");
            out.write("</td>\n");
            out.write("</tr>\n");
            out.write("<tr>\n");
            out.write("<td>Trade Time:</td>\n");
            out.write("<td>");
            out.print( result.getTime() );
            out.write("</td>\n");
            out.write("</tr>\n");
            out.write("<tr>\n");
            out.write("<td>Change:</td>\n");
            out.write("<td> ");
            out.print( result.getChange() );
            out.write("</td>\n");
            out.write("</tr>\n");
            out.write("<tr>\n");
            out.write("<td>Open:</td>\n");
            out.write("<td>");
            out.print((price != null)? price.getOpen() : "N/A");
            out.write("</td>\n");
            out.write("</tr>\n");
            out.write("<tr>\n");
            out.write("<td>Day's Range:</td>\n");
            out.write("<td>");
            out.print((price != null)? price.getDayLow() : "N/A");
            out.write(" - ");
            out.print((price != null)? price.getDayHigh() : "N/A");
            out.write("</td>\n");
            out.write("</tr>\n");
            out.write("<tr>\n");
            out.write("<td>52wk Range:</td>\n");
            out.write("<td>");
            out.print( (price != null)? price.getYearRange() : "N/A");
            out.write("</td>\n");
            out.write("</tr>\n");
            out.write("<tr>\n");
            out.write("<td>Volume:</td>\n");
            out.write("<td>");
            out.print( result.getVolume() );
            out.write("</td>\n");
            out.write("</tr>\n");
            out.write("<tr>\n");
            out.write("<td>Market Cap:</td>\n");
            out.write("<td>");
            out.print(result.getMarketCap());
            out.write("</td>\n");
            out.write("</tr>\n");
            out.write("</tbody>\n");
            out.write("</table>\n");
            out.write("<hr/>\n");
            
            // Image for checking request and response XML
            out.write("<h3>View SOAP Messages</h3>");
            out.write("<img name=\"Communications\" src=\"communication.gif\" " +
                "width=\"421\" height=\"203\" border=\"0\" " +
                "usemap=\"#m_arviimg\"><map name=\"m_arviimg\">\n");
            out.write("<area shape=\"rect\" coords=\"180,120,245,144\" " +
                "href=\"javascript:DoRemote('");
            out.write("SOAPMessage?dir=response");
            out.write("',843,897)\">");            
            out.write("<area shape=\"rect\" coords=\"179,74,245,100\" " +
                "href=\"javascript:DoRemote('");
            out.write("SOAPMessage?dir=request");
            out.write("',843,897)\">");
            out.write("</map>\n ");
            
            // Link to try again
            out.write("<hr/>\n");
            out.write("<a href=\"index.jsp\">Try again</a>");
            
            // Image for checking request and response XML
            
            
            // If SSOToken is present, provide a logout link
            Cookie[] cookies = request.getCookies();
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equalsIgnoreCase(
                    "iPlanetDirectoryPro")) {
                    // Provide a logout link
                    out.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<a href=\"/amserver/UI/Logout\">" +
                        "Logout</a>\n");
                }
            }
            out.write("</body>\n");
            out.write("</html>\n");
        } catch (Exception ex) {
            out.println(ex);
        }
        out.close();
    }
    
    private void addJavaScript(PrintWriter out) {
        out.write("<SCRIPT LANGUAGE=\"JavaScript\">\n");
        out.write("function DoRemote(url,w,h) {\n");
        out.write("remote= window.open(\"\",\"remotewin\"," +
            "'toolbar=0,location=0,directories=0,status=0,menubar=0," +
            "scrollbars=1,resizable=1,alwaysRaised=1,width='+w+',height='+h);");
        out.write("\nremote.resizeTo(w,h);\n");
        out.write("remote.location.href = url;\n");
        out.write("if (remote.opener == null) remote.opener = window;\n");
        out.write("remote.opener.name = \"opener\";");
        out.write("remote.focus();");
        out.write("}\n</SCRIPT>\n");
    }
    
//        out.write("<SCRIPT TYPE=\"text/javascript\">\n<!--\n");
//        out.write("function popup(mylink, windowname)\n{\n");
//        out.write("if (! window.focus)return true;\n");
//        out.write("var href;\nif (typeof(mylink) == 'string')\n");
//        out.write("href=mylink;\nelse\nhref=mylink.href;\n");
//        out.write("window.open(href, windowname, " +
//            "'width=843,height=897,scrollbars=yes');");
//        out.write("return false;\n} //-->\n");
//        out.write("</SCRIPT>");


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
}
