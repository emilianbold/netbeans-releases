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
package org.netbeans.modules.vmd.game.model;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.vmd.game.dialog.RenameSequenceDialog;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceEditingPanel;
import org.netbeans.modules.vmd.game.preview.SequencePreviewPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class Sequence implements Previewable, Editable {

	public static final boolean DEBUG = false;

	public static final int DEFAULT_FRAMES = 1;
	public static final int DEFAULT_SHOWTIME_MS = 200;
	
	public static final String PROPERTY_NAME = "sequence.prop.name"; // NOI18N
	public static final String PROPERTY_FRAME_MS = "sequence.prop.frames.ms"; // NOI18N
	
	EventListenerList listenerList = new EventListenerList();

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	private SequenceEditingPanel editor;
		
	private String name;
	
	private ImageResource imageResource;
	private boolean zeroBasedIndex;
	
	private int frameWidth;
	private int frameHeight;
	private int frameMs;
	private ArrayList<StaticTile> frames;
	
    Sequence(String name, ImageResource imageResource, int frameWidth, int frameHeight, boolean zeroBasedIndex) {
		this(name, imageResource, 1, frameWidth, frameHeight, zeroBasedIndex);
    }
	
	Sequence(String name, ImageResource imageResource, int numberFrames, int frameWidth, int frameHeight, boolean zeroBasedIndex) {
		this.name = name;
		this.imageResource = imageResource;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.zeroBasedIndex = zeroBasedIndex;
		
		this.frames = new ArrayList();
		for (int i = 0; i < numberFrames; i++) {
			this.frames.add((StaticTile) imageResource.getTile(0, this.frameWidth, this.frameHeight, this.zeroBasedIndex));
		}
		this.frameMs = DEFAULT_SHOWTIME_MS;
	}
	
	Sequence(String name, Sequence sequence) {
		this.name = name;
		this.imageResource = sequence.getImageResource();
		this.frameHeight = sequence.getFrameHeight();
		this.frameWidth = sequence.getFrameWidth();
		this.frameMs = sequence.frameMs;
		this.zeroBasedIndex = sequence.zeroBasedIndex;
		
		this.frames = new ArrayList();
		this.frames.addAll(sequence.getFrames());
	}

	public GlobalRepository getGameDesign() {
		return this.imageResource.getGameDesign();
	}
	
	public boolean isZeroBasedIndex() {
		return this.zeroBasedIndex;
	}
	
	public void setName(String name) {
		if (!this.getGameDesign().isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Sequence cannot be renamed because component name '" + name + "' already exists."); // NOI18N
		}
		String oldName = this.name;
		this.name = name;
		this.propertyChangeSupport.firePropertyChange(Sequence.PROPERTY_NAME, oldName, name);
	}
	
	public void addFrame(StaticTile frame) {
		if (frame == null)
			frame = (StaticTile) imageResource.getTile(0, this.frameWidth, this.frameHeight, this.zeroBasedIndex);
		this.frames.add(frame);
		int index = this.frames.indexOf(frame);
		this.fireFrameAdded(frame, index);
	}
	
	public void insertFrame(StaticTile frame, int index) {
		if (frame == null)
			frame = (StaticTile) imageResource.getTile(0, this.frameWidth, this.frameHeight, this.zeroBasedIndex);
		this.frames.add(index, frame);
		this.fireFrameAdded(frame, index);
	}
	
	public void removeFrame(int index) {
		StaticTile frame = (StaticTile) this.frames.remove(index);
		this.fireFrameRemoved(frame, index);
	}
	
	public void removeFrames(Set<Integer> indexes) {
		//sort from largest to smallest so that we remove the largest indexes first
		List<Integer> tmp = new ArrayList<Integer>(indexes);
		Collections.sort(tmp, new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
				return (a.intValue() > b.intValue() ? -1 : (a.intValue() == b.intValue() ? 0 : 1) );
            }
		});
		for (Integer integer : tmp) {
			this.removeFrame(integer);
		}
	}
	
	public StaticTile getFrame(int index) {
		return (StaticTile) this.frames.get(index);
	}

	public void setFrame(StaticTile frame, int index) {
		if (frame == null) {
			frame = (StaticTile) imageResource.getTile(0, this.frameWidth, this.frameHeight, this.zeroBasedIndex);
		}
		this.frames.ensureCapacity(index + 1);
		this.frames.set(index, frame);
		this.fireFrameModified(frame, index);
	}
	
	public int getFrameMs() {
		return frameMs;
	}

	public void setFrameMs(int frameMs) {
		if (DEBUG) System.out.println("FrameMS = " + frameMs); // NOI18N
		int oldMs = this.frameMs;
		this.frameMs = frameMs;
		this.propertyChangeSupport.firePropertyChange(Sequence.PROPERTY_FRAME_MS, oldMs, frameMs);
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public int getFrameCount() {
		return this.frames.size();
	}
	
	public Dimension getFrameSize() {
		return new Dimension(this.getFrameWidth(), this.getFrameHeight());
	}
	
	public List getFrames() {
		return Collections.unmodifiableList(this.frames);
	}
	
	public int[] getFramesAsArray() {
		int[] a = new int[this.frames.size()];
		for (int i = 0; i < a.length; i++) {
			a[i] = this.frames.get(i).getIndex();	
		}
		return a;
	}
	
	private void fireFrameAdded(StaticTile frame, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SequenceListener.class) {
				((SequenceListener) listeners[i+1]).frameAdded(this, index);
			}
		}
	}
	
	private void fireFrameRemoved(StaticTile frame, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SequenceListener.class) {
				((SequenceListener) listeners[i+1]).frameRemoved(this, index);
			}
		}
	}
	
	private void fireFrameModified(StaticTile frame, int index) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SequenceListener.class) {
				((SequenceListener) listeners[i+1]).frameModified(this, index);
			}
		}
	}	
	
	public JComponent getEditor() {
		return this.editor == null ? this.editor = new SequenceEditingPanel(this) : this.editor;
	}
	
	
	public ImageResource getImageResource() {
		return this.imageResource;
	}
	
	public ImageResourceInfo getImageResourceInfo() {
		return new ImageResourceInfo(this.imageResource, this.frameWidth, this.frameHeight, this.zeroBasedIndex);
	}

	public JComponent getNavigator() {
		return null;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}
	
	public synchronized void addSequenceListener(SequenceListener l) {
		this.listenerList.add(SequenceListener.class , l);
	}

	public synchronized void removeSequenceListener(SequenceListener l) {
		this.listenerList.remove(SequenceListener.class, l);
	}

	public String toString() {
		return this.getName();
	}

	//Previewable 
	public void paint(Graphics2D g, int x, int y) {
		this.getFrame(0).paint(g, x, y);
	}

    public int getWidth() {
		return this.frameWidth;
    }

    public int getHeight() {
		return this.frameHeight;
    }
	
	public JComponent getPreview() {
		return new SequencePreviewPanel(this);
	}


	public String getName() {
		return this.name;
	}
	
	//-------- Actions ---------
	
	public List<Action> getActions() {
		ArrayList actions = new ArrayList<Action>();
		actions.add(new RenameSequenceAction());
		actions.add(new EditSequenceAction());
		return Collections.unmodifiableList(actions);
	}

	public class EditSequenceAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Sequence.class, "Sequence.EditSequenceAction.text"));
		}

		public void actionPerformed(ActionEvent e) {
			Sequence.this.getGameDesign().getMainView().requestEditing(Sequence.this);
		}
	}

	public class RenameSequenceAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Sequence.class, "Sequence.RenameSequenceAction.text"));
		}

		public void actionPerformed(ActionEvent e) {
			RenameSequenceDialog dialog = new RenameSequenceDialog(Sequence.this);
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(Sequence.class, "Sequence.RenameSequenceAction.text"));
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}
}
