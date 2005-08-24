/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
    
    public String getIconUrl(MethodType mt) {
        mt.accept(this);
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
