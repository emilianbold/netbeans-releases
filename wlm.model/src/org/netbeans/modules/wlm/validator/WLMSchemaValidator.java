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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.netbeans.modules.wlm.model.api.WLMModel;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class WLMSchemaValidator extends XsdBasedValidator {
    
    static final String wsdlXSDUrl = "/org/netbeans/modules/wlm/validator/resources/wfse_def.xsd";
    
    public String getName() {
        return "WLMSchemaValidator"; //NO I18N
    }
    
    @Override
    protected Schema getSchema(Model model) {
        if (! (model instanceof WLMModel)) {
            return null;
        }
        
        InputStream wsdlSchemaInputStream = WLMSchemaValidator.class.getResourceAsStream(wsdlXSDUrl);
        Source wsdlSource = new StreamSource(wsdlSchemaInputStream);
        wsdlSource.setSystemId(WLMSchemaValidator.class.getResource(wsdlXSDUrl).toString());
        
        
        ArrayList<Source> isList = new ArrayList<Source>();
        isList.add(wsdlSource);
        Schema schema = getCompiledSchema(isList.toArray(new Source[isList.size()]), null, new SchemaErrorHandler());
        
        return schema;
    }

    class SchemaErrorHandler implements ErrorHandler {
    	public void error(SAXParseException exception) throws SAXException {
    		Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SchemaErrorHandler: " + exception.getMessage(), exception);
    	}
    	
    	public void fatalError(SAXParseException exception) throws SAXException {
    		Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SchemaErrorHandler: " + exception.getMessage(), exception);
    	}
    	
    	public void warning(SAXParseException exception) throws SAXException {
    		
    	}
    }
    
    
}
