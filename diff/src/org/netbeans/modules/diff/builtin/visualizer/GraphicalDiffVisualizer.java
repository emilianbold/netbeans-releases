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

package org.netbeans.modules.diff.builtin.visualizer;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import org.netbeans.api.diff.DiffVisualizer;

/**
 * The default graphical visualizer of diffs.
 *
 * @author  Martin Entlicher
 */
public class GraphicalDiffVisualizer extends DiffVisualizer {
    
    /** Creates a new instance of BuiltInDiffVisualizer */
    public GraphicalDiffVisualizer() {
    }
    
    /**
     * Get the display name of this diff visualizer.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(GraphicalDiffVisualizer.class, "GraphicalDiffVisualizer.displayName");
    }
    
    /**
     * Get a short description of this diff visualizer.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(GraphicalDiffVisualizer.class, "GraphicalDiffVisualizer.shortDescription");
    }
    
    /**
     * Some diff visualizers may have built-in the diff calculation. In such a case
     * the visualizer does not need any diff provider.
     * @return true when it relies on differences supplied, false if not.
     */
    public boolean needsProvider() {
        return true;
    }
    
    /**
     * Show the visual representation of the diff between two files.
     * @param diffs The list of differences (instances of {@link Difference}).
     *       may be <code>null</code> in case that it does not need diff provider.
     * @param fo1 the first FileObject
     * @param fo2 the second FileObject compared with the first one.
     * @return The TopComponent representing the diff visual representation
     *        or null, when the representation is outside the IDE.
     */
    public TopComponent showDiff(List diffs, FileObject fo1, FileObject fo2) {
        DiffComponent diff;
        try {
            diff = new DiffComponent(diffs, null, fo1.getMIMEType(),
                fo1.getName(), fo2.getName(),
                fo1.getPackageNameExt('/', '.'), fo2.getPackageNameExt('/', '.'),
                new InputStreamReader(fo1.getInputStream()),
                new InputStreamReader(fo2.getInputStream()));
        } catch (FileNotFoundException fnfex) {
            org.openide.TopManager.getDefault().notifyException(fnfex);
            return null;
        }
        return diff;
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
    public TopComponent showDiff(List diffs, String name1, String title1, Reader r1,
                                 String name2, String title2, Reader r2, String MIMEType) {
        DiffComponent diff;
        diff = new DiffComponent(diffs, null, MIMEType,
            name1, name2, title1, title2, r1, r2);
        return diff;
    }
    
}
