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
package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.BusinessMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.CreateMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;


/**
 *
 * @author Chris Webster
 */
public class IconVisitor implements MethodType.MethodTypeVisitor {
    private String iconURL;
    private static final String BASE = "org/netbeans/modules/j2ee/ejbcore/resources/"; //NOI18N
    private static final String CREATE = BASE+"CreateMethodIcon.gif"; //NOI18N
    private static final String BUSINESS = BASE+"BusinessMethodIcon.gif"; //NOI18N
    private static final String HOME = BASE+"HomeMethodIcon.gif"; //NOI18N
    private static final String FINDER = BASE+"FinderMethodIcon.gif"; //NOI18N
    
    public String getIconUrl(MethodType methodType) {
        methodType.accept(this);
        return iconURL;
    }
    
    public void visit(BusinessMethodType bmt) {
        iconURL = BUSINESS;
    }
       
    public void visit(CreateMethodType cmt) {
        iconURL = CREATE;
    }
    
    public void visit(HomeMethodType hmt) {
        iconURL = HOME;
    }
    
    public void visit(FinderMethodType fmt) {
        iconURL = FINDER;
    }
}
