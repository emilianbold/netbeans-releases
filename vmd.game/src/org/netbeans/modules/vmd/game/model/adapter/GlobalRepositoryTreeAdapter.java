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

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.GlobalRepositoryListener;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Scene.LayerInfo;
import org.netbeans.modules.vmd.game.model.SceneListener;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.TiledLayer;

/**
 * @author Karel Herink
 */
public class GlobalRepositoryTreeAdapter implements TreeModel, GlobalRepositoryListener, SceneListener, PropertyChangeListener {

	private static final boolean DEBUG = false;
	
	private EventListenerList listenerList;
	
	private GlobalRepository globalRepository;
	
	public GlobalRepositoryTreeAdapter(GlobalRepository globalRepository) {
		this.globalRepository = globalRepository;
		this.listenerList = new EventListenerList();
		this.registerListeners();
	}
	
	private void registerListeners() {
		this.globalRepository.addGlobalRepositoryListener(this);
		for (Scene scene : this.globalRepository.getScenes()) {
			scene.addSceneListener(this);
			scene.addPropertyChangeListener(this);
			for (Layer layer : scene.getLayers()) {
				layer.addPropertyChangeListener(this);
			}
		}
	}
	
	//------ TreeModel -----------
	
	public Object getRoot() {
		return this.globalRepository;
	}

	public Object getChild(Object parent, int index) {
		if (parent == this.globalRepository) {
			int numScenes = this.globalRepository.getScenes().size();
			if (index >= numScenes) {
				return null;
			}
			return this.globalRepository.getScenes().get(index);
		}
		if (parent instanceof Scene) {
			return ((Scene) parent).getLayerAt(index);
		}
		return null;
	}

	public int getChildCount(Object parent) {
		if (parent == this.globalRepository) {
			return this.globalRepository.getScenes().size();
		}
		if (parent instanceof Scene) {
			return ((Scene) parent).getLayers().size();
		}
		return 0;
	}

	public boolean isLeaf(Object node) {
		if (node == this.globalRepository || node instanceof Scene) {
			return false;
		}
		return true;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		if (DEBUG) System.out.println("GlobalRepositoryTreeAdapter.valueForPathChanged"); // NOI18N 
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent == this.globalRepository) {
			return this.globalRepository.getScenes().indexOf((Scene) child);
		}
		if (parent instanceof Scene) {
			return ((Scene) parent).indexOf((Layer) child);
		}
		return -1;
	}

	public void addTreeModelListener(TreeModelListener listener) {
		this.listenerList.add(TreeModelListener.class, listener);
	}

	public void removeTreeModelListener(TreeModelListener listener) {
		this.listenerList.remove(TreeModelListener.class, listener);
	}
	
	
	//------ GlobalRepositoryListener -----------
	
	public void sceneAdded(Scene scene, int index) {
		scene.addPropertyChangeListener(this);
		scene.addSceneListener(this);
		TreePath path = new TreePath(this.getRoot());
		this.fireNodeInserted(path, index, scene);
	}

	public void sceneRemoved(Scene scene, int index) {
		scene.removePropertyChangeListener(this);
		scene.removeSceneListener(this);
		TreePath path = new TreePath(this.getRoot());
		this.fireNodeRemoved(path, index, scene);
	}
	
    public void tiledLayerAdded(TiledLayer tiledLayer, int index) {
		tiledLayer.addPropertyChangeListener(this);
    }

    public void tiledLayerRemoved(TiledLayer tiledLayer, int index) {
		tiledLayer.removePropertyChangeListener(this);
    }

    public void spriteAdded(Sprite sprite, int index) {
		sprite.addPropertyChangeListener(this);
    }

    public void spriteRemoved(Sprite sprite, int index) {
		sprite.removePropertyChangeListener(this);
    }

	public void imageResourceAdded(ImageResource imageResource) {
		//ignore
    }


	//----------------------------------------
	
	//EVENTS firing
	
	private void fireNodeInserted(TreePath path, int index, Object object) {
		TreeModelEvent e = new TreeModelEvent(this, path, new int[] {index}, new Object[] {object});
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				((TreeModelListener) listeners[i+1]).treeNodesInserted(e);
			}
		}
	}
	private void fireNodeRemoved(TreePath path, int index, Object object) {		
		TreeModelEvent e = new TreeModelEvent(this, path, new int[] {index}, new Object[] {object});
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				((TreeModelListener) listeners[i+1]).treeNodesRemoved(e);
			}
		}
	}
	private void fireNodeChanged(TreePath path, int index, Object object) {
		TreeModelEvent e = new TreeModelEvent(this, path, new int[] {index}, new Object[] {object});
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				((TreeModelListener) listeners[i+1]).treeNodesChanged(e);
			}
		}
	}

	//--------------- SceneListener -------------------
	
 	public void layerAdded(Scene sourceScene, Layer layer, int index) {
		TreePath path = new TreePath(new Object[] {this.getRoot(), sourceScene});
		this.fireNodeInserted(path, index, layer);
	}
	
	public void layerRemoved(Scene sourceScene, Layer layer, LayerInfo info, int index) {
		TreePath path = new TreePath(new Object[] {this.getRoot(), sourceScene});
		this.fireNodeRemoved(path, index, layer);
	}
	
	public void layerMoved(Scene sourceScene, Layer layer, int indexOld, int indexNew) {
		this.layerRemoved(sourceScene, layer, null, indexOld);
		this.layerAdded(sourceScene, layer, indexNew);
	}
	
   public void layerPositionChanged(Scene sourceScene, Layer layer, Point oldPosition, Point newPosition, boolean inTransition) {
		//ignore
    }

    public void layerLockChanged(Scene sourceScene, Layer layer, boolean locked) {
		//ignore
    }

    public void layerVisibilityChanged(Scene sourceScene, Layer layer, boolean visible) {
		//ignore
    }

	//----------------- PropertyChangeListener -----------------
	
    public void propertyChange(PropertyChangeEvent e) {
		Object src = e.getSource();
		if (e.getPropertyName().equals(Editable.PROPERTY_NAME)) {
			if (src instanceof Scene) {
				Scene scene = (Scene) src;
				TreePath path = new TreePath(this.getRoot());
				this.fireNodeChanged(path, this.globalRepository.getScenes().indexOf(scene), scene);
			}
			else if (src instanceof Layer) {
				Layer layer = (Layer) src;
				for (Scene scene : this.globalRepository.getScenes()) {
					if (scene.getLayers().contains(layer)) {
						TreePath path = new TreePath(new Object[] {this.getRoot(), scene});
						this.fireNodeChanged(path, scene.getLayers().indexOf(layer), layer);
					}
				}
			}
		}
    }
	
}
