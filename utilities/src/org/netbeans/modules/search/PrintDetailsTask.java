/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openidex.search.SearchGroup;
import org.openidex.search.SearchType;

/**
 *
 * @author  Marian Petras
 */
public class PrintDetailsTask implements Runnable {
    
    /** */
    private static final int BUFFER_SIZE = 8;
    /** */
    private final Node[] nodes;
    /** */
    private final SearchGroup searchGroup;
    /** */
    private final Node[] buffer = new Node[BUFFER_SIZE];
    /** position of the first free item in the buffer */
    private int bufPos = 0;
    /** */
    private SearchDisplayer displayer;
    
    
    /** Creates a new instance of PrintDetailsTask */
    public PrintDetailsTask(final Node[] nodes,
                            final SearchGroup searchGroup) {
        this.nodes = nodes;
        this.searchGroup = searchGroup;
    }
    
    /** */
    public void run() {
        displayer = new SearchDisplayer();
        callDisplayerFromAWT("prepareOutput");                    //NOI18N
        
        final SearchType[] searchTypes = searchGroup.getSearchTypes();        
        
        int freeBufSpace = 0;
        for (int i = 0; i < nodes.length; i++) {

            /* Collect details about the found node: */
            Node[] allDetails = null;
            for (int j = 0; j < searchTypes.length; j++) {
                Node[] details = searchTypes[j].getDetails(nodes[i]);
                if (details == null || details.length == 0) {
                    continue;
                }
                if (allDetails == null) {
                    allDetails = details;
                } else {
                    allDetails = concatNodeArrays(allDetails, details);
                }
            }
            if (allDetails == null) {
                continue;
            }

            /* Print the collected details: */
            freeBufSpace = addToBuffer(allDetails, 0);
            while (freeBufSpace < 0) {
                printBuffer();

                int remainderIndex = allDetails.length + freeBufSpace;
                freeBufSpace = addToBuffer(allDetails, remainderIndex);
            }
            if (freeBufSpace == 0) {
                printBuffer();
            }
        }
        if (freeBufSpace != 0) {
            int smallBufSize = BUFFER_SIZE - freeBufSpace;
            Node[] smallBuffer = new Node[smallBufSize];
            System.arraycopy(buffer, 0, smallBuffer, 0, smallBufSize);
            displayer.displayNodes(smallBuffer);
        }
        callDisplayerFromAWT("finishDisplaying");
    }

    /**
     */
    public Reference getOutputWriterRef() {
        return displayer.getOutputWriterRef();
    }

    /**
     * Adds some or all of the given nodes to the buffer.
     * Nodes at position lesser than the given start index are ignored.
     * If the nodes to be added do not fit all into the buffer, the remaining
     * nodes are ignored.
     *
     * @param  detailNodes  array containing nodes to be added
     * @param  firstIndex  index of the first node to be added to the buffer
     * @return  positive number expressing number of free items in the buffer;
     *          negative number expressing number of remaining nodes that
     *                  did not fit into the buffer;
     *          or <code>0</code> if the nodes exactly filled the buffer
     */
    private int addToBuffer(Node[] detailNodes, int firstIndex) {
        assert firstIndex >=0 && firstIndex <= detailNodes.length;

        int nodesToAddCount = detailNodes.length - firstIndex;
        int newBufPos = bufPos + nodesToAddCount;
        int remainingSpace = BUFFER_SIZE - newBufPos;
        if (remainingSpace <= 0) {
            nodesToAddCount += remainingSpace;
            newBufPos = 0;
        }
        System.arraycopy(detailNodes, firstIndex, buffer, bufPos, nodesToAddCount);
        bufPos = newBufPos;
        return remainingSpace;
    }

    /**
     */
    private void printBuffer() {
        displayer.displayNodes(buffer);
    }

    /**
     */
    private Node[] concatNodeArrays(Node[] arrA, Node[] arrB) {
        Node[] result = new Node[arrA.length + arrB.length];

        System.arraycopy(arrA,   0,
                         result, 0,
                         arrA.length);
        System.arraycopy(arrB,   0,
                         result, arrA.length,
                         arrB.length);
        return result;
    }
    
    /**
     */
    private void callDisplayerFromAWT(final String methodName) {
        try {
            final Method method = SearchDisplayer.class
                                  .getDeclaredMethod(methodName, new Class[0]);
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        method.invoke(displayer, null);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            };
            if (EventQueue.isDispatchThread()) {
                runnable.run();
            } else {
                EventQueue.invokeAndWait(runnable);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
}
