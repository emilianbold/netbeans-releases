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

package org.netbeans.jemmy.util;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;

import java.awt.Component;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * Implementation of org.netbeans.jemmy.ComponentChooser interface.
 * Class can be used to find component by its field/methods values. <br>
 * Example:
 * <pre>
 * 	    String[] methods = {"getClientProperty"};
 * 	    Object[][] params = {{"classname"}};
 * 	    Class[][] classes = {{Object.class}};
 * 	    Object[] results = {"javax.swing.JCheckBox"};
 * 
 * 	    JCheckBox box = JCheckBoxOperator.findJCheckBox(frm0, new PropChooser(methods, params, classes, results));
 * </pre>
 * Or:
 * <pre>
 * 	    String[] methods = {"getText"};
 * 	    Object[] results = {"Open"};
 * 
 * 	    JButtonOperator box = new JButtonOperator(containerOperator, new PropChooser(fields, results));
 * </pre>
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class PropChooser implements ComponentChooser, Outputable{

    /**
     * Names of methods to check.
     */
    protected String[] propNames;

    /**
     * Methods parameters.
     */
    protected Object[][] params;

    /**
     * Classes of parameters.
     */
    protected Class[][] classes;

    /**
     * Expected results of methods.
     */
    protected Object[] results;

    private TestOut output;

    /**
     * Constructs a PropChooser object.
     * @param propNames Names of methods/fields
     * @param params Parameters values for methods. <BR>
     * params[0] is an array of parameters for propNames[0] methods. <BR>
     * If propNames[0] is a field, params[0] is ignored.
     * @param classes Parameters classes.
     * @param results Objects to compare method/field values to. <BR>
     * A value of propNames[0] method/field should be equal to results[0] object.
     */
    public PropChooser(String[] propNames, 
		       Object[][] params, 
		       Class[][] classes, 
		       Object[] results) {
	this.propNames = propNames;
	this.results = results;
	if(params != null) {
	    this.params = params;
	} else {
	    this.params = new Object[propNames.length][0];
	}
	if(classes != null) {
	    this.classes = classes; 
	} else {
	    this.classes = new Class[this.params.length][0];
	    for(int i = 0; i < this.params.length; i++) {
		Class[] clsss = new Class[this.params[i].length];
		for(int j = 0; j < this.params[i].length; j++) {
		    clsss[j] = this.params[i][j].getClass();
		}
		this.classes[i] = clsss;
	    }
	}
	setOutput(JemmyProperties.getCurrentOutput());
    }

    /**
     * Constructs a PropChooser object for checking of methods
     * with no parameters.
     * @param propNames Names of methods/fields
     * @param results Objects to compare method/field values to.
     */
    public PropChooser(String[] propNames, 
		       Object[] results) {
	this(propNames, null, null, results);
    }

    public void setOutput(TestOut output) {
	this.output = output;
    }

    public TestOut getOutput() {
	return(output);
    }

    public boolean checkComponent(Component comp) {
	try {
	    String propName = null;
	    Object value;
	    ClassReference disp = new ClassReference(comp);
	    for(int i = 0; i < propNames.length; i++) {
		propName = propNames[i];
		if(propName != null) {
		    if(isField(comp, propName, classes[i])) {
			try {
			    value = disp.getField(propName);
			} catch(IllegalStateException e) {
			    output.printStackTrace(e);
			    return(false);
			} catch(NoSuchFieldException e) {
			    output.printStackTrace(e);
			    return(false);
			} catch(IllegalAccessException e) {
			    output.printStackTrace(e);
			    return(false);
			}
		    } else {
			try {
			    value = disp.invokeMethod(propName, params[i], classes[i]);
			} catch(InvocationTargetException e) {
			    output.printStackTrace(e);
			    return(false);
			} catch(IllegalStateException e) {
			    output.printStackTrace(e);
			    return(false);
			} catch(NoSuchMethodException e) {
			    output.printStackTrace(e);
			    return(false);
			} catch(IllegalAccessException e) {
			    output.printStackTrace(e);
			    return(false);
			}
		    }
		    if(!checkProperty(value, results[i])) {
			return(false);
		    }
		}
	    }
	    return(true);
	} catch (SecurityException e) {
	    output.printStackTrace(e);
	    return(false);
	}
    }

    public String getDescription() {
	String result = "";
	for(int i = 0; i < propNames.length; i++) {
	    result = result + " " + propNames[i];
	}
	return("Component by properties array\n    :" + result);
    }

    /**
     * Method to check one method result with an etalon.
     * Can be overrided by a subclass.
     * @param value Method/field value
     * @param etalon Object to compare to.
     * @return true if the value matches the etalon.
     */
    protected boolean checkProperty(Object value, Object etalon) {
	return(value.equals(etalon));
    }

    /* try to define if propName is a field or method*/
    private boolean isField(Component comp, String propName, Class[] params) 
	throws SecurityException {
	try {
	    comp.getClass().getField(propName);
	    comp.getClass().getMethod(propName, params);
	} catch(NoSuchMethodException e) {
	    return(true);
	} catch(NoSuchFieldException e) {
	    return(false);
	}
	return(true);
    }
}
