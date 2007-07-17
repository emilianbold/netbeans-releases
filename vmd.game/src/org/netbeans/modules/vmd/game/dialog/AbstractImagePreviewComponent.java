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

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author kherink
 */
public abstract class AbstractImagePreviewComponent extends JComponent {

	public AbstractImagePreviewComponent() {
		this.setBorder(null);
	}

	abstract public void setTileWidth(int width);
	
	abstract public void setTileHeight(int height);
	
	abstract public int getTileWidth();
	
	abstract public int getTileHeight();
	
	abstract public void setImageURL(URL imgUrl) throws MalformedURLException, IllegalArgumentException;
	
	abstract public URL getImageURL();
	
	abstract public Image getImage();

	public List<Integer> getValidTileWidths() {
		int imgWidth = this.getImage().getWidth(null);
		return getEvenDivisors(imgWidth);
	}
	
	public List<Integer> getValidTileHeights() {
		int imgHeight = this.getImage().getHeight(null);
		return getEvenDivisors(imgHeight);
	}
	
	public static List getEvenDivisors(int number) {
		List divisors = new ArrayList();
		
		for (int i = 1; i <= number; i++) {
			if (number % i == 0) {
				divisors.add(new Integer(i));
			}
		}
		return divisors;
	}

}
