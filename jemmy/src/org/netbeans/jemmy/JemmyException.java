/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 *
 * Parent of all Jemmy exceptions.
 * Exception can be throught from inside jemmy methods,
 * if some exception occurs from code invoked from jemmy.
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JemmyException extends RuntimeException{

    private Throwable innerException = null;
    private Object object = null;

    /**
     * Constructor.
     * @param description An exception description.
     */
    public JemmyException(String description) {
	super(description);
    }

    /**
     * Constructor.
     * @param description An exception description.
     * @param innerException Exception from code invoked from jemmy.
     */
    public JemmyException(String description, Throwable innerException) {
	this(description);
	this.innerException = innerException;
    }

    /**
     * Constructor.
     * @param description An exception description.
     * @param object Object regarding which exception is thrown.
     */
    public JemmyException(String description, Object object) {
	this(description);
	this.object = object;
    }

    /**
     * Constructor.
     * @param description An exception description.
     * @param innerException Exception from code invoked from jemmy.
     * @param object Object regarding which exception is thrown.
     */
    public JemmyException(String description, Throwable innerException, Object object) {
	this(description, innerException);
	this.object = object;
    }

    /**
     * Returns "object" constructor parameter.
     * @return the Object value associated with the exception.
     */
    public Object getObject() {
	return(object);
    }

    /**
     * Returns inner exception.
     * @return An inner exception.
     * @deprecated Use getInnerThrowable()
     */
    public Exception getInnerException() {
        if(innerException instanceof Exception) {
            return((Exception)innerException);
        } else {
            return(null);
        }
    }

    /**
     * Returns inner throwable.
     * @return An inner throwable.
     */
    public Throwable getInnerThrowable() {
        return(innerException);
    }

    /**
     * Prints stack trace into System.out.
     */
    public void printStackTrace() {
	printStackTrace(System.out);
    }

    /**
     * Prints stack trace.
     * @param ps PrintStream to print stack trace into.
     */
    public void printStackTrace(PrintStream ps) {
	super.printStackTrace(ps);
	if(innerException != null) {
	    ps.println("Inner exception:");
	    innerException.printStackTrace(ps);
	}
	if(object != null) {
	    ps.println("Object:");
	    ps.println(object.toString());;
	}
    }

    /**
     * Prints stack trace.
     * 
     * @param	pw PrintWriter to print stack trace into.
     * 	
     */
    public void printStackTrace(PrintWriter pw) {
	super.printStackTrace(pw);
	if(innerException != null) {
	    pw.println("Inner exception:");
	    innerException.printStackTrace(pw);
	}
	if(object != null) {
	    pw.println("Object:");
	    pw.println(object.toString());;
	}
    }
}
