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
 * This servlet is responsible for adding a new entry
 * into the telephone directory database.
 */
public class AddNewEntry extends HttpServlet {

    @Inject
    Person person;

    @Inject
    PersistenceManager persistenceManager;

    /**
     * Creates a new {@link PersonEntity}, sets it in request scoped {@link Person} CDI bean
     * and then invokes {@link telephone_directory.cdi.PersistenceManager#persist()} to
     * store the {@link PersonEntity} in the database.
     * <p/>
     * Once the {@link PersonEntity} is stored, all the entries in telephone directory
     * are displayed in a tabular form.
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

        PersonEntity p = new PersonEntity();
        p.setName((request.getParameter("name").toString()).toUpperCase());
        p.setAddress((request.getParameter("address").toString()).toUpperCase());
        p.setLocation((request.getParameter("location").toString()).toUpperCase());
        p.setCountry((request.getParameter("country").toString()).toUpperCase());
        p.setPhoneNumber((request.getParameter("phone").toString()).toUpperCase());
        person.setPersonEntity(p);

        boolean added = persistenceManager.persist();

        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<link rel='stylesheet' type='text/css' href='stylesheet.css' />");
            out.println("<title>Create new entry in telephone book</title>");
            out.println("</head>");
            out.println("<body>");

            if (added) {
                out.println("<p>Entry has been created successfully.</p>");

                List<PersonEntity> allEntries = persistenceManager.getAllEntries();
                out.println("<table border='1' cellpadding='10'>");
                out.println("<tr>");
                out.println("<th> Name</th>");
                out.println("<th> Address</th>");
                out.println("<th> Location</th>");
                out.println("<th> Country</th>");
                out.println("<th> Phone Number</th>");
                out.println("</tr>");

                for (PersonEntity personEntity : allEntries) {
                    out.println("<tr>");
                    out.println("<td> " + personEntity.getName() + "</td>");
                    out.println("<td> " + personEntity.getAddress() + "</td>");
                    out.println("<td> " + personEntity.getLocation() + "</td>");
                    out.println("<td> " + personEntity.getCountry() + "</td>");
                    out.println("<td> " + personEntity.getPhoneNumber() + "</td>");
                    out.println("</tr>");
                }

                out.println("</table>");
            } else {
                out.println("<p>Unable to add entry. Please check the console output for errors.</p>");
            }
        } finally {
            out.println("<hr/><a href='index.jsp'>Home</a>");
            out.println("</body>");
            out.println("</html>");
            out.close();
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method by simply invoking {@link #processRequest}.
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
     * Handles the HTTP <code>POST</code> method by simply invoking {@link #processRequest}.
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
