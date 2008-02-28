/*
 *  ItemsResource
 *
 * Created on February 15, 2008, 8:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package o.n.m.qa.test;

import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.GET;

/**
 * REST Web Service
 *
 * @author lukas
 */

@Path("/items")
public class ItemsResource {
    @HttpContext
    private UriInfo context;

    /** Creates a new instance of ItemsResource */
    public ItemsResource() {
    }

    /**
     * Retrieves representation of an instance of o.n.m.qa.test.ItemsResource
     * @return an instance of java.lang.String
     */
    @GET
    @ProduceMime("application/xml")
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
