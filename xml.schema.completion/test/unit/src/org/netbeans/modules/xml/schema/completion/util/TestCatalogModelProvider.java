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
package org.netbeans.modules.xml.schema.completion.util;

import org.netbeans.modules.xml.schema.completion.util.CatalogModelProvider;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 * Helps in getting the model for code completion.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class TestCatalogModelProvider extends CatalogModelProvider {
    
    private TestCatalogModel catalogModel;
    
    public TestCatalogModelProvider() {
        catalogModel = TestCatalogModel.getDefault();
    }
    
    CatalogModel getCatalogModel() {
        return catalogModel;
    }

    ModelSource getModelSource(FileObject fo, boolean editable) throws CatalogModelException {
        return catalogModel.createModelSource(fo, editable);
    }
    
}
