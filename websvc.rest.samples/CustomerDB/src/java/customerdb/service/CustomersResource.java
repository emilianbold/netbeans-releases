/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package customerdb.service;

import customerdb.Customer;
import java.util.Collection;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Builder;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import customerdb.converter.CustomersConverter;
import customerdb.converter.CustomerConverter;

/**
 *
 * @author Peter Liu
 */

@UriTemplate("/customers/")
public class CustomersResource {
    @HttpContext
    private UriInfo context;
    
    /** Creates a new instance of CustomersResource */
    public CustomersResource() {
    }

    /**
     * Constructor used for instantiating an instance of dynamic resource.
     *
     * @param context HttpContext inherited from the parent resource
     */
    public CustomersResource(UriInfo context) {
        this.context = context;
    }

    /**
     * Get method for retrieving a collection of Customer instance in XML format.
     *
     * @return an instance of CustomersConverter
     */
    @HttpMethod("GET")
    @ProduceMime({"application/xml", "application/json"})
    public CustomersConverter get() {
        try {
            return new CustomersConverter(getEntities(), context.getURI());
        } finally {
            PersistenceService.getInstance().close();
        }
    }

    /**
     * Post method for creating an instance of Customer using XML as the input format.
     *
     * @param data an CustomerConverter entity that is deserialized from an XML stream
     * @return an instance of CustomerConverter
     */
    @HttpMethod("POST")
    @ConsumeMime({"application/xml", "application/json"})
    public Response post(CustomerConverter data) {
        PersistenceService service = PersistenceService.getInstance();
        try {
            service.beginTx();
            Customer entity = data.getEntity();
            createEntity(entity);
            service.commitTx();
            return Builder.created(context.getURI().resolve(entity.getCustomerId() + "/")).build();
        } finally {
            service.close();
        }
    }

    /**
     * Returns a dynamic instance of CustomerResource used for entity navigation.
     *
     * @return an instance of CustomerResource
     */
    @UriTemplate("{customerId}/")
    public customerdb.service.CustomerResource getCustomerResource() {
        return new CustomerResource(context);
    }

    /**
     * Returns all the entities associated with this resource.
     *
     * @return a collection of Customer instances
     */
    protected Collection<Customer> getEntities() {
        return PersistenceService.getInstance().createQuery("SELECT e FROM Customer e").getResultList();
    }

    /**
     * Persist the given entity.
     *
     * @param entity the entity to persist
     */
    protected void createEntity(Customer entity) {
        PersistenceService.getInstance().persistEntity(entity);
    }
}
