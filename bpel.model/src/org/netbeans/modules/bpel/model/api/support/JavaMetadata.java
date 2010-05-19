/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.model.api.support;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import javax.swing.Icon;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.metadata.AbstractArgument;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.ResultTypeCalculator;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.images.IconLoader;
import org.netbeans.modules.xml.xpath.ext.spi.ExtensionFunctionResolver;
import org.openide.util.NbBundle;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.11.27
 */
public class JavaMetadata implements ExtFunctionMetadata {

    public JavaMetadata(Method method) {
        this(method, null);
    }

    public JavaMetadata(QName qname) {
        this(null, qname);
    }

    private JavaMetadata(Method method, QName qname) {
        myArguments = new ArrayList<AbstractArgument>();
        String name;

        if (qname == null) {
            myDisplayName = method.getName();
            myResultType = getType(method.getReturnType());
            myQName = new QName(ExtensionFunctionResolver.JAVA_PROTOCOL + method.getDeclaringClass().getName(), method.getName());

            for (Class param : method.getParameterTypes()) {
                myArguments.add(new ArgumentDescriptor(getType(param), true, false, null));
            }
        }
        else {
//System.out.println();
//System.out.println("   local part: " + qname.getLocalPart());
//System.out.println("namespace URI: " + qname.getNamespaceURI());
//System.out.println("       prefix: " + qname.getPrefix());
//System.out.println();
            myQName = qname;
            myResultType = XPathType.ANY_TYPE;
            myDisplayName = qname.getLocalPart();
            myArguments.add(ArgumentDescriptor.Predefined.REPEATED_ANY_TYPE_0MIN);
        }
    }

    private XPathType getType(Class<?> clazz) {
        if (clazz.isAssignableFrom(String.class)) {
            return XPathType.STRING_TYPE;
        }
        if (clazz.isAssignableFrom(Boolean.class)) {
            return XPathType.BOOLEAN_TYPE;
        }
        if (clazz.isAssignableFrom(Number.class)) {
            return XPathType.NUMBER_TYPE;
        }
        if (clazz.isAssignableFrom(org.w3c.dom.Node.class)) {
            return XPathType.NODE_TYPE;
        }
        if (clazz.isAssignableFrom(javax.security.auth.Subject.class)) {
            return XPathType.SUBJECT_TYPE;
        }
        return XPathType.ANY_TYPE;
    }

    public QName getName() {
        return myQName;
    }

    public XPathType getResultType() {
        return myResultType;
    }

    public List<AbstractArgument> getArguments() {
        return myArguments;
    }

    public Icon getIcon() {
        return IconLoader.getIcon("java", BpelXPathExtFunctionMetadata.IMAGE_FOLDER_NAME); // NOI18N
    }
    
    public String getDisplayName() {
        return myDisplayName;
    }

    public boolean isContextItemRequired(XPathOperationOrFuntion function) {
        return false;
    }

    public ResultTypeCalculator getResultTypeCalculator() {
        return null;
    }
    
    public FunctionType getFunctionType() {
        return FunctionType.EXT_FUNCTION;
    }

    public String getLongDescription() {
        return null;
    }

    public String getShortDescription() {
        return null;
    }

    private QName myQName;
    private String myDisplayName;
    private XPathType myResultType;
    private List<AbstractArgument> myArguments;
}
