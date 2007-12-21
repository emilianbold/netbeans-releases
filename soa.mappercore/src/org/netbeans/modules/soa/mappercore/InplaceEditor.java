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

package org.netbeans.modules.soa.mappercore;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Operation;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.vertexitemeditor.NumberVertexItemEditor;
import org.netbeans.modules.soa.mappercore.vertexitemeditor.StringVertexItemEditor;

/**
 *
 * @author anjeleevich
 */
public class InplaceEditor extends MapperPropertyAccess implements 
        CellEditorListener 
{
    private Canvas canvas;
    
//    private VertexItemEditor vertexItemEditor 
//            = new DefaultVertexItemEditor();
    
    private Map<Class, VertexItemEditor> editors = new HashMap<Class, 
            VertexItemEditor>();
    
    private VertexItemEditor currentEditor = null;
    private TreePath currentTreePath = null;
    private Component currentEditorComponent = null;
    private VertexItem currentVertexItem = null;
 
    
    public InplaceEditor(Canvas canvas) {
        super(canvas.getMapper());
        this.canvas = canvas;
        editors.put(String.class, new StringVertexItemEditor());
        editors.put(Number.class, new NumberVertexItemEditor());
    }

    
    public void setVertexItemEditor(Class valueType, VertexItemEditor editor) {
        cancelEdit();
        
        if (editor == null) {
            editors.remove(valueType);
        } else {
            editors.put(valueType, editor);
        }
    }

    
    public VertexItemEditor getVertexItemEditor(Class valueType) {
        return editors.get(valueType);
    }
    
    
    public void startEdit(TreePath treePath, VertexItem vertexItem) {
        cancelEdit();
        
        if (vertexItem.isHairline()) return;
        if (vertexItem.getVertex() instanceof Operation) return;
        
        Class valueType = vertexItem.getValueType();
        
        if (valueType == null) return;
        
        currentEditor = editors.get(valueType);
        currentEditorComponent = currentEditor.getVertexItemEditorComponent(
                getMapper(), treePath, vertexItem);
        currentTreePath = treePath;
        currentVertexItem = vertexItem;
        
        canvas.add(currentEditorComponent);
        
        currentEditor.addEditorListener(this);
        currentEditorComponent.requestFocusInWindow();
        
        layoutEditor();
    }
    
    
    
    public void stopEdit() {
        if (currentEditor != null) {
            TreePath treePath = currentTreePath;
            VertexItem vertexItem = currentVertexItem;
            Object value = currentEditor.getVertexItemEditorValue();
            cancelEdit();
            getMapperModel().valueChanged(treePath, vertexItem, value);
        }
    }
    
    
    public void cancelEdit() {
        if (currentEditor != null) {
            currentEditor.removeEditorListener(this);
            
            boolean focusToCanvas = currentEditorComponent.isFocusOwner();
            
            canvas.remove(currentEditorComponent);
            canvas.repaint();
            
            currentEditor = null;
            currentEditorComponent = null;
            currentTreePath = null;
            currentVertexItem = null;
            
            if (focusToCanvas) {
                canvas.requestFocusInWindow();
            }
        }
    }
    
    
    public void layoutEditor() {
        if (currentEditorComponent != null) {
            Mapper mapper = getMapper();
            
            int step = mapper.getStepSize();
            
            int y = (step - 1) / 2 + 1 + currentVertexItem.getGlobalY() * step;
            int x = canvas.toCanvas(0) + currentVertexItem.getGlobalX() * step;
            
            MapperNode node = mapper.getNode(currentTreePath, true);
            while (node != null) {
                y += node.getY();
                node = node.getParent();
            }
            
            int w = currentVertexItem.getWidth() * step + 1;
            int h = currentVertexItem.getHeight() * step + 1;
            
            currentEditorComponent.setBounds(x, y, w, h);
        }
    }


    public void editingStopped(ChangeEvent e) {
        stopEdit();
    }

    
    public void editingCanceled(ChangeEvent e) {
        cancelEdit();
    }
}
