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
import java.util.Collection;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import com.sun.jersey.api.core.ResourceContext;
import javax.persistence.EntityManager;
import customerdb.DiscountCode;
import customerdb.converter.CustomersConverter;
import customerdb.converter.CustomerConverter;
import javax.persistence.PersistenceContext;
import com.sun.jersey.spi.resource.Singleton;
import com.sun.jersey.api.spring.Autowire;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author srividhyanarayanan
 */

@Path("/customers/")
@Singleton
@Autowire
public class CustomersResource {
    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    @PersistenceContext(unitName = "CustomerDBSpringPU")
    protected EntityManager em;
  
    /** Creates a new instance of CustomersResource */
    public CustomersResource() {
    }

    /**
     * Get method for retrieving a collection of Customer instance in XML format.
     *
     * @return an instance of CustomersConverter
     */
    @GET
    @Produces({"application/xml", "application/json"})
    @Transactional
    public CustomersConverter get(@QueryParam("start")
    @DefaultValue("0")
    int start, @QueryParam("max")
    @DefaultValue("10")
    int max, @QueryParam("expandLevel")
    @DefaultValue("1")
    int expandLevel, @QueryParam("query")
    @DefaultValue("SELECT e FROM Customer e")
    String query) {
        return new CustomersConverter(getEntities(start, max, query), uriInfo.getAbsolutePath(), expandLevel);
    }

    /**
     * Post method for creating an instance of Customer using XML as the input format.
     *
     * @param data an CustomerConverter entity that is deserialized from an XML stream
     * @return an instance of CustomerConverter
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    @Transactional
    public Response post(CustomerConverter data) {
        Customer entity = data.resolveEntity(em);
        createEntity(data.resolveEntity(em));
        return Response.created(uriInfo.getAbsolutePath().resolve(entity.getCustomerId() + "/")).build();
    }

    /**
     * Returns a dynamic instance of CustomerResource used for entity navigation.
     *
     * @return an instance of CustomerResource
     */
    @Path("{customerId}/")
    public CustomerResource getCustomerResource(@PathParam("customerId")
    Integer id) {
        CustomerResource resource = resourceContext.getResource(CustomerResource.class);
        resource.setId(id);
        return resource;
    }

    /**
     * Returns all the entities associated with this resource.
     *
     * @return a collection of Customer instances
     */
    protected Collection<Customer> getEntities(int start, int max, String query) {
        return em.createQuery(query).setFirstResult(start).setMaxResults(max).getResultList();
    }

    /**
     * Persist the given entity.
     *
     * @param entity the entity to persist
     */
    protected void createEntity(Customer entity) {
        em.persist(entity);
        DiscountCode discountCode = entity.getDiscountCode();
        if (discountCode != null) {
            discountCode.getCustomerCollection().add(entity);
        }
    }
}
