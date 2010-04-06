/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.wsdlextui.property.soap12;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.wsdl.refactoring.spi.WSDLExtensibilityElementRefactoringSupport;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author skini
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.refactoring.spi.WSDLExtensibilityElementRefactoringSupport.class)
public class SOAP12BindingRefactoringSupport extends WSDLExtensibilityElementRefactoringSupport {

    @Override
    public String getNamespace() {
        return SOAP12QName.SOAP_NS_URI;
    }

    @Override
    public WSDLVisitor getRenameVisitor(Referenceable referenced, String oldName) {
        return new SOAP12RenameVisitor(referenced, oldName);
    }

    @Override
    public WSDLVisitor getReferenceFinderVisitor(Referenceable referenced, List<Component> referencing) {
        return new SOAP12ReferenceFinderVisitor(referenced, referencing);
    }

}
