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
import java.util.*;

import org.openide.text.Line;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.compiler.*;
import org.openide.cookies.*;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import org.openidex.search.*;

import org.netbeans.modules.search.types.DetailHandler;
import org.netbeans.modules.search.res.*;

import org.netbeans.editor.*;

/**
 * Presents search results in output window. It can display
 * just nodes marked by DetailCookie.
 *
 * @author  Petr Kuzel
 * @version 
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
        DetailCookie cake = (DetailCookie) node.getCookie(DetailCookie.class);

        if (cake != null) {
            Enumeration en = cake.detail();
            while (en.hasMoreElements()) {
                Object obj = en.nextElement();

                if (obj instanceof DetailHandler) {
                    DetailHandler det = (DetailHandler)obj;
                    try {
                        ow.println(det.getDescription(), new DetailOutput(det));
                    } catch (IOException ex) {
                        ow.println(det.getDescription());
                    }
                }
            }
        }
    }

    /** Accepted nodes should be displayed.
     * @param nodes the nodes to consider
     * @return <CODE>true</CODE> if so
     */
    public synchronized boolean acceptNodes(Node[] nodes) {

        if (nodes == null) return false;

        if (nodes.length > 0 && ow == null)
            setOw(Res.text("TITLE_SEARCH_RESULTS"));

        for (int i=0; i < nodes.length; i++)
            displayNode(nodes[i]);

        return true;
    }
    
    public synchronized void resetOutput() {
        if (ow != null) {
            try {
                ow.reset();
            }
            catch (IOException ex) { // it doesn't matter here
            }
            ow = null;
        }
    }

    private class DetailOutput implements OutputListener
    {
        DetailHandler detail;
        
        DetailOutput(DetailHandler det) { 
            detail = det;
        }

        public void outputLineSelected (OutputEvent ev) {
            detail.showDetail(DetailHandler.DH_SHOW);
        }

        public void outputLineAction (OutputEvent ev) {
            detail.showDetail(DetailHandler.DH_GOTO);
        }

        public void outputLineCleared (OutputEvent ev) {
            detail.showDetail(DetailHandler.DH_HIDE);
        }
    }
}
