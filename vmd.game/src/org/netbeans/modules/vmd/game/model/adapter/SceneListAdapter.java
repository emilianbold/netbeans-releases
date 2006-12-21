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
package org.netbeans.modules.vmd.game.model.adapter;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.SceneListener;
import org.netbeans.modules.vmd.game.model.Scene.LayerInfo;

public class SceneListAdapter implements ListModel, SceneListener {

	private Scene scene;
	private ArrayList listeners = new ArrayList();
	
	public SceneListAdapter(Scene layerModel) {
		this.scene = layerModel;
		this.scene.addSceneListener(this);
	}
	
	public int getSize() {
		return this.scene.getLayerCount();
	}

	public Object getElementAt(int index) {
		return this.scene.getLayerAt(index);
	}

	public void addListDataListener(ListDataListener l) {
		this.listeners.add(l);
	}

	public void removeListDataListener(ListDataListener l) {
		this.listeners.remove(l);
	}


	public void layerAdded(Scene sourceScene, Layer layer, int index) {
		ListDataEvent lde = new ListDataEvent(sourceScene, ListDataEvent.INTERVAL_ADDED, index, index);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			ListDataListener listener = (ListDataListener) iter.next();
			listener.intervalAdded(lde);
		}
	}

	public void layerRemoved(Scene sourceScene, Layer layer, LayerInfo info, int index) {
		ListDataEvent lde = new ListDataEvent(sourceScene, ListDataEvent.INTERVAL_REMOVED, index, index);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			ListDataListener listener = (ListDataListener) iter.next();
			listener.intervalRemoved(lde);
		}
	}

	public void layerModified(Scene sourceScene, Layer layer) {
		int index = sourceScene.indexOf(layer);
		ListDataEvent lde = new ListDataEvent(sourceScene, ListDataEvent.CONTENTS_CHANGED, index, index);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			ListDataListener listener = (ListDataListener) iter.next();
			listener.contentsChanged(lde);
		}
	}

	public void layerMoved(Scene sourceScene, Layer layer, int indexOld, int indexNew) {
		this.layerModified(sourceScene, layer);
	}

	public void layerLockChanged(Scene sourceScene, Layer layer, boolean locked) {
		this.layerModified(sourceScene, layer);
	}

	public void layerPositionChanged(Scene sourceScene, Layer layer, Point oldPosition, Point newPosition) {
		this.layerModified(sourceScene, layer);
	}

	public void layerVisibilityChanged(Scene sourceScene, Layer layer, boolean visible) {
		this.layerModified(sourceScene, layer);
	}

}
