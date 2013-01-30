/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.navigator;

import java.util.Locale;
import java.util.StringTokenizer;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.nodes.Node;

/**
 *
 * @author marekfukala
 */
public class Utils {

    private static final char ELEMENT_PATH_ELEMENTS_DELIMITER = '/';
    private static final char ELEMENT_PATH_INDEX_DELIMITER = '|';
    
    private static final String STRIPPED_TEXT_POSTFIX = "..."; //NOI18N
    private static final int STRIPPED_TEXT_LEN = 30; //NOI18N
    

    private Utils() {
    }

    /**
     * Tries to find (to resolve) a corresponding  node to the given 
     * base node and the {@link HtmlElementDescription}. 
     * 
     * The passed {@link Node} must contain instance of web kit node in its lookup.
     * 
     * To do this it uses a path from the root 
     * node the represented element held by each {@link Description}.
     * 
     * @param base Base node, typically the root node
     * @param description The element description to resolve 
     * 
     * @return corresponding node or null if no matching element found.
     */
    public static Node findNode(Node base, Description description) {
        String path = description.getElementPath();
        StringTokenizer st = new StringTokenizer(path, Character.toString(ELEMENT_PATH_ELEMENTS_DELIMITER));
        Node found = base;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int indexDelim = token.indexOf(ELEMENT_PATH_INDEX_DELIMITER);

            String nodeName = indexDelim >= 0 ? token.substring(0, indexDelim) : token;
            nodeName = nodeName.toLowerCase(Locale.ENGLISH);

            String sindex = indexDelim >= 0 ? token.substring(indexDelim + 1, token.length()) : "0";
            int index = Integer.parseInt(sindex);

            int count = 0;
            Node foundLocal = null;


            for (Node child : found.getChildren().getNodes()) {
                if (LexerUtils.equals(getWebKitNodeName(getWebKitNode(child)), nodeName, true, false) && count++ == index) {
                    foundLocal = child;
                    break;
                }
            }

            if (foundLocal != null) {
                found = foundLocal;

                if (!st.hasMoreTokens()) {
                    //last token, we may return
                    return found;
                }

            } else {
                return null; //not found
            }
        }

        return null;
    }
    
    /**
     * Finds an instance of webkit Node in the given openide {@link Node}. 
     * It tries to get it from the given {@link Node} lookup.
     
     * @param node
     * @return instance of web kit Node or null if the given node doesn't contain it in its lookup.
     */
    public static org.netbeans.modules.web.webkit.debugging.api.dom.Node getWebKitNode(Node node) {
        return node.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
    }
    
    /**
     * Returns a system name for given web kit Node.
     * 
     * @param node webkit node
     */
    public static String getWebKitNodeName(org.netbeans.modules.web.webkit.debugging.api.dom.Node node) {
        switch(node.getNodeType()) {
            case org.w3c.dom.Node.ELEMENT_NODE:
                return node.getNodeName().toLowerCase();
            case org.w3c.dom.Node.DOCUMENT_NODE:
                return "html";
            default:
                return strip(node.getNodeValue().trim(), STRIPPED_TEXT_LEN);
        }
    }
    
    private static String strip(String text, int maxLen) {
        if(text.length() <= maxLen) {
            return text;
        }
        return new StringBuilder(text.substring(0, maxLen).trim()).append(STRIPPED_TEXT_POSTFIX).toString();
    }
}
