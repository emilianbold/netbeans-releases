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
package org.netbeans.modules.refactoring.php.findusages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.refactoring.php.findusages.AttributedNodes.AttributedElement;
import org.netbeans.modules.refactoring.php.findusages.AttributedNodes.ClassElement;
import org.netbeans.modules.refactoring.php.findusages.AttributedNodes.ClassMemberElement;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
public final class WhereUsedSupport {

    private ASTNode node;
    private FileObject fo;
    private int offset;
    private Kind kind;
    private AttributedElement aElement;
    private AttributedNodes semiAttribs;
    private Results results;
    private Set<Modifier> modifier;
    private PHPIndex idx;

    public static enum Kind {

        CLASS, METHOD, FIELD, VARIABLE,
        CONSTANT, FUNCTION, CLASS_CONSTANT
    }

    private WhereUsedSupport(PHPIndex idx,AttributedElement aElement, ASTNode node, FileObject fo) {
        this(idx, aElement, node.getStartOffset(), fo, null);
        this.node = node;
    }

    private WhereUsedSupport(PHPIndex idx, AttributedElement aElement, int offset, FileObject fo,
            AttributedNodes semiAttribs) {
        this.fo = fo;
        this.offset = offset;
        this.semiAttribs = semiAttribs;
        this.aElement = aElement;
        this.idx =idx;
        kind = getWhereUsedKind(aElement);
        this.results = new Results();
    }

    public String getName() {
        return aElement.getName();
    }

    public ASTNode getASTNode() {
        return node;
    }

    public FileObject getFileObject() {
        return fo;
    }

    public int getOffset() {
        return offset;
    }

    public Kind getKind() {
        return kind;
    }

    public ElementKind getElementKind() {
        switch (getKind()) {
            case CLASS:
                return ElementKind.CLASS;
            case CONSTANT:
                return ElementKind.CONSTANT;
            case FUNCTION:
                return ElementKind.METHOD;
            case VARIABLE:
                return ElementKind.VARIABLE;
            case METHOD:
                return ElementKind.METHOD;
            case FIELD:
                return ElementKind.FIELD;
            case CLASS_CONSTANT:
                return ElementKind.CONSTANT;
        }
        throw new IllegalStateException();
    }

    public Set<Modifier> getModifiers() {
        AttributedElement attributeElement = getAttributeElement();
        return getModifiers(attributeElement);
    }

    public WhereUsedSupport.Results getResults() {
        return results;
    }

    private WhereUsedSupport.ResultElement createResult(AttributedElement element, ASTNode node, FileObject fo) {
        return new WhereUsedSupport.ResultElement(node, fo, element);
    }

    public static WhereUsedSupport getInstance(final CompilationInfo info, final int offset) {
        List<ASTNode> path = RefactoringUtils.underCaret(info, offset);
        AttributedNodes attribs = AttributedNodes.getInstance(info);
        AttributedElement el = null;
        Collections.reverse(path);
        boolean isSelf = false;
        for (ASTNode leaf : path) {
            if (leaf instanceof StaticConstantAccess) {
                StaticConstantAccess constantAccess = (StaticConstantAccess) leaf;
                Identifier className = constantAccess.getClassName();
                Identifier constant = constantAccess.getConstant();
                leaf = (constant.getStartOffset() < offset) ? leaf : className;
            } else if (leaf instanceof StaticFieldAccess) {
                StaticFieldAccess fieldAccess = (StaticFieldAccess) leaf;
                Identifier className = fieldAccess.getClassName();
                Variable field = fieldAccess.getField();
                leaf = (field.getStartOffset() < offset) ? leaf : className;
            } else if (leaf instanceof ArrayAccess) {
                ArrayAccess arrayAccess = (ArrayAccess) leaf;
                leaf = arrayAccess.getIndex();
            } 
            el = attribs.getElement(leaf);
            if (el != null) {
                break;
            }
        }
        return (el != null) ? new WhereUsedSupport(PHPIndex.get(info.getIndex(PhpSourcePath.MIME_TYPE)),
                el, offset, info.getFileObject(), attribs) : null;
    }

    public void collectUsages(final FileObject fileObject) {    
        collectUsages(fileObject, false);
    }

    public void collectDirectSubclasses(final FileObject fileObject) {    
        collectUsages(fileObject, true);
    }
    
