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

package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyGroup;
import org.netbeans.modules.sql.framework.ui.editor.property.ITemplate;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class PropertyNode extends AbstractNode {

    private ArrayList propertySets = new ArrayList();
    private ITemplate template;

    /** Creates a new instance of PropertyNode */
    public PropertyNode() {
        super(Children.LEAF);
    }

    public PropertyNode(ITemplate template) {
        this();
        this.setName(template.getName());
        this.setDisplayName(template.getName());

        this.template = template;
        initializePropertySheet();
    }

    public IPropertyGroup getPropertyGroup(String groupName) {
        return template.getPropertyGroup(groupName);
    }

    public Node.PropertySet[] getPropertySets() {
        Node.PropertySet[] typePropertySet = new Node.PropertySet[propertySets.size()];
        return (Node.PropertySet[]) propertySets.toArray(typePropertySet);
    }

    void initializePropertySheet() {
        List list = template.getPropertyGroupList();
        Collections.sort(list);

        Iterator it = list.iterator();

        while (it.hasNext()) {
            IPropertyGroup pg = (IPropertyGroup) it.next();

            List properties = pg.getProperties();
            Collections.sort(properties);

            Sheet.Set set = new Sheet.Set();
            set.setName(pg.getName());
            set.setDisplayName(pg.getDisplayName());
            set.setShortDescription(pg.getToolTip());

            Node.Property[] typeProperties = new Node.Property[properties.size()];
            set.put((Node.Property[]) properties.toArray(typeProperties));
            propertySets.add(set);
        }
    }

}

