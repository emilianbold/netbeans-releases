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

package org.netbeans.modules.xml.multiview;

import org.netbeans.core.spi.multiview.*;

/**
 * DesignMultiViewDesc.java
 *
 * Created on October 9, 2004, 11:37 AM
 * @author mkuchtiak
 */
public abstract class DesignMultiViewDesc implements MultiViewDescription, java.io.Serializable {

    static final long serialVersionUID = -3640713597058983397L;

    private String name;
    private XmlMultiViewDataObject dObj;

    public DesignMultiViewDesc() {
    }

    public DesignMultiViewDesc(XmlMultiViewDataObject dObj, String name) {
        this.name=name;
        this.dObj=dObj;
    }

    public abstract MultiViewElement createElement();
    
    protected XmlMultiViewDataObject getDataObject() {
        return dObj;
    }

    public String getDisplayName() {
        return name;
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }
    
    public abstract java.awt.Image getIcon();

    public int getPersistenceType() {
        return org.openide.windows.TopComponent.PERSISTENCE_NEVER;
    }
    
    public abstract String preferredID();
        
}
