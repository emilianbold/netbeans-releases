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

package org.netbeans.modules.languages;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class Utils {

    private static Map collections;
    
    
    public static void startTest (String name, Collection c) {
        if (collections == null) {
            // init
            collections = new HashMap ();
            start ();
        }
        collections.put (name, new WeakReference (c));
    }
    
    public static void startTest (String name, Map m) {
        if (collections == null) {
            // init
            collections = new HashMap ();
            start ();
        }
        collections.put (name, new WeakReference (m));
    }
    
    private static void start () {
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                Map cs = new HashMap (collections);
                Iterator it = cs.keySet ().iterator ();
                while (it.hasNext ()) {
                    String name = (String) it.next ();
                    Object o = ((WeakReference) cs.get (name)).get ();
                    if (o == null)
                        collections.remove (name);
                    else
                        System.out.println (":" + name + " " + size (o));
                }
                start ();
            }
        }, 5000);
    }
    
    private static int size (Object o) {
        if (o instanceof Collection) {
            Collection c = (Collection) o;
            int s = c.size ();
            Iterator it = c.iterator ();
            while (it.hasNext ()) {
                Object item = it.next ();
                if (item instanceof Collection ||
                    item instanceof Map
                )
                    s += size (item);
            }
            return s;
        }
        Map m = (Map) o;
        int s = m.size ();
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            Object key = it.next ();
            if (key instanceof Collection ||
                key instanceof Map
            )
                s += size (key);
            Object value = m.get (key);
            if (value instanceof Collection ||
                value instanceof Map
            )
                s += size (value);
        }
        return s;
    }
}
