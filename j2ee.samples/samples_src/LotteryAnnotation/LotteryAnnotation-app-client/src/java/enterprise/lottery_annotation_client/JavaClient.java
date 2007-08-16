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
package enterprise.lottery_annotation_client;

import javax.naming.InitialContext;

import enterprise.lottery_annotation_ejb_stateful.Lottery;
import enterprise.lottery_annotation_ejb_stateless.Dice;

public class JavaClient {

    public static void main(String args[]) {

        try {

            InitialContext ic = new InitialContext();

            Lottery lottery = 
                (Lottery) ic.lookup("enterprise.lottery_annotation_ejb_stateful.Lottery");

	    Dice dice;
	    for(int i=0; i<5; i++) {
            	dice = 
		    (Dice) ic.lookup("enterprise.lottery_annotation_ejb_stateless.Dice");
		lottery.select(dice.play());
            }

            String lotteryName = lottery.getName();
            String lotteryNumber = lottery.getNumber();
            String lotteryDate = lottery.getDate();
           
            String results = "Your" + " " + lotteryName + " " + 
                "quick pick, played on" + " " + lotteryDate +
                    " " + "is" + " " + lotteryNumber;       

            System.out.println(results);

        } catch(Exception e) {
	    System.out.println("Exception: " + e);
            e.printStackTrace();
        }

    }

}
