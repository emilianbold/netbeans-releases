/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.editor.highlights;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseCaret;
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
    
    private Object tag = new Object();

     public HighlighterImpl() {
        comp2FO = new WeakHashMap/*<JTextComponent, FileObject>*/();
        fo2Comp = new WeakHashMap/*<FileObject, Collection<JTextComponent>>*/();
    }
    
    private HighlightLayer getLayer(JTextComponent c) {
        return (HighlightLayer) c.getClientProperty(tag);
    }
    
    synchronized void assureRegistered(JTextComponent c) {
        if (c == null || getLayer(c) != null)
            return ;
        
        comp2FO.put(c, null);
        c.addPropertyChangeListener(this);
        updateFileObjectMapping(c);
        
        HighlightLayer layer = new HighlightLayer();
        
        c.putClientProperty(tag, layer);
        
//        Logger.getLogger("TIMER").log(Level.FINE, "HighlightLayer", new Object[] {"HighlightLayers", layer});
        
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
        
        final Object source = evt.getSource();
        
        if ("UI".equals(evt.getPropertyName()) && source instanceof JTextComponent) { // NOI18N
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JTextComponent comp = (JTextComponent) source;
                    synchronized (HighlighterImpl.this) {
                        HighlightLayer layer = getLayer(comp);
                        
                        if (layer == null) {
                            return ;
                        }
                        
                        EditorUI eui = Utilities.getEditorUI(comp);

                        if (eui != null) {
                            if (eui.findLayer(HighlightLayer.NAME) == null) {
                                eui.addLayer(layer, HighlightLayer.VISIBILITY);
                            }
                        }
                    }
                }
            });
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
            
            // force caret repaint, see #100384
            Caret caret = c.getCaret();
            if (caret instanceof BaseCaret) {
                ((BaseCaret) caret).refresh();
            }
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        assureRegistered(Registry.getMostActiveComponent());
    }
    
}
