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
package org.netbeans.modules.edm.editor.widgets.property.editor;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.EnumSet;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.widgets.EDMPinWidget;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLObject;

/**
 * Column Selection Editor is an Inplace Editor that enables selection of columns.
 * Depending on whether the column is selected/de-selected it gets reflected in the Table Node.
 * 
 * @author Nithya
 */
public class ColumnSelectionEditor implements InplaceEditorProvider {
    
    private MashupDataObject mObj;
    
    public ColumnSelectionEditor(MashupDataObject dObj) {
        mObj = dObj;
    }
     public ColumnSelectionEditor() {
      
    }
    
    public void notifyOpened(EditorController controller, Widget widget, JComponent editor) {
        if(controller.isEditorVisible()){
            controller.openEditor(widget);
        }
        EDMPinWidget pin = (EDMPinWidget)widget;
        pin.enableEditor();
    }
    
    public void notifyClosing(EditorController controller, Widget widget, JComponent editor, boolean commit) {        
        EDMPinWidget pin = (EDMPinWidget)widget;
        pin.disableEditor();        
        SQLObject obj = mObj.getGraphManager().mapWidgetToObject(widget);
        mObj.getGraphManager().updateColumnSelection((SQLDBTable)((SQLDBColumn)obj).getParent());        
    }
    
    public JComponent createEditorComponent(EditorController controller, final Widget widget) {
        final EDMPinWidget pin = (EDMPinWidget)widget;
        final SQLObject obj = mObj.getGraphManager().mapWidgetToObject(widget);
        String name = (obj != null)? ((SQLDBColumn)obj).getDisplayName() : "";
        final JCheckBox check = pin.getEditor();
        check.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(widget instanceof EDMPinWidget) {
                    // Removed following condition
                    // || !((SQLDBColumn)obj).isNullable()
                    // We decided to allow deselecting NonNull columns too.
                    if(check.isSelected()) {
                        ((SQLDBColumn)obj).setVisible(true);
                    } else {
                        ((SQLDBColumn)obj).setVisible(false);
                    }                    
                    mObj.getMashupDataEditorSupport().synchDocument();                      
                }
            }
        });
        if(((SQLDBColumn)obj).isVisible()) {
            check.setSelected(true);
        } else {
            check.setSelected(false);
        }
        //For Data Mashup we are allowing to deselect any column
        /*
        if(((SQLDBColumn)obj).isNullable()) {
            check.setEnabled(true);
        }
        else {
            check.setEnabled(false);
        }*/
            
        return check;
    }
    
    public Rectangle getInitialEditorComponentBounds(EditorController controller, 
            Widget widget, JComponent editor, Rectangle viewBounds) {
        return viewBounds.getBounds();
    }
    
    public EnumSet getExpansionDirections(EditorController controller, 
            Widget widget, JComponent editor) {
        return EnumSet.of(InplaceEditorProvider.ExpansionDirection.LEFT, 
                InplaceEditorProvider.ExpansionDirection.RIGHT);
    }    
}