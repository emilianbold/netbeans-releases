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

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.Properties;

import java.util.jar.JarFile;

import java.util.zip.ZipFile;
import java.util.zip.ZipException;

/**
 * 
 * Load string resources from file.
 * Resources should be stored in <code>name=value</code> format.
 *
 * @see org.netbeans.jemmy.BundleManager
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class Bundle extends Object {

    private Properties resources;

    /**
     * Bunble constructor.
     */
    public Bundle() {
	resources = new Properties(); 
    }

    /**
     * Loads resources from an input stream.
     * 
     * @param	stream Stream to load resources from.
     * @exception	IOException
     */
    public void load(InputStream stream) 
	throws IOException {
	resources.load(stream);
    }

    /**
     * Loads resources from a simple file.
     * 
     * @param	fileName Name of the file to load resources from.
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public void loadFromFile(String fileName) 
	throws IOException, FileNotFoundException {
	load(new FileInputStream(fileName));
    }

    /**
     * Loads resources from a file in a jar archive.
     * 
     * @param	fileName Name of the jar archive.
     * @param	entryName ?enryName? Name of the file to load resources from.
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public void loadFromJar(String fileName, String entryName) 
	throws IOException, FileNotFoundException {
	JarFile jFile = new JarFile(fileName);
	load(jFile.getInputStream(jFile.getEntry(entryName)));
    }

    /**
     * Loads resources from a file in a zip archive.
     * 
     * @param	fileName Name of the zip archive.
     * @param	entryName ?enryName? Name of the file to load resources from.
     * @exception	ZipException
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public void loadFromZip(String fileName, String entryName)
	throws IOException, FileNotFoundException, ZipException {
	ZipFile zFile = new ZipFile(fileName);
	load(zFile.getInputStream(zFile.getEntry(entryName)));
    }

    /**
     * Prints bundle contents.
     * @param writer Writer to print data in.
     */
    public void print(PrintWriter writer) {
	Enumeration keys = resources.keys();
	while(keys.hasMoreElements()) {
	    String key = (String)keys.nextElement();
	    writer.println(key + "=" + getResource(key));
	}
    }

    /**
     * Prints bundle contents.
     * @param stream Stream to print data in.
     */
    public void print(PrintStream stream) {
	print(new PrintWriter(stream));
    }

    /**
     * Gets resource by key.
     * @param key Resource key
     * @return Resource value or null if resource was not found.
     */
    public String getResource(String key) {
	return(resources.getProperty(key));
    }

}
