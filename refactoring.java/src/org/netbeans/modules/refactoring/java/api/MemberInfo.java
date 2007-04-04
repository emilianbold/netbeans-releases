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
package org.netbeans.modules.refactoring.java.api;

import java.util.Set;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.java.source.UiUtils;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
public final class MemberInfo {
    private ElementHandle member;
    private String htmlText;
    private Icon icon;
    private int type = 0;
    private Set<Modifier> modifiers;
    private Lookup lookup;
    /**
     * 0..member
     * 1..implements interface
     */
    
    /** Creates a new instance of MemberInfo describing a field
     * to be pulled up.
     * @param field Field to be pulled up.
     */
    private MemberInfo(ElementHandle member, String htmlText, Icon icon) {
        this.member = member;
        this.htmlText = htmlText;
        this.icon = icon;
    }
    
    public ElementHandle getElementHandle() {
        return member;
    }
    
    public String getHtmlText() {
        return htmlText;
    }

    public MemberInfo(Element el, CompilationInfo c) {
        this(ElementHandle.create(el), UiUtils.getHeader(el, c, UiUtils.PrintPart.NAME), UiUtils.getDeclarationIcon(el));
        modifiers = el.getModifiers();
    }

    public MemberInfo(Element el, CompilationInfo c, int type) {
        this(ElementHandle.create(el), UiUtils.getHeader(el, c, UiUtils.PrintPart.NAME), UiUtils.getDeclarationIcon(el));
        this.type = type;
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    public ElementKind getKind() {
        return member.getKind();
    }
    
    public int getType() {
        return type;
    }
    
    public boolean equals(Object o) {
        if (o instanceof MemberInfo) {
            return ((MemberInfo) o).member.signatureEquals(this.member);
        }
        return false;
    }
    
    public int hashCode() {
        return member.hashCode();
    }
    
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    
    public Lookup getUserData() {
        if (lookup == null) {
            lookup = Lookup.EMPTY;
        }
        return lookup;
    }
    
    public void setUserData(Lookup userData) {
        this.lookup = userData;
    }
    
    public String toString() {
        return htmlText;
    }
    
}