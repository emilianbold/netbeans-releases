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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
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
