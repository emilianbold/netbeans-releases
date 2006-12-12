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

package org.netbeans.modules.languages.ext;

import org.netbeans.modules.languages.parser.ASTNode;

/**
 *
 * @author Jan Jancura
 */
public class NBS {
    
    public static Runnable hyperlink (final ASTNode n) {
        return new Runnable () {
            public void run () {
                String link = n.getAsText ();
                int i = link.lastIndexOf ('.');
                String className = link.substring (0, i).trim ();
                String method = link.substring (i + 1).trim ();
                System.out.println("className " + className);
                System.out.println("method " + method);
            }
        };
    }
}
