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
	private JComponent navigator;
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
		return this.navigator == null ? this.navigator = new SequenceContainerNavigator(this) : this.navigator;
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
