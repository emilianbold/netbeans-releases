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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.ToolTipManager;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.SequenceListener;
import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TileDataFlavor;
import org.openide.util.NbBundle;

/*
Implement DnD of multiple frames between sequences (give option to either insert of overwrite)
Implement DnD of multiple frames from image resource

Repaint only individual frames as needed instead of always the whole sequence - performance sucks!!!!
When dropping a frame at the end of sequence scroll to make it visible
When having more than one frame selected only play the selected frames in the preview
*/

/**
 * @author kherink
 */
 public class SequenceEditingPanel extends JComponent implements Scrollable, MouseMotionListener, MouseListener, SequenceListener {
	
	public static final boolean DEBUG = false;

	private static final int BOUNDARY_MIN = 10;
	private static final int SEPARATOR_WIDTH_MIN = 15;
	
	
	private static final Color COLOR_SEPARATOR_VERTICAL = new Color(204, 218, 228);
	private static final Color COLOR_SEPARATOR_HORIZONTAL = COLOR_SEPARATOR_VERTICAL; //new Color(162, 162, 178);
    private Color backgroundColor = Color.WHITE;

	private static final int NONE = -1;
	
	private Sequence sequence;
	private SequenceContainer sequenceContainer;
	
	private JViewport viewPort;
	
	private int frameWidth;
	private int frameHeight;

	private int unitWidth;
	private int unitHeight;

	private int separatorWidth;
	private int separatorHeight;
	
	private int outlineWidth;
	private int outlineHeight;
	
	private boolean centerVertically;
	private boolean centerHorizontally;
	
	private Point start = new Point(0, 0);
	
	private Point transOutlineToFrame;
	private Point transFrameToSeparator;
	private Point transOutlineToSeparator;
	private Point transSeparatorToOutline;
	
	private int filmUnitWidth;
		
	private FrameSelectionManager selection;
	
	private int hilitedColumn = NONE;
	
	EventListenerList listenerList = new EventListenerList();


	public SequenceEditingPanel(Sequence sequence) {
		this.setDoubleBuffered(true);
		this.sequence = sequence;
		this.selection = new FrameSelectionManager();
		this.sequence.addSequenceListener(this);
		this.sequence.addSequenceListener(this.selection);
		
		this.frameWidth = this.sequence.getFrameWidth();
		this.frameHeight = this.sequence.getFrameHeight();
		
		this.unitWidth = Math.max(this.frameWidth / 5 ,10);
		this.unitHeight = Math.max(this.frameHeight / 5, 10);

		this.outlineWidth = this.frameWidth + unitWidth * 2;
		this.outlineHeight = this.frameHeight + unitHeight * 2;
		
		this.separatorWidth = Math.max(unitWidth, SEPARATOR_WIDTH_MIN);
		
		this.transSeparatorToOutline = new Point(separatorWidth + unitWidth, 0);
		this.transOutlineToFrame = new Point(unitWidth, unitHeight);
		this.transFrameToSeparator = new Point(this.frameWidth + unitWidth * 2, -unitHeight);
		this.transOutlineToSeparator = new Point(unitWidth, 0);
		
		this.filmUnitWidth = this.transSeparatorToOutline.x + this.transOutlineToFrame.x + this.transFrameToSeparator.x;
		
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		
		//DnD
		DropTarget dropTarget = new DropTarget(this, new SequenceDropTargetListener());
		dropTarget.setActive(true);
		this.setDropTarget(dropTarget);

		DragSource dragSource = new DragSource();
		DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, new DGL());
	
		ToolTipManager.sharedInstance().registerComponent(this);		
	}
	
	public void setBackground(Color color) {
		this.backgroundColor = color;
	}
	

	private class DGL extends DragSourceAdapter implements DragGestureListener {

		public void dragGestureRecognized(DragGestureEvent dge) {
			Point dragOrigin = dge.getDragOrigin();
			int col = SequenceEditingPanel.this.getColumnForPoint(dragOrigin);
			int frame = SequenceEditingPanel.this.getFrameForColumn(col);
		}
		
		public void dragDropEnd(DragSourceDropEvent dsde) {
			super.dragDropEnd(dsde);
			if (dsde.getDropSuccess()) {
				if (DEBUG) System.out.println("Drop successful"); // NOI18N
			}
			else {
				if (DEBUG) System.out.println("Drop unsuccessful"); // NOI18N			
			}
		}
	
	}
	
	public void setSequenceContainer(SequenceContainer sequenceContainer) {
		this.sequenceContainer = sequenceContainer;
	}
	
	public Sequence getSequence() {
		return this.sequence;
	}
	
    public String getToolTipText(MouseEvent event) {
		int col = this.getColumnForPoint(event.getPoint());
		int frame = this.getFrameForColumn(col);
		if (frame == -1)
			return null;
		return NbBundle.getMessage(
				SequenceEditingPanel.class, 
				"SequenceEditingPanel.frame.tooltip", 
				new Object[] {frame, this.sequence.getFrame(frame).getIndex()});
//		return "Index: " + frame + ", tile: " + this.sequence.getFrame(frame).getIndex();
    }

	//DnD implementation
	private class SequenceDropTargetListener implements DropTargetListener {
		public void dragEnter(DropTargetDragEvent dtde) {
			if (DEBUG) System.out.println("dragEnter"); // NOI18N
		}
        public void dragOver(DropTargetDragEvent dtde) {
			SequenceEditingPanel.this.updateHiLite(dtde.getLocation());
        }
        public void dropActionChanged(DropTargetDragEvent dtde) {
			if (DEBUG) System.out.println("dropActionChanged"); // NOI18N
        }
        public void dragExit(DropTargetEvent dte) {
			if (DEBUG) System.out.println("dragExit"); // NOI18N
			SequenceEditingPanel.this.hilitedColumn = SequenceEditingPanel.NONE;
			SequenceEditingPanel.this.repaint();
        }
        public void drop(DropTargetDropEvent dtde) {
			Point dropPoint = dtde.getLocation();
			if (DEBUG) System.out.println("Start drop @: " + dropPoint); // NOI18N
			Transferable transferable = dtde.getTransferable();
			try {
				TileDataFlavor tileFlavor = new TileDataFlavor();
				if (transferable.isDataFlavorSupported(tileFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					List<Tile> tiles = (List<Tile>) transferable.getTransferData(tileFlavor);
					int col = SequenceEditingPanel.this.getColumnForPoint(dropPoint);
					//if dropping on separator i want to insert
					if (col % 2 ==0) {
						for (int i = 0; i < tiles.size(); i++) {
							SequenceEditingPanel.this.sequence.insertFrame((StaticTile) tiles.get(i), (col + (i*2)) / 2);
						}
					}
					//if dropping on frame i want to overwrite it
					else {
						for (Tile tile : tiles) {
						SequenceEditingPanel.this.sequence.setFrame((StaticTile) tile, SequenceEditingPanel.this.getFrameForColumn(col));
						}
					}
					dtde.dropComplete(true);
				}
				else {
					if (DEBUG) System.out.println("NOT a Tile :("); // NOI18N
					dtde.dropComplete(false);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				dtde.dropComplete(false);
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
				dtde.dropComplete(false);
			} catch (IOException e) {
				e.printStackTrace();
				dtde.dropComplete(false);
			}
		}
	}
	

	private void updateHiLite(Point p) {
		if (DEBUG) System.out.println("SET Hi-Lite on col " + this.getColumnForPoint(p)); // NOI18N
		//TODO : set a clip when optimizing preformance
		this.handleMouseMoveOver(p);
	}
		
	private class ResizeListener extends ComponentAdapter {
        public void componentResized(ComponentEvent e) {
			if (DEBUG && SequenceEditingPanel.this.viewPort != null) System.out.println(">> componentResized: "  // NOI18N
					+ SequenceEditingPanel.this.viewPort.getWidth() + ", " + SequenceEditingPanel.this.viewPort.getHeight()); // NOI18N
			SequenceEditingPanel.this.validate();
        }
	}
	
	public void setViewPort(JViewport viewPort) {
		this.viewPort = viewPort;
	}
	
    public Dimension getPreferredSize() {
		if (DEBUG) System.out.println("getPreferredSize"); // NOI18N
		
		int filmRollWidth = (this.filmUnitWidth * this.sequence.getFrameCount()) + this.transOutlineToSeparator.x + this.transSeparatorToOutline.x;
		int filmRollHeight = BOUNDARY_MIN + this.separatorHeight;

		int prefWidth = 0;
		int prefHeight = 0;

		if (this.viewPort != null && this.viewPort.getWidth() > filmRollWidth) {
			prefWidth += this.viewPort.getWidth();
		}
		else {
			prefWidth += filmRollWidth + 20;
			this.start.x = BOUNDARY_MIN;
		}

		prefHeight += filmRollHeight;
		this.start.y = BOUNDARY_MIN;
        
		return new Dimension(prefWidth, prefHeight);
    }

	//Always repaints the whole sequence :(
	public void paintComponent(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		g.setColor(this.backgroundColor);
		//g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(COLOR_SEPARATOR_HORIZONTAL);
		g.drawLine(0, this.getHeight()-1, this.getWidth(), this.getHeight()-1);
		
		this.start = new Point (this.separatorWidth, (this.getHeight() - this.outlineHeight) /2);
		Point current = new Point(this.start);
		int col = 0;
		for (int i = 0; i < this.sequence.getFrameCount(); i++) {
			this.drawSeparator(g, col, current);
			col++;
			
			current.translate(transSeparatorToOutline.x, transSeparatorToOutline.y);

			this.drawOutline(g, i, current);
			
			current.translate(transOutlineToFrame.x, transOutlineToFrame.y);
			StaticTile frame = this.sequence.getFrame(i);
			this.drawFrame(g, frame, current);
			col++;
			
			current.translate(transFrameToSeparator.x, transFrameToSeparator.y);
		}
		this.drawSeparator(g, col, current);
	}
		
	private void drawSeparator(Graphics2D g, int col, Point p) {
		Color c = (this.hilitedColumn == col) ? new Color(255, 235, 140) : this.backgroundColor;
		g.setColor(c);
		g.fillRect(p.x, 0, this.separatorWidth, this.getHeight() -1);
		g.setColor(COLOR_SEPARATOR_VERTICAL);
		int x = p.x + this.separatorWidth / 2;
		g.drawLine(x, 0, x, this.getHeight() -1);
	}
	
	private void drawOutline(Graphics2D g, int frameIndex, Point p) {
		int col = this.getColumnForFrame(frameIndex);
		int halfW = this.unitWidth / 2;
		int halfH = this.unitHeight / 2;
		if (this.selection.isFrameSelected(frameIndex)) {
			g.setColor(new Color(175, 195, 255));
			if (DEBUG) System.out.println("COL: " + frameIndex); // NOI18N
			g.fillRect(p.x - this.unitWidth, p.y - this.unitHeight, this.outlineWidth + 2*this.unitWidth, this.outlineHeight + 2*this.unitHeight);
		}
		if (this.hilitedColumn == col) {
			g.setColor(new Color(255, 235, 140));
			g.fillRect(p.x - halfW, p.y - halfH, this.outlineWidth + this.unitWidth, this.outlineHeight + this.unitHeight);
		}
		g.setColor(Color.WHITE);
		g.fillRect(p.x, p.y, this.outlineWidth, this.outlineHeight);
	}
	
	private void drawFrame(Graphics2D g, StaticTile frame, Point p) {
		//g.fillRect(p.x, p.y, this.frameWidth, this.frameHeight);
		frame.paint(g, p.x, p.y);
	}	

	private int getColumnForPoint(Point p) {
		if (p.x < this.start.x + this.transSeparatorToOutline.x)
			return 0;
		if (p.x > this.start.x + this.filmUnitWidth * (this.sequence.getFrameCount()))
			return this.getLastColIndex();
		//first determine in which film unit the point is
		int filmUnit = (p.x - this.start.x) / this.filmUnitWidth;
		//then offsetColumn
		int offsetCol = 0;
		int offset = (p.x - this.start.x) % this.filmUnitWidth;
		if (offset < this.transSeparatorToOutline.x)
			offsetCol = 0;
		else if (offset < this.transSeparatorToOutline.x + this.outlineWidth)
			offsetCol = 1;
		else
			offsetCol = 2;
		
		return 2 * filmUnit + offsetCol;
	}
	
	private Rectangle getAreaForColumn(int col) {
		int x = 0;
		int y = 0;
		int width = 0;
		int height = this.getHeight();
		//even columns are separators
		if (col % 2 == 0) {
			//first col is special case
			if (col == 0) {
				width = this.start.x + transSeparatorToOutline.x;
			}
			else {
				x = this.start.x + (this.sequence.getFrameCount() * this.filmUnitWidth) - this.transOutlineToSeparator.x;
				//last col is special case
				if (col == this.getLastColIndex()) {
					width = this.getWidth() - x;
				}
				//separators in the middle
				else {
					width = this.transSeparatorToOutline.x + this.transOutlineToSeparator.x;
				}
			}
		}
		//odd columns are frames
		else {
			x = this.start.x + this.transSeparatorToOutline.x + (this.filmUnitWidth * (col/2));
			width = this.outlineWidth;
		}
		return new Rectangle(x, y, width, height);
	}
	
	private int getLastColIndex() {
		return this.sequence.getFrameCount() * 2;
	}
	
	private int getColumnForFrame(int frameIndex) {
		return frameIndex * 2 + 1;
	}
	
	private int getFrameForColumn(int col) {
		if (col % 2 == 0)
			return -1;
		return  (col - 1) / 2;
	}
	
	private int getColumnCount() {
		return this.getLastColIndex() + 1;
	}
	
	//---------- Scrollable ------------
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

	//---------- MouseMotionListener ------------
    public void mouseDragged(MouseEvent e) {
		this.handleMouseMoveOver(e.getPoint());
    }

    public void mouseMoved(MouseEvent e) {
		this.handleMouseMoveOver(e.getPoint());
    }
	
	private void handleMouseMoveOver(Point p) {
		int old = this.hilitedColumn;
		this.hilitedColumn = this.getColumnForPoint(p);
		if (old != this.hilitedColumn) {
			if (DEBUG) System.out.println("HilitedCol = " + this.hilitedColumn); // NOI18N
			int frame = this.getFrameForColumn(this.hilitedColumn);
			if (frame != -1) {
				this.fireFrameHilited(frame);
			}
			this.repaint();
		}
	}

	//------------ SequenceListener ---------------
    public void frameAdded(Sequence sequence, int index) {
		if (DEBUG) System.out.println("SequenceEditingPanel.frameAdded"); // NOI18N
		this.scrollRectToVisible(this.getAreaForColumn(this.getColumnForFrame(index)));
		this.revalidate();
		this.repaint();
    }

    public void frameRemoved(Sequence sequence, int index) {
		this.revalidate();
		this.repaint();
    }

    public void frameModified(Sequence sequence, int index) {
		if (DEBUG) System.out.println("SequenceEditingPanel.frameModified"); // NOI18N
		this.repaint(this.getAreaForColumn(getColumnForFrame(index)));
    }

	//------------ MouseListener -----------
	//on separator right-click offer tween :)
	
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			this.handlePopUp(e);
		}
		else {
			int col = this.getColumnForPoint(e.getPoint());
			int f = this.getFrameForColumn(col);
			if (f == -1)
				return;
			if (isContinuousSelect(e)) {
				int anchor = this.selection.getAnchorSelectionIndex();
				if (anchor == FrameSelectionManager.NONE) {
					this.selection.setSelected(f, true);
				}
				else {
					this.selection.setIntervalSelection(anchor, f, true);
				}
			}
			else if (isMultiSelect(e)) {
				this.selection.flipSelection(f);
			}
			else {
				boolean alreadySelected = this.selection.isFrameSelected(f);
				this.selection.clearSelections();
				this.selection.setSelected(f, !alreadySelected);
			}
			this.repaint();
			this.sequence.getGameDesign().getMainView().requestPreview(this.sequence.getFrame(f));
		}
    }

    public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			this.handlePopUp(e);
		}
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
		this.hilitedColumn = NONE;
		this.repaint();
		this.fireHiliteLost();
    }

	private boolean isMultiSelect(MouseEvent e) {
		return e.isControlDown();
	}

	private boolean isContinuousSelect(MouseEvent e) {
		return e.isShiftDown();
	}
	
	private void handlePopUp(MouseEvent e) {
		JPopupMenu menu = new JPopupMenu();
		
		//common menu items
		if (this.sequenceContainer != null) {
			for (Iterator it = this.sequenceContainer.getActionsForSequence(this.sequence).iterator(); it.hasNext();) {
				Action a = (Action) it.next();
				menu.add(a);
			}

			menu.addSeparator();
		}
		
		int col = this.getColumnForPoint(e.getPoint());
		//menu for separator
		if (this.getFrameForColumn(col) == -1) {
			for (Iterator it = this.getSeparatorActions(col).iterator(); it.hasNext();) {
				Action a = (Action) it.next();
				menu.add(a);
			}
		}
		//menu for frame
		else {
			for (Iterator it = this.getFrameActions(col).iterator(); it.hasNext();) {
				Action a = (Action) it.next();
				menu.add(a);
			}
		}
		menu.show(this, e.getX(), e.getY());
	}
		
	//----------- Frame actions
	private List getFrameActions(int col) {
		ArrayList actions = new ArrayList();
		
		SelectAllAction saa = new SelectAllAction();
		actions.add(saa);

		RemoveAction ra = new RemoveAction();
		if (this.sequence.getFrameCount() == 1 || this.selection.getSelectedIndexes().size() > 1)
			ra.setEnabled(false);
		ra.putValue(RemoveAction.PROP_COL, new Integer(col));
		actions.add(ra);
		
		RemoveSelectedAction rsa = new RemoveSelectedAction();
		if (this.selection.isSelectionEmpty())
			rsa.setEnabled(false);
		
		 //I cannot erase all frames from sequence || no selection at all
		if (this.sequence.getFrameCount() <= this.selection.getSelectedIndexes().size() || this.selection.getSelectedIndexes().size() == 0)
			rsa.setEnabled(false);
		
		actions.add(rsa);
		
		return actions;
	}
	private class SelectAllAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(SequenceEditingPanel.class, "SequenceEditingPanel.menuSelectAll.txt"));
		}
		
        public void actionPerformed(ActionEvent e) {
			Sequence seq = SequenceEditingPanel.this.sequence;
			SequenceEditingPanel.this.selection.setIntervalSelection(0, seq.getFrameCount() -1, true);
			SequenceEditingPanel.this.repaint();
		}
	}

	private class RemoveAction extends AbstractAction {
		public static final String PROP_COL = "PROP_COL"; // NOI18N
		
		{
			this.putValue(NAME, NbBundle.getMessage(SequenceEditingPanel.class, "SequenceEditingPanel.menuRemoveFrame.txt"));
		}
		
        public void actionPerformed(ActionEvent e) {
			int col = ((Integer) this.getValue(PROP_COL)).intValue();
			Sequence seq = SequenceEditingPanel.this.sequence;
			seq.removeFrame(SequenceEditingPanel.this.getFrameForColumn(col));
		}
	}
	
	private class RemoveSelectedAction extends AbstractAction {
		
		{
			this.putValue(NAME, NbBundle.getMessage(SequenceEditingPanel.class, "SequenceEditingPanel.menuRemoveSelectedFrames.txt"));
		}
		
        public void actionPerformed(ActionEvent e) {
			Set<Integer> indexes = SequenceEditingPanel.this.selection.getSelectedIndexes();			
			SequenceEditingPanel.this.sequence.removeFrames(indexes);
		}
	}
	
	//----------- Separator actions
	private List getSeparatorActions(int col) {
		ArrayList actions = new ArrayList();
		
		TweenAction ta = new TweenAction();
		if (col == 0 || col == this.sequence.getFrameCount()*2)
			ta.setEnabled(false);
		ta.putValue(TweenAction.PROP_COL, new Integer(col));
		actions.add(ta);
		
		InsertEmptyFrameAction iefa = new InsertEmptyFrameAction();
		iefa.putValue(InsertEmptyFrameAction.PROP_COL, col);
		actions.add(iefa);
		
		return actions;
	}
	
	private class TweenAction extends AbstractAction {
		public static final String PROP_COL = "PROP_COL"; // NOI18N
		
		{
			this.putValue(NAME, NbBundle.getMessage(SequenceEditingPanel.class, "SequenceEditingPanel.menuTweenFrames.txt"));
		}
		
        public void actionPerformed(ActionEvent e) {
			int separatorCol = ((Integer) this.getValue(PROP_COL)).intValue();
			int beforeFrameIndex = SequenceEditingPanel.this.getFrameForColumn(separatorCol - 1);
			int afterFrameIndex = SequenceEditingPanel.this.getFrameForColumn(separatorCol + 1);
			Sequence seq = SequenceEditingPanel.this.sequence;
			StaticTile before = seq.getFrame(beforeFrameIndex);
			StaticTile after = seq.getFrame(afterFrameIndex);
			int indexBefore = before.getIndex();
			int indexAfter = after.getIndex();
			//can't tween two same frames or consecutive frames
			if (indexBefore == indexAfter || indexBefore == (indexAfter + 1) || indexBefore == (indexAfter - 1))
				return;
			int incr = 1;
			if (indexBefore > indexAfter) {
				incr = -1;
			}
			int insertIndex = afterFrameIndex;
			while ((indexBefore += incr) != indexAfter) {
				ImageResource imgRes = seq.getImageResource();
				StaticTile tile = (StaticTile) imgRes.getTile(indexBefore, frameWidth, frameHeight, seq.isZeroBasedIndex());
				seq.insertFrame(tile, insertIndex);
				if (DEBUG) System.out.println("insert tile: " + tile.getIndex() + " at index " + insertIndex + " at Column " + SequenceEditingPanel.this.getColumnForFrame(insertIndex)); // NOI18N
				SequenceEditingPanel.this.selection.setSelected(insertIndex, true);
				insertIndex++;
			}
        }
	}
	
	private class InsertEmptyFrameAction extends AbstractAction {
		public static final String PROP_COL = "PROP_COL"; // NOI18N
		
		{
			this.putValue(NAME, NbBundle.getMessage(SequenceEditingPanel.class, "SequenceEditingPanel.menuInsertFrame.txt"));
		}
		
        public void actionPerformed(ActionEvent e) {
			int separatorCol = ((Integer) this.getValue(PROP_COL)).intValue();
			int frameIndex = SequenceEditingPanel.this.getFrameForColumn(separatorCol + 1);
			Sequence seq = SequenceEditingPanel.this.sequence;
			seq.insertFrame(null, frameIndex);
		}
	}
	//----------- End actions

	
	public synchronized void addSequenceEditingPanelListener(SequenceEditingPanelListener l) {
		this.listenerList.add(SequenceEditingPanelListener.class, l);
	}

	public synchronized void removeSequenceEditingPanelListener(SequenceEditingPanelListener l) {
		this.listenerList.remove(SequenceEditingPanelListener.class, l);
	}

	private void fireFrameHilited(int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SequenceEditingPanelListener.class) {
				((SequenceEditingPanelListener) listeners[i+1]).frameHilited(this, index);
			}
		}
	}
	
	private void fireHiliteLost() {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SequenceEditingPanelListener.class) {
				((SequenceEditingPanelListener) listeners[i+1]).hiliteLost(this);
			}
		}
	}
	private void fireSelectioChange(int[] selectedFrameIdices) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SequenceEditingPanelListener.class) {
				((SequenceEditingPanelListener) listeners[i+1]).frameSelectionChange(this, selectedFrameIdices);
			}
		}
	}

	
	
	
	private class FrameSelectionManager implements SequenceListener {
		public static final int NONE = -1;

		private List<Boolean> frameSelections = new ArrayList();
		
		private int anchorSelectionIndex = NONE;
		private int leadSelectionIndex = NONE;
		
		{
			int size = SequenceEditingPanel.this.sequence.getFrameCount();
			for (int i = 0; i < size; i++) {
				this.frameSelections.add(Boolean.FALSE);
			}
		}
		
		public int getAnchorSelectionIndex() {
			return this.anchorSelectionIndex;
		}
		
		public int getLeadSelectionIndex() {
			return this.leadSelectionIndex;
		}
		
		public void clearSelections() {
			for (int i = 0; i < this.frameSelections.size(); i++)
				this.frameSelections.set(i, Boolean.FALSE);
			this.anchorSelectionIndex = NONE;
			this.leadSelectionIndex = NONE;
		}
		
		public boolean flipSelection(int frame) {
			Boolean x = (Boolean) this.frameSelections.get(frame);
			if (x == null)
				x = Boolean.FALSE;
			x = !x;
			this.frameSelections.set(frame, x);
			return x;
		}
		
		public void setSelected(int frame, boolean selected) {
			this.frameSelections.set(frame, selected);
			if (selected) {
				this.anchorSelectionIndex = frame;
			}
			else {
				this.anchorSelectionIndex = NONE;
			}
			this.leadSelectionIndex = NONE;	
		}
				
		public void setIntervalSelection(int startFrameInclusive, int endFrameInclusive, boolean selected) {
			if (startFrameInclusive <= endFrameInclusive) {
				for (int i = startFrameInclusive; i <= endFrameInclusive; i++) {
					this.frameSelections.set(i, selected);
				}
			}
			else {
				for (int i = startFrameInclusive; i >= endFrameInclusive; i--) {
					this.frameSelections.set(i, selected);
				}
			}
			if (selected) {
				this.anchorSelectionIndex = startFrameInclusive;
				this.leadSelectionIndex = endFrameInclusive;
			}
		}

		public boolean isFrameSelected(int index) {
			Boolean selection = (Boolean) this.frameSelections.get(index);
			assert selection != null;
			return selection;
		}
		
		public Set<Integer> getSelectedIndexes() {
			Set<Integer> indexes = new HashSet();
			for (int i = 0; i < this.frameSelections.size(); i++) {
				if (this.frameSelections.get(i).equals(Boolean.TRUE)) {
					indexes.add(i);
				}
			}
			return indexes;
		}
		
		public boolean isSelectionEmpty() {
			return this.frameSelections.isEmpty();
		}

		public void frameAdded(Sequence sequence, int index) {
			this.frameSelections.add(index, true);
		}

		public void frameRemoved(Sequence sequence, int index) {
			this.frameSelections.remove(index);
		}

		public void frameModified(Sequence sequence, int index) {
		}
	}	
	
	
	
}
