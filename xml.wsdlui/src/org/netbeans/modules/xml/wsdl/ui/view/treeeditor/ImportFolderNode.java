/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.ui.cookies.AddChildWSDLElementCookie;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ImportWSDLNewType;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;


/**
 *
 * @author Ritesh Adval
 *
 */
public class ImportFolderNode extends FolderNode {
    
    private Definitions mDef = null;
    
    public ImportFolderNode(Definitions element) {
        super(new ImportFolderChildren(element), element, Import.class);
        mDef = element;
        this.setDisplayName(NbBundle.getMessage(ImportFolderNode.class, 
        "IMPORT_FOLDER_NODE_NAME"));
        getLookupContents().add(new AddChildWSDLElementCookie(element));
        this.addNodeListener(new WSDLNodeListener(this));
    }
    
    public Object getWSDLConstruct() {
        return mDef;
    }
    
    public static final class ImportFolderChildren extends GenericWSDLComponentChildren {
        public ImportFolderChildren(Definitions definitions) {
            super(definitions);
        }
        
        @Override
        protected Collection getKeys() {
            Definitions def = (Definitions) getWSDLComponent();
            return def.getImports();
        }
    }
    
    @Override
    public final NewType[] getNewTypes()
    {
        if (isEditable()) {
            return new NewType[] {
                    //new ImportSchemaNewType(mDef),
                    new ImportWSDLNewType(mDef),
            };
        }
        return new NewType[] {};
    }

    @Override
    public Class getType() {
        return Import.class;
    }
    
}
