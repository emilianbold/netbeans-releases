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
package org.netbeans.modules.xslt.tmap.ui.editors;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xslt.tmap.model.api.PortTypeReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.nodes.TMapComponentNode;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyVetoError;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;

/**
 * This panel wraps any PortTypePropertyCustomizer and show it in the dialog 
 * which is automatically created by the PropertySheet when a user call 
 * the customizer with the help of the [...] button.
 * <p>
 * The panel is responsible for providing the standard life cycle 
 * which is required for any CustomNodeEditor. 
 * <p>
 * ATTENTION! This class contains some workaroud tricks. 
 * See the using of the addAncestorListener method below. 
 *
 * @author Vitaly Bychkov
 */
public class PortTypePropertyCustomizer extends JPanel
        implements PropertyChangeListener, HelpCtx.Provider {
    private static final long serialVersionUID = -3872150612193928109L;
    
    private PropertyEnv myPropertyEnv;
    private TMapComponentNode myParentNode;
    private ServiceParamChooser myEditor;
    private boolean subscribed = true;
    private Dialog myDialog;
    private static final Logger LOGGER = Logger.getLogger(PortTypePropertyCustomizer.class.getName());
    
    
    public PortTypePropertyCustomizer(PropertyEnv propertyEnv) {
        super();
        init(propertyEnv);
        setLayout(new BorderLayout());
        //
        TMapComponentNode parentNode = getParentNode();
        assert parentNode != null : "Impossible to retrieve the node"; // NOI18N
        //
        
        TMapComponent ref = parentNode.getComponentRef();
        ServiceParamChooser nodeCustomizer = new ServiceParamChooser(
                ref == null ? null : ref.getModel(), PortType.class);
        if (ref instanceof PortTypeReference) {
            WSDLReference<PortType> ptRef = ((PortTypeReference)ref).getPortType();
            PortType pt = ptRef == null ? null : ptRef.get();
            if (pt != null) {
                nodeCustomizer.setSelectedValue(pt);
            }
        }
        myEditor = nodeCustomizer;
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
                            Dialog.class, PortTypePropertyCustomizer.this);
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
    
    public TMapComponentNode getParentNode() {
        if (myParentNode == null) {
            Object[] beans = myPropertyEnv.getBeans();
            if (beans != null && beans.length != 0) {
                Object bean = beans[0];
                if (bean != null && bean instanceof TMapComponentNode) {
                    myParentNode = (TMapComponentNode)bean;
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
            Object modelEntity = myParentNode.getComponentRef();
            if (modelEntity instanceof PortTypeReference) {
                //
                // Save changes to the TMap model
                TMapModel model = ((TMapComponent)modelEntity).getModel();
                model.startTransaction();
                try {
                    success = processOkButtonImpl(myEditor, (PortTypeReference)modelEntity);
                } finally {
                    model.endTransaction();
                }
            }
        } catch (java.lang.Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return success;
    }
    
    private boolean processOkButtonImpl(ServiceParamChooser editor, PortTypeReference component) {
        boolean success = false;
        //
        // Stop listening events
        editor.unsubscribeListeners();
        subscribed = false;
        try {
            WSDLComponent selComponent = editor.getSelectedValue();
            if (component instanceof PortTypeReference && selComponent instanceof PortType) {
                component.setPortType(component.createWSDLReference((PortType)selComponent, PortType.class));
                success = true;
            }
            
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
        return HelpCtx.DEFAULT_HELP;
    }
    
}
