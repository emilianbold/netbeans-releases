/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package servlet.stateless;

import java.io.*;
import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Though it is perfectly fine to declare the dependency on the bean
// at the type level, it is not required for stateless session bean
// Hence the next two lines are commented and we rely on the
// container to inject the bean.
// @EJB(name="StatelessSession", beanInterface=StatelessSession.class)

@WebServlet(name="Servlet", urlPatterns={"/servlet"})
public class Servlet2Stateless
    extends HttpServlet {

    // Using injection for Stateless session bean is still thread-safe since
    // the ejb container will route every request to different
    // bean instances. However, for Stateful session beans the
    // dependency on the bean must be declared at the type level

    @EJB
    private StatelessSessionBean sless;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            out.println("<HTML> <HEAD> <TITLE> Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1> Servlet2Stateless:: Please enter your name </FONT> </CENTER> <p> ");
            out.println("<form method=\"POST\">");
            out.println("<TABLE>");
            out.println("<tr><td>Name: </td>");
            out.println("<td><input type=\"text\" name=\"name\"> </td>");
            out.println("</tr><tr><td></td>");
            out.println("<td><input type=\"submit\" name=\"sub\"> </td>");
            out.println("</tr>");
            out.println("</TABLE>");
            out.println("</form>");
            String val = req.getParameter("name");

            if ((val != null) && (val.trim().length() > 0)) {
                out
                        .println("<FONT size=+1 color=red> Greeting from StatelessSessionBean: </FONT> "
                                + sless.sayHello(val) + "<br>");
            }
            out.println("</BODY> </HTML> ");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("webclient servlet test failed");
            throw new ServletException(ex);
        }
    }

}
