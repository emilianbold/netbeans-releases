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

import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;

/**
 * This class represents a merge visualizer. It's used as a visual conflicts
 * resolution tool for the process of merging of file conflicts.
 * <p>The registered Merge Visualizers can be obtained via {@link org.openide.util.Lookup}
 * (e.g. you can get the default merge provider by
 *  <code>Lookup.getDefault().lookup(MergeVisualizer.class)</code>)
 *
 * @author  Martin Entlicher
 */
public abstract class MergeVisualizer extends Object {

    /**
     * Show the visual representation of the merging process of two sources.
     * The result of the merging process can be saved into a Writer even
     * before all conflicts are actually resolved.
     *
     * @param diffs The list of conflicts.
     * @param source1 the source of the first file
     * @param source2 the source of the second file
     * @param result the information about the result source
     * @return The Component representing the diff visual representation
     *         or null, when the representation is outside the IDE.
     * @throws IOException when the reading from input streams fails.
     */
    public abstract Component createView(Difference[] diffs, StreamSource source1,
                                         StreamSource source2, StreamSource result) throws IOException ;
    
}
