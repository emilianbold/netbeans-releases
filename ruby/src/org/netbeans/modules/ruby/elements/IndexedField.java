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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.elements;

import org.netbeans.api.gsf.ElementKind;
import java.util.Set;

import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.modules.ruby.RubyIndex;


/**
 *
 * @author Tor Norbye
 */
public class IndexedField extends IndexedElement {
    private boolean smart;
    private String name;

    private IndexedField(String name, RubyIndex index, String fileUrl, String fqn,
        String clz, String require, Set<Modifier> modifiers, String attributes) {
        super(index, fileUrl, fqn, clz, require, modifiers, attributes);
        this.name = name;
    }

    public static IndexedField create(RubyIndex index, String name, String fqn, String clz,
        String fileUrl, String require, Set<Modifier> modifiers, String attributes) {
        IndexedField m =
            new IndexedField(name, index, fileUrl, fqn, clz, require, modifiers, attributes);

        return m;
    }

    public ElementKind getKind() {
        return ElementKind.FIELD;
    }
    
    @Override
    public String getSignature() {
        return fqn + "#@" + (modifiers.contains(Modifier.STATIC) ? "@" : "") + name;
    }

    public String getName() {
        return name;
    }

    public boolean isSmart() {
        return smart;
    }
    
    public void setSmart(boolean smart) {
        this.smart = smart;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedField other = (IndexedField) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.fqn != other.fqn && (this.fqn == null || !this.fqn.equals(other.fqn))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 43 * hash + (this.fqn != null ? this.fqn.hashCode() : 0);
        return hash;
    }
}
