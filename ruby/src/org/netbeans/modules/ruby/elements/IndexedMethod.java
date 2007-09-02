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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.modules.ruby.RubyIndex;


/**
 * A class describing a Ruby method that is in "textual form" (signature, filename, etc.)
 * obtained from the code index.
 *
 * @todo Consider computing a lot of the extra info (requires, filenames, etc.)
 *  lazily - by just stashing the index map into the RubyMethod. Hopefully Lucene
 *  won't mind if we hang on to this information...
 * @todo Make an IndexedAttribute subclass which just has a different kind!
 *
 * @author Tor Norbye
 */
public final class IndexedMethod extends IndexedElement implements MethodElement {
    private String[] args;
    private String name;
    private List<String> parameters;
    private boolean smart;
    private boolean attribute;

    private IndexedMethod(String signature, RubyIndex index, String fileUrl, String fqn,
        String clz, String require, Set<Modifier> modifiers, String attributes) {
        super(signature, index, fileUrl, fqn, clz, require, modifiers, attributes);
    }

    public static IndexedMethod create(RubyIndex index, String signature, String fqn, String clz,
        String fileUrl, String require, Set<Modifier> modifiers, String attributes) {
        IndexedMethod m =
            new IndexedMethod(signature, index, fileUrl, fqn, clz, require, modifiers, attributes);

        return m;
    }

    public boolean isPrivate() {
        return modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED);
    }

    public boolean isStatic() {
        return modifiers.contains(Modifier.STATIC);
    }

    @Override
    public String toString() {
        return getSignature();
    }

    public String getName() {
        if (name == null) {
            int index = signature.indexOf('(');

            if (index == -1) {
                name = signature;
            } else {
                name = signature.substring(0, index);
            }
        }

        return name;
    }

    @Override
    public String getSignature() {
        return fqn + "#" + signature;
    }

    public String[] getArgs() {
        if (args == null) {
            // Parse signature
            int index = signature.indexOf('(');

            if (index == -1) {
                return new String[0];
            }

            String argsPortion = signature.substring(index + 1, signature.length() - 1);
            args = argsPortion.split(","); // NOI18N
        }

        return args;
    }

    public List<String> getParameters() {
        if (parameters == null) {
            String[] args = getArgs();

            if ((args != null) && (args.length > 0)) {
                parameters = new ArrayList<String>(args.length);

                for (String arg : args) {
                    parameters.add(arg);
                }
            } else {
                parameters = Collections.emptyList();
            }
        }

        return parameters;
    }

    public boolean isDeprecated() {
        return false;
    }

    public ElementKind getKind() {
        if (((name == null) && signature.startsWith("initialize(")) || // NOI18N
                ((name != null) && name.equals("initialize"))) { // NOI18N

            return ElementKind.CONSTRUCTOR;
        } else {
            return ElementKind.METHOD;
        }
    }
    
    public boolean isTopLevel() {
        if (attributes != null) {
            for (int i = 0, n = attributes.length(); i < n; i++) {
                if (attributes.charAt(i) == 't') {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean isSmart() {
        return smart;
    }
    
    public void setSmart(boolean smart) {
        this.smart = smart;
    }
    
    public boolean isAttribute() {
        return attribute;
    }
    
    public void setAttribute(boolean attribute) {
        this.attribute = attribute;
    }
}
