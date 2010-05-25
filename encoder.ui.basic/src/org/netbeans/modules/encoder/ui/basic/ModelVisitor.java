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

package org.netbeans.modules.encoder.ui.basic;

import javax.swing.text.Document;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Interface used to do model level visit, which only visits the model
 * specified and all the models that are reachable through direct references
 * (inlcudes, imports and redefines) or indirect references.
 *
 * @author Jun Xu
 */
public interface ModelVisitor {

    /**
     * Visits the model.
     *
     * @param model the model
     * @return false if want to stop visiting, otherwise true
     */
    public boolean visit(SchemaModel model);
    
    /**
     * Visits the document of the model.
     *
     * @param doc the document
     * @return false if want to stop visiting, otherwise true
     */
    public boolean visit(Document doc);
    
    /**
     * Visits the file object of the model.
     * 
     * @param fileObj the file object
     * @return false if want to stop visiting, otherwise true
     */
    public boolean visit(FileObject fileObj);
    
    /**
     * Visits the data object of the model
     *
     * @param dataObj the data object
     * @return false if want to stop visiting, otherwise true
     */
    public boolean visit(DataObject dataObj);
}
