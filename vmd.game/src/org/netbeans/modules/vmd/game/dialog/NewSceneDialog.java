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

import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.Scene;

public class NewSceneDialog extends AbstractNameValidationDialog {

	private Scene scene;
	private GlobalRepository gameDesign;

	public NewSceneDialog(GlobalRepository gameDesign) {
		this.gameDesign = gameDesign;
	}
	
	public NewSceneDialog(Scene scene) {
		this.gameDesign = scene.getGameDesign();
		this.scene = scene;
	}
		
	protected String getInitialStateDescriptionText() {
		return "Enter scene name.";
	}
	
	protected String getNameLabelText() {
		return "Scene name:";
	}
	protected String getDialogNameText() {
		return "Create a new Scene";
	}
	protected String getCurrentStateErrorText() {
		String sceneName = this.fieldName.getText();
		
		if (sceneName == null || "".equals(sceneName)) {
			return this.getInitialStateDescriptionText();
		}
		if (!this.gameDesign.isComponentNameAvailable(sceneName)) {
			return "Component name already exists. Choose a different name.";
		}		
		return null;
	}
	
	protected void handleOKButton() {
		if (this.scene == null) {
			this.gameDesign.createScene(this.fieldName.getText());
		}
		else {
			this.gameDesign.createScene(this.fieldName.getText(), scene);
		}
	}
	
}
