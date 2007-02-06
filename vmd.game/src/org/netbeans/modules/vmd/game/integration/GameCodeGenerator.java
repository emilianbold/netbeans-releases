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
 *
 */

package org.netbeans.modules.vmd.game.integration;

import org.netbeans.modules.vmd.api.codegen.JavaCodeGenerator;
import org.netbeans.modules.vmd.api.io.CodeGenerator;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.providers.DataObjectInterface;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.game.GameController;
import org.openide.loaders.DataObject;

import javax.swing.text.StyledDocument;

/**
 * @author David Kaspar
 */
public class GameCodeGenerator implements CodeGenerator {

    public void validateModelForCodeGeneration (DataObjectContext context, DesignDocument document) {
//        if (GameController.PROJECT_TYPE_GAME.equals (context.getProjectType ())) {
//        }
    }

    public void updateModelFromCode (DataObjectContext context, DesignDocument document) {
        if (GameController.PROJECT_TYPE_GAME.equals (context.getProjectType ())) {
            DataObject dataObject = context.getDataObject ();
            StyledDocument styledDocument = IOSupport.getDataObjectInteface (dataObject).getEditorDocument ();
            JavaCodeGenerator.getDefault ().updateUserCodesFromEditor (styledDocument);
        }
    }

    public void updateCodeFromModel (DataObjectContext context, DesignDocument document) {
        if (GameController.PROJECT_TYPE_GAME.equals (context.getProjectType ())) {
            DataObject dataObject = context.getDataObject ();
            DataObjectInterface dataObjectInteface = IOSupport.getDataObjectInteface (dataObject);
            StyledDocument styledDocument = dataObjectInteface.getEditorDocument ();
            JavaCodeGenerator.getDefault ().generateCode (styledDocument, document);
            dataObjectInteface.discardAllEditorSupportEdits ();
        }
    }

}
