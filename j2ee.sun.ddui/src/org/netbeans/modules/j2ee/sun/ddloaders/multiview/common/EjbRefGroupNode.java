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

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class EjbRefGroupNode extends NamedBeanGroupNode {

    public EjbRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, EjbRef.EJB_REF_NAME, 
                NbBundle.getMessage(EjbRefGroupNode.class, "LBL_EjbRefGroupHeader"), // NOI18N
                ICON_BASE_EJB_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(EjbRefGroupNode.class, "LBL_AddEjbRef")); // NOI18N
    }

    protected SectionNode createNode(CommonDDBean bean) {
        return new EjbRefNode(getSectionNodeView(), (EjbRef) bean, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        EjbRef [] serviceRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            serviceRefs = ((SunWebApp) commonDD).getEjbRef();
        } else if(commonDD instanceof Ejb) {
            serviceRefs = ((Ejb) commonDD).getEjbRef();
        } else if(commonDD instanceof SunApplicationClient) {
            serviceRefs = ((SunApplicationClient) commonDD).getEjbRef();
        }
        return serviceRefs;
    }

    protected CommonDDBean addNewBean() {
        EjbRef newEjbRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            SunWebApp sunWebApp = (SunWebApp) commonDD;
            newEjbRef = sunWebApp.newEjbRef();
            sunWebApp.addEjbRef(newEjbRef);
        } else if(commonDD instanceof Ejb) {
            Ejb ejb = (Ejb) commonDD;
            newEjbRef = ejb.newEjbRef();
            ejb.addEjbRef(newEjbRef);
        } else if(commonDD instanceof SunApplicationClient) {
            SunApplicationClient sunAppClient = (SunApplicationClient) commonDD;
            newEjbRef = sunAppClient.newEjbRef();
            sunAppClient.addEjbRef(newEjbRef);
        }
        
        newEjbRef.setEjbRefName("ejb_ref" + getNewBeanId()); // NOI18N
        
        return newEjbRef;
    }
    
    protected void removeBean(CommonDDBean bean) {
        EjbRef serviceRef = (EjbRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            SunWebApp sunWebApp = (SunWebApp) commonDD;
            sunWebApp.removeEjbRef(serviceRef);
        } else if(commonDD instanceof Ejb) {
            Ejb ejb = (Ejb) commonDD;
            ejb.removeEjbRef(serviceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            SunApplicationClient sunAppClient = (SunApplicationClient) commonDD;
            sunAppClient.removeEjbRef(serviceRef);
        }
    }
    
}
