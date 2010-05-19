/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.util.HashMap;
import javax.xml.namespace.QName;

/*
 *
 * @author Sherry Weng
 * @version 
 */
public class WSDLUtilities {
    private static HashMap excludedTypes = new HashMap();
    private static final String XSD_2001_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    private static final String XSD_1999_NAMESPACE = "http://www.w3.org/1999/XMLSchema";
    
    public static boolean isBuiltInType(QName typename) {
        return  XSD_2001_NAMESPACE.equals(typename.getNamespaceURI()) ||
                XSD_1999_NAMESPACE.equals(typename.getNamespaceURI());
    }
    
    public static boolean isBuiltInType(String namespaceURI) {
        return (XSD_2001_NAMESPACE.equals(namespaceURI) ||
                XSD_1999_NAMESPACE.equals(namespaceURI));
    }
    
    public static boolean isXsdAnyType(QName typename) {
    	return (XSD_2001_NAMESPACE.equals(typename.getNamespaceURI()) ||
                XSD_1999_NAMESPACE.equals(typename.getNamespaceURI()) ) &&
                "anyType".equals(typename.getLocalPart());
    }
}
