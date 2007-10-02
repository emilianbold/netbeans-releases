/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    protected final String signature;
    private String[] args;
    private String name;
    private List<String> parameters;
    private boolean smart;
    private boolean attribute;

    private IndexedMethod(String signature, RubyIndex index, String fileUrl, String fqn,
        String clz, String require, Set<Modifier> modifiers, String attributes) {
        super(index, fileUrl, fqn, clz, require, modifiers, attributes);
        this.signature = signature;
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
            int parenIndex = signature.indexOf('(');

            if (parenIndex == -1) {
                name = signature;
            } else {
                name = signature.substring(0, parenIndex);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedMethod other = (IndexedMethod) obj;
        if (this.signature != other.signature && (this.signature == null || !this.signature.equals(other.signature))) {
            return false;
        }
        if (this.fqn != other.fqn && (this.fqn == null || !this.fqn.equals(other.fqn))) {
            return false;
        }
        if (this.attributes != other.attributes && (this.attributes == null || !this.attributes.equals(other.attributes))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.signature != null ? this.signature.hashCode() : 0);
        hash = 53 * hash + (this.fqn != null ? this.fqn.hashCode() : 0);
        hash = 53 * hash + (this.attributes != null ? this.attributes.hashCode() : 0);
        return hash;
    }
    
    
}
