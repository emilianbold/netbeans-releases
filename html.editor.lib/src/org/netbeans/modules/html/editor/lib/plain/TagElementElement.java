/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.lib.plain;

import java.util.*;
import org.netbeans.modules.html.editor.lib.api.tree.*;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.CharSequences;

/**
 *
 * @author marekfukala
 */
public class TagElementElement extends AbstractElement implements TagElement {

    private CharSequence name;
    private List<Attribute> attribs;
    private boolean empty, openTag;

    public TagElementElement(CharSequence document, int from, int length,
            CharSequence name,
            List<Attribute> attribs,
            boolean openTag,
            boolean isEmpty) {
        super(document, from, length);
        this.name = name;
        this.attribs = attribs;
        this.openTag = openTag;
        this.empty = isEmpty;
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public Collection<Attribute> attributes() {
        return attribs == null ? Collections.EMPTY_LIST : attribs;
    }

    @Override
     public Collection<Attribute> attributes(AttributeFilter filter) {
        Collection<Attribute> filtered = new ArrayList<Attribute>(attributes().size() / 2);
        for (Attribute attr : attributes()) {
            if (filter.accepts(attr)) {
                filtered.add(attr);
            }
        }
        return filtered;
    }

    @Override
    public Attribute getAttribute(String name) {
        return getAttribute(name, true);
    }

    public Attribute getAttribute(String name, boolean ignoreCase) {
        for (Attribute ta : attributes()) {
            if (LexerUtils.equals(ta.name(), name, ignoreCase, false)) {
                return ta;
            }
        }
        return null;
    }

    @Override
    public ElementType type() {
        return openTag ? ElementType.OPEN_TAG : ElementType.END_TAG;
    }

    @Override
    public CharSequence name() {
        return name;
    }

    @Override
    public CharSequence namespacePrefix() {
        int colonIndex = CharSequences.indexOf(name(), ":");
        return colonIndex == -1 ? null : name().subSequence(0, colonIndex);

    }

    @Override
    public CharSequence unqualifiedName() {
        int colonIndex = CharSequences.indexOf(name(), ":");
        return colonIndex == -1 ? name() : name().subSequence(colonIndex + 1, name().length());
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(super.toString());
        ret.append(" - {");   // NOI18N

        for (Iterator i = attributes().iterator(); i.hasNext();) {
            ret.append(i.next());
            ret.append(", ");    // NOI18N
        }

        ret.append("}");      //NOI18N
        if (isEmpty()) {
            ret.append(" (EMPTY TAG)"); //NOI18N
        }
        return ret.toString();
    }
}
