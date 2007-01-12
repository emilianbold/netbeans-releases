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
package org.netbeans.modules.vmd.game.view.main;

import org.netbeans.modules.vmd.game.editor.grid.ResourceImageEditor;
import org.netbeans.modules.vmd.game.editor.tiledlayer.TiledLayerEditor;
import org.netbeans.modules.vmd.game.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.Frame;
import java.net.URL;
import javax.swing.event.EventListenerList;

public class MainView implements GlobalRepositoryListener, EditorManager {

	public static final boolean DEBUG = false;
	
	EventListenerList listenerList = new EventListenerList();

	private Editable currentEditable;
	
	private JPanel rootPanel;
	private JSplitPane mainSplit;
	private JSplitPane explorerSplit;
	private JSplitPane previewExplorerSplit;
	private JSplitPane editorSplit;
	
	//the right panel contains editor and optional resource grid
	private JPanel mainEditorPanel;
	private ResourceImageEditor resourceImageView;

	private static MainView instance;
	
	public static MainView getInstance() {
		return instance == null ? instance = new MainView() : instance;
	}
	
	private MainView() {
		GlobalRepository.getInstance().addGlobalRepositoryListener(this);
		this.initComponents();
		this.initLayout();
		loadPreviews();
	}

	private void initComponents() {
		this.rootPanel = new JPanel();
		this.rootPanel.setLayout(new BorderLayout());
		

		//editor components
		this.mainEditorPanel = new JPanel();
		this.mainEditorPanel.setLayout(new GridBagLayout());
		this.mainEditorPanel.setBorder(null);
		
		this.resourceImageView = new ResourceImageEditor();

		this.editorSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
		this.editorSplit.setDividerLocation(400);
		this.editorSplit.setResizeWeight(1.0);
		this.editorSplit.setOneTouchExpandable(true);
		this.editorSplit.setDividerSize(10);

	}
	
	private void initLayout() {
		
		//layout the main edit panel
		this.mainEditorPanel.setLayout(new GridLayout(1, 1));
		
		//layout the main window
		this.mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, this.previewExplorerSplit, this.mainEditorPanel);
		this.mainSplit.setDividerLocation(230);
		this.mainSplit.setDividerSize(10);
		this.mainSplit.setOneTouchExpandable(true);
		this.mainSplit.setResizeWeight(0.0);

		
		//TODO: here implement the toolbar panel
		//this.rootPanel.add(this.toolbarPanel);
		
