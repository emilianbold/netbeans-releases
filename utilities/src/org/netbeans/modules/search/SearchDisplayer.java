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

import java.awt.EventQueue;

import java.io.IOException;
import java.lang.reflect.Method;
import org.openide.ErrorManager;

import org.openide.nodes.Node;
import org.openide.util.Mutex;
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
public final class SearchDisplayer {

    /** name of attribute &quot;text to display in the Output Window&quot; */
    public static final String ATTR_OUTPUT_LINE = "output line";        //NOI18N
    /** output tab */
    private final InputOutput searchIO;
    /** writer to that tab */
    private OutputWriter ow = null;
    /** */
    private volatile boolean justPrepared;

    /** Creates new SearchDisplayer */
    SearchDisplayer() {
        String name = NbBundle.getMessage(ResultView.class,
                                          "TITLE_SEARCH_RESULTS");      //NOI18N
        searchIO = IOProvider.getDefault().getIO(name, false);
    }

    /**
     */
    void prepareOutput() {
        if (ow != null) {
            try {
                ow.reset();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        ow = searchIO.getOut();
        searchIO.select();
        justPrepared = true;
    }
    
    /**
     * Displays the given nodes.
     *
     * @param  nodes  nodes to display
     */
    void displayNodes(final Node[] nodes) {

        /* Prepare the output lines: */
        final String[] outputLines = new String[nodes.length];
        final OutputListener[] listeners = new OutputListener[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];
            final Object o = node.getValue(ATTR_OUTPUT_LINE);
            outputLines[i] = o instanceof String ? (String) o
                                                 : node.getShortDescription();
            listeners[i] = node instanceof OutputListener ? (OutputListener)node
                                                          : null;
        }

        /* Print the output lines: */
        final boolean requestFocus = justPrepared;
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    if (requestFocus) {
                        searchIO.setFocusTaken(true);
                    }
                    try {
                        for (int i = 0; i < outputLines.length; i++) {
                            OutputListener listener = listeners[i];
                            if (listener != null) {
                                ow.println(outputLines[i], listener);
                            } else {
                                ow.println(outputLines[i]);
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault()
                        .notify(ErrorManager.EXCEPTION, ex);
                    }
                    if (requestFocus) {
                        searchIO.setFocusTaken(false);
                    }
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        justPrepared = false;
    }
    
    /**
     */
    void finishDisplaying() {
        ow.flush();
        ow.close();
    }
    
}
