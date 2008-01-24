package hints;

import javax.ejb.Stateless;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebService;

@Stateless()
@WebService()
public class Exceptions {
    @Oneway()
    @WebMethod()
    public void setName() throws Exception {
        String name = "name";
    }
}