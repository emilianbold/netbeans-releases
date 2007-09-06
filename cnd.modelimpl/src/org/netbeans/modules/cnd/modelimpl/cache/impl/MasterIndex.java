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

package org.netbeans.modules.cnd.modelimpl.cache.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

 
/** implementation of master index file
 * it's responsibility to make pair for project and it's cache dir
 * (project cache dir is relative to main cndcache directory)
 * 
 * @author Vladimir Voskresensky
 */
final class MasterIndex extends AbstractCacheIndex implements Serializable {  
    private static final long serialVersionUID = -7790789617759717722L;
    
    // Map has structure <absolute-project-path, project-relative-cache-dir>
    
    public MasterIndex() {
    }
    
    public String getProjectDir(CsmProject project) {
        return (String) super.get(project);
    }

    public String putProject(CsmProject project) {
        return (String) super.put(project);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // save/load implmentation
    
    @Override
    protected void loadData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int version = ois.readInt();
        if (version >= 1) {
            // version 1
            //  - load base data
            super.loadData(ois);
        }
    }
    
    @Override
    protected void saveData(ObjectOutputStream oos) throws IOException {
        int version = 1;
        oos.writeInt(version);

        // version 1
        //  - save base data
        super.saveData(oos);
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // index map content support
    
    protected String getIndexKey(Object obj) {
        return ((ProjectBase) obj).getUniqueName();
    }  

    protected String getBaseCacheName(Object obj) {
        // use directory name for the project
        CsmProject project = (CsmProject)obj;
        String base = CacheUtil.mangleName(project.getName(), '_');
        return base;
    }
    
    protected Object createValue(String cacheName, Object obj2cache) {
        return cacheName;
    }

    protected boolean isEqual(Object value, String checkCacheName) {
        return ((String)value).equals(checkCacheName);
    }
}
