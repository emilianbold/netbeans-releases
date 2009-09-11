/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.abien.business.orderprocessor.boundary;

import com.abien.business.orderprocessor.entity.Order;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/** Implement context resolver to force json arrays to be represented as arrays even for single element array.
 * Example:
 * single element array would normally be represented as single elements, e.g.:
 * "{"order":{"id":"3","amount":"30","productId":"3","customer":"PUMA","paid":"false","delivered":"false"}}"
 * but in JavaScript we want to use this format:
 * "{"order":[{"id":"3","amount":"30","productId":"3","customer":"PUMA","paid":"false","delivered":"false"}]}"
 *
 * @author mkuchtiak
 */
@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;
    private Class[] types = {Order.class};

    public JAXBContextResolver() throws Exception {
     this.context =
         new JSONJAXBContext(
             JSONConfiguration.mapped().arrays("order").build(),
             types);
    }

    public JAXBContext getContext(Class<?> objectType) {
        for (Class type : types) {
            if (type == objectType) {
                return context;
            }
        }
        return null;
    }
}
