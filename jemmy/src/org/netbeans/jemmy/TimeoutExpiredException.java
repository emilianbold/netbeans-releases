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
 * Exception is supposed to be used to notice that some
 * waiting was expired.
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class TimeoutExpiredException extends JemmyException{

    /**
     * Constructor.
     * @param description Waiting description.
     */
    public TimeoutExpiredException(String description) {
	super(description);
    }
}
