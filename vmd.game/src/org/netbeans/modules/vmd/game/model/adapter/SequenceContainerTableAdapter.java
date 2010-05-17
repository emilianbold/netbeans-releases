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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.game.model.adapter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.SequenceContainerListener;
import org.netbeans.modules.vmd.game.model.SequenceListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author kherink
 */
public class SequenceContainerTableAdapter implements TableModel, SequenceContainerListener, SequenceListener, PropertyChangeListener {
	
	private static final int COLS = 4;
	
	private static final int COL_DEFAULT = 0;
	private static final int COL_NAME = 1;
	private static final int COL_FRAMES = 2;
	private static final int COL_DELAY = 3;

	private SequenceContainer sequenceContainer;
	private ArrayList listeners = new ArrayList();
	
	public SequenceContainerTableAdapter(SequenceContainer sequenceContainer) {
		this.sequenceContainer = sequenceContainer;
		this.sequenceContainer.addSequenceContainerListener(this);
		this.sequenceContainer.addPropertyChangeListener(this);
		for (Sequence s : this.sequenceContainer.getSequences()) {
			s.addSequenceListener(this);
			s.addPropertyChangeListener(this);
		}
	}

	//------- TableModel
	
	public int getRowCount() {
		return this.sequenceContainer.getSequenceCount();
	}

	public int getColumnCount() {
		return COLS;
	}

	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case COL_DEFAULT:
				return NbBundle.getMessage(SequenceContainerTableAdapter.class, "SequenceContainerTableAdapter.columnDefault.txt");
			case COL_NAME:
				return NbBundle.getMessage(SequenceContainerTableAdapter.class, "SequenceContainerTableAdapter.columnName.txt");
			case COL_FRAMES:
				return NbBundle.getMessage(SequenceContainerTableAdapter.class, "SequenceContainerTableAdapter.columnFrames.txt");
			case COL_DELAY:
				return NbBundle.getMessage(SequenceContainerTableAdapter.class, "SequenceContainerTableAdapter.columnDelay.txt");
			default:
				return "???"; // NOI18N
		}
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case COL_DEFAULT:
				return Boolean.class;
			case COL_NAME:
				return String.class;
			case COL_FRAMES:
				return Short.class;
			case COL_DELAY:
				return Integer.class;
			default:
				return Object.class;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case COL_DEFAULT:
				return true;
			case COL_NAME:
				return true;
			case COL_FRAMES:
				return false;
			case COL_DELAY:
				return true;
			default:
				return false;
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Sequence s = this.sequenceContainer.getSequenceAt(rowIndex);
		if (s == null) {
			return null;
		}
		switch (columnIndex) {
			case COL_DEFAULT:
				return this.sequenceContainer.getDefaultSequence() == s;
			case COL_NAME:
				return s.getName();
			case COL_FRAMES:
				return s.getFrameCount();
			case COL_DELAY:
				return s.getFrameMs();
			default:
				return null;
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Sequence s = this.sequenceContainer.getSequenceAt(rowIndex);
		switch (columnIndex) {
			case COL_DEFAULT:
				this.sequenceContainer.setDefaultSequence(s);
				break;
			case COL_NAME:
				String name = (String) aValue;
				if (!this.sequenceContainer.getGameDesign().isComponentNameAvailable(name)) {
					DialogDisplayer.getDefault().notify(
							new DialogDescriptor.Message(
							NbBundle.getMessage(SceneLayerTableAdapter.class, "SequenceContainerTableAdapter.noRenameDialog.txt", name),
							//"Sequence cannot be renamed because component name '" + name + "' already exists.", 
							DialogDescriptor.ERROR_MESSAGE)
					);
				}
				else {
					s.setName(name);
				}
				break;
			case COL_DELAY:
				s.setFrameMs((Integer) aValue);
				break;
		}
	}

	public void addTableModelListener(TableModelListener l) {
		this.listeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		this.listeners.remove(l);
	}

	//-------- SequenceContainerListener
	
	public void sequenceAdded(SequenceContainer source, Sequence sequence, int index) {
		TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		sequence.addPropertyChangeListener(this);
		sequence.addSequenceListener(this);
		this.fireTableChanged(e);
	}

	public void sequenceRemoved(SequenceContainer source, Sequence sequence, int index) {
		TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
		sequence.removePropertyChangeListener(this);
		sequence.removeSequenceListener(this);
		this.fireTableChanged(e);
	}

	public void sequenceMoved(SequenceContainer source, Sequence sequence, int indexOld, int indexNew) {
		this.sequenceRemoved(source, sequence, indexOld);
		this.sequenceAdded(source, sequence, indexNew);
	}

	private void fireTableChanged(TableModelEvent e) {
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			TableModelListener l = (TableModelListener) iter.next();
			l.tableChanged(e);
		}
		
	}

	//--------- PropertyChangeListener
	
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof  Sequence) {
			Sequence s = (Sequence) evt.getSource();
			int index = this.sequenceContainer.indexOf(s);
			TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
			this.fireTableChanged(e);
		}
		if (evt.getSource() instanceof  SequenceContainer) {
			SequenceContainer sc = (SequenceContainer) evt.getSource();
			if (evt.getPropertyName().equals(SequenceContainer.PROPERTY_DEFAULT_SEQUENCE)) {
				Sequence old = (Sequence) evt.getOldValue();
				Sequence current = (Sequence) evt.getNewValue();
				int oldIndex = this.sequenceContainer.indexOf(old);
				int newIndex = this.sequenceContainer.indexOf(current);
				//System.out.println("old = " + oldIndex + " new " + newIndex);
				TableModelEvent e = new TableModelEvent(this, oldIndex, oldIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
				this.fireTableChanged(e);
				
				e = new TableModelEvent(this, newIndex, newIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
				this.fireTableChanged(e);
			}
		}
	}

	public void frameAdded(Sequence sequence, int index) {
		int seqIndex = this.sequenceContainer.indexOf(sequence);
		TableModelEvent e = new TableModelEvent(this, seqIndex, seqIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		this.fireTableChanged(e);
	}

	public void frameRemoved(Sequence sequence, int index) {
		int seqIndex = this.sequenceContainer.indexOf(sequence);
		TableModelEvent e = new TableModelEvent(this, seqIndex, seqIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		this.fireTableChanged(e);
	}

	public void frameModified(Sequence sequence, int index) {
		//don't care
	}

	public void framesChanged(Sequence sequence) {
		TableModelEvent e = new TableModelEvent(this);
		this.fireTableChanged(e);
	}
	
}
