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
package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 * help methods to create repository keys
 */
public class KeyUtilities {
    
    // This constant is used as delimiter between function name and internal data (like signature) when it is necessary
    public static final String UID_INTERNAL_DATA_PREFIX = "###"; // NOI18N
    
    public static final int NON_INITIALIZED = Integer.MIN_VALUE + 1;

    /** Creates a new instance of KeyUtils */
    private KeyUtilities() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // key generators
    public static Key createFileKey(FileImpl file) {
        return KeyManager.instance().getSharedKey(new FileKey(file));
    }

    public static Key createNamespaceKey(CsmNamespace ns) {
        return new NamespaceKey(ns);
    }
    
    /**
     *
     * @param fs
     * @param projectNativeRoot /export/homeProjectName
     * @return 
     */
    public static UnitDescriptor createUnitDescriptor(FileSystem fs, CharSequence projectNativeRoot) {
        CharSequence uniqueName = ProjectBase.getRepositoryUnitName(fs, projectNativeRoot);
        return new UnitDescriptor(uniqueName, fs);
    }

    /**
     *
     * @param projectQualifiedName /export/homeProjectName/N
     * @param fs
     * @return 
     */
    public static UnitDescriptor createUnitDescriptor(CharSequence projectQualifiedName, FileSystem fs) {
        return new UnitDescriptor(projectQualifiedName, fs);
    }

    public static UnitDescriptor createUnitDescriptor(NativeProject nativeProject) {
        CharSequence uniqueName = ProjectBase.getRepositoryUnitName(nativeProject.getFileSystem(), nativeProject);
        return new UnitDescriptor(uniqueName, nativeProject.getFileSystem());
    }

    public static Key createLibraryProjectKey(UnitDescriptor unitDescriptor, int storageID) {
        return new ProjectKey(KeyUtilities.getLibraryUnitId(unitDescriptor, storageID));
    }

    public static Key createProjectKey(UnitDescriptor unitDescriptor) {
        return new ProjectKey(KeyUtilities.getUnitId(unitDescriptor));
    }

    public static Key createProjectKey(NativeProject nativeProject) {
        UnitDescriptor unitDescriptor = createUnitDescriptor(nativeProject);
        return new ProjectKey(KeyUtilities.getUnitId(unitDescriptor));
    }

    public static Key createProjectKey(ProjectBase project) {
        return new ProjectKey(project.getUnitId());
    }
    
    public static Key createOffsetableDeclarationKey(OffsetableDeclarationBase<?> obj) {
        assert obj != null;
        return OffsetableDeclarationKey.createOffsetableDeclarationKey(obj);
    }

    public static Key createUnnamedOffsetableDeclarationKey(OffsetableDeclarationBase<?> obj, int index) {
        assert obj != null;
        return OffsetableDeclarationKey.createUnnamedOffsetableDeclarationKey(obj, index);
    }

    public static Key createMacroKey(CsmMacro macro) {
        assert macro != null;
        return new MacroKey(macro);
    }

    public static Key createIncludeKey(CsmInclude incl) {
        assert incl != null;
        return KeyManager.instance().getSharedKey(new IncludeKey(incl));
    }

    public static Key createInheritanceKey(CsmInheritance inh) {
        assert inh != null;
        return KeyManager.instance().getSharedKey(InheritanceKey.createInheritanceKey(inh));
    }

    public static <T extends CsmNamedElement> Key createParamListKey(CsmParameterList<T> paramList) {
        assert paramList != null;
        return new ParamListKey(paramList);
    }
    
    public static Key createInstantiationKey(CsmInstantiation inst) {
        assert inst != null;
        return new InstantiationKey(inst);
    }
    
    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param cacheLocation can be null, in this case standard location 
     * ${userdir}/var/cache/cnd/model will be used
     */
    public static int getUnitId(UnitDescriptor unitDescriptor) {
        return Repository.getUnitId(unitDescriptor);
    }

    public static int getLibraryUnitId(UnitDescriptor unitDescriptor, int storageID) {
        return Repository.getUnitIdForStorage(unitDescriptor, storageID);
    }

    public static CharSequence getUnitName(int unitIndex) {
        return Repository.getUnitName(unitIndex);
    }

    public static CharSequence getUnitNameSafe(int unitIndex) {
        CharSequence unitName = Repository.getUnitName(unitIndex);
        return unitName == null ? "Unit-" + unitIndex : unitName; // NOI18N
    }

    public static int getFileIdByName(int unitId, CharSequence fileName) {
        return Repository.getFileIdByName(unitId, fileName);
    }

    public static CharSequence getFileNameByIdSafe(int unitId, int fileId) {
        return Repository.getFileNameByIdSafe(unitId, fileId);
    }

    public static CharSequence getFileNameById(int unitId, int fileId) {
        return Repository.getFileNameById(unitId, fileId);
    }

    public static CsmDeclaration.Kind getKeyKind(Key key) {
        if (key instanceof OffsetableDeclarationKey) {
            return Utils.getCsmDeclarationKind(((OffsetableDeclarationKey) key).getKind());
        }
        return null;
    }

    public static CsmVisibility getKeyVisibility(Key key) {
        if (key instanceof InheritanceKey) {
            Utils.getCsmVisibility(((InheritanceKey) key).getKind());
        }
        return null;
    }

    public static char getKeyChar(Key key) {
        if (key instanceof OffsetableKey) {
            return ((OffsetableKey) key).getKind();
        }
        return 0;
    }
    
    public static CharSequence getKeyName(Key key) {
        return getKeyName(key, false);
    }

    public static CharSequence getKeyName(Key key, boolean internalName) {
        if (key instanceof OffsetableKey) {
            CharSequence name = ((OffsetableKey) key).getName();
            return internalName ? name : filterOutInternalData(name);
        } else if (key instanceof FileKey) {
            FileKey fk = (FileKey) key;
            return getFileNameByIdSafe(fk.getUnitId(), fk.getProjectFileIndex());
        } else if (key instanceof ProjectKey) {
            int unitId = key.getUnitId();
            return getUnitNameSafe(unitId);
        }
        return null;
    }

    // returns unique id of file in project
    public static int getProjectFileIndex(Key key) {
        if (key instanceof ProjectFileNameBasedKey) {
            return ((ProjectFileNameBasedKey) key).getProjectFileIndex();
        }
        return -1;
    }

    // returns unique id of project
    public static int getProjectIndex(Key key) {
        if (key instanceof ProjectFileNameBasedKey) {
            return ((ProjectFileNameBasedKey) key).getUnitId();
        } else if (key instanceof ProjectKey) {
            return ((ProjectKey) key).getUnitId();
        }
        return -1;
    }

    public static int getKeyStartOffset(Key key) {
        if (key instanceof OffsetableKey) {
            return ((OffsetableKey) key).getStartOffset();
        }
        return -1;
    }

    public static int getKeyEndOffset(Key key) {
        if (key instanceof OffsetableKey) {
            return ((OffsetableKey) key).getEndOffset();
        }
        return -1;
    }
    
    public static void cacheKeyEndOffset(Key key, int endOffset) {
        if (key instanceof OffsetableKey) {
            ((OffsetableKey) key).cacheEndOffset(endOffset);
        }
    }
    
    private static final CharSequence filterOutInternalData(CharSequence name) {
        int indexOfSignature = CharSequences.indexOf(name, UID_INTERNAL_DATA_PREFIX);
        return indexOfSignature >= 0 ? name.subSequence(0, indexOfSignature) : name;
    }
    // have to be public or UID factory does not work
}
