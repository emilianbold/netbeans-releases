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

package org.netbeans.modules.vmd.game;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Set;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DesignListener;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.game.integration.components.GameTypes;
import org.netbeans.modules.vmd.game.model.AnimatedTile;
import org.netbeans.modules.vmd.game.model.AnimatedTileCD;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.GlobalRepositoryListener;
import org.netbeans.modules.vmd.game.model.Identifiable;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.ImageResourceCD;
import org.netbeans.modules.vmd.game.model.ImageResourceListener;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.LayerCD;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Scene.LayerInfo;
import org.netbeans.modules.vmd.game.model.SceneCD;
import org.netbeans.modules.vmd.game.model.SceneItemCD;
import org.netbeans.modules.vmd.game.model.SceneListener;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceCD;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.SequenceContainerCDProperties;
import org.netbeans.modules.vmd.game.model.SequenceContainerListener;
import org.netbeans.modules.vmd.game.model.SequenceListener;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.SpriteCD;
import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.TiledLayerCD;
import org.netbeans.modules.vmd.game.model.TiledLayerListener;
import org.netbeans.modules.vmd.game.nbdialog.SelectImageForLayerDialog;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Karel Herink
 */
public class GameController implements DesignDocumentAwareness, GlobalRepositoryListener, 
		SceneListener, TiledLayerListener, SequenceContainerListener,
		SequenceListener, ImageResourceListener, PropertyChangeListener, DesignListener {
	
	private static boolean DEBUG = false;
	private static boolean DEBUG_UNDO = false;
	
	private final Map<Long, String> changeMap = new HashMap<Long, String>();
	
	
	/**
	 * Maps game builder model objects to the component ids of their designer2 design component
	 * counter-parts. E.g. a TiledLayer instance is the key and value is design component id of 
	 * the design component that represents the tiled layer in the design document.
	 */
	private final Map<Identifiable, DesignComponent> designIdMap = new HashMap<Identifiable, DesignComponent>();
	
	public static final String PROJECT_TYPE_GAME = "vmd-midp-game"; // NOI18N
	
	private DataObjectContext context;
    private GameEditorView gameEditorView;
	private JComponent loadingPanel;
	private JPanel panel = new JPanel(new BorderLayout());
	
	private DesignDocument document;
	
	
	/** Creates a new instance of GameController */
	public GameController(DataObjectContext context, GameEditorView gameEditorView) {
		this.context = context;
		this.gameEditorView = gameEditorView;
		this.loadingPanel = IOUtils.createLoadingPanel();
		this.panel.add(this.loadingPanel);
		this.context.addDesignDocumentAwareness(this);
	}
	
	public JComponent getVisualRepresentation() {
		return this.panel;
	}
	
	public DesignDocument getDesignDocument() {
		return document;
	}
	
	public GlobalRepository getGameDesign() {
		final DesignDocument doc = this.getDesignDocument();
		if (doc == null) {
			return null;
		}
		final GlobalRepository[] gameDesign = {null};
		doc.getTransactionManager().readAccess(new Runnable() {
				public void run () {
					GameAccessController controller = doc.getListenerManager().getAccessController(GameAccessController.class);
					gameDesign[0] = controller.getGameDesign();
				}
		});
		return gameDesign[0];
	}
	
	
	private DesignComponent getImageResourceDC(Set<DesignComponent> set) {
		for (DesignComponent designComponent : set) {
			if (designComponent == null) {
				continue;
			}
			if (designComponent.getType().equals(ImageResourceCD.TYPEID)) {
				return designComponent;
			}
		}
		return null;
	}
	
	//handle external changes here (such as undo or redo events)
	public void designChanged(DesignEvent event) {
		if (DEBUG_UNDO) System.out.println("GameController.designChanged() : " + event.getEventID()); // NOI18N
		
		//first model the newly created design components
		for (DesignComponent designComponent : event.getCreatedComponents()) {
			this.modelComponent(designComponent);
		}
		
		DesignComponent imgResDC = null;
		DesignComponent root = this.document.getRootComponent();
		
		//we either added or removed a component from the game design root (scene, layer, image resource)
		if (event.getFullyAffectedHierarchies().contains(root)) {
			this.refreshGameDesign(root, event);
		}
		
		else if ((imgResDC = getImageResourceDC(event.getFullyAffectedHierarchies())) != null) {
			this.refreshImageResource(imgResDC, event);
		}
		
		//or we modified an existing component 
		else {
			//then update exiting design components
			for (DesignComponent designComponent : event.getFullyAffectedComponents()) {
				TypeID typeId = designComponent.getType();
				assert (typeId != null);

				if (DEBUG_UNDO) System.out.println("Affected ComponentID: " + designComponent.getComponentID() + ", type: " + designComponent.getType().getString()); // NOI18N

				if (typeId.equals(SceneCD.TYPEID)) {
					this.refreshScene(designComponent, event);
				}
				else if (typeId.equals(TiledLayerCD.TYPEID)) {
					this.refreshTiledLayer(designComponent, event);
				}
				else if (typeId.equals(SpriteCD.TYPEID)) {
					this.refreshSprite(designComponent, event);
				}
				else if (typeId.equals(SequenceCD.TYPEID)) {
					this.refreshSequence(designComponent, event);
				}
				else if (typeId.equals(AnimatedTileCD.TYPEID)) {
					this.refreshAnimatedTile(designComponent, event);
				}
				else {
					if (DEBUG_UNDO) System.out.println("Currently not handling this in type in GameController.designChanged() typeId: " + typeId.getString()); // NOI18N
				}
			}
		}
	}
	
	
	
	
	private void refreshGameDesign(DesignComponent designComponent, DesignEvent event) {
		
		if (changeMap.remove(designComponent.getComponentID()) != null) {
			return;
		}
		
		this.getGameDesign().removeGlobalRepositoryListener(this);
		this.getGameDesign().removePropertyChangeListener(this);
		
		GlobalRepository globalRepo = this.getGameDesign();
		Collection<DesignComponent> dcChildren = designComponent.getComponents();
		
		Set<Identifiable> children = new HashSet<Identifiable>();
		children.addAll(globalRepo.getImageResources());
		children.addAll(globalRepo.getScenes());
		children.addAll(globalRepo.getSprites());
		children.addAll(globalRepo.getTiledLayers());
		
		//add any components present in designDocument but not present in globalRepo
		for (DesignComponent dc : dcChildren) {
			boolean found = false;
			for (Identifiable child : children) {
				if (dc.getComponentID() == child.getId()) {
					found = true;
					break;
				}
			}
			if (!found) {
				this.modelComponent(dc);
			}
		}		
		
		//remove any components present in globalRepo but not present in designComponent
		for (Identifiable child : children) {
			boolean found = false;
			for (DesignComponent dc : dcChildren) {
				if (child.getId() == dc.getComponentID()) {
					found = true;
					break;
				}
			}
			if (!found) {
				globalRepo.removeIdentifiable(child.getId());
			}
		}

		this.getGameDesign().addGlobalRepositoryListener(this);
		this.getGameDesign().addPropertyChangeListener(this);
		globalRepo.getMainView().requestEditing(globalRepo);
	}
	
	
	private void refreshScene(DesignComponent designComponent, DesignEvent event) {
		Scene scene = this.getGameDesign().getScene(designComponent.getComponentID());
		scene.removePropertyChangeListener(this);
		scene.removeSceneListener(this);
		
		if (event.isComponentPropertyChanged(designComponent, SceneCD.PROPERTY_NAME)) {
			if (SceneCD.PROPERTY_NAME.equals(changeMap.get(designComponent.getComponentID()))) {
				changeMap.remove(designComponent.getComponentID());
			}
			else {
				String name = (String) designComponent.readProperty(SceneCD.PROPERTY_NAME).getPrimitiveValue();
				scene.setName(name);
			}
		}
		else {
			if (SceneCD.PROPERTY_SCENE_ITEMS.equals(changeMap.get(designComponent.getComponentID()))) {
				changeMap.remove(designComponent.getComponentID());
			}
			else {
				List<Layer> newLayers = new ArrayList<Layer>();
				
				List<PropertyValue> sceneItemsProps = designComponent.readProperty(SceneCD.PROPERTY_SCENE_ITEMS).getArray();
				for (PropertyValue sceneItemProp : sceneItemsProps) {
					DesignComponent sceneItemDC = sceneItemProp.getComponent();

					Point layerLocation = (Point) sceneItemDC.readProperty(SceneItemCD.PROPERTY_POSITION).getPrimitiveValue();
					Boolean locked = (Boolean) sceneItemDC.readProperty(SceneItemCD.PROPERTY_LOCK).getPrimitiveValue();
					Boolean visible = (Boolean) sceneItemDC.readProperty(SceneItemCD.PROPERTY_VISIBLE).getPrimitiveValue();
					DesignComponent layerDC = sceneItemDC.readProperty(SceneItemCD.PROPERTY_LAYER).getComponent();
					int zOrder = (Integer) sceneItemDC.readProperty(SceneItemCD.PROPERTY_Z_ORDER).getPrimitiveValue();

					Layer layer = this.getGameDesign().getLayer(layerDC.getComponentID());
					newLayers.add(layer);
							
					if (!scene.contains(layer)) {
						scene.append(layer);
					}
					
					//temporarly unlock the layer so that we can change its position in scene if needed (proper lock is reapplied a few lines down anyway)
					scene.setLayerLocked(layer, false);

					scene.move(layer, zOrder);
					scene.setLayerPosition(layer, layerLocation, false);
					scene.setLayerVisible(layer, visible);
					scene.setLayerLocked(layer, locked);
				}
				
				//now remove all layers that are no longer in the scene
				List<Layer> origLayers = new ArrayList<Layer>(scene.getLayers());
				for (Layer origLayer : origLayers) {
					boolean found = false;
					for (Layer newLayer : newLayers) {
						if (origLayer.getName().equals(newLayer.getName())) {
							found = true;
						}
					}
					if (!found) {
						scene.remove(origLayer);
					}
				}
			}
		}
		
		scene.addPropertyChangeListener(this);
		scene.addSceneListener(this);
		this.getGameDesign().getMainView().requestEditing(scene);
	}
	
	private void refreshLayerProperties(DesignComponent designComponent, DesignEvent event) {
		Layer layer = (Layer) this.getGameDesign().getLayer(designComponent.getComponentID());
		
		if (LayerCD.PROPERTY_NAME.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, LayerCD.PROPERTY_NAME)) {
				String name = (String) designComponent.readProperty(LayerCD.PROPERTY_NAME).getPrimitiveValue();
				layer.setName(name);
			}
		}
		
		if (event.isComponentPropertyChanged(designComponent, LayerCD.PROPERTY_IMAGE_RESOURCE)) {
			//not supported
		}
		if (event.isComponentPropertyChanged(designComponent, LayerCD.PROPERTY_TILE_WIDTH)) {
			//not supported
		}
		if (event.isComponentPropertyChanged(designComponent, LayerCD.PROPERTY_TILE_HEIGHT)) {
			//not supported
		}		
	}
	
	private void refreshTiledLayer(DesignComponent designComponent, DesignEvent event) {
		TiledLayer layer = (TiledLayer) this.getGameDesign().getLayer(designComponent.getComponentID());
		layer.removePropertyChangeListener(this);
		layer.removeTiledLayerListener(this);
		
		this.refreshLayerProperties(designComponent, event);
		if (TiledLayerCD.PROPERTY_TILES.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, TiledLayerCD.PROPERTY_TILES)) {
				int[][]  grid = (int[][]) designComponent.readProperty(TiledLayerCD.PROPERTY_TILES).getPrimitiveValue();		
				layer.setTiles(grid);
			}
		}
		
		layer.addPropertyChangeListener(this);
		layer.addTiledLayerListener(this);
		this.getGameDesign().getMainView().requestEditing(layer);
	}
	
	private void refreshSprite(DesignComponent designComponent, DesignEvent event) {
		Sprite layer = (Sprite) this.getGameDesign().getLayer(designComponent.getComponentID());
		layer.removePropertyChangeListener(this);
		layer.removeSequenceContainerListener(this);
		
		this.refreshLayerProperties(designComponent, event);
		
		DesignComponent imgResDC = designComponent.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.getGameDesign().getImageResource(imgResDC.getComponentID());
		
		if (SequenceContainerCDProperties.PROP_SEQUENCES.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, SequenceContainerCDProperties.PROP_SEQUENCES)) {
				List<PropertyValue> sequenceDCs = designComponent.readProperty(SequenceContainerCDProperties.PROP_SEQUENCES).getArray();
				List<Sequence> oldSequences = layer.getSequences();
				List<Sequence> newSequences = new ArrayList<Sequence>();

				//add sequences not previously contained
				for (PropertyValue propertyValue : sequenceDCs) {
					DesignComponent sequenceDC = propertyValue.getComponent();
					Sequence sequence = (Sequence) this.findIdentifiable(sequenceDC.getComponentID());
					assert (sequence != null);
					newSequences.add(sequence);

					if (!oldSequences.contains(sequence)) {
						layer.append(sequence);
					}
				}

				List<Sequence> iterOldSequences = new ArrayList<Sequence>(layer.getSequences());
				//remove sequences previously contained but no longer present
				for (Sequence sequence : iterOldSequences) {
					if (!newSequences.contains(sequence)) {
						
						if (DEBUG_UNDO) System.out.println("remove seq: " + sequence + " from sprite: " + layer); // NOI18N
						layer.remove(sequence);
					}
				}
			}
		}
		if (SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE)) {
				DesignComponent defaultSequenceDC = designComponent.readProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE).getComponent();
				Sequence defSeq = imgRes.getSequence(defaultSequenceDC.getComponentID());
				layer.setDefaultSequence(defSeq);
			}
		}
		
		
		layer.addPropertyChangeListener(this);
		layer.addSequenceContainerListener(this);
		this.getGameDesign().getMainView().requestEditing(layer);
	}
	
	private void refreshAnimatedTile(DesignComponent designComponent, DesignEvent event) {
		DesignComponent imgResDC = designComponent.readProperty(AnimatedTileCD.PROP_IMAGE_RESOURCE).getComponent();
		//imgResDC will be null only if sequence is being removed from it's container - therefor we can just return 
		if (imgResDC == null) {
			return;
		}
		
		ImageResource imgRes = this.getGameDesign().getImageResource(imgResDC.getComponentID());

		AnimatedTile animTile = (AnimatedTile) imgRes.getAnimatedTile(designComponent.getComponentID());
		
		animTile.removePropertyChangeListener(this);
		animTile.removeSequenceContainerListener(this);
		
		if (AnimatedTileCD.PROPERTY_NAME.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, AnimatedTileCD.PROPERTY_NAME)) {
				String name = (String) designComponent.readProperty(AnimatedTileCD.PROPERTY_NAME).getPrimitiveValue();
				animTile.setName(name);
			}
		}
		
		
		if (SequenceContainerCDProperties.PROP_SEQUENCES.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, SequenceContainerCDProperties.PROP_SEQUENCES)) {
				List<PropertyValue> sequenceDCs = designComponent.readProperty(SequenceContainerCDProperties.PROP_SEQUENCES).getArray();
				List<Sequence> sequences = animTile.getSequences();
				List<Sequence> newSequences = new ArrayList<Sequence>();

				//add sequences not previously contained
				for (PropertyValue propertyValue : sequenceDCs) {
					DesignComponent sequenceDC = propertyValue.getComponent();
					Sequence sequence = imgRes.getSequence(sequenceDC.getComponentID());
					newSequences.add(sequence);

					if (!sequences.contains(sequence)) {
						animTile.append(sequence);
					}
				}

				List<Sequence> iterOldSequences = new ArrayList<Sequence>(animTile.getSequences());
				//remove sequences previously contained but no longer present
				for (Sequence sequence : iterOldSequences) {
					if (!newSequences.contains(sequence)) {
						animTile.remove(sequence);
					}
				}
			}
		}
		
		if (SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE)) {
				DesignComponent defaultSequenceDC = designComponent.readProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE).getComponent();
				Sequence defSeq = imgRes.getSequence(defaultSequenceDC.getComponentID());
				animTile.setDefaultSequence(defSeq);
			}
		}
		
		
		animTile.addPropertyChangeListener(this);
		animTile.addSequenceContainerListener(this);
		this.getGameDesign().getMainView().requestEditing(animTile);
	}

	private Identifiable findIdentifiable(long id) {
		for (Iterator<Identifiable> it = this.designIdMap.keySet().iterator(); it.hasNext();) {
			Identifiable identifiable = it.next();
			if (identifiable.getId() == id) {
				return identifiable;
			}
		}
		return null;
	}
	
	private void refreshSequence(DesignComponent designComponent, DesignEvent event) {
		DesignComponent imgResDC = designComponent.readProperty(SequenceCD.PROPERTY_IMAGE_RESOURCE).getComponent();
		//imgResDC will be null only if sequence is being removed from it's container - therefor we can just return 
		if (imgResDC == null) {
			return;
		}
		
		Sequence sequence = (Sequence) this.findIdentifiable(designComponent.getComponentID());
		sequence.removePropertyChangeListener(this);
		sequence.removeSequenceListener(this);
		
		if (SequenceCD.PROPERTY_NAME.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, SequenceCD.PROPERTY_NAME)) {
				String name = (String) designComponent.readProperty(SequenceCD.PROPERTY_NAME).getPrimitiveValue();
				sequence.setName(name);
			}
		}
		if (SequenceCD.PROPERTY_FRAME_MS.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, SequenceCD.PROPERTY_FRAME_MS)) {
				int frameMs = (Integer) designComponent.readProperty(SequenceCD.PROPERTY_FRAME_MS).getPrimitiveValue();
				sequence.setFrameMs(frameMs);
			}
		}
		
		if (SequenceCD.PROPERTY_FRAMES.equals(changeMap.get(designComponent.getComponentID()))) {
			changeMap.remove(designComponent.getComponentID());
		}
		else {
			if (event.isComponentPropertyChanged(designComponent, SequenceCD.PROPERTY_FRAMES)) {
				int[]  frames = (int[]) designComponent.readProperty(SequenceCD.PROPERTY_FRAMES).getPrimitiveValue();
				sequence.setFrames(frames);
			}
		}
		
		if (event.isComponentPropertyChanged(designComponent, SequenceCD.PROPERTY_FRAME_WIDTH)) {
			//not supported
		}
		else if (event.isComponentPropertyChanged(designComponent, SequenceCD.PROPERTY_FRAME_HEIGHT)) {
			//not supported
		}
		else if (event.isComponentPropertyChanged(designComponent, SequenceCD.PROPERTY_ZERO_BASED_INDEX)) {
			//not supported
		}
		
		sequence.addPropertyChangeListener(this);
		sequence.addSequenceListener(this);
		//this.getGameDesign().getMainView().requestEditing(sequence.);
	}
	
	private void refreshImageResource(DesignComponent designComponent, DesignEvent event) {
		if (changeMap.remove(designComponent.getComponentID()) != null) {
			return;
		}		
		ImageResource imgRes = this.getGameDesign().getImageResource(designComponent.getComponentID());
		imgRes.removeImageResourceListener(this);
		
		Collection<DesignComponent> dcChildren = designComponent.getComponents();
		Set<AnimatedTile> children = new HashSet<AnimatedTile>(imgRes.getAnimatedTiles());
		
		//add any components present in designDocument but not present in globalRepo
		for (DesignComponent dc : dcChildren) {
			boolean found = false;
			for (Identifiable child : children) {
				if (dc.getComponentID() == child.getId()) {
					found = true;
					break;
				}
			}
			if (!found) {
				this.modelComponent(dc);
			}
		}		
		
		//remove any components present in globalRepo but not present in designComponent
		for (AnimatedTile child : children) {
			boolean found = false;
			for (DesignComponent dc : dcChildren) {
				if (child.getId() == dc.getComponentID()) {
					found = true;
					break;
				}
			}
			if (!found) {
				imgRes.removeAnimatedTile(child.getIndex());
			}
		}

		imgRes.addImageResourceListener(this);
		
		this.getGameDesign().getMainView().requestEditing(this.getGameDesign());
	}
	
	
	
	public void setDesignDocument(final DesignDocument designDocument) {
		if (DEBUG) System.out.println(">>>> Set design document to: " + designDocument); // NOI18N

		if (designDocument == this.document) {
			return;
		}
        
        this.panel.removeAll();
		
		GlobalRepository oldGameDesign = this.getGameDesign();
		
		//if we already have a document then de-register listeners and clean game model
		if (this.document != null) {
			this.document.getListenerManager().removeDesignListener(this);
			this.removeListeners(designIdMap.keySet().toArray());
			oldGameDesign.removeGlobalRepositoryListener(this);
			oldGameDesign.removeAllComponents();
			oldGameDesign.getMainView().removeEditorManagerListener(gameEditorView);
			designIdMap.clear();
		}
		
		JComponent view = null;
		
		this.document = designDocument;
		final GlobalRepository gameDesign = this.getGameDesign();
		
		this.gameEditorView.setGameDesign(gameDesign);
		
		if (designDocument == null) {
			view = this.loadingPanel;
		}
		else {
			designDocument.getTransactionManager().readAccess(new Runnable() {
				public void run() {
					if (true) {
						//add all components in the document
						DesignComponent root = designDocument.getRootComponent();
						GameController.this.modelComponent(root);
						GameController.this.registerListeners(designIdMap.keySet().toArray());
						
						DesignEventFilter f = new DesignEventFilter();
						f.setGlobal(true);
						f.addComponentFilter(root, true);
						designDocument.getListenerManager().addDesignListener(GameController.this, f);
					}
				}
			});
			
			//get user to fix invalid image resources - i.e. those with null URLs
                        if (!validateImages(gameDesign)){
			//gameDesign.getMainView().closeEditor(gameDesign);
                            context.getCloneableEditorSupport().close();
                            return;
                        }
			gameDesign.getMainView().addEditorManagerListener(gameEditorView);
			view = gameDesign.getMainView().getRootComponent();

			gameDesign.addGlobalRepositoryListener(GameController.this);
			gameDesign.getMainView().requestEditing(gameDesign);
		}
		this.panel.add(view);
		this.panel.validate();
	}
	
    private boolean validateImages(GlobalRepository gameDesign) {
        boolean imagesFixed = true;
	Map<ImageResource, String> updatedResources = new HashMap<ImageResource, String>();
        Collection<ImageResource> imageResources = gameDesign.getImageResources();
        for (ImageResource imageResource : imageResources) {
            if (imageResource.getURL() == null) {
                DesignComponent imageResourceDC = designIdMap.get(imageResource);
                if (! this.validateImageResource(imageResource, imageResourceDC, updatedResources)){
                    imagesFixed = false;
                    break;
                }
            }
        }
        if (imagesFixed){
            fixUpdatedImagesInDocument(updatedResources);
        }
        return imagesFixed;
    }
    
    private void fixUpdatedImagesInDocument(final Map<ImageResource, String> updatedResources) {
        document.getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                for (ImageResource imageResource : updatedResources.keySet()) {
                    DesignComponent imageResourceDC = designIdMap.get(imageResource);
                    String newPath = updatedResources.get(imageResource);
                    
                    changeMap.put(imageResourceDC.getComponentID(), ImageResourceCD.PROPERTY_IMAGE_PATH);
                    imageResourceDC.writeProperty(ImageResourceCD.PROPERTY_IMAGE_PATH,
                            MidpTypes.createStringValue(newPath));
                }
            }
        });
    }
        
	private void removeListeners(Object... objectsOfInterest) {
		for (Object o : objectsOfInterest) {
			if (DEBUG_UNDO) System.out.println("removeListeners on: " + o); // NOI18N
			if (o instanceof Scene) {
				Scene s = (Scene) o;
				s.removeSceneListener(this);
				s.removePropertyChangeListener(this);
			}
			else if (o instanceof TiledLayer) {
				TiledLayer tl = (TiledLayer) o;
				tl.removeTiledLayerListener(this);
				tl.removePropertyChangeListener(this);
			}
			else if (o instanceof Sprite) {
				Sprite s = (Sprite) o;
				s.removeSequenceContainerListener(this);
				s.removePropertyChangeListener(this);
			}
			else if (o instanceof Sequence) {
				Sequence s = (Sequence) o;
				s.removeSequenceListener(this);
				s.removePropertyChangeListener(this);
			}
			else if (o instanceof AnimatedTile) {
				AnimatedTile a = (AnimatedTile) o;
				a.removeSequenceContainerListener(this);
				a.removePropertyChangeListener(this);
			}
			else if (o instanceof ImageResource) {
				ImageResource i = (ImageResource) o;
				i.removeImageResourceListener(this);
			}
		}

	}
	
	private void registerListeners(Object... objectsOfInterest) {
		for (Object o : objectsOfInterest) {
			if (DEBUG_UNDO) System.out.println("registerListeners on: " + o); // NOI18N
			if (o instanceof Scene) {
				Scene s = (Scene) o;
				s.addSceneListener(this);
				s.addPropertyChangeListener(this);
			}
			else if (o instanceof TiledLayer) {
				TiledLayer tl = (TiledLayer) o;
				tl.addTiledLayerListener(this);
				tl.addPropertyChangeListener(this);
			}
			else if (o instanceof Sprite) {
				Sprite s = (Sprite) o;
				s.addSequenceContainerListener(this);
				s.addPropertyChangeListener(this);
			}
			else if (o instanceof Sequence) {
				Sequence s = (Sequence) o;
				s.addSequenceListener(this);
				s.addPropertyChangeListener(this);
			}
			else if (o instanceof AnimatedTile) {
				AnimatedTile a = (AnimatedTile) o;
				a.addSequenceContainerListener(this);
				a.addPropertyChangeListener(this);
			}
			else if (o instanceof ImageResource) {
				ImageResource i = (ImageResource) o;
				i.addImageResourceListener(this);
			}
		}
	}
	
	
	private void modelComponent(DesignComponent designComponent) {
		//this is sometimes null when i close and open the same project :(
		if (designComponent == null) {
			return;
		}
		
		Collection<DesignComponent> children = designComponent.getComponents();
		for (DesignComponent child : children) {
			this.modelComponent(child);
		}

		TypeID typeId = designComponent.getType();
		assert (typeId != null);
		
		if (typeId.equals(SceneCD.TYPEID)) {
			Scene scene = this.constructScene(designComponent);
			designIdMap.put(scene, designComponent);
		}
		else if (typeId.equals(TiledLayerCD.TYPEID)) {
			TiledLayer layer = this.constructTiledLayer(designComponent);
			designIdMap.put(layer, designComponent);
		}
		else if (typeId.equals(SpriteCD.TYPEID)) {
			Sprite sprite = this.constructSprite(designComponent);
			designIdMap.put(sprite, designComponent);
		}
		else if (typeId.equals(SequenceCD.TYPEID)) {
			Sequence sequence = this.constructSequence(designComponent);
			designIdMap.put(sequence, designComponent);
		}
		else if (typeId.equals(ImageResourceCD.TYPEID)) {
			ImageResource imageResource = this.constructImageResource(designComponent);
			designIdMap.put(imageResource, designComponent);
		}
		else if (typeId.equals(AnimatedTileCD.TYPEID)) {
			AnimatedTile animatedTile = this.constructAnimatedTile(designComponent);
			designIdMap.put(animatedTile, designComponent);
		}
	}
	
	//These methods create game model from design components
	
	private AnimatedTile constructAnimatedTile(DesignComponent animatedTileDC) {
		int index = (Integer) animatedTileDC.readProperty(AnimatedTileCD.PROPERTY_INDEX).getPrimitiveValue();
		String name = (String) animatedTileDC.readProperty(AnimatedTileCD.PROPERTY_NAME).getPrimitiveValue();
		DesignComponent imgResDC = animatedTileDC.readProperty(AnimatedTileCD.PROP_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.constructImageResource(imgResDC);
		
		//if ImageResource already has an animated tile of that name it must have been already constructed
		AnimatedTile animatedTile = imgRes.getAnimatedTileByName(name);
		if (animatedTile != null) {
			return animatedTile;
		}
		
		DesignComponent defaultSequenceDC = animatedTileDC.readProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE).getComponent();
		List<PropertyValue> sequenceDCs = animatedTileDC.readProperty(SequenceContainerCDProperties.PROP_SEQUENCES).getArray();

		Sequence defaultSequence = this.constructSequence(defaultSequenceDC);		
		animatedTile = imgRes.createAnimatedTile(index, name, defaultSequence);
		animatedTile.setId(animatedTileDC.getComponentID());
		
		for (PropertyValue propertyValue : sequenceDCs) {
			DesignComponent sequenceDC = propertyValue.getComponent();
			Sequence sequence = this.constructSequence(sequenceDC);
			//defaultSequence is already appended
			if (sequence != defaultSequence) {
				animatedTile.append(sequence);
			}
		}
		
		Identifiable old = this.findIdentifiable(animatedTileDC.getComponentID());
		if (old != null) {
			if (DEBUG_UNDO) System.out.println("> Recreated " + animatedTile + " remove old mapping and create a new one"); // NOI18N
			this.designIdMap.remove(old);
			this.removeListeners(old);
			this.designIdMap.put(animatedTile, animatedTileDC);
			this.registerListeners(animatedTile);
		}
		
		return animatedTile;
	}
	
	private Sprite constructSprite(DesignComponent spriteDC) {
		String name = (String) spriteDC.readProperty(LayerCD.PROPERTY_NAME).getPrimitiveValue();
		//if GlobalRepository already has a layer of that name it must have been already constructed
		Sprite sprite = (Sprite) this.getGameDesign().getLayerByName(name);
		if (sprite != null) {
			return sprite;
		}
		
		DesignComponent imgResDC = spriteDC.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.constructImageResource(imgResDC);

		DesignComponent defaultSequenceDC = spriteDC.readProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE).getComponent();
		List<PropertyValue> sequenceDCs = spriteDC.readProperty(SequenceContainerCDProperties.PROP_SEQUENCES).getArray();

		Sequence defaultSequence = this.constructSequence(defaultSequenceDC);
		sprite = this.getGameDesign().createSprite(name, imgRes, defaultSequence);
		sprite.setId(spriteDC.getComponentID());
		
		for (PropertyValue propertyValue : sequenceDCs) {
			DesignComponent sequenceDC = propertyValue.getComponent();
			Sequence sequence = this.constructSequence(sequenceDC);
			//defaultSequence is already appended
			if (sequence != defaultSequence) {
				sprite.append(sequence);
			}
		}
		
		Identifiable old = this.findIdentifiable(spriteDC.getComponentID());
		if (old != null) {
			if (DEBUG_UNDO) System.out.println("> Recreated " + sprite + " remove old mapping and create a new one"); // NOI18N
			this.designIdMap.remove(old);
			this.removeListeners(old);
			this.designIdMap.put(sprite, spriteDC);
			this.registerListeners(sprite);
		}
		
		return sprite;
	}
	
	private TiledLayer constructTiledLayer(DesignComponent tiledLayerDC) {
		String name = (String) tiledLayerDC.readProperty(LayerCD.PROPERTY_NAME).getPrimitiveValue();
		//if GlobalRepository already has a layer of that name it must have been already constructed
		TiledLayer tiledLayer = (TiledLayer) this.getGameDesign().getLayerByName(name);
		if (tiledLayer != null) {
			return tiledLayer;
		}
		
		DesignComponent imgResDC = tiledLayerDC.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.constructImageResource(imgResDC);
		int[][]  gridOriginal = (int[][]) tiledLayerDC.readProperty(TiledLayerCD.PROPERTY_TILES).getPrimitiveValue();
		int tileWidth = MidpTypes.getInteger(tiledLayerDC.readProperty(LayerCD.PROPERTY_TILE_WIDTH));
		int tileHeight = MidpTypes.getInteger(tiledLayerDC.readProperty(LayerCD.PROPERTY_TILE_HEIGHT));
		
		int[][] grid = TiledLayer.cloneTiles(gridOriginal);
		tiledLayer = this.getGameDesign().createTiledLayer(name, imgRes, grid, tileWidth, tileHeight);
		tiledLayer.setId(tiledLayerDC.getComponentID());
		
		Identifiable old = this.findIdentifiable(tiledLayerDC.getComponentID());
		if (old != null) {
			if (DEBUG_UNDO) System.out.println("> Recreated " + tiledLayer + " remove old mapping and create a new one"); // NOI18N
			this.designIdMap.remove(old);
			this.removeListeners(old);
			this.designIdMap.put(tiledLayer, tiledLayerDC);
			this.registerListeners(tiledLayer);
		}
		
		return tiledLayer;
	}
	
	private Sequence constructSequence(DesignComponent sequenceDC) {
		String name = (String) sequenceDC.readProperty(SequenceCD.PROPERTY_NAME).getPrimitiveValue();
		DesignComponent imgResDC = sequenceDC.readProperty(SequenceCD.PROPERTY_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.constructImageResource(imgResDC);
				
		//if GlobalRepository already has a sequence of that name it must have been already constructed
		Sequence sequence = imgRes.getSequenceByName(name);
		if (sequence != null) {
			return sequence;
		}
		
		int frameMs = (Integer) sequenceDC.readProperty(SequenceCD.PROPERTY_FRAME_MS).getPrimitiveValue();
		int[]  frames = (int[]) sequenceDC.readProperty(SequenceCD.PROPERTY_FRAMES).getPrimitiveValue();
		int frameWidth = (Integer) sequenceDC.readProperty(SequenceCD.PROPERTY_FRAME_WIDTH).getPrimitiveValue();
		int frameHeight = (Integer) sequenceDC.readProperty(SequenceCD.PROPERTY_FRAME_HEIGHT).getPrimitiveValue();
		boolean zeroBasedIndex = (Boolean) sequenceDC.readProperty(SequenceCD.PROPERTY_ZERO_BASED_INDEX).getPrimitiveValue();
		
		sequence = imgRes.createSequence(name, frames.length, frameWidth, frameHeight, zeroBasedIndex);
		sequence.setId(sequenceDC.getComponentID());
		sequence.setFrameMs(frameMs);

		for (int i = 0; i < frames.length; i++) {
			sequence.setFrame((StaticTile) imgRes.getTile(frames[i], frameWidth, frameHeight, zeroBasedIndex), i);
		}
				
		Identifiable old = this.findIdentifiable(sequenceDC.getComponentID());
		if (old != null) {
			if (DEBUG_UNDO) System.out.println("> Recreated " + sequence + " remove old mapping and create a new one"); // NOI18N
			this.designIdMap.remove(old);
			this.removeListeners(old);
			this.designIdMap.put(sequence, sequenceDC);
			this.registerListeners(sequence);
		}
		
		return sequence;
	}
	
	private ImageResource constructImageResource(final DesignComponent imageResourceDC) {
		URL imgResUrl = null;
		
		final String imgResPath = (String) imageResourceDC.readProperty(ImageResourceCD.PROPERTY_IMAGE_PATH).getPrimitiveValue();
		final String imgResName = (String) imageResourceDC.readProperty(ImageResourceCD.PROPERTY_NAME).getPrimitiveValue();
		ImageResource imgRes = this.getGameDesign().getImageResource(imgResPath);
		if (imgRes != null) {
			return imgRes;
		}

		Map<FileObject, FileObject> imagesMap = MidpProjectSupport.getFileObjectsForRelativeResourcePath(document, imgResPath);
		
		//if there is a single image matching this image resource path then excellent - all is as it should be and we have
		//no image name conflicts
		if (imagesMap.size() == 1) {
			FileObject fo = imagesMap.keySet().iterator().next();
			try {
				imgResUrl = fo.getURL();
			} catch (FileStateInvalidException e) {
				e.printStackTrace();
			}
		}
		
		//if at this point imgResUrl is null then we need to resolve image name conflicts or missing
		//images after the design document is loaded by running method validateImageResource() on
		//all imageResources that have null imageURL
		ImageResource imageResource = this.getGameDesign().getImageResource(imgResUrl, imgResPath);
		imageResource.setName(imgResName);
		imageResource.setId(imageResourceDC.getComponentID());
		return imageResource;
	}
		
        /**
         * validates and get user to fix invalid image resources - i.e. those with null URLs
         * @param imageResource
         * @param imageResourceDC
         * @param updatedUrls
         * @return was image fixed or not
         */
	private boolean validateImageResource(ImageResource imageResource, 
                final DesignComponent imageResourceDC,
                Map<ImageResource, String> updatedResources) 
        {
                boolean imageFixed = false;
		FileObject fo;
		URL imgResUrl;
		final String imgResPath = imageResource.getRelativeResourcePath();
		
		Map<FileObject, FileObject> imagesMap = MidpProjectSupport.getFileObjectsForRelativeResourcePath(document, imgResPath);
		if (imagesMap.size() > 1) {
			//multiple locations (e.g. jars) contain the same image - show a dialog to let the
			//user choose which single image should be used
			if (DEBUG) {
				System.out.println("found multiple images matching the relative path:"); // NOI18N
				for (Map.Entry<FileObject, FileObject> entry : imagesMap.entrySet()) {
					System.out.println("root: " + entry.getValue() + ", path: " + entry.getKey()); // NOI18N
				}
			}
			final SelectImageForLayerDialog dialog = new SelectImageForLayerDialog(
					NbBundle.getMessage(GameController.class, "GameController.SelectImageDialog.description1_txt", imgResPath),
					//"Multiple images found matching the relative path: " + imgResPath,
					imagesMap.keySet()
			);
			
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(GameController.class, "GameController.SelectImageDialog.title"));
			dd.setValid(false);
			dd.setButtonListener(dialog);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
                        
                        fo = dialog.getValue();
                            
                        //user did not choose one of the available images
			if (dialog.isCancelled() || fo == null){
                            return imageFixed;
                        }
                            
                        final String newPath = "/" + FileUtil.getRelativePath(imagesMap.get(fo), fo);
                        if (DEBUG) System.out.println("Setting new path: " + newPath); // NOI18N
                        updatedResources.put(imageResource, newPath);
                        if (DEBUG_UNDO) System.out.println("Set new path: " + newPath); // NOI18N
		}
		else if (imagesMap.isEmpty()) {
			//image is no longer on the classpath - prompt the user to select a replacement image
			fo = null;
			if (DEBUG) System.out.println("Image " + imgResPath + " doesn't exist, select a replacement."); // NOI18N
			
			Map<FileObject, String> images = MidpProjectSupport.getImagesForProject(document, false);
			
			final SelectImageForLayerDialog dialog = new SelectImageForLayerDialog(
					NbBundle.getMessage(GameController.class, "GameController.SelectImageDialog.description2_txt", imgResPath),
					//"Image " + imgResPath + " doesn't exist, select a replacement.",
					images.keySet()
			);
			
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(GameController.class, "GameController.SelectImageDialog.title"));
			dd.setValid(false);
			dd.setButtonListener(dialog);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
			
			fo = dialog.getValue();
        		//missing image could not be recovered by the user
			if (dialog.isCancelled() || fo == null){
                            return imageFixed;
                        }
                        
                        final String newPath = images.get(fo);
			if (DEBUG) System.out.println("Setting new path: " + newPath); // NOI18N
                        updatedResources.put(imageResource, newPath);
			if (DEBUG_UNDO) System.out.println("Set new path: " + newPath); // NOI18N
		}
		else {
			//there is a single matching image on the classpath - excellent :)
			fo = imagesMap.keySet().iterator().next();
			if (DEBUG) System.out.println("Found single matching image ULR: " + fo.getPath()); // NOI18N
		}
		
            try {
                if (fo != null) {
                    imgResUrl = fo.getURL();
                    imageResource.setURL(imgResUrl);
                    imageFixed = true;
                }
            } catch (FileStateInvalidException e) {
                throw new RuntimeException(e);
            }
            return imageFixed;
	}
        
	private Scene constructScene(DesignComponent sceneDC) {
		String name = (String) sceneDC.readProperty(SceneCD.PROPERTY_NAME).getPrimitiveValue();
		//if GlobalRepository already has a scene of that name it must have been already constructed
		Scene scene = this.getGameDesign().getSceneByName(name);
		if (scene != null) {
			return scene;
		}
		
		scene = this.getGameDesign().createScene(name);
		scene.setId(sceneDC.getComponentID());
		
		List<PropertyValue> sceneItemsProps = sceneDC.readProperty(SceneCD.PROPERTY_SCENE_ITEMS).getArray();
		for (PropertyValue sceneItemProp : sceneItemsProps) {
			DesignComponent sceneItemDC = sceneItemProp.getComponent();
			
			Point layerLocation = (Point) sceneItemDC.readProperty(SceneItemCD.PROPERTY_POSITION).getPrimitiveValue();
			Boolean locked = (Boolean) sceneItemDC.readProperty(SceneItemCD.PROPERTY_LOCK).getPrimitiveValue();
			Boolean visible = (Boolean) sceneItemDC.readProperty(SceneItemCD.PROPERTY_VISIBLE).getPrimitiveValue();
			DesignComponent layerDC = sceneItemDC.readProperty(SceneItemCD.PROPERTY_LAYER).getComponent();
			int zOrder = (Integer) sceneItemDC.readProperty(SceneItemCD.PROPERTY_Z_ORDER).getPrimitiveValue();
					
			Layer layer = null;
			if (layerDC.getType().equals(TiledLayerCD.TYPEID)) {
				layer = this.constructTiledLayer(layerDC);
			}
			else {
				layer = this.constructSprite(layerDC);
			}
			
			scene.append(layer);
			
			scene.move(layer, zOrder);
			scene.setLayerPosition(layer, layerLocation, false);
			scene.setLayerVisible(layer, visible);
			scene.setLayerLocked(layer, locked);
		}
		
		Identifiable old = this.findIdentifiable(sceneDC.getComponentID());
		if (old != null) {
			if (DEBUG_UNDO) System.out.println("> Recreated " + scene + " remove old mapping and create a new one"); // NOI18N
			this.designIdMap.remove(old);
			this.removeListeners(old);
			this.designIdMap.put(scene, sceneDC);
			this.registerListeners(scene);
		}
		
		return scene;
	}
	
	
	
	
	//These methods create design components from game identifiable model comnponents and update their ids
	
	
	
	
	public static DesignComponent createSceneDCFromScene(DesignDocument doc, Map<Identifiable, DesignComponent> designIdMap, Scene scene) {
		DesignComponent dcScene = designIdMap.get(scene);
		if (dcScene != null) {
			return dcScene;
		}
		dcScene = doc.createComponent(SceneCD.TYPEID);
		scene.setId(dcScene.getComponentID());
		
		dcScene.writeProperty(SceneCD.PROPERTY_NAME, MidpTypes.createStringValue(scene.getName()));
		writeSceneItemsToSceneDC(doc, designIdMap, dcScene, scene);
		return dcScene;
	}
	
	private static void writeSceneItemsToSceneDC(DesignDocument doc, Map<Identifiable, DesignComponent> designIdMap, DesignComponent dcScene, Scene scene) {
		//fist remove all scene items
		List<PropertyValue> sceneItemProps = dcScene.readProperty(SceneCD.PROPERTY_SCENE_ITEMS).getArray();
		for (Iterator<PropertyValue> it = sceneItemProps.iterator(); it.hasNext();) {
			PropertyValue propertyValue = it.next();
			DesignComponent dcSceneItemComp = propertyValue.getComponent();
			dcSceneItemComp.removeFromParentComponent();
		}
		dcScene.writeProperty(SceneCD.PROPERTY_SCENE_ITEMS, PropertyValue.createEmptyArray(SceneItemCD.TYPEID));

		//them add scene items according to the current state of the scene
		List<Layer> layers = scene.getLayers();
		List<PropertyValue> scenePropValues = new ArrayList<PropertyValue>();
		
		for (Iterator<Layer> it = layers.iterator(); it.hasNext();) {
			DesignComponent sceneItemDC = doc.createComponent(SceneItemCD.TYPEID);

			Layer layer = it.next();
			DesignComponent layerDC = designIdMap.get(layer);
			assert (layerDC != null);

			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_LAYER, PropertyValue.createComponentReference(layerDC));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_LOCK, MidpTypes.createBooleanValue(scene.isLayerLocked(layer)));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_VISIBLE, MidpTypes.createBooleanValue(scene.isLayerVisible(layer)));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_POSITION, GameTypes.createPointProperty(scene.getLayerPosition(layer)));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_Z_ORDER, MidpTypes.createIntegerValue(scene.indexOf(layer)));			
			
			dcScene.addComponent(sceneItemDC);
			
			PropertyValue sceneItemPropVal = PropertyValue.createComponentReference(sceneItemDC);
			scenePropValues.add(sceneItemPropVal);
		}
		dcScene.writeProperty(SceneCD.PROPERTY_SCENE_ITEMS, PropertyValue.createArray(SceneItemCD.TYPEID, scenePropValues));
	}
	
	public static DesignComponent createTiledLayerDCFromTiledLayer(DesignDocument doc, Map<Identifiable, DesignComponent> designIdMap, TiledLayer layer) {
		DesignComponent dcLayer = designIdMap.get(layer);
		if (dcLayer != null) {
			return dcLayer;
		}
		dcLayer = doc.createComponent(TiledLayerCD.TYPEID);
		layer.setId(dcLayer.getComponentID());
		
		dcLayer.writeProperty(LayerCD.PROPERTY_NAME, MidpTypes.createStringValue(layer.getName()));
		
		DesignComponent dcImgRes = designIdMap.get(layer.getImageResource());
		assert(dcImgRes != null);
		
		dcLayer.writeProperty(LayerCD.PROPERTY_IMAGE_RESOURCE, PropertyValue.createComponentReference(dcImgRes));
		PropertyValue propTiles = GameTypes.createTilesProperty(layer.getTiles());
		dcLayer.writeProperty(TiledLayerCD.PROPERTY_TILES, propTiles);
		dcLayer.writeProperty(LayerCD.PROPERTY_TILE_WIDTH, MidpTypes.createIntegerValue(layer.getTileWidth()));
		dcLayer.writeProperty(LayerCD.PROPERTY_TILE_HEIGHT, MidpTypes.createIntegerValue(layer.getTileHeight()));
		
		return dcLayer;
	}
		
	public DesignComponent createAnimatedTileDCFromAnimatedTile(AnimatedTile tile) {
		DesignComponent dcAt = designIdMap.get(tile);
		if (dcAt != null) {
			return dcAt;
		}
		dcAt = document.createComponent(AnimatedTileCD.TYPEID);
		tile.setId(dcAt.getComponentID());
		
		dcAt.writeProperty(AnimatedTileCD.PROPERTY_NAME, MidpTypes.createStringValue(tile.getName()));
		dcAt.writeProperty(AnimatedTileCD.PROPERTY_INDEX, MidpTypes.createIntegerValue(tile.getIndex()));
		dcAt.writeProperty(AnimatedTileCD.PROPERTY_WIDTH, MidpTypes.createIntegerValue(tile.getWidth()));
		dcAt.writeProperty(AnimatedTileCD.PROPERTY_HEIGHT, MidpTypes.createIntegerValue(tile.getHeight()));
		
		DesignComponent dcImgRes = designIdMap.get(tile.getImageResource());
		assert(dcImgRes != null);
		dcAt.writeProperty(AnimatedTileCD.PROP_IMAGE_RESOURCE, PropertyValue.createComponentReference(dcImgRes));
		
		List<PropertyValue> sequencePropValues = new ArrayList<PropertyValue>();
		for (Sequence seq : tile.getSequences()) {
			DesignComponent dcSeq = designIdMap.get(seq);					
			if (dcSeq == null) {
				dcSeq = this.createSequenceDCFromSequence(seq);
				designIdMap.put(seq, dcSeq);
			}
			if (!dcAt.getComponents().contains(dcSeq)) {
				dcAt.addComponent(dcSeq);
			}
			if (seq == tile.getDefaultSequence()) {
				dcAt.writeProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE, PropertyValue.createComponentReference(dcSeq));
			}
			PropertyValue seqPropertyValue = PropertyValue.createComponentReference(dcSeq);
			sequencePropValues.add(seqPropertyValue);
		}
		dcAt.writeProperty(SequenceContainerCDProperties.PROP_SEQUENCES, PropertyValue.createArray(SequenceCD.TYPEID, sequencePropValues));
		
		return dcAt;
	}
	
	public DesignComponent createSpriteDCFromSprite(Sprite layer) {
		DesignComponent dcLayer = designIdMap.get(layer);
		if (dcLayer != null) {
			return dcLayer;
		}
		dcLayer = document.createComponent(SpriteCD.TYPEID);
		layer.setId(dcLayer.getComponentID());
		
		dcLayer.writeProperty(LayerCD.PROPERTY_NAME, MidpTypes.createStringValue(layer.getName()));
		dcLayer.writeProperty(LayerCD.PROPERTY_TILE_WIDTH, MidpTypes.createIntegerValue(layer.getTileWidth()));
		dcLayer.writeProperty(LayerCD.PROPERTY_TILE_HEIGHT, MidpTypes.createIntegerValue(layer.getTileHeight()));
		
		DesignComponent dcImgRes = designIdMap.get(layer.getImageResource());
		assert(dcImgRes != null);
		dcLayer.writeProperty(LayerCD.PROPERTY_IMAGE_RESOURCE, PropertyValue.createComponentReference(dcImgRes));
		
		List<PropertyValue> sequencePropValues = new ArrayList<PropertyValue>();
		for (Sequence seq : layer.getSequences()) {
			DesignComponent dcSeq = designIdMap.get(seq);					
			if (dcSeq == null) {
				dcSeq = this.createSequenceDCFromSequence(seq);
				designIdMap.put(seq, dcSeq);
			}
			if (!dcLayer.getComponents().contains(dcSeq)) {
				dcLayer.addComponent(dcSeq);
			}
			if (seq == layer.getDefaultSequence()) {
				dcLayer.writeProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE, PropertyValue.createComponentReference(dcSeq));
			}
			PropertyValue seqPropertyValue = PropertyValue.createComponentReference(dcSeq);
			sequencePropValues.add(seqPropertyValue);
		}
		dcLayer.writeProperty(SequenceContainerCDProperties.PROP_SEQUENCES, PropertyValue.createArray(SequenceCD.TYPEID, sequencePropValues));
		
		return  dcLayer;
	}
	
	public DesignComponent createImageResourceDCFromImageResource(ImageResource imageResource) {
		DesignComponent dcImgRes = designIdMap.get(imageResource);
		if (dcImgRes != null) {
			return dcImgRes;
		}
		dcImgRes = document.createComponent(ImageResourceCD.TYPEID);
		imageResource.setId(dcImgRes.getComponentID());
		
		dcImgRes.writeProperty(ImageResourceCD.PROPERTY_IMAGE_PATH, MidpTypes.createStringValue(imageResource.getRelativeResourcePath()));
		dcImgRes.writeProperty(ImageResourceCD.PROPERTY_NAME, MidpTypes.createStringValue(imageResource.getName(true)));
		this.writeAnimatedTilesToImageResourceDC(dcImgRes, imageResource);
		return dcImgRes;
	}
	
	private void writeAnimatedTilesToImageResourceDC(DesignComponent dcImgRes, ImageResource imageResource) {
		for (AnimatedTile at : imageResource.getAnimatedTiles()) {
			DesignComponent dcAnimTile = designIdMap.get(at);
			if (dcAnimTile == null) {
				assert (!dcImgRes.getComponents().contains(dcAnimTile));
				dcAnimTile = this.createAnimatedTileDCFromAnimatedTile(at);
				dcImgRes.addComponent(dcAnimTile);
				designIdMap.put(at, dcAnimTile);
			}
		}
	}

	
	public DesignComponent createSequenceDCFromSequence(Sequence sequence) {
		DesignComponent dcSequence = designIdMap.get(sequence);
		if (dcSequence != null) {
			return dcSequence;
		}
		dcSequence = document.createComponent(SequenceCD.TYPEID);
		sequence.setId(dcSequence.getComponentID());
		
		dcSequence.writeProperty(SequenceCD.PROPERTY_NAME, MidpTypes.createStringValue(sequence.getName()));
		
		DesignComponent dcImg = designIdMap.get(sequence.getImageResource());
		assert(dcImg != null);
		dcSequence.writeProperty(SequenceCD.PROPERTY_IMAGE_RESOURCE, PropertyValue.createComponentReference(dcImg));
		dcSequence.writeProperty(SequenceCD.PROPERTY_FRAMES, GameTypes.createFramesProperty(sequence.getFramesAsArray()));
		dcSequence.writeProperty(SequenceCD.PROPERTY_FRAME_MS, MidpTypes.createIntegerValue(sequence.getFrameMs()));
		dcSequence.writeProperty(SequenceCD.PROPERTY_FRAME_WIDTH, MidpTypes.createIntegerValue(sequence.getFrameWidth()));
		dcSequence.writeProperty(SequenceCD.PROPERTY_FRAME_HEIGHT, MidpTypes.createIntegerValue(sequence.getFrameHeight()));
		dcSequence.writeProperty(SequenceCD.PROPERTY_ZERO_BASED_INDEX, MidpTypes.createBooleanValue(sequence.isZeroBasedIndex()));
		
		this.registerListeners(sequence);
		return dcSequence;
	}
	
	
	
	//--------------- GlobalRepositoryListener ------------------
	
    public void sceneAdded(final Scene scene, int index) {
		final DesignDocument doc = document;
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignComponent sceneDC = createSceneDCFromScene(doc, designIdMap, scene);
				designIdMap.put(scene, sceneDC);
				changeMap.put(doc.getRootComponent().getComponentID(), scene.getName());
				scene.addSceneListener(GameController.this);
				scene.addPropertyChangeListener(GameController.this);
				doc.getRootComponent().addComponent(sceneDC);
            }
		});
		if (DEBUG_UNDO) System.out.println("Scene added " + scene); // NOI18N
    }

    public void sceneRemoved(final Scene scene, int index) {
		final DesignDocument doc = document;
		scene.removeSceneListener(this);
		final DesignComponent dcScene = designIdMap.get(scene);
		assert (dcScene != null);
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				changeMap.put(doc.getRootComponent().getComponentID(), scene.getName());
				document.deleteComponent(dcScene);
            }
		});
		if (DEBUG_UNDO) System.out.println("Scene removed: " + scene); // NOI18N
    }

    public void tiledLayerAdded(final TiledLayer tiledLayer, int index) {
		final DesignDocument doc = document;
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignDocument doc = document;
				DesignComponent tiledLayerDC = createTiledLayerDCFromTiledLayer(doc, designIdMap, tiledLayer);
				changeMap.put(doc.getRootComponent().getComponentID(), tiledLayer.getName());
				tiledLayer.addTiledLayerListener(GameController.this);
				tiledLayer.addPropertyChangeListener(GameController.this);
				doc.getRootComponent().addComponent(tiledLayerDC);
            }
		});
		if (DEBUG_UNDO) System.out.println("TL added: " + tiledLayer); // NOI18N
    }

    public void tiledLayerRemoved(final TiledLayer tiledLayer, int index) {
		final DesignDocument doc = document;
		tiledLayer.removeTiledLayerListener(this);
		final DesignComponent dcLayer = designIdMap.get(tiledLayer);
		assert (dcLayer != null);
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				changeMap.put(doc.getRootComponent().getComponentID(), tiledLayer.getName());
				document.deleteComponent(dcLayer);
            }			
		});
		if (DEBUG_UNDO) System.out.println("TL removed: " + tiledLayer); // NOI18N
    }

    public void spriteAdded(final Sprite sprite, int index) {
		final DesignDocument doc = document;
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignComponent spriteDC = GameController.this.createSpriteDCFromSprite(sprite);
				designIdMap.put(sprite, spriteDC);
				changeMap.put(doc.getRootComponent().getComponentID(), sprite.getName());
				sprite.addSequenceContainerListener(GameController.this);
				sprite.addPropertyChangeListener(GameController.this);
				document.getRootComponent().addComponent(spriteDC);
            }
		});
		if (DEBUG_UNDO) System.out.println("Sprite added: " + sprite); // NOI18N
    }

    public void spriteRemoved(final Sprite sprite, int index) {
		final DesignDocument doc = document;
		sprite.removeSequenceContainerListener(this);
		final DesignComponent dcLayer = designIdMap.get(sprite);
		assert (dcLayer != null);
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				changeMap.put(doc.getRootComponent().getComponentID(), sprite.getName());
				document.deleteComponent(dcLayer);
            }
		});
		if (DEBUG_UNDO) System.out.println("Sprite removed: " + sprite); // NOI18N
    }

    public void imageResourceAdded(final ImageResource imageResource) {
		final DesignDocument doc = document;
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignDocument doc = document;
				DesignComponent imgResDC = createImageResourceDCFromImageResource(imageResource);
				designIdMap.put(imageResource, imgResDC);
				changeMap.put(doc.getRootComponent().getComponentID(), imageResource.getName());
				imageResource.addImageResourceListener(GameController.this);
				doc.getRootComponent().addComponent(imgResDC);
            }
		});
		if (DEBUG_UNDO) System.out.println("ImgRes added: " + imageResource); // NOI18N
    }

	//----------------- SceneListener -------------------
	
    public void layerAdded(final Scene sourceScene, final Layer layer, int index) {
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				changeMap.put(dcScene.getComponentID(), SceneCD.PROPERTY_SCENE_ITEMS);
				writeSceneItemsToSceneDC(document, designIdMap, dcScene, sourceScene);
            }
		});
		if (DEBUG_UNDO) System.out.println("Layer added: " + layer); // NOI18N
    }

    public void layerRemoved(final Scene sourceScene, final Layer layer, final LayerInfo info, int index) {
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				changeMap.put(dcScene.getComponentID(), SceneCD.PROPERTY_SCENE_ITEMS);
				writeSceneItemsToSceneDC(document, designIdMap, dcScene, sourceScene);
            }
		});
		if (DEBUG_UNDO) System.out.println("Layer removed: " + layer); // NOI18N
    }

    public void layerMoved(final Scene sourceScene, final Layer layer, int indexOld, int indexNew) {
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				changeMap.put(dcScene.getComponentID(), SceneCD.PROPERTY_SCENE_ITEMS);
				writeSceneItemsToSceneDC(document, designIdMap, dcScene, sourceScene);
            }
		});
		if (DEBUG_UNDO) System.out.println("Layer moved: " + layer); // NOI18N
    }

    public void layerPositionChanged(final Scene sourceScene, final Layer layer, final Point oldPosition, final Point newPosition, boolean inTransition) {
		if (inTransition) {
			return;
		}
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				changeMap.put(dcScene.getComponentID(), SceneCD.PROPERTY_SCENE_ITEMS);
				writeSceneItemsToSceneDC(document, designIdMap, dcScene, sourceScene);
            }
		});
		if (DEBUG_UNDO) System.out.println("Layer position changed: " + layer); // NOI18N
    }

    public void layerLockChanged(final Scene sourceScene, final Layer layer, boolean locked) {
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				changeMap.put(dcScene.getComponentID(), SceneCD.PROPERTY_SCENE_ITEMS);
				writeSceneItemsToSceneDC(document, designIdMap, dcScene, sourceScene);
            }
		});
		if (DEBUG_UNDO) System.out.println("Layer lock changed: " + layer); // NOI18N
    }

    public void layerVisibilityChanged(final Scene sourceScene, final Layer layer, boolean visible) {
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				changeMap.put(dcScene.getComponentID(), SceneCD.PROPERTY_SCENE_ITEMS);
				writeSceneItemsToSceneDC(document, designIdMap, dcScene, sourceScene);
            }
		});
		if (DEBUG_UNDO) System.out.println("Layer visibility changed: " + layer); // NOI18N
    }

	
	//----------------- TiledLayerListener ---------------------
	
    public void tileChanged(TiledLayer source, int row, int col) {
		this.updateTiledLayerDCProps(source);
    }

    public void tilesChanged(TiledLayer source, Set positions) {
		this.updateTiledLayerDCProps(source);
    }

    public void tilesStructureChanged(TiledLayer source) {
		this.updateTiledLayerDCProps(source);
    }

    public void columnsInserted(TiledLayer source, int index, int count) {
		this.updateTiledLayerDCProps(source);
    }

    public void columnsRemoved(TiledLayer source, int index, int count) {
		this.updateTiledLayerDCProps(source);
    }

    public void rowsInserted(TiledLayer source, int index, int count) {
		this.updateTiledLayerDCProps(source);
    }

    public void rowsRemoved(TiledLayer source, int index, int count) {
		this.updateTiledLayerDCProps(source);
    }

	private void updateTiledLayerDCProps(final TiledLayer layer) {
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				changeMap.put(dcLayer.getComponentID(), TiledLayerCD.PROPERTY_TILES);
				PropertyValue propTiles = GameTypes.createTilesProperty(layer.getTiles());
				dcLayer.writeProperty(TiledLayerCD.PROPERTY_TILES, propTiles);
			}
		});
		if (DEBUG_UNDO) System.out.println("Layer props changed: " + layer); // NOI18N
	}
	
	//------------------- SequenceContainerListener ------------------
	
    public void sequenceAdded(SequenceContainer source, Sequence sequence, int index) {
		this.sequenceContainerChanged(source);
    }

    public void sequenceRemoved(SequenceContainer source, Sequence sequence, int index) {
		this.sequenceContainerChanged(source);
    }

    public void sequenceMoved(SequenceContainer source, Sequence sequence, int indexOld, int indexNew) {
		this.sequenceContainerChanged(source);
    }
	
	private void sequenceContainerChanged(final SequenceContainer sequenceContainer) {
		DesignComponent dcSequenceContainer = designIdMap.get(sequenceContainer);
		assert(dcSequenceContainer != null);
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				writeSequencesToSequenceContainerDC(sequenceContainer);
			}
		});
		if (DEBUG_UNDO) System.out.println("Seq container changed: " + sequenceContainer); // NOI18N
	}

	private void writeSequencesToSequenceContainerDC(SequenceContainer sequenceContainer) {
		DesignComponent dcSequenceContainer = designIdMap.get(sequenceContainer);
		assert(dcSequenceContainer != null);

		List<PropertyValue> sequenceDCs = new ArrayList<PropertyValue>();
		List<Sequence> sequences = sequenceContainer.getSequences();

		List<DesignComponent> iter = new ArrayList<DesignComponent>(dcSequenceContainer.getComponents());
		//remove sequences not present in sequence container but present in design document
		for (DesignComponent dcSequence : iter) {
			boolean found = false;
			for (Sequence sequence : sequences) {
				if (designIdMap.get(sequence) == dcSequence) {
					found = true;
					break;
				}
			}
			if (!found) {
				dcSequenceContainer.removeComponent(dcSequence);
			}
		}

		//add sequences present in sequence container and not present in design document
		for (Sequence sequence : sequences) {
			DesignComponent dcSequence = designIdMap.get(sequence);
			if (dcSequence == null) {
				dcSequence = createSequenceDCFromSequence(sequence);
				designIdMap.put(sequence, dcSequence);
			}
			if (!dcSequenceContainer.getComponents().contains(dcSequence)) {
				dcSequenceContainer.addComponent(dcSequence);				
			}
			PropertyValue seqPropVal = PropertyValue.createComponentReference(dcSequence);
			sequenceDCs.add(seqPropVal);
		}


		changeMap.put(dcSequenceContainer.getComponentID(), SequenceContainerCDProperties.PROP_SEQUENCES);
		dcSequenceContainer.writeProperty(SequenceContainerCDProperties.PROP_SEQUENCES, PropertyValue.createArray(SequenceCD.TYPEID, sequenceDCs));
		
	}
	
	//----------------------- SequenceListener ----------------------------
	
    public void frameAdded(Sequence sequence, int index) {
		this.updateSequenceFrames(sequence);
    }

    public void frameRemoved(Sequence sequence, int index) {
		this.updateSequenceFrames(sequence);
    }

    public void frameModified(Sequence sequence, int index) {
		this.updateSequenceFrames(sequence);
    }
	
	public void framesChanged(Sequence sequence) {
		this.updateSequenceFrames(sequence);
	}

	private void updateSequenceFrames(final Sequence sequence) {
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignComponent dcSequence = designIdMap.get(sequence);
				assert(dcSequence != null);
				changeMap.put(dcSequence.getComponentID(), SequenceCD.PROPERTY_FRAMES);
				int[] frames = sequence.getFramesAsArray();
				dcSequence.writeProperty(SequenceCD.PROPERTY_FRAMES, GameTypes.createFramesProperty(frames));
            }
		});
		if (DEBUG_UNDO) System.out.println("Seq frames changed: " + sequence); // NOI18N
	}
	
	
	//----------------------- ImageResourceListener ---------------------------
	
    public void animatedTileAdded(final ImageResource imgRes, final AnimatedTile tile) {
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				if (DEBUG) System.out.println("animatedTileAdded: " + tile); // NOI18N
				DesignComponent dcImgRes = designIdMap.get(imgRes);
				assert (dcImgRes != null);
				
				//update the image resource holding the animated tile
				GameController.this.writeAnimatedTilesToImageResourceDC(dcImgRes, imgRes);
				
				DesignComponent dcAnimTile = designIdMap.get(tile);
				assert (dcAnimTile != null);
				changeMap.put(dcImgRes.getComponentID(), ImageResourceCD.PROPERTY_NAME);
				
				tile.addSequenceContainerListener(GameController.this);
				tile.addPropertyChangeListener(GameController.this);
			}
		});
		if (DEBUG_UNDO) System.out.println("Anim tile added: " + tile); // NOI18N
    }
	
    public void animatedTileRemoved(final ImageResource imgRes, final AnimatedTile tile) {
		document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
								
				if (DEBUG) System.out.println("animatedTileRemoved: " + tile); // NOI18N
				DesignComponent dcAnimTile = designIdMap.get(tile);
				assert (dcAnimTile != null);
				
				DesignComponent dcImgRes = designIdMap.get(imgRes);
				assert (dcImgRes != null);
				
				dcImgRes.removeComponent(dcAnimTile);
				
				List<DesignComponent> tmp = new ArrayList<DesignComponent>(dcAnimTile.getComponents());
				for (DesignComponent child : tmp) {
					changeMap.put(child.getComponentID(), null);
					dcAnimTile.removeComponent(child);
					document.deleteComponent(child);
				}
				document.deleteComponent(dcAnimTile);
				
				//update the image resource holding the animated tile
				changeMap.put(dcImgRes.getComponentID(), ImageResourceCD.PROPERTY_NAME);
			}
		});
		if (DEBUG_UNDO) System.out.println("Anim tile removed: " + tile); // NOI18N
		this.gameEditorView.discardAllEdits();
    }

    public void sequenceAdded(final ImageResource source, final Sequence sequence) {
		//do nothing here - let sequence container listener methods create the sequences !!!
		if (DEBUG_UNDO) System.out.println("IGNORE ImageResourceListener Seq added: " + sequence); // NOI18N
    }

    public void sequenceRemoved(final ImageResource source, final Sequence sequence) {
		//do nothing here - let sequence container listener methods remove the sequences !!!
		if (DEBUG_UNDO) System.out.println("IGNORE ImageResourceListener Seq removed: " + sequence); // NOI18N
    }

	
	//----------------------- PropertyChangeListener --------------------------
	
    public void propertyChange(PropertyChangeEvent e) {
		//System.out.println("PropertyChangeEvent source: " + e.getSource() + ", prop: " + e.getPropertyName() + ", new: " + e.getNewValue() + ", old: " + e.getOldValue());
		DesignComponent dc = designIdMap.get(e.getSource());
		assert (dc != null);
		
		if (dc.getType() == SceneCD.TYPEID) {
			this.handleScenePropChange(dc, e);
		}
		else if (dc.getType() == AnimatedTileCD.TYPEID) {
			this.handleAnimatedTilePropChange(dc, e);
		}
		else if (dc.getType() == ImageResourceCD.TYPEID) {
			this.handleImageResourcePropChange(dc, e);
		}
		else if (dc.getType() == SequenceCD.TYPEID) {
			this.handleSequencePropChange(dc, e);
		}
		else if (dc.getType() == SpriteCD.TYPEID) {
			this.handleSpritePropChange(dc, e);
		}
		else if (dc.getType() == TiledLayerCD.TYPEID) {
			this.handleTiledLayerPropChange(dc, e);
		}
    }
	
	private void handleScenePropChange(final DesignComponent dcScene, final PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Editable.PROPERTY_NAME)) {
			document.getTransactionManager().writeAccess(new Runnable() {
				public void run() {
					String newName = (String) e.getNewValue();
					dcScene.writeProperty(SceneCD.PROPERTY_NAME, MidpTypes.createStringValue(newName));
					changeMap.put(dcScene.getComponentID(), SceneCD.PROPERTY_NAME);
				}
			});
			if (DEBUG_UNDO) System.out.println("Scene name changed: " + dcScene); // NOI18N
		}
	}
	
	private void handleAnimatedTilePropChange(final DesignComponent dcAnimatedTile, final PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Editable.PROPERTY_NAME)) {
			document.getTransactionManager().writeAccess(new Runnable() {
				public void run() {
					String newName = (String) e.getNewValue();
					dcAnimatedTile.writeProperty(AnimatedTileCD.PROPERTY_NAME, MidpTypes.createStringValue(newName));
					changeMap.put(dcAnimatedTile.getComponentID(), AnimatedTileCD.PROPERTY_NAME);
				}
			});
			if (DEBUG_UNDO) System.out.println("AnimatedTile name changed: " + dcAnimatedTile); // NOI18N
		}
		if (e.getPropertyName().equals(SequenceContainer.PROPERTY_DEFAULT_SEQUENCE)) {
			document.getTransactionManager().writeAccess(new Runnable() {
				public void run() {
					Sequence newDefSeq = (Sequence) e.getNewValue();
					DesignComponent dcDefSeq = designIdMap.get(newDefSeq);
					dcAnimatedTile.writeProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE, PropertyValue.createComponentReference(dcDefSeq));
					changeMap.put(dcAnimatedTile.getComponentID(), SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE);
				}
			});
			if (DEBUG_UNDO) System.out.println("AnimatedTile def seq changed: " + dcAnimatedTile); // NOI18N
		}
	}
	
	private void handleImageResourcePropChange(final DesignComponent dcImageResource, final PropertyChangeEvent e) {
		
	}
	
	private void handleSequencePropChange(final DesignComponent dcSequence, final PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Sequence.PROPERTY_NAME)) {
			document.getTransactionManager().writeAccess(new Runnable() {
				public void run() {
					String newName = (String) e.getNewValue();
					dcSequence.writeProperty(SequenceCD.PROPERTY_NAME, MidpTypes.createStringValue(newName));
					changeMap.put(dcSequence.getComponentID(), SequenceCD.PROPERTY_NAME);
				}
			});
			if (DEBUG_UNDO) System.out.println("Sequence name changed: " + dcSequence); // NOI18N
		}
		else if (e.getPropertyName().equals(Sequence.PROPERTY_FRAME_MS)) {
			document.getTransactionManager().writeAccess(new Runnable() {
				public void run() {
					int ms = (Integer) e.getNewValue();
					dcSequence.writeProperty(SequenceCD.PROPERTY_FRAME_MS, MidpTypes.createIntegerValue(ms));
					changeMap.put(dcSequence.getComponentID(), SequenceCD.PROPERTY_FRAME_MS);
				}
			});
			if (DEBUG_UNDO) System.out.println("Sequence frame ms changed: " + dcSequence); // NOI18N
		}
	}
	
	private void handleSpritePropChange(final DesignComponent dcSprite, final PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Editable.PROPERTY_NAME)) {
			document.getTransactionManager().writeAccess(new Runnable() {
				public void run() {
					String newName = (String) e.getNewValue();
					dcSprite.writeProperty(LayerCD.PROPERTY_NAME, MidpTypes.createStringValue(newName));
					changeMap.put(dcSprite.getComponentID(), LayerCD.PROPERTY_NAME);
				}
			});
			if (DEBUG_UNDO) System.out.println("Sprite name changed: " + dcSprite); // NOI18N
		}
		else if (e.getPropertyName().equals(SequenceContainer.PROPERTY_DEFAULT_SEQUENCE)) {
			document.getTransactionManager().writeAccess(new Runnable() {
				public void run() {
					Sequence newDefSeq = (Sequence) e.getNewValue();
					DesignComponent dcDefSeq = designIdMap.get(newDefSeq);
					dcSprite.writeProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE, PropertyValue.createComponentReference(dcDefSeq));
					changeMap.put(dcSprite.getComponentID(), SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE);
				}
			});
			if (DEBUG_UNDO) System.out.println("Sprite def seq changed: " + dcSprite); // NOI18N
		}
	}
	
	private void handleTiledLayerPropChange(final DesignComponent dcTiledLayer, final PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Editable.PROPERTY_NAME)) {
			document.getTransactionManager().writeAccess(new Runnable() {
				public void run() {
					String newName = (String) e.getNewValue();
					dcTiledLayer.writeProperty(LayerCD.PROPERTY_NAME, MidpTypes.createStringValue(newName));
					changeMap.put(dcTiledLayer.getComponentID(), LayerCD.PROPERTY_NAME);
				}
			});
			if (DEBUG_UNDO) System.out.println("TiledLayer name changed: " + dcTiledLayer); // NOI18N
		}
	}

}