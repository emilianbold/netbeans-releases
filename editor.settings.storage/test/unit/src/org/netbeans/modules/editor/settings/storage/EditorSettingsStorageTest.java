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

package org.netbeans.modules.editor.settings.storage;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.junit.NbTestCase;
import junit.framework.*;
import org.netbeans.api.editor.settings.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;


/** Testing basic functionality of Editor Settings Storage friend API
 * 
 *  @author Martin Roskanin
 */
public class EditorSettingsStorageTest extends NbTestCase {

    private final int resultChangedCount[] = new int[1];
    private static final int WAIT_TIME_FIRING = 1500;    
    
    public EditorSettingsStorageTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EditorSettingsStorageTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
                getWorkDir(),
            new URL[] {
                getClass().getClassLoader().getResource(
                        "org/netbeans/modules/editor/settings/storage/resources/mf-layer.xml")
            },
            new Object[] {},
            getClass().getClassLoader()
        );
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    public void testSettingLookup() throws IOException{
        AttributeSet set = getSetting("text/x-java", "java-keywords");
        assertTrue(set != null);
    }
    
    public void testSettingChange() throws IOException{
        
        MimeLookup mimelookup = MimeLookup.getMimeLookup("text/x-java");
        FontColorSettings fcs = (FontColorSettings) mimelookup.lookup(FontColorSettings.class);
        AttributeSet set = fcs.getTokenFontColors("java-keywords");
        assertTrue(set != null);
        
        org.netbeans.modules.editor.settings.storage.api.FontColorSettings fcsStorage = 
                (org.netbeans.modules.editor.settings.storage.api.FontColorSettings) mimelookup.lookup(org.netbeans.modules.editor.settings.storage.api.FontColorSettings.class);
        set = fcsStorage.getTokenFontColors("java-keywords");
        assertTrue(set != null);

        set = fcs.getTokenFontColors("java-keywords");
        assertTrue(set != null);
        String name = (String) set.getAttribute(StyleConstants.NameAttribute);
        Color color = (Color) set.getAttribute(StyleConstants.Background);
        
        List colors = new ArrayList();
        SimpleAttributeSet a = new SimpleAttributeSet();
        a.addAttribute (
            StyleConstants.NameAttribute, 
            "java-keywords"
        );
        Color setColor = (color != Color.RED) ? Color.RED : Color.GREEN;
        a.addAttribute(StyleConstants.Background, setColor);
                    
        assertTrue(color != setColor);
        colors.add(a);
        fcsStorage.setAllFontColors(colors);
        
        set = fcsStorage.getTokenFontColors("java-keywords");
        assertTrue(set != null);
        name = (String) set.getAttribute(StyleConstants.NameAttribute);
        color = (Color) set.getAttribute(StyleConstants.Background);
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
        setSetting("text/x-java", "java-keywords", a);
        
        checkResultChange(1);
        
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
        

    }

    
    private AttributeSet getSetting(String mime, String settingName){
        MimeLookup mimelookup = MimeLookup.getMimeLookup(mime);
        FontColorSettings fcs = (FontColorSettings) mimelookup.lookup(FontColorSettings.class);
        return fcs.getTokenFontColors(settingName);
    }
    
    private void setSetting(String mime, String settingName, AttributeSet set){
        MimeLookup mimelookup = MimeLookup.getMimeLookup(mime);
        FontColorSettings fcs = (FontColorSettings) mimelookup.lookup(FontColorSettings.class);
       
        org.netbeans.modules.editor.settings.storage.api.FontColorSettings fcsStorage = 
                (org.netbeans.modules.editor.settings.storage.api.FontColorSettings) mimelookup.lookup(org.netbeans.modules.editor.settings.storage.api.FontColorSettings.class);

        List colors = new ArrayList();
        colors.add(set);
        fcsStorage.setAllFontColors(colors);
        
        // test setting was really set
        assertTrue(set.equals(getSetting(mime, settingName)));
        
    }
    
    private void checkResultChange(final int count) throws IOException{
        // wait for firing event
        SettingsStorageTestUtils.waitMaxMilisForValue(WAIT_TIME_FIRING, new SettingsStorageTestUtils.ValueResolver(){
            public Object getValue(){
                return Boolean.FALSE;
            }
        }, Boolean.TRUE);
        assertTrue(("resultChangedCount is:"+resultChangedCount[0]+" instead of "+count), resultChangedCount[0] == count);
    }

}
