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
import java.awt.Image;
import java.util.Enumeration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.classview.Diagnostic;
import org.openide.nodes.*;
/**
 *
 * @author vk155633
 */
class SmartChangeEvent {

    private Collection/*<CsmNamespace>*/ newNamespaces;
    private Collection/*<CsmDeclaration>*/ newDeclarations;
    private Collection/*<CsmDeclaration>*/ removedDeclarations;
    private Collection/*<CsmDeclaration>*/ changedDeclarations;
    
    /** Creates a new instance of SmartChangeEvent */
    public SmartChangeEvent(CsmChangeEvent ev, CsmProject project) {

        newDeclarations = new LinkedList/*<CsmDeclaration>*/();
        removedDeclarations = new LinkedList/*<CsmDeclaration>*/();
        changedDeclarations = new LinkedList/*<CsmDeclaration>*/();
        
        if( ev.getChangedProjects().contains(project) ) {
            newNamespaces = new LinkedList/*<CsmNamespace>*/(ev.getNewNamespaces());
        }
        else {
            newNamespaces = new LinkedList/*<CsmNamespace>*/();
            return;
        }
        
        addProjectDeclarations(newDeclarations, ev.getNewDeclarations(), project);
        addProjectDeclarations(removedDeclarations, ev.getRemovedDeclarations(), project);
        addProjectDeclarations(changedDeclarations, ev.getChangedDeclarations(), project);
    }
    
    private void addProjectDeclarations(Collection/*<CsmDeclaration>*/ toAdd, Collection/*<CsmDeclaration>*/ declarations, CsmProject project) {
        for (Iterator it = declarations.iterator(); it.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            if( decl instanceof CsmOffsetable ) { // paranoya
                if( ((CsmOffsetable) decl).getContainingFile().getProject() == project  ) {
                    toAdd.add(decl);
                }
            }
        }        
    }
    
    public Collection/*<CsmNamespace>*/ getNewNamespaces() {        
        return newNamespaces;
    }
    
    public Collection/*<CsmDeclaration>*/ getNewDeclarations() {
        return newDeclarations;
    }
    
    public Collection/*<CsmDeclaration>*/ getRemovedDeclarations() {
        return removedDeclarations;
        
    }
    
    public Collection/*<CsmDeclaration>*/ getChangedDeclarations() {
        return changedDeclarations;
    } 
    
}
