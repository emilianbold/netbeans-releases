/*
 * NBS.java
 *
 * Created on May 18, 2006, 1:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
