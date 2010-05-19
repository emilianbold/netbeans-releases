/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.gotodeclaration.symbol;

import java.io.File;
import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * SymbolDescriptor implementation for C/C++
 * @author Vladimir Kvashin
 */
public class CppSymbolDescriptor extends SymbolDescriptor {

    private final Icon icon;
    private final CsmProject project;
    private final CharSequence absPath;
    private final int offset;
    private final CharSequence ownerName;
    private final CharSequence name;
    
    public CppSymbolDescriptor(CsmOffsetable csmObj) {
        Parameters.notNull("csmObj", csmObj);
        CsmFile csmFile = csmObj.getContainingFile();
        absPath = csmFile.getAbsolutePath();
        offset = csmObj.getStartOffset();
        project = csmFile.getProject();
        if (CsmKindUtilities.isFunction(csmObj)) {
            name = ((CsmFunction) csmObj).getSignature();
        }
        else if (CsmKindUtilities.isNamedElement(csmObj)) {
            name = ((CsmNamedElement) csmObj).getName();
        } else {
            throw new IllegalArgumentException("should be CsmNamedElement, in fact " + csmObj.getClass().getName()); //NOI18N
        }

        if (CsmKindUtilities.isMacro(csmObj)) {
            //CsmMacro macro = (CsmMacro)  csmObj;
            ownerName = getBriefFileName(absPath);
        } else if (CsmKindUtilities.isOffsetableDeclaration(csmObj)) {
            CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) csmObj;
            CsmScope scope = decl.getScope();
            if (CsmKindUtilities.isFile(scope)) {
                ownerName = getBriefFileName(absPath);
            }
            else if (CsmKindUtilities.isQualified(scope)) {
                CharSequence fileName = getBriefFileName(absPath);
                CharSequence qName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
                if (qName.length() > 0) {
                    ownerName = NbBundle.getMessage(getClass(), "CPP_Descriptor_In_Compound", qName, fileName);
                } else {
                    ownerName = fileName;
                }
            } else {
                throw new IllegalArgumentException("should be either CsmFile or CsmQualifiedNamedElement, in fact " + csmObj.getClass().getName()); //NOI18N
            }
        } else {
            throw new IllegalArgumentException("should be either CsmMacro or CsmDeclaration, in fact " + csmObj.getClass().getName()); //NOI18N
        }
        icon = CsmImageLoader.getIcon(csmObj);
    }

    private CharSequence getBriefFileName(CharSequence fileName) {
        for (int i = fileName.length() - 1; i >= 0; i-- ) {
            char c = fileName.charAt(i);
            if (c == '/' || c == '\\') {
                return fileName.subSequence(i + 1, fileName.length());
            }
        }
        return fileName;
    }

    @Override
    public FileObject getFileObject() {
        return FileUtil.toFileObject(new File(absPath.toString()));
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public String getOwnerName() {
        return ownerName.toString();
    }

    @Override
    public Icon getProjectIcon() {
        return CsmImageLoader.getIcon(project);
    }

    @Override
    public String getProjectName() {
        CharSequence prjName = project.getName();
        if (project.isArtificial()) {
            prjName = CsmDisplayUtilities.shrinkPath(prjName, 32, 2, 2);
        }
        return prjName.toString();
    }

    @Override
    public String getSymbolName() {
        return name.toString();
    }

    /** gets name as such */
    public CharSequence getRawName() {
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '(') {
                return name.subSequence(0, i);
            }
        }
        return name;
    }

    @Override
    public void open() {
        CsmUtilities.openSource(getFileObject(), offset);
    }
}