		this.rootPanel.add(this.mainEditorPanel, BorderLayout.CENTER);
		
	}
	
	private class EditorDisposer implements Runnable {
		public void run() {
			MainView.this.currentEditable = null;
			MainView.this.mainEditorPanel.removeAll();
			MainView.this.mainEditorPanel.repaint();
			MainView.this.mainEditorPanel.validate();
		}
	}
	
	public void paintTileChanged(Tile tile) {
		if (DEBUG) System.out.println("MainView - paint tile changed " + tile.toString());
		JComponent editor = this.currentEditable.getEditor();
		if (editor instanceof TiledLayerEditor) {
			TiledLayerEditor tiledLayerEditor = (TiledLayerEditor) editor;
			tiledLayerEditor.setPaintTile(tile);
		}
	}


	
	public void sceneAdded(Scene scene, int index) {
		this.requestEditing(scene);
	}

	public void sceneRemoved(Scene scene, int index) {
		this.closeEditor(scene);
	}

	public void tiledLayerAdded(final TiledLayer tiledLayer, int index) {
		this.requestEditing(tiledLayer);
	}

	public void tiledLayerRemoved(TiledLayer tiledLayer, int index) {
		this.closeEditor(tiledLayer);
	}

	public void spriteAdded(Sprite sprite, int index) {
		if (this.rootPanel.isShowing())
			this.requestEditing(sprite);
	}

	public void spriteRemoved(Sprite sprite, int index) {
		this.closeEditor(sprite);
	}


	public void requestEditing(Editable editable) {
		if (this.currentEditable == editable)
			return;
		this.currentEditable = editable;
		ImageResource resource = this.currentEditable.getImageResource();
		JComponent editor = this.currentEditable.getEditor();

		this.mainEditorPanel.removeAll();
		
		if (resource != null) {
			this.resourceImageView.setImageResource(resource);
			this.editorSplit.setTopComponent(editor);
			this.editorSplit.setBottomComponent(this.resourceImageView);
			this.editorSplit.setOneTouchExpandable(true);
			this.editorSplit.setDividerSize(10);
			this.mainEditorPanel.add(MainView.this.editorSplit);
		}
		else {
			this.mainEditorPanel.add(editor);
		}
		this.mainEditorPanel.repaint();
		this.mainEditorPanel.validate();
		
		this.fireEditingChanged(this.currentEditable);
	}

	private void fireEditingChanged(Editable editable) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == EditorManagerListener.class) {
				((EditorManagerListener) listeners[i+1]).editing(editable);
			}
		}
	}	


	
	public void requestPreview(Previewable previewable) {
		if (DEBUG) System.out.println("Setting preview for: " + previewable);
		JComponent previewComponent = previewable.getPreview();
		if (previewComponent != null) {
			if (DEBUG) System.out.println("PREVIEW REQUESTED COMPONENT FOR: " + previewable.getClass().getName());
			return;
		}
		if (DEBUG) System.out.println("!!! CREATE PREVIEW COMPONENT FOR: " + previewable.getClass().getName() + " by implementing Previewable.getPreview() RIGHT NOW !!!");					
	}
	
	public void closeEditor(Editable editable) {
		if (currentEditable == editable) {
			SwingUtilities.invokeLater(new EditorDisposer());
		}
	}

	public static void center(JFrame frame) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point center = ge.getCenterPoint();
        Rectangle bounds = ge.getMaximumWindowBounds();
        int w = Math.min(frame.getWidth(), bounds.width);
        int h = Math.min(frame.getHeight(), bounds.height);
        int x = center.x - w/2, y = center.y - h/2;
        frame.setBounds(x, y, w, h);
        if (w == bounds.width && h == bounds.height)
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.validate();
   }

	
	public static void main(String[] args) {
		MainView view  = MainView.getInstance();
		JFrame frame = new JFrame("Zero Effort Game Builder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(900, 600));
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(view.getRootComponent(), BorderLayout.CENTER);
		MainView.center(frame);
		frame.setVisible(true);
		frame.getContentPane().doLayout();
	}

	private static void loadPreviews() {
		Scene scene = GlobalRepository.getInstance().createScene("scene1");
		
		URL urlRock = MainView.class.getResource("res/color_tiles.png");
		ImageResource imgRock = GlobalRepository.getImageResource(urlRock, 20, 20);
		Sprite rock = scene.createSprite("rock2", imgRock, 5);
		Sequence def = rock.getDefaultSequence();
		def.setFrame((StaticTile) imgRock.getTile(1), 0);
		def.setFrame((StaticTile) imgRock.getTile(3), 1);
		def.setFrame((StaticTile) imgRock.getTile(5), 2);
		def.setFrame((StaticTile) imgRock.getTile(7), 3);
		def.setFrame((StaticTile) imgRock.getTile(9), 4);
		def.setName("Slow");

		Sequence fast = rock.createSequence("Fast", 5);
		fast.setFrame((StaticTile) imgRock.getTile(1), 0);
		fast.setFrame((StaticTile) imgRock.getTile(3), 1);
		fast.setFrame((StaticTile) imgRock.getTile(5), 2);
		fast.setFrame((StaticTile) imgRock.getTile(7), 3);
		fast.setFrame((StaticTile) imgRock.getTile(9), 4);
		fast.setFrameMs(50);
		
		Sequence medium = rock.createSequence("Medium", 3);
		medium.setFrame((StaticTile) imgRock.getTile(1), 0);
		medium.setFrame((StaticTile) imgRock.getTile(5), 1);
		medium.setFrame((StaticTile) imgRock.getTile(9), 2);
		medium.setFrameMs(120);
		
		Sequence back = rock.createSequence("Back", 5);
		back.setFrame((StaticTile) imgRock.getTile(1), 4);
		back.setFrame((StaticTile) imgRock.getTile(3), 3);
		back.setFrame((StaticTile) imgRock.getTile(5), 2);
		back.setFrame((StaticTile) imgRock.getTile(7), 1);
		back.setFrame((StaticTile) imgRock.getTile(9), 0);
		back.setFrameMs(120);		
		
		URL urlBunny = MainView.class.getResource("res/color_tiles.png");
		ImageResource bunnyImgRes = GlobalRepository.getImageResource(urlBunny, 20, 20);
		Sprite bunny = scene.createSprite("bunny2", bunnyImgRes, 8);
		Sequence bunnyDefSeq = bunny.getDefaultSequence();
		bunnyDefSeq.setFrame((StaticTile) bunnyImgRes.getTile(1), 0);
		bunnyDefSeq.setFrame((StaticTile) bunnyImgRes.getTile(2), 1);
		bunnyDefSeq.setFrame((StaticTile) bunnyImgRes.getTile(3), 2);
		bunnyDefSeq.setFrame((StaticTile) bunnyImgRes.getTile(4), 3);
		bunnyDefSeq.setFrame((StaticTile) bunnyImgRes.getTile(5), 4);
		bunnyDefSeq.setFrame((StaticTile) bunnyImgRes.getTile(6), 5);
		bunnyDefSeq.setFrame((StaticTile) bunnyImgRes.getTile(7), 6);
		bunnyDefSeq.setFrame((StaticTile) bunnyImgRes.getTile(8), 7);
		bunnyDefSeq.setFrameMs(100);
		
		Sequence shorter = bunny.createSequence("shorter", 4);
		shorter.setFrame((StaticTile) bunnyImgRes.getTile(3), 0);
		shorter.setFrame((StaticTile) bunnyImgRes.getTile(5), 1);
		shorter.setFrame((StaticTile) bunnyImgRes.getTile(7), 2);
		shorter.setFrame((StaticTile) bunnyImgRes.getTile(9), 3);
		shorter.setFrameMs(130);
		
		URL urlTrees = MainView.class.getResource("res/color_tiles.png");
		ImageResource treesImgRes = GlobalRepository.getImageResource(urlTrees, 20, 20);
		
		AnimatedTile pineTree = treesImgRes.createAnimatedTile("pineTree2", 1);
		pineTree.getDefaultSequence().addFrame((StaticTile) treesImgRes.getTile(2));
		pineTree.getDefaultSequence().addFrame((StaticTile) treesImgRes.getTile(3));
		
		AnimatedTile palmTree = treesImgRes.createAnimatedTile("palmTree2", 4);
		palmTree.getDefaultSequence().addFrame((StaticTile) treesImgRes.getTile(5));
		
		TiledLayer trees = scene.createTiledLayer("trees2", treesImgRes, 8, 8);
		trees.setTileAt(palmTree.getIndex(), 0, 0);
		trees.setTileAt(pineTree.getIndex(), 7, 7);
		
		URL urlTiles = MainView.class.getResource("res/color_tiles.png");
		ImageResource imgRes = GlobalRepository.getImageResource(urlTiles, 20, 20);
		
		TiledLayer grass = scene.createTiledLayer("grass2", GlobalRepository.getImageResource(urlTiles, 20, 20), 10, 20);
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 20; j++) {
				grass.setTileAt(3, i, j);
			}
		}

		TiledLayer water = scene.createTiledLayer("water2", GlobalRepository.getImageResource(urlTiles, 20, 20), 4, 3);
		water.setTileAt(4, 0, 0);
		water.setTileAt(4, 0, 1);
		water.setTileAt(4, 0, 2);
		water.setTileAt(4, 1, 0);
		water.setTileAt(4, 1, 1);
		water.setTileAt(4, 1, 2);
		water.setTileAt(4, 2, 0);
		water.setTileAt(1, 2, 1);
		water.setTileAt(5, 2, 2);
		water.setTileAt(5, 3, 0);
		water.setTileAt(6, 3, 1);
		water.setTileAt(7, 3, 2);

		TiledLayer things = scene.createTiledLayer("things2", GlobalRepository.getImageResource(urlTiles, 20, 20), 7, 7);
		things.setTileAt(7, 0, 5);
		things.setTileAt(5, 0, 6);
		things.setTileAt(4, 2, 0);
		things.setTileAt(3, 4, 6);
		things.setTileAt(2, 5, 2);
		things.setTileAt(1, 6, 6);
		
		
		scene.setLayerPosition(rock, new Point(300, 22));
		scene.setLayerPosition(things, new Point(-210, -110));
		scene.setLayerPosition(water, new Point(200, 300));
		scene.setLayerPosition(grass, new Point(0, 0));
		scene.setLayerPosition(bunny, new Point(10, 10));
		scene.move(things, 0);
		scene.move(water, 1);
	}

	public JComponent getRootComponent() {
		return this.rootPanel;
	}

	public void addEditorManagerListener(EditorManagerListener l) {
		this.listenerList.add(EditorManagerListener.class, l);
	}

	public void removeEditorManagerListener(EditorManagerListener l) {
		this.listenerList.remove(EditorManagerListener.class, l);
	}

	public Editable getCurrentEditable() {
		return this.currentEditable;
	}

}
