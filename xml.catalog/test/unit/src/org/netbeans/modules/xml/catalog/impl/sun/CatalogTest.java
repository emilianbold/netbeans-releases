/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.catalog.impl.sun;

import junit.framework.TestCase;

import java.net.URL;

import org.xml.sax.InputSource;

/**
 * Tests OASIS XML catalog implementatin.
 *
 * @author Petr Kuzel
 */
public final class CatalogTest extends TestCase {

    public void testSpaceInPath() throws Exception {
        Catalog catalog = new Catalog();
        URL locationURL = getClass().getResource("data/catalog.xml");
        String location = locationURL.toExternalForm();
        catalog.setLocation(location);
        catalog.refresh();
        String s0 = catalog.resolvePublic("-//NetBeans//IZ53710//EN");
        new URL(s0).openStream();
        String s1 = catalog.resolvePublic("-//NetBeans//IZ53710 1//EN");
        new URL(s1).openStream();
        String s2 = catalog.resolvePublic("-//NetBeans//IZ53710 2//EN");
        // new URL(s2).openStream();
        String s3 = catalog.resolvePublic("-//NetBeans//IZ53710 3//EN");
        new URL(s3).openStream();

        InputSource in1 = catalog.resolveEntity("-//NetBeans//IZ53710 1//EN", null);
        InputSource in2 = catalog.resolveEntity("-//NetBeans//IZ53710 2//EN", null);
        InputSource in3 = catalog.resolveEntity("-//NetBeans//IZ53710 3//EN", null);

        System.err.println("Done");
    }
}
