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

package org.netbeans.modules.xml.schema.abe.nodes;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.abe.InstanceUIContext;
import org.netbeans.modules.xml.schema.abe.nodes.properties.*;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AnyElementNode extends ABEAbstractNode {
    
    
    /**
     * Creates a new instance of ElementNode
     */
    public AnyElementNode(AbstractElement element, InstanceUIContext context) {
        super(element, context);
    }
    
    public AnyElementNode(AbstractElement element) {
	super(element, new ABENodeChildren(element));
	setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/abe/resources/element.png");
    
    }
    
    protected void populateProperties(Sheet sheet) {
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        if(set == null) {
            set = sheet.createPropertiesSet();
        }
        
        try {
			// minOccurs
			Property minOccursProp = new MinOccursProperty(
				getAXIComponent(),
				String.class,
				Element.PROP_MINOCCURS,
				NbBundle.getMessage(ElementNode.class,"PROP_MinOccurs_DisplayName"), // display name
				NbBundle.getMessage(ElementNode.class,"PROP_MinOccurs_ShortDescription")	// descr
			);
			set.put(new SchemaModelFlushWrapper(getAXIComponent(), minOccursProp, getContext()));

			// maxOccurs
			Property maxOccursProp = new BaseABENodeProperty(
				getAXIComponent(),
				String.class,
				Element.PROP_MAXOCCURS,
				NbBundle.getMessage(ElementNode.class,"PROP_MaxOccurs_DisplayName"), // display name
				NbBundle.getMessage(ElementNode.class,"PROP_MaxOccurs_ShortDescription"),	// descr
				MaxOccursEditor.class
			);
			set.put(new SchemaModelFlushWrapper(getAXIComponent(), maxOccursProp, getContext()));
			
            // processContents
            Property processContentsProp = new BaseABENodeProperty(
				getAXIComponent(), // schema component
				org.netbeans.modules.xml.schema.model.AnyElement.ProcessContents.class, // Any.ProcessContents.class as value type
				AnyElement.PROP_PROCESSCONTENTS,
				NbBundle.getMessage(AnyElementNode.class,"PROP_ProcessContentsProp_DisplayName"), // display name
				NbBundle.getMessage(AnyElementNode.class,"PROP_ProcessContentsProp_ShortDescription"),	// descr
				ProcessContentsEditor.class);
            set.put(new SchemaModelFlushWrapper(getAXIComponent(), processContentsProp, getContext()));
			
        } catch (Exception ex) {
        }
        
        sheet.put(set);
    }
    
    public String getName(){
		if((AbstractElement) super.getAXIComponent() != null)
			return ((AbstractElement) super.getAXIComponent()).getName();
		else
			return "";		
    }

    protected String getTypeDisplayName() {
	return NbBundle.getMessage(AttributeNode.class,"LBL_Any");
    }
    
}
