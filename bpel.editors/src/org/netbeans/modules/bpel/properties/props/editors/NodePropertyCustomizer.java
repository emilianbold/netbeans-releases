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
package org.netbeans.modules.bpel.properties.props.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.bpel.properties.props.PropertyVetoError;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;

/**
 * This panel wraps any CustomNodeEditor and show it in the dialog 
 * which is automatically created by the PropertySheet when a user call 
 * the customizer with the help of the [...] button.
 * <p>
 * The panel is responsible for providing the standard life cycle 
 * which is required for any CustomNodeEditor. 
 * <p>
 * ATTENTION! This class contains some workaroud tricks. 
 * See the using of the addAncestorListener method below. 
 *
 * @author nk160297
 */
public class NodePropertyCustomizer extends JPanel
        implements PropertyChangeListener, HelpCtx.Provider {
    
    private PropertyEnv myPropertyEnv;
    private BpelNode myParentNode;
    private CustomNodeEditor myEditor;
    private boolean subscribed = true;
    private Dialog myDialog;
    
    public NodePropertyCustomizer(PropertyEnv propertyEnv) {
        super();
        init(propertyEnv);
        setLayout(new BorderLayout());
        //
        BpelNode parentNode = getParentBpelNode();
        assert parentNode != null : "Impossible to retrieve the node"; // NOI18N
        //
        Component nodeCustomizer = parentNode.getCustomizer();
        assert nodeCustomizer instanceof CustomNodeEditor;
        myEditor = (CustomNodeEditor)nodeCustomizer;
        this.add(nodeCustomizer, BorderLayout.CENTER);
        //
        ValidStateManager validationManager = myEditor.getValidStateManager(true);
        validationManager.addValidStateListener(new ValidStateListener() {
            public void stateChanged(ValidStateManager source, boolean isValid) {
                if (myPropertyEnv != null) {
                    if (isValid) {
                        myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                    } else {
                        myPropertyEnv.setState(PropertyEnv.STATE_INVALID);
                    }
                }
            }
        });
        //
        // Here is the dangerous approach is used to find the dialog which owns 
        // by the NodePropertyCustomizer panel. 
        // Then the new window listener is attached to the dialog to catch 
        // the event when dialog will be closed.  
        // It is necessary to provide correct closing processing for the 
        // enclosed CustomNodeEditor.
        this.addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                if (myDialog == null) {
                    Container cont = event.getAncestor();
                    myDialog = (Dialog)SwingUtilities.getAncestorOfClass(
                            Dialog.class, NodePropertyCustomizer.this);
                    if (myDialog != null) {
                        myDialog.addWindowListener(new WindowAdapter() {
                            public void windowClosed(WindowEvent e) {
                                processWindowClose();
                            }
                            public void windowClosing(WindowEvent e) {
                                processWindowClose();
                            }
                        });
                    }
                }
            }
            public void ancestorMoved(AncestorEvent event) {
            }
            public void ancestorRemoved(AncestorEvent event) {
            }
        });
    }
    
    public BpelNode getParentBpelNode() {
        if (myParentNode == null) {
            Object[] beans = myPropertyEnv.getBeans();
            if (beans != null && beans.length != 0) {
                Object bean = beans[0];
                if (bean != null && bean instanceof BpelNode) {
                    myParentNode = (BpelNode)bean;
                }
            }
        }
        return myParentNode;
    }
    
    public synchronized void init(PropertyEnv propertyEnv) {
        assert propertyEnv != null : "Wrong params"; // NOI18N
        //
        if (myPropertyEnv == propertyEnv) {
            return; // Prevent repeated initialization
        }
        //
        if (myPropertyEnv != null) {
            myPropertyEnv.removePropertyChangeListener(this);
        }
        //
        myPropertyEnv = propertyEnv;
        //
        myPropertyEnv.addPropertyChangeListener(this);
        //
        // The Ok button will not work without the following line!!!
        myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if (PropertyEnv.PROP_STATE.equals(event.getPropertyName()) &&
                event.getNewValue() == PropertyEnv.STATE_VALID) {
            //
            // Ok button is processed here!
            boolean success;
            try {
                success = processOkButton();
            } catch (PropertyVetoError ex) {
                success = false;
                PropertyVetoError.defaultProcessing(ex);
            }
            //
            if (!success) {
                myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            }
        }
    }
    
    private boolean processOkButton() {
        boolean success = false;
        try {
            Object modelEntity = myEditor.getEditedObject();
            if (modelEntity instanceof BpelEntity) {
                //
                // Save changes to the BPEL model
                BpelModel model = ((BpelEntity)modelEntity).getBpelModel();
                success = model.invoke(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return processOkButtonImpl(myEditor);
                    }
                }, this);
            } else if (modelEntity instanceof WSDLComponent){
                //
                // Save changes to the WSDL model
                WSDLModel model = ((WSDLComponent)modelEntity).getModel();
                model.startTransaction();
                try {
                    success = processOkButtonImpl(myEditor);
                } finally {
                    model.endTransaction();
                }
            } else {
                success = processOkButtonImpl(myEditor);
            }
        } catch (java.lang.Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return success;
    }
    
    private boolean processOkButtonImpl(CustomNodeEditor editor) {
        boolean success = false;
        //
        // Stop listening events
        editor.unsubscribeListeners();
        subscribed = false;
        try {
            success = editor.doValidateAndSave();
        } catch (java.lang.Exception ex) {
            success = false;
            ErrorManager.getDefault().notify(ex);
        } finally {
            if (!success){
                // Start listening events again
                editor.subscribeListeners();
                subscribed = true;
            }
        }
        return success;
    }

    public void processWindowClose() {
        if (subscribed) {
            myEditor.unsubscribeListeners();
            subscribed = false;
        }
        myEditor.afterClose();
    }
    
    public HelpCtx getHelpCtx() {
        return myEditor.getHelpCtx();
    }
    
}
