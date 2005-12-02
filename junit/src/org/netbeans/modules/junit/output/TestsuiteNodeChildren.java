/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
final class TestsuiteNodeChildren extends Children.Keys {
    
    /** */
    private static final Node[] EMPTY_NODE_ARRAY = new Node[0];
    
    /** */
    private final Report report;
    /** */
    private boolean filtered = false;
    /** */
    private boolean live = true;         //PENDING - temporary (should be false)
    
    /*
     * PENDING - threading, sychronization
     */
    
    /**
     * Creates a new instance of TestsuiteNodeChildren
     */
    TestsuiteNodeChildren(final Report report) {
        this.report = report;
    }
    
    /**
     */
    protected void addNotify() {
        super.addNotify();
        
        update();
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
    void update() {
        if (live) {
            setKeys(report.isClosed() ? report.getTests()
                                      : Collections.EMPTY_SET);
        }
    }
    
    /**
     */
    protected Node[] createNodes(Object key) {
        final Report.Testcase testcase = (Report.Testcase) key;
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
        
        if (isInitialized()) {
            for (Iterator i = report.getTests().iterator(); i.hasNext(); ) {
                Report.Testcase testcase = (Report.Testcase) i.next();
                if (testcase.trouble == null) {
                    refreshKey(testcase);
                }
            }
        }
    }
    
}
