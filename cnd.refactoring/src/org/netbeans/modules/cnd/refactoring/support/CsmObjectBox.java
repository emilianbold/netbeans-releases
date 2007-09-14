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

package org.netbeans.modules.cnd.refactoring.support;

import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.filesystems.FileObject;


/**
 * wrapper object for csm object to be stored in index collection
 * @author Vladimir Voskresensky
 */
public final class CsmObjectBox {

    private CsmUID<CsmObject> delegateElementHandle;
    private String toString;
    private FileObject fileObject;
    private Icon icon;

    /**
     * Creates a new instance of ElementGrip
     */
    public CsmObjectBox(CsmObject object) {
        this.delegateElementHandle = CsmRefactoringUtils.getHandler(object);
        this.toString = CsmRefactoringUtils.getHtml(object);
        this.fileObject = CsmRefactoringUtils.getFileObject(object);
        this.icon = CsmImageLoader.getIcon(object);
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return toString;
    }

    public CsmObjectBox getParent() {
        return CsmObjectBoxFactory.getDefault().getParent(this);
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public CsmUID<CsmObject> getHandle() {
        return delegateElementHandle;
    }
}