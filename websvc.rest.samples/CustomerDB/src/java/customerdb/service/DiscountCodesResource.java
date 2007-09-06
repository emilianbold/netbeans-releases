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

import customerdb.DiscountCode;
import java.util.Collection;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Builder;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import customerdb.Customer;
import customerdb.converter.DiscountCodesConverter;
import customerdb.converter.DiscountCodeConverter;

/**
 *
 * @author Peter Liu
 */

@UriTemplate("/discountCodes/")
public class DiscountCodesResource {
    @HttpContext
    private UriInfo context;
    
    /** Creates a new instance of DiscountCodesResource */
    public DiscountCodesResource() {
    }

    /**
     * Constructor used for instantiating an instance of dynamic resource.
     *
     * @param context HttpContext inherited from the parent resource
     */
    public DiscountCodesResource(UriInfo context) {
        this.context = context;
    }

    /**
     * Get method for retrieving a collection of DiscountCode instance in XML format.
     *
     * @return an instance of DiscountCodesConverter
     */
    @HttpMethod("GET")
    @ProduceMime({"application/xml", "application/json"})
    public DiscountCodesConverter get() {
        try {
            return new DiscountCodesConverter(getEntities(), context.getURI());
        } finally {
            PersistenceService.getInstance().close();
        }
    }

    /**
     * Post method for creating an instance of DiscountCode using XML as the input format.
     *
     * @param data an DiscountCodeConverter entity that is deserialized from an XML stream
     * @return an instance of DiscountCodeConverter
     */
    @HttpMethod("POST")
    @ConsumeMime({"application/xml", "application/json"})
    public Response post(DiscountCodeConverter data) {
        PersistenceService service = PersistenceService.getInstance();
        try {
            service.beginTx();
            DiscountCode entity = data.getEntity();
            createEntity(entity);
            service.commitTx();
            return Builder.created(context.getURI().resolve(entity.getDiscountCode() + "/")).build();
        } finally {
            service.close();
        }
    }

    /**
     * Returns a dynamic instance of DiscountCodeResource used for entity navigation.
     *
     * @return an instance of DiscountCodeResource
     */
    @UriTemplate("{discountCode}/")
    public customerdb.service.DiscountCodeResource getDiscountCodeResource() {
        return new DiscountCodeResource(context);
    }

    /**
     * Returns all the entities associated with this resource.
     *
     * @return a collection of DiscountCode instances
     */
    protected Collection<DiscountCode> getEntities() {
        return PersistenceService.getInstance().createQuery("SELECT e FROM DiscountCode e").getResultList();
    }

    /**
     * Persist the given entity.
     *
     * @param entity the entity to persist
     */
    protected void createEntity(DiscountCode entity) {
        PersistenceService.getInstance().persistEntity(entity);
        for (Customer value : entity.getCustomerCollection()) {
            value.setDiscountCode(entity);
        }
    }
}
