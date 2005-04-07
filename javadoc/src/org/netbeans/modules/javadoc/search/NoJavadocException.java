/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc.search;

import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Suchomel
 * @version 0.1
 */
public class NoJavadocException extends Exception {

    /** Creates new NoJavadocException */
    public NoJavadocException() {
        super(NbBundle.getMessage(NoJavadocException.class, "MSG_NoDoc" )); // NOI18N
    }
    
    /** Overrides toString() method */
    public String toString(){
        return NbBundle.getMessage(NoJavadocException.class, "MSG_NoDoc" ); // NOI18N
    }
}
