/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.editor.structure;

import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
 
/**
 * Mapping the DocumentModel folder to DocumentModelProvider class.
 *
 * @author Marek Fukala
 */
public class DocumentModelProviderClass2Layer implements Class2LayerFolder {
 
    public DocumentModelProviderClass2Layer() {
    }
 
    public Class getClazz() {
        return DocumentModelProvider.class;
    }
 
    public String getLayerFolderName() {
        return DocumentModelProviderFactory.FOLDER_NAME;
    }
 
    public InstanceProvider getInstanceProvider() {
        return null;
    }
     
}
