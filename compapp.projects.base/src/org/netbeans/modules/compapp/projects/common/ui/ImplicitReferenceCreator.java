/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.compapp.projects.common.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.Entry;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.EntryType;
import org.netbeans.modules.compapp.projects.common.ImplicitCatalogSupport;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/**
 * This class extends the NamespaceReferenceCreator to provide the required concrete
 * implementation required to select the namespace nodes belongs to wsdl or xsd files
 * across projects to use in the implicit namespace catalog support.
 * 
 * Users can use this class to show the gui in a dialog from which they can select the
 * namespaces that they will be using in their xml documents and populate the implicit
 * namespace catalog with the selected namespaces.
 * <p><blockquote><pre>
 *   Project prj = ...// get the project reference
 *   ImplicitReferenceCreator refCreator = 
 *        ImplicitReferenceCreator.newInstance(prj);
 *   // show the selection dialog
 *   Object result = 
 *    ImplicitReferenceCreator.showSelectionDialog(refCreator, "Select Namespaces");
 *   ....
 * 
 *   if ( result == DialogDescriptor.OK_OPTION) {
 *       // populate implicit catalog support with the selected namespaces.
 *       refCreator.addSelectedNamespacesToCatalog(prj); 
 *   }
 * 
 *   // OR.  use the selected namespace to resolve correpsonding file refrences.
 *       List<NSReference> nsList = refCreator.getSelectedNamespaces();
 *
 * </blockquote></pre><p>
 * @author chikkala
 */
public class ImplicitReferenceCreator extends NamespaceReferenceCreator<Definitions> {

    private ExternalReferenceDecorator decorator;

    public ImplicitReferenceCreator(Definitions defs, Model model) {
        super(defs, model);
    }

    @Override
    protected String getTargetNamespace(Model model) {
        if (model instanceof WSDLModel) {
            return ((WSDLModel) model).getDefinitions().getTargetNamespace();
        } else if (model instanceof SchemaModel) {
            return ((SchemaModel) model).getSchema().getTargetNamespace();
        } else {
            return null;
        }
    }

    @Override
    protected Map getPrefixes(Model model) {
        WSDLModel wm = (WSDLModel) model;
        AbstractDocumentComponent def =
                (AbstractDocumentComponent) wm.getDefinitions();
        return def.getPrefixes();
    }

    @Override
    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new ImplicitReferenceDecorator(this);
        }
        return decorator;
    }

    @Override
    public boolean mustNamespaceDiffer() {
        return true;
    }

    @Override
    protected String referenceTypeName() {
        return "Select";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void applyChanges() throws IOException {
    // super.applyChanges();
    }

    public List<NSReference> getSelectedNamespaces() {
        List<NSReference> list = new ArrayList<NSReference>();
        List<Node> nodes = getSelectedNodes();
        // System.out.println("GetSelNS:getSelectedNodes:size" + nodes.size());
        for (Node node : nodes) {
            if (node instanceof ExternalReferenceNode) {
                ExternalReferenceNode ern = (ExternalReferenceNode) node;
                Model model = ern.getModel();
                // Without a model, the selection is completely invalid.
                if (model != null && model != getModelComponent().getModel()) {
                    FileObject fileObj = (FileObject) model.getModelSource().
                            getLookup().lookup(FileObject.class);
                    String namespace = ern.getNamespace();
                    String location = this.getLocation(node);
                    EntryType type = EntryType.WSDL;
                    if (model instanceof WSDLModel) {
                        type = EntryType.WSDL;
                    } else if (model instanceof SchemaModel) {
                        type = EntryType.XSD;
                    }
                    String prefix = "";
                    if (ern instanceof ExternalReferenceDataNode) {
                        prefix = ((ExternalReferenceDataNode) ern).getPrefix();
                    }
                    NSReference nsRef = new NSReference(type);
                    nsRef.setNamespace(namespace);
                    nsRef.setLocation(location);
                    nsRef.setPrefix(prefix);
                    nsRef.setFileObject(fileObj);
                    list.add(nsRef);
                }
            }
        }
        return list;
    }

    public void addSelectedNamespacesToCatalog(Project prj) {
        ImplicitCatalogSupport catSupport = ImplicitCatalogSupport.getInstance(prj);
        List<NSReference> list = getSelectedNamespaces();
        for ( NSReference nsRef : list ) {
            try {
                catSupport.createImplicitCatalogEntry(
                        nsRef.getNamesapce(), nsRef.getFileObject(), nsRef.getType());
            } catch (Exception ex) {
                ex.printStackTrace();
            }            
        }        
    }
    
    protected static WSDLModel getCatalogWSDLModel(Project project) {
        WSDLModel wsdlModel = null;
        try {
            FileObject catFO = CatalogWSDL.getCatalogWSDLFile(project);
            ModelSource catModelSource = Utilities.getModelSource(catFO, true);
            wsdlModel = WSDLModelFactory.getDefault().getModel(catModelSource);
        } catch (IOException ex) {
            ex.printStackTrace(); //TODO: add to logger.
        }
        return wsdlModel;
    }

    public static ImplicitReferenceCreator newInstance(Project prj) {
        ImplicitReferenceCreator refCreator = null;
        WSDLModel wsdlModel = getCatalogWSDLModel(prj);
        refCreator = new ImplicitReferenceCreator(wsdlModel.getDefinitions(), wsdlModel);
        return refCreator;
    }

    public static Object showSelectionDialog(final ImplicitReferenceCreator customizer, String title) {
        java.awt.Component component = customizer.getComponent();
        final DialogDescriptor descriptor = new DialogDescriptor(component,
                title, true, null);
        descriptor.setHelpCtx(customizer.getHelpCtx());

        // customizer's property change listener to enable/disable OK
        final PropertyChangeListener pcl = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getSource() == customizer && evt.getPropertyName().
                        equals(Customizer.PROP_ACTION_APPLY)) {
                    descriptor.setValid(((Boolean) evt.getNewValue()).booleanValue());
                }
            }
            };
        customizer.addPropertyChangeListener(pcl);
        // dialog's action listener
        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION) ||
                        evt.getSource().equals(DialogDescriptor.CANCEL_OPTION) ||
                        evt.getSource().equals(DialogDescriptor.CLOSED_OPTION)) {
                    customizer.removePropertyChangeListener(pcl);
                }
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        customizer.apply();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        };
        descriptor.setButtonListener(al);
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);
        return descriptor.getValue();
    }

    /**
     * This class can hold the information about the implicit namespace such as
     * its catalog.wsdl entry details and the pointer to the file representing
     * the namespace. can be used in ui models.
     */
    public static class NSReference extends Entry {

        private String mPrefix;
        private FileObject mRefFO;

        public NSReference(EntryType type) {
            super(type);
        }

        public String getPrefix() {
            return mPrefix;
        }

        public void setPrefix(String prefix) {
            this.mPrefix = prefix;
        }

        public FileObject getFileObject() {
            return mRefFO;
        }

        public void setFileObject(FileObject refFO) {
            this.mRefFO = refFO;
        }
    }
}
