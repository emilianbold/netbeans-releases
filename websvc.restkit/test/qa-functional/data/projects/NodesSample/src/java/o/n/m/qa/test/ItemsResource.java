/*
 *  ItemsResource
 *
 * Created on February 15, 2008, 8:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package o.n.m.qa.test;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author lukas
 */

@Path("/items")
public class ItemsResource {
    @Context
    private UriInfo context;

    /** Creates a new instance of ItemsResource */
    public ItemsResource() {
    }

    /**
     * Retrieves representation of an instance of o.n.m.qa.test.ItemsResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    public String getXml() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * Sub-resource locator method for  {name}
     */
    @Path("{name}")
    public ItemResource getItemResource() {
        return new ItemResource();
    }
}
