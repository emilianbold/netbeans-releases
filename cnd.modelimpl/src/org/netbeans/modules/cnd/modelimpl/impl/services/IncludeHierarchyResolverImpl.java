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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 *
 * @author Alexander Simon
 */
public class IncludeHierarchyResolverImpl extends CsmIncludeHierarchyResolver {

    /** Creates a new instance of FriendResolverImpl */
    public IncludeHierarchyResolverImpl() {
    }

    public Collection<CsmFile> getFiles(CsmFile referencedFile) {
        CsmProject project = referencedFile.getProject();
        if (project instanceof ProjectBase) {
            return getReferences((ProjectBase)project, referencedFile);
        }
        return Collections.<CsmFile>emptyList();
    }

    public Collection<CsmReference> getIncludes(CsmFile referencedFile) {
        CsmProject project = referencedFile.getProject();
        if (project instanceof ProjectBase) {
            List<CsmReference> res = new ArrayList<CsmReference>();
            for (CsmFile file : getReferences((ProjectBase)project, referencedFile)){
                for (CsmInclude include : file.getIncludes()){
                    if (referencedFile.equals(include.getIncludeFile())){
                        res.add(new RefImpl(include));
                    }
                }
            }
            return res;
        }
        return Collections.<CsmReference>emptyList();
    }

    private Collection<CsmFile> getReferences(ProjectBase project, CsmFile referencedFile){
        Set<CsmFile> res = project.getGraph().getInLinks(referencedFile);
        for(ProjectBase dependent : project.getDependentProjects()){
            res.addAll(dependent.getGraph().getInLinks(referencedFile));
        }
        return res;
    }
    
    private static final class RefImpl extends OffsetableBase implements CsmReference {
        private final CsmUID<CsmInclude> delegate;
        
        private RefImpl(CsmInclude owner) {
            super(owner.getContainingFile(), owner.getStartOffset(), owner.getEndOffset());
            delegate = owner.getUID();
        }

        public CsmObject getReferencedObject() {
            CsmInclude incl = delegate.getObject();
            return incl != null ? incl.getIncludeFile() : incl;
        }

        public CsmObject getOwner() {
            return delegate.getObject();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RefImpl other = (RefImpl) obj;
            if (this.delegate != other.delegate && (this.delegate == null || !this.delegate.equals(other.delegate))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.delegate != null ? this.delegate.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "Include Reference: " + (this.delegate != null ? delegate.toString() : super.getOffsetString());
        }

        
    }
}