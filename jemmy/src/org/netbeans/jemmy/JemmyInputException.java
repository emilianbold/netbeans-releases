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

import java.awt.Component;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 
 * Exception can be thrown as a result of incorrect input operations.
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JemmyInputException extends JemmyException {

    /**
     * Constructor.
     * @param comp Component regarding which exception is thrown.
     */    
    public JemmyInputException(Component comp) {
	super("Input exception", comp);
    }

    /**
     * Constructor.
     * @param message A descriptive message.
     * @param comp Component regarding which exception is thrown.
     */    
    public JemmyInputException(String message, Component comp) {
	super(message, comp);
    }

    /**
     * Returns component.
     */
    public Component getComponent() {
	return((Component)getObject());
    }
}
