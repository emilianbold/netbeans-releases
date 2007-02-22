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

package org.netbeans.modules.xml.wsdl.ui.view.property;

import java.awt.Component;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.logging.Logger;

import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.OperationNode;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author radval
 *
 */
public class ParameterOrderPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor, PropertyChangeListener {

    /** The logger */
    private static final Logger logger = Logger.getLogger(ImportLocationPropertyEditor.class.getName());

    /** property name used by propertyChangeEvent */
    protected static final String PROP_NAME = "ParameterOrder";//NOI18N

    /** Environment passed to the ExPropertyEditor*/
    private PropertyEnv env;
    
    private OperationNode mOperationNode;
    
    private Operation mOperation;
    
    public ParameterOrderPropertyEditor(OperationNode operationNode) {
        this.mOperationNode = operationNode;
        this.mOperation = (Operation) this.mOperationNode.getWSDLConstruct();
    }
    
    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv ev) {
        this.env = ev;
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this is not editable  
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }
    
    @Override
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        //do not allow to set text. implace editor should be non editable
        super.setAsText(text);
    }
    
    
    /** @return tags */
    @Override
    public String[] getTags() {
        return null;
    }
    
    /** @return true */
    @Override
    public boolean supportsCustomEditor () {
        return XAMUtils.isWritable(mOperation.getModel());
    }
    
    /** @return editor component */
    @Override
    public Component getCustomEditor () {
        ParameterOrderPropertyPanel editor = new ParameterOrderPropertyPanel(mOperation, env);/*new ParameterOrderPropertyPanel(this.mOperationNode,
                                                                             this.mOperation, 
                                                                             this.env);*/
        editor.addPropertyChangeListener(PROP_NAME, this);
        return editor;
    }
    /** handles property change
     *  
     * @param evt propertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent evt) {
        setValue(evt.getNewValue());
    }
}

