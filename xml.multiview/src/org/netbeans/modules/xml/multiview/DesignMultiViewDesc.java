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
