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

import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.openide.nodes.Node;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author pfiala
 */
public class EjbNode extends EjbSectionNode {

    public EjbNode(SectionNodeView sectionNodeView, final Ejb ejb, String iconBase) {
        super(sectionNodeView, false, ejb, Utils.getEjbDisplayName(ejb), iconBase);
        ejb.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String oldDisplayName = getDisplayName();
                String newDisplayName = Utils.getEjbDisplayName(ejb);
                if (!oldDisplayName.equals(newDisplayName)) {
                    setDisplayName(newDisplayName);
                    firePropertyChange(Node.PROP_DISPLAY_NAME, oldDisplayName, newDisplayName);
                }
            }
        });
        helpProvider = true;
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return null;
    }
}
