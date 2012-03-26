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
package org.netbeans.modules.html.editor.lib.html4parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.CharSequences;

/**
 *
 * @author marekfukala
 */
public class XmlSTElements {
    
    static abstract class Named implements NamedElement {

        private CharSequence name;
        private int from, to;
        private Node parent;

        public Named(CharSequence name, int from, int to) {
            this.name = name;
            this.from = from;
            this.to = to;
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
        public int from() {
            return from;
        }

        @Override
        public int to() {
            return to;
        }

        @Override
        public CharSequence image() {
            return name();
        }

        @Override
        public CharSequence id() {
            return name();
        }

        @Override
        public Collection<ProblemDescription> problems() {
            return Collections.emptyList();
        }
        
        void setParent(Node parent) {
            this.parent = parent;
        }

        @Override
        public Node parent() {
            return parent;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append(name())
                    .append("(")
                    .append(type().name())
                    .append(")")
                    .append("; ")
                    .append(from())
                    .append("-")
                    .append(to())
                    .toString();
        }

    }
    
    static class EmptyOT extends Named implements OpenTag {

        private Collection<Attribute> attrs;

        public EmptyOT(Collection<Attribute> attrs, CharSequence name, int from, int to) {
            super(name, from, to);
            this.attrs = attrs;
        }
        
        @Override
        public Collection<Attribute> attributes() {
            return attrs;
        }

        @Override
        public Collection<Attribute> attributes(AttributeFilter filter) {
            Collection<Attribute> filtered = new ArrayList<Attribute>(1);
            for(Attribute a : attributes()) {
                if(filter.accepts(a)) {
                    filtered.add(a);
                }
            }
            return filtered;
        }

        @Override
        public Attribute getAttribute(String name) {
            //typically very low number of attrs so the linear search doesn't hurt
            for(Attribute a : attributes()) {
                if(LexerUtils.equals(name, a.name(), true, false)) {
                    return a;
                }
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public CloseTag matchingCloseTag() {
            return null;
        }

        @Override
        public int semanticEnd() {
            return to();
        }


        @Override
        public Collection<Element> children() {
            return Collections.emptyList();
        }

        @Override
        public Collection<Element> children(ElementType type) {
            return Collections.emptyList();
        }

        @Override
        public ElementType type() {
            return ElementType.OPEN_TAG;
        }
        
    }
    
    static class OT extends EmptyOT {
        
        private Collection<Element> children;
        private CloseTag matchingEndTag;
        private int logicalEndOffset;
        
        public OT(Collection<Attribute> attrs, CharSequence name, int from, int to) {
            super(attrs, name, from, to);
            logicalEndOffset = to;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
        
        void setMatchingEndTag(CloseTag endTag) {
            this.matchingEndTag = endTag;
        }

        @Override
        public CloseTag matchingCloseTag() {
            return matchingEndTag;
        }
        
        void addChild(Element child) {
            if(children == null) {
                children = new ArrayList<Element>(1);
            }
            children.add(child);
        }
        
        @Override
        public Collection<Element> children() {
            return children == null ? Collections.<Element>emptyList() : children;
        }

        @Override
        public Collection<Element> children(ElementType type) {
            Collection<Element> filtered = new ArrayList<Element>();
            for(Element e : children()) {
                if(e.type() == type) {
                    filtered.add(e);
                }
            }
            return filtered;
        }

        void setLogicalEndOffset(int to) {
            this.logicalEndOffset = to;
        }
        
        @Override
        public int semanticEnd() {
            return logicalEndOffset;
        }
        
    }
    
    static class ET extends Named implements CloseTag {

        private OpenTag matchingOpenTag;
        
        public ET(CharSequence name, int from, int to) {
            super(name, from, to);
        }

        @Override
        public ElementType type() {
            return ElementType.CLOSE_TAG;
        }

        @Override
        public OpenTag matchingOpenTag() {
            return matchingOpenTag;
        }
        
        void setMatchingOpenTag(OpenTag openTag) {
            this.matchingOpenTag = openTag;
        }
        
    }
    
    public static class Root extends OT implements FeaturedNode {

        private String namespace;
        
        public Root(String namespace, int sourceLength) {
            super(null, "root", 0, sourceLength); //NOI18N
            this.namespace = namespace;
        }

        @Override
        public ElementType type() {
            return ElementType.ROOT;
        }

        @Override
        public Object getProperty(String propertyName) {
            if(propertyName.equalsIgnoreCase("namespace")) { //NOI18N
                return namespace;
            }
            
            return null;
        }
        
    }
}
