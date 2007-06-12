package generator;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService()
public class ParamSetterTestService {
    @WebMethod()
    public String hi(@WebParam() String s) {
        return "Hi "+s;
    }
}
