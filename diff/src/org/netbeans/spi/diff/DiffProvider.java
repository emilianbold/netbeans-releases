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

import java.io.IOException;
import java.io.Reader;

//import org.openide.util.Lookup;

import org.netbeans.api.diff.Difference;

/**
 * This class represents a provider of diff algorithm. The implementing class
 * should calculate differences between two sources.
 * <p>The registered Diff Providers can be obtained via Lookup
 * (e.g. you can get the default diff provider by
 *  <code>Lookup.getDefault().lookup(DiffProvider.class)</code>)
 *
 * @author  Martin Entlicher
 */
public abstract class DiffProvider extends Object {

    /*
    public static DiffProvider getDefault() {
        return (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
    }
     */
    
    /**
     * Create the differences of the content two streams.
     * @param r1 the first source
     * @param r2 the second source to be compared with the first one.
     * @return the list of differences found, instances of {@link Difference};
     *         or <code>null</code> when some error occured.
     * @throws IOException when the reading from input streams fails.
     */
    public abstract Difference[] computeDiff(Reader r1, Reader r2) throws IOException;
}
