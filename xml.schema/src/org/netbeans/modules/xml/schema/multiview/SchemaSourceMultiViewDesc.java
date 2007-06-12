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

package org.netbeans.modules.xml.schema.multiview;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.schema.SchemaEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
/**
 *
 * @author Jeri Lockhart
 */
public class SchemaSourceMultiViewDesc 
                implements MultiViewDescription, Serializable {
    
    
    private static final long serialVersionUID = -4505309173196320880L;
    public static final String PREFERRED_ID = "xml-schema-sourceview";
    private SchemaDataObject schemaDataObject;
    
    // Constructor for reserialization
    public SchemaSourceMultiViewDesc( ) {
    }
    
    /** Creates a new instance of SchemaSourceMultiviewDesc */
    public SchemaSourceMultiViewDesc(SchemaDataObject schemaDataObject) {
        this.schemaDataObject = schemaDataObject;
    }

    public String preferredID() {
	return PREFERRED_ID;
    }

    public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public java.awt.Image getIcon() {
        return Utilities.loadImage(SchemaDataObject.SCHEMA_ICON_BASE_WITH_EXT);
    }

    public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SchemaColumnViewMultiViewDesc.class, "LBL_sourceView_name");
    }

    public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            SchemaEditorSupport editorSupport = 
		schemaDataObject.getSchemaEditorSupport();
            if (editorSupport != null) {
                SchemaSourceMultiViewElement editorComponent = new SchemaSourceMultiViewElement(schemaDataObject);
                return editorComponent;
            }
            return MultiViewFactory.BLANK_ELEMENT;
	
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
	out.writeObject(schemaDataObject);
    }

    public void readExternal(ObjectInput in)
	throws IOException, ClassNotFoundException
    {
	Object firstObject = in.readObject();
	if (firstObject instanceof SchemaDataObject)
	    schemaDataObject = (SchemaDataObject) firstObject;
    }
    
}
