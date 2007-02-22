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

import org.openide.nodes.Children;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.NewTypesFactory;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.schema.SchemaNode;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AdvancedSchemaNode extends SchemaNode {

    public AdvancedSchemaNode(SchemaUIContext context,
            SchemaComponentReference<Schema> reference,
            Children children) {
        super(context,reference,children);
    }

    protected NewTypesFactory getNewTypesFactory() {
        return new AdvancedNewTypesFactory();
    }

    public int getChildCount() {
        int count = 0;
        for (Class<? extends SchemaComponent> cls :
            CategorizedChildren.UNCATEGORIZED_TYPES) {
            count += getReference().get().getChildren(cls).size();
        }
        return count;
    }

    protected boolean allowReordering() {
        // Disallow reordering of our category children, as they cannot
        // be in any particular order anyway.
        return false;
    }
}
