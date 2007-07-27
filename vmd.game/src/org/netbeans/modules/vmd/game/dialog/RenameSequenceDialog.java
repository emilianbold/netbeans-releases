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
package org.netbeans.modules.vmd.game.dialog;


import org.netbeans.modules.vmd.game.model.Sequence;
import org.openide.util.NbBundle;

public class RenameSequenceDialog extends AbstractNameValidationDialog {

	private Sequence sequence;
	
	public RenameSequenceDialog(Sequence sequence) {
		super(sequence.getName());
		this.sequence = sequence;
	}
	
	protected String getInitialStateDescriptionText() {
		return NbBundle.getMessage(RenameSequenceDialog.class, "RenameSequenceDialog.InitialStateDescription.text");
	}
	
	protected String getNameLabelText() {
		return NbBundle.getMessage(RenameSequenceDialog.class, "RenameSequenceDialog.NameLabel.text");
	}
	
	protected String getDialogNameText() {
		return NbBundle.getMessage(RenameSequenceDialog.class, "RenameSequenceDialog.title.text");
	}
	
	protected String getCurrentStateErrorText() {
		String errMsg = null;
		String name = this.fieldName.getText();

		if (name.equals("")) {
			return this.getInitialStateDescriptionText();
		}
		
		if (!sequence.getImageResource().getGameDesign().isComponentNameAvailable(name)) {
			errMsg = NbBundle.getMessage(RenameSequenceDialog.class, "RenameSequenceDialog.sequenceExistsDescription.text");
		}
		return errMsg;
	}
	
	protected void handleOKButton() {
		this.sequence.setName(this.fieldName.getText());
	}

}
