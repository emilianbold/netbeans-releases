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

import org.openide.filesystems.FileObject;
import org.netbeans.modules.visualweb.insync.models.FacesModel;

/**
 * Manage the methods, fields and such that should be defined for Reef style beans.
 *
 * @author eric
 *
 */
public class ReefFacesBeanStructureScanner extends FacesBeanStructureScanner {

    public  ReefFacesBeanStructureScanner(FacesUnit unit) {
        super(unit);
        destroyInfo = new MethodInfo("afterRenderResponse"); //NOI18N
    }

    //Methods that need to be in a managed bean
    protected MethodInfo[] getMethodInfos(){
        return new MethodInfo[]{ctorInfo, destroyInfo};
    }

    public String getSuggestedThisClassSuperclass() {
        FileObject jspFile = FacesModel.getJspForJava(getJavaUnit().getFileObject());
        if (jspFile == null)
            return null;
        return "com.sun.jsfcl.app.AbstractPageBean"; // NOI18N
    }

}
