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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.classview;

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 *
 * @author vk155633
 */
public class SmartChangeEvent {
    protected Map<CsmNamespace,CsmProject>  newNamespaces = new HashMap<CsmNamespace,CsmProject>();
    protected Set<CsmDeclaration> newDeclarations = new HashSet<CsmDeclaration>();
    protected Set<CsmDeclaration> removedDeclarations = new HashSet<CsmDeclaration>();
    protected Set<CsmDeclaration> changedDeclarations = new HashSet<CsmDeclaration>();
    protected Set<CsmProject> changedProjects = new HashSet<CsmProject>();
    protected Set<String> changedUniqNames;
    protected Set<String> removedUniqNames;
    
    // to trace only
    private int count = 1;
    
    public SmartChangeEvent(CsmChangeEvent e){
        //super(e.getSource());
        changedProjects.addAll(e.getChangedProjects());
        CsmProject project = (CsmProject)e.getChangedProjects().iterator().next();
        for(Iterator it = e.getNewNamespaces().iterator(); it.hasNext();){
            newNamespaces.put((CsmNamespace)it.next(), project);
        }
        newDeclarations.addAll(e.getNewDeclarations());
        removedDeclarations.addAll(e.getRemovedDeclarations());
        changedDeclarations.addAll(e.getChangedDeclarations());
        changedDeclarations.removeAll(e.getNewDeclarations());
    }
    
    public boolean addChangeEvent(CsmChangeEvent e){
        if (/*getChangedProjects().size() == 1 && e.getChangedProjects().size() == 1 &&
                getChangedProjects().iterator().next() == e.getChangedProjects().iterator().next() &&*/
                getRemovedDeclarations().size() == 0 && e.getRemovedDeclarations().size() == 0){
            doAdd(e);
            count++;
            return true;
        }
        return false;
    }
    
    int getCount(){
        return count;
    }
    
    private void doAdd(CsmChangeEvent e){
        getChangedProjects().addAll(e.getChangedProjects());
        CsmProject project = (CsmProject)e.getChangedProjects().iterator().next();
        for(Iterator it = e.getNewNamespaces().iterator(); it.hasNext();){
            newNamespaces.put((CsmNamespace)it.next(), project);
        }
        getNewDeclarations().addAll(e.getNewDeclarations());
        getChangedDeclarations().addAll(e.getChangedDeclarations());
        getChangedDeclarations().removeAll(getNewDeclarations());
    }
    
    public Collection<CsmDeclaration> getNewDeclarations() {
        return newDeclarations;
    }
    
    Collection<CsmDeclaration> getRemovedDeclarations() {
        return removedDeclarations;
    }

    public Collection<String> getRemovedUniqueNames() {
        if (removedUniqNames == null){
            removedUniqNames = new HashSet();
            for (Iterator<CsmDeclaration> it = getRemovedDeclarations().iterator(); it.hasNext();){
                CsmDeclaration decl = it.next();
                removedUniqNames.add(decl.getUniqueName());
            }
        }
        return removedUniqNames;
    }
    
    Collection<CsmDeclaration> getChangedDeclarations() {
        return changedDeclarations;
    }
    
    public Collection<String> getChangedUniqueNames() {
        if (changedUniqNames == null){
            changedUniqNames = new HashSet();
            for (Iterator<CsmDeclaration> it = getChangedDeclarations().iterator(); it.hasNext();){
                CsmDeclaration decl = it.next();
                changedUniqNames.add(decl.getUniqueName());
            }
        }
        return changedUniqNames;
    }
   
    public Collection<CsmProject> getChangedProjects() {
        return changedProjects;
    }
    
    public Map<CsmNamespace,CsmProject> getNewNamespaces() {
        return newNamespaces;
    }
    
}
