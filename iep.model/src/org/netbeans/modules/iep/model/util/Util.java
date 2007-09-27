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

package org.netbeans.modules.iep.model.util;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.impl.IEPComponentBase;

import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



public class Util {

	/*
	 * XML entities and symbols helper constant.
	 */
	public static final char SEMICOLON = ';'; // NOI18N

	public static final char AMP = '&'; // NOI18N

	public static final String QUOT = AMP + "quot" + SEMICOLON; // NOI18N

	public static final String APOS = AMP + "apos" + SEMICOLON; // NOI18N

	public static final String GT = AMP + "gt" + SEMICOLON; // NOI18N

	private static final Map<String, Character> PRIVATE_ENTITIES = new HashMap<String, Character>();

	static final Map<String, Character> XML_ENTITIES = Collections
			.unmodifiableMap(PRIVATE_ENTITIES);
	static {
		PRIVATE_ENTITIES.put(GT, '>');
		PRIVATE_ENTITIES.put(APOS, '\'');
		PRIVATE_ENTITIES.put(QUOT, '"');
	}

	/*
	 * This method assume on input string that can contain : "&gt;", "&apos;",
	 * "&quot;". Method replace those strings to ">", "'" , "\"" respectively.
	 * Please note that there can be also "&lt;" and "&amp;" in original string,
	 * but this method doesn't assume presence of those symbols in string. This
	 * is because <code>str</code> in argument comes from XAM/XDM and it
	 * already have changed those symbols to appropriate values.
	 */
	public static String hackXmlEntities(String str) {
		if (str == null) {
			return null;
		}
		int index = str.indexOf(AMP);
		if (index >= 0) {
			StringBuilder builder = new StringBuilder(str);
			for (Entry<String, Character> entry : XML_ENTITIES.entrySet()) {
				String entity = entry.getKey();
				Character value = entry.getValue();
				for (index = builder.indexOf(entity); index >= 0; index = builder
						.indexOf(entity)) {
					builder.replace(index, index + entity.length(), Character
							.toString(value));
				}
			}
			return builder.toString();
		} else {
			return str;
		}
	}
	

   /*
    @SuppressWarnings("unchecked")
    public static Collection<WSDLModel> getWSDLModels( IEPModel model,
            String namespace ) throws CatalogModelException
    {
        if ( namespace == null ) {
            return Collections.EMPTY_LIST;
        }
        List<WSDLModel> list = new LinkedList<WSDLModel>();
        collectWsdlModelsViaImports(model, namespace, list);

        //collectWsdlModelsViaFS(model, namespace, list);
        
        return list;

    }
    
    
    private static void collectWsdlModelsViaImports( WLMModel model, String namespace, 
            List<WSDLModel> list ) throws CatalogModelException 
    {
       Collection <TImport> imports = model.getTasks().getImports();
        for (TImport imp : imports) {
            if ( namespace.equals(imp.getNamespace()) ){
                WSDLModel wsdlModel = imp.getImportedWSDLModel();
                if ( wsdlModel!= null && wsdlModel.getState() == Model.State.VALID ){
                    list.add( wsdlModel );
                }
            }
        }
    }
     */


	public static boolean findOptInPortType(Operation opt1, PortType portType) {
		// TODO Auto-generated method stub
		boolean result = false;
		Collection<Operation> opts = portType.getOperations();
		for (Operation opt: opts) {
			if (opt.getName().equals(opt1.getName())) {
				result = true;
				break;
			}
		}
		return result;

	}

	    
	    public static Element loadString(String xmlStr) throws Exception {
	        Document doc = XmlUtil.createDocumentFromXML(true, xmlStr);
	        return doc.getDocumentElement();
	    }

	    public static Element getElement(DOMSource source) throws Exception {
	        Node node = source.getNode();
	        if (node instanceof Document) {
	            return ((Document) node).getDocumentElement();
	        } else {
	            return (Element) node;
	        }
	    }


		public static String getNewPrefix(Component impl) {
			// TODO Auto-generated method stub
			IEPComponentBase com = IEPComponentBase.class.cast (impl);
			int i = 0; 
			String prefix = null;
			while (true) {
				prefix = "ns" + i;
			   if ( com.lookupNamespaceURI(prefix) == null) {
				   break;
			   }
			   ++i;
			}
			return prefix;
		}	
}
