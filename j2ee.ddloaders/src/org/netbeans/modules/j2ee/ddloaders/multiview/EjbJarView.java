/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.openide.nodes.Node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author pfiala
 */
class EjbJarView extends SectionNodeView implements PropertyChangeListener {

    protected EnterpriseBeansNode enterpriseBeansNode;
    protected EnterpriseBeans enterpriseBeans;
    protected EjbJar ejbJar;

    EjbJarView(EjbJarMultiViewDataObject dataObject) {
        super(dataObject);
        EjbSectionNode rootNode = new EjbSectionNode(this, this, Utils.getBundleMessage("LBL_Overview"),
                Utils.ICON_BASE_DD_VALID);
        ejbJar = dataObject.getEjbJar();
        ejbJar.addPropertyChangeListener(this);
        rootNode.addChild(new EjbJarDetailsNode(this, ejbJar));
        setRootNode(rootNode);
        checkEnterpriseBeans();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getOldValue() instanceof EnterpriseBeans || evt.getNewValue() instanceof EnterpriseBeans) {
            checkEnterpriseBeans();
        }
    }

    private void checkEnterpriseBeans() {
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        if (enterpriseBeans != this.enterpriseBeans) {
            SectionNode rootNode = getRootNode();
            if (enterpriseBeans != null) {
                enterpriseBeansNode = new EnterpriseBeansNode(this, enterpriseBeans);
                if (rootNode != null) {
                    rootNode.addChild(enterpriseBeansNode);
                    rootNode.populateBoxPanel();
                }
            } else {
                rootNode.getChildren().remove(new Node[]{enterpriseBeansNode});
            }
            this.enterpriseBeans = enterpriseBeans;
        }
    }
}
