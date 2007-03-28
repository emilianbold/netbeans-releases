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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.view.layout.TableLayout;

/**
 *
 * @author Ajit Bhate
 */
public class TableWidget extends Widget{
    
    private TableModel model;
    private final static int COLUMN_WIDTH = 100;

    /**
     * Creates a table widget for a tablemodel.
     * @param scene 
     * @param model 
     */
    public TableWidget(Scene scene, TableModel model) {
        super(scene);
        this.model = model;
        setLayout(new TableLayout(model.getColumnCount(), 0, 0,COLUMN_WIDTH));
        setBorder(BorderFactory.createLineBorder(1,Color.LIGHT_GRAY));
        createTableHeader();
        createTable();
    }
    
    private void createTableHeader() {
        Scene scene = getScene();
        for (int i = 0; i<model.getColumnCount();i++) {
            LabelWidget columnHeader = new LabelWidget(scene, model.getColumnName(i));
            columnHeader.setBorder(BorderFactory.createLineBorder(1,Color.LIGHT_GRAY));
            columnHeader.setAlignment(LabelWidget.Alignment.CENTER);
            columnHeader.setBackground(new Color(240,240,240));
            columnHeader.setOpaque(true);
            addChild(columnHeader);
        }
    }

    private void createTable() {
        Scene scene = getScene();
        for(int j=0; j<model.getRowCount();j++) {
            for (int i = 0; i<model.getColumnCount();i++) {
                final LabelWidget cellWidget = new LabelWidget(scene, model.getValueAt(j, i));
                cellWidget.setBorder(BorderFactory.createLineBorder(1,Color.LIGHT_GRAY));
                if(model.isCellEditable(j, i)) {
                    final int row = j;
                    final int column = i;
                    cellWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(
                            new TextFieldInplaceEditor(){
                        public boolean isEnabled(Widget widget) {
                            return true;
                        }

                        public String getText(Widget widget) {
                            return model.getValueAt(row, column);
                        }

                        public void setText(Widget widget, String text) {
                            model.setValueAt(text, row, column);
                            cellWidget.setLabel(text);
                        }
                    }));
                }
                addChild(cellWidget);
            }
        }
    }
}
