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
