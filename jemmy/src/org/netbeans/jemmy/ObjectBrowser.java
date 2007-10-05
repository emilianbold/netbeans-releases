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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

/**
 *
 * Class to display information about object: fields, methods, ancestors and so on.
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class ObjectBrowser implements Outputable {
    private Object object;

    private TestOut output;

    /**
     * Constructor.
     */
    public ObjectBrowser() {
    }

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #getOutput
     */
    public void setOutput(TestOut out) {
	output = out;
    }

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #setOutput
     */
    public TestOut getOutput() {
	return(output);
    }

    /**
     * Specifies the object value.
     * @param obj Object to work with.
     * @see #getObject
     */
    public void setObject(Object obj) {
	object = obj;
    }

    /**
     * Returns the object value.
     * @return Current object.
     * @see #setObject
     */
    public Object getObject() {
	return(object);
    }

    /**
     * Prints <code>toString()</code> information.
     */
    public void printToString() {
	output.printLine(object.toString());
    }

    /**
     * Prints object fields names and values.
     */
    public void printFields() {
	Class cl = object.getClass();
	output.printLine("Class: " + cl.getName());
	output.printLine("Fields: ");
	Field[] fields = cl.getFields();
	for(int i = 0; i < fields.length; i++) {
	    output.printLine(Modifier.toString(fields[i].getModifiers()) + " " + 
			  fields[i].getType().getName() + " " + 
			  fields[i].getName());
	    Object value = "Inaccessible";
	    try {
		value = fields[i].get(object);
	    } catch(IllegalAccessException e) {
	    }
	    output.printLine("    Value: " + value.toString());
	}
    }

    /**
     * Prints object methods names and parameters.
     */
    public void printMethods() {
	Class cl = object.getClass();
	output.printLine("Class: " + cl.getName());
	output.printLine("Methods: ");
	Method[] methods = cl.getMethods();
	for(int i = 0; i < methods.length; i++) {
	    output.printLine(Modifier.toString(methods[i].getModifiers()) + " " + 
			 methods[i].getReturnType().getName() + " " + 
			 methods[i].getName());
	    Class[] params = methods[i].getParameterTypes();
	    for(int j = 0; j < params.length; j++) {
		output.printLine("    " + params[j].getName());
	    }
	}
    }

    /**
     * Prints allsuperclasses names.
     */
    public void printClasses() {
	Class cl = object.getClass();
	do {
	    output.printLine(cl.getName());
	} while((cl = cl.getSuperclass()) != null);
    }

    /**
     * Prints everything.
     */
    public void printFull() {
	printFields();
	printMethods();
    }
}
