/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.test;

import org.netbeans.tax.*;
import java.util.Iterator;

public class Attribute {

    public static void main (String args[]) throws Exception {
        TreeElement element = new TreeElement ("element");
        element.addAttribute ("a", "a");
       
        print ("New attribute 'a'.", element);

        //I can rename attribuite to existing one.
        element.addAttribute ("b", "b");

        print ("New attribute 'b'.", element);

        TreeAttribute attr = element.getAttribute ("b");
        attr.setQName ("a");
        
        print ("Change atribute 'b' name to 'a'!", element);
        
        // I can get not existing attribute.
        System.out.println ("Attribute 'b'!");
        print (element.getAttribute ("b"));
    }
    
    private static void print (String title, TreeElement element) {
        System.out.println ("-> " + title);
        
        Iterator it = element.getAttributes().iterator();
        while (it.hasNext()) {
            print ((TreeAttribute)it.next());
        }
        
        System.out.println ("");
    }
    
    private static void print (TreeAttribute a) {
        if ( a == null ) {
            System.out.println (a);
        } else {
            System.out.println (a.getQName() + " = \"" + a.getValue() + "\"");
        }
    }

}
