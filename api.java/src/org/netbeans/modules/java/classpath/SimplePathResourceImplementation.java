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
