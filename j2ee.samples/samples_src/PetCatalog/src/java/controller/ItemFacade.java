/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.Serializable;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Item;

/**
 *
 * @author caroljmcdonald
 */
@Stateless
public class ItemFacade implements Serializable {

    @PersistenceContext(unitName = "catalogPU")
    private EntityManager em;

    public void create(Item item) {
        em.persist(item);
    }

    public void edit(Item item) {
        em.merge(item);
    }

    public void remove(Item item) {
        em.remove(em.merge(item));
    }

    public Item find(Object id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select object(o) from Item as o").getResultList();
    }

    public List<Item> findRange(int maxResults, int firstResult) {
        Query q = em.createQuery("select object(o) from Item as o");
        q.setMaxResults(maxResults);
        q.setFirstResult(firstResult);
        return q.getResultList();
    }

    public int getItemCount() {
        return ((Long) em.createQuery("select count(o) from Item as o").getSingleResult()).intValue();
    }
}
