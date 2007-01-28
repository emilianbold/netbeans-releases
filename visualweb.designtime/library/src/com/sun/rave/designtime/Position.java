/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
