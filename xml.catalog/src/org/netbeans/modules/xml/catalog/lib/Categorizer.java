/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog.lib;

/**
 * Classifying utilities.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class Categorizer {


    /** Returns true if the string is a valid URL. */
    public static boolean isURL(String str) {
        try {
            new java.net.URL(str);
            return true;
        }
        catch (java.net.MalformedURLException e) {
            // assume the worst
        }
        return false;
    }   
}
