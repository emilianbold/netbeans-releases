/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import com.sun.source.tree.Scope;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.LazyCompletionItem;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 *
 * @author Dusan Balek
 */
public class LazyTypeCompletionItem extends JavaCompletionItem implements LazyCompletionItem {
    
    public static final LazyTypeCompletionItem create(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, JavaSource javaSource) {
        return new LazyTypeCompletionItem(handle, kinds, substitutionOffset, javaSource);
    }
    
    private ElementHandle<TypeElement> handle;
    private EnumSet<ElementKind> kinds;
    private JavaSource javaSource;
    private String name;
    private String simpleName;
    private String pkgName;
    private JavaCompletionItem delegate = null;
    private LazyTypeCompletionItem nextItem = null;
    private CharSequence sortText;
    private int prefWidth = -1;
    
    private LazyTypeCompletionItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, JavaSource javaSource) {
        super(substitutionOffset);
        this.handle = handle;
        this.kinds = kinds;
        this.javaSource = javaSource;
        this.name = handle.getQualifiedName();
        int idx = name.lastIndexOf('.'); //NOI18N
        this.simpleName = idx > -1 ? name.substring(idx + 1) : name;
        this.pkgName = idx > -1 ? name.substring(0, idx) : ""; //NOI18N
        this.sortText = new ClassSortText(this.simpleName, this.pkgName);
    }
    
    public boolean accept() {
        if (handle != null) {
//            if (simpleName.length() == 0 || Character.isDigit(simpleName.charAt(0))) {
            if (isAnnonInner()) {
                handle = null;
                return false;
            }
            try {
                long t = System.currentTimeMillis();
                javaSource.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Scope scope = controller.getTrees().getScope(controller.getTreeUtilities().pathFor(substitutionOffset));
                        LazyTypeCompletionItem item = LazyTypeCompletionItem.this;
                        for (int i = 0; i < 1 && item != null;) {
                            if (item.init(controller, scope))
                                i++;
                            item = item.nextItem;
                        }
                    }
                }, true);
                //System.out.println("ACCEPT took: " + (System.currentTimeMillis() - t));
            } catch(Throwable t) {
            }
        }
        //System.out.println("accept name=" + name + " " + ( delegate != null ));
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
    
    void setNextItem(LazyTypeCompletionItem nextItem) {
        this.nextItem = nextItem;
    }
    
    boolean init(CompilationController controller, Scope scope) {
        //if (simpleName.length() >= 0 && !Character.isDigit(simpleName.charAt(0))) {
        if (!isAnnonInner()) {
            TypeElement e = handle.resolve(controller);
            if (e != null && controller.getTrees().isAccessible(scope, e)) {
                if (isOfKind(e, kinds))
                    delegate = JavaCompletionItem.createTypeItem((TypeElement)e, (DeclaredType)e.asType(), substitutionOffset, true, controller.getElements().isDeprecated(e), false);
            }
        }
        handle = null;
        // System.out.println("   init: name=" + name + " " + ( delegate != null ) );
        return delegate != null;
    }

    private boolean isOfKind(Element e, EnumSet<ElementKind> kinds) {
        if (kinds.contains(e.getKind()))
            return true;
        for (Element ee : e.getEnclosedElements())
            if (isOfKind(ee, kinds))
                return true;
        return false;
    }
}
