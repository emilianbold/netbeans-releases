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
