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

package org.netbeans.modules.cnd.refactoring.ui.tree;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmObjectBoxFactory;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.openide.filesystems.FileObject;

/**
 * factory of tree elements for C/C++ refactorings
 * 
 * @author Vladimir Voskresensky
 */
public class TreeElementFactoryImpl implements TreeElementFactoryImplementation {

    public Map<Object, TreeElement> map = new WeakHashMap<Object, TreeElement>();
    public static TreeElementFactoryImpl instance;
    {
        instance = this;
    }
    
    public TreeElement getTreeElement(Object o) {
        TreeElement result = map.get(o);
        if (result!= null) {
            return result;
        }
        if (o instanceof RefactoringElement) {
            CsmOffsetable csmObject = ((RefactoringElement) o).getLookup().lookup(CsmOffsetable.class);
            if (csmObject!=null) {
                result = new RefactoringTreeElement((RefactoringElement) o);
            } else {
                CsmObject obj = ((RefactoringElement) o).getLookup().lookup(CsmObject.class);
                if (obj != null) {
                    System.err.println("unhandled CsmObject: " + obj);
                }
            }
        } else if (CsmKindUtilities.isProject(o)) {
            result = new ProjectTreeElement((CsmProject)o);
        } else if (CsmKindUtilities.isCsmObject(o)) {
            CsmObject csm = (CsmObject)o;
            if (CsmKindUtilities.isFile(csm)) {
                FileObject fo = CsmUtilities.getFileObject((CsmFile)o);
                result = new FileTreeElement(fo, (CsmFile)o);
            } else {
                result = new ParentTreeElement(csm);
            }
        }
        if (result != null) {
            map.put(o, result);
        }
        return result;
    }

    public void cleanUp() {
        map.clear();
        CsmObjectBoxFactory.getDefault().cleanUp();
    }
}
