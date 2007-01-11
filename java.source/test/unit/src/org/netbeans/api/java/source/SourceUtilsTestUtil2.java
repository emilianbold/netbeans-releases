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
package org.netbeans.api.java.source;

import com.sun.source.tree.Tree;
import org.apache.lucene.store.FSDirectory;
import org.netbeans.api.java.source.transform.Transformer;
import org.netbeans.modules.java.source.ActivatedDocumentListener;

/**
 *
 * @author Jan Lahoda
 */
public final class SourceUtilsTestUtil2 {

    private SourceUtilsTestUtil2() {
    }

    public static void ignoreCompileRequests() {
        ActivatedDocumentListener.IGNORE_COMPILE_REQUESTS = true;
    }

    public static <R, P> void run(WorkingCopy wc, Transformer<R, P> t) {
        wc.run(t);
    }
    
    public static <R, P> void run(WorkingCopy wc, Transformer<R, P> t, Tree tree) {
        wc.run(t, tree);
    }
    
    public static void disableLocks() {
        FSDirectory.setDisableLocks(true);
    }
    
}
