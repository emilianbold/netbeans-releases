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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.SimpleElementVisitor6;
import org.netbeans.api.editor.completion.Completion;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Dongmei Cao
 */
public abstract class HibernateMappingCompletionItem implements CompletionItem {

    public static HibernateMappingCompletionItem createAttribValueItem(int substitutionOffset, String displayText, String docText) {
        return new AttribValueItem(substitutionOffset, displayText, docText);
    }

    public static HibernateMappingCompletionItem createPackageItem(int substitutionOffset, String packageName,
            boolean deprecated) {
        return new PackageItem(substitutionOffset, packageName, deprecated);
    }

    public static HibernateMappingCompletionItem createTypeItem(int substitutionOffset, TypeElement elem, ElementHandle<TypeElement> elemHandle,
            boolean deprecated, boolean smartItem) {
        return new ClassItem(substitutionOffset, elem, elemHandle, deprecated, smartItem);
    }
    
    static HibernateMappingCompletionItem createClassPropertyItem(int substitutionOffset, VariableElement variableElem, ElementHandle<VariableElement> elemHandle, boolean deprecated) {
        return new ClassPropertyItem(substitutionOffset, variableElem, elemHandle, deprecated);
    }
    
    protected int substitutionOffset;

    protected HibernateMappingCompletionItem(int substitutionOffset) {
        this.substitutionOffset = substitutionOffset;
    }

    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
        }
    }

    protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
        BaseDocument doc = (BaseDocument) c.getDocument();
        CharSequence prefix = getInsertPrefix();
        String text = prefix.toString();
        if (toAdd != null) {
            text += toAdd;
        }

        doc.atomicLock();
        try {
            Position position = doc.createPosition(offset);
            doc.remove(offset, len);
            doc.insertString(position.getOffset(), text.toString(), null);
        } catch (BadLocationException ble) {
        // nothing can be done to update
        } finally {
            doc.atomicUnlock();
        }
    }

    protected CharSequence getSubstitutionText() {
        return getInsertPrefix();
    }

    public void processKeyEvent(KeyEvent evt) {

    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(),
                getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(),
                getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }

    protected String getLeftHtmlText() {
        return null;
    }

    protected String getRightHtmlText() {
        return null;
    }

    protected ImageIcon getIcon() {
        return null;
    }
    
    
    /**
     * Represents a class in the completion popup. 
     * 
     * Heavily derived from Java Editor module's JavaCompletionItem class
     * 
     */
    private static class ClassPropertyItem extends HibernateMappingCompletionItem {

        private static final String FIELD_ICON = "org/netbeans/modules/editor/resources/completion/field_16.png"; //NOI18N
        private ElementHandle<VariableElement> elemHandle;
        private boolean deprecated;
        private String displayName;

        public ClassPropertyItem(int substitutionOffset, VariableElement elem, ElementHandle<VariableElement> elemHandle,
                boolean deprecated) {
            super(substitutionOffset);
            this.elemHandle = elemHandle;
            this.deprecated = deprecated;
            this.displayName = elem.getSimpleName().toString();
        }

       public int getSortPriority() {
            return 50;
        }

        public CharSequence getSortText() {
            return displayName;
        }

        public CharSequence getInsertPrefix() {
            return displayName;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayName;
        }
        
        @Override
        protected ImageIcon getIcon() {
            
            return new ImageIcon(Utilities.loadImage(FIELD_ICON));
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {

                @Override
                protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
                    try {
                        JavaSource js = getJavaSource(doc);
                        if (js == null) {
                            return;
                        }

                        js.runUserActionTask(new Task<CompilationController>() {

                            public void run(CompilationController cc) throws Exception {
                                cc.toPhase(Phase.RESOLVED);
                                Element element = elemHandle.resolve(cc);
                                if (element == null) {
                                    return;
                                }
                                HibernateMappingCompletionDocumentation doc = HibernateMappingCompletionDocumentation.createJavaDoc(cc, element);
                                resultSet.setDocumentation(doc);
                            }
                        }, false);
                        resultSet.finish();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, EditorRegistry.lastFocusedComponent());
        }
    }
    
    
    public static final String COLOR_END = "</font>"; //NOI18N
    public static final String STRIKE = "<s>"; //NOI18N
    public static final String STRIKE_END = "</s>"; //NOI18N
    public static final String BOLD = "<b>"; //NOI18N
    public static final String BOLD_END = "</b>"; //NOI18N

    /**
     * Represents a class in the completion popup. 
     * 
     * Heavily derived from Java Editor module's JavaCompletionItem class
     * 
     */
    private static class ClassItem extends HibernateMappingCompletionItem {

        private static final String CLASS = "org/netbeans/modules/editor/resources/completion/class_16.png"; //NOI18N
        private static final String CLASS_COLOR = "<font color=#560000>"; //NOI18N
        private static final String PKG_COLOR = "<font color=#808080>"; //NOI18N
        private ElementHandle<TypeElement> elemHandle;
        private boolean deprecated;
        private String displayName;
        private String enclName;
        private String sortText;
        private String leftText;
        private boolean smartItem;

        public ClassItem(int substitutionOffset, TypeElement elem, ElementHandle<TypeElement> elemHandle,
                boolean deprecated, boolean smartItem) {
            super(substitutionOffset);
            this.elemHandle = elemHandle;
            this.deprecated = deprecated;
            this.displayName = smartItem ? elem.getSimpleName().toString() : getRelativeName(elem);
            this.enclName = getElementName(elem.getEnclosingElement(), true).toString();
            this.sortText = this.displayName + getImportanceLevel(this.enclName) + "#" + this.enclName; //NOI18N
            this.smartItem = smartItem;
        }

        private String getRelativeName(TypeElement elem) {
            StringBuilder sb = new StringBuilder();
            sb.append(elem.getSimpleName().toString());
            Element parent = elem.getEnclosingElement();
            while (parent.getKind() != ElementKind.PACKAGE) {
                sb.insert(0, parent.getSimpleName().toString() + "$"); // NOI18N
                parent = parent.getEnclosingElement();
            }

            return sb.toString();
        }

        public int getSortPriority() {
            return 200;
        }

        public CharSequence getSortText() {
            return sortText;
        }

        public CharSequence getInsertPrefix() {
            return smartItem ? "" : elemHandle.getBinaryName(); // NOI18N
        }

        @Override
        protected CharSequence getSubstitutionText() {
            return elemHandle.getBinaryName();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(getColor());
                if (deprecated) {
                    sb.append(STRIKE);
                }
                sb.append(displayName);
                if (deprecated) {
                    sb.append(STRIKE_END);
                }
                if (smartItem && enclName != null && enclName.length() > 0) {
                    sb.append(COLOR_END);
                    sb.append(PKG_COLOR);
                    sb.append(" ("); //NOI18N
                    sb.append(enclName);
                    sb.append(")"); //NOI18N
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        protected String getColor() {
            return CLASS_COLOR;
        }

        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(Utilities.loadImage(CLASS));
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {

                @Override
                protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
                    try {
                        JavaSource js = getJavaSource(doc);
                        if (js == null) {
                            return;
                        }

                        js.runUserActionTask(new Task<CompilationController>() {

                            public void run(CompilationController cc) throws Exception {
                                cc.toPhase(Phase.RESOLVED);
                                Element element = elemHandle.resolve(cc);
                                if (element == null) {
                                    return;
                                }
                                HibernateMappingCompletionDocumentation doc = HibernateMappingCompletionDocumentation.createJavaDoc(cc, element);
                                resultSet.setDocumentation(doc);
                            }
                        }, false);
                        resultSet.finish();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, EditorRegistry.lastFocusedComponent());
        }
    }

    private static class PackageItem extends HibernateMappingCompletionItem {

        private static final String PACKAGE = "org/netbeans/modules/java/editor/resources/package.gif"; // NOI18N
        private static final String PACKAGE_COLOR = "<font color=#005600>"; //NOI18N
        private static ImageIcon icon;
        private boolean deprecated;
        private String simpleName;
        private String sortText;
        private String leftText;

        public PackageItem(int substitutionOffset, String packageFQN, boolean deprecated) {
            super(substitutionOffset);
            int idx = packageFQN.lastIndexOf('.'); // NOI18N
            this.simpleName = idx < 0 ? packageFQN : packageFQN.substring(idx + 1);
            this.deprecated = deprecated;
            this.sortText = this.simpleName + "#" + packageFQN; //NOI18N
        }

        public int getSortPriority() {
            return 50;
        }

        public CharSequence getSortText() {
            return sortText;
        }

        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                if (evt.getKeyChar() == '.') { // NOI18N
                    Completion.get().hideDocumentation();
                    JTextComponent component = (JTextComponent) evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    Completion.get().showCompletion();
                    evt.consume();
                }
            }
        }

        @Override
        protected ImageIcon getIcon() {
            if (icon == null) {
                icon = new ImageIcon(org.openide.util.Utilities.loadImage(PACKAGE));
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(PACKAGE_COLOR);
                if (deprecated) {
                    sb.append(STRIKE);
                }
                sb.append(simpleName);
                if (deprecated) {
                    sb.append(STRIKE_END);
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
    }

    private static class AttribValueItem extends HibernateMappingCompletionItem {

        private String displayText;
        private String docText;

        public AttribValueItem(int substitutionOffset, String displayText, String docText) {
            super(substitutionOffset);
            this.displayText = displayText;
            this.docText = docText;
        }

        public int getSortPriority() {
            return 50;
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {

                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    if (docText != null) {
                        CompletionDocumentation documentation = HibernateMappingCompletionDocumentation.getAttribValueDoc(docText);
                        resultSet.setDocumentation(documentation);
                    }
                    resultSet.finish();
                }
            });
        }
    }

    public static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) // NOI18N
        {
            weight -= 10;
        } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) // NOI18N
        {
            weight += 10;
        } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) // NOI18N
        {
            weight += 20;
        } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) // NOI18N
        {
            weight += 30;
        }
        return weight;
    }

    public static CharSequence getElementName(Element el, boolean fqn) {
        if (el == null || el.asType().getKind() == TypeKind.NONE) {
            return "";
        } //NOI18N
        return new ElementNameVisitor().visit(el, fqn);
    }

    private static class ElementNameVisitor extends SimpleElementVisitor6<StringBuilder, Boolean> {

        private ElementNameVisitor() {
            super(new StringBuilder());
        }

        @Override
        public StringBuilder visitPackage(PackageElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }

        @Override
        public StringBuilder visitType(TypeElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }
    }

    public static JavaSource getJavaSource(Document doc) {
        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
        if (fileObject == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return null;
        }
        // XXX this only works correctly with projects with a single sourcepath,
        // but we don't plan to support another kind of projects anyway (what about Maven?).
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            return JavaSource.create(ClasspathInfo.create(sourceGroup.getRootFolder()));
        }
        return null;
    }
}
