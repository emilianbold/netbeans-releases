 /*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package demo.rest.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import demo.rest.Message;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import org.glassfish.jersey.client.ClientResponse;

public class MessageBoardClient {

    public MessageBoardClient() {
    }

    public List<Message> getMessages() {
        MessageBoardResourceBean_JerseyClient client = new MessageBoardResourceBean_JerseyClient();
        List<Message> msgs = client.getMessages_XML(Message.class);
        client.close();
        return msgs;
    }

    public void addMessage(Message m) {
        MessageBoardResourceBean_JerseyClient client = new MessageBoardResourceBean_JerseyClient();
        client.addMessage(m);
        client.close();
    }

    public void deleteMessage(Message m) {
        MessageBoardResourceBean_JerseyClient client = new MessageBoardResourceBean_JerseyClient();
        client.deleteMessage(String.valueOf(m.getUniqueId()));
        client.close();
    }

    public void updateMessage(Message m) {
        MessageBoardResourceBean_JerseyClient client = new MessageBoardResourceBean_JerseyClient();
        client.deleteMessage(String.valueOf(m.getUniqueId()));
        client.addMessage(m);
        client.close();
    }

    private static class MessageBoardResourceBean_JerseyClient {

        private final WebTarget webTarget;
        private final Client client;
        private static final String BASE_URI = "http://localhost:8080/message-board/app"; //NOI18N

        MessageBoardResourceBean_JerseyClient() {
            client = ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("messages"); //NOI18N
        }

        public void deleteMessage(String msgNum) throws ClientErrorException {
            webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{msgNum})).request().delete();
        }

        public <T> T getMessage_XML(Class<T> responseType, String msgNum) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{msgNum})).
                    request(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
        }

        public <T> T getMessage_HTML(Class<T> responseType, String msgNum) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{msgNum})).
                    request(javax.ws.rs.core.MediaType.TEXT_HTML).get(responseType);
        }

        public ClientResponse addMessage(Object requestEntity) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_XML).
                    post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.TEXT_PLAIN), ClientResponse.class);
        }

        public <T> List<T> getMessages_XML(final Class<T> responseType) throws ClientErrorException {
            GenericType<List<T>> type = new GenericType<List<T>>(new PType(responseType)) {};
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_XML).get(type);
        }

        public <T> List<T> getMessages_HTML(Class<T> responseType) throws ClientErrorException {
            GenericType<List<T>> type = new GenericType<List<T>>(new PType(responseType)) {};
            return webTarget.request(javax.ws.rs.core.MediaType.TEXT_HTML).get(type);
        }

        public void close() {
            client.close();
        }
    }

    private static class PType implements ParameterizedType {

        private final Class<?> responseType;

        PType(Class<?> responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{responseType};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return List.class;
        }
    }
}
