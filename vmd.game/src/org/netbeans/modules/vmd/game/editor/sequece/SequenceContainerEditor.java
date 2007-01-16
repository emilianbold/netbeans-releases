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
package org.netbeans.modules.vmd.game.editor.sequece;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.SequenceContainerListener;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.preview.SequencePreviewPanelSidebar;
import org.netbeans.modules.vmd.game.view.main.MainView;

/**
 *
 * @author kherink
 */
public class SequenceContainerEditor extends JPanel implements SequenceEditingPanelListener, SequenceContainerListener, PropertyChangeListener {
	
	private SequenceContainer sequenceContainer;
	
	/** Maps sequences and their previews */
	private Map<Sequence, JComponent> previewMap = new HashMap();
	
	private JPanel previewsPanel;
	private JScrollPane scrollPreviews;
	
	private JPanel editorsPanel;
	private JScrollPane scrollEditors;
	
	private int syncComponentHeight = 0;
	
	public SequenceContainerEditor(SequenceContainer sequenceContainer) {
		this.setBackground(Color.WHITE);
		
		this.sequenceContainer = sequenceContainer;
		this.sequenceContainer.addSequenceContainerListener(this);
		this.sequenceContainer.addPropertyChangeListener(this);
		
		this.setSyncComponentHeight();
		this.rebuildUI();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SequenceContainer.PROPERTY_DEFAULT_SEQUENCE)) {
			Sequence oldSeq = (Sequence) evt.getOldValue();
			Sequence newSeq = (Sequence) evt.getNewValue();
			
			SequencePreviewPanelSidebar previewOld = (SequencePreviewPanelSidebar) this.previewMap.get(oldSeq);
			SequencePreviewPanelSidebar previewNew = (SequencePreviewPanelSidebar) this.previewMap.get(newSeq);
			
			if (previewOld != null)
				previewOld.setImportant(false);
			if (previewNew != null)
				previewNew.setImportant(true);
		}
	}
	

	private void rebuildUI() {
		this.removeAll();
		this.previewMap.clear();
		
		for (Sequence s : this.sequenceContainer.getSequences()) {
			((SequenceEditingPanel) s.getEditor()).removeSequenceEditingPanelListener(this);
		}
		
		this.previewsPanel = new FastScrollPanel();
		this.editorsPanel  = new FastScrollPanel();
		
		this.initPreviews();
		this.initEditors();
		this.initScrolling();
		
		this.setLayout(new BorderLayout());
		this.add(this.scrollPreviews, BorderLayout.WEST);
		this.add(this.scrollEditors, BorderLayout.CENTER);
		
		this.validate();
	}
	
	private void setSyncComponentHeight() {
		Sequence s = this.sequenceContainer.getDefaultSequence();
		JComponent preview = s.getPreview();
		JComponent editor = s.getEditor();
		this.syncComponentHeight = Math.max(preview.getPreferredSize().height, editor.getPreferredSize().height);
	}
	
	private void initPreviews() {
		this.previewsPanel.setBackground(Color.WHITE);
		this.previewsPanel.setLayout(new BoxLayout(this.previewsPanel, BoxLayout.Y_AXIS));
		for (Sequence s : this.sequenceContainer.getSequences()) {
			this.addPreviewForSequence(s);
		}
	}
	private void addPreviewForSequence(Sequence s) {
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		SequencePreviewPanelSidebar preview = (SequencePreviewPanelSidebar) s.getPreview();
		if (this.sequenceContainer.getDefaultSequence() == s) {
			preview.setImportant(true);
		}
		else {
			preview.setImportant(false);
		}
		p.add(preview);
		
		Dimension minSize = new Dimension(0, this.syncComponentHeight);
		Dimension prefSize = new Dimension(0, this.syncComponentHeight);
		Dimension maxSize = new Dimension(0, this.syncComponentHeight); //(0, Short.MAX); would make it resizable
		p.add(new Box.Filler(minSize, prefSize, maxSize));
		
		this.previewsPanel.add(p);
		this.previewMap.put(s, preview);
	}
	
	private void initEditors() {
		this.editorsPanel.setBackground(Color.WHITE);
		this.editorsPanel.setLayout(new BoxLayout(this.editorsPanel, BoxLayout.Y_AXIS));
		for (Sequence s : this.sequenceContainer.getSequences()) {
			this.addEditorForSequence(s);
		}
	}
	private void addEditorForSequence(Sequence s) {
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		SequenceEditingPanel editor = (SequenceEditingPanel) s.getEditor();
		editor.addSequenceEditingPanelListener(this);
		editor.setSequenceContainer(this.sequenceContainer);
		p.add(editor);
		
		Dimension minSize = new Dimension(0, this.syncComponentHeight);
		Dimension prefSize = new Dimension(0, this.syncComponentHeight);
		Dimension maxSize = new Dimension(0, this.syncComponentHeight); //(0, Short.MAX); would make it resizable
		p.add(new Box.Filler(minSize, prefSize, maxSize));
		
		this.editorsPanel.add(p);
	}
	
	
	private void initScrolling() {
		//Previews are synchronized with the editors, i.e. they scroll the same way that editors scroll so no need for scroll bars
		this.scrollPreviews = new JScrollPane(this.previewsPanel);
		this.scrollPreviews.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPreviews.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		this.scrollPreviews.setBorder(BorderFactory.createEmptyBorder());
		this.scrollPreviews.getViewport().setBackground(Color.WHITE);
		
		this.scrollEditors = new JScrollPane(this.editorsPanel);
		this.scrollEditors.setBorder(BorderFactory.createEmptyBorder());
		this.scrollEditors.getViewport().setBackground(Color.WHITE);
		
		//listen for changes in editors scrollpane and update the the previews scroll pane to reflect them
		this.scrollEditors.getViewport().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Point pPrev = SequenceContainerEditor.this.scrollPreviews.getViewport().getViewPosition();
				Point pEdit = SequenceContainerEditor.this.scrollEditors.getViewport().getViewPosition();
				Point n = new Point(pPrev.x, pEdit.y);
				SequenceContainerEditor.this.scrollPreviews.getViewport().setViewPosition(n);
			}
		});
	}
	
	//-------------- SequenceEditingPanelListener -----------------
	
	public void frameHilited(SequenceEditingPanel source, int frameIndex) {
		//System.out.println("frameHilited " + frameIndex);
		((SequencePreviewPanelSidebar) this.previewMap.get(source.getSequence())).requestPreviewFrame(frameIndex);
	}
	
	public void hiliteLost(SequenceEditingPanel source) {
		//System.out.println("hiliteLost");
		((SequencePreviewPanelSidebar) this.previewMap.get(source.getSequence())).requestPreviewFrame(0);
	}
	
	public void frameSelectionChange(SequenceEditingPanel source, int[] selectedFrameIdices) {
	}
	
	
	//------------- SequenceContainerListener -----------------------
	public void sequenceAdded(SequenceContainer source, Sequence sequence, int index) {
		this.rebuildUI();
	}
	
	public void sequenceRemoved(SequenceContainer source, Sequence sequence, int index) {
		this.rebuildUI();
	}
	
	public void sequenceMoved(SequenceContainer source, Sequence sequence, int indexOld, int indexNew) {
	}
	
	
	
	private static class FastScrollPanel extends JPanel implements Scrollable {
		public Dimension getPreferredScrollableViewportSize() {
			return this.getPreferredSize();
		}
		
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}
		
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}
		
		public boolean getScrollableTracksViewportWidth() {
			return false;
		}
		
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
		
	}
	
	public static void main(String args[]) {
		Scene scene = GlobalRepository.getInstance().createScene("scene1");
		
		URL urlRock = MainView.class.getResource("res/color_tiles.png");
		ImageResource imgRock = GlobalRepository.getInstance().getImageResource(urlRock, 20, 20);

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
		
		JFrame f = new JFrame("TEST");
		f.setSize(300,200);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SequenceContainerEditor editor = new SequenceContainerEditor(rock);
		//JScrollPane scroll = new JScrollPane(editor);
		//scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(editor, BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);
		
		try {	
			Thread.sleep(5000);
			rock.remove(medium);
			Thread.sleep(5000);
			rock.append(medium);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		
	}

}
