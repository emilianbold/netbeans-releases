/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * JavadocTestCase.java
 *
 * Created on February 7, 2003, 10:57 AM
 */

package org.netbeans.test.gui.javadoc;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.AssertionFailedError;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.AssertionFailedErrorException;

/**
 *
 * @author  mk97936
 */
public class JavadocTestCase extends JellyTestCase {
    
    /** Creates a new instance of JavadocTestCase */
    public JavadocTestCase(String testName) {
        super(testName);
        super.initEnvironment();
    }
    
    public static void assertTemplateInFile(File file, String templ, String msg) {
        
        BufferedReader br = null;
        boolean contains = false;
        
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException fnfe) {
            throw new AssertionFailedErrorException("Problems when reading file!", fnfe);
        }
        
        try {
            String line = br.readLine();            
            while (line != null) {
                if (line.indexOf(templ) != -1) {
                    contains = true;
                }
                line = br.readLine();
            }
        } catch (IOException ioe) {
            throw new AssertionFailedErrorException("Problems when reading file!", ioe);
        } 
        if (!contains) {
            throw new AssertionFailedError(msg);
        }
        
    }
    
}
