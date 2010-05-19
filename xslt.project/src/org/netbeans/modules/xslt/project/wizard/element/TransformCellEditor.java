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
 * License. When distributing the software, include this License Header
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
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xslt.project.XsltproConstants;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vitaly Bychkov 
 * @version 1.0
 */
public class TransformCellEditor extends AbstractCellEditor implements TableCellEditor {
    
    private TransformEditorPanel myPanel;
    /**
     * 
     * The delegate class which handles all methods sent from the
     * <code>CellEditor</code>.
     */
    private EditorDelegate myDelegate;

    /**
     * An integer specifying the number of clicks needed to start editing.
     * Even if <code>clickCountToStart</code> is defined as zero, it
     * will not initiate until a click occurs.
     */
    private int clickCountToStart = 1;



    public TransformCellEditor(FileObject folder, Project project) {
        TransformEditorPanel panel = new TransformEditorPanel(folder, project);
        myPanel = panel;
        myDelegate = new EditorDelegate(myPanel);
        myPanel.addActionListener(myDelegate);
        
    }

    public Component getTableCellEditorComponent(JTable table, 
                                                 Object value,
                                                  boolean isSelected,
                                                  int row, 
                                                  int column) {
        if(column == 1) {
            myDelegate.setValue(value.toString());
            return myPanel;                                                      
        }

        return null;
    }

    public Object getCellEditorValue() {
        return myDelegate.getCellEditorValue();
    }
    
    /**
     * Specifies the number of clicks needed to start editing.
     *
     * @param count  an int specifying the number of clicks needed to start editing
     * @see #getClickCountToStart
     */
    public void setClickCountToStart(int count) {
	clickCountToStart = count;
    }

    /**
     * Returns the number of clicks needed to start editing.
     * @return the number of clicks needed to start editing
     */
    public int getClickCountToStart() {
	return clickCountToStart;
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     * @see EditorDelegate#isCellEditable(EventObject)
     */
    public boolean isCellEditable(EventObject anEvent) { 
	return myDelegate.isCellEditable(anEvent); 
    }
    
    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     * @see EditorDelegate#shouldSelectCell(EventObject)
     */
    @Override
    public boolean shouldSelectCell(EventObject anEvent) { 
	return myDelegate.shouldSelectCell(anEvent); 
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     * @see EditorDelegate#stopCellEditing
     */
    @Override
    public boolean stopCellEditing() {
	return myDelegate.stopCellEditing();
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     * @see EditorDelegate#cancelCellEditing
     */
    @Override
    public void cancelCellEditing() {
	myDelegate.cancelCellEditing();
    }

    private class EditorDelegate implements ActionListener, Serializable {

        /**  The value of this cell. */
        private TransformEditorPanel myEditorPanel;

        public EditorDelegate(TransformEditorPanel editorPanel) {
            myEditorPanel = editorPanel;
        }        
        
       /**
        * Returns the value of this cell. 
        * @return the value of this cell
        */
        public Object getCellEditorValue() {
            return myEditorPanel.getTextField().getText();
        }

       /**
        * Sets the value of this cell. 
        * @param value the new value of this cell
        */
    	public void setValue(Object value) { 
	    myEditorPanel.getTextField().setText(value == null ? XsltproConstants.EMPTY_STRING : value.toString()); 
	}

       /**
        * Returns true if <code>anEvent</code> is <b>not</b> a
        * <code>MouseEvent</code>.  Otherwise, it returns true
        * if the necessary number of clicks have occurred, and
        * returns false otherwise.
        *
        * @param   anEvent         the event
        * @return  true  if cell is ready for editing, false otherwise
        * @see #setClickCountToStart
        * @see #shouldSelectCell
        */
        public boolean isCellEditable(EventObject anEvent) {
	    if (anEvent instanceof MouseEvent) { 
		return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
	    }
	    return true;
	}
    	
       /**
        * Returns true to indicate that the editing cell may
        * be selected.
        *
        * @param   anEvent         the event
        * @return  true 
        * @see #isCellEditable
        */
        public boolean shouldSelectCell(EventObject anEvent) { 
            return true; 
        }

       /**
        * Returns true to indicate that editing has begun.
        *
        * @param anEvent          the event
        */
        public boolean startCellEditing(EventObject anEvent) {
	    return true;
	}

       /**
        * Stops editing and
        * returns true to indicate that editing has stopped.
        * This method calls <code>fireEditingStopped</code>.
        *
        * @return  true 
        */
        public boolean stopCellEditing() { 
	    fireEditingStopped(); 
	    return true;
	}

       /**
        * Cancels editing.  This method calls <code>fireEditingCanceled</code>.
        */
       public void cancelCellEditing() { 
	   fireEditingCanceled(); 
       }

       /**
        * When an action is performed, editing is ended.
        * @param e the action event
        * @see #stopCellEditing
        */
        public void actionPerformed(ActionEvent e) {
            TransformCellEditor.this.stopCellEditing();
	}
    }
}
