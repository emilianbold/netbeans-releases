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

import java.net.URL;
import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public class SettingsProviderTest extends NbTestCase {
    
    /** Creates a new instance of SettingsProviderTest */
    public SettingsProviderTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                        "org/netbeans/modules/editor/settings/storage/test-layer.xml"),
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );
    }
    
    public void testSpecialTestMimeType() throws Exception {
        final String origMimeType = "text/x-orig";
        final String specialTestMimeType = "test123456_" + origMimeType;
        Marker origMarker;
        
        {
            Lookup origLookup = MimeLookup.getLookup(MimePath.parse(origMimeType));
            Collection<? extends Marker> markers = origLookup.lookupAll(Marker.class);
            assertEquals("Wrong number of orig markers", 1, markers.size());
            
            origMarker = markers.iterator().next();
            assertNotNull("Orig marker is null", origMarker);
            assertEquals("Wrong orig marker", 
                "Editors/text/x-orig/marker.instance", 
                origMarker.getHome().getPath());
        }
        
        {
            Lookup testLookup = MimeLookup.getLookup(MimePath.parse(specialTestMimeType));
            Collection<? extends Marker> markers = testLookup.lookupAll(Marker.class);
            assertEquals("Wrong number of test markers", 1, markers.size());
            
            Marker testMarker = markers.iterator().next();
            assertNotNull("Test marker is null", testMarker);
            assertEquals("Wrong test marker", 
                "Editors/text/x-orig/marker.instance", 
                testMarker.getHome().getPath());
            
            assertSame("Test marker and orig marker should be the same", origMarker, testMarker);
        }
    }

    public void testColoringsForMimeType() throws Exception {
        final String mimeType = "text/x-orig";
        
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
        
        // Check the API class
        Collection<? extends FontColorSettings> c = lookup.lookupAll(FontColorSettings.class);
        assertEquals("Wrong number of fcs", 1, c.size());
        
        FontColorSettings fcs = c.iterator().next();
        assertNotNull("FCS should not be null", fcs);
        assertTrue("Wrong fcs impl", fcs instanceof FontColorSettingsImpl.Immutable);
        
        FontColorSettingsImpl.Immutable fcsi = (FontColorSettingsImpl.Immutable) fcs;
        assertNull("Wrong fcsi test string", fcsi.test);
    }

    public void testColoringsForSpecialTestMimeType() throws Exception {
        final String origMimeType = "text/x-orig";
        final String specialTestMimeType = "test123456_" + origMimeType;
        
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(specialTestMimeType));
        
        // Check the API class
        Collection<? extends FontColorSettings> c = lookup.lookupAll(FontColorSettings.class);
        assertEquals("Wrong number of fcs", 1, c.size());
        
        FontColorSettings fcs = c.iterator().next();
        assertNotNull("FCS should not be null", fcs);
        assertTrue("Wrong fcs impl", fcs instanceof FontColorSettingsImpl.Immutable);
        
        FontColorSettingsImpl.Immutable fcsi = (FontColorSettingsImpl.Immutable) fcs;
        assertEquals("Wrong fcsi test string", "test123456", fcsi.test);
    }

    public void testKeybindingsForSpecialTestMimeType() throws Exception {
        final String origMimeType = "text/x-orig";
        final String specialTestMimeType = "test123456_" + origMimeType;
        
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(specialTestMimeType));
        
        // Check the API class
        Collection<? extends KeyBindingSettings> c = lookup.lookupAll(KeyBindingSettings.class);
        assertEquals("Wrong number of kbs", 1, c.size());
        
        KeyBindingSettings kbs = c.iterator().next();
        assertNotNull("KBS should not be null", kbs);
        assertTrue("Wrong kbs impl", kbs instanceof KeyBindingSettingsImpl.Immutable);
    }
    
    public static Marker createMarker(FileObject f) {
        return new Marker(f);
    }
    
    public static final class Marker {
        
        private FileObject home;
        
        public Marker(FileObject home) {
            this.home = home;
        }
        
        public FileObject getHome() {
            return home;
        }
    } // End of Marker class
    
}
