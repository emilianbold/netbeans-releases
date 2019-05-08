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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.cnd.refactoring.ui.tree;

import javax.swing.Icon;
import org.netbeans.modules.cnd.refactoring.support.ElementGrip;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * presentation of a leaf for refactoring element
 */
public class RefactoringTreeElement implements TreeElement { 
    
    private final RefactoringElement refactoringElement;
    private final Object parent;
    private final Icon icon;
    
    RefactoringTreeElement(RefactoringElement element) {
        this.refactoringElement = element;
        final Lookup lookup = element.getLookup(); 
        Object curParent = lookup.lookup(ElementGrip.class); 
        if (curParent == null) {
            curParent = lookup.lookup(FileObject.class);
        }
        this.parent = curParent;
        this.icon = lookup.lookup(Icon.class);
    }
    
    @Override
    public TreeElement getParent(boolean isLogical) {
        Object curParent = null;
        if (isLogical) {
            curParent = this.parent;
        } else {
            curParent = this.parent instanceof ElementGrip ? ((ElementGrip)this.parent).getFileObject() : this.parent;
        }
        return curParent != null ? TreeElementFactory.getTreeElement(curParent) : null;
    }
    
    @Override
    public Icon getIcon() {
        return icon;   
    }

    @Override
    public String getText(boolean isLogical) {
        return refactoringElement.getDisplayText();
    }

    @Override
    public Object getUserObject() {
        return refactoringElement;
    }
    
//    private CsmObject getCsmParent() {
//        CsmOffsetable obj = null;// thisObject.getObject();
//        CsmObject enclosing = null;
//        if (obj != null) {
//            enclosing = CsmRefactoringUtils.getEnclosingElement((CsmObject)obj);
//        }
//        return enclosing;
//    }
}
