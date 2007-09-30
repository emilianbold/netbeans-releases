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
package org.netbeans.modules.ruby.elements;

import java.util.Set;

import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.modules.ruby.RubyIndex;


/**
 * A class describing a Ruby class that is rin "textual form" (signature, filename, etc.)
 * obtained from the code index.
 *
 * @author Tor Norbye
 */
public final class IndexedClass extends IndexedElement implements ClassElement {
    boolean isModule;
    private String in;

    protected IndexedClass(RubyIndex index, String fileUrl, String fqn,
        String clz, String require, boolean isModule, Set<Modifier> modifiers, String attributes) {
        super(index, fileUrl, fqn, clz, require, modifiers, attributes);
        this.isModule = isModule;
    }

    public static IndexedClass create(RubyIndex index, String clz, String fqn, String fileUrl,
        String require, boolean isModule, Set<Modifier> modifiers, String attributes) {
        IndexedClass c =
            new IndexedClass(index, fileUrl, fqn, clz, require, isModule, modifiers, attributes);

        return c;
    }

    @Override
    public String getIn() {
        if (in == null) {
            if (fqn.endsWith("::" + clz)) {
                in = fqn.substring(0, fqn.length() - (clz.length() + 2));
            } else if ((require != null) && (require.length() > 0)) {
                // Show the require path instead
                in = require;
            }
        }

        return in;
    }

    // XXX Is this necessary?
    public String getSignature() {
        return fqn;
    }

    public String getName() {
        return getClz();
    }

    public ElementKind getKind() {
        return isModule ? ElementKind.MODULE : ElementKind.CLASS;
    }

    public Set<String> getIncludes() {
        return null;
    }
    
    @Override 
    public boolean equals(Object o) {
        //return ((IndexedClass)o).fqn.equals(fqn);
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        //return fqn.hashCode();
        return super.hashCode();
    }
}
