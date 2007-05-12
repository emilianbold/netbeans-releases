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

package com.sun.rave.designtime.markup;

import org.w3c.dom.Node;
import com.sun.rave.designtime.Position;

/**
 * The MarkupPosition extends the Position class to include specifics about DOM coordinates.  This
 * class is used when creating children, moving them, etc.
 *
 * @author Carl Quinn
 * @version 1.0
 */
public class MarkupPosition extends Position {

    /**
     * storage for the 'underParent' property
     */
    protected Node underParent;

    /**
     * storage for the 'beforeSibling' property
     */
    protected Node beforeSibling;

    /**
     * Constructs a default MarkupPosition with no settings
     */
    public MarkupPosition() {}

    /**
     * Constructs a MarkupPosition with the specified index
     *
     * @param index the desired index
     */
    public MarkupPosition(int index) {
        super(index);
    }

    /**
     * Constructs a MarkupPosition with the specified index and beforeSibling.  The 'beforeSibling'
     * denotes the immediate next sibling of the desired position.
     *
     * @param index The desired index
     * @param beforeSibling The desired beforeSibling - denotes the immediate next sibling of the
     *        desired position
     */
    public MarkupPosition(int index, Node beforeSibling) {
        super(index);
        this.beforeSibling = beforeSibling;
    }

    /**
     * Constructs a MarkupPosition with the specified underParent and beforeSibling.  The
     * 'underParent' denotes the desired parent for the position. The 'beforeSibling' denotes the
     * immediate next sibling of the desired position.
     *
     * @param underParent The desired underParent - denotes the desired parent for the position
     * @param beforeSibling The desired beforeSibling - denotes the immediate next sibling of the
     *        desired position
     */
    public MarkupPosition(Node underParent, Node beforeSibling) {
        this.underParent = underParent;
        this.beforeSibling = beforeSibling;
    }

    /**
     * Constructs a MarkupPosition with the specified beforeSibling. The 'beforeSibling' denotes the
     * immediate next sibling of the desired position.
     *
     * @param beforeSibling The desired beforeSibling - denotes the immediate next sibling of the
     *        desired position
     */
    public MarkupPosition(Node beforeSibling) {
        this.beforeSibling = beforeSibling;
    }

    /**
     * @return Returns the underParent. The 'underParent' denotes the desired parent for the
     * position.
     */
    public Node getUnderParent() {
        return underParent;
    }

    /**
     * @param underParent The underParent to set. The 'underParent' denotes the desired parent for
     * the position.
     */
    public void setUnderParent(Node underParent) {
        this.underParent = underParent;
    }

    /**
     * @return Returns the beforeSibling. The 'beforeSibling' denotes the immediate next sibling of
     * the desired position.
     */
    public Node getBeforeSibling() {
        return beforeSibling;
    }

    /**
     * @param beforeSibling The beforeSibling to set. The 'beforeSibling' denotes the
     * immediate next sibling of the desired position.
     */
    public void setBeforeSibling(Node beforeSibling) {
        this.beforeSibling = beforeSibling;
    }

    public String toString() {
        return "[MarkupPosition under:[" + underParent + "] before:[" + beforeSibling + "] index:[" +
            getIndex() + "]]"; // NOI18N
    }
}
