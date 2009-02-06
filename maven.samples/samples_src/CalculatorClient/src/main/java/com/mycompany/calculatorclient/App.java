package com.mycompany.calculatorclient;

import calc.NegativeNumberException_Exception;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try { // Call Web Service Operation
            calc.CalculatorService_Service service = new calc.CalculatorService_Service();
            calc.CalculatorService port = service.getCalculatorServicePort();
            int x = 1;
            int y = 2;
            port.log("Calculation started");
            int result = port.add(x, y);
            port.log("Calculation finished, result is "+result);
            System.out.println("Result = "+result);
        } catch (NegativeNumberException_Exception ex) {
            System.out.println("Error: "+ex.getMessage());
        }

    }
}
