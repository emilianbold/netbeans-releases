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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Node;
import org.openide.nodes.Children;

/**
 * @author pfiala
 */
public class EjbJarView extends SectionNodeView {

    protected EnterpriseBeansNode enterpriseBeansNode;
    protected EnterpriseBeans enterpriseBeans;
    protected EjbJar ejbJar;

    EjbJarView(EjbJarMultiViewDataObject dataObject) {
        super(dataObject);
        EjbSectionNode rootNode = new EjbSectionNode(this, this, Utils.getBundleMessage("LBL_Overview"),
                Utils.ICON_BASE_DD_VALID);
        ejbJar = dataObject.getEjbJar();
        rootNode.addChild(new EjbJarDetailsNode(this, ejbJar));
        rootNode.addChild(new EjbJarSecurityRolesNode(this, ejbJar));
        setRootNode(rootNode);
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (oldValue instanceof EnterpriseBeans || newValue instanceof EnterpriseBeans ||
                propertyName.indexOf("MethodPermission") > 0 || propertyName.indexOf("SecurityIdentity") > 0) {   //NOI18N
            scheduleRefreshView();
        }
        super.dataModelPropertyChange(source, propertyName, oldValue, newValue);
    }

    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return ((EjbJarMultiViewDataObject) getDataObject()).getModelSynchronizer();
    }

    public void refreshView() {
        checkEnterpriseBeans();
        super.refreshView();
    }

    private void checkEnterpriseBeans() {
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        if (enterpriseBeans != this.enterpriseBeans) {
            SectionNode rootNode = getRootNode();
            final Children children = rootNode.getChildren();
            final Node[] nodes = children.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node instanceof EnterpriseBeansNode) {
                    children.remove(new Node[]{node});
                }
            }
            if (enterpriseBeans != null) {
                enterpriseBeansNode = new EnterpriseBeansNode(this, enterpriseBeans);
                if (rootNode != null) {
                    rootNode.addChild(enterpriseBeansNode);
                    rootNode.populateBoxPanel();
                }
            }
            this.enterpriseBeans = enterpriseBeans;
        }
    }
}
