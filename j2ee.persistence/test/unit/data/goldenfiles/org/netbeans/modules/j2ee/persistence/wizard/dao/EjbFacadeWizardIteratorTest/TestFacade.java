package foo;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class TestFacade implements TestFacadeLocal, TestFacadeRemote {
    @PersistenceContext
    private EntityManager em;

    public void create(Test test) {
        em.persist(test);
    }

    public void edit(Test test) {
        em.merge(test);
    }

    public void remove(Test test) {
        em.remove(em.merge(test));
    }

    public Test find(Object id) {
        return em.find(Test.class, id);
    }

    public List findAll() {
        return em.createQuery("select object(o) from Test as o").getResultList();
    }
}