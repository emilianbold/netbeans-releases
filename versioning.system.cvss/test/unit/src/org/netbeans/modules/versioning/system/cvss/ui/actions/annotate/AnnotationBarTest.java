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

package org.netbeans.modules.versioning.system.cvss.ui.actions.annotate;

import junit.framework.TestCase;
import org.netbeans.modules.versioning.system.cvss.util.Utils;

/**
 * Test utility method in AnnotationBar
 *
 * @author Petr Kuzel
 */
public class AnnotationBarTest extends TestCase {


    public static void testPreviousRevision() {
        assertNull(Utils.previousRevision("1.1"));
        assertEquals("1.1", Utils.previousRevision("1.2"));
        assertEquals("1.2.1.1", Utils.previousRevision("1.2.1.2"));
        assertEquals("1.199", Utils.previousRevision("1.200.0.1.1.1"));
    }

}
