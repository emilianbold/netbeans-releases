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

import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

public class GlobalRepository implements PropertyChangeListener {

	public static final boolean DEBUG = false;
	
	EventListenerList listenerList = new EventListenerList();
	
	private HashMap layers = new HashMap();
	private ArrayList tiledLayers = new ArrayList();
	private ArrayList sprites = new ArrayList();
	
	private ArrayList scenes = new ArrayList();
	
	private static GlobalRepository instance;
	
	
	private GlobalRepository() {
	}
	
	public static GlobalRepository getInstance() {
		return (instance == null) ? instance = new GlobalRepository() : instance;
	}
	
	public void addGlobalRepositoryListener(GlobalRepositoryListener l) {
		this.listenerList.add(GlobalRepositoryListener.class, l);
	}
	public void removeGlobalRepositoryListener(GlobalRepositoryListener l) {
		this.listenerList.remove(GlobalRepositoryListener.class, l);
	}
	
	boolean addTiledLayer(TiledLayer layer) {
		if (!this.addLayer(layer))
			return false;
		this.tiledLayers.add(layer);
		Collections.sort(this.tiledLayers, new Layer.NameComparator());
		int index = this.tiledLayers.indexOf(layer);
		this.fireTiledLayerAdded(layer, index);
		return true;
	}
	
	private void fireTiledLayerAdded(TiledLayer layer, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GlobalRepositoryListener.class) {
				((GlobalRepositoryListener) listeners[i+1]).tiledLayerAdded(layer, index);
			}
		}
	}

	boolean addSprite(Sprite layer) {
		if (!this.addLayer(layer))
			return false;
		this.sprites.add(layer);
		Collections.sort(this.sprites, new Layer.NameComparator());
		int index = this.sprites.indexOf(layer);
		this.fireSpriteAdded(layer, index);
		return true;
	}
	
	private void fireSpriteAdded(Sprite layer, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GlobalRepositoryListener.class) {
				((GlobalRepositoryListener) listeners[i+1]).spriteAdded(layer, index);
			}
		}
	}

	private boolean addLayer(Layer layer) {
		if (this.layers.containsKey(layer.getName()))
			return false;
		this.layers.put(layer.getName(), layer);
		layer.addPropertyChangeListener(this);
		return true;
	}
	
	void removeTiledLayer(TiledLayer layer) {
		this.removeLayerfromLayers(layer);
		int index = this.tiledLayers.indexOf(layer);
		this.tiledLayers.remove(layer);
		this.fireTiledLayerRemoved(layer, index);
	}
	
	private void fireTiledLayerRemoved(TiledLayer layer, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GlobalRepositoryListener.class) {
				((GlobalRepositoryListener) listeners[i+1]).tiledLayerRemoved(layer, index);
			}
		}
	}

	void removeSprite(Sprite layer) {
		this.removeLayerfromLayers(layer);
		int index = this.sprites.indexOf(layer);
		this.sprites.remove(layer);
		this.fireSpriteRemoved(layer, index);
	}
	
	private void fireSpriteRemoved(Sprite layer, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GlobalRepositoryListener.class) {
				((GlobalRepositoryListener) listeners[i+1]).spriteRemoved(layer, index);
			}
		}
	}
	
	void removeLayer(Layer layer) {
		if (layer instanceof TiledLayer) {
			TiledLayer tl = (TiledLayer) layer;
			this.removeTiledLayer(tl);
		}
		else if (layer instanceof Sprite) {
			Sprite sprite = (Sprite) layer;
			this.removeSprite(sprite);
		}
	}
	
	private void removeLayerfromLayers(Layer layer) {
		this.layers.remove(layer.getName());
		layer.removePropertyChangeListener(this);
	}
	
	public Layer getLayerByName(String name) {
		return (Layer) this.layers.get(name);
	}


	boolean addScene(Scene scene) {
		if (this.getSceneByName(scene.getName()) != null)
			return false;
		this.scenes.add(scene);
		Collections.sort(this.scenes, new Scene.NameComparator());
		int index = this.scenes.indexOf(scene);
		this.fireSceneAdded(scene, index);
		return true;
	}
	
	private void fireSceneAdded(Scene scene, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GlobalRepositoryListener.class) {
				((GlobalRepositoryListener) listeners[i+1]).sceneAdded(scene, index);
			}
		}
	}


	void removeScene(Scene scene) {
		int index = this.scenes.indexOf(scene);
		this.scenes.remove(scene);
		this.fireSceneRemoved(scene, index);
	}	
	
	private void fireSceneRemoved(Scene scene, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GlobalRepositoryListener.class) {
				((GlobalRepositoryListener) listeners[i+1]).sceneRemoved(scene, index);
			}
		}
	}
	
	
	public Scene getSceneByName(String name) {
		for (Iterator iter = this.scenes.iterator(); iter.hasNext();) {
			Scene scene = (Scene) iter.next();
			if (scene.getName().equals(name)) {
				return scene;
			}
		}
		return null;
	}
	
	public List <Scene> getScenes() {
		return Collections.unmodifiableList(this.scenes);
	}

	public List <TiledLayer> getTiledLayers() {
		return Collections.unmodifiableList(this.tiledLayers);
	}

	public List <Sprite> getSprites() {
		return Collections.unmodifiableList(this.sprites);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (DEBUG) System.out.println("GlobalRepository propertyChange() event: " + evt);
		if (evt.getSource() instanceof Layer) {
			Layer layer = (Layer) evt.getSource();
			if (evt.getPropertyName().equals(Layer.PROPERTY_NAME)) {
				//layer name has changed re-key it in the layer table
				this.layers.remove(evt.getOldValue());
				this.layers.put(evt.getNewValue(), layer);
			}
		}
	}

	public Scene createScene(String name) {
		Scene scene = new Scene(name);
		if (!this.addScene(scene))
			throw new IllegalArgumentException("Scene " + name + " already exists.");
		return scene;
	}
	
	public Scene createScene(String name, Scene original) {
		Scene scene = new Scene(name, original);
		if (!this.addScene(scene))
			throw new IllegalArgumentException("Scene " + name + " already exists.");
		return scene;
	}
	
	public TiledLayer createTiledLayer(String name, ImageResource imageResource, int rows, int columns) {
		TiledLayer layer = new TiledLayer(name, imageResource, rows, columns);
		if (!this.addTiledLayer(layer)) {
			throw new IllegalArgumentException("ERR: Layer " + name + " already exists!");
		}
		return layer;
	}

	public TiledLayer createTiledLayer(String name, ImageResource imageResource, int[][] grid) {
		TiledLayer layer = new TiledLayer(name, imageResource, grid);
		if (!this.addTiledLayer(layer)) {
			throw new IllegalArgumentException("ERR: Layer " + name + " already exists!");
		}
		return layer;
	}

	public TiledLayer duplicateTiledLayer(String name, TiledLayer original) {
		TiledLayer layer = new TiledLayer(name, original);
		if (!this.addTiledLayer(layer)) {
			throw new IllegalArgumentException("ERR: Layer " + name + " already exists!");
		}
		return layer;
	}
	
	public Sprite createSprite(String name, ImageResource imageResource, int numberFrames) {
		assert (numberFrames >= 1);
		Sprite sprite = new Sprite(name, imageResource, numberFrames);
		if (!this.addSprite(sprite)) {
			throw new IllegalArgumentException("ERR: Layer " + name + " already exists!");
		}
		return sprite;
	}
	
	public String toString() {
		return "[Global Repository]";
	}
	
}
