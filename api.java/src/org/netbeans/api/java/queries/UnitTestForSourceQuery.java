/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.queries;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Query to find Java package root of unit tests for Java package root of 
 * sources and vice versa.
 *
 * @see org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation
 * @author David Konecny
 * @since org.netbeans.api.java/1 1.4
 */
public class UnitTestForSourceQuery {
    
    private static final Lookup.Result/*<UnitTestForSourceQueryImplementation>*/ implementations =
        Lookup.getDefault().lookup (new Lookup.Template (UnitTestForSourceQueryImplementation.class));
    private static final Lookup.Result/*<MultipleRootsUnitTestForSourceQueryImplementation>*/ mrImplementations = 
        Lookup.getDefault().lookup (new Lookup.Template (MultipleRootsUnitTestForSourceQueryImplementation.class));

    private UnitTestForSourceQuery() {
    }

    /**
     * Returns the test root for a given source root.
     *
     * @param source java package root with sources
     * @return corresponding java package root with unit tests. The
     *     returned URL does not have to point to existing file. It can be null
     *     when mapping from source to unit test is not known.
     * @deprecated Use {@link #findUnitTests} instead.
     */
    public static URL findUnitTest(FileObject source) {
        URL[] result = findUnitTests (source);
        return result.length == 0 ? null : result[0];
    }


    /**
     * Returns the test roots for a given source root.
     *
     * @param source java package root with sources
     * @return corresponding java package roots with unit tests. The
     *     returned URLs do not have to point to existing file. It can be an empty
     *     array when mapping from source to unit test is not known but can't be null.
     */
    public static URL[] findUnitTests(FileObject source) {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source cannot be null"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            MultipleRootsUnitTestForSourceQueryImplementation query =
                    (MultipleRootsUnitTestForSourceQueryImplementation)it.next();
            URL[] urls = query.findUnitTests(source);
            if (urls != null) {
                return urls;
            }
        }
        it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            UnitTestForSourceQueryImplementation query = (UnitTestForSourceQueryImplementation)it.next();
            URL u = query.findUnitTest(source);
            if (u != null) {
                return new URL[] {u};
            }
        }
        return new URL[0];
    }

    /**
     * Returns the source root for a given test root.
     *
     * @param unitTest java package root with unit tests
     * @return corresponding java package root with sources. It can be null
     *     when mapping from unit test to source is not known.
     * @deprecated Use {@link #findSources} instead.
     */
    public static URL findSource(FileObject unitTest) {
        URL[] result =  findSources (unitTest);
        return result.length == 0 ? null : result[0];
    }

    /**
     * Returns the source roots for a given test root.
     *
     * @param unitTest java package root with unit tests
     * @return corresponding java package roots with sources. It can be an empty array
     *     when mapping from unit test to source is not known but it can't be null.
     */
    public static URL[] findSources (FileObject unitTest) {
        if (unitTest == null) {
            throw new IllegalArgumentException("Parameter unitTest cannot be null"); // NOI18N
        }
        Iterator it = mrImplementations.allInstances().iterator();
        while (it.hasNext()) {
            MultipleRootsUnitTestForSourceQueryImplementation query =
                    (MultipleRootsUnitTestForSourceQueryImplementation)it.next();
            URL[] urls = query.findSources(unitTest);
            if (urls != null) {
                return urls;
            }
        }
        it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            UnitTestForSourceQueryImplementation query =
                    (UnitTestForSourceQueryImplementation)it.next();
            URL u = query.findSource(unitTest);
            if (u != null) {
                return new URL[] {u};
            }
        }
        return new URL[0];
    }

}
