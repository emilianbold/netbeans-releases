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

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.game.view.GameDesignOverViewPanel;
import org.netbeans.modules.vmd.game.view.main.MainView;
import org.openide.util.NbBundle;

public class GlobalRepository implements PropertyChangeListener, Editable {

	private DesignDocument designDocument;
	private MainView mainView;
	
	public static final boolean DEBUG = false;
	
	EventListenerList listenerList = new EventListenerList();
	
	private HashMap<String, Layer> layers = new HashMap<String, Layer>();
	private ArrayList<TiledLayer> tiledLayers = new ArrayList<TiledLayer>();
	private ArrayList<Sprite> sprites = new ArrayList<Sprite>();
	private ArrayList<Scene> scenes = new ArrayList<Scene>();	
	private Map<String, ImageResource> imgResourceMap = new HashMap<String, ImageResource>();
	
	
	public GlobalRepository(DesignDocument designDocument) {
		this.designDocument = designDocument;
	}
	
	public DesignDocument getDesignDocument() {
		return this.designDocument;
	}
	
	public void addGlobalRepositoryListener(GlobalRepositoryListener l) {
		this.listenerList.add(GlobalRepositoryListener.class, l);
	}
	public void removeGlobalRepositoryListener(GlobalRepositoryListener l) {
		this.listenerList.remove(GlobalRepositoryListener.class, l);
	}
	
	public MainView getMainView() {
		if (this.mainView == null) {
			this.mainView = new MainView();
			this.addGlobalRepositoryListener(this.mainView);
		}
		return this.mainView;
	}
		
	/**
	 * Removes all layers, scenes
	 */
	public void removeAllComponents() {
		this.removeAllScenes();
		this.removeAllLayers();
		this.imgResourceMap.clear();
	}
	
	private void removeAllScenes() {
		Collection<Scene> tmp = new ArrayList<Scene>();
		tmp.addAll(this.scenes);
		for (Scene scene : tmp) {
			this.removeScene(scene);
		}
	}
	
	private void removeAllLayers() {
	    List<Layer> tmp = new ArrayList<Layer>();
	    tmp.addAll(this.layers.values());
	    for (Layer layer : tmp) {
			this.removeLayer(layer);
	    }
	}
	
	/**
	 * May return null if the image resource hasn't yet been created.
	 */
	public ImageResource getImageResource(String relativeResourcePath) {
		return imgResourceMap.get(relativeResourcePath);
	}
	
	/**
	 * Returns ImageResource instance.
	 */
	public ImageResource getImageResource(URL imageURL, String relativeResourcePath) {
		ImageResource imgResource = imgResourceMap.get(relativeResourcePath);
		if (imgResource == null) {
			imgResource = new ImageResource(this, imageURL, relativeResourcePath);
			imgResourceMap.put(relativeResourcePath, imgResource);
			this.fireImageResourceAdded(imgResource);
			
			if (DEBUG) {
				System.out.println("Added " + imageURL + ". ImgResourceMap now contains:"); // NOI18N 
				for (Iterator iter = imgResourceMap.keySet().iterator(); iter.hasNext();) {
					URL url = (URL) iter.next();
					System.out.println("\t" + url); // NOI18N 
				}
			}//end DEBUG
		}
		return imgResource;
	}
	
	public Collection<ImageResource> getImageResources() {
		return Collections.unmodifiableCollection(this.imgResourceMap.values());
	}
	
