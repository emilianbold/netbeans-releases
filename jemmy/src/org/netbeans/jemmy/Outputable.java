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
 * Communicate the identity of the output streams or writers
 * used by the application.  Communicate the identity of the input
 * stream, too. Any object with methods that generates print output
 * should implement this interface.
 *
 * @see org.netbeans.jemmy.TestOut
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public interface Outputable {

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see #getOutput
     */
    public void setOutput(TestOut out);

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see #setOutput
     */
    public TestOut getOutput();
}
