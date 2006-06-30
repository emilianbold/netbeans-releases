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

import java.io.IOException;
import java.io.Reader;

//import org.openide.util.Lookup;

import org.netbeans.api.diff.Difference;

/**
 * This class represents a provider of diff algorithm. The implementing class
 * should calculate differences between two sources.
 * <p>The registered Diff Providers can be obtained via {@link org.openide.util.Lookup}
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
