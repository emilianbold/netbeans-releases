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

package org.netbeans.spi.debugger.ui;


/**
 * Contains various debuggercore-ui constants.
 *
 * @author   Jan Jancura
 */
public interface Constants {

    /**
     * Thread State column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String THREAD_STATE_COLUMN_ID = "ThreadState";

    /**
     * Thread Suspended column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String THREAD_SUSPENDED_COLUMN_ID = "ThreadSuspended";

    /**
     * Breakpoint Enabled column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String BREAKPOINT_ENABLED_COLUMN_ID = 
        "BreakpointEnabled";

    /**
     * CallStackFrame Location column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String CALL_STACK_FRAME_LOCATION_COLUMN_ID = 
        "CallStackFrameLocation";

    /**
     * Locals toString () column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String LOCALS_TO_STRING_COLUMN_ID = "LocalsToString";

    /**
     * Locals Tyoe column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String LOCALS_TYPE_COLUMN_ID = "LocalsType";

    /**
     * Locals Value column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String LOCALS_VALUE_COLUMN_ID = "LocalsValue";

    /**
     * Watch toString () column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     */
    public static final String WATCH_TO_STRING_COLUMN_ID = "WatchToString";

    /**
     * Watch Tyoe column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String WATCH_TYPE_COLUMN_ID = "WatchType";

    /**
     * Watch Value column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String WATCH_VALUE_COLUMN_ID = "WatchValue";

    /**
     * Session Host Name column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String SESSION_HOST_NAME_COLUMN_ID = "SessionHostName";

    /**
     * Session State column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String SESSION_STATE_COLUMN_ID = "SessionState";

    /**
     * Session Language column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String SESSION_LANGUAGE_COLUMN_ID = "SessionLanguage";
}