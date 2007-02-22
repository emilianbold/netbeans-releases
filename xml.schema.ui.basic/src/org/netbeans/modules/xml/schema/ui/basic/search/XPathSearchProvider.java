/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
