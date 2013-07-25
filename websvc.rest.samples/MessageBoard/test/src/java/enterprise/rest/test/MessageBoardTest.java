/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package enterprise.rest.test;

import enterprise.messageboard.entities.Message;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;
import static org.junit.Assert.*;

public class MessageBoardTest {

    private WebTarget webTarget;
    
    public MessageBoardTest() {
        URI serverUri;
        try {
            if(System.getProperty("samples.javaee.serveruri") != null) {
                serverUri = new URI(System.getProperty("samples.javaee.serveruri"));
            } else {
                serverUri = new URI("http://localhost:8080");
            }
            Client client = ClientBuilder.newClient();
            webTarget = client.target(serverUri).path("message-board/app/messages");

        } catch(URISyntaxException e) {
            assertTrue(false);
        }
    }

    @Test public void testDeployed() {
        
        // test GET [app/messages]
        String result = webTarget.request().get(String.class);
        assertFalse(result.length() == 0);
    }

    @Test public void testPostGetDeleteMessage() {
        
        // test POST [app/messages]
        Response response = webTarget.request().post(Entity.entity("Hello World !!!", MediaType.TEXT_PLAIN), Response.class);
        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        
        // test GET [app/messages/{number}]
        URI location = response.getLocation();
        String resourceNum = location.getPath().substring(location.getPath().lastIndexOf("/")+1);
        Message message = webTarget.path(resourceNum).request(MediaType.APPLICATION_XML).get(Message.class);
        assertEquals("Hello World !!!", message.getMessage());
        
        // test DELETE [app/messages/{number}]
        response = webTarget.path(resourceNum).request().delete();
        assertNotSame(Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}

