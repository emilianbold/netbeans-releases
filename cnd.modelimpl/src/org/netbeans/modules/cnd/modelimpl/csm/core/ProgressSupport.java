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

import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.util.WeakList;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 * @author Alexander Simon
 */
public class ProgressSupport {
    private static ProgressSupport instance = new ProgressSupport();
    private WeakList<CsmProgressListener> progressListeners = new WeakList<CsmProgressListener>();
    
    /** Creates a new instance of ProgressSupport */
    private ProgressSupport() {
    }
    
    /*package-local*/ static ProgressSupport instance() {
        return instance;
    }
    
    /*package-local*/ void addProgressListener(CsmProgressListener listener) {
        progressListeners.add(listener);
    }
    
    /*package-local*/ void removeProgressListener(CsmProgressListener listener) {
        progressListeners.remove(listener);
    }
    
    /*package-local*/ Iterator<CsmProgressListener> getProgressListeners() {
        return progressListeners.iterator();
    }
   
    
    /*package-local*/ void fireFileInvalidated(FileImpl file) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileInvalidated " + file.getAbsolutePath());
        for( CsmProgressListener listener : progressListeners ) {
            listener.fileInvalidated(file);
        }
    }
    
    /*package-local*/ void fireFileParsingStarted(FileImpl file) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileParsingStarted " + file.getAbsolutePath());
        for( CsmProgressListener listener : progressListeners ) {
            listener.fileParsingStarted(file);
        }
    }
    
    
    /*package-local*/ void fireFileParsingFinished(FileImpl file) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileParsingFinished " + file.getAbsolutePath());
        for( CsmProgressListener listener : progressListeners ) {
            listener.fileParsingFinished(file);
        }
    }
    
    /*package-local*/ void fireProjectParsingStarted(ProjectBase project) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireProjectParsingStarted " + project.getName());
        for( CsmProgressListener listener : progressListeners ) {
            listener.projectParsingStarted(project);
        }
    }
    
    /*package-local*/ void fireProjectParsingFinished(ProjectBase project) {
        for( CsmProgressListener listener : progressListeners ) {
            listener.projectParsingFinished(project);
        }
    }
    
   /*package-local*/ void fireIdle() {
        for( CsmProgressListener listener : progressListeners ) {
            listener.parserIdle();
        }
    }
    
    /*package-local*/ void fireProjectFilesCounted(ProjectBase project, int cnt){
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireProjectFilesCounted " + project.getName() + ' ' + cnt);
        for( CsmProgressListener listener : progressListeners ) {
            listener.projectFilesCounted(project, cnt);
        }
    }
}
