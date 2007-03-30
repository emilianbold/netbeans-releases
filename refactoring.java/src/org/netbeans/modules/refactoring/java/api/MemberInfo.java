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

import javax.swing.Icon;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;

/**
 *
 * @author Jan Becicka
 */
public final class MemberInfo {
    public final ElementHandle member;
    public final boolean makeAbstract;
    private String htmlText;
    private Icon icon;
    
    /** Creates a new instance of MemberInfo describing a method.
     * @param method Method to be pulled up.
     * @param makeAbstract Indicates whether the method should be made abstract
     *              in the supertype.
     */
    public MemberInfo(ElementHandle method, boolean makeAbstract, String htmlText, Icon icon) {
        this.member = method;
        this.makeAbstract = makeAbstract;
        this.htmlText = htmlText;
        this.icon = icon;
    }
    
    /** Creates a new instance of MemberInfo describing a field
     * to be pulled up.
     * @param field Field to be pulled up.
     */
    public MemberInfo(ElementHandle innerClass, String htmlText, Icon icon) {
        this(innerClass, false, htmlText, icon);
    }
    
    public String getHtmlText() {
        return htmlText;
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    //        /** Creates a new instance of MemberInfo describing a field
    //         * to be pulled up.
    //         * @param field Field to be pulled up.
    //         */
    //        public MemberInfo(Field field) {
    //            this(field, false);
    //        }
    //
    //        /** Creates a new instance of MemberInfo describing an interface name
    //         * from the implements clause that should be pulled up.
    //         * @param interfaceName Interface name to be pulled up.
    //         */
    //        public MemberInfo(MultipartId interfaceName) {
    //            this(interfaceName, false);
    //        }
}