/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

/**
 * Holds information about an <code>AntSession</code>.
 *
 * @author  Marian Petras
 * @see  JUnitAntLogger
 * @see  AntSession
 */
final class AntSessionInfo {

    /**
     * constant for &quot;uknown session type&quot;;
     * the value is <code>0</code> (zero)
     */
    static final int SESSION_TYPE_UNKNOWN = 0;
    /**
     * constant for &quot;JUnit test session&quot;;
     * the value is positive
     */
    static final int SESSION_TYPE_TEST = 2;
    /**
     * constant for &quot;JUnit test debugging session&quot;;
     * the value is positive
     */
    static final int SESSION_TYPE_DEBUG_TEST = 3;
    /**
     * constant for &quot;other session type&quot;;
     * the value is positive
     */
    static final int SESSION_TYPE_OTHER = 1;
    
    final JUnitOutputReader outputReader;
    /**
     * type of the session - one of the <code>SESSION_TYPE_xxx</code> constants
     */
    final int sessionType;
    
    AntSessionInfo(JUnitOutputReader outputReader, int sessionType) {
        this.outputReader = outputReader;
        this.sessionType = sessionType;
    }
    
}
