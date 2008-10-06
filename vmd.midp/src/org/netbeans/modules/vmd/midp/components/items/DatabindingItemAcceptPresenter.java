/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.components.items;

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.DatabindingCategoryCD;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetAbstractCD;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetConnectorCD;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;

/**
 *
 * @author Karol Harezlak
 */
public abstract class DatabindingItemAcceptPresenter extends AcceptPresenter {

    private final TypeID typeID;

    public DatabindingItemAcceptPresenter() {
        super(AcceptPresenter.Kind.COMPONENT_PRODUCER);
        typeID = DataSetAbstractCD.TYPEID;
    }

    @Override
    public final boolean isAcceptable(ComponentProducer producer, AcceptSuggestion suggestion) {
        DescriptorRegistry registry = getComponent().getDocument().getDescriptorRegistry();
        TypeID producerTypeID = producer.getMainComponentTypeID ();
        return registry.isInHierarchy(typeID, producerTypeID) ;
    }

    @Override
    public final ComponentProducer.Result accept (ComponentProducer producer, AcceptSuggestion suggestion) {
        ComponentProducer.Result result = producer.createComponent (getComponent ().getDocument ());
        DesignComponent component = result.getMainComponent();
        if (component != null)
            notifyCreated(component);
        DesignComponent category =  MidpDocumentSupport.getCategoryComponent(component.getDocument(), DatabindingCategoryCD.TYPEID);
        category.addComponent(component);
        return result;
    }
    
    protected abstract void notifyCreated(DesignComponent component);

    public static Presenter create(final String... propertyNames) {

        return new DatabindingItemAcceptPresenter() {

            protected void notifyCreated(DesignComponent component) {
                for (String propertyName : propertyNames) {
                    MidpDatabindingSupport.removeUnusedConnector(component, propertyName);
                    createConnector(component, propertyName);
                }
            }
        };
    }

    protected void createConnector(DesignComponent dataSet, String propertyName) {
        DesignComponent connector = dataSet.getDocument().createComponent(DataSetConnectorCD.TYPEID);
        connector.writeProperty(DataSetConnectorCD.PROP_BINDED_PROPERTY, MidpTypes.createStringValue(propertyName));
        connector.writeProperty(DataSetConnectorCD.PROP_COMPONENT_ID, MidpTypes.createLongValue(getComponent().getComponentID()));
        dataSet.addComponent(connector);
        getComponent().resetToDefault(propertyName);
    }
}
