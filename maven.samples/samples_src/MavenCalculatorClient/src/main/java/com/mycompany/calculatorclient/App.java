/*
 * Copyright (c) 2010, Oracle. All rights reserved.
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

package com.mycompany.calculatorclient;

import calc.CalculatorService;
import calc.CalculatorService_Service;
import calc.NegativeNumberException_Exception;

/** Client for Calculator web service.
 *
 * @author mkuchtiak
 */
public class App 
{
    public static void main( String[] args )
    {
        try { // Call Web Service Operation
            int x = 1;
            int y = 2;
            App app = new App();
            CalculatorService port = app.getCalculatorPort();
            port.log("Calculation started");
            int result = port.add(x, y);
            port.log("Calculation finished, result is "+result);
            System.out.println("Result = "+result);
        } catch (NegativeNumberException_Exception ex) {
            System.out.println("Error: "+ex.getMessage());
        }

    }

    /** Get service port stub for Calculator web service. */
    CalculatorService getCalculatorPort() {
        CalculatorService_Service service = new CalculatorService_Service();
        return service.getCalculatorServicePort();
    }
}
