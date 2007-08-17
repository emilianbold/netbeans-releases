package foo;

import javax.ejb.Local;

@Local
public interface TestFacadeLocal {

    void create(Test test);

    void edit(Test test);

    void remove(Test test);

    Test find(Object id);

    java.util.List findAll();
}