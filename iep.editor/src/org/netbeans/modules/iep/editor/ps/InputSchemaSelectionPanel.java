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

package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.openide.util.NbBundle;

/**
 * InputSchemaSelectionPanel.java
 *
 * Created on November 15, 2006, 2:42 PM
 *
 * @author Bing Lu
 */
public class InputSchemaSelectionPanel extends JPanel implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(InputSchemaSelectionPanel.class.getName());
    
    private List mCheckBoxList = new ArrayList();
    private List<SchemaAttribute> mAttributeList = new ArrayList<SchemaAttribute>();
    
    /** Creates a new instance of InputSchemaSelectionPanel */
    public InputSchemaSelectionPanel(IEPModel model, OperatorComponent component) {
        try {
            String msg = NbBundle.getMessage(InputSchemaSelectionPanel.class, "InputSchemaSelectionPanel.SELECTED_INPUT_ATTRIBUTES");
            setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
            setLayout(new BorderLayout(5, 5));
            JScrollPane scrollPane = new JScrollPane();
            add(scrollPane, BorderLayout.CENTER);
            
            //Put the check boxes in a column in a panel
            JPanel checkPanel = new JPanel(new GridBagLayout());
            scrollPane.getViewport().add(checkPanel);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            int gGridy = 0;
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;

            List<OperatorComponent> inputs = component.getInputOperatorList();
            if (inputs.size() < 1) {
                msg = NbBundle.getMessage(InputSchemaSelectionPanel.class,
                        "InputSchemaSelectionPanel.INPUT_IS_NOT_SPECIFIED");
                JLabel label = new JLabel(msg);
                checkPanel.add(label, gbc);
                setToolTipText(NbBundle.getMessage(InputSchemaSelectionPanel.class, "InputSchemaTreePanel_Tooltip.inputoperator_not_connected"));
                return;
            }
            setToolTipText(NbBundle.getMessage(InputSchemaSelectionPanel.class, "InputSchemaTreePanel_Tooltip.inputoperator_connected"));
            OperatorComponent input = inputs.get(0);
            SchemaComponent outputSchema = input.getOutputSchema();
            if (outputSchema == null) {
                msg = NbBundle.getMessage(InputSchemaSelectionPanel.class,
                        "InputSchemaSelectionPanel.INPUT_DOES_NOT_HAVE_ANY_SCHEMA");
                JLabel label = new JLabel(msg);
                checkPanel.add(label, gbc);
                return;
            }
            
            List<String> fromColumnList = (List) component.getStringList(PROP_FROM_COLUMN_LIST);
            List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
            Iterator<SchemaAttribute> attrsIt = attrs.iterator();
            while(attrsIt.hasNext()) {
                SchemaAttribute sa = attrsIt.next();
                mAttributeList.add(sa);
                String attributeName = sa.getName();
                JCheckBox cb = new JCheckBox(attributeName);
                mCheckBoxList.add(cb);
                if (fromColumnList.contains(attributeName)) {
                    cb.setSelected(true);
                } else {
                    cb.setSelected(false);
                }
                gbc.gridy = gGridy++;
                checkPanel.add(cb, gbc);
            }
            gbc.gridy = gGridy++;
            gbc.weighty = 1.0D;
            gbc.fill = GridBagConstraints.VERTICAL;
            checkPanel.add(Box.createHorizontalGlue(), gbc);
        } catch(Exception e) {
            mLog.log(Level.SEVERE, NbBundle.getMessage(InputSchemaSelectionPanel.class,
                    "InputSchemaSelectionPanel.FAIL_TO_BUILD_SELECTION_LIST_FOR", component.getTitle()), e);
        }
    }
    
    public List<SchemaAttribute> getSelectedAttributeList() {
        List<SchemaAttribute> ret = new ArrayList<SchemaAttribute>();
        for (int i = 0, I = mCheckBoxList.size(); i < I; i++) {
            if (((JCheckBox)mCheckBoxList.get(i)).isSelected()) {
                ret.add(mAttributeList.get(i));
            }
        }
        return ret;
    }

    public List<String> getSelectedAttributeNameList() {
        List<String> ret = new ArrayList<String>();
        try {
            for (int i = 0, I = mCheckBoxList.size(); i < I; i++) {
                if (((JCheckBox)mCheckBoxList.get(i)).isSelected()) {
                    ret.add((mAttributeList.get(i)).getAttributeName());
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }
    
    public void addItemListener(ItemListener listener) {
        for (int i = 0, I = mCheckBoxList.size(); i < I; i++) {
            ((JCheckBox)mCheckBoxList.get(i)).addItemListener(listener);
        }
    }
}
