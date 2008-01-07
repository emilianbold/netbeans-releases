package hints;

import javax.ejb.Stateless;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;

@Stateless()
@WebService()
public class IOParametrs {
    @WebMethod(operationName="setName")
    public void setName(@WebParam(name="name", mode=Mode.INOUT) String name) {
        String personName = name;
    }

    @Oneway()
    @WebMethod(operationName="setAge")
    public void setAge(@WebParam(name="age", mode=Mode.OUT) int age) {
        int personAge = age;
    }

    @Oneway()
    @WebMethod(operationName="setSurname")
    public void setSurname(@WebParam(name="surname", mode=Mode.IN) String surname) {
        String personSurname = surname;
    }
}