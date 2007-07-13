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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.core.syntax;

import org.netbeans.modules.web.core.syntax.spi.AutoTagImporterProvider;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;

/**
 * Mapping the AutoTagImportProvider folder to AutoTagImportProvider class.
 *
 * @author Marek Fukala
 */
public class AutoTagImporterProviderClass2Layer implements Class2LayerFolder {

    private static final String FOLDER_NAME = "AutoTagImportProviders";
    
    public AutoTagImporterProviderClass2Layer() {
    }

    public Class getClazz() {
        return AutoTagImporterProvider.class;
    }
 
    public String getLayerFolderName() {
        return FOLDER_NAME;
    }
 
    public InstanceProvider getInstanceProvider() {
        return null;
    }
     
}
