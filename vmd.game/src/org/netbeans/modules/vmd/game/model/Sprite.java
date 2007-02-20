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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.game.dialog.NewSequenceDialog;
import org.netbeans.modules.vmd.game.dialog.RenameSpriteDialog;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceContainerEditor;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceContainerNavigator;
import org.netbeans.modules.vmd.game.preview.SequenceContainerPreview;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;


public class Sprite extends Layer implements SequenceContainer {
	
	private SequenceContainerEditor editor;
	private SequenceContainerImpl sequenceContainer;
	
	/**
	 * Creates a new animated Sprite using frames contained in the provided Image.
	 */
	Sprite(String name, ImageResource imageResource, int numberFrames) {
		super(name, imageResource);
		this.sequenceContainer = new SequenceContainerImpl(this, null, super.propertyChangeSupport, imageResource);
		String seqName = this.getNextSequenceName(name + "seq");
		Sequence defaultSequence = this.createSequence(seqName, numberFrames);
		this.setDefaultSequence(defaultSequence);
	}
    
	Sprite(String name, ImageResource imageResource, Sequence defaultSequence) {
		super(name, imageResource);
		this.sequenceContainer = new SequenceContainerImpl(this, null, super.propertyChangeSupport, imageResource);
		this.setDefaultSequence(defaultSequence);
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
	
	public Sequence createSequence(String name, int numberFrames) {
		return this.sequenceContainer.createSequence(name, numberFrames);
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
		return this.getDefaultSequence().getFrameHeight();
	}

	public int getWidth() {
		return this.getDefaultSequence().getFrameWidth();
	}

	public List getActions() {
		List superActions = super.getActions();
		ArrayList actions = new ArrayList();
		actions.add(new RenameAction());
		actions.add(new AddSequenceAction());
		actions.addAll(superActions);
		return actions;
	}
	
	private class AddSequenceAction extends AbstractAction {
		{
			this.putValue(NAME, "Add sequence");
		}
		public void actionPerformed(ActionEvent e) {
			NewSequenceDialog dialog = new NewSequenceDialog(Sprite.this);
			DialogDescriptor dd = new DialogDescriptor(dialog, "Add Sequence");
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}

	private class RenameAction extends AbstractAction {
		{
			this.putValue(NAME, "Rename " + getDisplayableTypeName());
		}
		public void actionPerformed(ActionEvent e) {
			RenameSpriteDialog dialog = new RenameSpriteDialog(Sprite.this);
			DialogDescriptor dd = new DialogDescriptor(dialog, "Rename Sprite");
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}
	
	public String getDisplayableTypeName() {
		return "sprite";
	}

	public JComponent getPreview() {
		return new SequenceContainerPreview("Sprite:", this);
	}

    public void paint(Graphics2D g, int x, int y) {
		this.getDefaultSequence().getFrame(0).paint(g, x, y);
    }

	//---------------CodeGenerator---------
    public Collection getCodeGenerators() {
		HashSet gens = new HashSet();
		gens.add(this.getImageResource());
		for (Iterator it = this.getSequences().iterator(); it.hasNext();) {
			Sequence sequence = (Sequence) it.next();
			gens.add(sequence);
		}
		return gens;
    }

    public void generateCode(PrintStream ps) {
		ps.println("private Sprite sprite_" + this.getName() + ";");
		ps.println("public Sprite getLayer_" + this.getName() +"() throws IOException {");
		ps.println("	if (this.sprite_" + this.getName() + " == null) {");
		ps.println("		this.sprite_" + this.getName() + " = new Sprite(this.getImage_" + this.getImageResource().getNameNoExt() + "(), " + this.getTileWidth() + ", " + this.getTileHeight() + ");");
		ps.println("		this.sprite_" + this.getName() + ".setFrameSequence(this.sequence_" + this.getDefaultSequence().getName() + "_" + this.getImageResource().getNameNoExt() + ");");
		ps.println("	}");
		ps.println("	return this.sprite_" + this.getName() + ";");
		ps.println("}");
    }

	@Override
	public void paint(Graphics2D g) {
		this.getDefaultSequence().getFrame(0).paint(g, 0, 0);
	}

}
