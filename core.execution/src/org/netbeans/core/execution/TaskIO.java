/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.execution;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;

import org.openide.windows.InputOutput;

/** simply contains all ins n' outs for running task
* There is one instance for every running task.
*
* @author Ales Novak
* @version 0.11 April 24, 1998
*/
class TaskIO {

    /** No name */
    static final String VOID = "noname"; // NOI18N

    /** stdout for task */
    Writer out;
    /** stderr */
    Writer err;
    /** stdin */
    Reader in;

    /** 'theme' for this task */
    InputOutput inout;

    /** name for the TaskIO */
    private String name;

    /** Should not be this TaskIO processed by IOTable? */
    boolean foreign;

    TaskIO () {
        name = VOID;
    }

    /**
    * @param inout is an InputOutput
    * @param name is a name
    */
    TaskIO (InputOutput inout) {
        this(inout, VOID);
    }

    /**
    * @param inout is an InputOutput
    * @param name is a name
    */
    TaskIO (InputOutput inout, String name) {
        this.inout = inout;
        this.name = name;
    }

    /**
    * @param inout is an InputOutput
    * @param name is a name
    * @param foreign if true then IOTable never cares about this TaskIO
    */
    TaskIO (InputOutput inout, String name, boolean foreign) {
        this.inout = inout;
        this.name = name;
        this.foreign = foreign;
    }

    /** inits out */
    void initOut () {
        if (out == null) {
            out = inout.getOut();
        }
    }

    /** inits err */
    void initErr() {
        if (err == null) {
            err = inout.getErr();
        }
    }

    /** inits in */
    void initIn() {
        if (in == null) {
            in = inout.getIn();
        }
    }

    /**
    * @return name
    */
    String getName() {
        return name;
    }

    /**
    * @return InputOutput for this TaskIO
    */
    InputOutput getInout() {
        return inout;
    }
}
