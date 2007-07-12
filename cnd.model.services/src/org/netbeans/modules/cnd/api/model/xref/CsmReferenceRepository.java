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

package org.netbeans.modules.cnd.api.model.xref;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.openide.util.Lookup;

/**
 * entry point to search references of model object in projects
 * @author Vladimir Voskresensky
 */
public abstract class CsmReferenceRepository {
    /** A dummy Repository that never returns any results.
     */
    private static final CsmReferenceRepository EMPTY = new Empty();
    
    /** default instance */
    private static CsmReferenceRepository defaultRepository;
    
    protected CsmReferenceRepository() {
    }
    
    /** Static method to obtain the Repository.
     * @return the Repository
     */
    public static synchronized CsmReferenceRepository getDefault() {
        if (defaultRepository != null) {
            return defaultRepository;
        }
        defaultRepository = (CsmReferenceRepository) Lookup.getDefault().lookup(CsmReferenceRepository.class);
        return defaultRepository == null ? EMPTY : defaultRepository;
    }
    
    /**
     * look for references of target object in project
     * @param target target object to find references
     * @param project project as scope where to search
     * @param includeSelfDeclarations flag indicating wether or not to include 
     *      self declaration object in collection
     * @return references for target object, empty collection if not found
     */
    public abstract Collection<CsmReference> getReferences(CsmObject target, CsmProject project, boolean includeSelfDeclarations);

    /**
     * look for references of target object in project
     * @param target target object to find references
     * @param file file as scope where to search
     * @param includeSelfDeclarations flag indicating wether or not to include 
     *      self declaration object in collection
     * @return references for target object, empty collection if not found
     */
    public abstract Collection<CsmReference> getReferences(CsmObject target, CsmFile file, boolean includeSelfDeclarations);
    
    /**
     * look for references of target objects in project
     * @param targets target objects to find references
     * @param project project as scope where to search
     * @param includeSelfDeclarations flag indicating wether or not to include 
     *      self declaration object in collection
     * @return references for target object, empty collection if not found
     */
    //public abstract Map<CsmObject, Collection<CsmReference>> getReferences(CsmObject[] targets, CsmProject project, boolean includeSelfDeclarations);

    /**
     * look for references of target object in project
     * @param target target object to find references
     * @param file file as scope where to search
     * @param includeSelfDeclarations flag indicating wether or not to include 
     *      self declaration object in collection
     * @return references for target object, empty collection if not found
     */
    //public abstract Map<CsmObject, Collection<CsmReference>> getReferences(CsmObject[] targets, CsmFile file, boolean includeSelfDeclarations);
    
    //
    // Implementation of the default Repository
    //
    private static final class Empty extends CsmReferenceRepository {
        Empty() {
        }

        public Collection<CsmReference> getReferences(CsmObject target, CsmProject project, boolean includeSelfDeclarations) {
            return Collections.<CsmReference>emptyList();
        }

        public Map<CsmObject, Collection<CsmReference>> getReferences(CsmObject[] targets, CsmProject project, boolean includeSelfDeclarations) {
            return Collections.<CsmObject, Collection<CsmReference>>emptyMap();
        }

        public Collection<CsmReference> getReferences(CsmObject target, CsmFile file, boolean includeSelfDeclarations) {
            return Collections.<CsmReference>emptyList();
        }

        public Map<CsmObject, Collection<CsmReference>> getReferences(CsmObject[] targets, CsmFile file, boolean includeSelfDeclarations) {
            return Collections.<CsmObject, Collection<CsmReference>>emptyMap();
        }
    }    
}