	private void fireImageResourceAdded(ImageResource imgRes) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GlobalRepositoryListener.class) {
				((GlobalRepositoryListener) listeners[i+1]).imageResourceAdded(imgRes);
			}
		}
	}
	
	private void addTiledLayer(TiledLayer layer) {
		this.tiledLayers.add(layer);
		this.layers.put(layer.getName(), layer);
		layer.addPropertyChangeListener(this);
		Collections.sort(this.tiledLayers, new Layer.NameComparator());
		int index = this.tiledLayers.indexOf(layer);
		this.fireTiledLayerAdded(layer, index);
	}
	
	private void fireTiledLayerAdded(TiledLayer layer, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GlobalRepositoryListener.class) {
				((GlobalRepositoryListener) listeners[i+1]).tiledLayerAdded(layer, index);
			}
		}
	}

	private void addSprite(Sprite layer) {
		this.sprites.add(layer);
		this.layers.put(layer.getName(), layer);
		layer.addPropertyChangeListener(this);
		Collections.sort(this.sprites, new Layer.NameComparator());
		int index = this.sprites.indexOf(layer);
		this.fireSpriteAdded(layer, index);
	}
	
	private void fireSpriteAdded(Sprite layer, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GlobalRepositoryListener.class) {
				((GlobalRepositoryListener) listeners[i+1]).spriteAdded(layer, index);
			}
		}
	}

	private void removeTiledLayer(TiledLayer layer) {
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

	private void removeSprite(Sprite layer) {
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
		List<Scene> sceneList = this.getScenes();
		for (Scene scene : sceneList) {
			scene.remove(layer);
		}
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


	private void addScene(Scene scene) {
		this.scenes.add(scene);
		Collections.sort(this.scenes, new Scene.NameComparator());
		int index = this.scenes.indexOf(scene);
		this.fireSceneAdded(scene, index);
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
	
	public boolean isComponentNameAvailable(String name) {
		if (this.getLayerByName(name) != null)
			return false;
		if (this.getSceneByName(name) != null) 
			return false;
		for (ImageResource imgRes : this.getImageResources()) {
			if (imgRes.getSequenceByName(name) != null)
				return false;
			if (imgRes.getAnimatedTileByName(name) != null)
				return false;
		}
		return true;
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
		if (DEBUG) System.out.println("GlobalRepository propertyChange() event: " + evt); // NOI18N 
		if (evt.getSource() instanceof Layer) {
			Layer layer = (Layer) evt.getSource();
			if (evt.getPropertyName().equals(Editable.PROPERTY_NAME)) {
				//layer name has changed re-key it in the layer table
				this.layers.remove(evt.getOldValue());
				this.layers.put((String) evt.getNewValue(), layer);
			}
		}
	}

	public Scene createScene(String name) {
		if (!this.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Scene cannot be created because component name '" + name + "' already exists."); // NOI18N 
		}
		Scene scene = new Scene(this, name);
		this.addScene(scene);
		return scene;
	}
	
	public Scene createScene(String name, Scene original) {
		if (!this.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Scene cannot be created because component name '" + name + "' already exists."); // NOI18N 
		}
		Scene scene = new Scene(this, name, original);
		this.addScene(scene);
		return scene;
	}
	
	public TiledLayer createTiledLayer(String name, ImageResource imageResource, int rows, int columns, int tileWidth, int tileHeight) {
		if (!this.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Scene cannot be created because component name '" + name + "' already exists."); // NOI18N 
		}
		TiledLayer layer = new TiledLayer(this, name, imageResource, rows, columns, tileWidth, tileHeight);
		this.addTiledLayer(layer);
		return layer;
	}

	public TiledLayer createTiledLayer(String name, ImageResource imageResource, int[][] grid, int tileWidth, int tileHeight) {
		if (!this.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("TiledLayer cannot be created because component name '" + name + "' already exists."); // NOI18N 
		}		
		TiledLayer layer = new TiledLayer(this, name, imageResource, grid, tileWidth, tileHeight);
		this.addTiledLayer(layer);
		return layer;
	}

	public TiledLayer duplicateTiledLayer(String name, TiledLayer original) {
		if (!this.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("TiledLayer cannot be created because component name '" + name + "' already exists."); // NOI18N 
		}
		TiledLayer layer = new TiledLayer(this, name, original);
		this.addTiledLayer(layer);
		return layer;
	}
	
	public Sprite createSprite(String name, ImageResource imageResource, int numberFrames, int frameWidth, int frameHeight) {
		assert (numberFrames >= 1);
		if (!this.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Sprite cannot be created because component name '" + name + "' already exists."); // NOI18N 
		}
		Sprite sprite = new Sprite(this, name, imageResource, numberFrames, frameWidth, frameHeight);
		this.addSprite(sprite);
		return sprite;
	}
	public Sprite createSprite(String name, ImageResource imageResource, Sequence defaultSequence) {
		assert (defaultSequence != null);
		if (!this.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Sprite cannot be created because component name '" + name + "' already exists."); // NOI18N 
		}
		Sprite sprite = new Sprite(this, name, imageResource, defaultSequence);
		this.addSprite(sprite);
		return sprite;
	}
	
	public String toString() {
		return this.getName();
	}

    public JComponent getEditor() {
		JPanel top = new JPanel(new BorderLayout());
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(new GameDesignOverViewPanel(this));
		scroll.getViewport().setBackground(Color.WHITE);
		top.add(scroll, BorderLayout.CENTER);
		return top;
    }

    public ImageResourceInfo getImageResourceInfo() {
        return null;
    }

    public JComponent getNavigator() {
        //return new GameDesignNavigator(this);
		return null;
    }

    public String getName() {
        return NbBundle.getMessage(GlobalRepository.class, "GlobalRepository.name");
    }

    public List<Action> getActions() {
        return Collections.EMPTY_LIST;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
	
}
