/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
