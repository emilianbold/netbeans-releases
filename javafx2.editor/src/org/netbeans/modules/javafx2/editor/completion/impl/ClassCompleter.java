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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class ClassCompleter implements Completer, Completer.Factory {
    private static final Logger LOG = Logger.getLogger(ClassCompleter.class.getName());
    
    private static final int NODE_PRIORITY = 100;
    private static final int IMPORTED_PRIORITY = 50;
    private static final int OTHER_PRIORITY = 200;
    private static final int PACKAGE_PRIORITY = 150;
    
    /**
     * If the class prefix is >= than this treshold, the hierarchy + prefix filter
     * will first get the classes that match the filter, then checks their inheritance
     * hierarchy.
     */
    private static final int PREFIX_TRESHOLD = 3;
   
    
    private final CompletionContext ctx;

    public ClassCompleter() {
        this.ctx = null;
    }

    private ClassCompleter(CompletionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        if (ctx.getType() == CompletionContext.Type.BEAN ||
            ctx.getType() == CompletionContext.Type.ROOT) {
            return new ClassCompleter(ctx);
        }
        
        return null;
    }
    
    private Set<ElementHandle<TypeElement>> namedTypes;
    
    Set<ElementHandle<TypeElement>> loadDescenantsOfNode() {
        // get javafx.scene.Node descendants
        TypeElement baseClass = ctx.getCompilationInfo().getElements().getTypeElement(JavaFXEditorUtils.FXML_NODE_CLASS);
        if (baseClass == null) {
            // something wrong, fxml rt class does not exist
            LOG.warning("javafx.scene.Node class not fond");
            return Collections.emptySet();
        }
        
        ClasspathInfo info = ctx.getClasspathInfo();
        String namePrefix = ctx.getPrefix();
        
        if (namePrefix.startsWith("<")) {
            namePrefix = namePrefix.substring(1);
        }
        
        ElementHandle<TypeElement> nodeHandle = ElementHandle.create (baseClass);
        
        Set<ElementHandle<TypeElement>> allTypesSeen = new HashSet<ElementHandle<TypeElement>>();
        Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        Deque<ElementHandle<TypeElement>> handles = new LinkedList<ElementHandle<TypeElement>>();
        handles.add(nodeHandle);
        
        long time = System.currentTimeMillis();
        
        while (!handles.isEmpty()) {
            ElementHandle<TypeElement> baseHandle = handles.poll();
            LOG.log(Level.FINE, "Loading descendants of {0}", baseHandle);
            Set<ElementHandle<TypeElement>> descendants = new HashSet<ElementHandle<TypeElement>>(
                    info.getClassIndex().getElements(baseHandle,
                        EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), 
                        EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES, ClassIndex.SearchScope.SOURCE)
                    )
            );
            // eliminate duplicates
            descendants.removeAll(allTypesSeen);
            allTypesSeen.addAll(descendants);
            handles.addAll(descendants);

            LOG.log(Level.FINE, "Unique descendants: {0}", descendants);

            if (namePrefix != null) {
                // add descendants not yet seen to the next processing round
                for (ElementHandle<TypeElement> htype : descendants) {
                    if (!(
                        htype.getKind() == ElementKind.CLASS || htype.getKind() == ElementKind.INTERFACE)) {
                        continue;
                    }
                    String n = htype.getQualifiedName();
                    if (n.length() < namePrefix.length()) {
                        // shorter name, does not match prefix
                        continue;
                    }
                    int lastDot = n.lastIndexOf('.');
                    if (n.subSequence(0, namePrefix.length()).toString().compareToIgnoreCase(namePrefix) == 0 ||
                        (lastDot > 0 && (n.length() - lastDot - 1) >= namePrefix.length() &&
                            n.subSequence(lastDot + 1, lastDot + 1 + namePrefix.length()).toString().compareToIgnoreCase(namePrefix) == 0)) {
                        result.add(htype);
                    }
                }
            } else {
                result.addAll(descendants);
            }            
        }
        
        long diff = System.currentTimeMillis();
        LOG.log(Level.FINE, "Loading Node descendants took: {0}ms", diff);
        return result;
    }
    
    private Set<ElementHandle<TypeElement>> loadFromAllTypes() {
        ClasspathInfo info = ctx.getClasspathInfo();
        String namePrefix = ctx.getPrefix();
        
        if (namePrefix.startsWith("<")) {
            namePrefix = namePrefix.substring(1);
        }

        Set<ElementHandle<TypeElement>> els = info.getClassIndex().getDeclaredTypes(namePrefix, ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX, 
                EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES, ClassIndex.SearchScope.SOURCE));
        return els;
    }
    
    private CompletionItem createItem(ElementHandle<TypeElement> handle, int priority) {
        TypeElement el = handle.resolve(ctx.getCompilationInfo());
        if (el == null) {
            // element does not exist etc
            return null;
        }
        if (el.getKind() != ElementKind.CLASS) {
            // do not honour interfaces
            return null;
        }
        if (!el.getModifiers().contains(Modifier.PUBLIC)) {
            return null;
        }
        CompletionItem item = null;
        
        Collection<? extends ClassItemFactory> converters = MimeLookup.getLookup(JavaFXEditorUtils.FXML_MIME_TYPE).lookupAll(ClassItemFactory.class);
        for (ClassItemFactory converter : converters) {
            item = converter.convert(el, ctx, priority);
            if (item != null) {
                break;
            }
        }
        return item;
    }
    
    private List<CompletionItem> createItems(Collection<? extends ElementHandle<TypeElement>> elems, int priority) {
        List<ElementHandle<TypeElement>> sorted = new ArrayList<ElementHandle<TypeElement>>(elems);
        Collections.sort(sorted, CLASS_SORTER);
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        for (ElementHandle<TypeElement> tel : sorted) {
            CompletionItem item = createItem(tel, priority);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }
    
    @Override
    public List<CompletionItem> complete() {
        Set<ElementHandle<TypeElement>> nodeCandidates = loadDescenantsOfNode();
        Set<ElementHandle<TypeElement>> allCandidates = new HashSet<ElementHandle<TypeElement>>(loadFromAllTypes());
        
        allCandidates.removeAll(nodeCandidates);
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        items.addAll(createItems(nodeCandidates, NODE_PRIORITY));
        if (!ctx.getPrefix().equals("<")) {
            items.addAll(createItems(allCandidates, OTHER_PRIORITY));
        }
        
        return items;
    }
    
    private static final Comparator<ElementHandle<TypeElement>> CLASS_SORTER = 
            new Comparator<ElementHandle<TypeElement>>() {
        @Override
        public int compare(ElementHandle<TypeElement> o1, ElementHandle<TypeElement> o2) {
            String fn1 = o1.getQualifiedName();
            String fn2 = o2.getQualifiedName();
            
            int dot1 = fn1.lastIndexOf('.');
            int dot2 = fn2.lastIndexOf('.');
            
            String sn1 = dot1 == -1 ? fn1 : fn1.substring(dot1 + 1);
            String sn2 = dot2 == -1 ? fn2 : fn2.substring(dot2 + 1);
            
            int diff = sn1.compareToIgnoreCase(sn2);
            if (diff != 0) {
                return diff;
            }
            return fn1.compareToIgnoreCase(fn2);
        }
    };

    private static final Comparator<ElementHandle<TypeElement>> FQN_SORTER = 
            new Comparator<ElementHandle<TypeElement>>() {
        @Override
        public int compare(ElementHandle<TypeElement> o1, ElementHandle<TypeElement> o2) {
            String fn1 = o1.getQualifiedName();
            String fn2 = o2.getQualifiedName();
            
            return fn1.compareToIgnoreCase(fn2);
        }
    };
}
