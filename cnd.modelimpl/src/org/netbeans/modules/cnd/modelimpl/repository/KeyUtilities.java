/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
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
 
    
    // have to be public or UID factory does not work

}
