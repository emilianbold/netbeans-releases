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

import org.netbeans.modules.cnd.modelimpl.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.modelimpl.csm.Diagnostic;

/**
 *
 * @author vk155633
 */
public class ParserThread implements Runnable {
    
    public void run() {
        ParserQueue queue = ParserQueue.instance();
        while( true ) {
            if( TraceFlags.TRACE_PARSER_QUEUE ) trace("polling queue");
            ParserQueue.Entry entry = queue.poll();
            if( entry == null ) {
                try {
                    if( TraceFlags.TRACE_PARSER_QUEUE ) trace("waiting");
                    queue.waitReady();
                } catch (InterruptedException ex) {
                    break;
                }
            }
            else {
                FileImpl file = entry.getFile();
                if( TraceFlags.TRACE_PARSER_QUEUE ) trace("parsing started: " + file.getName());
		Diagnostic.StopWatch stw = (TraceFlags.TIMING_PARSE_PER_FILE_FLAT && ! file.isParsed()) ? new Diagnostic.StopWatch() : null;
                if (TraceFlags.USE_APT) {
                    APTPreprocState preprocState = file.getProjectImpl().createDefaultPreprocState(null);
                    preprocState.setState(entry.getPreprocStateState());
                    file.ensureParsed(preprocState);
                } else {
                    file.ensureParsed(entry.getCallback());
                }
                if( stw != null ) stw.stopAndReport("parsing " + file.getAbsolutePath());
                queue.onFileParsingFinished(file);
                if( TraceFlags.TRACE_PARSER_QUEUE ) trace("parsing done: " + file.getName());
                Notificator.instance().flush();
                if( TraceFlags.TRACE_PARSER_QUEUE ) trace("model event flushed");
            }
        }
    }
    
    private void trace(String text) {
        System.err.println(Thread.currentThread().getName() + ": " + text);
    }
    
}
