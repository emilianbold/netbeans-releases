/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.upgrade;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;



/** A test that is initialized based on includes and excludes.
 *
 * @author Jaroslav Tulach
 */
final class IncludeExclude extends AbstractSet {
    /** List<Boolean and Pattern> 
     */
    private ArrayList patterns = new ArrayList ();
    
    private IncludeExclude () {
    }

    /** Reads the include/exclude set from a given reader.
     * @param r reader 
     * @return set that accepts names based on include exclude from the file
     */
    public static Set create (Reader r) throws IOException {
        IncludeExclude set = new IncludeExclude ();
        
        BufferedReader buf = new BufferedReader (r);
        for (;;) {
            String line = buf.readLine ();
            if (line == null) break;
            
            line = line.trim ();
            if (line.length () == 0 || line.startsWith ("#")) {
                continue;
            }
            
            Boolean plus;
            if (line.startsWith ("include ")) {
                line = line.substring (8);
                plus = Boolean.TRUE;
            } else {
                if (line.startsWith ("exclude ")) {
                    line = line.substring (8);
                    plus = Boolean.FALSE;
                } else {
                    throw new java.io.IOException ("Wrong line: " + line);
                }
            }
            
            Pattern p = Pattern.compile (line);
            
            set.patterns.add (plus);
            set.patterns.add (p);
        }
        
        return set; 
    }
    
    
    public Iterator iterator () {
        return null;
    }
    
    public int size () {
        return 0;
    }
    
    public boolean contains (Object o) {
        String s = (String)o;
        
        boolean yes = false;
        
        Iterator it = patterns.iterator ();
        while (it.hasNext ()) {
            Boolean include = (Boolean)it.next ();
            Pattern p = (Pattern)it.next ();
            
            Matcher m = p.matcher (s);
            if (m.matches ()) {
                yes = include.booleanValue ();
            }
        }
        
        return yes;
    }
    
}
