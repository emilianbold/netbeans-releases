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

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;


public interface Editable {

	public static final String PROPERTY_NAME = "editable.prop.name"; // NOI18N
	
	public JComponent getEditor();

	public ImageResourceInfo getImageResourceInfo();

	public JComponent getNavigator();
	
	public String getName();

	public List<Action> getActions();
	
	public void addPropertyChangeListener(PropertyChangeListener l);
	
	public void removePropertyChangeListener(PropertyChangeListener l);
	
	public class ImageResourceInfo {
		private ImageResource imgRes;
		private int tileWidth;
		private int tileHeight;
		private boolean zeroBasedIndex;

		public ImageResourceInfo(ImageResource imgRes, int tileWidth, int tileHeight, boolean zeroBasedIndex) {
			this.imgRes = imgRes;
			this.tileWidth = tileWidth;
			this.tileHeight = tileHeight;
			this.zeroBasedIndex = zeroBasedIndex;
		}
		
		public ImageResource getImageResource() {
			return this.imgRes;
		}		
		public int getTileWidth() {
			return this.tileWidth;
		}
		public int getTileHeight() {
			return this.tileHeight;
		}
		public boolean isZeroBasedIndex() {
			return this.zeroBasedIndex;
		}	
	}
}
