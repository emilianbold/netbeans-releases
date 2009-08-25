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

package enterprise.lottery_annotation_web_servlet;

import java.io.IOException;
import java.util.Locale; 
import java.util.ResourceBundle; 

import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import enterprise.lottery_annotation_ejb_stateful.Lottery;
import enterprise.lottery_annotation_ejb_stateless.Dice;


@WebServlet(name="PlayLotteryServlet", urlPatterns={"/PlayLotteryServlet"})
public class PlayLotteryServlet extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html");

	Lottery lottery;
        Dice dice;
        int NO_OF_DIGITS = 6;
        ResourceBundle rb = ResourceBundle.getBundle("LocalStrings", Locale.getDefault());

        try {
            InitialContext initContext  = new InitialContext();

            lottery = 
                (Lottery) initContext.lookup("enterprise.lottery_annotation_ejb_stateful.Lottery");
	    dice =
                (Dice) initContext.lookup("enterprise.lottery_annotation_ejb_stateless.Dice");
        } 
        catch (Exception e) { 
            System.out.println(rb.getString("exception_creating_initial_context") +
                ": " + e.toString()); 
            return; 
        } 

	lottery.setName(request.getParameter("lottery_name"));

	for(int i=0; i<NO_OF_DIGITS; i++) {
            lottery.select(dice.play());
        }

	String lotteryName = lottery.getName();
        String lotteryNumber = lottery.getNumber();
	String lotteryDate = lottery.getDate();

        //set the results in the Request object
        request.setAttribute("lottery_name", lottery.getName()); 
        request.setAttribute("lottery_number", lottery.getNumber()); 
        request.setAttribute("lottery_date", lottery.getDate()); 

        // dispatch jsp for output
        response.setContentType("text/html");  
        RequestDispatcher dispatcher = 
            getServletContext().getRequestDispatcher("/LotteryView.jsp"); 
        dispatcher.include(request, response);
        return;
    }


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
        ResourceBundle rb = ResourceBundle.getBundle("LocalStrings", Locale.getDefault());
        return rb.getString("servlet_description"); 
    }
}
