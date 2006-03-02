/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Component;

/**
 * Recursive visitor.
 *
 * @author nn136682
 */
public class ChildVisitor extends DefaultVisitor {
    
    protected void visitComponent(WSDLComponent container) {
        List<WSDLComponent> children = container.getChildren();
        for (WSDLComponent ch : children) {
            ch.accept(this);
        }
    }
}
