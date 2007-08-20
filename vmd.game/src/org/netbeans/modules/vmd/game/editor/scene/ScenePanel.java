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
package org.netbeans.modules.vmd.game.editor.scene;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import org.netbeans.modules.vmd.game.dialog.NewSceneDialog;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Position;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.SceneListener;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.TiledLayerListener;
import org.netbeans.modules.vmd.game.model.Scene.CreateSpriteAction;
import org.netbeans.modules.vmd.game.model.Scene.CreateTiledLayerAction;
import org.netbeans.modules.vmd.game.model.Scene.LayerInfo;
import org.netbeans.modules.vmd.game.model.Scene.RemoveSceneAction;
import org.netbeans.modules.vmd.game.model.Scene.RenameSceneAction;
import org.netbeans.modules.vmd.game.nbdialog.SpriteDialog;
import org.netbeans.modules.vmd.game.nbdialog.TiledLayerDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class ScenePanel extends JPanel implements SceneListener,
		TiledLayerListener, PropertyChangeListener, MouseMotionListener,
		MouseListener {

	private static final int DEFAULT_GRID_X = 20;
	private static final int DEFAULT_GRID_Y = 20;
	private static final int GRID_MINORS_IN_A_MAJOR = 5;

	private static final Color COLOR_BG = Color.WHITE;
	private static final Color COLOR_GRID_MINOR = Color.GRAY;
	private static final Color COLOR_GRID_MAJOR = Color.BLACK;
	private static final Color COLOR_GRID_QUADRANT = Color.LIGHT_GRAY;

	private Scene scene;

	private Ruler horizontalRuler = new Ruler(Ruler.HORIZONTAL);
	private Ruler verticalRuler = new Ruler(Ruler.VERTICAL);
	private GridButton gridButton = new GridButton();
	
	private boolean showGrid = true;
	private boolean snapGrid = false;
	
	private int gridX = DEFAULT_GRID_X;
	private int gridY = DEFAULT_GRID_Y;
	
	// ///////// ANIMATION SUPPORT ///////////
	// TODO finish animation support for AnimatedTiles and Sprites
	private boolean animated;

	public ScenePanel(Scene scene) {
		ToolTipManager.sharedInstance().registerComponent(this);
		this.scene = scene;
		this.scene.addSceneListener(this);
		this.scene.addPropertyChangeListener(this);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		for (Iterator iter = this.scene.getLayers().iterator(); iter.hasNext();) {
			Layer layer = (Layer) iter.next();
			this.registerLayerListeners(layer);
		}
	}

	private void registerLayerListeners(Layer layer) {
		layer.addPropertyChangeListener(this);
		if (layer instanceof TiledLayer) {
			TiledLayer tl = (TiledLayer) layer;
			tl.addTiledLayerListener(this);
		}
		if (layer instanceof Sprite) {
			Sprite sprite = (Sprite) layer;
			// /sprite.addSequenceContainerListener(this);
		}
	}

	private void unregisterLayerListeners(Layer layer) {
		layer.removePropertyChangeListener(this);
		if (layer instanceof TiledLayer) {
			TiledLayer tl = (TiledLayer) layer;
			tl.removeTiledLayerListener(this);
		}
		if (layer instanceof Sprite) {
			Sprite sprite = (Sprite) layer;
			// sprite.removeSequenceContainerListener(this);
		}
	}

	public Dimension getPreferredSize() {
		Rectangle bounds = this.scene.getAllLayersBounds();
		bounds.add(0, 0);
		return bounds.getSize();
	}

	public void paintComponent(Graphics g) {
		//System.out.println("paintComponent " + g.getClipBounds());
		this.clearBackground(g);

		this.drawQuadrants(g);
		
		if (isShowGrid()) {
			this.drawGrid(g);
		}

		this.drawSceneItems(g);
	}

	private void clearBackground(Graphics g) {
		Rectangle rect = g.getClipBounds();
		g.setColor(COLOR_BG);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}

	private void drawGrid(Graphics g) {
		Point quadrantOrigin = this.adjustFromOriginShift(new Point(0, 0));

		Rectangle rect = g.getClipBounds();
		//System.out.println("repaint " + rect);
		int colMin = (int) (rect.getX() / this.gridX);
		int colMax = (int) ((rect.getX() + rect.getWidth()) / this.gridX);

		int rowMin = (int) (rect.getY() / this.gridY);
		int rowMax = (int) ((rect.getY() + rect.getHeight()) / this.gridY);

		for (int r = rowMin; r <= rowMax; r++) {
			for (int c = colMin; c <= colMax; c++) {
				if ((0 == (c % GRID_MINORS_IN_A_MAJOR))
						|| (0 == (r % GRID_MINORS_IN_A_MAJOR))) {
					g.setColor(COLOR_GRID_MAJOR);
				} else {
					g.setColor(COLOR_GRID_MINOR);
				}
				int x = (c * this.gridX) + quadrantOrigin.x % this.gridX;
				int y = (r * this.gridY) + quadrantOrigin.y % this.gridY;
				//System.out.println("dot: " + x + ", " + y);
				g.drawLine(x, y, x, y);
			}
		}
	}

	private void drawQuadrants(Graphics g) {
		Point quadrantOrigin = this.adjustFromOriginShift(new Point(0, 0));

		Rectangle clip = g.getClipBounds();
		g.setColor(COLOR_GRID_QUADRANT);

		// vertical quadrant separator
		if (clip.intersectsLine(quadrantOrigin.x, Integer.MAX_VALUE,
				quadrantOrigin.x, Integer.MIN_VALUE)) {
			g.drawLine(quadrantOrigin.x, clip.y, quadrantOrigin.x, clip.y
					+ clip.height);
		}

		// horizontal quadrant separator
		if (clip.intersectsLine(Integer.MAX_VALUE, quadrantOrigin.y,
				Integer.MIN_VALUE, quadrantOrigin.y)) {
			g.drawLine(clip.x, quadrantOrigin.y, clip.x + clip.width,
					quadrantOrigin.y);
		}
	}

	private void drawSceneItems(Graphics g) {
		this.drawLayers(g);
		for (int i = 0; i < this.scene.getLayerCount(); i++) {
			Layer layer = (Layer) this.scene.getLayerAt(i);
			this.drawLayerDecorations((Graphics2D) g, layer);
		}
	}

	void drawLayers(Graphics g) {
		for (int i = this.scene.getLayerCount() - 1; i >= 0; i--) {
			Layer layer = (Layer) this.scene.getLayerAt(i);
			if (this.scene.isLayerVisible(layer)) {
				this.drawLayer(g, layer);
			}
		}
	}

	private void drawLayer(Graphics g, Layer layer) {
		Rectangle origClipBounds = g.getClipBounds();
		Point layerPos = scene.getLayerPosition(layer);
		this.adjustFromOriginShift(layerPos);
		Rectangle layerRect = new Rectangle(layerPos.x, layerPos.y, layer
				.getWidth(), layer.getHeight());
				
		Rectangle intersection = layerRect.intersection(origClipBounds);
		if (intersection.isEmpty())
			return;

		// set the graphics object relative to layer
		g.setClip(intersection);
		g.translate(layerPos.x, layerPos.y);

		// paint layer
		layer.paint((Graphics2D) g);

		// reset the graphics object
		g.translate(-layerPos.x, -layerPos.y);
		g.setClip(origClipBounds);
	}

	private void drawLayerDecorations(Graphics2D g, Layer layer) {
		if (!this.hilitedLayers.containsKey(layer) && !this.selectedLayers.contains(layer)) {
			return;
		}
		
		if (this.selectedLayers.contains(layer)) {
			g.setColor(colorSelection);
		}
		else if (this.hilitedLayers.containsKey(layer)) {
			g.setColor(this.hilitedLayers.get(layer));
		}
		else {
			return;
		}
		
		Rectangle r = null;
		
		
		//top
		r = this.getDecorationRectangleForLayerBottom(layer);
		r.translate(0, -layer.getHeight() -DECOR_BOTTOM_H);
		g.fillRect(r.x, r.y, r.width, r.height);
		//bottom
		r = this.getDecorationRectangleForLayerBottom(layer);
		g.fillRect(r.x, r.y, r.width, r.height);
		//left
		r = this.getDecorationRectangleForLayerLeft(layer);
		g.fillRect(r.x, r.y, r.width, r.height);
		//right
		r = this.getDecorationRectangleForLayerRight(layer);
		g.fillRect(r.x, r.y, r.width, r.height);
	}
	
	private static final int DECOR_TOP_W = 0;
	private static final int DECOR_TOP_H = 25;
	private static final int DECOR_SIDE_W = 5;
	private static final int DECOR_BOTTOM_W = 5;
	private static final int DECOR_BOTTOM_H = 5;
	
	private Rectangle getDecorationRectangleForLayerTop(Layer layer) {
		Point pos = this.adjustFromOriginShift(scene.getLayerPosition(layer));
		return this.getDecorationRectangleForLayerTop(pos, layer);
	}
	private Rectangle getDecorationRectangleForLayerBottom(Layer layer) {
		Point pos = this.adjustFromOriginShift(scene.getLayerPosition(layer));
		return this.getDecorationRectangleForLayerBottom(pos, layer);
	}
	private Rectangle getDecorationRectangleForLayerLeft(Layer layer) {
		Point pos = this.adjustFromOriginShift(scene.getLayerPosition(layer));
		return this.getDecorationRectangleForLayerLeft(pos, layer);
	}
	private Rectangle getDecorationRectangleForLayerRight(Layer layer) {
		Point pos = this.adjustFromOriginShift(scene.getLayerPosition(layer));
		return this.getDecorationRectangleForLayerRight(pos, layer);
	}
	
	private Rectangle getDecorationRectangleForLayerTop(Point pos, Layer layer) {
		return new Rectangle(pos.x - DECOR_TOP_W, pos.y - DECOR_TOP_H, DECOR_TOP_W + layer.getWidth() + DECOR_TOP_W, DECOR_TOP_H);
	}
	private Rectangle getDecorationRectangleForLayerBottom(Point pos, Layer layer) {
		return new Rectangle(pos.x - DECOR_BOTTOM_W, pos.y + layer.getHeight(), DECOR_BOTTOM_W + layer.getWidth() + DECOR_BOTTOM_W, DECOR_BOTTOM_H);
	}
	private Rectangle getDecorationRectangleForLayerLeft(Point pos, Layer layer) {
		return new Rectangle(pos.x - DECOR_SIDE_W, pos.y, DECOR_SIDE_W, layer.getHeight());
	}
	private Rectangle getDecorationRectangleForLayerRight(Point pos, Layer layer) {
		return new Rectangle(pos.x + layer.getWidth(), pos.y, DECOR_SIDE_W, layer.getHeight());
	}
	
	
	private void repaintLayerWithDecorations(Layer layer) {
		Point p = this.scene.getLayerPosition(layer);
		this.repaintLayerWithDecorations(p, layer);
	}
	private void repaintLayerWithDecorations(Point posUnadjusted, Layer layer) {
		Point pos = this.adjustFromOriginShift(posUnadjusted);
		this.repaintLayer(pos, layer);
		this.repaintLayerDecorations(pos, layer);
	}
	
	private void repaintLayer(Layer layer) {
		Point p = this.adjustFromOriginShift(this.scene.getLayerPosition(layer));
		this.repaintLayer(p, layer);
	}
	private void repaintLayer(Point pos, Layer layer) {
		this.repaint(pos.x, pos.y, layer.getWidth(), layer.getHeight());
	}
	
	
	private void repaintLayerDecorations(Point pos, Layer layer) {	
		this.repaint(this.getDecorationRectangleForLayerTop(pos, layer)); 
		this.repaint(this.getDecorationRectangleForLayerBottom(pos, layer)); 
		this.repaint(this.getDecorationRectangleForLayerLeft(pos, layer)); 
		this.repaint(this.getDecorationRectangleForLayerRight(pos, layer)); 
	}
	private void repaintLayerDecorations(Layer layer) {
		if (layer == null)
			return;
		Point p = this.adjustFromOriginShift(this.scene.getLayerPosition(layer));
		this.repaintLayerDecorations(p, layer);
	}

	private void repaintAllLayerDecorations() {
		List<Layer> layers = this.scene.getLayers();
		for (Layer layer : layers) {
			this.repaintLayerDecorations(layer);
		}
	}

	// -------- SceneListener ---------

	public void layerAdded(Scene sourceScene, Layer layer, int index) {
		this.registerLayerListeners(layer);
		//this.repaintLayerWithDecorations(layer);
		this.repaintAllLayerDecorations();
	}

	public void layerMoved(Scene sourceScene, Layer layer, int indexOld,
			int indexNew) {
		//this.repaintLayerWithDecorations(layer);
		this.repaintAllLayerDecorations();
	}

	public void layerRemoved(Scene sourceScene, Layer layer, LayerInfo info,
			int index) {
		this.unregisterLayerListeners(layer);
		//this.repaintLayerWithDecorations(layer);
		this.repaintAllLayerDecorations();
	}

	public void layerLockChanged(Scene sourceScene, Layer layer, boolean locked) {
	}

	public void layerPositionChanged(Scene sourceScene, Layer layer,
			Point oldPosition, Point newPosition, boolean inTransition) {
		this.repaintLayerWithDecorations(oldPosition, layer);
		this.repaintLayerWithDecorations(newPosition, layer);
	}
	
	
	public void layerVisibilityChanged(Scene sourceScene, Layer layer,
			boolean visible) {
		this.repaintLayerWithDecorations(layer);
	}

	private Point adjustFromOriginShift(Point p) {
		Rectangle layersBox = this.scene.getAllLayersBounds();
		layersBox.add(0, 0);
		p.translate(-layersBox.x, -layersBox.y);
		return p;
	}

	private Point adjustToOriginShift(Point p) {
		Rectangle layersBox = this.scene.getAllLayersBounds();
		layersBox.add(0, 0);
		p.translate(layersBox.x, layersBox.y);
		return p;
	}

	// -------- PropertyChangeListener ---------

	public void propertyChange(PropertyChangeEvent e) {
		//System.out.println("ScenePanel.propertyChange: " + e);
		if (e.getSource() == this.scene) {
			if (e.getPropertyName().equals(Scene.PROPERTY_LAYERS_BOUNDS)) {
				this.revalidate();
				this.repaint();
				this.horizontalRuler.repaint();
				this.verticalRuler.repaint();
			}
		} else if (e.getSource() instanceof TiledLayer) {
			TiledLayer tl = (TiledLayer) e.getSource();
			// TODO
		} else if (e.getSource() instanceof Sprite) {
			Sprite s = (Sprite) e.getSource();
			if (e.getPropertyName().equals(
					SequenceContainer.PROPERTY_DEFAULT_SEQUENCE)) {
				this.repaintLayerWithDecorations(s);
			} else if (e.getPropertyName().equals(Editable.PROPERTY_NAME)) {
				// TODO
			}
		}
	}

	// -------- TiledLayerListener ---------

	public void columnsInserted(TiledLayer source, int index, int count) {
		this.repaintLayerWithDecorations(source);
	}

	public void columnsRemoved(TiledLayer source, int index, int count) {
		this.repaintLayerWithDecorations(source);
	}

	public void rowsInserted(TiledLayer source, int index, int count) {
		this.repaintLayerWithDecorations(source);
	}

	public void rowsRemoved(TiledLayer source, int index, int count) {
		this.repaintLayerWithDecorations(source);
	}

	public void tileChanged(TiledLayer source, int row, int col) {
		int w = source.getTileWidth();
		int h = source.getTileHeight();
		Point p = this.scene.getLayerPosition(source);
		p.translate(col * w, row * h);
		this.adjustFromOriginShift(p);
		this.repaint(p.x, p.y, w, h);
	}

	public void tilesChanged(TiledLayer source, Set positions) {
		for (Object object : positions) {
			Position pos = (Position) object;
			this.tileChanged(source, pos.getRow(), pos.getCol());
		}
	}

	public String getToolTipText(MouseEvent e) {
		StringBuffer sb = new StringBuffer();
		Point p = this.adjustToOriginShift(e.getPoint());
		List<Layer> layers = this.scene.getLayersAtPoint(p);
		if (layers.isEmpty()) {
			return null;
		}
		for (Layer layer : layers) {
			sb.append(layer.getName());
			sb.append(", ");
		}
		sb.setLength(sb.length() - 2);
		return sb.toString();
	}

	
	// -------- MouseMotionListener ---------

	private Layer topHilitedLayer;
	private Map<Layer, Color> hilitedLayers = new HashMap<Layer, Color>();
	private Color colorSelection = new Color(0, 0, 255, 170);
	private Color colorHilitePrimary = new Color(0, 0, 0, 120);
	private Color colorHiliteSecondary = new Color(0, 0, 0, 50);
	
	public void mouseMoved(MouseEvent e) {		
		Point p = this.adjustToOriginShift(e.getPoint());
		List<Layer> layers = this.scene.getLayersAtPoint(p);
		if (layers.size() == 0)
			return;
		
		//add hilited layers
		for (Layer layer : layers) {
			if (!hilitedLayers.containsKey(layer)) {
				this.hilitedLayers.put(layer, colorHiliteSecondary);
				this.repaintLayerDecorations(layer);
			}
		}
		Layer oldTop = this.topHilitedLayer;
		Layer top = layers.get(0);
		if (oldTop != top) {
			this.hilitedLayers.put(top, colorHilitePrimary);
			this.hilitedLayers.put(oldTop, colorHiliteSecondary);
			this.topHilitedLayer = top;
			this.repaintLayerDecorations(oldTop);
			this.repaintLayerDecorations(top);
		}
		
		//find layers that are no longer hilited
		for (Iterator iter = this.hilitedLayers.keySet().iterator(); iter.hasNext();) {
			Layer layer = (Layer) iter.next();
			if (!layers.contains(layer)) {
				this.repaintLayerDecorations(layer);
				iter.remove();
			}
		}
	}
	
	
	
	private Set<Layer> selectedLayers = new HashSet<Layer>();
	private Map<Layer, Point> dragLayerStartPoints = new HashMap<Layer, Point>();
	
	private Point startDragPoint = null;
	private Point lastDragPoint = null;
	private Layer snapToGridReferenceLayer = null;
	private String dragDirectionVertical = BorderLayout.NORTH;
	private String dragDirectionHorizontal = BorderLayout.WEST;

	private boolean mouseDragging;
	
	private void clearSelectedLayers() {
		for (Iterator iter = this.selectedLayers.iterator(); iter.hasNext();) {
			Layer layer = (Layer) iter.next();
			iter.remove();
			this.repaintLayerDecorations(layer);
		}
		this.dragLayerStartPoints.clear();
		this.lastDragPoint = null;
		this.startDragPoint = null;
		this.snapToGridReferenceLayer = null;
		this.dragDirectionHorizontal = BorderLayout.WEST;
		this.dragDirectionVertical = BorderLayout.NORTH;
	}
	
	private void addSelectedLayer(Layer layer, boolean toogleSelection) {
		if (toogleSelection) {
			if (this.selectedLayers.contains(layer)) {
				this.selectedLayers.remove(layer);
			}
			else {
				this.selectedLayers.add(layer);
			}
		}
		else {
			this.selectedLayers.add(layer);
		}
		
		this.repaintLayerDecorations(layer);
		
		//TODO : repaint only the layer outlines not the whole rulers
		this.horizontalRuler.repaint();
		this.verticalRuler.repaint();
	}

	public void mouseDragged(MouseEvent e) {
    	//System.out.println("mouseDragged");
		Point p = this.adjustToOriginShift(e.getPoint());
		
		//initialize dragging
		if (this.mouseDragging == false) {
			//check that there are layers to drag and that were starting the drag inside a layer
			if (selectedLayers.isEmpty() || this.scene.getLayersAtPoint(p).isEmpty())
				return;
			
			//make sure that we are starting the drag inside a layer selected for dragging
			boolean dragStartPointOK = false;
			for (Layer selectedLayer : this.selectedLayers) {
				if (scene.getLayerBounds(selectedLayer).contains(p)) {
					dragStartPointOK = true;
					break;
				}
			}
			if (!dragStartPointOK)
				return;
						
			//now i know i am doing a drag and that the cursor is inside a dragged layer
			//use the topmost layer as the reference for snapping to grid
			for (Layer tmp : this.scene.getLayersAtPoint(p)) {
				if (this.selectedLayers.contains(tmp)) {
					this.snapToGridReferenceLayer = tmp;
					break;
				}
			}
			
			for (Layer dragLayer : this.selectedLayers) {
				this.dragLayerStartPoints.put(dragLayer, this.scene.getLayerPosition(dragLayer));
			}
			
			this.startDragPoint = p;
			this.mouseDragging = true;
			return;
		}
		
		int dx = p.x - this.startDragPoint.x;
		int dy = p.y - this.startDragPoint.y;
		
		//determine the direction of drag
		if (this.lastDragPoint != null) {
			if (p.x < this.lastDragPoint.x) {
				//System.out.println("drag left");
				this.dragDirectionHorizontal = BorderLayout.WEST;
			}
			else if (p.x > this.lastDragPoint.x) {
				//System.out.println("drag right");
				this.dragDirectionHorizontal = BorderLayout.EAST;
			}
			if (p.y < this.lastDragPoint.y) {
				//System.out.println("drag up");
				this.dragDirectionVertical = BorderLayout.NORTH;
			}
			else if (p.y > this.lastDragPoint.y) {
				//System.out.println("drag down");
				this.dragDirectionVertical = BorderLayout.SOUTH;
			}			
		}
		
		//if grid snapping
		if (isSnapGrid() && (this.lastDragPoint != null)) {
			Point delta = findSnapToGridDelta(p, dx, dy);
			this.translateSelectedLayers(delta.x, delta.y, true);
		}
		else {
			//System.out.println("translate dx = " + dx);
			this.translateSelectedLayers(dx, dy, true);
		}
		
		this.lastDragPoint = p;
		//System.out.println("last drag point: " + this.lastDragPoint);

		//TODO : repaint only the layer outlines not the whole rulers
		this.horizontalRuler.repaint();
		this.verticalRuler.repaint();
		
		//make sure to scroll while dragging outside the visible area
		this.scrollRectToVisible(new Rectangle(e.getPoint()));
	}

	private Point findSnapToGridDelta(Point p, int dx, int dy ) {
			Point s =  new Point(this.dragLayerStartPoints.get(snapToGridReferenceLayer));
			
			//reference point for grid snapping
			Point ref = new Point(s);
			ref.translate(dx, dy);
			
			if (p.x < this.lastDragPoint.x) {
				//do nothing
			}
			else if (p.x > this.lastDragPoint.x) {
				ref.translate(this.snapToGridReferenceLayer.getWidth(), 0);
			}
			else {
				if (this.dragDirectionHorizontal == BorderLayout.EAST) {
					ref.translate(this.snapToGridReferenceLayer.getWidth(), 0);
				}
			}
			if (p.y < this.lastDragPoint.y) {
				//do nothing
			}
			else if (p.y > this.lastDragPoint.y) {
				ref.translate(0, this.snapToGridReferenceLayer.getHeight());
			}
			else {
				if (this.dragDirectionVertical == BorderLayout.SOUTH) {
					ref.translate(0, this.snapToGridReferenceLayer.getHeight());
				}
			}
			
			//find grid point closest to the reference point
			Point nearest = this.findNearestGridPoint(ref);
						
			//translate nearest to be at the left top corner of the layer
			if (this.dragDirectionHorizontal == BorderLayout.EAST) {
				nearest.translate(-this.snapToGridReferenceLayer.getWidth(), 0);
				//System.out.println("moving EAST - setting left top at " + nearest);
			}
			if (this.dragDirectionVertical == BorderLayout.SOUTH) {
				nearest.translate(0, -this.snapToGridReferenceLayer.getHeight());
				//System.out.println("moving SOUTH - setting left top at " + nearest);
			}

			dx = nearest.x - s.x;
			dy = nearest.y - s.y;
			
			return new Point(dx, dy);
	}
	
	private Point findNearestGridPoint(Point p) {
		
		Point nearest = new Point(p);
		int offX = p.x % this.gridX;
		int offY = p.y % this.gridY;
		
		if (offX == 0 && offY == 0) {
			return nearest;
		}
		
		if (offX != 0) {
			int gx1 = (p.x / this.gridX);
			int gx2 = (p.x >= 0 ? gx1 +1 : gx1 -1);
			gx1 *= this.gridX;
			gx2 *= this.gridX;
			//System.out.println("X " + gx1 + " -- " + gx2);
			if (Math.abs(p.x - gx1) < Math.abs(p.x - gx2)) {
				nearest.x = gx1;
			}
			else {
				nearest.x = gx2;
			}
		}
		
		if (offY != 0) {
			int gy1 = (p.y / this.gridY);
			int gy2 = (p.y >= 0 ? gy1 +1 : gy1 -1);
			gy1 *= this.gridY;
			gy2 *= this.gridY;
			//System.out.println("Y " + gy1 + " -- " + gy2);
			if (Math.abs(p.y - gy1) < Math.abs(p.y - gy2)) {
				nearest.y = gy1;
			}
			else {
				nearest.y = gy2;
			}
		}
		//System.out.println(p + " -> " + nearest);
		return nearest;
	}
	
	private void translateSelectedLayers(int dx, int dy, boolean inTransition) {
		//System.out.println("translate dx: " + dx + ", dy: " + dy + ", " + inTransition);
		for (Layer dragLayer : this.selectedLayers) {
			Point slp = new Point(this.dragLayerStartPoints.get(dragLayer));
			slp.translate(dx, dy);
			if (this.scene.isLayerLocked(dragLayer)) {
				continue;
			}
			this.scene.setLayerPosition(dragLayer, slp, inTransition);
		}
	}
	
	// -------- MouseListener ---------

	
	private Layer lastAddedLayerByMousePress = null;
	
    public void mouseReleased(MouseEvent e) {
    	//System.out.println("mouseReleased");
    	if (e.isPopupTrigger()) {
    		this.handlePopUp(e);
    		return;
    	}

		Point p = this.adjustToOriginShift(e.getPoint());
		List<Layer> layers = this.scene.getLayersAtPoint(p);
		if (!layers.isEmpty()) {
			Layer layer = layers.get(0);
	    	if (this.mouseDragging == false && this.lastAddedLayerByMousePress != layer) {
	    		if (!this.isMultiSelect(e)) {
	    			this.clearSelectedLayers();
	    		}
	    	}
			if (this.mouseDragging == true && this.lastDragPoint != null && this.startDragPoint != null) {
				int dx = this.lastDragPoint.x - this.startDragPoint.x;
				int dy = this.lastDragPoint.y - this.startDragPoint.y;
				if (isSnapGrid()) {
					Point delta = findSnapToGridDelta(this.lastDragPoint, dx, dy);
					this.translateSelectedLayers(delta.x, delta.y, false);
				}
				else {
					this.translateSelectedLayers(dx, dy, false);
				}
			}
		}
		this.mouseDragging = false;
    }
    
    public void mousePressed(MouseEvent e) {
    	//System.out.println("mousePressed");
		this.mouseDragging = false;
		this.lastAddedLayerByMousePress = null;
    	if (e.isPopupTrigger()) {
    		this.handlePopUp(e);
    		return;
    	}
    	
		//only left mouse button manipulates the scene
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}
		Point p = this.adjustToOriginShift(e.getPoint());
		List<Layer> layers = this.scene.getLayersAtPoint(p);

		if (!isMultiSelect(e)) {
			for (Layer layer : layers) {
				if (this.selectedLayers.contains(layer)) {
					return;
				}
			}
			//System.out.println("NOT MultiSelect - clearDragSelection");
			this.clearSelectedLayers();
		}
		if (layers.isEmpty()) {
			Layer tmp = this.topHilitedLayer;
			this.topHilitedLayer = null;
			this.hilitedLayers.remove(tmp);
			this.repaintLayerDecorations(tmp);
			return;
		}
				
		Layer layer = layers.get(0);
		if (!this.selectedLayers.contains(layer)) {
			this.addSelectedLayer(layer, false);
			this.lastAddedLayerByMousePress = layer;
	    	this.repaintLayerDecorations(layer);
		}
		else {
			if (isMultiSelect(e)) {
				this.addSelectedLayer(layer, true);
			}
		}
    	//System.out.println("selected layer: " + layer);
    }

	public void mouseClicked(MouseEvent e) {
    	//System.out.println("mouseClicked");
		this.mouseDragging = false;
		if (e.getClickCount() >= 2) {
			
			Point p = this.adjustToOriginShift(e.getPoint());
			List<Layer> layers = this.scene.getLayersAtPoint(p);
			if (!layers.isEmpty()) {
				this.scene.getGameDesign().getMainView().requestEditing(layers.get(0));
			}
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

    private void handlePopUp(MouseEvent e) {
		
    	//scene stuff
    	
//    	CreateSceneAction cs = new CreateSceneAction();
		DuplicateSceneAction ds = new DuplicateSceneAction();
		RenameSceneAction rs = this.scene.new RenameSceneAction();
		RemoveSceneAction rsa = this.scene.new RemoveSceneAction();

		
		//add stuff
		
		CreateTiledLayerAction ctl = this.scene.new CreateTiledLayerAction();
		CreateSpriteAction csp = this.scene.new CreateSpriteAction();
		
		JMenu sub2MenuTiledLayers = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuAddTiledLayer.txt"));
		List<TiledLayer> tiledLayers = this.scene.getGameDesign().getTiledLayers();
		for (TiledLayer layer : tiledLayers) {
			if (this.scene.contains(layer)) {
				continue;
			}
			AddLayerAction al = new AddLayerAction();
			al.putValue(AbstractAction.NAME, layer.getName());
			al.putValue(AddLayerAction.PROP_LAYER, layer);
			al.putValue(AddLayerAction.PROP_POSITION, this.adjustToOriginShift(e.getPoint()));
			sub2MenuTiledLayers.add(al);
		}
		if (sub2MenuTiledLayers.getItemCount() == 0) {
			sub2MenuTiledLayers.setEnabled(false);
		}
		
		JMenu sub2MenuSprites = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuAddSprite.txt"));
		List<Sprite> sprites = this.scene.getGameDesign().getSprites();
		for (Sprite layer : sprites) {
			if (this.scene.contains(layer)) {
				continue;
			}
			AddLayerAction al = new AddLayerAction();
			al.putValue(AbstractAction.NAME, layer.getName());
			al.putValue(AddLayerAction.PROP_LAYER, layer);
			al.putValue(AddLayerAction.PROP_POSITION, this.adjustToOriginShift(e.getPoint()));
			sub2MenuSprites.add(al);
		}
		if (sub2MenuSprites.getItemCount() == 0) {
			sub2MenuSprites.setEnabled(false);
		}
		
		//order stuff
		
		JMenu sub2MenuPushUp = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuPushUp.txt"));
		JMenu sub2MenuPushDown = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuPushDown.txt"));
		JMenu sub2MenuToTop = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuTop.txt"));
		JMenu sub2MenuToBottom = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuBottom.txt"));

		Point poitAdjusted = this.adjustToOriginShift(e.getPoint());
		List<Layer> layersUnderCursor = this.scene.getLayersAtPoint(poitAdjusted);
		
		if (layersUnderCursor.size() < 2) {
			sub2MenuPushUp.setEnabled(false);
			sub2MenuPushDown.setEnabled(false);
		}
		else {
			//can push up any except the first (it's already on top)
			for (int i = 0; i < layersUnderCursor.size(); i++) {
				PushUpLayerAction pul = new PushUpLayerAction();
				sub2MenuPushUp.add(pul);
				pul.putValue(AbstractAction.NAME, layersUnderCursor.get(i).getName());
				if (i == 0) {
					pul.setEnabled(false);
					continue;
				}
				pul.putValue(PushUpLayerAction.PROP_LAYER, layersUnderCursor.get(i));
				pul.putValue(PushUpLayerAction.PROP_LAYER_TO_TOP, layersUnderCursor.get(i - 1));
			}
			//can push down any except the last (it's already at bottom)
			for (int i = 0; i < layersUnderCursor.size(); i++) {
				PushDownLayerAction pdl = new PushDownLayerAction();
				sub2MenuPushDown.add(pdl);
				pdl.putValue(AbstractAction.NAME, layersUnderCursor.get(i).getName());
				if (i == layersUnderCursor.size()-1) {
					pdl.setEnabled(false);
					continue;
				}
				pdl.putValue(PushDownLayerAction.PROP_LAYER, layersUnderCursor.get(i));
				pdl.putValue(PushDownLayerAction.PROP_LAYER_TO_BELOW, layersUnderCursor.get(i + 1));
			}
		}
		
		//can push to the top any except the one that's already on top
		for (int i = 0; i < layersUnderCursor.size(); i++) {
			LayerToTopAction ltt = new LayerToTopAction();
			sub2MenuToTop.add(ltt);
			ltt.putValue(AbstractAction.NAME, layersUnderCursor.get(i).getName());
			if (this.scene.getLayerAt(0) == layersUnderCursor.get(i)) {
				ltt.setEnabled(false);
				continue;
			}
			ltt.putValue(LayerToTopAction.PROP_LAYER, layersUnderCursor.get(i));
		}
		if (layersUnderCursor.isEmpty()) {
			sub2MenuToTop.setEnabled(false);
		}
		
		//can push any to the bottom except the one that's already at bottom
		for (int i = 0; i < layersUnderCursor.size(); i++) {
			LayerToBottomAction ltb = new LayerToBottomAction();
			sub2MenuToBottom.add(ltb);
			ltb.putValue(AbstractAction.NAME, layersUnderCursor.get(i).getName());
			if (this.scene.getLayerAt(this.scene.getLayerCount() -1) == layersUnderCursor.get(i)) {
				ltb.setEnabled(false);
				continue;
			}
			ltb.putValue(LayerToBottomAction.PROP_LAYER, layersUnderCursor.get(i));
		}
		if (layersUnderCursor.isEmpty()) {
			sub2MenuToBottom.setEnabled(false);
		}
		
		//edit stuff
		JMenu sub1MenuEditLayers = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuEdit.txt"));
		if (layersUnderCursor.isEmpty()) {
			sub1MenuEditLayers.setEnabled(false);
		}
		else {
			for (int i = 0; i < layersUnderCursor.size(); i++) {
				EditLayerAction rl = new EditLayerAction();
				sub1MenuEditLayers.add(rl);
				rl.putValue(EditLayerAction.PROP_LAYER, layersUnderCursor.get(i));
				rl.putValue(AbstractAction.NAME, layersUnderCursor.get(i).getName());
			}
		}
		//
		//view stuff
		JMenu sub1MenuViewLayers = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuView.txt"));
		if (layersUnderCursor.isEmpty()) {
			sub1MenuViewLayers.setEnabled(false);
		}
		else {
			for (int i = 0; i < layersUnderCursor.size(); i++) {
				Layer layer = layersUnderCursor.get(i);
				JCheckBoxMenuItem item = new JCheckBoxMenuItem(layer.getName(), this.scene.isLayerVisible(layer));
				sub1MenuViewLayers.add(item);
				item.addItemListener(new VisibilityListener(layer));
			}
		}
		
		//lock stuff
		JMenu sub1MenuViewLocks = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuLock.txt"));
		if (layersUnderCursor.isEmpty()) {
			sub1MenuViewLocks.setEnabled(false);
		}
		else {
			for (int i = 0; i < layersUnderCursor.size(); i++) {
				Layer layer = layersUnderCursor.get(i);
				JCheckBoxMenuItem item = new JCheckBoxMenuItem(layer.getName(), this.scene.isLayerLocked(layer));
				sub1MenuViewLocks.add(item);
				item.addItemListener(new LockListener(layer));
			}
		}
		
		//align stuff
		JMenu sub1MenuAlign = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuAlign.txt"));
		if (this.selectedLayers.size() < 2) {
			sub1MenuAlign.setEnabled(false);
		}
		else {
			AlignLayersTopAction alt = new AlignLayersTopAction();
			alt.putValue(AlignLayersTopAction.PROP_LAYERS, this.selectedLayers);
			AlignLayersLeftAction all = new AlignLayersLeftAction();
			all.putValue(AlignLayersTopAction.PROP_LAYERS, this.selectedLayers);
			AlignLayersBottomAction alb = new AlignLayersBottomAction();
			alb.putValue(AlignLayersBottomAction.PROP_LAYERS, this.selectedLayers);
			AlignLayersRightAction alr = new AlignLayersRightAction();
			alr.putValue(AlignLayersRightAction.PROP_LAYERS, this.selectedLayers);
			
			sub1MenuAlign.add(alt);
			sub1MenuAlign.add(all);
			sub1MenuAlign.add(alb);
			sub1MenuAlign.add(alr);
		}
		
		
		//select stuff
		JMenu sub1MenuSelectLayers = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuSelect.txt"));
		if (!layersUnderCursor.isEmpty()) {
			for (int i = 0; i < layersUnderCursor.size(); i++) {
				Layer layer = layersUnderCursor.get(i);
				JCheckBoxMenuItem item = new JCheckBoxMenuItem(layer.getName(), this.selectedLayers.contains(layer));
				sub1MenuSelectLayers.add(item);
				item.addItemListener(new SelectListener(layer));
			}
			sub1MenuSelectLayers.addSeparator();
			SelectLayersUnderCursorAction slc = new SelectLayersUnderCursorAction();
			poitAdjusted = this.adjustToOriginShift(e.getPoint());
			slc.putValue(SelectLayersUnderCursorAction.PROP_POINT, poitAdjusted);
			sub1MenuSelectLayers.add(slc);
		}
		SelectAllLayersAction sal = new SelectAllLayersAction();
		sub1MenuSelectLayers.add(sal);
		
		
		//remove stuff
		JMenu sub1MenuRemoveLayers = new JMenu(NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuRemove.txt"));
		if (layersUnderCursor.isEmpty()) {
			sub1MenuRemoveLayers.setEnabled(false);
		}
		else {
			for (int i = 0; i < layersUnderCursor.size(); i++) {
				RemoveLayerAction rl = new RemoveLayerAction();
				sub1MenuRemoveLayers.add(rl);
				rl.putValue(RemoveLayerAction.PROP_LAYER, layersUnderCursor.get(i));
				rl.putValue(AbstractAction.NAME, layersUnderCursor.get(i).getName());
			}
		}
		RemoveSelectedLayersAction rsl = new RemoveSelectedLayersAction();
		if (this.selectedLayers.isEmpty()) {
			rsl.setEnabled(false);
		}
		else {
			rsl.putValue(RemoveSelectedLayersAction.PROP_LAYERS, this.selectedLayers);
		}
		
		
		JPopupMenu menu = new JPopupMenu();
//		menu.add(cs);
		menu.add(ds);
		menu.add(rs);
		menu.add(rsa);
		menu.addSeparator();
		
		//menu.add(sub1MenuAddLayers);
		menu.add(ctl);
		menu.add(csp);
		menu.add(sub2MenuTiledLayers);
		menu.add(sub2MenuSprites);
		menu.addSeparator();
		
		//menu.add(sub1MenuOrderLayers);
		menu.add(sub2MenuPushUp);
		menu.add(sub2MenuPushDown);
		menu.add(sub2MenuToTop);
		menu.add(sub2MenuToBottom);		
		menu.addSeparator();
		
		menu.add(sub1MenuEditLayers);
		menu.add(sub1MenuViewLayers);
		menu.add(sub1MenuViewLocks);
		menu.add(sub1MenuAlign);
		menu.addSeparator();
		
		menu.add(sub1MenuSelectLayers);
		menu.addSeparator();
		
		menu.add(sub1MenuRemoveLayers);
		menu.add(rsl);
		
		menu.show(this, e.getX(), e.getY());
    }
    
    private class VisibilityListener implements ItemListener {
    	private Layer layer;
    	public VisibilityListener(Layer layer) {
    		this.layer = layer;
    	}
		public void itemStateChanged(ItemEvent e) {
			boolean visible = e.getStateChange() == ItemEvent.SELECTED ? true : false;
			ScenePanel.this.scene.setLayerVisible(this.layer, visible);
			System.out.println("set " + layer.getName() + " visible " + visible); // NOI18N
		}
    }
    
    private class LockListener implements ItemListener {
    	private Layer layer;
    	public LockListener(Layer layer) {
    		this.layer = layer;
    	}
		public void itemStateChanged(ItemEvent e) {
			boolean locked = e.getStateChange() == ItemEvent.SELECTED ? true : false;
			ScenePanel.this.scene.setLayerLocked(this.layer, locked);
			System.out.println("set " + layer.getName() + " locked " + locked); // NOI18N
		}
    }
    private class SelectListener implements ItemListener {
    	private Layer layer;
    	public SelectListener(Layer layer) {
    		this.layer = layer;
    	}
		public void itemStateChanged(ItemEvent e) {
			//boolean selected = e.getStateChange() == ItemEvent.SELECTED ? true : false;
			ScenePanel.this.addSelectedLayer(layer, true);
			System.out.println("set " + layer.getName() + " selection toogled"); // NOI18N
		}
    }
      
	private boolean isMultiSelect(MouseEvent e) {
		return e.isControlDown() || e.isMetaDown();
	}

	private class EditLayerAction extends AbstractAction {
		public static final String PROP_LAYER = "PROP_LAYER"; // NOI18N

		public void actionPerformed(ActionEvent e) {
			Layer layer = (Layer) this.getValue(PROP_LAYER);
			ScenePanel.this.scene.getGameDesign().getMainView().requestEditing(layer);
		}
	}
	
	private class AlignLayersTopAction extends AbstractAction {
		public static final String PROP_LAYERS = "PROP_LAYERS"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuTop.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Set<Layer> layers = (Set<Layer>) this.getValue(PROP_LAYERS);
			int top = Integer.MAX_VALUE;
			for (Layer layer : layers) {
				top = scene.getLayerPosition(layer).y < top ? scene.getLayerPosition(layer).y : top;
			}
			for (Layer layer : layers) {
				scene.setLayerPositionY(layer, top, false);
			}
		}
	}
	private class AlignLayersLeftAction extends AbstractAction {
		public static final String PROP_LAYERS = "PROP_LAYERS"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuLeft.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Set<Layer> layers = (Set<Layer>) this.getValue(PROP_LAYERS);
			int top = Integer.MAX_VALUE;
			for (Layer layer : layers) {
				top = scene.getLayerPosition(layer).x < top ? scene.getLayerPosition(layer).x : top;
			}
			for (Layer layer : layers) {
				scene.setLayerPositionX(layer, top, false);
			}
		}
	}
	private class AlignLayersBottomAction extends AbstractAction {
		public static final String PROP_LAYERS = "PROP_LAYERS"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuBottom.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Set<Layer> layers = (Set<Layer>) this.getValue(PROP_LAYERS);
			int bottom = Integer.MIN_VALUE;
			for (Layer layer : layers) {
				bottom = scene.getLayerPosition(layer).y + layer.getHeight() > bottom ? scene.getLayerPosition(layer).y + layer.getHeight() : bottom;
			}
			for (Layer layer : layers) {
				scene.setLayerPositionY(layer, bottom - layer.getHeight(), false);
			}
		}
	}
	private class AlignLayersRightAction extends AbstractAction {
		public static final String PROP_LAYERS = "PROP_LAYERS"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuRight.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Set<Layer> layers = (Set<Layer>) this.getValue(PROP_LAYERS);
			int right = Integer.MIN_VALUE;
			for (Layer layer : layers) {
				right = scene.getLayerPosition(layer).x + layer.getWidth() > right ? scene.getLayerPosition(layer).x + layer.getWidth() : right;
			}
			for (Layer layer : layers) {
				scene.setLayerPositionX(layer, right - layer.getWidth(),false);
			}
		}
	}
	
	
	private class RemoveLayerAction extends AbstractAction {
		public static final String PROP_LAYER = "PROP_LAYER"; // NOI18N

		public void actionPerformed(ActionEvent e) {
			Layer layer = (Layer) this.getValue(PROP_LAYER);
			ScenePanel.this.scene.remove(layer);
		}
	}
	private class RemoveSelectedLayersAction extends AbstractAction {
		public static final String PROP_LAYERS = "PROP_LAYERS"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuRemoveSelected.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Set<Layer> layers = (Set<Layer>) this.getValue(PROP_LAYERS);
			for (Layer layer : layers) {
				ScenePanel.this.scene.remove(layer);
			}
		}
	}
	
	
	private class SelectLayersUnderCursorAction extends AbstractAction {
		public static final String PROP_POINT = "PROP_POINT"; // NOI18N

		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuSelectUnder.txt"));
		}

		public void actionPerformed(ActionEvent e) {
			Point p = (Point) this.getValue(PROP_POINT);
			List<Layer> layers = ScenePanel.this.scene.getLayersAtPoint(p);
			for (Layer layer : layers) {
				ScenePanel.this.addSelectedLayer(layer, false);
			}
		}
	}
	private class SelectAllLayersAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.menuSelectAll.txt"));
		}

		public void actionPerformed(ActionEvent e) {
			List<Layer> layers = ScenePanel.this.scene.getLayers();
			for (Layer layer : layers) {
				ScenePanel.this.addSelectedLayer(layer, false);
			}
		}		
	}

	private class PushUpLayerAction extends AbstractAction {
		public static final String PROP_LAYER = "PROP_LAYER"; // NOI18N
		public static final String PROP_LAYER_TO_TOP = "PROP_LAYER_TO_TOP"; // NOI18N

		public void actionPerformed(ActionEvent e) {
			Layer layer = (Layer) this.getValue(PROP_LAYER);
			Layer toTop = (Layer) this.getValue(PROP_LAYER_TO_TOP);
			ScenePanel.this.scene.insert(layer, ScenePanel.this.scene.indexOf(toTop));
		}		
	}
	private class PushDownLayerAction extends AbstractAction {
		public static final String PROP_LAYER = "PROP_LAYER"; // NOI18N
		public static final String PROP_LAYER_TO_BELOW = "PROP_LAYER_TO_BELOW"; // NOI18N
		
		public void actionPerformed(ActionEvent e) {
			Layer layer = (Layer) this.getValue(PROP_LAYER);
			Layer toBelow = (Layer) this.getValue(PROP_LAYER_TO_BELOW);
			ScenePanel.this.scene.insert(layer, ScenePanel.this.scene.indexOf(toBelow));
		}		
	}
	private class LayerToTopAction extends AbstractAction {
		public static final String PROP_LAYER = "PROP_LAYER"; // NOI18N

		public void actionPerformed(ActionEvent e) {
			Layer layer = (Layer) this.getValue(PROP_LAYER);
			ScenePanel.this.scene.insert(layer, 0);
		}		
	}
	private class LayerToBottomAction extends AbstractAction {
		public static final String PROP_LAYER = "PROP_LAYER"; // NOI18N

		public void actionPerformed(ActionEvent e) {
			Layer layer = (Layer) this.getValue(PROP_LAYER);
			ScenePanel.this.scene.insert(layer, ScenePanel.this.scene.getLayerCount()-1);
		}		
	}
