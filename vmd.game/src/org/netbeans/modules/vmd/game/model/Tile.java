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



public abstract class Tile implements Previewable, Comparable {

	public static final int EMPTY_TILE_INDEX = 0;

	private ImageResource imageResource;

	private int index;
	private int width;
	private int height;

	//TODO : change the constructor to take (ImageResourceURL, width, height, index)
	Tile(ImageResource imageResource, int index, int width, int height) {
		this.imageResource = imageResource;
		this.index = index;
		this.width = width;
		this.height = height;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getHeight() {
		return this.height;
	}
	
	public int getWidth() {
		return this.width;
	}

	public String toString() {
		return "Tile index = " + this.index + ", height = " + this.getHeight() + ", width = " + this.getWidth(); // NOI18N
	}

    public ImageResource getImageResource() {
    	return this.imageResource;
    }

    public int compareTo(Object o) throws ClassCastException {
		if (o instanceof Tile) {
			Tile t = (Tile) o;
			return new Integer(this.index).compareTo(new Integer(t.index));
		}
		throw new ClassCastException(o.getClass() + " cannot be compared to " + this.getClass()); // NOI18N
    }
	
}
