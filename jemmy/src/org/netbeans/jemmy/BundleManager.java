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

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import java.util.zip.ZipException;

/**
 *
 * Provides functionality to work with a bunch of resource files. <BR>
 *
 * @see org.netbeans.jemmy.Bundle
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class BundleManager extends Object {

    private Hashtable bundles;

    /**
     * Bundle manager constructor.
     */
    public BundleManager() {
	bundles = new Hashtable();
	try {
	    load();
	} catch(IOException e) {
	}
    }

    /**
     * Adds a Bundle to the managed collection of resource files.
     * @param bundle Bundle object
     * @param ID Symbolic bundle id
     * @return First parameter or null if bundle with ID already exists.
     * @see org.netbeans.jemmy.Bundle
     */
    public Bundle addBundle(Bundle bundle, String ID) {
	if(getBundle(ID) != null) {
	    return(null);
	} else {
	    bundles.put(ID, bundle);
	    return(bundle);
	}
    }

    /**
     * Removes a Bundle from the managed collection of resource files.
     * @param ID Symbolic bundle id
     * @return Removed bundle or null if no bundle ID is.
     */
    public Bundle removeBundle(String ID) {
	Bundle value = getBundle(ID);
	bundles.remove(ID);
	return(value);
    }

    /**
     * Returns a Bundle given it's symbolic ID.
     * @param ID Symbolic bundle ID
     * @return the Bundle.  A null reference is returned if
     * no bundle with the symbolic ID was found.
     */
    public Bundle getBundle(String ID) {
	return((Bundle)bundles.get(ID));
    }

    /**
     * Create a new Bundle, load resources from a simple text file,
     * and add the bundle.
     * Load resources from a text file to a new Bundle object.  The new
     * Bundle is added to the collection of objects managed by this
     * <code>BundleManager</code>.
     * 
     * @param	fileName Name of a file to load resources from.
     * @param	ID Symbolic bundle ID used to identify the new bundle used
     * to manage the resources from the file.
     * @return	a newly created bundle.
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public Bundle loadBundleFromFile(String fileName, String ID) 
	throws IOException, FileNotFoundException {
	if(getBundle(ID) != null) {
	    return(null);
	}
	Bundle bundle = new Bundle();
	bundle.loadFromFile(fileName);
	return(addBundle(bundle, ID));
    }

    /**
     * Loads resources from simple text file pointed by jemmy.resources system property.
     * The resources are loaded into the Bundle with ID "".
     * Does not do anything if jemmy.resources has not been set or is empty.
     * 
     * @return	a newly created bundle.
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public Bundle load() 
	throws IOException, FileNotFoundException {
	if(System.getProperty("jemmy.resources") != null &&
	   !System.getProperty("jemmy.resources").equals("")) {
	    return(loadBundleFromFile(System.getProperty("jemmy.resources"), ""));
	}
	return(null);
    }

    /**
     * Loads resources from file in jar archive into new Bundle object and adds it.
     * 
     * @param	fileName Name of jar file.
     * @param	entryName ?enryName? Name of file to load resources from.
     * @param	ID Symbolic bundle id
     * @return	a newly created bundle.
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public Bundle loadBundleFromJar(String fileName, String entryName, String ID) 
	throws IOException, FileNotFoundException {
	if(getBundle(ID) != null) {
	    return(null);
	}
	Bundle bundle = new Bundle();
	bundle.loadFromJar(fileName, entryName);
	return(addBundle(bundle, ID));
    }

    /**
     * Loads resources from file in zip archive into new Bundle object and adds it.
     * 
     * @param	fileName Name of jar file.
     * @param	entryName ?enryName? Name of file to load resources from.
     * @param	ID Symbolic bundle id
     * @return	a newly created bundle.
     * @exception	ZipException
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public Bundle loadBundleFromZip(String fileName, String entryName, String ID)
	throws IOException, FileNotFoundException, ZipException {
	if(getBundle(ID) != null) {
	    return(null);
	}
	Bundle bundle = new Bundle();
	bundle.loadFromZip(fileName, entryName);
	return(addBundle(bundle, ID));
    }

    /**
     * Prints bundles contents.
     * @param writer Writer to print data in.
     */
    public void print(PrintWriter writer) {
	Enumeration keys = bundles.keys();
	Bundle bundle;
	String key;
	while (keys.hasMoreElements()) {
	    key = (String)keys.nextElement();
	    writer.println("\"" + key + "\" bundle contents");
	    bundle = getBundle(key);
	    bundle.print(writer);
	}
    }

    /**
     * Prints bundles contents.
     * @param stream Stream to print data in.
     */
    public void print(PrintStream stream) {
	print(new PrintWriter(stream));
    }

    /**
     * Returns resource from ID bundle.
     * @param bundleID Bundle ID.
     * @param key Resource key.
     * @return the resource value.  If the bundle ID does not exist
     * if the resource with the given key cannot be found, a null
     * reference is returned.
     */
    public String getResource(String bundleID, String key) {
	Bundle bundle = getBundle(bundleID);
	if(bundle != null) {
	    return(bundle.getResource(key));
	}
	return(null);
    }

    /**
     * Searches for a resource in all the managed Bundles.
     * @param	key Resource key.
     * @return	first resource value found that is indexed by the given key.
     * If no resource is found, return a null reference.
     */
    public String getResource(String key) {
	Enumeration data = bundles.elements();
	String value;
	while (data.hasMoreElements()) {
	    value = ((Bundle)data.nextElement()).getResource(key);
	    if(value != null) {
		return(value);
	    }
	}
	return(null);
    }

    /**
     * Counts the number of resource occurences in all the managed Bundles.
     * @param key Resource key
     * @return the number of resource occurences with the given key among
     * all the Bundles managed by this BundleManager.
     */
    public int calculateResources(String key) {
	Enumeration data = bundles.elements();
	int count = 0;
	while (data.hasMoreElements()) {
	    if(((Bundle)data.nextElement()).getResource(key) != null) {
		count++;;
	    }
	}
	return(count);
    }

    /**
     * Creates a shallow copy of this BundleManager.
     * Does not copy bundles, only their references.
     * @return a copy of this BundleManager.
     */
    public BundleManager cloneThis() {
	BundleManager result = new BundleManager();
	Enumeration keys = bundles.keys();
	Enumeration elements = bundles.elements();
	while(keys.hasMoreElements()) {
	    result.bundles.put(keys.nextElement(),
			       elements.nextElement());
	}
	return(result);
    }
}
