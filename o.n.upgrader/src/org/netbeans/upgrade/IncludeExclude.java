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

package org.netbeans.upgrade;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;
import org.openide.util.Union2;



/** A test that is initialized based on includes and excludes.
 *
 * @author Jaroslav Tulach
 */
final class IncludeExclude extends AbstractSet {
    /** List<Boolean and Pattern>
     */
    private List<Union2<Boolean, Pattern>> patterns = new ArrayList<Union2<Boolean, Pattern>> ();

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
            
            set.patterns.add (Union2.<Boolean,Pattern>createFirst(plus));
            set.patterns.add (Union2.<Boolean,Pattern>createSecond(p));
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
        
        Iterator<Union2<Boolean,Pattern>> it = patterns.iterator ();
        while (it.hasNext ()) {
            Boolean include = it.next ().first();
            Pattern p = it.next ().second();
            
            Matcher m = p.matcher (s);
            if (m.matches ()) {
                yes = include.booleanValue ();
            }
        }
        
        return yes;
    }
    
}
