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

import java.awt.Graphics2D;



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

    public abstract void paint(Graphics2D g, int x, int y, int scaledWidth, int scaledHeight);
    
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
