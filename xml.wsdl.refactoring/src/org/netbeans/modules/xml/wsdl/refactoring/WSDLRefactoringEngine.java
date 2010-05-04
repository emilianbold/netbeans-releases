/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Nam Nguyen
 */
public class WSDLRefactoringEngine {
    public static final String WSDL_MIME_TYPE = "text/x-wsdl+xml";  // NOI18N

    public Component getSearchRoot(FileObject fo) throws IOException {
        return getWSDLDefinitions(fo);
    }
    
    public static Definitions getWSDLDefinitions(FileObject fo) throws IOException {
        if (! WSDL_MIME_TYPE.equals(FileUtil.getMIMEType(fo))) {
            return null;
        }
        ModelSource modelSource = Utilities.getModelSource(fo, true);
        WSDLModel model = WSDLModelFactory.getDefault().getModel(modelSource);
        if (model != null) {
            if (model.getState().equals(Model.State.VALID)) {
                return model.getDefinitions();
            } else {
                String msg = NbBundle.getMessage(WSDLRefactoringEngine.class, 
                        "MSG_ModelSourceMalformed", fo.getPath());
                throw new IOException(msg);
            }
        }
        return null;
    }

    public List<WSDLRefactoringElement> findUsages(Component target, Component searchRoot) {
        if (target instanceof ReferenceableWSDLComponent &&
            searchRoot instanceof Definitions) 
        {
            return new FindWSDLUsageVisitor().findUsages((ReferenceableWSDLComponent)target, (Definitions)searchRoot);
        }
        return Collections.emptyList();
    }

    public void _refactorUsages(Model model, Set<RefactoringElementImplementation> elements, AbstractRefactoring request) {
//System.out.println();
//System.out.println("DO REFACTOR: " + elements.size());

        if (request == null || elements == null || model == null) {
//System.out.println("return 1");
            return;
        }
//System.out.println("           " + model.getModelSource().getLookup().lookup(FileObject.class));
        boolean startTransaction = !model.isIntransaction();

        try {
            if (startTransaction) {
                model.startTransaction();
            }
            for (RefactoringElementImplementation element : elements) {
                Import _import = element.getLookup().lookup(Import.class);
//System.out.println("          import: " + _import);

                if (_import != null) {
                    String newLocation = _import.getLocation();
                    
                    if (request instanceof RenameRefactoring) {
                         newLocation = SharedUtils.calculateNewLocationString(_import.getLocation(), (RenameRefactoring) request);
                    }
                    else if (request instanceof MoveRefactoring) {
                        try {
                             newLocation = SharedUtils.calculateNewLocationString(model, (MoveRefactoring) request);
                        }
                        catch (Exception e) {
                            // do nothing, let the old wsdl location remain
                        }
                    }
                    _import.setLocation(newLocation);
                }
                // # 172444
                SchemaModelReference reference = element.getLookup().lookup(SchemaModelReference.class);
//System.out.println("      schema ref: " + reference);
                if (reference != null) {
                    String newLocation = reference.getSchemaLocation();
                    
                    if (request instanceof MoveRefactoring) {
                        try {
                            newLocation = getRelativePath(reference.resolveReferencedModel(), (MoveRefactoring) request);
//System.out.println("    new location: " + newLocation);
                        }
                        catch (Exception e) {
                            // do nothing, let the old schema location remain
                        }
                    }
//System.out.println("    new location: " + newLocation);
                    // temporary commented till # 172444 is fixed
                    // TODO reference.setSchemaLocation(newLocation);
                }
            }
//System.out.println();
//System.out.println();
        }
        finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
//System.out.println(" DONE");
    }

    private String getRelativePath(SchemaModel model, MoveRefactoring request) throws IOException, URISyntaxException, CatalogModelException {
        URI sourceURI = ((MoveRefactoring) request).getTarget().lookup(URL.class).toURI();
//System.out.println("    source: " + sourceURI);

        FileObject targetFile = model.getModelSource().getLookup().lookup(FileObject.class);
        URI targetURI = FileUtil.toFile(targetFile.getParent()).toURI();
//System.out.println("    target: " + targetURI);

        return SharedUtils.getRelativePath(sourceURI.toString(), targetURI.toString()) + "/" + targetFile.getNameExt();
    }
}
