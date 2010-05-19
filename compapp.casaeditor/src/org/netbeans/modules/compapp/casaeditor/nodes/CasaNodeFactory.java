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
package org.netbeans.modules.compapp.casaeditor.nodes;

import java.util.List;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.openide.util.lookup.InstanceContent;


public class CasaNodeFactory {
    
    private CasaDataObject mDataObject;
    private CasaWrapperModel mModel;
    
    
    public CasaNodeFactory(CasaDataObject dataObject, CasaWrapperModel model) {
        mDataObject = dataObject;
        mModel = model;
    }
    
    
    public CasaWrapperModel getCasaModel() {
        return mModel;
    }
    
    public InstanceContent createInstanceContent() {
        InstanceContent content = new InstanceContent();
        content.add(mDataObject);
        content.add(mModel);
        return content;
    }
    
    public CasaNode createModelNode(CasaWrapperModel model) {
        assert model != null;
        return new CasaRootNode(model, this);
    }
    
    public CasaNode createNode_connectionList(List<CasaConnection> data) {
        return new ConnectionsNode(data, this);
    }
    
    public CasaNode createNode_consumesList(List<CasaConsumes> data) {
        return new ConsumesListNode(data, this);
    }
    
    public CasaNode createNode_providesList(List<CasaProvides> data) {
        return new ProvidesListNode(data, this);
    }
    
    public CasaNode createNode_process(CasaEndpoint data) {
        return new ServiceUnitProcessNode(data, this);
    }
    
    public CasaNode createNode_suList(List<CasaServiceEngineServiceUnit> data) {
        return new ServiceEnginesNode(data, this);
    }
    
    public CasaNode createNode_portList(List<CasaPort> data) {
        return new WSDLEndpointsNode(data, this);
    }
    
    public CasaNode createNodeFor(CasaComponent component) {
        CasaNodeCreationVisitor visitor = new CasaNodeCreationVisitor(this);
        component.accept(visitor);
        return visitor.getNode();
    }
    

    /**
     * Checks if the classes from source array are assignable to the
     * corresponding classes from target array.
     * Both arrays has to have the same quantity of elements.
     *
    private boolean isAssignable(Class<?>[] source, Class<?>[] target) {
        if (source == null || target == null || source.length != target.length) {
            return false;
        }
        for (int index = 0; index < source.length; index++) {
            if (!target[index].isAssignableFrom(source[index])) {
                return false;
            }
        }
        return true;
    }
    */
}
