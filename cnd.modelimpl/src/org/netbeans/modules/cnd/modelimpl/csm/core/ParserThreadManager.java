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
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
public class ParserThreadManager  {
    
    private static ParserThreadManager instance;
    
    private static final String threadNameBase = "Code Model Parser";
    private RequestProcessor processor;
    private Set/*<Thread>*/ threads = Collections.synchronizedSet(new HashSet()/*<Thread>*/);
    private int currThread = 0;
    
    private class Wrapper implements Runnable {
        
        private Runnable delegate;
        
        public Wrapper(Runnable delegate) {
            this.delegate = delegate;
        }
        
        public void run() {
            try {
                Thread.currentThread().setName(threadNameBase + ' ' + currThread++);
                threads.add(Thread.currentThread());
                delegate.run();
            }
            finally {
                threads.remove(Thread.currentThread());
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
            
    public void init(boolean standalone) {
        
        int threadCount = Integer.getInteger("cnd.modelimpl.parser.threads",
                Math.max(Runtime.getRuntime().availableProcessors()-1, 1)).intValue();
        
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

    /**
     * For stand-alone usage only
     */
    public void shutdown() {
        for (Iterator it = new ArrayList(threads).iterator(); it.hasNext();) {
            Thread thread = (Thread) it.next();
            thread.interrupt();
        }
    }
    
    public boolean isParserThread() {
        if( isStandalone() ) {
            Thread current = Thread.currentThread();
	    for (Iterator it = threads.iterator(); it.hasNext();) {
		if( it.next() == current ) {
		    return true;
		}
	    }
	    return false;
        }
        else {
            return processor.isRequestProcessorThread();
        }
    }      
}
