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
package org.netbeans.modules.editor.fold;


import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;

/**
 * Mapping the FoldManager folder to FoldManagerFactory class.
 *
 * @author Martin Roskanin
 */
public class FoldManagerClass2LayerFolder implements Class2LayerFolder {

    public FoldManagerClass2LayerFolder() {
    }

    public Class getClazz() {
        return FoldManagerFactory.class;
    }

    public String getLayerFolderName() {
        return "FoldManager"; // NOI18N
    }

    public InstanceProvider getInstanceProvider() {
        return null;
    }
    
}
