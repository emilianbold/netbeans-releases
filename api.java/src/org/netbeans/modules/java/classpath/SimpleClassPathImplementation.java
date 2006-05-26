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

import java.util.List;
import java.beans.PropertyChangeListener;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;


public class SimpleClassPathImplementation implements ClassPathImplementation {
    
    List<? extends PathResourceImplementation> entries;
    
    public SimpleClassPathImplementation() {
        this(new ArrayList<PathResourceImplementation>());
    }

    public SimpleClassPathImplementation(List<? extends PathResourceImplementation> entries) {
        this.entries = entries;
    }
    
    public List <? extends PathResourceImplementation> getResources() {
        return Collections.unmodifiableList(entries);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // XXX TBD
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // XXX TBD
    }
    
    public String toString () {
        StringBuilder builder = new StringBuilder ("SimpleClassPathImplementation["); //NOI18N
        for (PathResourceImplementation impl : this.entries) {
            URL[] roots = impl.getRoots();
            for (URL root : roots) {
                builder.append (root.toExternalForm());
                builder.append (", ");  //NOI18N
            }
        }
        builder.append ("]");   //NOI18N
        return builder.toString ();
    }    
}
