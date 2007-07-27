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
package org.netbeans.modules.vmd.game.editor.sequece;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.adapter.SequenceContainerTableAdapter;
import org.openide.util.NbBundle;

/**
 *
 * @author kherink
 */
public class SequenceContainerNavigator extends JTable {
	
	private SequenceContainer sequenceContainer;
	
	//private ButtonGroup groupDefaultRadio = new Butt;
	
	public SequenceContainerNavigator(SequenceContainer sequenceContainer) {
		this.setModel(new SequenceContainerTableAdapter(sequenceContainer));
		this.getColumnModel().setColumnMargin(0);
		this.setShowVerticalLines(false);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		this.setDefaultRenderer(Short.class, new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				this.setHorizontalAlignment(SwingConstants.CENTER);
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		});
		this.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				this.setHorizontalAlignment(SwingConstants.CENTER);
				return super.getTableCellRendererComponent(
						table, 
						NbBundle.getMessage(SequenceContainerNavigator.class, "SequenceContainerNavigator.animationDelay.txt", value), 
						isSelected, hasFocus, row, column);
			}
		});
		
		this.setDefaultRenderer(Boolean.class, new SequenceDefaultTableEditor());
		this.setDefaultEditor(Boolean.class, new SequenceDefaultTableEditor());
		
		this.setDefaultEditor(Integer.class, new SequenceDelayTableEditor());
	}

}
