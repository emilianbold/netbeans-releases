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
package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;


/**
 *
 * @author Chris Webster
 */
class IconVisitor extends org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.IconVisitor {
    
    public void visit(HomeMethodType hmt) {
        assert false: "session beans do not have home methods";
    }
    
    public void visit(FinderMethodType fmt) {
        assert false: "session beans do not have finder methods";
    }
}
