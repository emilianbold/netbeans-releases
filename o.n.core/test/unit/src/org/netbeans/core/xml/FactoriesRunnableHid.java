/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.xml;

import java.io.*;
import java.net.*;
import java.util.*; 
import javax.xml.parsers.*;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.util.*;
import org.xml.sax.*;



/**
 * Will be loaded by different classloader by FactoriesTest.
 * @author Jaroslav Tulach
 */
public class FactoriesRunnableHid extends HashMap implements Runnable {
    public void run () {
        try {
            put ("dom", javax.xml.parsers.DocumentBuilderFactory.newInstance());
            put ("sax", javax.xml.parsers.SAXParserFactory.newInstance ());
        } catch (Exception ex) {
            IllegalStateException e = new IllegalStateException (ex.getMessage ());
            e.initCause (ex);
            throw e;
        }
    }
}
