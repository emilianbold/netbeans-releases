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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.java;

import com.sun.source.tree.Scope;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.EnumSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.LazyCompletionItem;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 *
 * @author Dusan Balek
 */
public class LazyTypeCompletionItem extends JavaCompletionItem implements LazyCompletionItem {
    
    public static final LazyTypeCompletionItem create(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, Source source, boolean insideNew, boolean afterExtends) {
        return new LazyTypeCompletionItem(handle, kinds, substitutionOffset, source, insideNew, afterExtends);
    }
    
    private ElementHandle<TypeElement> handle;
    private EnumSet<ElementKind> kinds;
    private Source source;
    private boolean insideNew;
    private boolean afterExtends;
    private String name;
    private String simpleName;
    private String pkgName;
    private JavaCompletionItem delegate = null;
    private CharSequence sortText;
    private int prefWidth = -1;
    
    private LazyTypeCompletionItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, Source source, boolean insideNew, boolean afterExtends) {
        super(substitutionOffset);
        this.handle = handle;
        this.kinds = kinds;
        this.source = source;
        this.insideNew = insideNew;
        this.afterExtends = afterExtends;
        this.name = handle.getQualifiedName();
        int idx = name.lastIndexOf('.'); //NOI18N
        this.simpleName = idx > -1 ? name.substring(idx + 1) : name;
        this.pkgName = idx > -1 ? name.substring(0, idx) : ""; //NOI18N
        this.sortText = this.simpleName + Utilities.getImportanceLevel(this.pkgName) + "#" + this.pkgName; //NOI18N
    }
    
    public boolean accept() {
        if (handle != null) {
            if (isAnnonInner()) {
                handle = null;
                return false;
            }
            try {
                ParserManager.parse(Collections.singletonList(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController controller = CompilationController.get(resultIterator.getParserResult(substitutionOffset));
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Scope scope = controller.getTrees().getScope(controller.getTreeUtilities().pathFor(substitutionOffset));
                        if (!isAnnonInner()) {
                            TypeElement e = handle.resolve(controller);
                            Elements elements = controller.getElements();
                            if (e != null && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && controller.getTrees().isAccessible(scope, e)) {
                                if (isOfKind(e, kinds) && (!afterExtends || !e.getModifiers().contains(Modifier.FINAL)) && (!isInDefaultPackage(e) || isInDefaultPackage(scope.getEnclosingClass())) && !Utilities.isExcluded(e.getQualifiedName()))
                                    delegate = JavaCompletionItem.createTypeItem(e, (DeclaredType)e.asType(), substitutionOffset, true, controller.getElements().isDeprecated(e), insideNew, false);
                            }
                        }
                        handle = null;
                    }
                });
            } catch(Throwable t) {
            }
        }
        return delegate != null;
    }

    public void defaultAction(JTextComponent component) {
        if (delegate != null)
            delegate.defaultAction(component);
    }

    public void processKeyEvent(KeyEvent evt) {
        if (delegate != null)
            delegate.processKeyEvent(evt);
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        if (prefWidth < 0)
            prefWidth = CompletionUtilities.getPreferredWidth(simpleName + " (" + pkgName + ")", null, g, defaultFont); //NOI18N
        return prefWidth;
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        if (delegate != null)
            delegate.render(g, defaultFont, defaultColor, backgroundColor, width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        if (delegate != null)
            return delegate.createDocumentationTask();
        return null;
    }

    public CompletionTask createToolTipTask() {
        if (delegate != null)
            return delegate.createToolTipTask();
        return null;
    }

    public int getSortPriority() {
        return 700;
    }

    public CharSequence getSortText() {
        return sortText;
    }

    public CharSequence getInsertPrefix() {
        return simpleName;
    }
    
    boolean isAnnonInner() {
        return simpleName.length() == 0 || Character.isDigit(simpleName.charAt(0));
    }
    
    private boolean isOfKind(Element e, EnumSet<ElementKind> kinds) {
        if (kinds.contains(e.getKind()))
            return true;
        for (Element ee : e.getEnclosedElements())
            if (isOfKind(ee, kinds))
                return true;
        return false;
    }

    private boolean isInDefaultPackage(Element e) {
        while (e != null && e.getKind() != ElementKind.PACKAGE)
            e = e.getEnclosingElement();
        return e != null && e.getSimpleName().length() == 0;
    }
}
