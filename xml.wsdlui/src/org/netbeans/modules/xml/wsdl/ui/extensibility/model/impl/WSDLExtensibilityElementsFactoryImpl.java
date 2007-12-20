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
 * Created on May 25, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl;

import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WSDLExtensibilityElementsFactoryImpl extends WSDLExtensibilityElementsFactory {
	
	private static final String DEFAULT_FOLDER = "WSDLEditor";//NOI18N
	
	private WSDLExtensibilityElements mElements;
	
	public WSDLExtensibilityElementsFactoryImpl() {
		DataFolder wsdlEditorFolder = getRootFolder(DEFAULT_FOLDER);
		if(wsdlEditorFolder == null) {
			throw new IllegalStateException(NbBundle.getMessage(WSDLExtensibilityElementsFactoryImpl.class, "ERR_MSG_NO_FOLDER_FOUND", DEFAULT_FOLDER));
		}
		
		mElements = new WSDLExtensibilityElementsImpl(wsdlEditorFolder);
	}
	
	private DataFolder getRootFolder(String folderName) {
        try {
            org.openide.filesystems.FileObject fo =
                    Repository.getDefault().getDefaultFileSystem().findResource(folderName);

            if (fo == null) {
                throw new Exception(NbBundle.getMessage(WSDLExtensibilityElementsFactoryImpl.class, "ERR_MSG_FOLDERN_NOT_FOUND", folderName));
            }

            return DataFolder.findFolder(fo);
        } catch (Exception ex) {
            throw new InternalError(NbBundle.getMessage(WSDLExtensibilityElementsFactoryImpl.class, "ERR_MSG_FOLDERN_NOT_FOUND", folderName));
        }
    }

	
	public WSDLExtensibilityElements getWSDLExtensibilityElements() {
		return mElements;
	}
}
