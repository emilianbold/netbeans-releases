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
package org.netbeans.modules.java.classpath;


import org.netbeans.spi.java.classpath.support.PathResourceBase;
import org.netbeans.spi.java.classpath.ClassPathImplementation;

import java.net.URL;


/**
 * Provides implementation of the single rooted PathResoruceImplementation
 */

public final class SimplePathResourceImplementation  extends PathResourceBase {

    private URL[] url;



    public SimplePathResourceImplementation (URL root) {
        if (root == null)
            throw new IllegalArgumentException ();
        this.url = new URL[] {root};
    }


    public URL[] getRoots() {
        return this.url;
    }

    public ClassPathImplementation getContent() {
        return null;
    }

    public String toString () {
        return "SimplePathResource{"+this.getRoots()[0]+"}";   //NOI18N
    }

    public int hashCode () {
        return this.url[0].hashCode();
    }

    public boolean equals (Object other) {
        if (other instanceof SimplePathResourceImplementation) {
            SimplePathResourceImplementation opr = (SimplePathResourceImplementation) other;
            return this.url[0].equals (opr.url[0]);
        }
        else
            return false;
    }
}
