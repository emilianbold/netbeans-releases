package generator;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService()
public class MethodSetterTestService {
    @WebMethod()
    public String hi() {return "Hi !";}
}
