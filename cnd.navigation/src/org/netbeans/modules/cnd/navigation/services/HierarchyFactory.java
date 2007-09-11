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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.navigation.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;

/**
 *
 * @author Alexander Simon
 */
public class HierarchyFactory {
    
    private HierarchyFactory(){
    }
    
    public static HierarchyFactory getInstance(){
        return new HierarchyFactory();
    }

    public HierarchyModel buildTypeHierarchyModel(CsmClass cls, boolean subDirection){
        return new HierarchyModelImpl(cls, subDirection);
    }

    public IncludedModel buildIncludeHierarchyModel(CsmFile file, boolean whoIncludes, boolean plain, boolean recursive){
        if (whoIncludes && plain && !recursive) {
            Collection<CsmFile> list = CsmIncludeHierarchyResolver.getDefault().getFiles(file);
            final Map<CsmFile, Set<CsmFile>> map = new HashMap<CsmFile, Set<CsmFile>>();
            map.put(file, new HashSet<CsmFile>(list));
            return new IncludedModelAdapter(map);
        }
        return new IncludedModelImpl(file, whoIncludes, plain, recursive);
    }
    
    private static class IncludedModelAdapter implements IncludedModel{
        private Map<CsmFile, Set<CsmFile>> map;
        public IncludedModelAdapter(Map<CsmFile, Set<CsmFile>> map){
            this.map = map;
        }
        public Map<CsmFile, Set<CsmFile>> getModel() {
            return map;
        }
    }
}
