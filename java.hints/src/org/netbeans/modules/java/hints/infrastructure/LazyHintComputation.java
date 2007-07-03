/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.java.hints.infrastructure;
import org.netbeans.modules.java.hints.infrastructure.CreatorBasedLazyFixList;
import org.netbeans.modules.java.hints.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class LazyHintComputation implements CancellableTask<CompilationInfo> {
    
    private FileObject file;
    
    /** Creates a new instance of LazyHintComputation */
    public LazyHintComputation(FileObject file) {
        this.file = file;
    }

    public synchronized void cancel() {
        cancelled.set(true);
        if (delegate != null) {
            delegate.cancel();
        }
    }

    private synchronized void setDelegate(CreatorBasedLazyFixList delegate) {
        this.delegate = delegate;
    }
    
    private AtomicBoolean cancelled = new AtomicBoolean();
    private CreatorBasedLazyFixList delegate;
    private boolean isCancelled() {
        return cancelled.get();
    }
    
    private void resume() {
        cancelled.set(false);
    }
    
    public void run(CompilationInfo info) {
        resume();
        
        boolean cancelled = false;
        
        List<CreatorBasedLazyFixList> toCompute = new LinkedList<CreatorBasedLazyFixList>();
        
        try {
            toCompute.addAll(LazyHintComputationFactory.getAndClearToCompute(file));
            
            if (isCancelled()) {
                cancelled = true;
                return;
            }
            
            while (!toCompute.isEmpty()) {
                if (isCancelled()) {
                    cancelled = true;
                    return;
                }
                
                CreatorBasedLazyFixList l = toCompute.remove(0);
                
                setDelegate(l);
                l.compute(info, this.cancelled);
                setDelegate(null);
                
                if (isCancelled()) {
                    toCompute.add(0, l);
                    cancelled = true;
                    return;
                }
            }
        } finally {
            if (cancelled && !toCompute.isEmpty()) {
                for (CreatorBasedLazyFixList l : toCompute) {
                    LazyHintComputationFactory.addToCompute(file, l);
                }
            }
        }
    }
    
}
