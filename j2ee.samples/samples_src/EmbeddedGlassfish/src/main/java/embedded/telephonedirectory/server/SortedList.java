/*
 * Copyright (c) 2011, Oracle. All rights reserved.
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
 * * Neither the name of Oracle nor the names of its contributors
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
package embedded.telephonedirectory.server;


import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * This servlet is responsible for displaying all the telephone
 * directory entries in sorted order.
 */
public class SortedList extends HttpServlet {

    @Inject
    PersistenceManager persistenceManager;

    /**
     * Retrieves all the {@link PersonEntity} entries from the database
     * and sorts them in the ascending order of name.
     * <p/>
     * The sorted entries are displayd in a tabular form.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();


        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<link rel='stylesheet' type='text/css' href='stylesheet.css' />");
            out.println("<title>All entries in telephone directory</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1 align=center>All entries in telephone directory<p/></h1><p/>");

            List<PersonEntity> sortedEntries = persistenceManager.getSortedEntries();

            if (sortedEntries.isEmpty()) {
                out.println("<p/>Telephone directory is EMPTY.");
            } else {
                out.println("<table border='1' cellpadding='10'>");
                out.println("<tr>");
                out.println("<th> Name</th>");
                out.println("<th> Address</th>");
                out.println("<th> Location</th>");
                out.println("<th> Country</th>");
                out.println("<th> Phone Number</th>");
                out.println("</tr>");
                for (PersonEntity personEntity : sortedEntries) {
                    out.println("<tr>");
                    out.println("<td> " + personEntity.getName() + "</td>");
                    out.println("<td> " + personEntity.getAddress() + "</td>");
                    out.println("<td> " + personEntity.getLocation() + "</td>");
                    out.println("<td> " + personEntity.getCountry() + "</td>");
                    out.println("<td> " + personEntity.getPhoneNumber() + "</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
        } finally {
            out.println("<hr/><a href='index.jsp'>Home</a>");
            out.println("</body>");
            out.println("</html>");
            out.close();
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
