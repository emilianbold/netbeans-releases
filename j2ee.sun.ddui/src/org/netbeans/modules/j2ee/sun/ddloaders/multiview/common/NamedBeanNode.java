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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Node;


/**
 * @author Peter Williams
 */
public abstract class NamedBeanNode extends BaseSectionNode {

    public NamedBeanNode(final SectionNodeView sectionNodeView, final CommonDDBean bean, 
            final String beanNameProperty, final String iconBase, final ASDDVersion version) {
        super(sectionNodeView, bean, version, Utils.getBeanDisplayName(bean, beanNameProperty), iconBase);
        
        bean.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String oldDisplayName = getDisplayName();
                String newDisplayName = Utils.getBeanDisplayName(bean, beanNameProperty);
                if (!oldDisplayName.equals(newDisplayName)) {
                    setDisplayName(newDisplayName);
                    firePropertyChange(Node.PROP_DISPLAY_NAME, oldDisplayName, newDisplayName);
                }
            }
        });
        
        helpProvider = true;
    }

    protected abstract SectionNodeInnerPanel createNodeInnerPanel();
}
