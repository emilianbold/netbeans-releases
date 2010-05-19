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
package org.netbeans.modules.edm.editor.property.impl;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.edm.editor.property.IProperty;
import org.netbeans.modules.edm.editor.property.IPropertyGroup;
import org.netbeans.modules.edm.editor.property.ITemplate;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class PropertyNode extends AbstractNode {

    private ArrayList<Node.PropertySet> propertySets = new ArrayList<Node.PropertySet>();
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

    @Override
    public Node.PropertySet[] getPropertySets() {
        Node.PropertySet[] typePropertySet = new Node.PropertySet[propertySets.size()];
        return propertySets.toArray(typePropertySet);
    }

    @SuppressWarnings("unchecked")
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

    public void addPropertyChangeSupport(PropertyChangeListener l) {
        if (l != null) {
            for (int i = 0; i < propertySets.size(); i++) {
                Sheet.Set set = (Set) propertySets.get(i);
                Node.Property[] properties = set.getProperties();
                IProperty p = (IProperty) properties[0];
                ((PropertyGroup) p.getParent()).addPropertyChangeListener(l);
            }
        }
    }
}

