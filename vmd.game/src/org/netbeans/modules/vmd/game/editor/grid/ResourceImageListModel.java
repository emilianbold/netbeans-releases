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

package org.netbeans.modules.vmd.game.editor.grid;

import javax.swing.AbstractListModel;
import org.netbeans.modules.vmd.game.model.ImageResource;

/**
 *
 * @author kherink
 */
public class ResourceImageListModel extends AbstractListModel {
	
	private ImageResource imgResource;
	private int tileWidth;
	private int tileHeight;

	public void update(ImageResource imgResource, int tileWidth, int tileHeight) {
		this.imgResource = imgResource;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.fireContentsChanged(this, 0, this.getSize() -1);
	}
	
    public int getSize() {
		return this.imgResource.getStaticTileCount(this.tileWidth, this.tileHeight) + 1;
    }

    public Object getElementAt(int index) {
		return this.imgResource.getTile(index, this.tileWidth, this.tileHeight);
    }
	
}
