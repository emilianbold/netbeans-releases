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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.mappercore.vertexitemeditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.VertexItemEditor;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.utils.MetalTextFieldBorder;

/**
 *
 * @author anjeleevich
 */
public abstract class AbstractTextVertexItemEditor extends JTextField 
    implements FocusListener, ActionListener, VertexItemEditor
{
    private List<CellEditorListener> listeners = null;


    public AbstractTextVertexItemEditor() {
        MetalTextFieldBorder.installIfItIsNeeded(this);
        
        addFocusListener(this);
        addActionListener(this);
        
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fireEditingCanceled();
            }
        };
        
        InputMap inputMap = getInputMap();
        ActionMap actionMap = getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "cancel-editing-action");
        actionMap.put("cancel-editing-action", cancelAction);
    }
    
    
    public void addEditorListener(CellEditorListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<CellEditorListener>();
        }
        listeners.add(listener);
    }
    
    
    public void removeEditorListener(CellEditorListener listener) {
        if (listeners != null) {
            int index = listeners.lastIndexOf(listener);
            if (index >= 0) {
                listeners.remove(index);
                if (listeners.isEmpty()) {
                    listeners = null;
                }
            }
        }
    }
    
    
    protected CellEditorListener[] getEditorListeners() {
        return (listeners == null) ? null
                : listeners.toArray(new CellEditorListener[listeners.size()]);
    }
    
    
    protected void fireEditingStopped() {
        CellEditorListener[] listeners = getEditorListeners();
        
        if (listeners != null) {
            ChangeEvent event = new ChangeEvent(this);
            for (int i = listeners.length - 1; i >= 0; i--) {
                listeners[i].editingStopped(event);
            }
        }
    }
    
    
    protected void fireEditingCanceled() {
        CellEditorListener[] listeners = getEditorListeners();
        
        if (listeners != null) {
            ChangeEvent event = new ChangeEvent(this);
            for (int i = listeners.length - 1; i >= 0; i--) {
                listeners[i].editingCanceled(event);
            }
        }
    }

    
    public void focusGained(FocusEvent e) {}

    
    public void focusLost(FocusEvent e) {
        if (getParent() != null) {
            fireEditingStopped();
        }
    }

    
    public void actionPerformed(ActionEvent e) {
        fireEditingStopped();
    }
    
    
    public abstract Component getVertexItemEditorComponent(Mapper mapper, 
            TreePath treePath, VertexItem vertexItem);
    
    public abstract Object getVertexItemEditorValue();

    public void stopVertexItemEditing() {
        fireEditingStopped();
    }

    public void cancelVertexItemEditing() {
        fireEditingCanceled();
    }
}
