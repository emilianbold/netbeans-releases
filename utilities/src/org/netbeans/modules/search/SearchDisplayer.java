/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.io.IOException;

import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;


/**
 * Presents search results in output window.
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
public class SearchDisplayer extends Object implements NodeAcceptor {

    /** name of attribute &quot;text to display in the Output Window&quot; */
    public static final String ATTR_OUTPUT_LINE = "output line";        //NOI18N
    /** output tab */
    private InputOutput searchIO;
    /** writer to that tab */
    private OutputWriter ow = null;

    /** Creates new SearchDisplayer */
    public SearchDisplayer() {
    }

    private void setOw (String name) {
        searchIO = IOProvider.getDefault().getIO(name, false);
        ow = searchIO.getOut();
    }
    
    private void displayNode(Node node) {
        String outputLine;
        
        Object o = node.getValue(ATTR_OUTPUT_LINE);
        if (o != null && o instanceof String) {
            outputLine = (String) o;
        } else {
            outputLine = node.getShortDescription();
        }
        try {
            if(node instanceof OutputListener)
                ow.println(outputLine, (OutputListener) node);
            else
                ow.println(outputLine);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** Accepted nodes should be displayed.
     * @param nodes the nodes to consider
     * @return <CODE>true</CODE> if so
     */
    public synchronized boolean acceptNodes(Node[] nodes) {

        if (nodes == null) {
            return false;
        }
        if (nodes.length == 0) {
            return true;
        }

        if (ow == null) {
            setOw(NbBundle.getMessage(ResultView.class,
                                      "TITLE_SEARCH_RESULTS"));         //NOI18N
        }
        searchIO.select();
        searchIO.setFocusTaken(true);
        displayNode(nodes[0]);
        searchIO.setFocusTaken(false);
        for (int i = 1; i < nodes.length; i++) {
            displayNode(nodes[i]);
        }
        return true;
    }
    
    public synchronized void resetOutput() {
        if (ow != null) {
            try {
                ow.reset();
            } catch (IOException ioe) { // it doesn't matter here
                ioe.printStackTrace();
            }
            
            ow = null;
        }
    }

}
