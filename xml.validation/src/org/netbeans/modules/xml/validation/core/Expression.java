/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.validation.core;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathModelTracerVisitor;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.12.10
 */
public final class Expression {

    private Expression() {}

    public static List<String> getUsedVariables(String expression) {
        if (expression == null || expression.length() == 0) {
            return null;
        }
        try {
            XPathModel model = XPathModelHelper.getInstance().newXPathModel();
            XPathExpression xpath = model.parseExpression(expression);

            FindVaribleVisitor visitor = new FindVaribleVisitor();
            xpath.accept(visitor);
            return visitor.getFoundVariables();
        }
        catch (XPathException e) {
            return null;
        }
    }

    public static boolean contains(String expression, String variable, String part) {
        if (expression == null || expression.length() == 0) {
            return false;
        }
        try {
            XPathModel model = XPathModelHelper.getInstance().newXPathModel();
            XPathExpression xpath = model.parseExpression(expression);

            FindVaribleVisitor visitor = new FindVaribleVisitor(variable, part);
            xpath.accept(visitor);
            return visitor.isFound();
        }
        catch (XPathException e) {
            return false;
        }
    }

    // ----------------------------------------------------------------------
    public static class FindVaribleVisitor extends XPathModelTracerVisitor {
        
        public FindVaribleVisitor() {
            myFoundVariables = new ArrayList<String>();
        }
        
        public FindVaribleVisitor(String name) {
            this();
            myName = name;
        }

        public FindVaribleVisitor(String variableName, String partName) {
            this(variableName);
            myPartName = partName;
        }
        
        @Override
        public void visit(XPathExtensionFunction function) {
            if (myName == null && "getVariableProperty".equals(function.getName().getLocalPart())) { // NOI18N
                myFirstArgumentOfGetVariableProperty = true;
            }
//out("VISIT ext func: " + function);
            super.visit(function);
        }

        @Override
        public void visit(XPathStringLiteral stringLiteral) {
//out("VISIT str literal: " + stringLiteral);
            if (myFirstArgumentOfGetVariableProperty) {
                myFirstArgumentOfGetVariableProperty = false;
                String variableName = stringLiteral.getValue();

                if (variableName != null) {
                    variableName = variableName.trim();
                    int index = variableName.indexOf(":");

                    if (index >= 0) {
                        variableName = variableName.substring(index + 1);
                    }
                    if (variableName.length() > 0) {
                        myFoundVariables.add(variableName);
                    }
                }
            }
            super.visit(stringLiteral);
        }
            
        @Override
        public void visit(XPathVariableReference variable) {
//out();
//out("VISIT var reference: " + variable);
            QName qName = variable.getVariableName();

            if (qName == null) {
                return;
            }
            String local = qName.getLocalPart();

            if (local == null) {
                return;
            }
//out("              local: " + local);
            int index = local.indexOf("."); 
//out("        index: " + index);

            if (index < 0) {
                myFoundVariables.add(local);

                if (myIsFound) {
                    return;
                }
                myIsFound = local.equals(myName);

                if (myIsFound && myPartName != null) {
                    myIsFound = false;
                }
            }
            else {
                String variableName = local.substring(0, index);
//out("        varName: " + varName);
                myFoundVariables.add(variableName);

                if (myIsFound) {
                    return;
                }
//out("         myName: " + myName);
//out("     myPartName: " + myPartName);
                myIsFound = variableName.equals(myName);
                
                if (myIsFound && myPartName != null) {
                    String part = (index < (local.length() - 1)) ? local.substring(index + 1) : null;
//out("           part: " + part);
                    myIsFound = myPartName.equals(part); 
                }
//out("isFound: " + myIsFound);
            }
        }

        public List<String> getFoundVariables() {
            return myFoundVariables;
        }
        
        public boolean isFound() {
            return myIsFound;
        }
        
        private String myName;
        private String myPartName;
        private boolean myIsFound; 
        private List<String> myFoundVariables;
        private boolean myFirstArgumentOfGetVariableProperty;
    }
}
