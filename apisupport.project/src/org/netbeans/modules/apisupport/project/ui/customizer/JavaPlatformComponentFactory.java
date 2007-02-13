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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.util.WeakListeners;

/**
 * GUI tools for working with the Java platform.
 * @author Jesse Glick
 */
public class JavaPlatformComponentFactory {
    
    private JavaPlatformComponentFactory() {}
    
    public static ComboBoxModel/*<JavaPlatform>*/ javaPlatformListModel() {
        return new Model();
    }
    
    public static ListCellRenderer/*<JavaPlatform>*/ javaPlatformListCellRenderer() {
        return new Renderer();
    }
    
    private static final class Model implements ComboBoxModel/*<JavaPlatform>*/, PropertyChangeListener, Comparator<JavaPlatform> {
        
        private static final Collator COLL = Collator.getInstance();
        private static final JavaPlatformManager mgr = JavaPlatformManager.getDefault();
        private final SortedSet<JavaPlatform> platforms = new TreeSet(this);
        private final List<ListDataListener> listeners = new ArrayList();
        private JavaPlatform selected;
        
        public Model() {
            refresh();
            mgr.addPropertyChangeListener(WeakListeners.propertyChange(this, mgr));
        }
        
        private void refresh() {
            platforms.clear();
            platforms.addAll(Arrays.asList(mgr.getInstalledPlatforms()));
        }

        public int getSize() {
            return platforms.size();
        }

        public Object getElementAt(int index) {
            return (JavaPlatform) new ArrayList(platforms).get(index);
        }

        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
        
        public void setSelectedItem(Object sel) {
            if (sel != selected) {
                selected = (JavaPlatform) sel;
                fireChange();
            }
        }

        public Object getSelectedItem() {
            return selected;
        }
        
        private void fireChange() {
            ListDataEvent ev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0);
            Iterator it = new ArrayList(listeners).iterator();
            while (it.hasNext()) {
                ((ListDataListener) it.next()).contentsChanged(ev);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            refresh();
            fireChange();
        }

        public int compare(JavaPlatform p1, JavaPlatform p2) {
            int res = COLL.compare(p1.getDisplayName(), p2.getDisplayName());
            if (res != 0) {
                return res;
            } else {
                String id1 = ModuleProperties.getPlatformID(p1);
                String id2 = ModuleProperties.getPlatformID(p2);
                if (id1 != null && id2 != null) {
                    return id1.compareTo(id2);
                } else {
                    return System.identityHashCode(p1) - System.identityHashCode(p2);
                }
            }
        }

    }
    
    private static final class Renderer extends JLabel implements ListCellRenderer, UIResource /*<JavaPlatform>*/ {
        
        public Renderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            String name = value != null ? ((JavaPlatform) value).getDisplayName() : null;
            setText(name);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }
    
}
