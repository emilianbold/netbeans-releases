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

package org.netbeans.api.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.junit.NbTestCase;

/**
 * Tests of editor registry.
 *
 * @author Miloslav Metelka
 */
public class EditorRegistryTest extends NbTestCase {
    
    public EditorRegistryTest(String name) {
        super(name);
    }
    
    public void testRegistry() throws Exception {
        // Start listening
        EditorRegistry.addPropertyChangeListener(EditorRegistryListener.INSTANCE);

        // Test registration
        JTextComponent c1 = new JEditorPane();
        EditorRegistry.register(c1);
        JTextComponent c2 = new JEditorPane();
        EditorRegistry.register(c2);
        List<? extends JTextComponent> jtcList = EditorRegistry.componentList();
        assertSame(2, jtcList.size());
        assertSame(c1, jtcList.get(0));
        assertSame(c2, jtcList.get(1));
        
        // Ignore repetitive registration
        EditorRegistry.register(c2);
        EditorRegistry.register(c2);
        assertSame(2, EditorRegistry.componentList().size());
        
        // Extra component
        JTextComponent c3 = new JEditorPane();
        EditorRegistry.register(c3);
        assertSame(3, EditorRegistry.componentList().size());
        
        // Simulate focusGained
        EditorRegistry.focusGained(c3, null);
        assertSame(1, EditorRegistryListener.INSTANCE.firedCount);
        assertSame(c3, EditorRegistryListener.INSTANCE.newValue);
        assertSame(null, EditorRegistryListener.INSTANCE.oldValue);
        EditorRegistryListener.INSTANCE.reset(); // Reset to 0

        jtcList = EditorRegistry.componentList();
        assertSame(3, jtcList.size());
        assertSame(c3, jtcList.get(0));

        // Simulate document change of focused component
        Document oldDoc = c3.getDocument();
        Document newDoc = c3.getUI().getEditorKit(c3).createDefaultDocument();
        c3.setDocument(newDoc);
        assertSame(1, EditorRegistryListener.INSTANCE.firedCount);
        assertSame(newDoc, EditorRegistryListener.INSTANCE.newValue);
        assertSame(oldDoc, EditorRegistryListener.INSTANCE.oldValue);
        EditorRegistryListener.INSTANCE.reset(); // Reset to 0
        oldDoc = null;
        newDoc = null;

        // Simulate focusLost
        EditorRegistry.focusLost(c3, null);
        assertSame(1, EditorRegistryListener.INSTANCE.firedCount);
        assertSame(null, EditorRegistryListener.INSTANCE.newValue);
        assertSame(c3, EditorRegistryListener.INSTANCE.oldValue);
        EditorRegistryListener.INSTANCE.reset(); // Reset to 0

        EditorRegistry.focusGained(c1, null);
        assertSame(1, EditorRegistryListener.INSTANCE.firedCount);
        assertSame(c1, EditorRegistryListener.INSTANCE.newValue);
        assertSame(null, EditorRegistryListener.INSTANCE.oldValue);
        EditorRegistryListener.INSTANCE.reset(); // Reset to 0
        
        // Partial GC: c3
        c3 = null;
        jtcList = null;
        System.gc();
        assertSame(2, EditorRegistry.componentList().size());
        
        // Test full GC
        jtcList = null;
        c1 = null;
        c2 = null;
        c3 = null;
        EditorRegistryListener.INSTANCE.reset();
        System.gc();
        assertSame(0, EditorRegistry.componentList().size());

        
    }
    
    private static final class EditorRegistryListener implements PropertyChangeListener {
        
        static final EditorRegistryListener INSTANCE = new EditorRegistryListener();
        
        int firedCount;
        
        String propertyName;
        
        Object oldValue;
        
        Object newValue;
        
        public void propertyChange(PropertyChangeEvent evt) {
            firedCount++;
            propertyName = evt.getPropertyName();
            oldValue = evt.getOldValue();
            newValue = evt.getNewValue();
        }
        
        public void reset() {
            firedCount = 0;
            propertyName = null;
            oldValue = null;
            newValue = null;
        }

    }

}
