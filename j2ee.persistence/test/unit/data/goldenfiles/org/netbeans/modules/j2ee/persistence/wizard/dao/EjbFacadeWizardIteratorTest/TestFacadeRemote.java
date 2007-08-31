package foo;

import java.util.List;
import javax.ejb.Remote;

@Remote
public interface TestFacadeRemote {

    void create(Test test);

    void edit(Test test);

    void remove(Test test);

    Test find(Object id);

    List findAll();
}