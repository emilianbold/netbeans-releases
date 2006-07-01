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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.execution;

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
