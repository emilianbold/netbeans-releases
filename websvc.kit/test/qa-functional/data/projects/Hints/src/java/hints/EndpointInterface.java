package hints;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;

@Stateless()
@WebService(endpointInterface="test.NewWebService")
public interface EndpointInterface {
    @WebMethod
    public String operation();
    
}
