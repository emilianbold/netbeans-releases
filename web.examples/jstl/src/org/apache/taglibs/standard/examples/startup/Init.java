/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 

package org.apache.taglibs.standard.examples.startup;

import java.util.*;

import javax.servlet.*;
import org.apache.taglibs.standard.examples.beans.*;

/**
 * Initialization class. Builds all the data structures
 * used in the "examples" webapp.
 *
 * @author Pierre Delisle
 * @version $Revision$ $Date$
 */
public class Init implements ServletContextListener {
    
    //*********************************************************************
    // ServletContextListener methods
    
    // recovers the one context parameter we need
    public void contextInitialized(ServletContextEvent sce) {
        //p("contextInitialized");
        init(sce);
    }
    
    public void contextDestroyed(ServletContextEvent sce) {
        //p("contextInitialized");
    }
    
    //*********************************************************************
    // Initializations
    
    private void init(ServletContextEvent sce) {
        /*
         *  Customers
         */
        Customers.create("Richard", "Maurice", "5/15/35",
        "123 Chemin Royal", "Appt. #301",
        "Montreal", "QC", "H3J 9R9", "Canada");
        Customers.create("Mikita", "Stan", "12/25/47",
        "45 Fisher Blvd", "Suite 203",
        "Chicago", "IL", "65982", "USA", "(320)876-9784", null);
        Customers.create("Gilbert", "Rod", "3/11/51",
        "123 Main Street", "",
        "New-York City", "NY", "19432", "USA");
        Customers.create("Howe", "Gordie", "7/25/46",
        "7654 Wings Street", "",
        "Detroit", "MG", "07685", "USA", "(465)675-0761", "(465)879-9802");
        Customers.create("Sawchuk", "Terrie", "11/05/46",
        "12 Maple Leafs Avenue", "",
        "Toronto", "ON", "M5C 1Z1", "Canada");
        sce.getServletContext().setAttribute("customers", Customers.findAll());

	/**
	 * Array of primitives (int)
	 */
	int[] intArray = new int[] {10, 20, 30, 40, 50};
        sce.getServletContext().setAttribute("intArray", intArray);

	/**
	 * Array of Objects (String)
	 */
	String[] stringArray = new String[] {
	    "A first string",
	    "La deuxieme string",
	    "Ella troisiemo stringo",
	};
        sce.getServletContext().setAttribute("stringArray", stringArray);

	/**
        * String-keyed Map
        */
        Hashtable stringMap = new Hashtable();
        sce.getServletContext().setAttribute("stringMap", stringMap);
        stringMap.put("one", "uno");
        stringMap.put("two", "dos");
        stringMap.put("three", "tres");
        stringMap.put("four", "cuatro");
        stringMap.put("five", "cinco");
        stringMap.put("six", "seis");
        stringMap.put("seven", "siete");
        stringMap.put("eight", "ocho");
        stringMap.put("nine", "nueve");
        stringMap.put("ten", "diez");

        /**
         * Integer-keyed Map
	 */
	// we use a Hashtable so we can get an Enumeration easily, below
        Hashtable numberMap = new Hashtable();
	sce.getServletContext().setAttribute("numberMap", numberMap);
	numberMap.put(new Integer(1), "uno");
	numberMap.put(new Integer(2), "dos");
	numberMap.put(new Integer(3), "tres");
	numberMap.put(new Integer(4), "cuatro");
	numberMap.put(new Integer(5), "cinco");
	numberMap.put(new Integer(6), "seis");
	numberMap.put(new Integer(7), "siete");
	numberMap.put(new Integer(8), "ocho");
	numberMap.put(new Integer(9), "nueve");
	numberMap.put(new Integer(10), "diez");

	/**
	 * Enumeration
	 */
	Enumeration e = numberMap.keys();
	// don't use 'enum' for attribute name because it is a 
	// reserved word in EcmaScript.
        sce.getServletContext().setAttribute("enumeration", e);

	/**
	 * Message arguments for parametric replacement
	 */
	Object[] serverInfoArgs =
	    new Object[] {
		sce.getServletContext().getServerInfo(),
		System.getProperty("java.version")
	    };
	sce.getServletContext().setAttribute("serverInfoArgs", serverInfoArgs);
    }
    
    //*********************************************************************
    // Initializations
    
    private void p(String s) {
        System.out.println("[Init] " + s);
    }
}
