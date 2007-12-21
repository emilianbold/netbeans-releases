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
