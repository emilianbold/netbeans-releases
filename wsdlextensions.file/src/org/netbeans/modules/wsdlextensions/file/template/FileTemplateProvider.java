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

package org.netbeans.modules.wsdlextensions.file.template;

import java.io.InputStream;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementTemplateProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator;
import org.openide.util.NbBundle;

public class FileTemplateProvider extends ExtensibilityElementTemplateProvider {
    
    static final String fileTemplateUrl = "/org/netbeans/modules/wsdlextensions/file/template/template.xml";

    public InputStream getTemplateInputStream() {
        return FileTemplateProvider.class.getResourceAsStream(fileTemplateUrl);
    }

    public String getLocalizedMessage(String str, Object[] objects) {
        return NbBundle.getMessage(FileTemplateProvider.class, str, objects);
    }
    
    @Override
    public InputStream getTemplateFileInputStream(String filePath) {
        return FileTemplateProvider.class.getResourceAsStream("/org/netbeans/modules/wsdlextensions/file/template/" + filePath);
    }
    
    @Override
    public WSDLWizardExtensionIterator getWSDLWizardExtensionIterator(WSDLWizardContext context) {
        return new FileWSDLWizardExtensionIterator(context);
    }

}
