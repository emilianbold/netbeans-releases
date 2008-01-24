package hints;

import javax.ejb.Stateless;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebService;

@Stateless()
@WebService()
public class ReturnValue{
    @Oneway()
    @WebMethod
    public String getName() {
        return "name";
    }
}