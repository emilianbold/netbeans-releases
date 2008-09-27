/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package customerdb.service;

import customerdb.Customer;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import com.sun.jersey.api.core.ResourceContext;
import javax.ws.rs.WebApplicationException;
import javax.persistence.NoResultException;
import javax.persistence.EntityManager;
import customerdb.DiscountCode;
import customerdb.converter.CustomerConverter;
import javax.persistence.PersistenceContext;
import com.sun.jersey.api.spring.Autowire;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author srividhyanarayanan
 */

@Autowire
public class CustomerResource {
    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    @PersistenceContext(unitName = "CustomerDBSpringPU")
    protected EntityManager em;
    protected Integer id;
  
    /** Creates a new instance of CustomerResource */
    public CustomerResource() {
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get method for retrieving an instance of Customer identified by id in XML format.
     *
     * @param id identifier for the entity
     * @return an instance of CustomerConverter
     */
    @GET
    @Produces({"application/xml", "application/json"})
    @Transactional
    public CustomerConverter get(@QueryParam("expandLevel")
    @DefaultValue("1")
    int expandLevel) {
        return new CustomerConverter(getEntity(), uriInfo.getAbsolutePath(), expandLevel);
    }

    /**
     * Put method for updating an instance of Customer identified by id using XML as the input format.
     *
     * @param id identifier for the entity
     * @param data an CustomerConverter entity that is deserialized from a XML stream
     */
    @PUT
    @Consumes({"application/xml", "application/json"})
    @Transactional
    public void put(CustomerConverter data) {
        updateEntity(getEntity(), data.resolveEntity(em));
    }

    /**
     * Delete method for deleting an instance of Customer identified by id.
     *
     * @param id identifier for the entity
     */
    @DELETE
    @Transactional
    public void delete() {
        deleteEntity(getEntity());
    }

    /**
     * Returns an instance of Customer identified by id.
     *
     * @param id identifier for the entity
     * @return an instance of Customer
     */
    protected Customer getEntity() {
        try {
            return (Customer) em.createQuery("SELECT e FROM Customer e where e.customerId = :customerId").setParameter("customerId", id).getSingleResult();
        } catch (NoResultException ex) {
            throw new WebApplicationException(new Throwable("Resource for " + uriInfo.getAbsolutePath() + " does not exist."), 404);
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
        DiscountCode discountCode = entity.getDiscountCode();
        DiscountCode discountCodeNew = newEntity.getDiscountCode();
        entity = em.merge(newEntity);
        if (discountCode != null && !discountCode.equals(discountCodeNew)) {
            discountCode.getCustomerCollection().remove(entity);
        }
        if (discountCodeNew != null && !discountCodeNew.equals(discountCode)) {
            discountCodeNew.getCustomerCollection().add(entity);
        }
        return entity;
    }

    /**
     * Deletes the entity.
     *
     * @param entity the entity to deletle
     */
    protected void deleteEntity(Customer entity) {
        DiscountCode discountCode = entity.getDiscountCode();
        if (discountCode != null) {
            discountCode.getCustomerCollection().remove(entity);
        }
        em.remove(entity);
    }

    /**
     * Returns a dynamic instance of DiscountCodeResource used for entity navigation.
     *
     * @param id identifier for the parent entity
     * @return an instance of DiscountCodeResource
     */
    @Path("discountCode/")
    @Transactional
    public DiscountCodeResource getDiscountCodeResource() {
        DiscountCodeResourceSub resource = resourceContext.getResource(DiscountCodeResourceSub.class);
        resource.setParent(getEntity());
        return resource;
    }

    public static class DiscountCodeResourceSub extends DiscountCodeResource {

        private Customer parent;

        public void setParent(Customer parent) {
            this.parent = parent;
        }

        @Override
        protected DiscountCode getEntity() {
            DiscountCode entity = parent.getDiscountCode();
            if (entity == null) {
                throw new WebApplicationException(new Throwable("Resource for " + uriInfo.getAbsolutePath() + " does not exist."), 404);
            }
            return entity;
        }
    }
}
