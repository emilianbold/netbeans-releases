/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
