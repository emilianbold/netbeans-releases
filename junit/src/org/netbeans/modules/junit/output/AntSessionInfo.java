/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

    JUnitOutputReader outputReader = null;
    /** */
    long timeOfTestTaskStart;
    /** */
    TaskType currentTaskType;
    /**
     * type of the session - one of the <code>SESSION_TYPE_xxx</code> constants
     */
    TaskType sessionType;
    
    /**
     */
    AntSessionInfo() { }
    
    /**
     */
    long getTimeOfTestTaskStart() {
        return timeOfTestTaskStart;
    }
    
}
