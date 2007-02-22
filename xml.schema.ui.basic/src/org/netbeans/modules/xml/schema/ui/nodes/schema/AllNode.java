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

package org.netbeans.modules.xml.schema.ui.nodes.schema;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Occur;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.editors.ZeroOrOneEditor;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Jeri Lockhart
 */
public class AllNode extends SchemaComponentNode<All>
{
    /**
     *
     *
     */
    public AllNode(SchemaUIContext context,
	SchemaComponentReference<All> reference,
	Children children) {
	super(context,reference,children);
	
	setIconBaseWithExtension(
	    "org/netbeans/modules/xml/schema/ui/nodes/resources/"+
	    "all.png");
	
	setDefaultExpanded(true);
    }
    
    
    /**
     *
     *
     */
    @Override
	public String getTypeDisplayName() {
	return NbBundle.getMessage(AllNode.class,
	    "LBL_AllNode_TypeDisplayName"); // NOI18N
    }
    
    
    @Override
	protected Sheet createSheet() {
	Sheet sheet = super.createSheet();
	Sheet.Set props = sheet.get(Sheet.PROPERTIES);
	if (props == null) {
	    props = Sheet.createPropertiesSet();
	    sheet.put(props);
	}
	
	try {
	    if (getReference().get().allowsFullMultiplicity()) {
		
		Node.Property minOccursProp = new BaseSchemaProperty(
		    getReference().get(),
		    Occur.ZeroOne.class, // Occur.ZeroOne.class as value type
		    All.MIN_OCCURS_PROPERTY, //property name
		    NbBundle.getMessage(AllNode.class,"PROP_MinOccurs_DisplayName"), // display name
		    NbBundle.getMessage(AllNode.class,"HINT_zero_or_one"),	// descr
		    ZeroOrOneEditor.class // ZeroOrOneEditor
		    );
		props.put(new SchemaModelFlushWrapper(getReference().get(), minOccursProp));
	    }
	} catch(NoSuchMethodException nsme) {
	    assert false : "properties should be defined";
	}
	return sheet;
    }
}
