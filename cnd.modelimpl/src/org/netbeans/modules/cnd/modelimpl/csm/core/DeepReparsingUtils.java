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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * Reparse dependant files.
 * @author Alexander Simon
 */
public final class DeepReparsingUtils {
    
    private DeepReparsingUtils() {
    }
    
    /**
     * Reparse including/included files at fileImpl content changed.
     */
    public static void reparseOnEdit(FileImpl fileImpl, ProjectBase project) {
	reparseOnEdit(fileImpl, project, true);
    }
    /**
     * Reparse including/included files at fileImpl content changed.
     */
    public static void reparseOnEdit(FileImpl fileImpl, ProjectBase project, boolean scheduleParsing) {
        Set<CsmFile> topParents = project.getGraph().getTopParentFiles(fileImpl);
        if (topParents.size()>0){
            Set<CsmFile> coherence = project.getGraph().getCoherenceFiles(fileImpl);
            for(CsmFile parent : coherence){
                if (!topParents.contains(parent)){
                    invalidateFileAndPreprocState(project, parent);
                }
            }
	    if( scheduleParsing ) {
		addToReparse(project, topParents, false);
	    }
        } else {
	    if( scheduleParsing ) {
		ParserQueue.instance().addFirst(fileImpl, project.getPreprocHandler(fileImpl.getBuffer().getFile()).getState(), false);
	    }
        }
    }
    
    /**
     * Reparse including/included files at file properties changed.
     */
    public static void reparseOnPropertyChanged(NativeFileItem nativeFile, ProjectBase project) {
        FileImpl file = project.getFile(nativeFile.getFile());
        if( file == null ) {
            return;
        }
        if (TraceFlags.USE_DEEP_REPARSING) {
            Set<CsmFile> top = project.getGraph().getTopParentFiles(file);
            Set<CsmFile> coherence = project.getGraph().getIncludedFiles(file);
            for(CsmFile parent : coherence){
                if (!top.contains(parent)){
                    invalidateFileAndPreprocState(project, parent);
                }
            }
            addToReparse(project, nativeFile, file);
            top.remove(file);
            addToReparse(project, top, false);
        } else {
            addToReparse(project, nativeFile, file);
        }
    }
    
    /**
     * Reparse including/included files at file properties changed.
     */
    public static void reparseOnPropertyChanged(List<NativeFileItem> items, ProjectBase project) {
        try {
            ParserQueue.instance().onStartAddingProjectFiles(project);
            if (TraceFlags.USE_DEEP_REPARSING) {
                Map<FileImpl,NativeFileItem> pairs = new HashMap<FileImpl,NativeFileItem>();
                Set<CsmFile> top = new HashSet<CsmFile>();
                Set<CsmFile> coherence = new HashSet<CsmFile>();
                for(NativeFileItem item : items) {
                    FileImpl file = project.getFile(item.getFile());
                    if( file != null ) {
                        pairs.put(file,item);
                        top.addAll(project.getGraph().getTopParentFiles(file));
                        coherence.addAll(project.getGraph().getIncludedFiles(file));
                    }
                }
                for(CsmFile parent : coherence){
                    if (!top.contains(parent)){
                        invalidateFileAndPreprocState(project, parent);
                    }
                }
                for(CsmFile parent : top){
                    if (parent.getProject() == project){
                        FileImpl parentImpl = (FileImpl) parent;
                        if (pairs.containsKey(parentImpl)) {
                            NativeFileItem item = pairs.get(parentImpl);
                            addToReparse(project, item, parentImpl);
                        } else {
                            addToReparse(project, parentImpl, true);
                        }
                    }
                }
            } else {
                for(NativeFileItem item : items) {
                    FileImpl file = project.getFile(item.getFile());
                    if( file != null ) {
                        addToReparse(project, item, file);
                    }
                }
            }
        } catch( Exception e ) {
            e.printStackTrace(System.err);
        } finally {
            ParserQueue.instance().onEndAddingProjectFiles(project);
        }
    }
    
    /**
     * Reparse included files at file added.
     */
    public static void reparseOnAdded(NativeFileItem nativeFile, ProjectBase project){
        if (!TraceFlags.USE_DEEP_REPARSING){
            return;
        }
        String name = nativeFile.getFile().getName();
        Set<CsmFile> resolved = new HashSet<CsmFile>();
        for(CsmFile file : project.getSourceFiles()){
            for(CsmInclude incl : file.getIncludes()){
                if (incl.getIncludeName().endsWith(name)/* && incl.getIncludeFile() == null*/){
                    resolved.add(file);
                    break;
                }
            }
        }
        if (resolved.size() > 0){
            Set<CsmFile> top = new HashSet<CsmFile>();
            Set<CsmFile> coherence = new HashSet<CsmFile>();
            for(CsmFile file : resolved) {
                top.addAll(project.getGraph().getTopParentFiles(file));
                coherence.addAll(project.getGraph().getIncludedFiles(file));
            }
            for(CsmFile parent : coherence){
                if (!top.contains(parent)){
                    invalidateFileAndPreprocState(project, parent);
                }
            }
            addToReparse(project, top, true);
        }
    }

