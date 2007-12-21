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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

/**
 * @author ads
 */
public interface ToPartContainer extends BpelContainer {

    /**
     * @return ToPart's children array.
     */
    ToPart[] getToParts();

    /**
     * Getter for <code>i</code>-th ToPart child.
     *
     * @param i
     *            Index in ToPart's children array.
     * @return <code>i</code>-th ToPart child.
     */
    ToPart getToPart( int i );

    /**
     * Setter for <code>i</code>-th ToPart child.
     *
     * @param part
     *            New ToPart child.
     * @param i
     *            Index in ToPart's children array.
     */
    void setToPart( ToPart part, int i );

    /**
     * Insert new <code>part</code> inside children list on the <code>i</code>-th
     * place.
     * 
     * @param part
     *            New ToPart child.
     * @param i
     *            Index in ToPart's children array.
     */
    void insertToPart( ToPart part, int i );

    /**
     * Adds new ToPart child at the end of FromPart's children list.
     * 
     * @param part
     *            New ToPart child.
     */
    void addToPart( ToPart part );

    /**
     * Removes <code>i</code>-th ToPart child.
     * 
     * @param i
     *            Index in ToPart's children array.
     */
    void removeToPart( int i );

    /**
     * Set new ToPart's children array.
     * 
     * @param parts
     *            New array.
     */
    void setToPart( ToPart[] parts );

    /**
     * @return Size of ToPart's children array.
     */
    int sizeOfToParts();
}
