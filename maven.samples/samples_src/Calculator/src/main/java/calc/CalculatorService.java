package calc;

import javax.annotation.Resource;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/** Web service containin two operations: add and log.
 * add operation calculates the sum of two numbers
 * log operation logs message onto server
 *
 * @author mkuchtiak
 */
@WebService(serviceName="CalculatorService")
public class CalculatorService {

    // use @Resource injection to create a WebServiceContext for server logging
    private @Resource
    WebServiceContext webServiceContext;

    /** Calculates the sum of two numbers
     *
     * @param x integer number
     * @param y integer number
     * @return sum of two numbers
     * @throws calc.NegativeNumberException if one of two numbers is negative
     */
    @WebMethod(operationName = "add")
    public int add(@WebParam(name = "x")int x, @WebParam(name = "y")int y)
        throws NegativeNumberException {

        getServletContext().log("Parameters: x="+x+", y="+y);
        if (x < 0) {
            throw new NegativeNumberException("x is less then zero");
        } else if (y < 0) {
            throw new NegativeNumberException("y is less then zero");
        } else {
            return x+y;
        }
    }

    /** Used for server logging.
     *  The operation is oneway: provides no response
     *
     * @param text
     */
    @WebMethod(operationName = "log")
    @Oneway
    public void logServer(@WebParam(name = "message") String text) {
        // log message onto server
        getServletContext().log(text);
    }

    /** Get ServletContext.
     *
     * @return ServletContext object
     */
    private ServletContext getServletContext() {
        return (ServletContext) webServiceContext.getMessageContext().get(
                MessageContext.SERVLET_CONTEXT);
    }

}
