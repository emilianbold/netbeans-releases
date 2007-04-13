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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.metadata.model.api;

import junit.framework.TestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
public class MetadataModelExceptionTest extends TestCase {

    public MetadataModelExceptionTest(String testName) {
        super(testName);
    }

    public void testCause() throws Exception {
        MetadataModelException mme = new MetadataModelException(new RuntimeException("foo"));
        assertTrue(mme.getMessage().contains("foo"));
        assertFalse(mme.getMessage().contains("RuntimeException"));
        assertTrue(mme.getCause().getClass().equals(RuntimeException.class));
    }
}
