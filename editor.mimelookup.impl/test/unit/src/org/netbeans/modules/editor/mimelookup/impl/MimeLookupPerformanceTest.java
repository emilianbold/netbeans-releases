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

package org.netbeans.modules.editor.mimelookup.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import junit.framework.*;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.CutAction;
import org.openide.actions.FindAction;
import org.openide.actions.NewAction;
import org.openide.actions.RenameAction;
import org.openide.actions.ReplaceAction;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

/** Testing functionality of dynamic change over inherited folders
 * 
 *  @author Martin Roskanin
 */
public class MimeLookupPerformanceTest extends NbTestCase {

    private static final int WAIT_TIME = 5000;    
    private static MemoryFilter filter;
    
    private String fsstruct [];
    
    public MimeLookupPerformanceTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        fsstruct = new String [] {
            "Editors/Popup/org-openide-actions-CutAction.instance", //NOI18N
            "Editors/Popup/org-openide-actions-CopyAction.instance", //NOI18N
            "Editors/Popup/org-openide-actions-PasteAction.instance", //NOI18N
            "Editors/text/x-java/Popup/org-openide-actions-DeleteAction.instance", //NOI18N
            "Editors/text/x-java/Popup/org-openide-actions-RenameAction.instance", //NOI18N
            "Editors/text/x-java/text/xml/Popup/org-openide-actions-PrintAction.instance", //NOI18N
            "Editors/text/x-java/text/xml/text/html/Popup/org-openide-actions-NewAction.instance", //NOI18N
        };

        EditorTestLookup.setLookup(fsstruct, getWorkDir(), new Object[] {},
                   getClass().getClassLoader());
        
    }

    private void gc(){
        for (int i = 0; i<15; i++){
            System.gc();
            try {
                Thread.sleep(123);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }

    private static synchronized MemoryFilter getFilter(){
        if (filter == null){
            filter = new MemoryFilter(){
                public boolean reject(Object obj){
                    return false;
                }
            };
        }
        return filter;
    }

    public void testMimeLookupObjectInstallingUninstallingSize() throws IOException{
        MimePath mp = MimePath.get(MimePath.get("text/x-java"), "text/xml"); //NOI18N
        Lookup lookup = MimeLookup.getLookup(mp);
        PopupActions popup = (PopupActions) lookup.lookup(PopupActions.class);
        List list = popup.getPopupActions();
        checkPopupItemPresence(lookup, RenameAction.class, true);
        int size = 0;
        
        for (int i=0; i<30; i++){
            //delete RenameAction
            TestUtilities.deleteFile(getWorkDir(),
                    "Editors/text/x-java/Popup/org-openide-actions-RenameAction.instance");
            checkPopupItemPresence(lookup, RenameAction.class, false);

            //delete base CutAction
            TestUtilities.deleteFile(getWorkDir(),
                    "Editors/Popup/org-openide-actions-CutAction.instance");
            checkPopupItemPresence(lookup, CutAction.class, false);

            //simulate module installation, new action will be added
            TestUtilities.createFile(getWorkDir(), 
                    "Editors/Popup/org-openide-actions-FindAction.instance"); //NOI18N      
            checkPopupItemPresence(lookup, FindAction.class, true);

            // now reverse the operations

            //simulate module installation, new action will be added
            TestUtilities.createFile(getWorkDir(), 
                    "Editors/text/x-java/Popup/org-openide-actions-RenameAction.instance"); //NOI18N      
            checkPopupItemPresence(lookup, RenameAction.class, true);

            TestUtilities.createFile(getWorkDir(), 
                    "Editors/Popup/org-openide-actions-CutAction.instance"); //NOI18N      
            checkPopupItemPresence(lookup, CutAction.class, true);

            //delete RenameAction
            TestUtilities.deleteFile(getWorkDir(),
                    "Editors/Popup/org-openide-actions-FindAction.instance");
            checkPopupItemPresence(lookup, FindAction.class, false);

            if (i == 0){
                gc();                
                size = assertSize("", Arrays.asList( new Object[] {lookup} ), 10000000,  getFilter());
            }
        }       
        gc(); gc();
        assertSize("", size + 3000, lookup); // 3000 is threshold
    }
    
    public void testClassLookuping() throws IOException{
        MimePath mp = MimePath.parse("text/x-java/text/xml/text/html");
        Lookup lookup = MimeLookup.getLookup(mp);
        PopupActions popup = (PopupActions) lookup.lookup(PopupActions.class);
        List list = popup.getPopupActions();
        checkPopupItemPresence(lookup, RenameAction.class, true);
        gc();
        int size = assertSize("", Arrays.asList( new Object[] {lookup} ), 10000000,  getFilter());
        for (int i=0; i<30; i++){
            popup = (PopupActions) lookup.lookup(PopupActions.class);
            list = popup.getPopupActions();
            checkPopupItemPresence(lookup, RenameAction.class, true);
        }
        gc();
        assertSize("", size, lookup);
    }

    public void testTemplateLookuping() throws IOException{
        MimePath mp = MimePath.parse("text/x-java/text/xml/text/html");
        Lookup lookup = MimeLookup.getLookup(mp);
        Result result = lookup.lookup(new Template(PopupActions.class));
        Collection col = result.allInstances();
        checkPopupItemPresence(lookup, RenameAction.class, true);
        gc();
        int size = assertSize("", Arrays.asList( new Object[] {lookup} ), 10000000,  getFilter());
        for (int i=0; i<30; i++){
            result = lookup.lookup(new Template(PopupActions.class));
            col = result.allInstances();
            checkPopupItemPresence(lookup, RenameAction.class, true);
        }
        gc();
        assertSize("", size, lookup);
    }
    
    
    
    private void checkPopupItemPresence(final Lookup lookup, final Class checkedClazz, final boolean shouldBePresent){
        TestUtilities.waitMaxMilisForValue(WAIT_TIME, new TestUtilities.ValueResolver(){
            public Object getValue(){
                PopupActions pa = (PopupActions)lookup.lookup(PopupActions.class);
                if (pa == null){
                    return Boolean.FALSE;
                }
                boolean bool = false;
                List items = pa.getPopupActions();
                for (int i=0; i<items.size(); i++){
                    Object obj = items.get(i);
                    if (checkedClazz == obj.getClass()){
                        bool = true;
                        break;
                    }
                }
                if (!shouldBePresent){
                    bool = !bool;
                }
                return Boolean.valueOf(bool);
            }
        }, Boolean.TRUE);
        PopupActions pa = (PopupActions)lookup.lookup(PopupActions.class);
        assertTrue("PopupActions should be found", pa != null);        
        boolean bool = false;
        List items = pa.getPopupActions();
        for (int i=0; i<items.size(); i++){
            Object obj = items.get(i);
            if (checkedClazz == obj.getClass()){
                bool = true;
                break;
            }
        }
        if (shouldBePresent){
            assertTrue("Class: "+checkedClazz+" should be present in lookup", bool);
        }else{
            assertTrue("Class: "+checkedClazz+" should not be present in lookup", !bool);
        }
    }
    
    
}
