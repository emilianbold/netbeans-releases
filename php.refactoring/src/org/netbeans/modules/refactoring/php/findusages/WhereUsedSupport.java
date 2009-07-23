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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.core.UiUtils;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.FindUsageSupport;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.OccurencesSupport;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.refactoring.php.findusages.AttributedNodes.AttributedElement;
import org.netbeans.modules.refactoring.php.findusages.AttributedNodes.ClassElement;
import org.netbeans.modules.refactoring.php.findusages.AttributedNodes.ClassMemberElement;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Radek Matous
 */
public final class WhereUsedSupport {

    private ASTNode node;
    private FileObject fo;
    private int offset;
    private PhpKind kind;
    private ModelElement modelElement;
    private Results results;
    private Set<Modifier> modifier;
    private FindUsageSupport usageSupport;

    private WhereUsedSupport(PHPIndex idx,ModelElement aElement, ASTNode node, FileObject fo) {
        this(idx, aElement, node.getStartOffset(), fo);
        this.node = node;
    }

    private WhereUsedSupport(PHPIndex idx, ModelElement aElement, int offset, FileObject fo) {
        this.fo = fo;
        this.offset = offset;
        this.modelElement = aElement;
        this.usageSupport =FindUsageSupport.getInstance(idx, aElement);
        kind = aElement.getPhpKind();
        this.results = new Results();
    }

    public String getName() {
        return modelElement.getName();
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

    public PhpKind getKind() {
        return kind;
    }

    public ElementKind getElementKind() {
        return modelElement.getPHPElement().getKind();
    }

    public Set<Modifier> getModifiers() {
        ModelElement attributeElement = getModelElement();
        return getModifiers(attributeElement);
    }

    public WhereUsedSupport.Results getResults() {
        return results;
    }

    void collectDirectSubclasses(final FileObject fileObject) {
        try {
            ParserManager.parse(Collections.singleton(Source.create(fileObject)), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ParserResult parameter = (ParserResult) resultIterator.getParserResult();
                    AttributedNodes a = AttributedNodes.getInstance(parameter);
                    Map<ASTNode, AttributedElement> findOccurences = null;
                    findOccurences = a.findDirectSubclasses(modelElement);
                    for (Entry<ASTNode, AttributedElement> entry : findOccurences.entrySet()) {
                       results.addEntry(fileObject, entry);
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void collectUsages(FileObject fileObject) {
        Collection<Occurence> occurences = usageSupport.occurences(fileObject);
        for (Occurence occurence : occurences) {
            results.addEntry(fileObject, occurence);
        }
    }

    public static WhereUsedSupport getInstance(final ParserResult info, final int offset) {
        Model model = ModelFactory.getModel(info);
        OccurencesSupport occurencesSupport = model.getOccurencesSupport(offset);
        Occurence occurence = occurencesSupport.getOccurence();
        final boolean canContinue = occurence != null && occurence.getDeclaration() != null && occurence.getAllDeclarations().size() <= 1;
        return canContinue ? new WhereUsedSupport(PHPIndex.get(info),
                occurence.getDeclaration(), offset, info.getSnapshot().getSource().getFileObject()) : null;
    }

    public ModelElement getModelElement() {
        return modelElement;
    }

    Set<FileObject> getRelevantFiles() {
        return usageSupport.inFiles();
    }

    private Set<Modifier> getModifiers(ModelElement mElement) {
        if (modifier == null) {
            Set<Modifier> retval = Collections.emptySet();
            if (mElement != null && mElement.getInScope() instanceof TypeScope) {
                retval = new HashSet<Modifier>();
                if (mElement.getPhpModifiers().isPrivate()) {
                    retval.add(Modifier.PRIVATE);
                } else if (mElement.getPhpModifiers().isProtected()) {
                    retval.add(Modifier.PROTECTED);
                }
                if (mElement.getPhpModifiers().isPublic()) {
                    retval.add(Modifier.PUBLIC);
                }
                if (mElement.getPhpModifiers().isStatic()) {
                    retval.add(Modifier.STATIC);
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

    public static boolean matchDirectSubclass(ModelElement elemToBeFound, ASTNode node, AttributedElement elem) {
        boolean retval = false;
        if (elem != null && elem instanceof ClassElement && node instanceof ClassDeclaration) {
            ClassElement superClass = ((ClassElement) elem).getSuperClass();
            if (superClass != null && superClass.getName().equals(elemToBeFound.getName())) {
                retval = true;
            }
        }
        return retval;
    }

    public class Results {
        Collection<WhereUsedElement> elements = new TreeSet<WhereUsedElement>(new Comparator<WhereUsedElement>() {
            public int compare(WhereUsedElement o1, WhereUsedElement o2) {
                String path1 = o1.getParentFile().getPath();
                String path2 = o2.getParentFile().getPath();
                int retval = path1.compareTo(path2);
                if (retval == 0) {
                    int offset1 = o1.getPosition().getBegin().getOffset();
                    int offset2 = o2.getPosition().getBegin().getOffset();
                    retval =  offset1 < offset2 ? -1 : 1;
                }
                return retval;
            }
        });

        private Results() {
        }
        private void addEntry(FileObject fo, Occurence occurence) {
            ModelElement decl = occurence.getDeclaration();
            Icon icon = UiUtils.getElementIcon(WhereUsedSupport.this.getElementKind(), decl.getPHPElement().getModifiers());
            elements.add(WhereUsedElement.create(decl.getName(), fo, occurence.getOccurenceRange(), icon));
        }
        private void addEntry(FileObject fo, Entry<ASTNode, AttributedElement> entry) {
            AttributedElement element = entry.getValue();
            ASTNode node = entry.getKey();
            if (node instanceof ClassDeclaration) {
                node = ((ClassDeclaration)node).getName();
            }
            OffsetRange range = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            Icon icon = UiUtils.getElementIcon(WhereUsedSupport.this.getElementKind(), getModifiers(element));
            elements.add(WhereUsedElement.create(element.getName(), fo, range, icon));
        }
        public Collection<WhereUsedElement> getResultElements() {
            return elements;
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

    }
}
