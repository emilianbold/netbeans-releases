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
package org.netbeans.modules.editor.highlights;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Registry;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.highlights.spi.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.WeakSet;

/**
 *
 * @author Jan Lahoda
 */
public final class HighlighterImpl implements PropertyChangeListener, ChangeListener {
    
    private static HighlighterImpl INSTANCE = new HighlighterImpl();
    
    public static HighlighterImpl getDefault() {
        return INSTANCE;
    }
    
    private Map/*<JTextComponent, FileObject>*/ comp2FO;
    private Map/*<FileObject, Collection<JTextComponent>>*/ fo2Comp;
    
    private Map/*<JTextComponent, Reference<HighlightLayer>>*/ comp2Layer;

     public HighlighterImpl() {
        comp2FO = new WeakHashMap/*<JTextComponent, FileObject>*/();
        fo2Comp = new WeakHashMap/*<FileObject, Collection<JTextComponent>>*/();
        comp2Layer = new WeakHashMap/*<JTextComponent, Reference<HighlightLayer>>*/();
    }
    
    private HighlightLayer getLayer(JTextComponent c) {
        Reference r = (Reference) comp2Layer.get(c);
        
        if (r == null)
            return null;
        
        return (HighlightLayer) r.get();
    }
    
    synchronized void assureRegistered(JTextComponent c) {
        if (c == null || getLayer(c) != null)
            return ;
        
        comp2FO.put(c, null);
        c.addPropertyChangeListener(this);
        updateFileObjectMapping(c);
        
        HighlightLayer layer = new HighlightLayer();
        
        comp2Layer.put(c, new WeakReference(layer));
        
        EditorUI eui = Utilities.getEditorUI(c);
        
        if (eui != null)
            eui.addLayer(layer, HighlightLayer.VISIBILITY);
    }
    
    private synchronized void updateFileObjectMapping(JTextComponent c) {
        Document doc = c.getDocument();
        Object   stream = doc.getProperty(Document.StreamDescriptionProperty);
        
        FileObject old = (FileObject) comp2FO.put(c, null);
        
        if (old != null) {
            Collection/*<JTextComponent>*/ components = (Collection) fo2Comp.get(old);
            
            if (components != null) {
                components.remove(c);
            }
        }
                
        if (stream != null && stream instanceof DataObject) {
            FileObject fo = ((DataObject) stream).getPrimaryFile();
            
            comp2FO.put(c, fo);
            getComponents(fo).add(c);
        }
        
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ("document".equals(evt.getPropertyName())) { // NOI18N
            updateFileObjectMapping((JTextComponent) evt.getSource());
        }
    }
    
    private Collection/*<JTextComponent>*/ getComponents(FileObject fo) {
        Collection/*<JTextComponent>*/ components = (Collection) fo2Comp.get(fo);
        
        if (components == null) {
            fo2Comp.put(fo, components = new WeakSet/*<JTextComponent>*/());
        }
        
        return components;
    }
    
    public synchronized void setHighlights(FileObject fo, String type, Collection/*<Highlight>*/ highlights) {
//        for (JTextComponent c : getComponents(fo)) {
        for (Iterator i = getComponents(fo).iterator(); i.hasNext(); ) {
            JTextComponent c = (JTextComponent) i.next();
            HighlightLayer layer = getLayer(c);
            
            if (layer == null)
                continue;
            
            layer.setHighlights(type, highlights);
            
            c.repaint(); //TODO: not very efficient.
        }
    }

    public void stateChanged(ChangeEvent e) {
        assureRegistered(Registry.getMostActiveComponent());
    }
    
}
