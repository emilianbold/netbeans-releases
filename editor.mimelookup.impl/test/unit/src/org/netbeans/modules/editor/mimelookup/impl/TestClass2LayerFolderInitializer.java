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


package org.netbeans.modules.editor.mimelookup.impl;

import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
/**
 *
 * @author Martin Roskanin
 */
public class TestClass2LayerFolderInitializer implements Class2LayerFolder{

    /** Creates a new instance of TestClass2LayerFolderInitializer */
    public TestClass2LayerFolderInitializer() {
    }

    public Class getClazz(){
        return TestLookupObject.class;
    }

    /** Gets layer folder name, where the class should be found.
     *  Folder should be located in the appropriate mime type path, i.e.
     *  Editors/text/x-java/@lt;desired-layer-folder-name@gt;
     *  
     *  @return layer folder name
     */
    public String getLayerFolderName(){
        return "testLookup";
    }

    public org.netbeans.spi.editor.mimelookup.InstanceProvider getInstanceProvider() {
        return null;
    }
    
}