    private  void collectUsages(final FileObject fileObject, final boolean directSubclasses) {
        Source source = Source.forFileObject(fileObject);
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {

                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    AttributedNodes a = AttributedNodes.getInstance(parameter);
                    Map<ASTNode, AttributedElement> findOccurences = null;
                    findOccurences = (directSubclasses) ?  a.findDirectSubclasses(aElement) : 
                        a.findUsages(aElement);                    
                    if (findOccurences.size() > 0) {
                        results.addEntry(fileObject, findOccurences);
                    }
                }

                public void cancel() {
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static IndexedElement getIndexElement(final AttributedElement el) {
        IndexedElement tmpElement = null;
        if (el != null) {
            List<Union2<ASTNode, IndexedElement>> writes = el.getWrites();
            for (Union2<ASTNode, IndexedElement> union2 : writes) {
                if (union2.hasSecond()) {
                    tmpElement = union2.second();
                    break;
                }
            }
        }
        return tmpElement;
    }

    public Set<FileObject> getRelevantFiles() {
        Set<FileObject> relevantFiles = new LinkedHashSet<FileObject>();        
        SemiAttrsProviderTask attribsTask = new SemiAttrsProviderTask();
        final IndexedElement indexedElement = getIndexElement(aElement);

        if (indexedElement != null) {
            boolean declInOtherFile = !fo.equals(indexedElement.getFileObject());
            if (declInOtherFile) {
                relevantFiles.add(indexedElement.getFileObject());
                Source source = Source.forFileObject(indexedElement.getFileObject());
                try {
                    source.runUserActionTask(attribsTask, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                semiAttribs = attribsTask.get();
            }
        }

        boolean globalScope = semiAttribs.hasGlobalVisibility(aElement);
        if (globalScope) {
            relevantFiles.addAll(getAllFiles());
        } else {
            if (indexedElement != null) {
                relevantFiles.add(indexedElement.getFileObject());
            } else {
                relevantFiles.add(fo);
            }
        }
        return relevantFiles;
    }

    private Set<? extends FileObject> getAllFiles() {
        Set<FileObject> retval = new LinkedHashSet<FileObject>();
        retval.add(fo);
        retval.addAll(idx.filesWithIdentifiers(this.getName())); 
        return retval;
    }

    public AttributedElement getAttributeElement() {
        return aElement;
    }

    private Set<Modifier> getModifiers(AttributedElement attributeElement) {
        if (modifier == null) {
            Set<Modifier> retval = Collections.emptySet();
            if (attributeElement != null && attributeElement.isClassMember()) {
                ClassMemberElement element = (ClassMemberElement) attributeElement;
                if (element.getModifier() >= 0) {
                    retval = new HashSet<Modifier>();
                    if (element.isPrivate()) {
                        retval.add(Modifier.PRIVATE);
                    } else if (element.isProtected()) {
                        retval.add(Modifier.PROTECTED);
                    }
                    if (element.isPublic()) {
                        retval.add(Modifier.PUBLIC);
                    }
                    if (element.isStatic()) {
                        retval.add(Modifier.STATIC);
                    }
                }
            }
           modifier = retval;
        }
        return modifier;
    }
    
    public static boolean isAlreadyInResults(ASTNode node, Set<ASTNode> results) {
        OffsetRange newOne = new OffsetRange(node.getStartOffset(), node.getEndOffset());
        for (Iterator<ASTNode> it = results.iterator(); it.hasNext();) {
            ASTNode aSTNode = it.next();
            OffsetRange oldOne = new OffsetRange(aSTNode.getStartOffset(), aSTNode.getEndOffset());
            if (newOne.containsInclusive(oldOne.getStart()) || oldOne.containsInclusive(newOne.getStart())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean matchDirectSubclass(AttributedElement elemToBeFound, ASTNode node, AttributedElement elem) {
        boolean retval = false;
        if (elem != null && elem instanceof ClassElement && node instanceof ClassDeclaration) {
            ClassElement superClass = ((ClassElement) elem).getSuperClass();
            if (superClass != null && superClass.equals(elemToBeFound)) {
                retval = true;
            }
        }
        return retval;
    }

    public static boolean match(AttributedElement elemToBeFound, AttributedElement elem) {
        boolean retval = elemToBeFound != null && elem != null && elemToBeFound.getName().equals(elem.getName());
        if (retval) {
            WhereUsedSupport.Kind valueKind = WhereUsedSupport.getWhereUsedKind(elem);
            WhereUsedSupport.Kind elKind = WhereUsedSupport.getWhereUsedKind(elemToBeFound);
            retval = (valueKind == elKind) && elemToBeFound.getScopeName().equals(elem.getScopeName());
        }
        return retval;
    }
    
    public static WhereUsedSupport.Kind getWhereUsedKind(AttributedElement aElement) {
        Kind retval = null;
        switch (aElement.getKind()) {
            case CLASS:
                retval = Kind.CLASS;
                break;
            case CONST:
                retval = (aElement.isClassMember()) ? Kind.CLASS_CONSTANT : Kind.CONSTANT;
                break;
            case FUNC:
                retval = (aElement.isClassMember()) ? Kind.METHOD : Kind.FUNCTION;
                break;
            case VARIABLE:
                retval = (aElement.isClassMember()) ? Kind.FIELD : Kind.VARIABLE;
                break;
        }
        return retval;
    }

    public class Results {

        private Map<FileObject, Map<ASTNode, AttributedElement>> data =
                new LinkedHashMap<FileObject, Map<ASTNode, AttributedElement>>();

        private Results() {            
        }

        public Collection<FileObject> getFiles() {
            return data.keySet();
        }

        public Collection<ResultElement> getResultElements(FileObject fo) {            
            ArrayList<ResultElement> retval = new ArrayList<ResultElement>();
            Map<ASTNode, AttributedElement> values = data.get(fo);
            for (Entry<ASTNode, AttributedElement> entry : values.entrySet()) {
                retval.add(createResult(entry.getValue(), entry.getKey(), fo));
            }
            return retval;
        }

        private void addEntry(FileObject fo, Map<ASTNode, AttributedElement> map) {
            for (Entry<ASTNode, AttributedElement> entry : map.entrySet()) {
                addEntry(fo, entry.getKey(), entry.getValue());
            }
        }

        private void addEntry(FileObject fo, ASTNode node, AttributedElement elem) {
            Map<ASTNode, AttributedElement> map = data.get(fo);
            if (map == null) {
                map = new TreeMap<ASTNode, AttributedElement>(new Comparator<ASTNode>() {

                    public int compare(ASTNode o1, ASTNode o2) {
                        return Integer.valueOf(o1.getStartOffset()).compareTo(o2.getStartOffset());
                    }
                });
                data.put(fo, map);
            }
            map.put(node, elem);
            WhereUsedSupport.this.modifier = null;
        }
    }

    public class ResultElement {

        private ASTNode node;
        private FileObject fo;
        private AttributedElement aElement;
        private BaseDocument doc;

        private ResultElement(ASTNode node, FileObject fo, AttributedElement aElement) {
            this.node = node;
            this.fo = fo;
            this.aElement = aElement;
        }

        public String getName() {
            return WhereUsedSupport.this.getName();
        }

        public ASTNode getASTNode() {
            return node;
        }

        public FileObject getFileObject() {
            return fo;
        }

        public int getOffset() {
            return offset;
        }

        public Kind getKind() {
            return kind;
        }

        public ElementKind getElementKind() {
            return WhereUsedSupport.this.getElementKind();
        }

        public Set<Modifier> getModifiers() {
            return WhereUsedSupport.this.getModifiers(aElement);
        }

        public BaseDocument getDocument() {
            if (doc == null) {
                FileObject fo = getFileObject();
                try {
                    DataObject od = DataObject.find(fo);
                    EditorCookie ec = od.getCookie(EditorCookie.class);

                    if (ec != null) {
                        doc = (BaseDocument) ec.openDocument();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return doc;
        }
    }

    private class SemiAttrsProviderTask implements CancellableTask<CompilationController> {

        private final List<AttributedNodes> attResult;

        public SemiAttrsProviderTask() {
            this.attResult = new ArrayList<AttributedNodes>();
        }

        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(Phase.RESOLVED);
            AttributedNodes a = semiAttribs.getInstance(parameter);
            attResult.add(a);
        }

        public void cancel() {
        }

        public AttributedNodes get() {
            return (attResult.size() > 0) ? attResult.get(0) : null;
        }
    }
}
