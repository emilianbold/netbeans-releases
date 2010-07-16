/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
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
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.game.model;

import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public abstract class Layer implements Previewable, Editable, Transferable, Identifiable {
	
	private long id = Identifiable.ID_UNKNOWN;

	public static final String ACTION_PROP_SCENE = "layer.action.prop.scene"; // NOI18N
	
	protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	private GlobalRepository gameDesign;
	
	private String name;
	private ImageResource imageResource;
	private int cellWidth;
	private int cellHeight;
	
	protected Layer(GlobalRepository gameDesign, String name, ImageResource imageResource, int cellWidth, int cellHeight) {
		assert (gameDesign != null);
		this.gameDesign = gameDesign;
		this.name = name;
		this.imageResource = imageResource;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
	}
		
	public String getName() {
		return this.name;
	}
	
	public GlobalRepository getGameDesign() {
		return this.gameDesign;
	}
	
	public void setName(String name) {
		if (name == null) {
			return;
		}
		if (this.getName().equals(name)) {
			return;
		}
		if (!this.gameDesign.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Layer cannot be renamed because component name '" + name + "' already exists."); // NOI18N
		}
		String oldName = this.name;
		this.name = name;
		this.propertyChangeSupport.firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	public ImageResource getImageResource() {
		return this.imageResource;
	}

	public String toString() {
		return this.getName();
	}

	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}

	public abstract void paint(Graphics2D g);
	
	public abstract int getHeight();
	
	public abstract int getWidth();
	
	//Transferable Interface
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return this;
	}

	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[1];
		try {
			flavors[0] = new LayerDataFlavor();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor instanceof LayerDataFlavor)
			return true;
		return false;
	}
	
	public int getTileHeight() {
		return this.cellHeight;
	}

	public int getTileWidth() {
		return this.cellWidth;
	}

	public abstract String getDisplayableTypeName();
	
	public static class NameComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			if ( !(arg0 instanceof Layer) || !(arg1 instanceof Layer))
				throw new ClassCastException("Compared object not instance of Layer"); // NOI18N
			Layer l0 = (Layer) arg0;
			Layer l1 = (Layer) arg1;
			return (l0.getName().compareTo(l1.getName()));
		}
	}
	
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>();
		actions.add(new EditLayerAction());
		actions.add(new RemoveAction());
		return Collections.unmodifiableList(actions);
	}

	public JComponent getNavigator() {
		//TODO : implement this
		return null;
	}
	
	public class EditLayerAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Layer.class, "Layer.editAction", getDisplayableTypeName()));
		}
		public void actionPerformed(ActionEvent e) {
			Layer.this.gameDesign.getMainView().requestEditing(Layer.this);
		}
	}

	public class RemoveAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Layer.class, "Layer.removeAction", getDisplayableTypeName()));
		}
		public void actionPerformed(ActionEvent e) {
			Scene scene = (Scene) this.getValue(Layer.ACTION_PROP_SCENE);
			//if we were called in the context of a scene we remove the layer from a scene
			if (scene != null) {
				scene.remove(Layer.this);
			}
			//else delete completely
			else {
				Object response = DialogDisplayer.getDefault().notify(
						new NotifyDescriptor(NbBundle.getMessage(Layer.class, "Layer.removeDialog.text", new Object[] {getDisplayableTypeName(), getName()}),
						NbBundle.getMessage(Layer.class, "Layer.removeDialog.title"),
						NotifyDescriptor.YES_NO_OPTION,
						NotifyDescriptor.QUESTION_MESSAGE,
						new Object[] {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION},
						NotifyDescriptor.YES_OPTION));
				if (response == NotifyDescriptor.YES_OPTION) {
					Layer.this.gameDesign.removeLayer(Layer.this);
				}
			}
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
