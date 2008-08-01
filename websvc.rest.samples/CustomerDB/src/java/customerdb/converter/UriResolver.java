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

package customerdb.converter;

import javax.ws.rs.WebApplicationException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import javax.xml.bind.JAXBContext;

/**
 * Utility class for resolving an uri into an entity.
 *
 * @author PeterLiu
 */
public class UriResolver {
    
    private static ThreadLocal<UriResolver> instance = new ThreadLocal<UriResolver>() {
        protected UriResolver initialValue() {
            return new UriResolver();
        }
    };
    
    private boolean inProgress = false;
    
    private UriResolver() {
    }
    
    /**
     * Returns an instance of UriResolver.
     *
     * @return an instance of UriResolver.
     */
    public static UriResolver getInstance() {
        return instance.get();
    }
    
    private static void removeInstance() {
        instance.remove();
    }
    
    /**
     * Returns the entity associated with the given uri.
     *
     * @param type the converter class used to unmarshal the entity from XML
     * @param uri the uri identifying the entity
     * @return the entity associated with the given uri
     */
    public <T> T resolve(Class<T> type, URI uri) {
        if (inProgress) return null;
        
        inProgress = true;
        
        try {
            if (uri == null) {
                throw new RuntimeException("No uri specified in a reference.");
            }
            
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() == 200) {
                JAXBContext context = JAXBContext.newInstance(type);
                
                return (T) context.createUnmarshaller().unmarshal(conn.getInputStream());
            } else {
                throw new WebApplicationException(new Throwable("Resource for " + uri + " does not exist."), 404);
            }
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new WebApplicationException(ex);
        } finally {
            removeInstance();
        }
    }
}
