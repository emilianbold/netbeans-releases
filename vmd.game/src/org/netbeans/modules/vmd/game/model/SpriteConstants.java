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

public interface SpriteConstants {
	
	/**
	 * No transform is applied to the Sprite.
	 */
	public final static int TRANS_NONE = 0;

	/**
	 * Causes the Sprite to appear reflected about its vertical center.
	 */
	public final static int TRANS_MIRROR = 2;

	/**
	 * Causes the Sprite to appear reflected about its vertical center and then rotated clockwise by 90 degrees.
	 */
	public final static int TRANS_MIRROR_ROT90 = 7;

	/**
	 * Causes the Sprite to appear reflected about its vertical center and then rotated clockwise by 180 degrees.
	 */
	public final static int TRANS_MIRROR_ROT180 = 1;

	/**
	 * Causes the Sprite to appear reflected about its vertical center and then rotated clockwise by 270 degrees.
	 */
	public final static int TRANS_MIRROR_ROT270 = 4;

	/**
	 * Causes the Sprite to appear rotated clockwise by 90 degrees.
	 */
	public final static int TRANS_ROT90 = 5;

	/**
	 * Causes the Sprite to appear rotated clockwise by 180 degrees.
	 */
	public final static int TRANS_ROT180 = 3;

	/**
	 * Causes the Sprite to appear rotated clockwise by 270 degrees.
	 */
	public final static int TRANS_ROT270 = 6;

}
