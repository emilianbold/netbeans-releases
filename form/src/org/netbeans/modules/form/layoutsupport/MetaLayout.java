/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.beans.*;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * @author Tomas Pavek
 */

class MetaLayout extends RADComponent {

    private LayoutSupport layoutSupport;
    private RADVisualContainer container;

    public MetaLayout(LayoutSupport laysup, RADVisualContainer container) {
        super();
        this.layoutSupport = laysup;
        this.container = container;
        initialize(container.getFormModel());
    }

    public void updateInstance(Object beanInstance) {
        if (container.getLayoutSupport() == layoutSupport) {
            Container cont =
                container.getContainerDelegate(container.getBeanInstance());
            RADVisualComponent[] comps = container.getSubComponents();

            for (int i=0; i < comps.length; i++)
                cont.remove(comps[i].getComponent());

            super.updateInstance(beanInstance); // calls setBeanInstance

            for (int i=0; i < comps.length; i++) {
                Component comp = comps[i].getComponent();

                // hack for AWT components - fake peer must be attached again
                boolean attached = FakePeerSupport.attachFakePeer(comp);
                if (attached && comp instanceof Container)
                        FakePeerSupport.attachFakePeerRecursively((Container)comp);

                cont.add(comp);
            }
//            getFormModel().fireFormChanged();
        }
        else super.updateInstance(beanInstance); // calls setBeanInstance
    }

    protected Object createBeanInstance() {
        Container cont = container.getContainerDelegate(container.getBeanInstance());
        return layoutSupport.createDefaultLayoutInstance(cont);
    }

    protected void setBeanInstance(Object beanInstance) {
        super.setBeanInstance(beanInstance);

        if (container.getLayoutSupport() == layoutSupport) {
            Container cont =
                container.getContainerDelegate(container.getBeanInstance());
            cont.setLayout((LayoutManager)beanInstance);
        }
    }

    protected void createPropertySets(java.util.List propSets) {
        super.createPropertySets(propSets);

        // temporary hack - until we implement code generation properties...
        for (int i=0, n=propSets.size(); i < n; i++) {
            Node.PropertySet propSet = (Node.PropertySet)propSets.get(i);
            if (!"properties".equals(propSet.getName()) // NOI18N
                    && !"properties2".equals(propSet.getName())) { // NOI18N
                propSets.remove(i);
                i--;  n--;
            }
        }
    }

    protected PropertyChangeListener createPropertyListener() {
        // cannot reuse RADComponent.PropertyListener, because this is not
        // a regular RADComponent (properties have a special meaning)
        return new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                getFormModel().fireContainerLayoutChanged(container,
                                                          null, null);

                LayoutNode node = container.getLayoutNodeReference();
                if (node == null)
                    return;

                // propagate the change to node
                if (FormProperty.PROP_VALUE.equals(ev.getPropertyName()))
                    node.fireLayoutPropertiesChange();
                else if (FormProperty.CURRENT_EDITOR.equals(ev.getPropertyName()))
                    node.fireLayoutPropertySetsChange();
            }
        };
    }
}
