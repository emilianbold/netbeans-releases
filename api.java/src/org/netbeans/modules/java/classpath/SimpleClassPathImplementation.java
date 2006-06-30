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
