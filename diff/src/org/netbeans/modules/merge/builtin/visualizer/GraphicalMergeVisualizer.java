/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.merge.builtin.visualizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.io.Serializable;

import org.netbeans.api.diff.Difference;

import org.netbeans.modules.diff.builtin.visualizer.DiffComponent;

/**
 * The default graphical visualizer of merge conflicts.
 *
 * @author  Martin Entlicher
 */
public class GraphicalMergeVisualizer implements Serializable {
    
    private Color colorAdded = DiffComponent.COLOR_ADDED;
    private Color colorMissing = DiffComponent.COLOR_MISSING;
    private Color colorChanged = DiffComponent.COLOR_CHANGED;
    
    /** Listener to the buttons of currently opened merge dialog. */
    private MergeDialogActionListener mergeListener;
    /** The currently opened merge dialog. */
    private MergeDialogDescriptor merge;
    
    static final long serialVersionUID =-2175410667258166512L;
    /** Creates a new instance of GraphicalMergeVisualizer */
    public GraphicalMergeVisualizer() {
        mergeListener = new MergeDialogActionListener();
        merge = null;
    }
    
    /**
     * Show the visual representation of the diff between two sources.
     * @param diffs The list of differences (instances of {@link Difference}).
     *       may be <code>null</code> in case that it does not need diff provider.
     * @param name1 the name of the first source
     * @param title1 the title of the first source
     * @param r1 the first source
     * @param name2 the name of the second source
     * @param title2 the title of the second source
     * @param r2 the second resource compared with the first one.
     * @param MIMEType the mime type of these sources
     * @return The TopComponent representing the diff visual representation
     *        or null, when the representation is outside the IDE.
     */
    public Component createView(Difference[] diffs, String name1, String title1, Reader r1,
                                String name2, String title2, Reader r2,
                                String name3, String title3, Writer w3, String MIMEType) {
        merge = new MergeDialogDescriptor(diffs, name1+" <> "+name2, MIMEType,
            name1, name2, name3, title1, title2, title3, r1, r2, w3,
            new Color[] { colorMissing, colorAdded, colorChanged }, mergeListener);
        return org.openide.TopManager.getDefault().createDialog(merge);
    }
    
    private class MergeDialogActionListener extends Object implements ActionListener {
        
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
}
