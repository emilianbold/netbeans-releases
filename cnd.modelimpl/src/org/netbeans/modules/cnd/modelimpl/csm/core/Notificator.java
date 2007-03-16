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

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * Coordiates all change notifications.
 * Singleton.
 * @author vk155633
 */
public class Notificator {

    private static Notificator instance =  new Notificator();
    private int transactionLevel = 0;
    private ChangeEventImpl currEvent;
    private ModelImpl model;

    private Notificator() {
	model = (ModelImpl) CsmModelAccessor.getModel();
    }

    public static Notificator instance() {
	return instance;
    }
    
    private String getCurrThreadString() {
        Thread thread = Thread.currentThread();
        return thread.getName() + ' ' + thread.hashCode();
    }
    
    public void startTransaction() {
	synchronized( this ) {
	    transactionLevel++;
            if( TraceFlags.DEBUG ) Diagnostic.trace("    > " + transactionLevel + ' ' + getCurrThreadString()); // NOI18N
	    resetEvent();
	}
    }
    
    public void endTransaction() {
	synchronized( this ) {
	    transactionLevel--;
            if( TraceFlags.DEBUG ) Diagnostic.trace("    < " + transactionLevel + ' ' + getCurrThreadString()); // NOI18N
	    if( transactionLevel <= 0 ) {
		flush();
	    }
	}
    }
    
    private ChangeEventImpl getEvent() {
	if( currEvent == null ) {
	    //synchronized( this ) {
		//if( currEvent == null ) {
                    // TODO: think over, whether this does not contain a well-known double-check problem
                    ChangeEventImpl ev = new ChangeEventImpl(this);
		    currEvent = ev;
		//}
	    //}
	} 
	return currEvent;
    }
    
    private void resetEvent() {
	currEvent = null;
    }
    
    private boolean isEventEmpty() {
        return currEvent == null || currEvent.isEmpty();
    }
    
    public void registerNewFile(CsmFile file) {
	synchronized( this ) {
            getEvent().addNewFile(file);
        }
    }

    public void registerRemovedFile(CsmFile file) {
	synchronized( this ) {
            getEvent().addRemovedFile(file);
        }
    }
    
    public void registerChangedFile(CsmFile file) {
	synchronized( this ) {
            getEvent().addChangedFile(file);
        }
    }

    public void registerNewDeclaration(CsmOffsetableDeclaration decl) {
	synchronized( this ) {
            getEvent().addNewDeclaration(decl);
        }
    }

    public void registerRemovedDeclaration(CsmOffsetableDeclaration decl) {
	synchronized( this ) {
            getEvent().addRemovedDeclaration(decl);
        }
    }
    
    public void registerChangedDeclaration(CsmOffsetableDeclaration decl) {
	synchronized( this ) {
            getEvent().addChangedDeclaration(decl);
        }
    }

    public void registerNewNamespace(CsmNamespace ns) {
	synchronized( this ) {
            getEvent().addNewNamespace(ns);
        }
    }
    
    public void registerRemoveNamespace(CsmNamespace ns) {
	synchronized( this ) {
            getEvent().addRemovedNamespace(ns);
        }
    }    

    /**
     * Generally, we should rely on hashCode() and equals()
     * of the CsmFile && CsmDeclaration.
     *
     * But for now (last day of Deimos project) it's much easier
     * to ensure this here than to write/test/debug hashCode() and equals()
     *
     * TODO: ensure correct hashCode() and equals()
     * in CsmFile && CsmDeclaration, remove IdMaker and related
     */
    private interface IdMaker {
	String id(Object o);
    }
    
    public void flush() {
        
	ChangeEventImpl ev;
        
	synchronized( this ) {
            transactionLevel = 0;
            if( isEventEmpty() ) {
                return;
            }
	    ev = getEvent();
	    resetEvent();
	}
        
	IdMaker idMaker;
        
	idMaker = new IdMaker() {
	    public String id(Object o) {
		return ((CsmFile) o).getAbsolutePath();
	    }
	};
	process(idMaker, ev.getNewFiles(), ev.getRemovedFiles(), ev.getChangedFiles());
        
	idMaker = new IdMaker() {
	    public String id(Object o) {
		return ((CsmDeclaration) o).getUniqueName();
	    }
	};
	process(idMaker, ev.getNewDeclarations(), ev.getRemovedDeclarations(), ev.getChangedDeclarations());
        
        gatherProjects(ev);
        
	if( model != null ) {
	    model.fireModelChanged(ev);
	}
    }
    
    private static void gatherProjects(ChangeEventImpl ev) {
        Collection/*<CsmProject>*/ projects = ev.getChangedProjects();
        Collection/*CsmFile*/[] files = new Collection/*CsmFile*/[] { ev.getNewFiles(), ev.getChangedFiles(), ev.getRemovedFiles() };
        for( int i = 0; i < files.length; i++ ) {
            for( Iterator iter = files[i].iterator(); iter.hasNext(); ) {
                projects.add(((CsmFile) iter.next()).getProject());
            }
        }
        Collection/*CsmDeclaration*/[] decls = new Collection/*CsmDeclaration*/[] { ev.getNewDeclarations(), ev.getChangedDeclarations(), ev.getRemovedDeclarations() };
        for( int i = 0; i < decls.length; i++ ) {
            for( Iterator iter = decls[i].iterator(); iter.hasNext(); ) {
                Object o = iter.next();
                if( o instanceof CsmOffsetableDeclaration ) {
                    projects.add( ((CsmOffsetableDeclaration) o).getContainingFile().getProject());
                }
            }
        }
    }
    
    private static void process(IdMaker idMaker, Collection added, Collection removed, Collection changed) {
	
	
	Set idsAdded = new HashSet();
	for( Iterator iter = added.iterator(); iter.hasNext(); ) {
	    idsAdded.add(idMaker.id(iter.next()));
	}
	
	Set idsRemoved = new HashSet();
	for( Iterator iter = removed.iterator(); iter.hasNext(); ) {
	    idsRemoved.add(idMaker.id(iter.next()));
	}
	
	Set rightAdded = new HashSet();
	Set rightRemoved = new HashSet();

        for( Iterator iter = removed.iterator(); iter.hasNext(); ) {
	    Object o = iter.next();
	    String id = idMaker.id(o);
	    if( idsAdded.contains(id)) {
		changed.add(o);
	    }
	    else {
		rightRemoved.add(o);
	    }
	}

	for( Iterator iter = added.iterator(); iter.hasNext(); ) {
	    Object o = iter.next();
	    String id = idMaker.id(o);
	    if( ! idsRemoved.contains(id)) {
		rightAdded.add(o);
	    }
	}
	
	added.clear();
	added.addAll(rightAdded);
	
	removed.clear();
	removed.addAll(rightRemoved);
    }

}
