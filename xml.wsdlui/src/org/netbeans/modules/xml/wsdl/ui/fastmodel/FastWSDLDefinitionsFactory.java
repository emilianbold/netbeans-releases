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

/*
 * Created on Jan 26, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.fastmodel;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import org.openide.util.NbBundle;



/**
 * @author radval
 *
 * A factory which parses wsdl fast.
 * Just parse some attributes from wsdl and ignore rests.
 */
public abstract class FastWSDLDefinitionsFactory {
	
	private static FastWSDLDefinitionsFactory factory;
	
	public FastWSDLDefinitionsFactory() {
		
	}
        
    /**
     * Gets the Fast WSDL Definitions factory singleton.
     * @return  a Fast WSDL Definitions factory.
     * @throws  EInsightModelException  When implementing factory class not found.
     */
    public static synchronized FastWSDLDefinitionsFactory getInstance() throws Exception {
        if (null == factory) {
            String wsdlFac = System.getProperty(FastWSDLDefinitionsFactory.class.getName(),
                                                "org.netbeans.modules.xml.wsdl.ui.fastmodel.impl.FastWSDLDefinitionsFactoryImpl");//NOI18N
            try {
                factory = (FastWSDLDefinitionsFactory) Class.forName(wsdlFac).newInstance();
            } catch (Exception e) {
                throw new Exception(
                    NbBundle.getMessage(FastWSDLDefinitionsFactory.class, "ERR_MSG_WSDL_CLASS_NOT_FOUND", wsdlFac), e);
            }
        }
        return factory;
    }    
    
    public abstract FastWSDLDefinitions newFastWSDLDefinitions(Reader in, boolean parseImports);
    
    public abstract FastWSDLDefinitions newFastWSDLDefinitions(InputStream in, boolean parseImports);
    
	public abstract FastWSDLDefinitions newFastWSDLDefinitions(String defFileUrl);
	
	public abstract FastWSDLDefinitions newFastWSDLDefinitions(String defFileUrl, 
													  			boolean parseImports);
	public abstract FastWSDLDefinitions newFastWSDLDefinitions(File file);
	
	public abstract FastWSDLDefinitions newFastWSDLDefinitions(File file, 
	        boolean parseImports);
		
}
