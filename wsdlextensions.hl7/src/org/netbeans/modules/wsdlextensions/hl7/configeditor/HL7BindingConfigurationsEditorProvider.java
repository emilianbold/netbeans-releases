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

package org.netbeans.modules.wsdlextensions.hl7.configeditor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.hl7.HL7QName;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 * Provider implementation for HL7 extensibility elements.
 *
 * @author Vishnuvardhan P.R
 * @see HL7BindingConfigurationsEditor
 */

public class HL7BindingConfigurationsEditorProvider 
        extends ExtensibilityElementConfigurationEditorProvider{

    // Editor cache
    private final Map<WSDLModel, HL7BindingConfigurationsEditorComponent> mModelEditors =
            new HashMap<WSDLModel, HL7BindingConfigurationsEditorComponent>();

    // Record of latest time every model was accessed. Used to
    // remove cached editors that have not been used after a while.
    private final Map<WSDLModel, Date> mModelLastAccessTimes =
            new HashMap<WSDLModel, Date>();

    // Cached editors not accessed for this much time or longer, are removed
    // from the cache during each pruning pass.
    private final long PRUNE_AGE = 1800000; // milliseconds; 30 minutes
    
    private String mLinkDirection = null;
    private WSDLComponent wsdlComponent;
    
    private HL7BindingConfigurationsEditorComponent editor = null;

    /**
     * Return the namespace for which this plugin provides components.
     *
     * @return String namespace corresponding to BC's schema file.
     */
    public String getNamespace() {
        return HL7QName.HL7_NS_URI;
    }

    /**
     * Provides component at current context using qname and/or wsdlcomponent.
     * Return an appropriate EditorComponent corresponding to the qname and/or
     * wsdlcomponent.
     *
     * @param qname QName of the element in the wsdl
     * @param component WSDLComponent in the wsdl.
     *
     * @return An ExtensibilityElementConfigurationEditorComponent populated
     *         with the information contained in the component's WSDL model.
     */
    public ExtensibilityElementConfigurationEditorComponent getComponent(QName qname, WSDLComponent component) {
        if (component == null) {
            throw new NullPointerException("component");
        }
        this.editor = findEditor(qname, component);

        // Important Thing To Remember:
        // We cannot assume this editor is the only entity working on the model.
        //
        // The model may have been modified externally between calls to
        // this method; e.g., user modifies the model using this editor, then
        // edits it manually (in source view), then brings the model up in this
        // editor again.
        //
        // The model must be reparsed EVERY TIME. Even if we're returning
        // a cached editor.
        this.editor.reparse(component);

        return this.editor;
    }
    
    /**
     * If configuration is supported on the extensibility element, return true,
     * else false.
     *
     * @param qname qname of the extensibility element
     *
     * @return boolean
     */
    @Override
    public boolean isConfigurationSupported(QName qname) {
        boolean isSupported;
        if (qname == null || !HL7QName.HL7_NS_URI.equals(qname.getNamespaceURI())) {
            isSupported = super.isConfigurationSupported(qname);
        } else {
            isSupported = HL7QName.ADDRESS.getQName().equals(qname);
            isSupported |= HL7QName.BINDING.getQName().equals(qname);
            isSupported |= HL7QName.OPERATION.getQName().equals(qname);
            isSupported |= HL7QName.POTOCOLPROPERTIES.getQName().equals(qname);
            isSupported |= HL7QName.MESSAGE.getQName().equals(qname);
        }
        return isSupported;
    }
    
    
        /**
     * Find (or create) an editor component for this supplied WSDL component.
     * This method is more complicated than I would like, because it caches
     * editors, and prunes its cache at every opportunity for old editors.
     * Editors are cached because calls to {@link #getComponent} can happen for
     * ANY WSDLComponent in the SAME model.  Creating a new editor component for
     * the entire model EACH TIME a node in that model is visited is crazy
     * expensive. The caching will alleviate some allocation stress, but since
     * every model must be reparsed anyway no matter what, it does not mitigate
     * the computational cost.
     *
     * @param qname Name of component
     * @param component The component itself
     *
     * @return An ExtensibilityElementConfigurationEditor.
     */
    private HL7BindingConfigurationsEditorComponent findEditor(QName qname,
                                                     WSDLComponent component) {
        assert component != null;
        assert component.getModel() != null;

        WSDLModel model;
        HL7BindingConfigurationsEditorComponent editor;

        synchronized (mModelEditors) {
            // Find/create an editor.
            model = component.getModel();
            editor = mModelEditors.get(model);
            if (editor == null) {
                // No cache hit - create a new one
                editor = new HL7BindingConfigurationsEditorComponent(model,this.mLinkDirection);
            }

            final Date AGO = new Date();
            Iterator it = mModelLastAccessTimes.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<WSDLModel, Date> timeMap = (Map.Entry)it.next();
                Date date = timeMap.getValue();
                if (date.before(AGO)) {
                    mModelEditors.remove(timeMap.getKey());
                    it.remove();
                }
            }

            // Update cache
            mModelEditors.put(model, editor);
            mModelLastAccessTimes.put(model, new Date());
        }

        return editor;
    }

        /**
     * The next two methods are interlinked. It allows for binding configuration per operation.
     * This is the entry point from casa editor, the skeleton template is loaded from the template.xml at this point.
     * It is recommended that the port and the link direction is cached and reused when the getComponent(Operation operation) is called.
     * 
     * @param component
     * @param linkDirection
     * @return
     */
    public void initOperationBasedEditingSupport(WSDLComponent component, String linkDirection) {
        wsdlComponent = component;
        mLinkDirection = linkDirection;
        
    }

    /**
     * Return the component for the operation. This can be called multiple times, so it is recommended to cache it.
     * 
     * @param operation
     * @return
     */
    public ExtensibilityElementConfigurationEditorComponent getOperationBasedEditorComponent(Operation operation) {
        return getComponent(null, wsdlComponent);
    }


    /**
     * Called when OK is pressed in the dialog, commit all the panels related to each operation in the operation list.
     * 
     * @param operationList
     * @return true if successfully committed
     */
    public boolean commitOperationBasedEditor(ArrayList<Operation> operationList) {
        return this.editor.commit();
                
    }
    
    /**
     * Called when dialog is cancelled/closed, rollback all the panels related to each operation in the operation list.
     * Can be used to cleanup.
     * @param operationList
     */
    public void rollbackOperationBasedEditor(ArrayList<Operation> operationList) {
        this.editor.rollback();
    }

}
