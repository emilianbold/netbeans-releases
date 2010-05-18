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

package org.netbeans.modules.xml.schema.ui.basic.search;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xam.ui.search.Query;
import org.netbeans.modules.xml.xam.ui.search.SearchException;
import org.netbeans.modules.xml.xam.ui.search.SearchProvider;
import org.netbeans.modules.xml.xdm.visitor.XPathFinder;
import org.openide.util.NbBundle;

/**
 * Implements a SearchProvider that finds components matching an XML Path.
 *
 * @author Nathan Fiedler
 */
public class XPathSearchProvider implements SearchProvider {
    /** Model in which to perform the search. */
    private SchemaModel model;
    /** List of matching schema components. */
    private List<Object> results;

    /**
     * Creates a new instance of XPathSearchProvider.
     *
     * @param  model  schema model in which to perform search.
     */
    public XPathSearchProvider(SchemaModel model) {
        this.model = model;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(XPathSearchProvider.class,
                "LBL_SearchProvider_XPath");
    }

    public String getInputDescription() {
        return NbBundle.getMessage(XPathSearchProvider.class,
                "HELP_SearchProvider_XPath");
    }

    public String getShortDescription() {
        return NbBundle.getMessage(XPathSearchProvider.class,
                "HINT_SearchProvider_XPath");
    }

    public List<Object> search(Query query) throws SearchException {
        results = new ArrayList<Object>();
        Schema schema = model.getSchema();
        org.w3c.dom.Document document = model.getDocument();
        if (document instanceof Document) {
            XPathFinder xfinder = new XPathFinder();
            try {
                List<Node> nodes = xfinder.findNodes((Document) document,
                        query.getQuery());
                FindSchemaComponentFromDOM dfinder = new FindSchemaComponentFromDOM();
                for (Node node : nodes) {
                    if (node instanceof Element) {
                        SchemaComponent comp = dfinder.findComponent(schema,
                                (Element) node);
                        results.add(comp);
                    }
                    // Else, Attr is not connected to the DOM tree, so no
                    // way to find the path from the root.
                }
            } catch (RuntimeException re) {
                // Some expressions (e.g. /) lead to an exception.
                // Ignore them and indicate a failed search.
            }
        }
        return results;
    }
}
