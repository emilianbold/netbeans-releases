package generator;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService()
public class ParamSetterTestService_1 {
    @WebMethod()
    public String hi(String s) {
        return "Hi "+s;
    }
}
