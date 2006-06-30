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
 * Any class which contains methods requiring waiting or
 * sleeping should implement this interface.  Waiting and
 * sleeping operations have time limits that can be set or
 * returned using the methods of this interface.
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public interface Timeoutable {
    /**
     * Defines current timeouts.
     * @param t A collection of timeout assignments.
     * @see #getTimeouts
     */
    public void setTimeouts(Timeouts t);

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see #setTimeouts
     */
    public Timeouts getTimeouts();
}
