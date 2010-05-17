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
package org.netbeans.modules.vmd.game.editor.sequece;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.SequenceContainerListener;
import org.netbeans.modules.vmd.game.preview.SequencePreviewPanel;
import org.openide.util.NbBundle;

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
	
	private int maxFrameCount = 0;
	private int filmUnitWidth = 0;
	
	public SequenceContainerEditor(SequenceContainer sequenceContainer) {
		this.setBackground(Color.WHITE);
		
		this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SequenceContainerEditor.class, "SequenceContainerEditor.accessible.name"));
		this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SequenceContainerEditor.class, "SequenceContainerEditor.accessible.description"));		

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
			
			SequencePreviewPanel previewOld = (SequencePreviewPanel) this.previewMap.get(oldSeq);
			SequencePreviewPanel previewNew = (SequencePreviewPanel) this.previewMap.get(newSeq);
			
			if (previewOld != null)
				previewOld.setImportant(false);
			if (previewNew != null)
				previewNew.setImportant(true);
		}
	}
	

	private void rebuildUI() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				SequenceContainerEditor.this.removeAll();
				SequenceContainerEditor.this.previewMap.clear();

				for (Sequence s : SequenceContainerEditor.this.sequenceContainer.getSequences()) {
					((SequenceEditingPanel) s.getEditor()).removeSequenceEditingPanelListener(SequenceContainerEditor.this);
				}

				SequenceContainerEditor.this.previewsPanel = new FastScrollPanel();
				SequenceContainerEditor.this.editorsPanel  = new FastScrollPanel();

				SequenceContainerEditor.this.initScrolling();
				SequenceContainerEditor.this.initPreviews();
				SequenceContainerEditor.this.initEditors();

				SequenceContainerEditor.this.setLayout(new BorderLayout());
				SequenceContainerEditor.this.add(SequenceContainerEditor.this.scrollPreviews, BorderLayout.WEST);
				SequenceContainerEditor.this.add(SequenceContainerEditor.this.scrollEditors, BorderLayout.CENTER);

				SequenceContainerEditor.this.validate();
			}
		});
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
		SequencePreviewPanel preview = (SequencePreviewPanel) s.getPreview();
		preview.setSequenceContainer(this.sequenceContainer);
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
		Color colorEven = new Color(245, 245, 255);
		Color colorOdd = Color.WHITE;
		int index = 0;
		for (Sequence s : this.sequenceContainer.getSequences()) {
			Color bg = (index++ % 2 == 0) ? colorEven : colorOdd;
			this.addEditorForSequence(s, bg);
		}
	}
	private void addEditorForSequence(Sequence s, Color c) {
		JPanel p = new JPanel();
		p.setBackground(c);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		SequenceEditingPanel editor = (SequenceEditingPanel) s.getEditor();
		editor.setBackground(c);
		editor.addSequenceEditingPanelListener(this);
		editor.setSequenceContainer(this.sequenceContainer);
		//editor.setViewPort(this.scrollEditors.getViewport());
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
		((SequencePreviewPanel) this.previewMap.get(source.getSequence())).requestPreviewFrame(frameIndex);
	}
	
	public void hiliteLost(SequenceEditingPanel source) {
		//System.out.println("hiliteLost");
		((SequencePreviewPanel) this.previewMap.get(source.getSequence())).requestPreviewFrame(0);
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
	
	
	class RulerHorizontal  extends JComponent {
		private static final int SIZE = 18;
		private static final boolean DEBUG = false;
				
		public RulerHorizontal() {
			ToolTipManager.sharedInstance().registerComponent(this);
		}
		
		public String getToolTipText(MouseEvent event) {
			return NbBundle.getMessage(SequenceContainerEditor.class, "SequenceContainerEditor.ruler.tooltip", this.getColumnAtPoint(event.getPoint()));
		}
		
		public Dimension getPreferredSize() {
			Dimension size = SequenceContainerEditor.this.editorsPanel.getPreferredSize();
			size.height = SIZE;
			return size;
		}
		protected void paintComponent(Graphics graphincs) {
			Graphics2D g = (Graphics2D) graphincs;
			
			Rectangle rect = g.getClipBounds();
			g.setColor(Color.WHITE);
			g.fill(rect);
			
			if (DEBUG) System.out.println("RulerHorizontal.repaint " + rect); // NOI18N 
			
			int unit = 1 + SequenceContainerEditor.this.filmUnitWidth;
			
			for (int x = (rect.x / unit) * unit; x <= rect.x + rect.width; x+= unit) {
				int col =  x / unit;
				
				if (col >= SequenceContainerEditor.this.maxFrameCount) {
					break;
				}
				
				boolean raised = true;
				//if (DEBUG) System.out.println("paint col: " + col);
				g.setColor(new Color(240, 238, 230));
				
				g.fill3DRect(x, 0, unit, SIZE, raised);
				
			}
		}
		
		private int getColumnAtPoint(Point point) {
			return this.getColumnAtCoordinates(point.x, point.y);
		}
		private int getColumnAtCoordinates(int x, int y) {
			return (x - 1) / (SequenceContainerEditor.this.filmUnitWidth + 1);
		}
				
	}
		

	
//	public static void main(String[] args) {
//		GlobalRepository gameDesign = new GlobalRepository(null);
//		URL imageURL = SequenceEditingPanel.class.getResource("../../view/main/res/color_tiles.png"); // NOI18N 
//		ImageResource imgRes = gameDesign.getImageResource(imageURL, "Path"); // NOI18N 
//		
//		Sequence s1 = imgRes.createSequence("seq1", 5, 20, 20, true);
//		s1.setFrame((StaticTile) imgRes.getTile(1, 20, 20, true), 0);
//		s1.setFrame((StaticTile) imgRes.getTile(2, 20, 20, true), 1);
//		s1.setFrame((StaticTile) imgRes.getTile(3, 20, 20, true), 2);
//		
//		Sequence s2 = imgRes.createSequence("seq2", 5, 20, 20, true);
//		s2.setFrame((StaticTile) imgRes.getTile(1, 20, 20, true), 0);
//		s2.setFrame((StaticTile) imgRes.getTile(2, 20, 20, true), 1);
//		s2.setFrame((StaticTile) imgRes.getTile(3, 20, 20, true), 2);
//		
//		Sequence s3 = imgRes.createSequence("seq3", 3, 20, 20, true);
//		s3.setFrame((StaticTile) imgRes.getTile(4, 20, 20, true), 0);
//		s3.setFrame((StaticTile) imgRes.getTile(5, 20, 20, true), 1);
//		s3.setFrame((StaticTile) imgRes.getTile(6, 20, 20, true), 2);
//		
//		Sequence s4 = imgRes.createSequence("seq4", 3, 20, 20, true);
//		s4.setFrame((StaticTile) imgRes.getTile(2, 20, 20, true), 0);
//		s4.setFrame((StaticTile) imgRes.getTile(3, 20, 20, true), 1);
//		s4.setFrame((StaticTile) imgRes.getTile(4, 20, 20, true), 2);
//		
//		AnimatedTile at = imgRes.createAnimatedTile("AT1", s1);
//		at.append(s2);
//		at.append(s3);
//		at.append(s4);
//		
//		SequenceContainerEditor sep = new SequenceContainerEditor(at);
//		
//		JFrame frame = new JFrame("Test SequenceEditing Panel"); // NOI18N 
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setSize(new Dimension(900, 600));
//		frame.getContentPane().setLayout(new BorderLayout());
//		frame.getContentPane().add(sep, BorderLayout.CENTER);
//		MainView.center(frame);
//		frame.setVisible(true);
//		frame.getContentPane().doLayout();
//	}
		
}
