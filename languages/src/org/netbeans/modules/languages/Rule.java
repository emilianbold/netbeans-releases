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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages;

import java.util.List;

/**
 *
 * @author hanz
 */
public class Rule {

    private String  nt;
    private List    right;

    private Rule () {}

    public static Rule create (
        String      nt, 
        List        right
    ) {
        Rule r = new Rule ();
        r.nt = nt;
        r.right = right;
        return r;
    }

    public String getNT () {
        return nt;
    }

    public List getRight () {
        return right;
    }

    private String toString = null;

    @Override
    public String toString () {
        if (toString == null) {
            StringBuilder sb = new StringBuilder ();
            sb.append ("Rule ").append (nt).append (" = ");
            int i = 0, k = right.size ();
            if (i < k) 
                sb.append (right.get (i++));
            while (i < k)
                sb.append (' ').append (right.get (i++));
            toString = sb.toString ();
        }
        return toString;
    }
}
