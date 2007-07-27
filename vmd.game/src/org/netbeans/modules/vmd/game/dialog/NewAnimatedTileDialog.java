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

import org.netbeans.modules.vmd.game.model.AnimatedTile;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Tile;
import org.openide.util.NbBundle;

/**
 *
 * @author kherink
 */
public class NewAnimatedTileDialog extends AbstractNameValidationDialog {
	
	private ImageResource imageResource;
	private int tileWidth;
	private int tileHeight;
	
	/** Creates a new instance of NewAnimatedTileDialog */
	public NewAnimatedTileDialog(ImageResource imgRes, int tileWidth, int tileHeight) {
		super("");
		this.imageResource = imgRes;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	protected String getInitialStateDescriptionText() {
		return NbBundle.getMessage(NewAnimatedTileDialog.class, "NewAnimatedTileDialog.InitialStateDescription.text");
	}
	
	protected String getNameLabelText() {
		return NbBundle.getMessage(NewAnimatedTileDialog.class, "NewAnimatedTileDialog.NameLabel.text");
	}
	
	protected String getDialogNameText() {
		return NbBundle.getMessage(NewAnimatedTileDialog.class, "NewAnimatedTileDialog.title.text");
	}
	
	protected String getCurrentStateErrorText() {
		String errMsg = null; 
		
		String name = this.fieldName.getText();

		if (name.equals("")) {
			return this.getInitialStateDescriptionText();
		}
		if (!this.imageResource.getGameDesign().isComponentNameAvailable(name)) {
			errMsg = NbBundle.getMessage(NewAnimatedTileDialog.class, "NewAnimatedTileDialog.animatedTileExistsDescription.text");
		}
		return errMsg;
	}
	
	protected void handleOKButton() {
		AnimatedTile tile = this.imageResource.createAnimatedTile(this.fieldName.getText(), Tile.EMPTY_TILE_INDEX, this.tileWidth, this.tileHeight);
		this.imageResource.getGameDesign().getMainView().requestEditing(tile);
	}

}
