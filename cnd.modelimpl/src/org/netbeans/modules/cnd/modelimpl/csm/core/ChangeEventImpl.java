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

package org.netbeans.modules.cnd.modelimpl.csm.core;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;

/**
 * CsmChangeEvent implementation
 * @author vk155633
 */
public class ChangeEventImpl extends CsmChangeEvent {

    protected Set/*<CsmFile>*/ newFiles;
    protected Set/*<CsmFile>*/ removedFiles;
    protected Set/*<CsmFile>*/ changedFiles;
    
    protected Set/*<CsmDeclaration>*/ newDeclarations;
    protected Set/*<CsmDeclaration>*/ removedDeclarations;
    protected Set/*<CsmDeclaration>*/ changedDeclarations;
    
    protected Set/*<CsmProject>*/   changedProjects;
    
    protected Map<String, CsmNamespace>   newNamespaces;
    protected Map<String, CsmNamespace>   removedNamespaces;
    
    public ChangeEventImpl(Object source) {
	super(source);
    }
    
    public Collection/*<CsmFile>*/ getNewFiles() {
	if( newFiles == null ) {
	    newFiles = new HashSet/*CsmFile*/();
	}
	return newFiles;
    }
    
    public Collection/*<CsmFile>*/ getRemovedFiles() {
	if( removedFiles == null ) {
	    removedFiles = new HashSet/*CsmFile*/();
	}
	return removedFiles;
    }
    
    public Collection/*<CsmFile>*/ getChangedFiles() {
	if( changedFiles == null ) {
	    changedFiles = new HashSet/*CsmFile*/();
	}
	return changedFiles;
    }
    
    public Collection/*<CsmDeclaration>*/ getNewDeclarations() {
	if( newDeclarations == null ) {
	    newDeclarations = new HashSet/*CsmDeclaration*/();
	}
	return newDeclarations;
    }
    
    public Collection/*<CsmDeclaration>*/ getRemovedDeclarations() { 
	if( removedDeclarations == null ) { 
	    removedDeclarations = new HashSet/*CsmDeclaration*/(); 
	} 
	return removedDeclarations; 
    }
    
    public Collection/*<CsmDeclaration>*/ getChangedDeclarations() { 
	if( changedDeclarations == null ) { 
	    changedDeclarations = new HashSet/*CsmDeclaration*/(); 
	} 
	return changedDeclarations; 
    }
    
    public Collection/*<CsmProject>*/ getChangedProjects() {
        if( changedProjects == null ) {
            changedProjects = new HashSet/*<CsmProject>*/();
        }
        return changedProjects;
    }
    
    public Collection/*<CsmNamespace>*/ getNewNamespaces() {
        if( newNamespaces != null ) {
            return newNamespaces.values();
        }
        return Collections.EMPTY_LIST;
    }
    
    public Collection/*<CsmNamespace>*/ getRemovedNamespaces() {
        if( removedNamespaces != null ) {
            return removedNamespaces.values();
        }
        return Collections.EMPTY_LIST;
    }
    
    public boolean isEmpty() {
        return 
            (changedProjects == null || changedProjects.isEmpty()) &&
            (newFiles == null || newFiles.isEmpty()) && 
            (changedFiles == null || changedFiles.isEmpty()) &&
            (removedFiles == null || removedFiles.isEmpty()) &&
            (newDeclarations == null || newDeclarations.isEmpty()) && 
            (removedDeclarations== null || removedDeclarations.isEmpty()) && 
            (changedDeclarations == null || changedDeclarations.isEmpty()) &&
            (newNamespaces == null || newNamespaces.isEmpty()) &&
            (removedNamespaces == null || removedNamespaces.isEmpty());
    }    
    
    public void addChangedFile(CsmFile file) {
        getChangedFiles().add(file);
        getChangedProjects().add(file.getProject());
    }

    public void addNewFile(CsmFile file) {
        getNewFiles().add(file);
        getChangedProjects().add(file.getProject());
    }
    
    public void addRemovedFile(CsmFile file) {
        getRemovedFiles().add(file);
        getChangedProjects().add(file.getProject());
    }

    public void  addChangedDeclaration(CsmOffsetableDeclaration declaration) {
        getChangedDeclarations().add(declaration);
        addChangedFile(declaration.getContainingFile());
    }

    public void addNewDeclaration(CsmOffsetableDeclaration declaration) {
        getNewDeclarations().add(declaration);
        CsmFile file = declaration.getContainingFile();
        if( ! getNewFiles().contains(file) ) {
            addChangedFile(file);
        }
    }
    
    public void addRemovedDeclaration(CsmOffsetableDeclaration declaration) {
        getRemovedDeclarations().add(declaration);
        CsmFile file = declaration.getContainingFile();
        if( ! getRemovedFiles().contains(file) ) {
            addChangedFile(file);
        }
    }
    
    public void addNewNamespace(CsmNamespace ns) {
        // remove from removed
        if (_getRemovedNamespaces().remove(ns.getQualifiedName()) == null) {
            _getNewNamespaces().put(ns.getQualifiedName(), ns);
            getChangedProjects().add(ns.getProject());
        }
    }
    
    public void addRemovedNamespace(CsmNamespace ns) {
        // put in removed only if not added as "new"
        if (_getNewNamespaces().get(ns.getQualifiedName()) == null) {
            _getRemovedNamespaces().put(ns.getQualifiedName(), ns);
            getChangedProjects().add(ns.getProject());
        }
    }   
    
    private Map<String, CsmNamespace> _getRemovedNamespaces() {
        if (removedNamespaces == null) {
            removedNamespaces = new HashMap<String, CsmNamespace>();            
        }
        return removedNamespaces;
    }
    
    private Map<String,CsmNamespace> _getNewNamespaces() {
        if (newNamespaces == null) {
            newNamespaces = new HashMap<String, CsmNamespace>();            
        }
        return newNamespaces;
    }    
}
