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

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.LazyCompletionItem;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 *
 * @author Dusan Balek
 */
public class LazyTypeCompletionItem extends JavaCompletionItem implements LazyCompletionItem {
    
    public static final LazyTypeCompletionItem create(String name, EnumSet<ElementKind> kinds, int substitutionOffset, CompilationInfo info) {
        return new LazyTypeCompletionItem(name, kinds, substitutionOffset, info);
    }
    
    private JavaCompletionItem delegate = null;
    private String name;
    private String simpleName;
    private String pkgName;
    private EnumSet<ElementKind> kinds;
    private CompilationInfo info;
    
    private LazyTypeCompletionItem(String name, EnumSet<ElementKind> kinds, int substitutionOffset, CompilationInfo info) {
        super(substitutionOffset);
        this.name = name;
        int idx = name.lastIndexOf('.'); //NOI18N
        this.simpleName = idx > -1 ? name.substring(idx + 1) : name;
        this.pkgName = idx > -1 ? name.substring(0, idx) : ""; //NOI18N
        this.kinds = kinds;
        this.info = info;
    }
    
    public boolean accept() {
        try {
            if (simpleName.length() == 0 || Character.isDigit(simpleName.charAt(0)))
                return false;
            TypeElement e = info.getElements().getTypeElement(name);
            if (e != null && info.getTrees().isAccessible(info.getTrees().getScope(info.getTreeUtilities().pathFor(substitutionOffset)), e)) {
                if (isOfKind(e, kinds)) {
                    delegate = JavaCompletionItem.createTypeItem((TypeElement)e, (DeclaredType)e.asType(), substitutionOffset, true, info.getElements().isDeprecated(e));
                }
            }
        } catch(Throwable t) {}        
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
        return CompletionUtilities.getPreferredWidth(simpleName + " (" + pkgName + ")", null, g, defaultFont); //NOI18N
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

    public boolean instantSubstitution(JTextComponent component) {
        if (delegate != null)
            return delegate.instantSubstitution(component);
        return false;
    }

    public int getSortPriority() {
        return 700;
    }

    public CharSequence getSortText() {
        return simpleName + "#" + name; //NOI18N
    }

    public CharSequence getInsertPrefix() {
        return getItemText();
    }
    
    String getItemText() {
        return simpleName;
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
