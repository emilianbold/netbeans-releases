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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.iep.editor.refactoring;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPModelFactory;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.xml.refactoring.spi.RefactoringEngine;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class IEPRefactoringEngine {//extends RefactoringEngine {
    public static final String IEP_MIME_TYPE = "text/x-iep+xml";  // NOI18N
    public static final String WSDL_MIME_TYPE = "text/x-wsdl+xml";  // NOI18N
    
    /** Creates a new instance of WSDLRefactoringEngine */
    public IEPRefactoringEngine() {
    }

    public Component getSearchRoot(FileObject fo) throws IOException {
        if (IEP_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
            return getPlanComponent(fo);
        } else if (WSDL_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
            return getWSDLDefinitions(fo);
        }
        
        return null;
    }
    
    public static Definitions getWSDLDefinitions(FileObject fo) throws IOException {
        
        
        ModelSource modelSource = Utilities.getModelSource(fo, true);
        WSDLModel model = WSDLModelFactory.getDefault().getModel(modelSource);
        if (model != null) {
            if (model.getState().equals(Model.State.VALID)) {
                return model.getDefinitions();
            } else {
                String msg = NbBundle.getMessage(IEPRefactoringEngine.class, 
                        "MSG_ModelSourceMalformed", fo.getPath());
                throw new IOException(msg);
            }
        }
        return null;
    }
    
    public static PlanComponent getPlanComponent(FileObject fo) throws IOException {
        
        ModelSource modelSource = Utilities.getModelSource(fo, true);
        IEPModel model = IEPModelFactory.getDefault().getModel(modelSource);
        if (model != null) {
            if (model.getState().equals(Model.State.VALID)) {
                return model.getPlanComponent();
            } else {
                String msg = NbBundle.getMessage(IEPRefactoringEngine.class, 
                        "MSG_ModelSourceMalformed", fo.getPath());
                throw new IOException(msg);
            }
        }
        return null;
    }

//    public List<WSDLRefactoringElement> findUsages(Component target, Component searchRoot) {
//        if (target instanceof ReferenceableWSDLComponent &&
//            searchRoot instanceof Definitions) 
//        {
//            return new FindWSDLUsageVisitor().findUsages((ReferenceableWSDLComponent)target, (Definitions)searchRoot);
//        }
//        return Collections.emptyList();
//    }

  
    
   public void _refactorUsages(Model model,Set<RefactoringElementImplementation> elements, AbstractRefactoring request) {
        if (request == null || elements == null || model == null) return;
        if (! (model instanceof IEPModel)) return;

        
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
        
            for (RefactoringElementImplementation u : elements) {
//                Import ref = u.getLookup().lookup(Import.class);
//                if (ref!=null) {
//                    String newLocation = ref.getLocation();
//                    if(request instanceof RenameRefactoring ) {
//                         newLocation = SharedUtils.calculateNewLocationString(ref.getLocation(), (RenameRefactoring)request);
//                    } else if (request instanceof MoveRefactoring) {
//                        try {
//                             newLocation = SharedUtils.calculateNewLocationString(model , (MoveRefactoring)request);
//                        } catch (Exception io) {
//                            //do nothing, let the old schema location remain
//                        }
//                    }
//                    ref.setLocation(newLocation);
//                }
                PlanComponent ref = u.getLookup().lookup(PlanComponent.class);
                if(ref != null) {
                     
                    if(request instanceof RenameRefactoring ) {
                        RenameRefactoring rr = (RenameRefactoring) request;
                        String newName = rr.getNewName();
                        ref.setName(newName);
                    } else if (request instanceof MoveRefactoring) {
                        
                    }
                }
            }
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }
    
    public String calculateNewLocationString(String currentLocationString, RenameRefactoring request) {
        StringBuilder sb = new StringBuilder();
        int i = currentLocationString.lastIndexOf('/');
        if (i > -1) {
            sb.append(currentLocationString.substring(0, i+1));
        }
        sb.append(request.getNewName());
        sb.append("."); //NOI18N
        Referenceable ref = request.getRefactoringSource().lookup(Referenceable.class);
        Model model = SharedUtils.getModel(ref);
        FileObject fo = model.getModelSource().getLookup().lookup(FileObject.class);
        sb.append(fo.getExt());
        return sb.toString();
    }

    

    
    
    
    public String getModelReference(Component component) {
        if (component instanceof Import) {
            return ((Import)component).getLocation();
        }
        return null;
    }
    
  

   
}
