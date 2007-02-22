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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.axi.visitor;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Element;

/**
 *
 * @author Ayub Khan
 */
public class FindUsageVisitor extends AXINonCyclicVisitor {
    
    Preview p = null;
    
    Element usedBy = null;
    
    /** Creates a new instance of FindUsageVisitor */
    public FindUsageVisitor(AXIModel am) {
        super(am);
        p = new Preview();
    }
    
    public Preview findUsages(AXIDocument root) {
        if(root == null) return null;
        java.util.List<Element> axiges = root.getElements();
        for(Element e : axiges) {
            findUsages(e);
        }
        return p;
    }
    
    public Preview findUsages(Element e) {
        usedBy = e;
        p.addToUsage(e, usedBy);
        for(AXIComponent child: e.getChildren()) {
            child.accept(this);
        }
        return p;
    }
    
    public void visit(Element e) {
        if(!canVisit(e)) //skip recursion
            return;
        p.addToUsage(e, usedBy);
        visitChildren(e);
    }
}
