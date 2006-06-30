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
import java.io.IOException;
import java.net.URL;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Testing basic functionality of Editor Settings Storage friend API
 * 
 *  @author Martin Roskanin
 */
public class EditorSettingsStorageTest extends EditorSettingsStorageTestBase {

    public EditorSettingsStorageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
                getWorkDir(),
            new URL[] {
                getClass().getClassLoader().getResource(
                        "org/netbeans/modules/defaults/mf-layer.xml"),
                getClass().getClassLoader().getResource(
                        "org/netbeans/modules/java/editor/resources/layer.xml"),
            },
            new Object[] {},
            getClass().getClassLoader()
        );
    }

    public void testSettingLookup() throws IOException{
        AttributeSet set = getSetting("text/x-java", "java-keywords");
        assertTrue(set != null);
    }
    
    public void testSettingChange() throws IOException{
        EditorSettings editorSettings = EditorSettings.getDefault ();
        
        // gather original color
        MimeLookup mimelookup = MimeLookup.getMimeLookup("text/x-java");
        FontColorSettings fcs = (FontColorSettings) mimelookup.lookup(FontColorSettings.class);
        AttributeSet set = fcs.getTokenFontColors("java-keywords");
        assertTrue(set != null);
        
        String name = (String) set.getAttribute(StyleConstants.NameAttribute);
        Color color = (Color) set.getAttribute(StyleConstants.Background);
        
        // change the color
        SimpleAttributeSet a = new SimpleAttributeSet();
        a.addAttribute (
            StyleConstants.NameAttribute, 
            "java-keywords"
        );
        Color setColor = (color != Color.RED) ? Color.RED : Color.GREEN;
        a.addAttribute(StyleConstants.Background, setColor);
        assertTrue(color != setColor);
        setSetting("text/x-java", "java-keywords", a);
        
        // gather modified color
        fcs = (FontColorSettings) mimelookup.lookup(FontColorSettings.class);
        set = fcs.getTokenFontColors("java-keywords");
        assertTrue(set != null);
        name = (String) set.getAttribute(StyleConstants.NameAttribute);
        color = (Color) set.getAttribute(StyleConstants.Background);
        
        // check the setted in color is also available in lookup
        assertTrue(color == setColor);
    }
    
    public void testSettingChangeFiring() throws IOException{
        MimeLookup mimelookup = MimeLookup.getMimeLookup("text/x-java");
        Lookup.Result result = mimelookup.lookup(
                new Lookup.Template(FontColorSettings.class));
        result.allInstances();
        
        LookupListener listener = new LookupListener(){
            public void resultChanged(LookupEvent ev){
                resultChangedCount[0]++;
            }
        };
        
        resultChangedCount[0] = 0;
        result.addLookupListener(listener);
        
        // perform setting change
        AttributeSet oldSet = getSetting("text/x-java", "java-keywords");
        Color color = (Color) oldSet.getAttribute(StyleConstants.Foreground);
        SimpleAttributeSet a = new SimpleAttributeSet();
        a.addAttribute (
            StyleConstants.NameAttribute, 
            "java-keywords"
        );
        
        Color setColor = (color != Color.RED) ? Color.RED : Color.GREEN;
        a.addAttribute(StyleConstants.Foreground, setColor);
        setSetting("text/x-java", "java-keywords", a, true);
        
        checkResultChange(1);
        
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
    }
    

}
