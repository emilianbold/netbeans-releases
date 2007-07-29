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
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.game.dialog.RenameSpriteDialog;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceContainerEditor;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceContainerNavigator;
import org.netbeans.modules.vmd.game.preview.SequenceContainerPreview;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


public class Sprite extends Layer implements SequenceContainer {
	
	private SequenceContainerEditor editor;
	private SequenceContainerImpl sequenceContainer;
	
	/**
	 * Creates a new animated Sprite using frames contained in the provided Image.
	 */
	Sprite(GlobalRepository gameDesign, String name, ImageResource imageResource, int numberFrames, int frameWidth, int frameHeight) {
		super(gameDesign, name, imageResource, frameWidth, frameHeight);
		this.sequenceContainer = new SequenceContainerImpl(this, null, super.propertyChangeSupport, imageResource, frameWidth, frameHeight, true);
		String seqName = this.getNextSequenceName(name + "seq"); // NOI18N
		Sequence defaultSequence = this.createSequence(seqName, numberFrames, frameWidth, frameHeight);
		this.setDefaultSequence(defaultSequence);
	}
    
	Sprite(GlobalRepository gameDesign, String name, ImageResource imageResource, Sequence defaultSequence) {
		super(gameDesign, name, imageResource, defaultSequence.getFrameWidth(), defaultSequence.getFrameHeight());
		this.sequenceContainer = new SequenceContainerImpl(this, null, super.propertyChangeSupport, imageResource, defaultSequence.getFrameWidth(), defaultSequence.getFrameHeight(), true);
		this.setDefaultSequence(defaultSequence);
	}
	
	public ImageResourceInfo getImageResourceInfo() {
		return new ImageResourceInfo(this.getImageResource(), this.getTileWidth(), this.getTileHeight(), true);
	}
	
	public String getNextSequenceName(String prefix) {
		return this.sequenceContainer.getNextSequenceName(prefix);
	}

	public void addSequenceContainerListener(SequenceContainerListener listener) {
		this.sequenceContainer.addSequenceContainerListener(listener);
	}

	public void removeSequenceContainerListener(SequenceContainerListener listener) {
		this.sequenceContainer.removeSequenceContainerListener(listener);
	}

	//------SequenceContainer-------
	
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
	
	//------Editable-------
	
	public JComponent getEditor() {
		return this.editor == null ? this.editor = new SequenceContainerEditor(this) : this.editor;
	}
	
	public JComponent getNavigator() {
		return new SequenceContainerNavigator(this);
	}

	public int getHeight() {
		return super.getTileHeight();
	}

	public int getWidth() {
		return super.getTileWidth();
	}

	public List<Action> getActions() {
		List<Action> superActions = super.getActions();
		List<Action> actions = new ArrayList<Action>();
		actions.addAll(superActions);
		actions.add(new RenameAction());
//		actions.add(new AddSequenceAction());
		return actions;
	}
	
//	private class AddSequenceAction extends AbstractAction {
//		{
//			this.putValue(NAME, "Add sequence");
//		}
//		public void actionPerformed(ActionEvent e) {
//			NewSequenceDialog dialog = new NewSequenceDialog(Sprite.this, Sprite.this.getTileWidth(), Sprite.this.getTileHeight());
//			DialogDescriptor dd = new DialogDescriptor(dialog, "Add Sequence");
//			dd.setButtonListener(dialog);
//			dd.setValid(false);
//			dialog.setDialogDescriptor(dd);
//			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
//			d.setVisible(true);
//		}
//	}

	private class RenameAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(Sprite.class, "Sprite.RenameAction.text"));
		}
		public void actionPerformed(ActionEvent e) {
			RenameSpriteDialog dialog = new RenameSpriteDialog(Sprite.this);
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(Sprite.class, "Sprite.RenameAction.text"));
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}
	
	public String getDisplayableTypeName() {
		return NbBundle.getMessage(Sprite.class, "Sprite.text");
	}

	public JComponent getPreview() {
		return new SequenceContainerPreview( NbBundle.getMessage(Sprite.class, "Sprite.preview.title"), this);
	}

    public void paint(Graphics2D g, int x, int y) {
		this.getDefaultSequence().getFrame(0).paint(g, x, y);
    }

	public void paint(Graphics2D g) {
		this.getDefaultSequence().getFrame(0).paint(g, 0, 0);
	}

}
