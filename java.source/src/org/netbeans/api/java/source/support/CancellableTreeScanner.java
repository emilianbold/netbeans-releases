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
package org.netbeans.api.java.source.support;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;

/**
 *
 * @author Jan Lahoda
 */
public class CancellableTreeScanner<R,P> extends TreeScanner<R,P> {

    private boolean canceled;

    /**
     * Creates a new instance of CancellableTreeScanner
     */
    public CancellableTreeScanner() {
    }

    protected synchronized boolean isCanceled() {
        return canceled;
    }

    public synchronized void cancel() {
        canceled = true;
    }

    /** @inheritDoc
     */
    public R scan(Tree tree, P p) {
        if (isCanceled())
            return null;
        
        return super.scan(tree, p);
    }

    /** @inheritDoc
     */
    public R scan(Iterable<? extends Tree> trees, P p) {
        if (isCanceled())
            return null;
        
        return super.scan(trees, p);
    }

}
