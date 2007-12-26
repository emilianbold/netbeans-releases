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
        return ((ProjectBase) obj).getUniqueName().toString();
    }  

    protected String getBaseCacheName(Object obj) {
        // use directory name for the project
        CsmProject project = (CsmProject)obj;
        String base = CacheUtil.mangleName(project.getName(), '_');
        return base;
    }
    
    protected Object createValue(CharSequence cacheName, Object obj2cache) {
        return cacheName;
    }

    protected boolean isEqual(Object value, CharSequence checkCacheName) {
        return ((String)value).equals(checkCacheName);
    }
}
