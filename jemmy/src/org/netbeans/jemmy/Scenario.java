/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy;

/**
 * 
 * A test scenario.  This interface provides a mechanism for
 * putting something into execution.  The execution is conditioned
 * in a very general way by passing a <code>java.lang.Object</code>
 * to it's <code>runIt</code> method.
 *
 * @see Test
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public interface Scenario {

    /**
     * Defines a way to execute this test scenario.
     * @param param An object passed to configure the test scenario
     * execution.  For example, this parameter might be a
     * <code>java.lang.String[]<code> object that lists the
     * command line arguments to the Java application corresponding
     * to a test.
     * @return an int that tells something about the execution.
     * For, example, a status code.
     */
    public int runIt(Object param);
}
