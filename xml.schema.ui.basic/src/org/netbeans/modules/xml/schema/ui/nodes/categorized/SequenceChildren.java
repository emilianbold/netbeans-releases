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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.ReadOnlyCookie;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.nodes.Node;

/**
 * Subclass of CategorizedChildren that does not attempt to arrange the
 * order of its children according to their type, as that would be quite
 * wrong for a sequence component.
 *
 * @author Nathan Fiedler
 */
public class SequenceChildren<C extends SchemaComponent>
        extends CategorizedChildren<C> {

    /**
     * Creates a new instance of SequenceChildren.
     *
     * @param  context    schema context.
     * @param  reference  schema component.
     */
    public SequenceChildren(SchemaUIContext context,
            SchemaComponentReference<C> reference) {
        super(context,reference);
    }

    protected List<Node> createKeys() {
        C parentComponent = getReference().get();
        List<Node> keys = new ArrayList<Node>();

        CustomizerProvider provider = (CustomizerProvider) getNode().
                getLookup().lookup(CustomizerProvider.class);
        ReadOnlyCookie roc = (ReadOnlyCookie) getContext().getLookup().lookup(
                ReadOnlyCookie.class);
        if (provider != null && (roc == null || !roc.isReadOnly())) {
            keys.add(new DetailsNode(getContext(),provider));
        }
        List<SchemaComponent> children = parentComponent.getChildren();

        // Create a node for each of the children.
        for (SchemaComponent child : children) {
            Node node = getContext().getFactory().createNode(child);
            keys.add(node);
        }
        return keys;
    }
}
