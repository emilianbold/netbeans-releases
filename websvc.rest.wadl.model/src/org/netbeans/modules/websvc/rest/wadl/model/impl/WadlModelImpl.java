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

package org.netbeans.modules.websvc.rest.wadl.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.websvc.rest.wadl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.SyncUnit;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Ayub Khan
 */
public class WadlModelImpl extends WadlModel {

    public static final String EMPTY_WADL = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"+
        "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"+
        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n"+
        "       xsi:schemaLocation=\"http://research.sun.com/wadl/2006/10 https://wadl.dev.java.net/wadl20061109.xsd\" \n"+
        "       xmlns=\"http://research.sun.com/wadl/2006/10\"> \n" +
        "   <grammars/> \n"+
        "   <resources base=\"http://api.example.com/services/\"> \n"+
        "       <resource path=\"service1\"> \n"+
        "           <method id=\"newMethod\" name=\"GET\"> \n"+
        "               <request> \n"+
        "                   <param name=\"a\" type=\"xsd:string\" style=\"query\" default=\"10\"/> \n"+
        "                   <param name=\"b\" type=\"xsd:string\" style=\"query\"/> \n"+
        "               </request> \n"+
        "               <response> \n"+
        "                   <representation mediaType=\"application/xml\"/> \n"+
        "               </response> \n"+
        "           </method> \n"+
        "       </resource> \n"+
        "   </resources> \n"+
        "</application> \n";
    
    private ApplicationImpl application;
    private WadlComponentFactory wcf;
    
    public WadlModelImpl(ModelSource source) {
        super(source);
        wcf = new WadlComponentFactoryImpl(this);
    }
    	
    public WadlComponent createRootComponent(Element root) {
        ApplicationImpl newApplication = null;
        QName q = root == null ? null : AbstractDocumentComponent.getQName(root);
        if (root != null && WadlQNames.APPLICATION.getQName().equals(q)) {
            newApplication = new ApplicationImpl(this, root);
            setApplication(newApplication);
        } else {
            return null;
        }
        
        return getApplication();
    }
    
    public WadlComponent getRootComponent() {
        return application;
    }
    
    public WadlComponent createComponent(WadlComponent parent, Element element) {
        return getFactory().create(element, parent);
    }
    
    protected ComponentUpdater<WadlComponent> getComponentUpdater() {
        return new ChildComponentUpdateVisitor<WadlComponent>();
    }
    
    public WadlComponentFactory getFactory() {
        return wcf;
    }
    
    public void setApplication(ApplicationImpl def){
        assert (def instanceof ApplicationImpl) ;
        application = ApplicationImpl.class.cast(def);
    }
    
