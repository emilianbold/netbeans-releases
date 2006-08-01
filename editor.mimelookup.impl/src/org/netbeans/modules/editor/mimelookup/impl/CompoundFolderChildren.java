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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 *
 * @author vita
 */
public final class CompoundFolderChildren {

    public static final String PROP_CHILDREN = "FolderChildren.PROP_CHILDREN"; //NOI18N

    private static final String HIDDEN_SUFFIX = "_hidden"; //NOI18N
    private static final String HIDDEN_ATTR_NAME = "hidden"; //NOI18N
    
    private static final Logger LOG = Logger.getLogger(CompoundFolderChildren.class.getName());
    
    private final String LOCK = new String("CompoundFolderChildren.LOCK"); //NOI18N
    private FolderChildren [] layers = null;
    private List children = Collections.EMPTY_LIST;
    
    private PCL listener = new PCL();
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public CompoundFolderChildren(String [] paths) {
        this(paths, true);
    }
    
    /** Creates a new instance of FolderPathLookup */
    public CompoundFolderChildren(String [] paths, boolean includeSubfolders) {
        this.layers = new FolderChildren [paths.length];
        for (int i = 0; i < paths.length; i++) {
            this.layers[i] = new FolderChildren(paths[i], includeSubfolders);
            this.layers[i].addPropertyChangeListener(listener);
        }
        
        rebuild();
    }
    
    public List getChildren() {
        synchronized (LOCK) {
            return children;
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private void rebuild() {
        PropertyChangeEvent event = null;
        
        synchronized (LOCK) {
            // Collect all children
            HashMap visible = new HashMap();
            HashMap hidden = new HashMap();
            
            for (int i = 0; i < layers.length; i++) {
                List layerKids = layers[i].getChildren();
                for (Iterator j = layerKids.iterator(); j.hasNext(); ) {
                    FileObject f = (FileObject) j.next();
                    String name = realName(f);
                    
                    if (isHidden(f)) {
                        if (!hidden.containsKey(name)) {
                            hidden.put(name, f);
                        }
                    }

                    if (!hidden.containsKey(name)) {
                        if (!visible.containsKey(name)) {
                            visible.put(name, f);
                        }
                    }
                }
            }

            // Collect all edges
            HashMap edges = new HashMap();
            for (int i = 0; i < layers.length; i++) {
                Map layerAttrs = layers[i].getFolderAttributes();
                for (Iterator j = layerAttrs.keySet().iterator(); j.hasNext(); ) {
                    String attrName = (String) j.next();
                    Object attrValue = layerAttrs.get(attrName);
                    
                    // Check whether the attribute affects sorting
                    int slashIdx = attrName.indexOf('/'); //NOI18N
                    if (slashIdx == -1 || !(attrValue instanceof Boolean)) {
                        continue;
                    }

                    // Get the file names
                    String name1 = attrName.substring(0, slashIdx);
                    String name2 = attrName.substring(slashIdx + 1);
                    if (!((Boolean) attrValue).booleanValue()) {
                        // Swap the names
                        String s = name1;
                        name1 = name2;
                        name2 = s;
                    }
                    
                    // Get the files and add them among the edges
                    FileObject from = (FileObject) visible.get(name1);
                    FileObject to = (FileObject) visible.get(name2);
                    
                    if (from != null && to != null) {
                        HashSet vertices = (HashSet) edges.get(from);
                        if (vertices == null) {
                            vertices = new HashSet();
                            edges.put(from, vertices);
                        }
                        vertices.add(to);
                    }
                }
            }
            
            // Sort the children
            List sorted;
            
            try {
                sorted = Utilities.topologicalSort(visible.values(), edges);
            } catch (TopologicalSortException e) {
                LOG.log(Level.WARNING, "Can't sort folder children.", e); //NOI18N
                sorted = e.partialSort();
            }
            
            if (!children.equals(sorted)) {
                event = new PropertyChangeEvent(this, PROP_CHILDREN, children, sorted);
                children = sorted;
            }
        }
        
        if (event != null) {
            pcs.firePropertyChange(event);
        }
    }

    private boolean isHidden(FileObject fo) {
        if (fo.getNameExt().endsWith(HIDDEN_SUFFIX)) {
            return true;
        }
        
        for (Enumeration e = fo.getAttributes(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            if (HIDDEN_ATTR_NAME.equals(name)) {
                Object value = fo.getAttribute(name);
                if ((value instanceof Boolean) && ((Boolean) value).booleanValue()){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private String realName(FileObject fo) {
        String nameExt = fo.getNameExt();
        if (nameExt.endsWith(HIDDEN_SUFFIX)) {
            return nameExt.substring(0, nameExt.length() - HIDDEN_SUFFIX.length());
        } else {
            return nameExt;
        }
    }
    
    private class PCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            // TODO: This should be optimized for ignoring changes coming from a layer,
            // which didn't contribute the file in the final result.
            if (evt.getPropertyName() != null &&
                FolderChildren.PROP_CHILD_CHANGED.equals(evt.getPropertyName()))
            {
                // The content of a child changed
                pcs.firePropertyChange(new PropertyChangeEvent(CompoundFolderChildren.this, PROP_CHILDREN, null, null));
            } else {
                // The list of children or their sorting changed
                rebuild();
            }
        }

    } // End of FolderListener class
}
