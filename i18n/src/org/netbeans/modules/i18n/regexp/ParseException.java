/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.regexp;

/**
 * Singals a syntax error which occured while parsing a regular expression.
 *
 * @author  Marian Petras
 */
public class ParseException extends Exception {

    /** regular expression the syntax error was found in */
    private String regexp;

    /** position of the syntax error within the regular expression */
    private int position;

    /**
     * Constructs a <code>ParseException</code>.
     *
     * @param  regexp  regular expression a syntax error was found in
     * @param  position  position of a syntax error within the regular expression
     */
    public ParseException(String regexp, int position) {
        this.regexp = regexp;
        this.position = position;
    }

    /**
     * Returns a regular expression which caused this exception to be thrown.
     *
     * @return  regular expression containing the syntax error
     */
    public String getRegexp() {
        return regexp;
    }

    /**
     * Returns a position of the syntax error within the regular expression.
     *
     * @return  position of the syntax error within the regular expression
     * @see  #getRegexp()
     */
    public int getPosition() {
        return position;
    }

}
