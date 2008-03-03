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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class JsAnalyzer implements StructureScanner {
    public static final String NETBEANS_IMPORT_FILE = "__netbeans_import__"; // NOI18N
    
    public List<? extends StructureItem> scan(CompilationInfo info, HtmlFormatter formatter) {
        JsParseResult result = AstUtilities.getParseResult(info);
        AnalysisResult ar = result.getStructure();

        List<?extends AstElement> elements = ar.getElements();
        List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        Map<String,List<AstElement>> classes = new HashMap<String,List<AstElement>>();
        List<AstElement> outside = new ArrayList<AstElement>();
        List<String> classNames = new ArrayList<String>(); // Preserves source order for the map
        Map<String,AstElement> realClass = new HashMap<String,AstElement>();
        for (AstElement e : elements) {
            String in = e.getIn();
            if (e.getKind() == ElementKind.CLASS) {
                if (in != null && in.length() > 0) {
                    in = in + "." + e.getName();
                } else {
                    in = e.getName();
                }
            }
            if (in != null && in.length() > 0) {
                List<AstElement> list = classes.get(in);
                if (list == null) {
                    list = new ArrayList<AstElement>();
                    classes.put(in, list);
                    classNames.add(in);
                }
                list.add(e);
            } else {
                outside.add(e);
            }
        }
        
        for (AstElement e : outside) {
            JsAnalyzer.JsStructureItem item = new JsStructureItem(e, info, formatter);
            itemList.add(item);
        }
        
        for (String clz : classNames) {
            List<AstElement> list = classes.get(clz);
            assert list != null;

            AstElement first = list.get(0);
            JsFakeClassStructureItem currentClass = new JsFakeClassStructureItem(clz, ElementKind.CLASS, 
                    first, info, formatter);
            itemList.add(currentClass);
            
            int firstAstOffset = first.getNode().getSourceStart();
            int lastAstOffset = first.getNode().getSourceEnd();
            
            for (AstElement e : list) {
                if (e.getKind() != ElementKind.CLASS) {
                    JsAnalyzer.JsStructureItem item = new JsStructureItem(e, info, formatter);
                    currentClass.addChild(item);
                } else {
                    currentClass.element = e;
                }
                Node n = e.getNode();
                if (n.getSourceStart() < firstAstOffset) {
                    firstAstOffset = n.getSourceStart();
                }
                if (n.getSourceEnd() > lastAstOffset) {
                    lastAstOffset = n.getSourceEnd();
                }
            }
            
            currentClass.begin = LexUtilities.getLexerOffset(info, firstAstOffset);
            currentClass.end = LexUtilities.getLexerOffset(info, lastAstOffset);
        }
        
        return itemList;
    }

    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        JsParseResult result = AstUtilities.getParseResult(info);
        TranslatedSource source = result.getTranslatedSource();
        AnalysisResult ar = result.getStructure();

        List<?extends AstElement> elements = ar.getElements();
        //List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        Map<String,List<OffsetRange>> folds = new HashMap<String,List<OffsetRange>>();
        List<OffsetRange> codeblocks = new ArrayList<OffsetRange>();
        folds.put("codeblocks", codeblocks); // NOI18N

        try {
            BaseDocument doc = (BaseDocument)info.getDocument();

            for (AstElement element : elements) {
                ElementKind kind = element.getKind();
                switch (kind) {
                case METHOD:
                case CONSTRUCTOR:
                case CLASS:
                case MODULE:
                    Node node = element.getNode();
                    OffsetRange range = AstUtilities.getRange(node);
                    
                    if(source != null) {
                        //recalculate the range if we parsed the virtual source
                        range = new OffsetRange(source.getLexicalOffset(range.getStart()), 
                                source.getLexicalOffset(range.getEnd()));
                    }

                    if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR ||
                        // Only make nested classes/modules foldable, similar to what the java editor is doing
                        (range.getStart() > Utilities.getRowStart(doc, range.getStart()))) {

                        int start = range.getStart();
                        // Start the fold at the END of the line
                        start = org.netbeans.editor.Utilities.getRowEnd(doc, start);
                        int end = range.getEnd();
                        if (start != (-1) && end != (-1) && start < end && end <= doc.getLength()) {
                            range = new OffsetRange(start, end);
                            codeblocks.add(range);
                        }
                    }
                    break;
                }

                assert element.getChildren().size() == 0;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return folds;
    }
    
    static AnalysisResult analyze(JsParseResult result, CompilationInfo info) {
        AnalysisResult analysisResult = new AnalysisResult(info);
        ParseTreeWalker walker = new ParseTreeWalker(analysisResult);
        Node root = result.getRootNode();
        if (root != null) {
            walker.walk(root);
        }
        analysisResult.postProcess(result);
        
        return analysisResult;
    }

    public static class AnalysisResult implements ParseTreeVisitor {
        private List<AstElement> elements = new ArrayList<AstElement>();
        private List<String> imports;
        private CompilationInfo info;
        private Map<String,String> classExtends;
        
        private AnalysisResult(CompilationInfo info) {
            this.info = info;
        }

        String getExtends(String name) {
            if (classExtends != null) {
                return classExtends.get(name);
            }
            return null;
        }
        
        Map<String,String> getExtendsMap() {
            return classExtends;
        }
        
        public boolean visit(Node node) {
            switch (node.getType()) {
            case Token.CALL: {
                if (node.hasChildren()) {
                    Node child = node.getFirstChild();
                    if (child.getType() == Token.NAME) {
                        String s = child.getString();
                        if (s.equals(NETBEANS_IMPORT_FILE)) {
                            processImports(child.getNext());
                        }
                    }
                }
                
                break;
            }
            case Token.OBJECTLIT: {
                // Foo.Bar = { foo : function() }
                String[] classes = AstUtilities.getObjectLitFqn(node);
                String className = classes[0];
                String superName = classes[1];
                if (className != null) {
                    if (className.endsWith(AstUtilities.DOT_PROTOTYPE)) {
                        className = className.substring(0, className.length()-AstUtilities.DOT_PROTOTYPE.length());

                        // Make a constructor function for this type of object
                        AstElement js = AstElement.getElement(info, node);
                        if (js != null) {
                            String name = className;
                            String in = className;
                            int dot = className.lastIndexOf('.');
                            if (dot != -1) {
                                in = className.substring(0, dot);
                                name = className.substring(dot+1);
                            }
                            js.setName(name, in);
                            js.setKind(ElementKind.CONSTRUCTOR);
                            elements.add(js);
                        }
                    }

                    if (superName != null) {
                        if (classExtends == null) {
                            classExtends = new HashMap<String, String>();
                        }
                        classExtends.put(className, superName.toString());
                        
                    }
                    
                    // TODO - only do this for capitalized names??
                    int index = 0;
                    for (Node child = node.getFirstChild(); child != null; child = child.getNext(), index++) {
                        if (child.getType() == Token.OBJLITNAME) {
                            Node f = AstUtilities.getLabelledNode(child);
                            if (f != null) {
                                AstElement js = AstElement.getElement(info, f);
                                if (js != null) {
                                    js.setName(child.getString(), className);
                                    if (f.getType() != Token.FUNCTION) {
                                        js.setKind(ElementKind.PROPERTY);
                                    }
                                    elements.add(js);
                                }
                            }
                        }
                    }
                }
                
                break;
            }
            case Token.FUNCTION: {
                FunctionNode func = (FunctionNode) node;
                String name = AstUtilities.getFunctionFqn(node);
                    
                if (name != null) {
                    String in = "";
                    boolean isInstance = true;
                    int lastDotIndex = name.lastIndexOf('.');
                    if (lastDotIndex != -1) {
                        in = name.substring(0, lastDotIndex);
                        name = name.substring(lastDotIndex+1);
                        if (in.endsWith(AstUtilities.DOT_PROTOTYPE)) {
                            in = in.substring(0, in.length()-AstUtilities.DOT_PROTOTYPE.length()); // NOI18N
                        } else {
                            isInstance = false;
                        }
                    }
                    
                    FunctionAstElement js = new FunctionAstElement(info, func);
                    js.setName(name, in);
                    if (!isInstance) {
                        js.setModifiers(AstElement.STATIC);
                    }
                    elements.add(js);
                } else {
                    // Some other dynamic function, like this:
                    //   this.timer = setInterval(function() { self.drawEffect(); }, this.interval);
                    // Skip these
                }

                break;
            }
            }
            
            return false;
        }
        
        public boolean unvisit(Node node) {
            return false;
        }

        private void postProcess(JsParseResult result) {
            if (result.getRootNode() != null) {
                VariableVisitor visitor = result.getVariableVisitor();
                Collection<Node> globalVars = visitor.getGlobalVars(true);
                if (globalVars.size() > 0) {
                    Set<String> globals = new HashSet<String>();
                    for (Node node : globalVars) {
                        String name = node.getString();
                        if (!globals.contains(name)) {
                            globals.add(name);
                            GlobalAstElement global = new GlobalAstElement(info, node);
                            elements.add(global);
                        }
                    }
                }
            }
        }

        private void processImports(Node node) {
            if (imports == null) {
                imports = new ArrayList<String>();
            }
            while (node != null) {
                assert node.getType() == Token.STRING;
                String path = node.getString();
                if (path.indexOf(",") != -1) {
                    String[] paths = path.split(",");
                    for (String s : paths) {
                        if (s.startsWith("'") || s.startsWith("\"")) {
                            imports.add(s.substring(1, s.length()-1));
                        } else {
                            imports.add(s);
                        }
                    }
                } else {
                    imports.add(path);
                }
                node = node.getNext();
            }
        }
        
        public List<String> getImports() {
            if (imports == null) {
                return Collections.emptyList();
            }
            
            return imports;
        }
        
        public List<?extends AstElement> getElements() {
            return elements;
        }
    }
    
    /** Fake up classes to wrap something like
     * Spry.Effect.Animator.prototype.stop = function()
     *  This creates a fake class "Spry", containing "Effect", containing
     *  "Animator", and so on.
     */
    private class JsFakeClassStructureItem implements StructureItem {
        private List<StructureItem> children = new ArrayList<StructureItem>();
        private String name;
        private AstElement element;
        private ElementKind kind;
        private CompilationInfo info;
        private HtmlFormatter formatter;
        private int begin;
        private int end;

        private JsFakeClassStructureItem(String name, ElementKind kind, AstElement node, CompilationInfo info, HtmlFormatter formatter) {
            this.name = name;
            this.kind = kind;
            this.element = node;
            this.info = info;
            this.formatter = formatter;
        }
        
        private void addChild(StructureItem child) {
            children.add(child);
        }

        public String getName() {
            return name;
        }

        public String getHtml() {
            formatter.reset();
            formatter.appendText(name);

            return formatter.getText();
        }

        public ElementHandle getElementHandle() {
            return element;
        }

        public ElementKind getKind() {
            return kind;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            return children.size() == 0;
        }

        public List<? extends StructureItem> getNestedItems() {
            return children;
        }

        public long getPosition() {
            return begin;
        }

        public long getEndPosition() {
            return end;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            if (!(o instanceof JsFakeClassStructureItem)) {
                return false;
            }

            JsFakeClassStructureItem d = (JsFakeClassStructureItem)o;

            if (kind != d.kind) {
                return false;
            }

            if (!getName().equals(d.getName())) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;

            hash = (29 * hash) + ((this.getName() != null) ? this.getName().hashCode() : 0);
            hash = (29 * hash) + ((this.kind != null) ? this.kind.hashCode() : 0);

            return hash;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private class JsStructureItem implements StructureItem {
        private AstElement element;
        private ElementKind kind;
        private CompilationInfo info;
        private HtmlFormatter formatter;
        private String name;

        private JsStructureItem(AstElement node, CompilationInfo info, HtmlFormatter formatter) {
            this.element = node;
            this.info = info;
            this.formatter = formatter;

            kind = node.getKind();
        }
        
        void setKind(ElementKind kind) {
            this.kind = kind;
        }
        
        void setName(String name) {
            this.name = name;
        }

        public String getName() {
            if (name == null) {
                name = element.getName();
            }
            
            return name;
        }

        public String getHtml() {
            formatter.reset();
            formatter.appendText(getName());

            if (element instanceof FunctionAstElement) {
                // Append parameters
                FunctionAstElement jn = (FunctionAstElement)element;

                Collection<String> parameters = jn.getParameters();

                if ((parameters != null) && (parameters.size() > 0)) {
                    formatter.appendHtml("(");
                    formatter.parameters(true);

                    for (Iterator<String> it = parameters.iterator(); it.hasNext();) {
                        String ve = it.next();
                        // TODO - if I know types, list the type here instead. For now, just use the parameter name instead
                        formatter.appendText(ve);

                        if (it.hasNext()) {
                            formatter.appendHtml(", ");
                        }
                    }

                    formatter.parameters(false);
                    formatter.appendHtml(")");
                }
            }

            return formatter.getText();
        }

        public ElementHandle getElementHandle() {
            return element;
        }

        public ElementKind getKind() {
            return kind;
        }

        public Set<Modifier> getModifiers() {
            return element.getModifiers();
        }

        public boolean isLeaf() {
            switch (kind) {
            case ATTRIBUTE:
            case CONSTANT:
            case CONSTRUCTOR:
            case METHOD:
            case FIELD:
            case KEYWORD:
            case VARIABLE:
            case OTHER:
            case GLOBAL:
            case PACKAGE:
            case PROPERTY:
                return true;

            case MODULE:
            case CLASS:
                return false;

            default:
                throw new RuntimeException("Unhandled kind: " + kind);
            }
        }

        public List<?extends StructureItem> getNestedItems() {
            List<AstElement> nested = element.getChildren();

            if ((nested != null) && (nested.size() > 0)) {
                List<JsStructureItem> children = new ArrayList<JsStructureItem>(nested.size());

                for (Element co : nested) {
                    children.add(new JsStructureItem((AstElement)co, info, formatter));
                }

                return children;
            } else {
                return Collections.emptyList();
            }
        }

        public long getPosition() {
            return LexUtilities.getLexerOffset(info, element.getNode().getSourceStart());
        }

        public long getEndPosition() {
            return LexUtilities.getLexerOffset(info, element.getNode().getSourceEnd());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            if (!(o instanceof JsStructureItem)) {
                return false;
            }

            JsStructureItem d = (JsStructureItem)o;

            if (kind != d.kind) {
                return false;
            }

            if (!getName().equals(d.getName())) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;

            hash = (29 * hash) + ((this.getName() != null) ? this.getName().hashCode() : 0);
            hash = (29 * hash) + ((this.kind != null) ? this.kind.hashCode() : 0);

            return hash;
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
