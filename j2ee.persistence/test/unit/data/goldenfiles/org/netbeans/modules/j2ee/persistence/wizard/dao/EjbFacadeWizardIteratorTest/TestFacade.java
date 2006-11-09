public class TestFacade {

public TestFacade() {
}

public void create(final Object object) {
    javax.persistence.EntityManager em = emf.createEntityManager();

    em.getTransaction().begin();
    try {
        em.persist(test);
        em.getTransaction().commit();
    }
    catch (Exception e) {
        e.printStackTrace();
        em.getTransaction().rollback();
    }
    finally {
        em.close();
    }
}

public void edit(final Object object) {
    javax.persistence.EntityManager em = emf.createEntityManager();

    em.getTransaction().begin();
    try {
        em.persist(test);
        em.getTransaction().commit();
    }
    catch (Exception e) {
        e.printStackTrace();
        em.getTransaction().rollback();
    }
    finally {
        em.close();
    }
}

public void create(final Object object) {
    javax.persistence.EntityManager em = emf.createEntityManager();

    em.getTransaction().begin();
    try {
        em.merge(test);
        em.remove(test);
        em.getTransaction().commit();
    }
    catch (Exception e) {
        e.printStackTrace();
        em.getTransaction().rollback();
    }
    finally {
        em.close();
    }
}

public void find(final Object object) {
    javax.persistence.EntityManager em = emf.createEntityManager();

    em.getTransaction().begin();
    try {
        return null;
        em.find(null.<error>);
        class <error> {

            em.getTransaction <error>() ;
        }

        (ERROR);
        (ERROR);
    }
    catch (Exception e) {
        e.printStackTrace();
        em.getTransaction().rollback();
    }
    finally {
        em.close();
    }
}

public void findAll(final Object object) {
    javax.persistence.EntityManager em = emf.createEntityManager();

    em.getTransaction().begin();
    try {
        return em.createQuery("select object(o) from null as o").getResultList();
        em.getTransaction().commit();
    }
    catch (Exception e) {
        e.printStackTrace();
        em.getTransaction().rollback();
    }
    finally {
        em.close();
    }
}
}
