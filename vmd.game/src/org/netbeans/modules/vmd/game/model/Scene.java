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
package org.netbeans.modules.vmd.game.model;

import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.vmd.game.dialog.RenameSceneDialog;
import org.netbeans.modules.vmd.game.editor.scene.SceneEditor;
import org.netbeans.modules.vmd.game.editor.scene.SceneLayerNavigator;
import org.netbeans.modules.vmd.game.editor.scene.ScenePreviewPanel;
import org.netbeans.modules.vmd.game.nbdialog.SpriteDialog;
import org.netbeans.modules.vmd.game.nbdialog.TiledLayerDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class Scene implements GlobalRepositoryListener, PropertyChangeListener, Previewable, Editable {

	public static final boolean DEBUG = true;
	
	public static final String PROPERTY_LAYERS_BOUNDS = "prop.layers.bounds"; // NOI18N

	EventListenerList listenerList = new EventListenerList();

	// ---------- PropertyChangeSupport
	PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	private ArrayList<Layer> layers = new ArrayList<Layer>();
	private HashMap<Layer, LayerInfo> layerInfos = new HashMap<Layer, LayerInfo>();
	private String name;
	
	private SceneLayerNavigator navigator;
    private ScenePreviewPanel preview;
	
	private Rectangle allLayersBounds = new Rectangle();
	private SceneEditor editor;
	
	private GlobalRepository gameDesign;

	Scene(GlobalRepository gameDesign, String name) {
		assert (gameDesign != null);
		this.gameDesign = gameDesign;
		editor = null;
		this.name = name;
		this.gameDesign.addGlobalRepositoryListener(this);
	}

	Scene(GlobalRepository gameDesign, String name, Scene other) {
		this(gameDesign, name);
		for (Iterator iter = other.layers.iterator(); iter.hasNext();) {
			Layer layer = (Layer) iter.next();
			this.insert(layer, other.indexOf(layer));
			this.setLayerLocked(layer, other.isLayerLocked(layer));
			this.setLayerPosition(layer, other.getLayerPosition(layer), false);
			this.setLayerVisible(layer, other.isLayerVisible(layer));
		}
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (!this.gameDesign.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Scene cannot be renamed because component name '" + name + "' already exists."); // NOI18N
		}
		String oldName = this.name;
		this.name = name;
		this.propertyChangeSupport.firePropertyChange(PROPERTY_NAME, oldName, this.name);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}

	public synchronized void addSceneListener(SceneListener l) {
		this.listenerList.add(SceneListener.class, l);
	}

	public synchronized void removeSceneListener(SceneListener l) {
		this.listenerList.remove(SceneListener.class, l);
	}

	public Rectangle getAllLayersBounds() {
		return this.allLayersBounds;
	}
	private void updateLayersBounds() {
		int xMin = Integer.MAX_VALUE;
		int xMax = Integer.MIN_VALUE;
		int yMin = Integer.MAX_VALUE;
		int yMax = Integer.MIN_VALUE;
		
		for (Iterator iter = this.getLayers().iterator(); iter.hasNext();) {
			Layer layer = (Layer) iter.next();
			///System.out.println("looking at " + layer);
			Point point = this.getLayerPosition(layer);
			xMin = xMin < point.x ? xMin : point.x;
			yMin = yMin < point.y ? yMin : point.y;
			
			int right = point.x + layer.getWidth();
			xMax = xMax > right ? xMax : right;
			
			int bottom = point.y + layer.getHeight();;
			yMax = yMax > bottom ? yMax : bottom;
		}
		Rectangle newBounds = new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
		if (!this.allLayersBounds.equals(newBounds)) {
			//System.out.println("bounds changed from: " + this.allLayersBounds + " to " + newBounds);
			Rectangle oldBounds = this.allLayersBounds;
			this.allLayersBounds = newBounds;
			this.propertyChangeSupport.firePropertyChange(PROPERTY_LAYERS_BOUNDS, oldBounds, newBounds);
		}
	}
	
	public TiledLayer createTiledLayer(String name, ImageResource imageResource, int rows, int columns, int tileWidth, int tileHeight) {
		TiledLayer layer = this.gameDesign.createTiledLayer(name, imageResource, rows, columns, tileWidth, tileHeight);
		this.append(layer);
		return layer;
	}

	public Sprite createSprite(String name, ImageResource imageResource, int numberFrames, int frameWidth, int frameHeight) {
		Sprite sprite = this.gameDesign.createSprite(name, imageResource, numberFrames, frameWidth, frameHeight);
		this.append(sprite);
		return sprite;
	}

	private LayerInfo getLayerInfo(Layer l) {
		return this.layerInfos.get(l);
	}
	
	public boolean contains(Layer layer) {
		return this.indexOf(layer) >= 0;
	}
	
	/**
	 * Appends a layer to the end of this scene (furthest position from the user), 
	 * if the layer is already in the scene it is first removed.
	 */
	public void append(Layer layer) {
		if (DEBUG) System.out.println(this + " append " + layer); // NOI18N
		if (this.layers.contains(layer)) {
			this.remove(layer);
		}
		this.layers.add(layer);
		LayerInfo info = new LayerInfo();
		this.layerInfos.put(layer, info);
		int index = layers.indexOf(layer);
		layer.addPropertyChangeListener(this);
		this.updateLayersBounds();
		this.fireLayerAdded(layer, index);
	}

	/**
	 * Inserts a layer to this scene at the specified index, 
	 * if the layer is already in the scene it is first removed.
	 */
	public void insert(Layer layer, int index) {
		if (DEBUG) System.out.println(this + " insert " + layer); // NOI18N
		if (this.layers.contains(layer)) {
			this.move(layer, index);
			return;
		}
		this.layers.add(index, layer);
		LayerInfo info = new LayerInfo();
		this.layerInfos.put(layer, info);
		layer.addPropertyChangeListener(this);
		this.updateLayersBounds();
		this.fireLayerAdded(layer, index);
	}

	/**
	 * Removed the layer from this scene.
	 */
	public void remove(Layer layer) {
		if (DEBUG) System.out.println(this + " remove " + layer); // NOI18N
		int index = layers.indexOf(layer);
		if (this.layers.remove(layer)) {
			layer.removePropertyChangeListener(this);
			this.updateLayersBounds();
			this.fireLayerRemoved(layer, index);
		}
	}

	/**
	 * Move layer to a different position in the scene.
	 */
	public void move(Layer layer, int newIndex) {
		int oldIndex = layers.indexOf(layer);
		if (oldIndex == -1) {
			if (DEBUG) System.out.println(this + " cannot move " + layer + " - it is not present"); // NOI18N
			return;
		}
		if (DEBUG) System.out.println(this + " move " + layer + " from " + oldIndex + " to " + newIndex); // NOI18N
		this.layers.remove(layer);
		this.layers.ensureCapacity(newIndex + 1);
		this.layers.add(newIndex, layer);
		this.updateLayersBounds();
		this.fireLayerMoved(layer, oldIndex, newIndex);
	}

	private void fireLayerMoved(Layer layer, int oldIndex, int newIndex) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SceneListener.class) {
				((SceneListener) listeners[i+1]).layerMoved(this, layer, oldIndex, newIndex);
			}
		}
	}
	
	private void fireLayerAdded(Layer layer, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SceneListener.class) {
				((SceneListener) listeners[i+1]).layerAdded(this, layer, index);
			}
		}
	}

	private void fireLayerRemoved(Layer layer, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SceneListener.class) {
				((SceneListener) listeners[i+1]).layerRemoved(this, layer, (LayerInfo) this.getLayerInfo(layer), index);
			}
		}
	}

	private void fireLayerPositionModified(Layer layer, int index, Point oldPosition, Point newPosition, boolean inTransition) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SceneListener.class) {
				((SceneListener) listeners[i+1]).layerPositionChanged(this, layer, oldPosition, newPosition, inTransition);
			}
		}
	}
	private void fireLayerLockModified(Layer layer, int index, boolean locked) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SceneListener.class) {
				((SceneListener) listeners[i+1]).layerLockChanged(this, layer, locked);
			}
		}
	}
	private void fireLayerVisibilityModified(Layer layer, int index, boolean visible) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SceneListener.class) {
				((SceneListener) listeners[i+1]).layerVisibilityChanged(this, layer, visible);
			}
		}
	}

	public int indexOf(Layer layer) {
		return this.layers.indexOf(layer);
	}

	public Layer getLayerAt(int index) {
		return (Layer) layers.get(index);
	}

	public int getLayerCount() {
		return layers.size();
	}

	public List<Layer> getLayers() {
		return Collections.unmodifiableList(this.layers);
	}

	public List<Layer> getLayersAtPoint(Point p) {
		List<Layer> layers = new ArrayList<Layer>();
		for (Iterator iter = this.layers.iterator(); iter.hasNext();) {
			Layer l = (Layer) iter.next();
			LayerInfo li = this.getLayerInfo(l);
			Point lp = li.getPosition();
			if (lp.equals(p)) {
				layers.add(l);
				continue;
			}
			//if too far right
			if (lp.x > p.x) {
				continue;
			}
			//if too far below
			if (lp.y > p.y) {
				continue;
			}
			if (this.getLayerBounds(l).contains(p)) {
				layers.add(l);
			}
		}
		Collections.sort(layers, new LayerIndexComparator());
		return layers;
	}
	
	private class LayerIndexComparator implements Comparator<Layer> {
		public int compare(Layer l1, Layer l2) {
			return Scene.this.layers.indexOf(l1) - Scene.this.layers.indexOf(l2);
		}
	}
	
	public Rectangle getLayerBounds(Layer layer) {
		Point p = this.getLayerPosition(layer);
		return new Rectangle(p.x, p.y, layer.getWidth(), layer.getHeight()); 
	}
	
	public Point getLayerPosition(Layer layer) {
		Point position = ((LayerInfo) this.getLayerInfo(layer)).getPosition();
		return position;
	}

	public void setLayerPositionX(Layer layer, int x, boolean inTransition) {
		Point p = this.getLayerPosition(layer);
		p.x = x;
		this.setLayerPosition(layer, p, inTransition);
	}
	
	public void setLayerPositionY(Layer layer, int y, boolean inTransition) {
		Point p = this.getLayerPosition(layer);
		p.y = y;
		this.setLayerPosition(layer, p, inTransition);
	}
	
	public void setLayerPosition(Layer layer, int x, int y, boolean inTransition) {
		Point p = this.getLayerPosition(layer);
		p.x = x;
		p.y = y;
		this.setLayerPosition(layer, p, inTransition);
	}
	
	
	public void setLayerPosition(Layer layer, Point position, boolean inTransition) {
		if (this.isLayerLocked(layer))
			throw new IllegalArgumentException("Layer: " + layer + " is locked"); // NOI18N
		if (DEBUG) System.out.println(layer + " set position " + position); // NOI18N
		LayerInfo info = (LayerInfo) this.getLayerInfo(layer);
		Point old = info.getPosition();
		info.setPosition(position);
		this.updateLayersBounds();
		this.fireLayerPositionModified(layer, this.layers.indexOf(layer), old, info.getPosition(), inTransition);
	}

	public boolean isLayerVisible(Layer layer) {
		return ((LayerInfo) this.getLayerInfo(layer)).isVisible();
	}

	public void setLayerVisible(Layer layer, boolean visible) {
		if (DEBUG) System.out.println(layer + " set visible " + visible); // NOI18N
		((LayerInfo) this.getLayerInfo(layer)).setVisible(visible);
		this.fireLayerVisibilityModified(layer, this.layers.indexOf(layer), visible);
	}

	public boolean isLayerLocked(Layer layer) {
		return ((LayerInfo) this.getLayerInfo(layer)).isLocked();
	}

	public void setLayerLocked(Layer layer, boolean locked) {
		if (DEBUG) System.out.println(layer + " set locked " + locked); // NOI18N
		((LayerInfo) this.getLayerInfo(layer)).setLocked(locked);
		this.fireLayerLockModified(layer, this.layers.indexOf(layer), locked);
	}

	public static class LayerInfo {
		private Point position;
		private boolean visible;
		private boolean locked;

		public LayerInfo() {
			this.position = new Point();
			this.visible = true;
		}
		
		@Override
		public String toString() {
			return "Position: " + this.position + ", visible: " + this.visible + ", locked: " + this.locked; // NOI18N
		}
		
		/**
		 * Returns a COPY of layers position, changes to the returned objet don't affect the
		 * LayerInfo, to change the layer position use LayerInfo.setPosition()
		 * @return layer position within scene.
		 */
		public Point getPosition() {
			return new Point(this.position);
		}

		public void setPosition(Point position) {
			this.position = position;
		}

		public boolean isVisible() {
			return this.visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public boolean isLocked() {
			return this.locked;
		}

		public void setLocked(boolean locked) {
			if (DEBUG) System.out.println("setting layer locked = " + locked); // NOI18N
			this.locked = locked;
		}

	}

	// PropertyChangeListener
	public void propertyChange(PropertyChangeEvent evt) {
		if (DEBUG) System.out.println("property change from " + evt.getSource() + " new value: " + evt.getNewValue()); // NOI18N
	}

	public JComponent getEditor() {
		if (null == this.editor)
			this.editor = new SceneEditor(this);
		return this.editor.getJComponent();
	}

	public ImageResourceInfo getImageResourceInfo() {
		return null;
	}

	public GlobalRepository getGameDesign() {
		return this.gameDesign;
	}
	
	public String toString() {
		return this.name;
	}

	static class NameComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			if (!(arg0 instanceof Scene) || !(arg1 instanceof Scene))
				throw new ClassCastException("Compared object not instance of Scene"); // NOI18N
			Scene s0 = (Scene) arg0;
			Scene s1 = (Scene) arg1;
			return (s0.getName().compareTo(s1.getName()));
		}
	}

	public List<Action> getActions() {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new EditSceneAction());
		actions.add(new RemoveSceneAction());
		actions.add(new RenameSceneAction());
		return Collections.unmodifiableList(actions);
	}

	
	public class RenameSceneAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Scene.class, "Scene.RenameSceneAction.text"));
		}

		public void actionPerformed(ActionEvent e) {
			RenameSceneDialog dialog = new RenameSceneDialog(Scene.this);
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(Scene.class, "Scene.RenameSceneAction.text"));
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}
	
	public class EditSceneAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Scene.class, "Scene.EditSceneAction.text"));
		}

		public void actionPerformed(ActionEvent e) {
			Scene.this.gameDesign.getMainView().requestEditing(Scene.this);
		}
	}

	public class CreateSpriteAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Scene.class, "Scene.CreateSpriteAction.text"));
		}

		public void actionPerformed(ActionEvent e) {
			SpriteDialog nld = new SpriteDialog(Scene.this);
			DialogDescriptor dd = new DialogDescriptor(nld, NbBundle.getMessage(Scene.class, "Scene.CreateSpriteAction.text"));
			dd.setButtonListener(nld);
			dd.setValid(false);
			nld.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}

	public class CreateTiledLayerAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Scene.class, "Scene.CreateTiledLayerAction.text"));
		}

		public void actionPerformed(ActionEvent e) {
			TiledLayerDialog nld = new TiledLayerDialog(Scene.this);
			DialogDescriptor dd = new DialogDescriptor(nld, NbBundle.getMessage(Scene.class, "Scene.CreateTiledLayerAction.text"));
			dd.setButtonListener(nld);
			dd.setValid(false);
			nld.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}

	public class RemoveSceneAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Scene.class, "Scene.RemoveSceneAction.text"));
		}

		public void actionPerformed(ActionEvent e) {
				Object response = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
						NbBundle.getMessage(Scene.class, "Scene.RemoveDialog.text", getName()),
						NbBundle.getMessage(Scene.class, "Scene.RemoveSceneAction.text"),
						NotifyDescriptor.YES_NO_OPTION,
						NotifyDescriptor.QUESTION_MESSAGE,
						new Object[] {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION},
						NotifyDescriptor.YES_OPTION));
				if (response == NotifyDescriptor.YES_OPTION) {
					System.out.println("said YES to delete scene"); // NOI18N
					gameDesign.removeScene(Scene.this);
				}
		}
	}

	//----GlobalRepositoryListener
	public void sceneAdded(Scene scene, int index) {
	}

	public void sceneRemoved(Scene scene, int index) {
	}

	public void tiledLayerAdded(TiledLayer tiledLayer, int index) {
	}

	public void tiledLayerRemoved(TiledLayer tiledLayer, int index) {
		this.remove(tiledLayer);
	}

	public void spriteAdded(Sprite sprite, int index) {
	}

	public void spriteRemoved(Sprite sprite, int index) {
		this.remove(sprite);
	}
	
    public void imageResourceAdded(ImageResource imageResource) {
    }

	//-----Previewable
	public JComponent getPreview() {
		if (this.preview == null) {
            return this.preview = new ScenePreviewPanel(this);
        }
		return this.preview;
	}

	public JComponent getNavigator() {
		if (this.navigator == null) {
            return this.navigator = new SceneLayerNavigator(this);
        }
		return this.navigator;
	}

	public void paint(Graphics2D g, int x, int y) {
	}

    public int getWidth() {
		return 0;
    }

    public int getHeight() {
		return 0;
    }

}
