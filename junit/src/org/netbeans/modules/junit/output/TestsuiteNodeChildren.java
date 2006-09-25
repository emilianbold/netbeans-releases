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

package org.netbeans.modules.junit.output;

import java.util.Collections;
import java.util.Iterator;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
final class TestsuiteNodeChildren extends Children.Keys<Report.Testcase> {

    /** */
    private static final Node[] EMPTY_NODE_ARRAY = new Node[0];

    /** */
    private final Report report;
    /** */
    private boolean filtered;
    /** */
    private boolean live = true;         //PENDING - temporary (should be false)

    /*
     * PENDING - threading, sychronization
     */
    
    /**
     * Creates a new instance of TestsuiteNodeChildren
     */
    TestsuiteNodeChildren(final Report report, final boolean filtered) {
        this.report = report;
        this.filtered = filtered;
    }
    
    /**
     */
    protected void addNotify() {
        super.addNotify();
        
        if (live) {
            setKeys(report.getTests());
        }
        //live = true;                          //PENDING
    }
    
    /**
     */
    protected void removeNotify() {
        super.removeNotify();
        
        setKeys(Collections.EMPTY_SET);
        //live = false;                         //PENDING
    }
    
    /**
     */
    protected Node[] createNodes(final Report.Testcase testcase) {
        if (filtered && (testcase.trouble == null)) {
            return EMPTY_NODE_ARRAY;
        }
        return new Node[] {new TestMethodNode(testcase)};
    }
    
    /**
     */
    void setFiltered(final boolean filtered) {
        if (filtered == this.filtered) {
            return;
        }
        this.filtered = filtered;
        
        if ((report.errors + report.failures) == report.totalTests) {
            return;
        }
                
        if (isInitialized()) {
            for (Report.Testcase testcase : report.getTests()) {
                if (testcase.trouble == null) {
                    refreshKey(testcase);
                }
            }
        }
    }
    
}
