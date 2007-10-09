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
package org.netbeans.modules.vmd.game.view.main;

import org.netbeans.modules.vmd.game.editor.grid.ResourceImageEditor;
import org.netbeans.modules.vmd.game.editor.tiledlayer.TiledLayerEditor;
import org.netbeans.modules.vmd.game.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.Frame;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.vmd.game.model.Editable.ImageResourceInfo;

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
	
	public MainView() {
		this.initComponents();
		this.initLayout();
	}

	private void initComponents() {
		this.rootPanel = new JPanel();
		this.rootPanel.setLayout(new BorderLayout());
		this.rootPanel.setBackground(Color.WHITE);

		//editor components
		this.mainEditorPanel = new JPanel();
		this.mainEditorPanel.setBorder(null);
		this.mainEditorPanel.setBackground(Color.WHITE);
		
		this.resourceImageView = new ResourceImageEditor();

		this.editorSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
		this.editorSplit.setDividerLocation(400);
		this.editorSplit.setResizeWeight(1.0);
		this.editorSplit.setOneTouchExpandable(false);
		this.editorSplit.setDividerSize(5);

	}
	
	private void initLayout() {		
		this.mainEditorPanel.setLayout(new BorderLayout());
		
		//layout the main window
		this.mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, this.previewExplorerSplit, this.mainEditorPanel);
		this.mainSplit.setDividerLocation(230);
		this.mainSplit.setDividerSize(5);
		this.mainSplit.setOneTouchExpandable(false);
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
		if (DEBUG) System.out.println("MainView - paint tile changed " + tile); // NOI18N
		if (tile == null) {
			return;
		}
		JComponent editor = this.currentEditable.getEditor();
		if (editor instanceof TiledLayerEditor) {
			TiledLayerEditor tiledLayerEditor = (TiledLayerEditor) editor;
			tiledLayerEditor.setPaintTile(tile);
		}
	}


//--------GlobalRepositoryListener	
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

    public void imageResourceAdded(ImageResource imageResource) {
    }

	public void requestEditing(Editable editable) {
		if (this.currentEditable == editable) {
			return;
		}
		this.currentEditable = editable;
		ImageResourceInfo resource = this.currentEditable.getImageResourceInfo();
		JComponent editor = this.currentEditable.getEditor();

		this.mainEditorPanel.removeAll();
		
		if (resource != null) {
			this.resourceImageView.setImageResourceInfo(resource);
			this.editorSplit.setTopComponent(editor);
			this.editorSplit.setBottomComponent(this.resourceImageView);
			this.editorSplit.setOneTouchExpandable(false);
			this.editorSplit.setDividerSize(5);
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


	
	public void requestPreview(Previewable previewable) { // NOI18N
		if (DEBUG) System.out.println("Setting preview for: " + previewable);
		JComponent previewComponent = previewable.getPreview();
		if (previewComponent != null) {
			if (DEBUG) System.out.println("PREVIEW REQUESTED COMPONENT FOR: " + previewable.getClass().getName()); // NOI18N
			return;
		}
		if (DEBUG) System.out.println("!!! CREATE PREVIEW COMPONENT FOR: " + previewable.getClass().getName() + " by implementing Previewable.getPreview() RIGHT NOW !!!"); // NOI18N
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
		MainView view  = new MainView();
		JFrame frame = new JFrame("Zero Effort Game Builder"); // NOI18N
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(900, 600));
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(view.getRootComponent(), BorderLayout.CENTER);
		MainView.center(frame);
		frame.setVisible(true);
		frame.getContentPane().doLayout();
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
