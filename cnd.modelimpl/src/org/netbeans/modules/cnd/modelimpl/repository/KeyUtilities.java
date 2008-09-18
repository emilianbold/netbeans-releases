/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.Key;

/**
 * help methods to create repository keys
 * @author Vladimir Voskresensky
 */
public class KeyUtilities {
    
    /** Creates a new instance of KeyUtils */
    private KeyUtilities() {
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // key generators
    
    public static Key createFileKey(FileImpl file) {
        return new FileKey(file);
    }
    
    public static Key createNamespaceKey(CsmNamespace ns) {
        return new NamespaceKey(ns);
    }
    
    public static Key createProjectKey(ProjectBase project) {
        return new ProjectKey(project);
    }
    
    public static Key createProjectKey(String projectQualifiedName) {
        return new ProjectKey(projectQualifiedName);
    }

    public static Key createProjectKey(NativeProject nativeProject) {
        return new ProjectKey(ProjectBase.getUniqueName(nativeProject).toString());
    }
    
    public static Key createOffsetableDeclarationKey(OffsetableDeclarationBase obj) {
        assert obj != null;
        return new OffsetableDeclarationKey(obj);
    }
    
    public static Key createUnnamedOffsetableDeclarationKey(OffsetableDeclarationBase obj, int index) {
        assert obj != null;
        return new OffsetableDeclarationKey(obj, index);
    }
    
    public static Key createMacroKey(CsmMacro macro) {
        assert macro != null;
        return new MacroKey(macro);
    }
    
    public static Key createIncludeKey(CsmInclude incl) {
        assert incl != null;
        return new IncludeKey(incl);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
     public static int getUnitId(String unitName) {
	return RepositoryUtils.getUnitId(unitName);
    }
    
    public static String getUnitName(int unitIndex) {
	return RepositoryUtils.getUnitName(unitIndex);
    }
    
    public static int getFileIdByName(final int unitId, final String fileName){
        return RepositoryUtils.getFileIdByName(unitId, fileName);
    }
    
    public static String getFileNameById(final int unitId, final int fileId){
        return RepositoryUtils.getFileNameById(unitId, fileId);
    }
    public static String getFileNameByIdSafe(final int unitId, final int fileId){
        return RepositoryUtils.getFileNameByIdSafe(unitId, fileId);
    }    
 
    public static CsmDeclaration.Kind getKeyKind(Key key){
        if (key instanceof OffsetableDeclarationKey) {
            return Utils.getCsmDeclarationKind( ((OffsetableDeclarationKey)key).getKind() );
        }
        return null;
    }

    public static CharSequence getKeyName(Key key){
        if (key instanceof OffsetableKey) {
            return ((OffsetableKey)key).getName();
        } else if(key instanceof FileKey) {
            return ((FileKey) key).getName();
        } else if(key instanceof ProjectKey) {
            return ((ProjectKey) key).getProjectName();
        }
        return null;
    }

    public static int getKeyStartOffset(Key key){
        if (key instanceof OffsetableKey) {
            return ((OffsetableKey)key).getStartOffset();
        }
        return -1;
    }

    public static int getKeyEndOffset(Key key){
        if (key instanceof OffsetableKey) {
            return ((OffsetableKey)key).getEndOffset();
        }
        return -1;
    }
    
    // have to be public or UID factory does not work

}
