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

package org.netbeans.modules.xslt.mapper.model;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xslt.mapper.view.PredicateManager;

/**
 * This class is a container of AXIOM component combined with 
 * an array of predicates. It is intended to be used as a paramether 
 * for construction of a PredicatedSchemaNode with the help of NodeFactory.
 * 
 * @author nk160297
 */
public class PredicatedAxiComponent {
    
    private AXIComponent myAxiComponent;
    private XPathPredicateExpression[] myPredicateArr;
    
    public PredicatedAxiComponent(AXIComponent axiComponent,
            XPathPredicateExpression[] predicateArr) {
        myAxiComponent = axiComponent;
        myPredicateArr = predicateArr;
    }
    
    public AXIComponent getType() {
        return myAxiComponent;
    }
    
    public XPathPredicateExpression[] getPredicates() {
        return myPredicateArr;
    }
    
    public void setPredicates(XPathPredicateExpression[] newPArr) {
        myPredicateArr = newPArr;
    }
    
    public String getPredicatesText() {
        return PredicateManager.toString(myPredicateArr);
    }
    
    public boolean hasSamePredicates(XPathPredicateExpression[] predArr) {
        // Compare predicates count
        int myCount = myPredicateArr == null ? 0 : myPredicateArr.length;
        int otherCount = predArr == null ? 0 : predArr.length;
        if (myCount != otherCount) {
            return false;
        }
        // Compare predicates one by one
        for (int index = 0; index < myCount; index++) {
            XPathPredicateExpression myPredicate = myPredicateArr[index];
            XPathPredicateExpression otherPredicate = predArr[index];
            String myPredText = myPredicate.getExpressionString();
            String otherPredText = otherPredicate.getExpressionString();
            if (!(myPredText.equals(otherPredText))) {
                return false;
            }
        }
        return true;
    }
    
    public boolean equals(Object obj) {
        // Compare class
        if (!(obj instanceof PredicatedAxiComponent)) {
            return false;
        }
        PredicatedAxiComponent comp2 = (PredicatedAxiComponent)obj;
        // Compare type
        if (!(comp2.getType().equals(myAxiComponent))) {
            return false;
        }
        // Compare predicates 
        XPathPredicateExpression[] otherPredicateArr = comp2.getPredicates();
        return hasSamePredicates(otherPredicateArr);        
    }

    public String toString() {
        String predicatesText = PredicateManager.toString(myPredicateArr);
        if (predicatesText.length() == 0) {
            return myAxiComponent.toString();
        } else {
            return myAxiComponent.toString() + " " + predicatesText;
        }
    }
}
