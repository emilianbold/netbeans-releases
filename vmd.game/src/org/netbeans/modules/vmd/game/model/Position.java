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

/**
 * Immutable.
 * 
 * @author kherink
 */
public class Position {
	private int row;
	private int col;
	
	public Position(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Position) {
			Position cell = (Position) obj;
			if (cell.row == this.row && cell.col == this.col)
				return true;
		}
        return super.equals(obj);
    }

	public int hashCode() {
		return this.row * this.col;
	}
	
	public String toString() {
        return ("Cell row: " + this.row + ", col: " + this.col); // NOI18N
    }

}
