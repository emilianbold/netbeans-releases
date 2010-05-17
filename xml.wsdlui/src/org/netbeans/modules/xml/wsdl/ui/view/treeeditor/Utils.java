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
 * Created on Jun 23, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.AbstractXSDVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaElementAttributeFinderVisitor;
import org.netbeans.modules.xml.xam.Nameable;
import org.w3c.dom.NamedNodeMap;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Utils {
	
	public static boolean isMissingAttributes(WSDLComponent element, Element schemaElement) {
		boolean result = false;
		
//		//go through attributes defined in schema element
//		//and check if they are already available in WSDLElement
//		//if so, skip them and add if not.
		NamedNodeMap elementAttrs = element.getPeer().getAttributes();
		SchemaElementAttributeFinderVisitor seaFinder = new SchemaElementAttributeFinderVisitor(schemaElement);
		schemaElement.accept(seaFinder);
		List<Attribute> attrs = seaFinder.getAttributes();
		Iterator<Attribute> it = attrs.iterator();
		while(it.hasNext()) {
		    Attribute attr = it.next();
		    Nameable namedAttr = (Nameable) attr;
		    //check if attribute is already added
		    //TODO: need to check namespace as well
		    if(elementAttrs.getNamedItem(namedAttr.getName())== null) {
		        result = true;
		        break;
		    }
		}
		
		return result;
	}
    
    public static boolean isExtensionAttributesAllowed(Element element) {
        AnyAttributesVisitor visitor = new AnyAttributesVisitor();
        element.accept(visitor);
        return visitor.isExtensionAttributesAllowed();
    }
    
    


}

class AnyAttributesVisitor extends AbstractXSDVisitor{
    private boolean hasAnyAttributes = false;
    @Override
    public void visit(AnyAttribute anyAttr) {
        hasAnyAttributes = true;
    }
    public boolean isExtensionAttributesAllowed() {
        return hasAnyAttributes;
    }
};
