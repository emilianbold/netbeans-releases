/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.io.IOException;

import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;


/**
 * Presents search results in output window.
 *
 * @author  Petr Kuzel
 */
public class SearchDisplayer extends Object implements NodeAcceptor {

    /** output tab */
    private InputOutput searchIO;
    /** writer to that tab */
    private OutputWriter ow = null;

    /** Creates new SearchDisplayer */
    public SearchDisplayer() {
    }

    private void setOw (String name) {
        searchIO = TopManager.getDefault().getIO(name, false);
        searchIO.setFocusTaken(false);
        ow = searchIO.getOut();
    }
    
    private void displayNode(Node node) {
        
        try {
            if(node instanceof OutputListener)
                ow.println(node.getShortDescription(), (OutputListener)node);
            else
                ow.println(node.getShortDescription());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** Accepted nodes should be displayed.
     * @param nodes the nodes to consider
     * @return <CODE>true</CODE> if so
     */
    public synchronized boolean acceptNodes(Node[] nodes) {

        if (nodes == null) return false;

        if (nodes.length > 0 && ow == null)
            setOw(NbBundle.getBundle(ResultViewTopComponent.class).getString("TEXT_TITLE_SEARCH_RESULTS"));
        
        for (int i=0; i < nodes.length; i++)
            displayNode(nodes[i]);

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
