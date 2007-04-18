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

package org.netbeans.modules.j2ee.persistence.wizard.library;

import junit.framework.TestCase;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibraryPanel;
import org.openide.util.Utilities;

/**
 *
 * @author Erno Mononen
 */
public class PersistenceLibraryPanelTest extends TestCase {
    
    public PersistenceLibraryPanelTest(String testName) {
        super(testName);
    }

    /**
     * Tests that the icons used by the panel are present.
     */ 
    public void testIconsArePresent() {

        String warning = PersistenceLibraryPanel.WARNING_GIF;
        assertNotNull("Could not find an image in path " + warning, Utilities.loadImage(warning));

        String error = PersistenceLibraryPanel.ERROR_GIF;
        assertNotNull("Could not find an image in path " + error, Utilities.loadImage(error));
        
    }
    
}