    static void reparseOnAdded(List<NativeFileItem> toReparse, ProjectBase project) {
        if (!TraceFlags.USE_DEEP_REPARSING){
            return;
        }
        Set<String> names = new HashSet<String>();
        for(NativeFileItem item : toReparse){
            names.add(item.getFile().getName());
        }
        Set<CsmFile> resolved = new HashSet<CsmFile>();
        for(CsmFile file : project.getSourceFiles()){
            findResolved(names, resolved, file);
        }
        for(CsmFile file : project.getHeaderFiles()){
            findResolved(names, resolved, file);
        }
        if (resolved.size() > 0){
            Set<CsmFile> top = new HashSet<CsmFile>();
            Set<CsmFile> coherence = new HashSet<CsmFile>();
            for(CsmFile file : resolved) {
                top.addAll(project.getGraph().getTopParentFiles(file));
                coherence.addAll(project.getGraph().getIncludedFiles(file));
            }
            for(CsmFile parent : coherence){
                if (!top.contains(parent)){
                    invalidateFileAndPreprocState(project, parent);
                }
            }
            addToReparse(project, top, true);
        }
    }

    private static void findResolved(final Set<String> names, final Set<CsmFile> resolved, final CsmFile file) {
        for(CsmInclude incl : file.getIncludes()){
            String name = incl.getIncludeName();
            int i = Math.max(name.lastIndexOf('\\'),name.lastIndexOf('/'));
            if (i > 0){
                name = name.substring(i);
            }
            if (names.contains(name)) {
                resolved.add(file);
                break;
            }
        }
    }
    
    /**
     * Reparse including/included files at file removed.
     */
    public static void reparseOnRemoved(FileImpl impl, ProjectBase project) {
        if (!TraceFlags.USE_DEEP_REPARSING){
            return;
        }
        Set<CsmFile> topParents = project.getGraph().getTopParentFiles(impl);
        Set<CsmFile> coherence = project.getGraph().getCoherenceFiles(impl);
        project.getGraph().removeFile(impl);
        topParents.remove(impl);
        coherence.remove(impl);
        if (topParents.size()>0){
            for(CsmFile parent : coherence){
                if (!topParents.contains(parent)){
                    invalidateFileAndPreprocState(project, parent);
                }
            }
            addToReparse(project, topParents, false);
        }
    }

    static void reparseOnRemoved(List<FileImpl> toReparse, ProjectBase project) {
        if (!TraceFlags.USE_DEEP_REPARSING){
            return;
        }
        Set<CsmFile> topParents = new HashSet<CsmFile>();
        Set<CsmFile> coherence = new HashSet<CsmFile>();
        for (FileImpl impl : toReparse){
            topParents.addAll(project.getGraph().getTopParentFiles(impl));
            coherence.addAll(project.getGraph().getCoherenceFiles(impl));
            project.getGraph().removeFile(impl);
            topParents.remove(impl);
            coherence.remove(impl);
        }
        if (topParents.size()>0){
            for(CsmFile parent : coherence){
                if (!topParents.contains(parent)){
                    invalidateFileAndPreprocState(project, parent);
                }
            }
            addToReparse(project, topParents, false);
        }
    }
    
    private static void addToReparse(final ProjectBase project, final Set<CsmFile> topParents, boolean invalidateCache) {
        boolean progress = false;
        try {
            if (topParents.size()>5) {
                ParserQueue.instance().onStartAddingProjectFiles(project);
                progress = true;
            }
            for(CsmFile parent : topParents){
                if (parent.getProject() == project){
                    FileImpl parentImpl = (FileImpl) parent;
                    addToReparse(project, parentImpl, invalidateCache);
                }
            }
        } catch( Exception e ) {
            e.printStackTrace(System.err);
        } finally{
            if (progress) {
                ParserQueue.instance().onEndAddingProjectFiles(project);
            }
        }
    }

    private static void addToReparse(final ProjectBase project, final FileImpl parentImpl, final boolean invalidateCache) {
        parentImpl.stateChanged(invalidateCache);
        ParserQueue.instance().addFirst(parentImpl, project.getPreprocHandler(parentImpl.getBuffer().getFile()).getState(), false);
        if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
            System.out.println("Add file to reparse "+parentImpl.getAbsolutePath()); // NOI18N
        }
    }
    
    private static void addToReparse(final ProjectBase project, final NativeFileItem nativeFile, final FileImpl file) {
        file.stateChanged(true);
        APTPreprocHandler.State state;
        //if (file.isSourceFile()) {
            state = project.createPreprocHandler(nativeFile).getState();
        //} else {
        //    state = project.getPreprocHandler(nativeFile.getFile()).getState();
        //}
        if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
            System.out.println("Add file to reparse "+file.getAbsolutePath()); // NOI18N
        }
        ParserQueue.instance().addFirst(file, state, false);
    }
    
    
    private static void invalidateFileAndPreprocState(final ProjectBase project, final CsmFile parent) {
        if (parent.getProject() == project){
            FileImpl parentImpl = (FileImpl) parent;
            project.invalidatePreprocState(parentImpl.getBuffer().getFile());
            parentImpl.stateChanged(false);
            if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
                System.out.println("Invalidate file to reparse "+parent.getAbsolutePath()); // NOI18N
            }
        }
    }
}
