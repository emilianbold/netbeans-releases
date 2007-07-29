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
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.game.dialog.NewSequenceDialog;
import org.netbeans.modules.vmd.game.dialog.RenameAnimatedTileDialog;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceContainerEditor;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceContainerNavigator;
import org.netbeans.modules.vmd.game.preview.SequenceContainerPreview;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class AnimatedTile extends Tile implements SequenceContainer, Editable {

	public static final boolean DEBUG = false;
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private SequenceContainerImpl sequenceContainer;
	
	private SequenceContainerEditor editor;
	private String name;
	
	
	AnimatedTile(String name, ImageResource imageResource, int index, Sequence sequence, int width, int height) {
		super(imageResource, index, width, height);
		this.sequenceContainer = new SequenceContainerImpl(this, null, this.propertyChangeSupport, imageResource, width, height, false);
		this.name = name;
		this.setDefaultSequence(sequence);
	}
	
	AnimatedTile(String name, ImageResource imageResource, int index, int width, int height) {
		super(imageResource, index, width, height);
		this.sequenceContainer = new SequenceContainerImpl(this, null, this.propertyChangeSupport, imageResource, width, height, false);
		this.name = name;
		String seqName = this.getNextSequenceName(this.name + "seq"); // NOI18N
		Sequence sequence = this.createSequence(seqName, 1, width, height);
		this.setDefaultSequence(sequence);
	}

	
	public String getNextSequenceName(String prefix) {
		return this.sequenceContainer.getNextSequenceName(prefix);
	}

	public GlobalRepository getGameDesign() {
		return this.getImageResource().getGameDesign();
	}	
	
	public void addSequenceContainerListener(SequenceContainerListener listener) {
		this.sequenceContainer.addSequenceContainerListener(listener);
	}

	public void removeSequenceContainerListener(SequenceContainerListener listener) {
		this.sequenceContainer.removeSequenceContainerListener(listener);
	}

	public void setName(String name) {
		if (!this.getGameDesign().isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("AnimatedTile cannot be renamed because component name '" + name + "' already exists."); // NOI18N
		}
		String oldName = this.name;
		this.name = name;
		this.propertyChangeSupport.firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	//------SequenceContainer-------
	
	public String getName() {
		return this.name;
	}

	public Sequence createSequence(String name, int numberFrames, int frameWidth, int frameHeight) {
		return this.sequenceContainer.createSequence(name, numberFrames, frameWidth, frameHeight);
	}
	
	public Sequence createSequence(String name, Sequence s) {
		return this.sequenceContainer.createSequence(name, s);
	}
	
	public boolean append(Sequence sequence) {
		return this.sequenceContainer.append(sequence);
	}
	
	public boolean insert(Sequence sequence, int index) {
		return this.sequenceContainer.insert(sequence, index);
	}
	
	public boolean remove(Sequence sequence) {
		return this.sequenceContainer.remove(sequence);
	}
	
	public void move(Sequence sequence, int newIndex) {
		this.sequenceContainer.move(sequence, newIndex);
	}
	
	public List<Sequence> getSequences() {
		return this.sequenceContainer.getSequences();
	}

	public int getSequenceCount() {
		return this.sequenceContainer.getSequenceCount();
	}
	
	public Sequence getSequenceByName(String name) {
		return this.sequenceContainer.getSequenceByName(name);
	}
	
	public void setDefaultSequence(Sequence defaultSequence) {
		this.sequenceContainer.setDefaultSequence(defaultSequence);
	}
	
	public Sequence getDefaultSequence() {
		return this.sequenceContainer.getDefaultSequence();
	}
	
	public int indexOf(Sequence sequence) {
		return this.sequenceContainer.indexOf(sequence);
	}
	
	public Sequence getSequenceAt(int index) {
		return this.sequenceContainer.getSequenceAt(index);
	}
	
    public List<Action> getActionsForSequence(Sequence sequence) {
        return this.sequenceContainer.getActionsForSequence(sequence);
    }
	
	public JComponent getEditor() {
		return this.editor == null ? this.editor = new SequenceContainerEditor(this) : this.editor;
	}

    public ImageResourceInfo getImageResourceInfo() {
    	return new ImageResourceInfo(this.getImageResource(), this.getWidth(), this.getHeight(), false);
    }
	
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>();
		actions.add(new RenameAction());
		actions.add(new AddSequenceAction());
		return actions;
	}
	
	private class AddSequenceAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.AddSequenceAction.name")); // NOI18N
		}
		public void actionPerformed(ActionEvent e) {
			NewSequenceDialog dialog = new NewSequenceDialog(AnimatedTile.this, getWidth(), getHeight());
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.AddSequenceAction.name")); // NOI18N
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}

	private class RenameAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.RenameAction.name")); // NOI18N
		}
		public void actionPerformed(ActionEvent e) {
			RenameAnimatedTileDialog dialog = new RenameAnimatedTileDialog(AnimatedTile.this);
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.RenameAction.name")); // NOI18N
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}

	public JComponent getPreview() {
		return new SequenceContainerPreview(NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.previewLabel.text"), this); // NOI18N
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}

	//--------PropertyChangeListener
	
	public void propertyChange(PropertyChangeEvent evt) {
		if (DEBUG) System.out.println(this.getClass() + "unimplemented propertyChange() from " + evt.getSource()); // NOI18N
	}

    public void paint(Graphics2D g, int x, int y) {
		this.getDefaultSequence().getFrame(0).paint(g, x, y);
    }

	public JComponent getNavigator() {
		return new SequenceContainerNavigator(this);
	}

}
