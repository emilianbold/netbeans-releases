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

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import org.netbeans.api.diff.DiffVisualizer;

/**
 * The textual visualizer of diffs.
 *
 * @author  Martin Entlicher
 */
public class TextDiffVisualizer extends DiffVisualizer {

    /** Creates a new instance of TextDiffVisualizer */
    public TextDiffVisualizer() {
    }

    /**
     * Get the display name of this diff visualizer.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(TextDiffVisualizer.class, "TextDiffVisualizer.displayName");
    }
    
    /**
     * Get a short description of this diff visualizer.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(TextDiffVisualizer.class, "TextDiffVisualizer.shortDescription");
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
     * @throws IOException when the reading from input streams fails.
     */
    public TopComponent showDiff(List diffs, String name1, String title1,
                                 Reader r1, String name2, String title2,
                                 Reader r2, String MIMEType) throws IOException {
        return null;
    }
    
}
