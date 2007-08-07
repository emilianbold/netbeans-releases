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
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.Tree;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.java.source.support.CancellableTreeScanner;

/**
 *
 * @author Jan Lahoda
 */
public abstract class ScanningCancellableTask<T> implements CancellableTask<T> {

    protected AtomicBoolean canceled = new AtomicBoolean();

    /** Creates a new instance of ScanningCancellableTask */
    protected ScanningCancellableTask() {
    }

    public final synchronized void cancel() {
        canceled.set(true);
        
        if (pathScanner != null) {
            pathScanner.cancel();
        }
        if (scanner != null) {
            scanner.cancel();
        }
    }

    public abstract void run(T parameter) throws Exception;
    
    protected final synchronized boolean isCancelled() {
        return canceled.get();
    }
    
    protected final synchronized void resume() {
        canceled.set(false);
    }
    
    private CancellableTreePathScanner pathScanner;
    private CancellableTreeScanner     scanner;
    
    protected <R, P> R scan(CancellableTreePathScanner<R, P> scanner, Tree toScan, P p) {
        if (isCancelled())
            return null;
        
        try {
            synchronized (this) {
                this.pathScanner = scanner;
            }
            
            if (isCancelled())
                return null;
            
            return scanner.scan(toScan, p);
        } finally {
            synchronized (this) {
                this.pathScanner = null;
            }
        }
    }

    protected <R, P> R scan(CancellableTreeScanner<R, P> scanner, Tree toScan, P p) {
        if (isCancelled())
            return null;
        
        try {
            synchronized (this) {
                this.scanner = scanner;
            }
            
            if (isCancelled())
                return null;
            
            return scanner.scan(toScan, p);
        } finally {
            synchronized (this) {
                this.scanner = null;
            }
        }
    }
    
}