    public ApplicationImpl getApplication(){
        if(application == null) {
            try {
                Document doc = getBaseDocument();
                if(doc != null && !doc.getText(0, doc.getLength()).contains("WADL_NS_URI")) {
                    doc.remove(0, doc.getLength());
                    getBaseDocument().insertString(0, EMPTY_WADL, null);
                    sync();
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return application;
    }
    
    ElementFactoryRegistry getElementRegistry() {
        return ElementFactoryRegistry.getDefault();
    }
    
    public List<SchemaModel> getImportedSchemaModels() {
        List<SchemaModel> ret = new ArrayList<SchemaModel>();
//        Collection<Import> imports = getApplication().getImports();
//        for (Import i:imports) {
//            try {
//                SchemaModel m = ((ImportImpl)i).resolveToSchemaModel();
//                if (m != null) {
//                    ret.add(m);
//                }
//            } catch(Exception e) {
//                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "getImportedSchemaModels", e); //NOI18N
//            }
//        }
        return ret;
    }

    public List<SchemaModel> getEmbeddedSchemaModels() {
        List<SchemaModel> ret = new ArrayList<SchemaModel>();
//        Types types = getApplication().getTypes();
//        List<WadlSchema> embeddedSchemas = Collections.emptyList();
//        if (types != null) {
//            embeddedSchemas = types.getExtensibilityElements(WadlSchema.class);
//        }
//        for (WadlSchema wschema : embeddedSchemas) {
//            ret.add(wschema.getSchemaModel());
//        }
        return ret;
    }
    
    public List<WadlModel> findWadlModel(String namespace) {
        if (namespace == null) {
            return Collections.emptyList();
        }
        
//        List<WadlModel> models = getImportedWadlModels();
        List<WadlModel> models = new ArrayList<WadlModel>();
        models.add(0, this);

        List<WadlModel> ret = new ArrayList<WadlModel>();
        for (WadlModel m : models) {
            String targetNamespace = m.getApplication().getTargetNamespace();
            if (namespace.equals(targetNamespace)) {
                ret.add(m);
            }
        }
        return ret;
    }

    public List<Schema> findSchemas(String namespace) {
        List<Schema> ret = new ArrayList<Schema>();
        for (SchemaModel sm : getEmbeddedSchemaModels()) {
            try {
                ret.addAll(sm.findSchemas(namespace));
            } catch(Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "findSchemas", ex);
            }
        }
        SchemaModel sm = findSchemaModelFromImports(namespace);
        if (sm != null) {
            ret.add(sm.getSchema());
        }
        return ret;
    }
    
    private SchemaModel findSchemaModelFromImports(String namespace) {
        if (namespace == null) {
            return null;
        }
        
        List<SchemaModel> models = getImportedSchemaModels();
        for (SchemaModel m : models) {
            String targetNamespace = m.getSchema().getTargetNamespace();
            if (namespace.equals(targetNamespace)) {
                return m;
            }
        }
        return null;
    }

    public <T extends ReferenceableWadlComponent> T findComponentByName(String name, Class<T> type) {
        return type.cast(new FindReferencedVisitor(getApplication()).find(name, type));
    }
    
    public <T extends ReferenceableWadlComponent> T findComponentByName(QName name, Class<T> type) {
        String namespace = name.getNamespaceURI();
        if (namespace == null) {
            return findComponentByName(name.getLocalPart(), type);
        } else {
            for (WadlModel targetModel : findWadlModel(namespace)) {
                T found = targetModel.findComponentByName(name.getLocalPart(), type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    public Set<QName> getQNames() {
        return getElementRegistry().getKnownQNames();
    }

    public Set<String> getElementNames() {
        return getElementRegistry().getKnownElementNames();
    }
    
    @Override
    public ChangeInfo prepareChangeInfo(List<? extends Node> pathToRoot,
            List<? extends Node> nsContextPathToRoot) {
        ChangeInfo change = super.prepareChangeInfo(pathToRoot, nsContextPathToRoot);
        DocumentComponent parentComponent = findComponent(change.getRootToParentPath());
        if (parentComponent == null) {
            return change;
        }
        if (! (parentComponent.getModel() instanceof WadlModel)) 
        {
            getElementRegistry().addEmbeddedModelQNames((AbstractDocumentModel)parentComponent.getModel());
            change = super.prepareChangeInfo(pathToRoot, nsContextPathToRoot);
        } else if (isDomainElement(parentComponent.getPeer()) && 
                ! change.isDomainElement() && change.getChangedElement() != null) 
        {
            if (change.getOtherNonDomainElementNodes() == null ||
                change.getOtherNonDomainElementNodes().isEmpty()) 
            {
                // case add or remove generic extensibility element
                change.setDomainElement(true);
                change.setParentComponent(null);
            } else if (! (parentComponent instanceof Documentation)) {
                List<Element> rootToChanged = new ArrayList<Element>(change.getRootToParentPath());
                rootToChanged.add(change.getChangedElement());
                DocumentComponent changedComponent = findComponent(rootToChanged);
                if (changedComponent != null && 
                    changedComponent.getClass().isAssignableFrom(GenericExtensibilityElement.class)) {
                    // case generic extensibility element changed
                    change.markNonDomainChildAsChanged();
                    change.setParentComponent(null);
                }
            }
        } else {
            change.setParentComponent(parentComponent);
        }
        return change;
    }
    
    public SyncUnit prepareSyncUnit(ChangeInfo changes, SyncUnit unit) {
        unit = super.prepareSyncUnit(changes, unit);
        if (unit != null) {
            return new SyncReviewVisitor().review(unit);
        }
        return null;
    }
    
    public AbstractDocumentComponent findComponent(
            AbstractDocumentComponent current,
            List<org.w3c.dom.Element> pathFromRoot, 
            int iCurrent) {
        
//        if (current instanceof ExtensibilityElement.EmbeddedModel) {
//            ExtensibilityElement.EmbeddedModel emb = (ExtensibilityElement.EmbeddedModel) current;
//            AbstractDocumentModel axm = (AbstractDocumentModel) emb.getEmbeddedModel();
//            AbstractDocumentComponent embedded = (AbstractDocumentComponent) axm.getRootComponent();
//            return axm.findComponent(embedded, pathFromRoot, iCurrent);
//        } else {
            return super.findComponent(current, pathFromRoot, iCurrent);
//        }
    }

    @Override
    public Map<QName, List<QName>> getQNameValuedAttributes() {
        return WadlAttribute.getQNameValuedAttributes();
    }
}
