/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
