/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
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
import org.netbeans.modules.vmd.game.view.ColorConstants;
import org.netbeans.modules.vmd.game.view.GameDesignNavigator;
import org.netbeans.modules.vmd.game.view.GameDesignOverViewPanel;
import org.netbeans.modules.vmd.game.view.main.MainView;
import org.openide.util.NbBundle;

public class GlobalRepository implements PropertyChangeListener, Editable {

	private JComponent editor;
	
	private DesignDocument designDocument;
	private MainView mainView;
	
	public static final boolean DEBUG = false;
	
	EventListenerList listenerList = new EventListenerList();
	
	private GameDesignNavigator navigator;
	
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
			this.mainView = new MainView(this);
			this.addGlobalRepositoryListener(this.mainView);
		}
		return this.mainView;
	}
		
	public boolean removeIdentifiable(long id) {
		Collection<Scene> tmpScene = new ArrayList<Scene>();
		tmpScene.addAll(this.scenes);
		for (Scene scene : tmpScene) {
			if (scene.getId() == id) {
				this.removeScene(scene);
				return true;
			}
		}
	    List<Layer> tmpLayer = new ArrayList<Layer>();
	    tmpLayer.addAll(this.layers.values());
	    for (Layer layer : tmpLayer) {
			if (layer.getId() == id) {
				this.removeLayer(layer);
				return true;
			}
	    }
		return false;
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
					Object url = iter.next();
					System.out.println("\t" + url); // NOI18N 
				}
			}//end DEBUG
		}
		return imgResource;
	}
	
	public Collection<ImageResource> getImageResources() {
		return Collections.unmodifiableCollection(this.imgResourceMap.values());
	}
	
	public ImageResource getImageResource(long id) {
		for (ImageResource imgRes : this.getImageResources()) {
			if (imgRes.getId() == id) {
				return imgRes;
			}
		}
		return null;
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
		return this.layers.get(name);
	}

	public Layer getLayer(long id) {
		for (Layer layer : this.layers.values()) {
			if (layer.getId() == id) {
				return layer;
			}
		}
		return null;
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
		
	public boolean isComponentNameAvailable(String fieldName) {
        List<String> derivedNames = deriveUsedNames(fieldName);
        for (String derivedName : derivedNames) {
            if (!this.isNameAvailable(derivedName)) {
                return false;
            }
        }
		return true;
	}
	
    public static List<String> deriveUsedNames(String fieldName) {
        List<String> derivedNames = new ArrayList<String>();
        derivedNames.add(CodeUtils.decapitalize(fieldName));
        derivedNames.add(CodeUtils.capitalize(fieldName));
        derivedNames.add(CodeUtils.createGetterMethodName(fieldName));
        derivedNames.add(CodeUtils.createSetterMethodName(fieldName));
        return derivedNames;
    }
    
	private boolean isNameAvailable(String name) {		
		if (this.getLayerByName(name) != null) {
			return false;
		}
		if (this.getSceneByName(name) != null) {
			return false;
		}
		for (ImageResource imgRes : this.getImageResources()) {
			if (imgRes.getName(false).equals(name)) {
				return false;
			}
			if (imgRes.getSequenceByName(name) != null) {
				return false;
			}
			if (imgRes.getAnimatedTileByName(name) != null) {
				return false;
			}
		}
		return true;
	}
	
	
	public String getNextAvailableComponentName(String name) {
		String next = name;
		int count = 1;
		while (!isComponentNameAvailable(next)) {
			next = name;
			if (count < 100) {
				next += "0";
			}
			if (count < 10) {
				next += "0";
			}
			next += count++;
		}
		return next;
	}
	
	public Scene getScene(long id) {
		for (Iterator iter = this.scenes.iterator(); iter.hasNext();) {
			Scene scene = (Scene) iter.next();
			if (scene.getId() == id) {
				return scene;
			}
		}
		return null;
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
		//we will be able to keep a single editor once the GameDesignOverViewPanel implements listeners for 
		//scene, tiledlayer, and sprite so that it can redraw pewview components when changes occur
//		if (this.editor == null) {
			JPanel top = new JPanel(new BorderLayout());
			top.setBackground(ColorConstants.COLOR_EDITOR_PANEL);
			JScrollPane scroll = new JScrollPane();
			scroll.setViewportView(new GameDesignOverViewPanel(this));
			scroll.getViewport().setBackground(Color.WHITE);
			top.add(scroll, BorderLayout.CENTER);
			this.editor = top;
//		}
		return this.editor;
    }

    public ImageResourceInfo getImageResourceInfo() {
        return null;
    }

    public JComponent getNavigator() {
        return this.navigator == null ? this.navigator = new GameDesignNavigator(this) : this.navigator;
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
