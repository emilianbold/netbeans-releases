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
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.TiledLayer;

/**
 *
 * @author kaja
 */
public class NewSimpleTiledLayerDialog extends AbstractNamingDialog {
	
	private ImageResource imgRes;
	private int[][] grid;
	
	/** Creates a new instance of DuplicateTiledLayerDialog */
	public NewSimpleTiledLayerDialog(ImageResource imgRes, int[][] grid) {
		this.imgRes = imgRes;
		this.grid = grid;
	}

	protected String getInitialStateDescriptionText() {
		return "Enter a Tiled Layer name.";
	}

	protected String getNameLabelText() {
		return "Tiled Layer name:";
	}

	protected String getDialogNameText() {
		return "Create new Tiled Layer";
	}

	protected String getCurrentStateErrorText() {
		String errMsg = null; 
		String layerName = this.fieldName.getText();

		if (layerName.equals("")) {
			return this.getInitialStateDescriptionText();
		}
		if (GlobalRepository.getInstance().getLayerByName(layerName) != null) {
			errMsg = "Layer name already exists.";
		}
		return errMsg;
	}

	protected void handleOKButton() {
		GlobalRepository.getInstance().createTiledLayer(this.fieldName.getText(), this.imgRes, this.grid);
		this.frame.dispose();
	}
	
}
