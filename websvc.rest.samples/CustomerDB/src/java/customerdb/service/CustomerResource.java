/*
 *  CustomerResource
 *
 * Created on August 17, 2007, 4:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package customerdb.service;

import customerdb.Customer;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.UriParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.WebApplicationException;
import javax.persistence.NoResultException;
import customerdb.DiscountCode;
import customerdb.converter.CustomerConverter;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Peter Liu
 */

public class CustomerResource {
    private UriInfo context;
    
    /** Creates a new instance of CustomerResource */
    public CustomerResource() {
    }

    /**
     * Constructor used for instantiating an instance of dynamic resource.
     *
     * @param context HttpContext inherited from the parent resource
     */
    public CustomerResource(UriInfo context) {
        this.context = context;
    }

    /**
     * Get method for retrieving an instance of Customer identified by id in XML format.
     *
     * @param id identifier for the entity
     * @return an instance of CustomerConverter
     */
    @HttpMethod("GET")
    @ProduceMime({"application/xml", "application/json"})
    public CustomerConverter get(@UriParam("customerId")
    Integer id) {
        try {
            return new CustomerConverter(getEntity(id), context.getURI());
        } finally {
            PersistenceService.getInstance().close();
        }
    }

    /**
     * Put method for updating an instance of Customer identified by id using XML as the input format.
     *
     * @param id identifier for the entity
     * @param data an CustomerConverter entity that is deserialized from a XML stream
     */
    @HttpMethod("PUT")
    @ConsumeMime({"application/xml", "application/json"})
    public void put(@UriParam("customerId")
    Integer id, CustomerConverter data) {
        PersistenceService service = PersistenceService.getInstance();
        try {
            service.beginTx();
            updateEntity(getEntity(id), data.getEntity());
            service.commitTx();
        } finally {
            service.close();
        }
    }

    /**
     * Delete method for deleting an instance of Customer identified by id.
     *
     * @param id identifier for the entity
     */
    @HttpMethod("DELETE")
    public void delete(@UriParam("customerId")
    Integer id) {
        PersistenceService service = PersistenceService.getInstance();
        try {
            service.beginTx();
            Customer entity = getEntity(id);
            service.removeEntity(entity);
            service.commitTx();
        } finally {
            service.close();
        }
    }

    /**
     * Returns a dynamic instance of DiscountCodeResource used for entity navigation.
     *
     * @param id identifier for the parent entity
     * @return an instance of DiscountCodeResource
     */
    @UriTemplate("discountCode/")
    public customerdb.service.DiscountCodeResource getDiscountCodeResource(@UriParam("customerId")
    Integer id) {
        final Customer parent = getEntity(id);
        return new DiscountCodeResource(context) {

            @Override
            protected DiscountCode getEntity(String id) {
                DiscountCode entity = parent.getDiscountCode();
                if (entity == null) {
                    throw new WebApplicationException(new Throwable("Resource for " + context.getURI() + " does not exist."), 404);
                }
                return entity;
            }
        };
    }

    /**
     * Returns an instance of Customer identified by id.
     *
     * @param id identifier for the entity
     * @return an instance of Customer
     */
    protected Customer getEntity(Integer id) {
        try {
            return (Customer) PersistenceService.getInstance().createNamedQuery("Customer.findByCustomerId").setParameter("customerId", id).getSingleResult();
        } catch (NoResultException ex) {
            throw new WebApplicationException(new Throwable("Resource for " + context.getURI() + " does not exist."), 404);
        }
    }

    /**
     * Updates entity using data from newEntity.
     *
     * @param entity the entity to update
     * @param newEntity the entity containing the new data
     * @return the updated entity
     */
    protected Customer updateEntity(Customer entity, Customer newEntity) {
        newEntity.setCustomerId(entity.getCustomerId());
        entity = PersistenceService.getInstance().mergeEntity(newEntity);
        return entity;
    }
}