//	private class CreateSceneAction extends AbstractAction {
//		{
//			this.putValue(NAME, "Create new scene");
//		}
//
//		public void actionPerformed(ActionEvent e) {
//			NewSceneDialog dialog = new NewSceneDialog(ScenePanel.this.scene.getGameDesign());
//			DialogDescriptor dd = new DialogDescriptor(dialog, "Create a new Scene");
//			dd.setButtonListener(dialog);
//			dd.setValid(false);
//			dialog.setDialogDescriptor(dd);
//			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
//			d.setVisible(true);
//		}		
//	}
	
	public class DuplicateSceneAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.actionDuplicateScene.txt"));
		}

		public void actionPerformed(ActionEvent e) {
			NewSceneDialog dialog = new NewSceneDialog(ScenePanel.this.scene);
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(ScenePanel.class, "ScenePanel.actionDuplicateScene.txt"));
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}
	
	public class AddNewTiledLayerAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.actionNewTiledLayer.txt"));
		}

		public void actionPerformed(ActionEvent e) {
			TiledLayerDialog nld = new TiledLayerDialog(ScenePanel.this.scene);
			DialogDescriptor dd = new DialogDescriptor(nld, NbBundle.getMessage(ScenePanel.class, "ScenePanel.actionNewTiledLayer.txt"));
			dd.setButtonListener(nld);
			dd.setValid(false);
			nld.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}
	public class AddNewSpriteAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(ScenePanel.class, "ScenePanel.actionNewSprite.txt"));
		}

		public void actionPerformed(ActionEvent e) {
			SpriteDialog nld = new SpriteDialog(ScenePanel.this.scene);
			DialogDescriptor dd = new DialogDescriptor(nld, NbBundle.getMessage(ScenePanel.class, "ScenePanel.actionNewSprite.txt"));
			dd.setButtonListener(nld);
			dd.setValid(false);
			nld.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}
	public class AddLayerAction extends AbstractAction {
		public static final String PROP_LAYER = "PROP_LAYER"; // NOI18N
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		
		public void actionPerformed(ActionEvent e) {
			Layer layer = (Layer) this.getValue(PROP_LAYER);
			Point position = (Point) this.getValue(PROP_POSITION);
			
			ScenePanel.this.scene.insert(layer, 0);
			ScenePanel.this.scene.setLayerPosition(layer, position, false);
		}
	}
	
	public JComponent getGridButton() {
		return this.gridButton;
	}
	
	private class GridButton extends JComponent implements MouseListener {
		private static final int BORDER = 4;
		public GridButton() {
			ToolTipManager.sharedInstance().registerComponent(this);
			this.addMouseListener(this);
		}
		
		@Override
		public String getToolTipText() {
			return NbBundle.getMessage(ScenePanel.class, "ScenePanel.gridButton.tooltip");
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(UIManager.getColor("Panel.background")); // NOI18N
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.setColor(Color.BLACK);
			int startx = BORDER;
			int starty = BORDER;
			int x = BORDER;
			int y = BORDER;
			int w = this.getWidth() - (3*BORDER);
			int h = this.getHeight() - (3*BORDER);
			if (ScenePanel.this.isShowGrid()) {
				if (ScenePanel.this.isSnapGrid()) {
					g.setColor(Color.RED);
				}
				int offX = w / 3;
				int offY = h / 3;
				
				x += offX;
				g.drawLine(x, starty, x, starty + h);
				x += offX;
				g.drawLine(x, starty, x, starty + h);
				
				y += offY;
				g.drawLine(startx, y, startx + w, y);
				y += offY;
				g.drawLine(startx, y, startx + w, y);
				
			}
			g.setColor(Color.BLACK);
			g.drawRoundRect(startx, starty, w, h, BORDER, BORDER);
		}

		public void mouseClicked(MouseEvent e) {
			if (ScenePanel.this.isShowGrid()) {
				if (ScenePanel.this.isSnapGrid()) {
					ScenePanel.this.setShowGrid(false);
					ScenePanel.this.setSnapGrid(false);
				}
				else {
					ScenePanel.this.setSnapGrid(true);
				}
			}
			else {
				ScenePanel.this.setShowGrid(true);
				ScenePanel.this.setSnapGrid(false);
			}
			this.repaint();
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}
	
	public Ruler getRulerHorizontal() {
		return this.horizontalRuler;
	}
	
	public Ruler getRulerVertical() {
		return this.verticalRuler;
	}
	
	class Ruler extends JComponent {
		
	    public static final int HORIZONTAL = 0;
	    public static final int VERTICAL = 1;
	    
	    private static final int SIZE_H = 24;
	    private static final int SIZE_W = 37;
	    
	    private static final int TICK_MAJOR = 6;
	    private static final int TICK_MINOR = 3;
	    
	    
	    private static final int LOC_INSET = 2;
	    private static final int SHADOW_OFF = 3;
	    public final Color locFgColor = Color.WHITE;
	    public final Color locBgColor = new Color(0, 0, 255, 200);
		public final Color locOutlineColor = Color.WHITE;
	    public final Color locShadowColor = new Color(0, 0, 255, 64);

	    public final Color locFgColorSecondary = Color.WHITE;
	    public final Color locBgColorSecondary = new Color(128, 128, 255, 200);
	    public final Color locOutlineColorSecondary = locFgColorSecondary;
	    public final Color locShadowColorSecondary = new Color(128, 128, 255, 64);
	    
	    
	    private final Color COLOR_MAJOR = Color.BLACK;
	    private final Color COLOR_MINOR = Color.GRAY;
	    private final Font FONT = new Font("SansSerif", Font.PLAIN, 9); // NOI18N 
	    
	    public final Color RulerColor = UIManager.getColor ("Panel.background"); // NOI18N 
		//new Color(230, 230, 255);

		public int orientation;
	    private int increment = 10;
	    private int units = 50;

	    public Ruler(int orientation) {
	        this.orientation = orientation;
	    }

	    @Override
	    public Dimension getPreferredSize() {
			Rectangle bounds = ScenePanel.this.scene.getAllLayersBounds();
			bounds.add(0, 0);
	    	if (this.orientation == HORIZONTAL) {
	    		return new Dimension(bounds.width, SIZE_H);
	    	}
	    	return new Dimension(SIZE_W, bounds.height);
	    }
	    
	    public int getIncrement() {
	        return increment;
	    }

	    protected void paintComponent(Graphics graph) {
	    	Graphics2D g = (Graphics2D) graph;
	        Rectangle clip = g.getClipBounds();

	        Rectangle sceneBounds = ScenePanel.this.scene.getAllLayersBounds();
			sceneBounds.add(0, 0);
	        int offX = sceneBounds.x;
	        int offY = sceneBounds.y;
	        
	        // Fill clipping area blue
	        g.setColor(RulerColor);
	        g.fillRect(clip.x, clip.y, clip.width, clip.height);	        

	        // Do the ruler labels in a small font that's black.
	        g.setFont(FONT);

	        // Some vars we need.
	        
	        int end;
	        int start;
	        int tickLength = 0;
	        String text = null;

	        // Use clipping bounds to calculate first and last tick locations.
	        if (orientation == HORIZONTAL) {
	            start = ((clip.x + offX) / increment) * increment;
	            end = (((clip.x + offX + clip.width) / increment) + 1)
	                  * increment;
	            //System.out.println("HOR start: " + start + " end: " + end);
	        }
	        else {
	            start = ((clip.y + offY) / increment) * increment;
	            end = (((clip.y + offY + clip.height) / increment) + 1)
	                  * increment;
	            //System.out.println("VER start: " + start + " end: " + end);
	        }
	        
	        // ticks and labels
	        for (int i = start; i < end; i += increment) {
	            if (i % units == 0)  {
	            	g.setColor(COLOR_MAJOR);
	                tickLength = TICK_MAJOR;
	                if (i == 0) {
	                	text = " " + Integer.toString(i);
	                }
	                else {
	                	text = Integer.toString(i);
	                }
	            } else {
	            	g.setColor(COLOR_MINOR);
	                tickLength = TICK_MINOR;
	                text = null;
	            }

	            if (tickLength != 0) {
	                if (orientation == HORIZONTAL) {
	                    g.drawLine(i-offX, SIZE_H-1, i-offX, SIZE_H-tickLength-1);
	                    if (text != null) {
	                    	int y = SIZE_H - tickLength - (FONT.getSize()/2);
	                    	int x = (int) (i-offX - (FONT.getStringBounds(text, g.getFontRenderContext()).getWidth() /2));
	                        g.drawString(text, x, y);
	                    }
	                }
	                else {
	                    g.drawLine(SIZE_W-1, i-offY, SIZE_W-tickLength-1, i-offY);
	                    if (text != null) {
	                    	int y = i-offY + (FONT.getSize()/2);
	                        g.drawString(text, 1, y);
	                    }
	                }
	            }
	        }
	        
	        //draw outlines of all selected layers on the rulers
	        //make sure to draw the ScenePanel.this.snapToGridReferenceLayer last (i.e. on top of the other layers)
	        
	        List<Layer> tmp = new ArrayList<Layer>(ScenePanel.this.selectedLayers);
	        if (ScenePanel.this.snapToGridReferenceLayer != null) {
	        	tmp.remove(ScenePanel.this.snapToGridReferenceLayer);
	        	tmp.add(ScenePanel.this.snapToGridReferenceLayer);
	        }
	        
	        for (Layer layer : tmp) {
				Color bgColor = null;
				Color fgColor = null;
				Color shadowColor = null;
				Color outlineColor = null;
				//set painting colors
				if (ScenePanel.this.snapToGridReferenceLayer == null || layer == ScenePanel.this.snapToGridReferenceLayer) {
					bgColor = locBgColor;
					fgColor = locFgColor;
					shadowColor = locShadowColor;
					outlineColor = locOutlineColor;
				}
				else {
					bgColor = locBgColorSecondary;
					fgColor = locFgColorSecondary;
					shadowColor = locShadowColorSecondary;
					outlineColor = locOutlineColorSecondary;					
				}
				
	        	Rectangle bounds = ScenePanel.this.scene.getLayerBounds(layer);
	        	if (orientation == HORIZONTAL) {
	        		int leftLim = clip.x + offX;
	        		int rightLim = clip.x + offX + clip.width;
	        		if (leftLim <= (bounds.x + bounds.width) && (bounds.x + bounds.width) < rightLim) {
	        			//draw layers right outline
	    	        	g.setColor(colorSelection);
	        			g.drawLine(bounds.x + bounds.width - offX, 0, bounds.x + bounds.width - offX, SIZE_H);
	        			//draw layer X position on the right
	        			//if (ScenePanel.this.dragDirectionHorizontal == BorderLayout.EAST) {
	        				String strPosX = Integer.toString(bounds.x + bounds.width);
	        				Rectangle2D strBounds = FONT.getStringBounds(strPosX, g.getFontRenderContext());
	        				LineMetrics lm = FONT.getLineMetrics(strPosX, g.getFontRenderContext());
	        				float ascend = lm.getAscent();
	        				Rectangle strOutline = new Rectangle((int) strBounds.getX() -LOC_INSET, (int) strBounds.getY() -LOC_INSET, (int) strBounds.getWidth() + LOC_INSET*2, (int) strBounds.getHeight() + LOC_INSET*2);
		        			g.setColor(bgColor);
	        				g.fillRoundRect((int) ((bounds.x + bounds.width - offX) - strOutline.getWidth()/2), 0, (int) strOutline.getWidth(), (int) strOutline.getHeight(), LOC_INSET*2, LOC_INSET*2);
		        			g.setColor(shadowColor);
	        				g.fillRoundRect((int) ((bounds.x + bounds.width - offX) - strOutline.getWidth()/2) + SHADOW_OFF, 0 + SHADOW_OFF, (int) strOutline.getWidth(), (int) strOutline.getHeight(), LOC_INSET*2, LOC_INSET*2);
		        			g.setColor(fgColor);
		        			g.drawString(strPosX, (int) ((bounds.x + bounds.width - offX) - strBounds.getWidth()/2), LOC_INSET + ascend);
		        			//hi-lite the reference layer position
		        			if (layer == ScenePanel.this.snapToGridReferenceLayer) {
		        				g.setColor(outlineColor);
		        				g.drawRoundRect((int) ((bounds.x + bounds.width - offX -1) - strOutline.getWidth()/2), 0, (int) strOutline.getWidth() +1, (int) strOutline.getHeight(), LOC_INSET*2, LOC_INSET*2);
		        			}
	        			//}
	        		}
	        		if (leftLim <= bounds.x && bounds.x < rightLim) {
	        			//draw layers left outline
	    	        	g.setColor(colorSelection);
	        			g.drawLine(bounds.x - offX, 0, bounds.x - offX, SIZE_H);
	        			//draw layer X position on the left
	        			//if (ScenePanel.this.dragDirectionHorizontal == BorderLayout.WEST) {
	        				String strPosX = Integer.toString(bounds.x);
	        				Rectangle2D strBounds = FONT.getStringBounds(strPosX, g.getFontRenderContext());
	        				LineMetrics lm = FONT.getLineMetrics(strPosX, g.getFontRenderContext());
	        				float ascend = lm.getAscent();
	        				Rectangle strOutline = new Rectangle((int) strBounds.getX() -LOC_INSET, (int) strBounds.getY() -LOC_INSET, (int) strBounds.getWidth() + LOC_INSET*2, (int) strBounds.getHeight() + LOC_INSET*2);
		        			g.setColor(bgColor);
	        				g.fillRoundRect((int) ((bounds.x - offX) - strOutline.getWidth()/2), 0, (int) strOutline.getWidth(), (int) strOutline.getHeight(), LOC_INSET*2, LOC_INSET*2);
		        			g.setColor(shadowColor);
	        				g.fillRoundRect((int) ((bounds.x - offX) - strOutline.getWidth()/2) + SHADOW_OFF, 0 + SHADOW_OFF, (int) strOutline.getWidth(), (int) strOutline.getHeight(), LOC_INSET*2, LOC_INSET*2);
		        			g.setColor(fgColor);
		        			g.drawString(strPosX, (int) ((bounds.x - offX) - strBounds.getWidth()/2), LOC_INSET + ascend);
		        			//hi-lite the reference layer position
		        			if (layer == ScenePanel.this.snapToGridReferenceLayer) {
			        			g.setColor(outlineColor);
		        				g.drawRoundRect((int) ((bounds.x - offX -1) - strOutline.getWidth()/2), 0, (int) strOutline.getWidth() +1, (int) strOutline.getHeight(), LOC_INSET*2, LOC_INSET*2);
		        			}
	        			//}
	        		}
	        	}
	        	else {
	        		int topLim = clip.y + offY;
	        		int bottomLim = clip.y + offY + clip.height;
	        		if (topLim <= (bounds.y + bounds.height) && (bounds.y + bounds.height) < bottomLim) {
	        			//draw layers bottom outline
	    	        	g.setColor(colorSelection);
	        			g.drawLine(0, bounds.y + bounds.height - offY, SIZE_W, bounds.y + bounds.height - offY);
	        			//if (ScenePanel.this.dragDirectionVertical == BorderLayout.SOUTH) {
		        			//draw layer Y position on the bottom
	        				String strPosY = Integer.toString(bounds.y + bounds.height);
	        				Rectangle2D strBounds = FONT.getStringBounds(strPosY, g.getFontRenderContext());
	        				LineMetrics lm = FONT.getLineMetrics(strPosY, g.getFontRenderContext());
	        				float ascend = lm.getAscent();
	        				Rectangle strOutline = new Rectangle((int) strBounds.getX() -LOC_INSET, (int) strBounds.getY() -LOC_INSET, (int) strBounds.getWidth() + LOC_INSET*2, (int) strBounds.getHeight() + LOC_INSET*2);
		        			g.setColor(bgColor);
		        			g.fillRoundRect(1, bounds.y + bounds.height - offY - strOutline.height/2, strOutline.width, strOutline.height, LOC_INSET*2, LOC_INSET*2);
		        			g.setColor(shadowColor);
		        			g.fillRoundRect(1 + SHADOW_OFF, bounds.y + bounds.height - offY - strOutline.height/2 + SHADOW_OFF, strOutline.width, strOutline.height, LOC_INSET*2, LOC_INSET*2);
		        			g.setColor(fgColor);
		        			g.drawString(strPosY, 1 + LOC_INSET, bounds.y + bounds.height - offY - strOutline.height/2 + ascend +LOC_INSET);
		        			//hi-lite the reference layer position
		        			if (layer == ScenePanel.this.snapToGridReferenceLayer) {
			        			g.setColor(outlineColor);
			        			g.drawRoundRect(0, bounds.y + bounds.height - offY - strOutline.height/2, strOutline.width + 1, strOutline.height, LOC_INSET*2, LOC_INSET*2);
		        			}
	        			//}
	        		}
	        		if (topLim <= bounds.y && bounds.y < bottomLim) {
	        			//draw layers top outline
	    	        	g.setColor(colorSelection);
	        			g.drawLine(0, bounds.y - offY, SIZE_W, bounds.y - offY);
	        			//draw layer Y position on the top
	        			//if (ScenePanel.this.dragDirectionVertical == BorderLayout.NORTH) {
	        				String strPosY = Integer.toString(bounds.y);
	        				Rectangle2D strBounds = FONT.getStringBounds(strPosY, g.getFontRenderContext());
	        				LineMetrics lm = FONT.getLineMetrics(strPosY, g.getFontRenderContext());
	        				float ascend = lm.getAscent();
	        				Rectangle strOutline = new Rectangle((int) strBounds.getX() -LOC_INSET, (int) strBounds.getY() -LOC_INSET, (int) strBounds.getWidth() + LOC_INSET*2, (int) strBounds.getHeight() + LOC_INSET*2);
		        			g.setColor(bgColor);
		        			g.fillRoundRect(1, bounds.y - offY - strOutline.height/2, strOutline.width, strOutline.height, LOC_INSET*2, LOC_INSET*2);
		        			g.setColor(shadowColor);
		        			g.fillRoundRect(1 + SHADOW_OFF, bounds.y - offY - strOutline.height/2 + SHADOW_OFF, strOutline.width, strOutline.height, LOC_INSET*2, LOC_INSET*2);
		        			g.setColor(fgColor);
		        			g.drawString(strPosY, 1 + LOC_INSET, bounds.y - offY - strOutline.height/2 + ascend +LOC_INSET);
		        			//hi-lite the reference layer position
		        			if (layer == ScenePanel.this.snapToGridReferenceLayer) {
			        			g.setColor(outlineColor);
			        			g.drawRoundRect(0, bounds.y - offY - strOutline.height/2, strOutline.width + 1, strOutline.height, LOC_INSET*2, LOC_INSET*2);
		        			}
	        			//}	        			
	        		}
	        	}
			}
	    }
	}

	public boolean isSnapGrid() {
		return this.snapGrid;
	}
	
	public void setSnapGrid(boolean snapGrid) {
		if (this.snapGrid == snapGrid) {
			return;
		}
		this.snapGrid = snapGrid;
		this.repaint();
	}
	
	public boolean isShowGrid() {
		return showGrid;
	}

	public void setShowGrid(boolean showGrid) {
		if (this.showGrid == showGrid) {
			return;
		}
		this.showGrid = showGrid;
		this.repaint();
	}

	public void setGridSizeHorizontal(int unitSize) {
		this.gridX = unitSize;
		this.repaint();
	}
	public void setGridSizeVertical(int unitSize) {
		this.gridY = unitSize;
		this.repaint();
	}
}
