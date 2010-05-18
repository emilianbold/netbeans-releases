/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.insync.live;

import org.netbeans.modules.visualweb.insync.java.JavaClass;
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
            JavaClass adapterClass = unit.sourceUnit.getThisClass();
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
