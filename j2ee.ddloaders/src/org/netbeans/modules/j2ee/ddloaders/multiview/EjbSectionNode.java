/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author pfiala
 */
public class EjbSectionNode extends SectionNode implements PropertyChangeListener {

    public EjbSectionNode(SectionNodeView sectionNodeView, boolean isLeaf, Object key, String title, String iconBase) {
        super(sectionNodeView, isLeaf ? Children.LEAF : new Children.Array(), key, title, iconBase);
        if (key instanceof CommonDDBean) {
            ((CommonDDBean) key).addPropertyChangeListener(this);
        }
    }

    public EjbSectionNode(SectionNodeView sectionNodeView, Object key, String title, String iconBase) {
        this(sectionNodeView, false, key, title, iconBase);
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        propertyChanged(evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }

    public void propertyChanged(Object source, String propertyName, Object oldValue, Object newValue) {
        SectionNodePanel panel = getSectionNodePanel();
        if (panel != null) {
            panel.propertyChanged(source, propertyName, oldValue, newValue);
        }
    }
}
