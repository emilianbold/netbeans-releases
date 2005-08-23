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
package org.netbeans.lib.editor.codetemplates;


import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;

/**
 * Mapping the CodeTemplateProcessorFactories folder to CodeTemplateProcessorFactory class.
 *
 * @author Martin Roskanin
 */
public class CodeTemplateClass2LayerFolder implements Class2LayerFolder {

    public CodeTemplateClass2LayerFolder() {
    }

    public Class getClazz() {
        return CodeTemplateProcessorFactory.class;
    }

    public String getLayerFolderName() {
        return "CodeTemplateProcessorFactories"; // NOI18N
    }

    public InstanceProvider getInstanceProvider() {
        return null;
    }
    
}
