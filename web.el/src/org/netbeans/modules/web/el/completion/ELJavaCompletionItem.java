/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el.completion;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.swing.ImageIcon;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Erno Mononen
 */
final class ELJavaCompletionItem extends DefaultCompletionProposal {

    private static final String ICON_PATH = "org/netbeans/modules/web/el/completion/resources/jsf_bean_16.png";//NOI18N

    private final String elementName;
    private final Element javaElement;
    private final ELElement elElement;
    private final ELTypeUtilities typeUtilities;
    private final ElementHandleAdapter adapter;

    public ELJavaCompletionItem(Element javaElement, ELElement elElement, ELTypeUtilities typeUtilities) {
        this(javaElement, null, elElement, typeUtilities);
    }
    
    public ELJavaCompletionItem(Element javaElement, String elementName, ELElement elElement, ELTypeUtilities typeUtilities) {
        assert javaElement != null;
        this.javaElement = javaElement;
        this.elElement = elElement;
        this.typeUtilities = typeUtilities;
        this.elementKind = ElementKind.METHOD;
        this.elementName = elementName;
        this.adapter = new ElementHandleAdapter();
        setAnchorOffset(elElement.getOriginalOffset().getStart());
    }

    @Override
    public ElementHandle getElement() {
        return adapter;
    }

    @Override
    public String getName() {
        return adapter.getName();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.singleton(Modifier.PUBLIC);
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        ElementKind kind = getKind();
        formatter.name(kind, true);
        formatter.appendText(getName());
        if (javaElement.getKind() == javax.lang.model.element.ElementKind.METHOD) {
            formatter.appendText(typeUtilities.getParametersAsString((ExecutableElement) javaElement));
        }
        formatter.name(kind, false);

        return formatter.getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return typeUtilities.getTypeNameFor(javaElement);
    }

    @Override
    public ImageIcon getIcon() {
        if (adapter.getKind() == ElementKind.CLASS) {
            return ImageUtilities.loadImageIcon(ICON_PATH, false);
        }
        return super.getIcon();
    }

    @Override
    public List<String> getInsertParams() {
        if (!adapter.isMethod()) {
            return null;
        }
        return typeUtilities.getParameterNames((ExecutableElement) javaElement);
    }

    @Override
    public String[] getParamListDelimiters() {
        return new String[]{"(", ")"};
    }

    final class ElementHandleAdapter extends ELElementHandle {

        @Override
        public FileObject getFileObject() {
            return elElement.getSnapshot().getSource().getFileObject();
        }

        @Override
        public String getMimeType() {
            return "text/java";
        }

        @Override
        public String getName() {
            return elementName != null ? elementName : RefactoringUtil.getPropertyName(javaElement.getSimpleName().toString(), true);
        }

        @Override
        public String getIn() {
            if (isMethod()) {
                return javaElement.getEnclosingElement().getSimpleName().toString();
            }
            return javaElement.getSimpleName().toString();
        }

        @Override
        public ElementKind getKind() {
            return isMethod() ? ElementKind.METHOD : ElementKind.CLASS;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.singleton(Modifier.PUBLIC);
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return getName().equals(handle.getName());
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return elElement.getOriginalOffset();
        }

        public Element getOriginalElement() {
            return javaElement;
        }

        private boolean isMethod() {
            return javaElement.getKind() == javax.lang.model.element.ElementKind.METHOD;
        }

        @Override
        String document(ParserResult info) {
            final String[] result = new String[1];
            try {
                ClasspathInfo cp = ClasspathInfo.create(info.getSnapshot().getSource().getFileObject());
                JavaSource source = JavaSource.create(cp);
                if (source == null) {
                    return null;
                }
                source.runUserActionTask(new Task<CompilationController>() {

                    @Override
                    public void run(CompilationController parameter) throws Exception {
                        ElementJavadoc javadoc = ElementJavadoc.create(parameter, getOriginalElement());
                        result[0] = javadoc.getText();
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return result[0];
        }
    }
}
