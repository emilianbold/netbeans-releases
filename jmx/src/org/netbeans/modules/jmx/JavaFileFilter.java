/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx;

import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.filechooser.*;


/**
 * Class resposible for the construction of a java file filter for
 * a JFileChooser
 */
public class JavaFileFilter extends FileFilter {

    private Hashtable filters = null;
    
    /**
     * Constructor
     * Instantiates the filter list
     */
    public JavaFileFilter() {
	this.filters = new Hashtable();
    }

    /**
     * Implemented method from abstract superclass
     * @param f the tested file
     * @return boolean true if the file tested is accepted as valid
     */
    public boolean accept(File f) {
	if(f != null) {
	    if(f.isDirectory()) {
		return true;
	    }
	    String extension = getExtension(f);
	    if(extension != null && filters.get(extension) != null &&
                (filters.get(extension) != "java" && // NOI18N
                    filters.get(extension) != "class")) { // NOI18N
		return true;
	    };
	}
	return false;
    }

    /**
     * Returns the extension of the given file
     * @param f the file to analyze
     * @return String the file extension or null if it has no one
     */
     public String getExtension(File f) {
	if(f != null) {
	    String filename = f.getName();
	    int i = filename.lastIndexOf('.');
	    if(i>0 && i<filename.length()-1) {
		return filename.substring(i+1).toLowerCase();
	    };
	}
	return null;
    }

    /**
     * Adds a filter extension to the filter list
     * @param extension the extension to add to the filter list
     */
    public void addExtension(String extension) {
	if(filters == null) {
	    filters = new Hashtable(5);
	}
	filters.put(extension.toLowerCase(), this);
    }


    /**
     * Returns the description of the filter list
     * @return String the description
     */
    public String getDescription() {
	return "Java/Class files"; // NOI18N
    }
}

