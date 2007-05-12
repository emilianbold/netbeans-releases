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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.rave.designtime;

/**
 * <p>A descriptor that identifies the position of a DesignBean within its container.</p>
 *
 * @author Carl Quinn
 * @version 1.0
 */
public class Position {

    /**
     * Protected storage field for the index property.
     */
    protected int index;

    /**
     * Constructs a Position object with the default index (-1: unspecified)
     */
    public Position() {
        index = -1; // < 0 is unspecified
    }

    /**
     * Constructs a Position object with the specified index
     *
     * @param index The desired index for this Position
     */
    public Position(int index) {
        this.index = index;
    }

    /**
     * @return Returns the position index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index The index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }
}
