package hints;

import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPMessageHandler;
import javax.jws.soap.SOAPMessageHandlers;

@Stateless()
@WebService()
@HandlerChain(file="NewWebService.java")
@SOAPMessageHandlers(value=@SOAPMessageHandler())
public class Handlers {
    @WebMethod
    public String operation(){return "";};

}