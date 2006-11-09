@javax.ejb.Local
public interface TestFacadeLocal {

void create(Test test) ;

void edit(Test test) ;

void destroy(Test test) ;

void find(Test test) ;

void findAll(Test test) ;
}