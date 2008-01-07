package hints;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;

@Stateless()
@WebService(serviceName="service")
public interface ServiceName {
        @WebMethod
    public String operation();

    
}