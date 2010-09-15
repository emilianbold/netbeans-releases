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
package org.netbeans.modules.web.beans.hints;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;

import javax.lang.model.element.Modifier;

import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.util.TreePath;


/**
 * @author ads
 *
 */
final class CreateQualifierFix implements Fix {

    CreateQualifierFix( CompilationInfo compilationInfo, String name , 
            String packageName , FileObject fileObject)
    {
        myInfo = compilationInfo;
        myName = name;
        myPackage = packageName;
        myFileObject = fileObject;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#getText()
     */
    @Override
    public String getText() {
        if ( myPackage == null || myPackage.length() == 0 ){
            return NbBundle.getMessage(CreateQualifierFix.class, 
                    "LBL_FixCreateQualifierDefaultPackage");
        }
        return NbBundle.getMessage(CreateQualifierFix.class, 
                "LBL_FixCreateQualifier" , myName , myPackage );
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#implement()
     */
    @Override
    public ChangeInfo implement() throws Exception {
        FileObject template = FileUtil.getConfigFile("Templates/CDI/Qualifier.java"); // NOI18N
        FileObject target;
        
        FileObject root = myInfo.getClasspathInfo()
                .getClassPath(PathKind.SOURCE)
                .findOwnerRoot(myInfo.getFileObject());
        FileObject pakage = FileUtil.createFolder(root, myPackage.replace('.', '/'));
        
        DataObject templateDO = DataObject.find(template);
        DataObject od = templateDO.createFromTemplate(
                DataFolder.findFolder(pakage), myName);

        target = od.getPrimaryFile();

        /*JavaSource javaSource = JavaSource.forFileObject(target);
        ModificationResult diff = javaSource.
            runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy working) throws IOException {
                working.toPhase(Phase.RESOLVED);
                
                TreeMaker make = working.getTreeMaker();
                CompilationUnitTree cut = working.getCompilationUnit();
                ExpressionTree pack = cut.getPackageName() ;
                ClassTree source =   (ClassTree) cut.getTypeDecls().get(0);
                
                ModifiersTree modifiers = make.Modifiers(EnumSet.<Modifier>of(Modifier.PUBLIC));
                
                ClassTree targetTree = (ClassTree)(new TreePath(
                        new TreePath(cut), source)).getLeaf();
                ClassTree annotationTree = make.AnnotationType(modifiers, 
                        targetTree.getSimpleName(), targetTree.getMembers());
                
                working.rewrite(cut, make.CompilationUnit(pack, cut.getImports(), 
                        Collections.singletonList(annotationTree), cut.getSourceFile()));
            }
        });
        diff.commit();*/
        return new ChangeInfo(target, null, null);
    }
    
    private CompilationInfo myInfo;
    private String myName;
    private String myPackage;
    private FileObject myFileObject;

}
