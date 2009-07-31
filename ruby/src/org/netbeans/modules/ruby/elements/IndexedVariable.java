/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * License Header, with the variables enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.elements;

import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.ruby.RubyIndex;
import org.openide.filesystems.FileObject;

/**
 * @author Tor Norbye
 */
public class IndexedVariable extends IndexedElement {

    private boolean smart;
    private String name;
    private boolean inherited;
    private ElementKind kind;
    private String in;

    private IndexedVariable(String name, RubyIndex index, IndexResult result, String fqn,
        String clz, String require, String attributes, int flags, ElementKind kind, FileObject context) {
        super(index, result, fqn, clz, require, attributes, flags, context);
        this.name = name;
        this.kind = kind;
    }

    public static IndexedVariable create(RubyIndex index, String name, String fqn, String clz,
        IndexResult result, String require, String attributes, int flags, ElementKind kind, FileObject context) {
        IndexedVariable m =
            new IndexedVariable(name, index, result, fqn, clz, require, attributes, flags, kind, context);

        return m;
    }

    public ElementKind getKind() {
        return kind;
    }
    
    @Override
    public String getSignature() {
        return fqn + "#@" + (isStatic() ? "@" : "") + name;
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
    public String getIn() {
        if (in == null) {
            if (file != null) {
                in = file.getNameExt();
            }
        }
        return in;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedVariable other = (IndexedVariable) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.fqn != other.fqn && (this.fqn == null || !this.fqn.equals(other.fqn))) {
            return false;
        }
        if (this.flags != other.flags) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 43 * hash + (this.fqn != null ? this.fqn.hashCode() : 0);
        hash = 53 * hash + flags;
        return hash;
    }

    public boolean isInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }
    public static String decodeFlags(int flags) {
        return IndexedElement.decodeFlags(flags);
    }
}
