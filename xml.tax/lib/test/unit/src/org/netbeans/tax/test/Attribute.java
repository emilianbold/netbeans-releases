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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
