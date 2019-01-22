/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.midp.codegen;

import org.netbeans.modules.vmd.api.codegen.JavaCodeGenerator;
import org.netbeans.modules.vmd.api.io.CodeGenerator;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.providers.DataObjectInterface;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.openide.loaders.DataObject;

import javax.swing.text.StyledDocument;

/**
 * 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.io.CodeGenerator.class)
public class MidpCodeGenerator implements CodeGenerator {

    public void validateModelForCodeGeneration (DataObjectContext context, DesignDocument document) {
//        if (MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())) {
//            System.out.println ("MidpCodeGenerator.validateModelForCodeGeneration");
//        }
        DataObject dataObject = context.getDataObject ();
            DataObjectInterface dataObjectInteface = IOSupport.getDataObjectInteface (dataObject);
            StyledDocument styledDocument = dataObjectInteface.getEditorDocument ();
            JavaCodeGenerator.getDefault ().preUpdateCode (styledDocument, document);
    }

    public void updateModelFromCode (DataObjectContext context, DesignDocument document) {
        if (MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())) {
//            System.out.println ("MidpCodeGenerator.updateModelFromCode");
            DataObject dataObject = context.getDataObject ();
            StyledDocument styledDocument = IOSupport.getDataObjectInteface (dataObject).getEditorDocument ();
            JavaCodeGenerator.getDefault ().updateUserCodesFromEditor (styledDocument, document);
        }
    }

    public void updateCodeFromModel (DataObjectContext context, DesignDocument document) {
        if (MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())) {
//            System.out.println ("MidpCodeGenerator.updateCodeFromModel");
            DataObject dataObject = context.getDataObject ();
            DataObjectInterface dataObjectInteface = IOSupport.getDataObjectInteface (dataObject);
            StyledDocument styledDocument = dataObjectInteface.getEditorDocument ();
            JavaCodeGenerator.getDefault ().generateCode (styledDocument, document);
            dataObjectInteface.discardAllEditorSupportEdits ();
        }
    }

}
