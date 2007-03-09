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

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 * We need to write unit tests for this module, however there is a
 * challenge. This class is purely needed to solve that challenge.
 * 
 * In order to get a model source or a model, we must first have a
 * CatalogModel. In general you can get a CatalogModel from the
 * CatalogModelFactory. But for unit tests, we need a special TestCatalogModel.
 *
 * In this module, we do not directly deal with CatalogModel and hence it is
 * very difficult to use a TestCatalogModel from the unit tests.
 *
 * So the code uses lookup to find all CatalogModelProvider. If found uses it,
 * else uses the real CatalogModel. The way it will work is, unit test code will
 * create CatalogModelProvider which is going to return a TestCatalogModel.
 *
 * For all other use-cases, no CatalogModelProvider will be found and the module
 * will use the project based CatalogModel.
 *
 * @see DefaultModelProvider#getCatalogModelProvider
 *
 * @author Samaresh
 */
public abstract class CatalogModelProvider {
    abstract CatalogModel getCatalogModel();
    
    abstract ModelSource getModelSource(FileObject fo, boolean editable)
    throws CatalogModelException ;
}
