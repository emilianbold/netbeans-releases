/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
