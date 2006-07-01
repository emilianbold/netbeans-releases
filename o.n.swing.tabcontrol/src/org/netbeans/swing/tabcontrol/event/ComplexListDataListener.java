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
 *//*
 * ComplexListDataListener.java
 *
 * Created on May 26, 2003, 6:01 PM
 */

package org.netbeans.swing.tabcontrol.event;

import javax.swing.event.ListDataListener;

/**
 * An extension to javax.swing.ListDataListener to allow handling of events on
 * non-contiguous elements
 *
 * @author Tim
 */
public interface ComplexListDataListener extends ListDataListener {
    /**
     * Elements have been added at the indices specified by the event's
     * getIndices() value
     *
     * @param e The event
     */
    void indicesAdded(ComplexListDataEvent e);

    /**
     * Elements have been removed at the indices specified by the event's
     * getIndices() value
     *
     * @param e The event
     */
    void indicesRemoved(ComplexListDataEvent e);

    /**
     * Elements have been changed at the indices specified by the event's
     * getIndices() value.  If the changed data can affect display width (such
     * as a text change or a change in icon size), the event's
     * <code>isTextChanged()</code> method will return true.
     *
     * @param e The event
     */
    void indicesChanged(ComplexListDataEvent e);
}
