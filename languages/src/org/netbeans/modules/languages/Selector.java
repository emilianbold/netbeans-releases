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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Jan Jancura
 */
public class Selector {
    
    public static Selector create (String selector) {
        List<String> path = new ArrayList<String> ();
        int s = 0, e = selector.indexOf ('.');
        while (e >= 0) {
            path.add (selector.substring (s, e));
            s = e + 1;
            e = selector.indexOf ('.', s);
        }
        path.add (selector.substring (s));
        return new Selector (path);
    }
    
    private List<String> path;
    
    private Selector (List<String> path) {
        this.path = path;
    }
    
    List<String> getPath () {
        return path;
    }
    
    private String asText;
    
    public String getAsString () {
        if (asText == null) {
            Iterator<String> it = path.iterator ();
            StringBuilder sb = new StringBuilder ();
            if (it.hasNext ())
                sb.append (it.next ());
            while (it.hasNext ())
                sb.append ('.').append (it.next ());
            asText = sb.toString ();
        }
        return asText;
    }
    
    public String toString () {
        return getAsString ();
    }
}
