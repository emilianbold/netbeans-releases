/*
 *  HelloWorldResource
 *
 * Created on August 17, 2007, 4:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package helloworld;

import javax.ws.rs.UriTemplate;
import javax.ws.rs.UriParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;

/**
 * REST Web Service
 *
 * @author Peter Liu
 */

@UriTemplate("/helloWorld")
public class HelloWorldResource {
    @HttpContext
    private UriInfo context;
    
    /** Creates a new instance of HelloWorldResource */
    public HelloWorldResource() {
    }

    /**
     * Retrieves representation of an instance of helloworld.HelloWorldResource
     * @return an instance of java.lang.String
     */
    @HttpMethod("GET")
    @ProduceMime("application/xml")
    public String getXml() {
        return "Hello World!";
    }

    /**
     * PUT method for updating or creating an instance of HelloWorldResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @HttpMethod("PUT")
    @ConsumeMime("application/xml")
    public void putXml(String content) {
    }
}
