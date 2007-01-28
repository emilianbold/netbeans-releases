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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync.live;

import org.netbeans.modules.visualweb.insync.java.JavaClassAdapter;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;

import javax.faces.component.UIViewRoot;

import com.sun.rave.designtime.EventDescriptor;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;

/**
 * Internal SourceDesignBean root container.
 *
 * @author Carl Quinn
 */
public class SourceLiveRoot extends SourceDesignBean {

    /**
     * @param unit
     */
    SourceLiveRoot(LiveUnit unit) {
        super(unit, BeansUnit.getBeanInfo(Object.class, unit.getModel().getFacesModelSet().getProjectClassLoader()), null, null, null);
        children = new ArrayList();
    }

    //------------------------------------------------------------------------------- SourceDesignBean

    /*
     * @see com.sun.rave.designtime.DesignBean#getInstanceName()
     */
    public String getInstanceName() {
        if(unit.sourceUnit != null) {
            JavaClassAdapter adapterClass = unit.sourceUnit.getThisClass();
            if( adapterClass != null)
                return adapterClass.getShortName();
        }
        return null;
    }

    /**
     * Invoke the registered cleanup method for the bean's instance
     */
    public void invokeCleanupMethod() {
        if (instance instanceof UIViewRoot) {
            UIViewRoot uiViewRoot = ((UIViewRoot)instance);
            uiViewRoot.getChildren().clear();
            uiViewRoot.getFacets().clear();
        }
    }

    /*
     * @see com.sun.rave.designtime.DesignBean#canSetInstanceName()
     */
    public boolean canSetInstanceName() {
        return false;  // don't allow renaming host class from here
    }

    /*
     * @see com.sun.rave.designtime.DesignBean#setInstanceName(java.lang.String)
     */
    public boolean setInstanceName(String name) {
        return false;  // don't allow renaming host class from here
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.live.SourceDesignBean#newDesignProperty(java.beans.PropertyDescriptor)
     */
    protected SourceDesignProperty newDesignProperty(PropertyDescriptor descriptor) {
        return null;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.live.SourceDesignBean#newDesignEvent(com.sun.rave.designtime.EventDescriptor)
     */
    protected SourceDesignEvent newDesignEvent(EventDescriptor descriptor) {
        return null;
    }

    //--------------------------------------------------------------------------------------- Object

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SLR instance:" + getInstance() + " "); // + getChildCount() + " kids:");
        //liveChildren.toString(sb);
        sb.append("]");
        return sb.toString();
    }

}
