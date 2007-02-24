/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


/*
 * Created on Jun 9, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.treetable;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;

/**
 * @author sumitabhk
 *
 */
/**
 * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
 * to listen for changes in the ListSelectionModel it maintains. Once
 * a change in the ListSelectionModel happens, the paths are updated
 * in the DefaultTreeSelectionModel.
 */
class TreeTableSelectionModel1 extends DefaultTreeSelectionModel 
{ 

	/** Set to true when we are updating the ListSelectionModel. */
	protected boolean         updatingListSelectionModel;

	/**
	 * 
	 */
	public TreeTableSelectionModel1()
	{
		super();
		getListSelectionModel().addListSelectionListener
								(createListSelectionListener());
	}

	/**
	 * Returns the list selection model. ListToTreeSelectionModelWrapper
	 * listens for changes to this model and updates the selected paths
	 * accordingly.
	 */
	ListSelectionModel getListSelectionModel() {
		return listSelectionModel; 
	}

	/**
	 * This is overridden to set <code>updatingListSelectionModel</code>
	 * and message super. This is the only place DefaultTreeSelectionModel
	 * alters the ListSelectionModel.
	 */
	public void resetRowSelection() {
		if(!updatingListSelectionModel) {
		updatingListSelectionModel = true;
		try {
			super.resetRowSelection();
		}
		finally {
			updatingListSelectionModel = false;
		}
		}
		// Notice how we don't message super if
		// updatingListSelectionModel is true. If
		// updatingListSelectionModel is true, it implies the
		// ListSelectionModel has already been updated and the
		// paths are the only thing that needs to be updated.
	}

	/**
	 * Creates and returns an instance of ListSelectionHandler.
	 */
	protected ListSelectionListener createListSelectionListener() {
		return new ListSelectionHandler();
	}

	/**
	 * If <code>updatingListSelectionModel</code> is false, this will
	 * reset the selected paths from the selected rows in the list
	 * selection model.
	 */
	protected void updateSelectedPathsFromSelectedRows() {
		if(!updatingListSelectionModel) {
			updatingListSelectionModel = true;
			try {
				// This is way expensive, ListSelectionModel needs an
				// enumerator for iterating.
				int        min = listSelectionModel.getMinSelectionIndex();
				int        max = listSelectionModel.getMaxSelectionIndex();
	
				clearSelection();
				if(min != -1 && max != -1) {
					for(int counter = min; counter <= max; counter++) {
						if(listSelectionModel.isSelectedIndex(counter)) {
//						TreePath     selPath = tree.getPathForRow
//													(counter);
//		
//							if(selPath != null) {
//								addSelectionPath(selPath);
//							}
						}
					}
				}
			}
			finally {
				updatingListSelectionModel = false;
			}
		}
	}

	/**
	 * Class responsible for calling updateSelectedPathsFromSelectedRows
	 * when the selection of the list changse.
	 */
	class ListSelectionHandler implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
		updateSelectedPathsFromSelectedRows();
		}
	}
}



