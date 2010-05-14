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

package org.netbeans.modules.xslt.tmap.ui.importchooser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCreator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xslt.tmap.model.api.Import;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Vitaly Bychkov
 * @author nk160297
 */
public abstract class ImportCreator extends ExternalReferenceCreator<TransformMap> {
    private ExternalReferenceDecorator decorator;
    
    /**
     * Creates a new instance of ImportCustomizer
     *
     * @param  defs  component to contain the import(s).
     */
    public ImportCreator(TransformMap transformMap) {
        super(transformMap, null);
    }
    
    @Override
    public void applyChanges() throws IOException {
        boolean setTransaction = false;
        TMapModel model = null;
        try {
            super.applyChanges();
            TransformMap transformMap = getModelComponent();
            model = transformMap.getModel();
            if (model != null && !model.isIntransaction()) {
                model.startTransaction();
                setTransaction = true;
            }
            List<Node> nodes = getSelectedNodes();
            for (Node node : nodes) {
                Import imp = model.getFactory().createImport();
                //
                String namespace = getNamespace(node);
                if (mustNamespaceDiffer()) {
                    imp.setNamespace(namespace);
                }
                imp.setLocation(getLocation(node));
                //
                // Save the prefix.
                ExNamespaceContext nsCont = transformMap.getNamespaceContext();
                if (nsCont != null) {
                    String oldPrefix = nsCont.getPrefix(namespace);
                    String newPrefix = null;
                    if (node instanceof ExternalReferenceDataNode) {
                        newPrefix = ((ExternalReferenceDataNode) node).getPrefix();
                    }
                    //
                    if (oldPrefix == null || !oldPrefix.equals(newPrefix)) {
                        if (newPrefix == null || newPrefix.length() == 0) {
                            nsCont.addNamespace(namespace);
                        } else {
                            nsCont.addNamespace(newPrefix, namespace);
                        }
                    }
                }
                //
                transformMap.addImport(imp);
            }
        } catch (Exception ex) {
            // provide user with any problem appeared during this dialog.
            ErrorManager.getDefault().notify(ex);
            IOException ioEx = new IOException();
            ioEx.initCause(ex);
            throw ioEx;
        } finally {
            if (setTransaction && model != null) {
                model.endTransaction();
            }
        }
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // return new HelpCtx(ImportWSDLCreator.class);
    }
    
    protected String getTargetNamespace(Model model) {
        return Util.getTargetNamespace(model);
    }
    
    protected Map<String, String> getPrefixes(Model model) {
        if (model != null) {
            assert model instanceof TMapModel;
            TMapModel tMapModel = (TMapModel)model;
            return ((AbstractDocumentComponent)tMapModel.getTransformMap()).getPrefixes();
        }
        return new HashMap<String, String>();
    }
    
    public abstract Utilities.DocumentTypesEnum getImportType();
    
    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new ReferenceDecorator(getModelComponent(), this, 
                    getImportType());
        }
        return decorator;
    }
        
    public boolean mustNamespaceDiffer() {
        return true;
    }
    
    protected String referenceTypeName() {
          return NbBundle.getMessage(ImportCreator.class,
              "LBL_WSDLFileImportDialog_Type");
    }
    
}
