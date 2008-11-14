/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.services.strikeiron;

import com.strikeiron.search.AUTHENTICATIONSTYLE;
import com.strikeiron.search.SORTBY;
import com.strikeiron.search.SearchOutPut;
import com.strikeiron.search.SearchResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Employ the StrikeIron Marketplace Search web service using the RESTful
 * interface to avoid dependencies on JAX-WS which is not available here.
 *
 * @author  Nathan Fiedler
 */
public class StrikeIronSearch {
    private static final String BASE_URL =
            "http://ws.strikeiron.com/StrikeIron/MarketplaceSearch/SISearchService/Search";

    /**
     * Invoke the StrikeIron marketplace search web service with the given
     * parameter values and return the results.
     *
     * @param userId         registered user identifier.
     * @param passwd         registered user password.
     * @param term           the search term.
     * @param sortBy         how to sort the results.
     * @param useCustomWsdl  whether to use a custom wSDL or not.
     * @param authStyle      which authentication style to accept in results.
     * @return the search output.
     * @throws StrikeIronSearch.SearchException
     *         if anything goes wrong (get nested cause for details).
     */
    public static SearchOutPut search(String userId, String passwd,
            String term, SORTBY sortBy, Boolean useCustomWsdl,
            AUTHENTICATIONSTYLE authStyle) throws SearchException {
        String uri = String.format("%s?LicenseInfo.RegisteredUser.UserID=%s" +
                "&LicenseInfo.RegisteredUser.Password=%s" +
                "&Search.SearchTerm=%s" +
                "&Search.SortBy=%s" +
                "&Search.UseCustomWSDL=%s" +
                "&Search.AuthenticationStyle=%s", BASE_URL, userId, passwd,
                term, sortBy.value(), useCustomWsdl, authStyle.value());
        try {
            // Wanted to avoid using DOM, but how else to skip the root
            // element which JAXB is not expecting?
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(uri);
            NodeList list = doc.getElementsByTagName("SearchResponse");
            if (list != null && list.getLength() > 0) {
                Node node = list.item(0);
                // Now that we have the desired element, delegate to JAXB.
                JAXBContext jc = JAXBContext.newInstance("com.strikeiron.search");
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                SearchResponse resp = (SearchResponse) unmarshaller.unmarshal(node);
                return resp.getSearchResult();
            }
        } catch (JAXBException jbe) {
            throw new SearchException(jbe);
        } catch (MalformedURLException mue) {
            throw new SearchException(mue);
        } catch (ParserConfigurationException pce) {
            throw new SearchException(pce);
        } catch (SAXException se) {
            throw new SearchException(se);
        } catch (IOException ioe) {
            throw new SearchException(ioe);
        }
        return null;
    }

    /**
     * A wrapper around the various exceptions that can occur when invoking
     * the StrikeIron search web service. Get the nested cause exception for
     * the pertinent details on the error.
     */
    public static class SearchException extends Exception {
        private static final long serialVersionUID = 1L;

        public SearchException(Throwable cause) {
            super(cause);
        }
    }
}
