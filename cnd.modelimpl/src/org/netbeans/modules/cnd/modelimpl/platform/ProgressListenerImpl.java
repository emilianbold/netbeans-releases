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

package org.netbeans.modules.cnd.modelimpl.platform;

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.TraceFlags;

/**
 *
 * @author vk155633
 */
public class ProgressListenerImpl implements CsmProgressListener {
    
    private Map/*<CsmProject, ParsingProgress>*/ handles = new HashMap();
    
    private synchronized ParsingProgress getHandle(CsmProject project, boolean createIfNeed) {
        ParsingProgress handle = (ParsingProgress) handles.get(project); 
        if( handle == null && createIfNeed ) {
            handle = new ParsingProgress(project);
            handles.put(project, handle);
        }
        return handle;
    }
    
    public void projectParsingStarted(CsmProject project) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ProgressListenerImpl.projectParsingStarted " + project.getName());
        getHandle(project, true).start();
    }

    public void projectFilesCounted(CsmProject project, int filesCount) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ProgressListenerImpl.projectFilesCounted " + project.getName() + ' ' + filesCount);
        getHandle(project, true).switchToDeterminate(filesCount);
    }

    public void projectParsingFinished(CsmProject project) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ProgressListenerImpl.projectParsingFinished " + project.getName());
	done(project);
    }

    public void projectParsingCancelled(CsmProject project) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ProgressListenerImpl.projectParsingCancelled " + project.getName());
	done(project);
    }
    
    private void done(CsmProject project) {
        getHandle(project, true).finish();
        synchronized( this ) {
            handles.remove(project);
        }
    }
    
    public void fileParsingStarted(CsmFile file) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("  ProgressListenerImpl.fileParsingStarted " + file.getAbsolutePath());
        ParsingProgress handle = getHandle(file.getProject(), false);
        if( handle != null ) {
            handle.nextCsmFile(file);
        }
    }

    public void fileParsingFinished(CsmFile file) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("  ProgressListenerImpl.fileParsingFinished " + file.getAbsolutePath());
    }
    
    public void parserIdle() {
	if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("  ProgressListenerImpl.parserIdle");
    }
    
}
