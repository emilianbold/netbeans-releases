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
package org.netbeans.modules.visualweb.insync.faces;

import org.openide.util.NbBundle;

public class ThresherFacesApplicationBeanStructureScanner extends ThresherFacesBeanStructureScanner {

    public ThresherFacesApplicationBeanStructureScanner(FacesUnit unit) {
        super(unit);
    }

    //Methods that need to be in a managed bean
    protected MethodInfo[] getMethodInfos(){
        return new MethodInfo[]{ ctorInfo, initInfo, destroyInfo, propertiesInitInfo};
    }

    public String getComment(String id) {
        return NbBundle.getMessage(ThresherFacesApplicationBeanStructureScanner.class, id);
    }

    public String getConstructorComment() {
        return getComment("COMMENT_ApplicationBeanConstructorComment");
    }

    public String getDestroyMethodComment() {
        return getComment("COMMENT_ApplicationBeanDestroyMethodComment");
    }

    public String getInitMethodComment() {
        return getComment("COMMENT_ApplicationBeanInitMethodComment");
    }

    public String getSuggestedThisClassSuperclass() {
        return "com.sun.rave.web.ui.appbase.AbstractApplicationBean"; // NOI18N
    }

    public String getThisClassComment() {
        return getComment("COMMENT_ApplicationBeanClassComment"); // NOI18N
    }

}
