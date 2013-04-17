/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.java;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;

import com.sun.source.tree.Scope;
import com.sun.source.util.Trees;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.spi.editor.completion.LazyCompletionItem;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class LazyStaticMemberCompletionItem extends JavaCompletionItem.WhiteListJavaCompletionItem<TypeElement> implements LazyCompletionItem {

    private static final String LOCAL_VARIABLE = "org/netbeans/modules/editor/resources/completion/localVariable.gif"; //NOI18N
    private static final String PARAMETER_COLOR = "<font color=#00007c>"; //NOI18N
    private static final String PKG_COLOR = "<font color=#808080>"; //NOI18N
    private static ImageIcon icon;

    public static final LazyStaticMemberCompletionItem create(ElementHandle<TypeElement> handle, String name, int substitutionOffset, ReferencesCount referencesCount, Source source, WhiteListQuery.WhiteList whiteList) {
        return new LazyStaticMemberCompletionItem(handle, name, substitutionOffset, referencesCount, source, whiteList);
    }

    private Source source;
    private String name;
    private CharSequence sortText;
    private String leftText;
    private ElementHandle<Element> memberHandle;

    private LazyStaticMemberCompletionItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, ReferencesCount referencesCount, Source source, WhiteListQuery.WhiteList whiteList) {
        super(substitutionOffset, handle, whiteList);
        this.name = name;
        this.sortText = new LazySortText(this.name, handle.getQualifiedName(), handle, referencesCount);
        this.source = source;
    }

    @Override
    public boolean accept() {
        if (getElementHandle() != null) {
            try {
                JavaCompletionProvider.JavaCompletionQuery.javadocBreak.set(true);
                ParserManager.parse(Collections.singletonList(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController controller = CompilationController.get(resultIterator.getParserResult(substitutionOffset));
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Elements elements = controller.getElements();
                        Trees trees = controller.getTrees();
                        Scope scope = controller.getTrees().getScope(controller.getTreeUtilities().pathFor(substitutionOffset));
                        TypeElement te = getElementHandle().resolve(controller);
                        if (te != null) {
                            for (Element e : te.getEnclosedElements()) {
                                if ((e.getKind().isField() || e.getKind() == ElementKind.METHOD)
                                        && name.contentEquals(Utilities.isCaseSensitive() ? e.getSimpleName() : e.getSimpleName().toString().toLowerCase())
                                        && e.getModifiers().contains(Modifier.STATIC)
                                        && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                        && trees.isAccessible(scope, e, (DeclaredType) te.asType())) {
                                    memberHandle = ElementHandle.create(e);
                                    name = e.getSimpleName().toString();
                                    break;
                                }
                            }
                        }
                    }
                });
            } catch (Throwable t) {
            }
        }
        return memberHandle != null;
    }

    @Override
    public int getSortPriority() {
        return 690;
    }

    @Override
    public CharSequence getSortText() {
        return sortText;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return name;
    }

    @Override
    protected String getLeftHtmlText() {
        if (leftText == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(PARAMETER_COLOR).append(BOLD);
            sb.append(name);
            sb.append(BOLD_END).append(COLOR_END).append(PKG_COLOR);
            sb.append(" ("); //NOI18N
            sb.append(getElementHandle().getQualifiedName());
            sb.append(")"); //NOI18N
            sb.append(COLOR_END);
            leftText = sb.toString();
        }
        return leftText;
    }

    @Override
    protected ImageIcon getBaseIcon() {
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon(LOCAL_VARIABLE, false);
        }
        return icon;
    }

    @Override
    protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
        CharSequence cs = super.substituteText(c, offset, length, text, toAdd);
        final AtomicBoolean cancel = new AtomicBoolean();
        ProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ModificationResult.runModificationTask(Collections.singletonList(Source.create(c.getDocument())), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            if (cancel.get()) {
                                return;
                            }
                            final WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult(offset));
                            copy.toPhase(JavaSource.Phase.RESOLVED);
                            if (cancel.get()) {
                                return;
                            }
                            Element e = memberHandle.resolve(copy);
                            if (e != null) {
                                copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), Collections.singleton(e)));
                            }
                        }
                    }).commit();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, NbBundle.getMessage(LazyStaticMemberCompletionItem.class, "JCI-import_resolve"), cancel, false); //NOI18N
        if (cs == null) {
            cs = "${cursor completionInvoke}"; //NOI18N
        }
        return cs;
    }
}
