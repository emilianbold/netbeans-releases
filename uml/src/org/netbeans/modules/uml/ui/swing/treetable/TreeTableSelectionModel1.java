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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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



