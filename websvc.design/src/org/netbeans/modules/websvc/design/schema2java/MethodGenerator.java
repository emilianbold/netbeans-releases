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

package org.netbeans.modules.websvc.design.schema2java;

import java.io.IOException;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
public class MethodGenerator {
    FileObject implClassFo;
    WsdlModel wsdlModel;
    /** Creates a new instance of MethodGenerator */
    public MethodGenerator(WsdlModel wsdlModel, FileObject implClassFo) {
        this.wsdlModel=wsdlModel;
        this.implClassFo=implClassFo;
    }
    /** generate new method to implementation class
     */ 
    public void generateMethod(String operationName) {   
        
        org.netbeans.modules.websvc.core.MethodGenerator delegatedGenerator = 
                new org.netbeans.modules.websvc.core.MethodGenerator(wsdlModel, implClassFo);
        try {
            delegatedGenerator.generateMethod(operationName);
        } catch (IOException ex) {
            
        }

        //open in editor
        try {
            DataObject dobj = DataObject.find(implClassFo);
            openFileInEditor(dobj);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }

    }
    
    private static void openFileInEditor(DataObject dobj){
        final EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }
    
}
