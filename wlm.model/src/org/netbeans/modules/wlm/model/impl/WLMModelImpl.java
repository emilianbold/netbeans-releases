/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wlm.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMComponentFactory;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class WLMModelImpl extends WLMModel {

    private TTask task;
    private WLMComponentFactory wcf;

    public WLMModelImpl(ModelSource source) {
        super(source);
        wcf = new WLMComponentFactoryImpl(this);
    }

    @Override
    public WLMComponent createRootComponent(Element root) {
        // TODO Auto-generated method stub
        String namespace = root.getNamespaceURI();
        if (WLMModel.WLM_NAMESPACE.equals(namespace) &&
                WLMModel.WLM_TASK.equals(root.getLocalName())) {
            task = new TaskImpl(this, root);
            return task;
        }
        return null;
    }

    @Override
    protected ComponentUpdater<WLMComponent> getComponentUpdater() {
        // TODO Auto-generated method stub
        return new ChildComponentUpdateVisitor<WLMComponent>();
    }

    public List<WSDLModel> findWSDLModel(String namespaceURI) {
        // TODO Auto-generated method stub
        if (namespaceURI == null) {
            return Collections.emptyList();
        }

        List<WSDLModel> models = getImportedWSDLModels();

        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        for (WSDLModel m : models) {
            String targetNamespace = m.getDefinitions().getTargetNamespace();
            if (namespaceURI.equals(targetNamespace)) {
                ret.add(m);
            }
        }
        return ret;
    }

    public List<WSDLModel> getImportedWSDLModels() {
        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        Collection<TImport> imports = getTask().getImports();
        for (TImport i : imports) {
            try {
                WSDLModel m = i.getImportedWSDLModel();
                if (m != null) {
                    ret.add(m);
                }
            } catch (Exception e) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "getImportedWSDLModels", e);
            }
        }
        return ret;
    }

    public WLMComponentFactory getFactory() {
        // TODO Auto-generated method stub
        return wcf;
    }

    public WLMComponent createComponent(WLMComponent parent, Element element) {
        // TODO Auto-generated method stub
        return parent.createChild(element);
    }

    public WLMComponent getRootComponent() {
        // TODO Auto-generated method stub
        return task;
    }

    @Override
    public TTask getTask() {
        return task;
    }

    @Override
    public Set<QName> getQNames() {
        return WLMQNames.getQNames();
    }

    @Override
    public void setTask(TTask task) {
        this.task = task;
    }

    @Override
    public ChangeInfo prepareChangeInfo(List<Node> pathToRoot) {
        ChangeInfo change = super.prepareChangeInfo(pathToRoot);
        DocumentComponent parentComponent = findComponent(change.getRootToParentPath());
        if (parentComponent == null) {
            return change;
        }
        change.setParentComponent(parentComponent);
        return change;
    }
}
