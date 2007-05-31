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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class EjbGroupNode extends NamedBeanGroupNode {

    private SunEjbJar sunEjbJar;
    
    public EjbGroupNode(SectionNodeView sectionNodeView, SunEjbJar sunEjbJar, ASDDVersion version) {
        super(sectionNodeView, sunEjbJar, Ejb.EJB_NAME, 
                NbBundle.getMessage(EjbGroupNode.class, "LBL_EjbGroupHeader"), // NOI18N
                ICON_EJB_GROUP_NODE, version);
        
        this.sunEjbJar = sunEjbJar;
        enableAddAction(NbBundle.getMessage(EjbGroupNode.class, "LBL_AddEjb")); // NOI18N
    }

    protected SectionNode createNode(CommonDDBean bean) {
        return new EjbNode(getSectionNodeView(), (Ejb) bean, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
        if(eb != null) {
            return eb.getEjb();
        }
        return null;
    }

    protected CommonDDBean addNewBean() {
        EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
        if(eb == null) {
            eb = sunEjbJar.newEnterpriseBeans();
            sunEjbJar.setEnterpriseBeans(eb);
        }
        Ejb newEjb = eb.newEjb();
        eb.addEjb(newEjb);
        
        newEjb.setEjbName("ejb" + getNewBeanId()); // NOI18N
        
        return newEjb;
    }
    
    protected void removeBean(CommonDDBean bean) {
        EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
        if(eb != null) {
            Ejb ejb = (Ejb) bean;
            eb.removeEjb(ejb);
            // TODO if eb is empty of all data now, we could remove it too.
        }
    }
    
}