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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.Icon;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.java.source.UiUtils;

/**
 *
 * @author Jan Becicka
 */
public final class MemberInfo {
    private ElementHandle member;
    private String htmlText;
    private Icon icon;
    
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

    public MemberInfo(Element el, CompilationController c) {
        this(ElementHandle.create(el), UiUtils.getHeader(el, c, UiUtils.PrintPart.NAME), UiUtils.getDeclarationIcon(el));
    }

    public Icon getIcon() {
        return icon;
    }
    
    public ElementKind getKind() {
        return member.getKind();
    }
}