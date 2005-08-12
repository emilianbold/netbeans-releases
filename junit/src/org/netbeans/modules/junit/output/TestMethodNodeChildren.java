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

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
final class TestMethodNodeChildren extends Children.Array {
    
    /** */
    private final Report.Testcase testcase;
    
    /** Creates a new instance of TestMethodNodeChildren */
    public TestMethodNodeChildren(final Report.Testcase testcase) {
        this.testcase = testcase;
    }
    
    /**
     */
    protected void addNotify() {
        final Report.Trouble trouble = testcase.trouble;

        int nodesCount = 1;                     //exception class name
        if (trouble.message != null) {
            nodesCount++;
        }
        if (trouble.stackTrace != null) {
            nodesCount += trouble.stackTrace.length;
        }
        
        final Node[] children = new Node[nodesCount];
        int index = 0;
        if (trouble.message != null) {
            children[index++] = createNoIconNode(trouble.message);
        }
        children[index++] = createNoIconNode(trouble.exceptionClsName);
        for (int i = 0; index < nodesCount; i++) {
            children[index++] = new CallstackFrameNode(trouble.stackTrace[i]);
        }
        add(children);
    }
    
    /**
     */
    private Node createNoIconNode(String displayName) {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setDisplayName(displayName);
        node.setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/empty.gif");     //NOI18N
        return node;
    }
    
    /**
     */
    protected void removeNotify() {
        remove(getNodes());
    }
    
}
