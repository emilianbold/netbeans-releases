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
