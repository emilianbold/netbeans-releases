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
/*
 * VeryComplexListDataEvent.java
 *
 * Created on November 6, 2003, 10:28 AM
 */

package org.netbeans.swing.tabcontrol.event;

import org.netbeans.swing.tabcontrol.TabData;

import java.util.Arrays;

/**
 * Event which provides granular data on setTabs() events which may contain
 * arbitrary differences, moves, additions and removals of array contents (or no
 * changes at all).
 * <p>
 * This event class is used in the case of calls to TabDataModel.setTabs(),
 * where one array of TabData objects is replaced with a different array of
 * TabData objects, which may contain additions, removals, deletions or moves.
 * The heavy lifting is done by <code>ArrayDiff</code>, which provides lists of
 * the affected indices for those things that are added/removed/changed/
 * deleted.
 * <p>
 * Note that this class should eventually be merged with ComplexListDataEvent, along with
 * some normalization of how things are done - it was written for expedience, not beauty.
 *
 * @author Tim Boudreau
 * @see org.netbeans.swing.tabcontrol.event.ArrayDiff
 */
public final class VeryComplexListDataEvent extends ComplexListDataEvent {
    TabData[] old, nue;
    
    //XXX, probably the structure of ComplexListDataEvent should eventually
    //be modified to work more like this; the question is if it's killing
    //a mosquito with a sledgehammer for simple changes - most changes will
    //be simple, after all.
    
    /**
     * Creates a new instance of VeryComplexListDataEvent
     */
    public VeryComplexListDataEvent(Object source, TabData[] old,
                                    TabData[] nue) {
        super(source, ITEMS_CHANGED, -1, -1);
        this.old = old;
        this.nue = nue;
    }

    /**
     * Returns an ArrayDiff object if the two arrays this event was created with
     * are not identical
     */
    public ArrayDiff getDiff() {
        return ArrayDiff.createDiff(old, nue);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("VeryComplexListEvent - old array: ");
        sb.append(Arrays.asList(old));
        sb.append(" new array: ");
        sb.append(Arrays.asList(nue));
        sb.append(" diff: ");
        sb.append(getDiff());
        return sb.toString();
    }

    private static final void arr2str(Object[] o, StringBuffer sb) {
        for (int i = 0; i < o.length; i++) {
            sb.append(o[i]);
            if (i != o.length - 1) {
                sb.append(","); //NOI18N
            }
        }
    }

}
