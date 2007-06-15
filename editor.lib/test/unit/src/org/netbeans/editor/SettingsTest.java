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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.editor;

import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.swing.KeyStroke;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;

/**
 *
 * @author vita
 */
public class SettingsTest extends NbTestCase {

    public SettingsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/editor/test-layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
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
    
// XXX: needs more cleaning in BaseOptions
//    public void testKeybindingsBridgeRead() {
//        Object value = Settings.getValue(MyKit.class, SettingsNames.KEY_BINDING_LIST);
//        assertNotNull("Keybinding list should not be null", value);
//        assertTrue("Wrong type", value instanceof List);
//
//        List list = (List) value;
//        assertEquals("Wrong number of keybindings", 1, list.size());
//        
//        Object kb = list.get(0);
//        assertTrue("The keybindings should be MultiKeyBinding; but is " + kb, kb instanceof MultiKeyBinding);
//        
//        MultiKeyBinding mkb = (MultiKeyBinding) kb;
//        assertNull("MultiKeyBinding key should be null", mkb.key);
//        assertNotNull("MultiKeyBinding keys[] should not be null", mkb.keys);
//        assertEquals("Wrong size of MultiKeyBinding keys[]", 1, mkb.keys.length);
//        assertEquals("Wrong MultiKeyBinding keys[0]", 
//            KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK), 
//            mkb.keys[0]);
//        assertEquals("Wrong keybinding's action", "test-action-1", mkb.actionName);
//    }

    public void testCodeTemplatesBridgeRead() {
        Object value = Settings.getValue(MyKit.class, SettingsNames.ABBREV_MAP);
        assertNotNull("CodeTemplates map should not be null", value);
        assertTrue("Wrong type", value instanceof Map);
        
        Map map = (Map) value;
        assertEquals("Wrong number of abbreviations", 1, map.size());
        
        Object k = map.keySet().iterator().next();
        assertTrue("The abbreviation should be String; but is " + k, k instanceof String);
        assertEquals("Wrong abbreviation", "hw", (String) k);
        
        Object v = map.get(k);
        assertTrue("The code-text should be String; but is " + v, v instanceof String);
        assertEquals("Wrong code-text", "Hello World!", (String) v);
    }
    
    public static final class MyKit extends BaseKit {
        public MyKit() {
            super();
        }

        @Override
        public String getContentType() {
            return "text/x-type-A";
        }
    } // End of MyKit class
}
