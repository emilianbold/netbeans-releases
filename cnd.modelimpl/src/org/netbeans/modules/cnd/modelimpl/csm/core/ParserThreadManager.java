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
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
public class ParserThreadManager  {
    
    private static ParserThreadManager instance;
    
    private static final String threadNameBase = "Code Model Parser"; // NOI18N
    private RequestProcessor processor;
    private Set<Wrapper> wrappers = Collections.synchronizedSet(new HashSet<Wrapper>());
    private int currThread = 0;
    
    private class Wrapper implements Runnable {
        
        private ParserThread delegate;
        private Thread thread;
        
        public Wrapper(ParserThread delegate) {
            this.delegate = delegate;
        }
        
        public void stop() {
            assert this.delegate != null;
            this.delegate.stop();
        }
        
        public void run() {
            try {
                thread = Thread.currentThread();
                thread.setName(threadNameBase + ' ' + currThread++);
                wrappers.add(this);
                delegate.run();
            }
            finally {
                wrappers.remove(this);
            }
        }
    }

    private ParserThreadManager() {
    }
    
    public static synchronized ParserThreadManager instance() {
        if( instance == null ) {
            instance = new ParserThreadManager();
        }
        return instance;
    }
    
    public boolean isStandalone() {
        return (processor == null);
    }
            
    // package-local
    void startup(boolean standalone) {
        
	ParserQueue.instance().startup();
	
//        int threadCount = Integer.getInteger("cnd.modelimpl.parser.wrappers",
//                Math.max(Runtime.getRuntime().availableProcessors()-1, 1)).intValue();

        int threadCount = Integer.getInteger("cnd.modelimpl.parser.threads",
		Runtime.getRuntime().availableProcessors()).intValue(); // NOI18N

	threadCount = Math.min(threadCount, 4);
	threadCount = Math.max(threadCount, 1);
	
        
        if( ! standalone ) {
            processor = new RequestProcessor(threadNameBase, threadCount);
        }
        for (int i = 0; i < threadCount; i++) {
            Runnable r = new Wrapper(new ParserThread());
            if( standalone ) {
                new Thread(r).start();
            }
            else {
                processor.post(r);
            }
        }
    }

    
    // package-local
    void shutdown() {
	if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("=== ParserThreadManager.shutdown");
        for (Wrapper wrapper : wrappers) {
            wrapper.stop();
        }  
	ParserQueue.instance().shutdown();
    }
    
    public boolean isParserThread() {
        if( isStandalone() ) {
            Thread current = Thread.currentThread();
            for (Wrapper wrapper : wrappers) {
                if (wrapper.thread == current) {
                    return true;
                }
            }
	    return false;
        } else {
            return processor.isRequestProcessorThread();
        }
    }      

    public void waitEmptyProjectQueue(ProjectBase prj) {
        ParserQueue.instance().waitEmpty(prj);
    }
}
