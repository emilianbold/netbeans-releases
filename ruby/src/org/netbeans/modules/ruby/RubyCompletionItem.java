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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.jrubyparser.ast.Node;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.ruby.elements.ClassElement;
import org.netbeans.modules.ruby.elements.Element;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedField;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.IndexedVariable;
import org.netbeans.modules.ruby.elements.KeywordElement;
import org.openide.util.ImageUtilities;

class RubyCompletionItem extends DefaultCompletionProposal {

    private static final boolean FORCE_COMPLETION_SPACES = Boolean.getBoolean("ruby.complete.spaces"); // NOI18N
    protected final CompletionRequest request;
    protected final Element element;
    protected boolean symbol;

    RubyCompletionItem(Element element, int anchorOffset, CompletionRequest request) {
        this.element = element;
        this.anchorOffset = anchorOffset;
        this.request = request;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    public void setSymbol(boolean symbol) {
        this.symbol = symbol;
    }

    @Override
    public String getInsertPrefix() {
        if (symbol) {
            return ":" + getName();
        } else {
            return getName();
        }
    }

    public ElementHandle getElement() {
        return element;
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        if (element.getKind() == ElementKind.GLOBAL && (element instanceof IndexedVariable)) {
            IndexedVariable idx = (IndexedVariable) element;

            String in = idx.getIn();
            if (in != null) {
                formatter.appendText(in);
                return formatter.getText();
            }
        }

        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override
    public String toString() {
        String cls = getClass().getName();
        cls = cls.substring(cls.lastIndexOf('.') + 1);

        return cls + "(" + getKind() + "): " + getName();
    }

    @Override
    public String[] getParamListDelimiters() {
        return new String[]{"(", ")"}; // NOI18N
    }

    static class KeywordItem extends RubyCompletionItem {

        private static ImageIcon keywordIcon;
        private static final String RUBY_KEYWORD = "org/netbeans/modules/ruby/jruby.png"; //NOI18N
        private final String keyword;
        private final String description;

        KeywordItem(String keyword, String description, int anchorOffset, CompletionRequest request) {
            super(null, anchorOffset, request);
            this.keyword = keyword;
            this.description = description;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(final HtmlFormatter formatter) {
            return null;
        }

        @Override
        public String getLhsHtml(final HtmlFormatter formatter) {
            ElementKind kind = getKind();
            formatter.name(kind, true);
            formatter.appendText(keyword);
            formatter.appendText(" "); // NOI18N
            formatter.name(kind, false);
            if (description != null) {
                formatter.appendHtml(description);
            }
            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = ImageUtilities.loadImageIcon(RUBY_KEYWORD, false);
            }

            return keywordIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return new KeywordElement(keyword);
        }
    }

    static class ClassItem extends RubyCompletionItem {

        ClassItem(Element element, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            String in = ((ClassElement) element).getIn();

            if (in != null) {
                formatter.appendText(in);
            } else {
                return null;
            }

            return formatter.getText();
        }
    }

    static class FieldItem extends RubyCompletionItem {

        FieldItem(Element element, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if (element instanceof IndexedField) {
                IndexedField field = (IndexedField) element;
                boolean emphasize = !field.isInherited();
                if (emphasize) {
                    formatter.emphasis(true);
                }
                formatter.name(ElementKind.FIELD, true);
                formatter.appendText(getName());
                formatter.name(ElementKind.FIELD, false);
                if (emphasize) {
                    formatter.emphasis(false);
                }

                return formatter.getText();
            }
            return super.getLhsHtml(formatter);
        }

        @Override
        public String getInsertPrefix() {
            String name;
            if (element.getModifiers().contains(Modifier.STATIC)) {
                name = "@@" + getName();
            } else {
                name = "@" + getName();
            }
            if (symbol) {
                name = ":" + name;
            }

            return name;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // Top level methods (defined on Object) : print
            // the defining file instead
            if (element instanceof IndexedField) {
                IndexedField idx = (IndexedField) element;

                // TODO - check if top level?
                //if (me.isTopLevel() && me instanceof IndexedMethod) {
                //IndexedMethod im = (IndexedMethod)element;
                //if (im.isTopLevel() && im.getRequire() != null) {
                //    formatter.appendText(im.getRequire());
                //
                //    return formatter.getText();
                //}
                //}

                String in = idx.getIn();
                if (in != null) {
                    formatter.appendText(in);
                    return formatter.getText();
                }
            }

            return null;
        }
    }

    static class ParameterItem extends RubyCompletionItem {

        private static ImageIcon symbolIcon;
        private static final String CONSTANT_ICON = "org/netbeans/modules/ruby/symbol.png"; //NOI18N
        private final String name;
        private final String desc;
        private final String insert;

        ParameterItem(IndexedMethod element, String name, String symbol, String insert, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
            this.name = name;
            this.desc = symbol;
            this.insert = insert;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (desc != null) {
                formatter.appendText(desc);
                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PARAMETER;
        }

        @Override
        public ImageIcon getIcon() {
            if (symbolIcon == null) {
                symbolIcon = ImageUtilities.loadImageIcon(CONSTANT_ICON, false);
            }

            return symbolIcon;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getInsertPrefix() {
            return insert;
        }
    }

    static class CallItem extends MethodItem {

        private final IndexedMethod method;
        private final int index;

        CallItem(IndexedMethod method, int parameterIndex, int anchorOffset, CompletionRequest request) {
            super(method, anchorOffset, request);
            this.method = method;
            this.index = parameterIndex;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CALL;
        }

        @Override
        public String getInsertPrefix() {
            return "";
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();
            formatter.name(kind, true);
            formatter.appendText(getName());

            List<String> parameters = method.getParameters();

            if ((parameters != null) && (!parameters.isEmpty())) {
                formatter.appendHtml("("); // NOI18N

                if (index > 0 && index < parameters.size()) {
                    formatter.appendText("... , ");
                }

                formatter.active(true);
                formatter.appendText(parameters.get(Math.min(parameters.size() - 1, index)));
                formatter.active(false);

                if (index < parameters.size() - 1) {
                    formatter.appendText(", ...");
                }

                formatter.appendHtml(")"); // NOI18N
            }

            if (method.hasBlock() && !method.isBlockOptional()) {
                formatter.appendText(" { }");
            }

            formatter.name(kind, false);

            return formatter.getText();
        }

        @Override
        public boolean isSmart() {
            return true;
        }

        @Override
        public List<String> getInsertParams() {
            return null;
        }

        @Override
        public String getCustomInsertTemplate() {
            return null;
        }
    }

    /** Methods/attributes inferred from ActiveRecord migrations */
    static class DbItem extends RubyCompletionItem {

        private final String name;
        private final String type;

        DbItem(String name, String type, int anchorOffset, CompletionRequest request) {
            super(null, anchorOffset, request);
            this.name = name;
            this.type = type;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.emphasis(true);
            formatter.name(ElementKind.DB, true);
            formatter.appendText(getName());
            formatter.name(ElementKind.DB, false);
            formatter.emphasis(false);

            return formatter.getText();
        }

        @Override
        public String getInsertPrefix() {
            return name;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // TODO - include table name somewhere?
            formatter.appendText(type);
            return formatter.getText();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.DB;
        }

        @Override
        public ImageIcon getIcon() {
            return null;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean isSmart() {
            // All database attributes are considered smart matches
            return true;
        }
    }

    static class MethodItem extends RubyCompletionItem {

        protected final IndexedMethod method;

        MethodItem(IndexedMethod element, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
            this.method = element;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();
            boolean emphasize = !method.isInherited();
            if (emphasize) {
                formatter.emphasis(true);
            }
            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);
            if (emphasize) {
                formatter.emphasis(false);
            }

            Collection<String> parameters = method.getParameters();

            if ((parameters != null) && (!parameters.isEmpty())) {
                formatter.appendHtml("("); // NOI18N

                Iterator<String> it = parameters.iterator();

                while (it.hasNext()) { // && tIt.hasNext()) {
                    formatter.parameters(true);
                    formatter.appendText(it.next());
                    formatter.parameters(false);

                    if (it.hasNext()) {
                        formatter.appendText(", "); // NOI18N
                    }
                }

                formatter.appendHtml(")"); // NOI18N
            }

            if (method.hasBlock() && !method.isBlockOptional()) {
                formatter.appendText(" { }");
            }

            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // Top level methods (defined on Object) : print
            // the defining file instead
            if (method.isTopLevel() && method.getRequire() != null) {
                formatter.appendText(method.getRequire());

                return formatter.getText();
            }

            String in = method.getIn();

            if (in != null) {
                formatter.appendText(in);
                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public String getCustomInsertTemplate() {
            final String insertPrefix = getInsertPrefix();
            List<String> params = method.getParameters();

            String startDelimiter;
            String endDelimiter;
            boolean hasBlock = false;
            int paramCount = params.size();
            int printArgs = paramCount;

            boolean hasHashArgs = method.getEncodedAttributes() != null &&
                    method.getEncodedAttributes().indexOf("=>") != -1; // NOI18N

            if (paramCount > 0 && params.get(paramCount - 1).startsWith("&")) { // NOI18N
                hasBlock = true;
                printArgs--;

                // Force parentheses around the call when using { } blocks
                // to avoid presedence problems
                startDelimiter = "("; // NOI18N
                endDelimiter = ")"; // NOI18N
            } else if (method.hasBlock()) {
                hasBlock = true;
                if (paramCount > 0) {
                    // Force parentheses around the call when using { } blocks
                    // to avoid presedence problems
                    startDelimiter = "("; // NOI18N
                    endDelimiter = ")"; // NOI18N
                } else {
                    startDelimiter = "";
                    endDelimiter = "";
                }
            } else {
                String[] delimiters = getParamListDelimiters();
                assert delimiters.length == 2;
                startDelimiter = delimiters[0];
                endDelimiter = delimiters[1];

                // When there are no args, don't use parentheses - and no spaces
                // Don't add two blank spaces for the case where there are no args
                if (printArgs == 0 /*&& startDelimiter.length() > 0 && startDelimiter.charAt(0) == ' '*/) {
                    startDelimiter = "";
                    endDelimiter = "";
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(insertPrefix);

            if (hasHashArgs && skipHashes()) {
                // Uhm, no don't do this until we get to the first arg that takes a hash
                // For methods with hashes, rely on code completion to insert args
                sb.append(getInsertSuffix());
                return sb.toString();
            }

            sb.append(startDelimiter);

            int id = 1;
            for (int i = 0; i < printArgs; i++) {
                String paramDesc = params.get(i);
                sb.append("${"); //NOI18N
                // Ensure that we don't use one of the "known" logical parameters
                // such that a parameter like "path" gets replaced with the source file
                // path!
                sb.append("ruby-cc-"); // NOI18N
                sb.append(Integer.toString(id++));
                sb.append(" default=\""); // NOI18N
                sb.append(paramDesc);
                sb.append("\""); // NOI18N
                sb.append("}"); //NOI18N
                if (i < printArgs - 1) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(endDelimiter);

            if (hasBlock) {
                String[] blockArgs = null;
                String attrs = method.getEncodedAttributes();
                if (attrs != null) {
                    int yieldNameBegin = attrs.indexOf(';');
                    if (yieldNameBegin != -1) {
                        int yieldNameEnd = attrs.indexOf(';', yieldNameBegin + 1);
                        if (yieldNameEnd != -1) {
                            blockArgs = attrs.substring(yieldNameBegin + 1,
                                    yieldNameEnd).split(",");
                        }
                    }
                }
                // TODO - if it's not an indexed class, pull this from the
                // method comments instead!

                sb.append(" { |"); // NOI18N
                if (blockArgs != null && blockArgs.length > 0) {
                    for (int i = 0; i < blockArgs.length; i++) {
                        if (i > 0) {
                            sb.append(","); // NOI18N
                        }
                        String arg = blockArgs[i];
                        sb.append("${unusedlocal defaults=\""); // NOI18N
                        sb.append(arg);
                        sb.append("\"}"); // NOI18N
                    }
                } else {
                    sb.append("${unusedlocal defaults=\"i,e\"}"); // NOI18N
                }
                sb.append("| ${"); // NOI18N
                sb.append("ruby-cc-"); // NOI18N
                sb.append(Integer.toString(id++));
                sb.append(" default=\"\"} }${cursor}"); // NOI18N

            } else {
                sb.append("${cursor}"); // NOI18N
            }

            // XXX: take this back, was commented after refactoring
//            // Facilitate method parameter completion on this item
//            try {
//                callLineStart = Utilities.getRowStart(request.doc, anchorOffset);
//                callMethod = method;
//            } catch (BadLocationException ble) {
//                Exceptions.printStackTrace(ble);
//            }

            return sb.toString();
        }

        protected boolean skipHashes() {
            return true;
        }

        protected String getInsertSuffix() {
            return " ";
        }

        @Override
        public String[] getParamListDelimiters() {
            // TODO - convert methods with NO parameters that take a block to insert { <here> }
            String n = getName();
            String in = element.getIn();
            if ("Module".equals(in)) {
                // Module.attr_ methods typically shouldn't use parentheses
                if (n.startsWith("attr_")) {
                    return new String[]{" :", " "};
                } else if (n.equals("include") || n.equals("import")) { // NOI18N
                    return new String[]{" ", " "};
                } else if (n.equals("include_package")) { // NOI18N
                    return new String[]{" '", "'"}; // NOI18N
                }
            } else if ("Kernel".equals(in)) {
                // Module.require: insert quotes!
                if (n.equals("require")) { // NOI18N
                    return new String[]{" '", "'"}; // NOI18N
                } else if (n.equals("p")) {
                    return new String[]{" ", " "}; // NOI18N
                }
            } else if ("Object".equals(in)) {
                if (n.equals("include_class")) { // NOI18N
                    return new String[]{" '", "'"}; // NOI18N
                }
            }

            if (forceCompletionSpaces()) {
                // Can't have "" as the second arg because a bug causes pressing
                // return to complete editing the last field (at he end of a buffer)
                // such that the caret ends up BEFORE the last char instead of at the
                // end of it
                boolean ambiguous = false;

                AstPath path = request.path;
                if (path != null) {
                    Iterator<Node> it = path.leafToRoot();

                    while (it.hasNext()) {
                        Node node = it.next();

                        if (AstUtilities.isCall(node)) {
                            // We're in a call; see if it has parens
                            // TODO - no problem with ambiguity if it's on a separate line, correct?

                            // Is this the method we're trying to complete?
                            if (node != request.target) {
                                // See if the outer call has parentheses!
                                ambiguous = true;
                                break;
                            }
                        }
                    }
                }

                if (ambiguous) {
                    return new String[]{"(", ")"}; // NOI18N
                } else {
                    return new String[]{" ", " "}; // NOI18N
                }
            }

            if (element instanceof IndexedElement) {
                List<String> comments = RubyCodeCompleter.getComments(null, element);
                if (comments != null && !comments.isEmpty()) {
                    // Look through the comment, attempting to identify
                    // a usage of the current method and determine whether it
                    // is using parentheses or not.
                    // We only look for comments that look like code; e.g. they
                    // are indented according to rdoc conventions.
                    String name = getName();
                    boolean spaces = false;
                    boolean parens = false;
                    for (String line : comments) {
                        if (line.startsWith("#  ")) { // NOI18N
                            // Look for usages - there could be many
                            int i = 0;
                            int length = line.length();
                            while (true) {
                                int index = line.indexOf(name, i);
                                if (index == -1) {
                                    break;
                                }
                                index += name.length();
                                i = index;
                                if (index < length) {
                                    char c = line.charAt(index);
                                    if (c == ' ') {
                                        spaces = true;
                                    } else if (c == '(') {
                                        parens = true;
                                    }
                                }
                            }
                        }
                    }

                    // Only use spaces if no parens were seen and we saw spaces
                    if (!parens && spaces) {
                        //return new String[] { " ", "" }; // NOI18N
                        // HACK because live code template editing doesn't seem to work - it places the caret at theront of the word when the last param is in the text!
                        return new String[]{" ", " "}; // NOI18N
                    }
                }

            // Take a look at the method definition itself and look for parens there

            }

            // Default - (,)
            return super.getParamListDelimiters();
        }

        @Override
        public ElementKind getKind() {
            if (method.getMethodType() == IndexedMethod.MethodType.ATTRIBUTE) {
                return ElementKind.ATTRIBUTE;
            }

            return element.getKind();
        }
    }

    /**
     * Represents a completion item for a dynamic finder method, such as 
     * "find_all_by_name_and_price".
     */
    static class FinderMethodItem extends MethodItem {

        public FinderMethodItem(IndexedMethod element, int anchorOffset, CompletionRequest request) {
            super(element, anchorOffset, request);
        }

        @Override
        protected boolean skipHashes() {
            // XXX: should return false, returning true for perf reasons now
            return true;
        }
    }

    static class VirtualFinderMethodItem extends MethodItem {

        private final String prefix;

        public VirtualFinderMethodItem(IndexedMethod element, int anchorOffset, CompletionRequest request, String prefix) {
            super(element, anchorOffset, request);
            this.prefix = prefix;
        }

        @Override
        protected boolean skipHashes() {
            // XXX: should return false, returning true for perf reasons now
            return true;
        }

        @Override
        protected String getInsertSuffix() {
            return "";
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();
            boolean emphasize = !method.isInherited();
            if (emphasize) {
                formatter.emphasis(true);
            }
            formatter.name(kind, true);
            formatter.appendText(prefix + "...");
            formatter.name(kind, false);
            if (emphasize) {
                formatter.emphasis(false);
            }
            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // Top level methods (defined on Object) : print
            // the defining file instead
            if (method.isTopLevel() && method.getRequire() != null) {
                formatter.appendText(method.getRequire());

                return formatter.getText();
            }

            String in = method.getIn();

            if (in != null) {
                formatter.appendText(in);
                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public String getInsertPrefix() {
            return prefix;
        }


    }

    private static boolean forceCompletionSpaces() {
        return FORCE_COMPLETION_SPACES;
    }
}
