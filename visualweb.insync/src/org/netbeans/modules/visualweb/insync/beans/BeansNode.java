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
package org.netbeans.modules.visualweb.insync.beans;


/**
 * Common superclass for all beans package nodes.
 *
 * @author cquinn
 */
public abstract class BeansNode {

    protected final BeansUnit unit;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a populated root node
     *
     * @param unit The BeansUnit that this node will belong to.
     */
    protected BeansNode(BeansUnit unit) {
        this.unit = unit;
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * @return This node's Unit.
     */
    public BeansUnit getUnit() {
        return unit;
    }

    //--------------------------------------------------------------------------------------- Output

    /**
     * Utility function for debugging--return the name-part of this object's class name
     *
     * @return The name-part of this object's class name.
     */
    protected String clzName() {
        String cls = getClass().getName();
        int pre = cls.lastIndexOf('.');
        return cls.substring(pre + 1);
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(30);
        sb.append("[");
        sb.append(clzName());
        toString(sb);
        sb.append("]");
        return sb.toString();
    }

    /**
     * Helper method for this class hierarchy to simplify and speed up toString() implementation.
     *
     * @param sb  StringBuffer to toString() this object into.
     */
    public void toString(StringBuffer sb) {
    }

}
