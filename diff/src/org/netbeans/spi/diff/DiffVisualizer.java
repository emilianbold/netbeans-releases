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

package org.netbeans.spi.diff;

import java.awt.Component;
import java.io.IOException;
import java.io.Reader;

//import org.openide.util.Lookup;

import org.netbeans.api.diff.Difference;

/**
 * This class represents a diff visualizer. It's used as a presenter of a visual
 * representation of the source differences to the user.
 * <p>The registered Diff Visualizers can be obtained via Lookup
 * (e.g. you can get the default diff provider by
 *  <code>Lookup.getDefault().lookup(DiffVisualizer.class)</code>)
 *
 * @author  Martin Entlicher
 */
public abstract class DiffVisualizer extends Object {

    /*
    public static DiffVisualizer getDefault() {
        return (DiffVisualizer) Lookup.getDefault().lookup(DiffVisualizer.class);
    }
     */
    
    /**
     * Show the visual representation of the diff between two sources.
     * @param diffs The list of differences.
     * @param name1 the name of the first source
     * @param title1 the title of the first source
     * @param r1 the first source
     * @param name2 the name of the second source
     * @param title2 the title of the second source
     * @param r2 the second resource compared with the first one.
     * @param MIMEType the mime type of these sources
     * @return The Component representing the diff visual representation
     *         or null, when the representation is outside the IDE.
     * @throws IOException when the reading from input streams fails.
     */
    public abstract Component createView(Difference[] diffs, String name1, String title1,
                                         Reader r1, String name2, String title2,
                                         Reader r2, String MIMEType) throws IOException ;
}
