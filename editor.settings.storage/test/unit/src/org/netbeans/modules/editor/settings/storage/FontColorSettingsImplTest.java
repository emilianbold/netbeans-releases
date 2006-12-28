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

package org.netbeans.modules.editor.settings.storage;

import java.awt.Color;
import java.net.URL;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/** Testing basic functionality of Editor Settings Storage friend API
 * 
 *  @author Martin Roskanin
 */
public class FontColorSettingsImplTest extends NbTestCase {

    public FontColorSettingsImplTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                        "org/netbeans/modules/editor/settings/storage/test-layer.xml"),
                getClass().getClassLoader().getResource(
                        "org/netbeans/modules/editor/settings/storage/layer.xml"),
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );

        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();
    }

    public void testDefaults() {
        checkSingleAttribute("test-inheritance-coloring-1", StyleConstants.Background, 0xAA0000);
        checkSingleAttribute("test-inheritance-coloring-2", StyleConstants.Background, 0xAA0000);

        checkSingleAttribute("test-inheritance-coloring-A", StyleConstants.Background, 0xABCDEF);
        checkSingleAttribute("test-inheritance-coloring-B", StyleConstants.Background, 0xABCDEF);

        checkSingleAttribute("test-inheritance-coloring-X", StyleConstants.Background, 0xBB0000);
        checkSingleAttribute("test-inheritance-coloring-Y", StyleConstants.Background, 0xBB0000);
    }

    private void checkSingleAttribute(String coloringName, Object attributeKey, int rgb) {
        MimePath mimePath = MimePath.parse("text/x-type-B");
        Lookup lookup = MimeLookup.getLookup(mimePath);
        
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        
        AttributeSet attribs = fcs.getTokenFontColors(coloringName);
        assertNotNull("Can't find " + coloringName + " coloring", attribs);
        assertEquals("Wrong bgColor", new Color(rgb), attribs.getAttribute(attributeKey));
    }
    
}
