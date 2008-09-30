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

/*
 * Created on Jun 14, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.actions;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.AttributePanel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CommonAddExtensibilityAttributeAction extends CommonNodeAction {

    /**
     *
     */
    private static final long serialVersionUID = 2110730939475660217L;
    private static final ImageIcon ICON  = new ImageIcon
    (ImageUtilities.loadImage
     ("org/netbeans/modules/xml/wsdl/ui/view/resources/message.png"));

    public CommonAddExtensibilityAttributeAction() {
        this.setIcon(ICON);
        this.putValue(Action.SHORT_DESCRIPTION, this.getName());
    }
    

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[] {Import.class, OperationParameter.class, Part.class, PortType.class, ExtensibilityElement.class};
    }

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if(activatedNodes.length != 0) {
            Node node = activatedNodes[0];
            WSDLComponent wsdlComponent = node.getLookup().lookup(WSDLComponent.class);
            if(wsdlComponent != null) {
                Vector namespaces = getNamespaces(wsdlComponent);

                final AttributePanel panel = new AttributePanel(isNamespaceRequired(), namespaces, wsdlComponent);
                final DialogDescriptor dd = new DialogDescriptor(panel,
                        NbBundle.getMessage(CommonAddExtensibilityAttributeAction.class, "CommonAddExtensibilityAttributeAction_TITLE"));
                panel.addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals(AttributePanel.STATE_CHANGED)) {
                            dd.setValid(panel.isStateValid());
                        }

                    }

                });


                Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                dialog.getAccessibleContext().setAccessibleDescription(dd.getTitle());
                dd.setValid(false);

                dialog.setVisible(true);
                dialog.toFront();

                boolean cancelled = dd.getValue() != DialogDescriptor.OK_OPTION;
                if (!cancelled) {
                    String name = panel.getAttributeName();
                    String namespace = panel.getNamespace();

                    QName attrQName = new QName(namespace, name);
                    WSDLComponent element = wsdlComponent;
                    element.getModel().startTransaction();

                    if(Utility.getNamespacePrefix(namespace, wsdlComponent) == null) {
                            String prefixName = NameGenerator.getInstance().generateNamespacePrefix(null, wsdlComponent);
                            ((AbstractDocumentComponent) element).addPrefix(prefixName, namespace);
                    }

                    ((AbstractDocumentComponent) element).setAnyAttribute(attrQName, "");
                        element.getModel().endTransaction();
                }
/*                AttributeView attrView = new AttributeView(node, wsdlComponent);
                attrView.setNamespaceRequired(isNamespaceRequired());
                //Vector namespaces = getNamespaces(wsdlComponent);
                attrView.setNamespaces(namespaces);

                GenericDialog gd = new GenericDialog(attrView,
                        NbBundle.getMessage(CommonAddExtensibiltyElementAction.class, "CommonAddExtensibilityAttributeAction_TITLE"),
                        true);

                gd.enableOkButton(false);

                int dialogWidth = 400;
                int dialogHeight = 250;

                gd.getDialog().setSize(new Dimension(dialogWidth, dialogHeight));
                int windowWidth = WindowManager.getDefault().getMainWindow().getWidth();
                int windowHeight = WindowManager.getDefault().getMainWindow().getHeight();

                int dialogX = (windowWidth - dialogWidth) /2;
                int dialogY = (windowHeight - dialogHeight) /2;

                gd.getDialog().setLocation(dialogX, dialogY);
                gd.getDialog().setVisible(true);

                if(gd.getButtonState() == GenericDialog.OK_BUTTON) {*/
/*                    String name = attrView.getNewAttributeName();
                                        String namespace = attrView.getNewNamespace();

                                        QName attrQName = new QName(namespace, name);
                                        WSDLComponent element = cookie.getWSDLComponent();
                                        element.getModel().startTransaction();

                                        if(Utility.getNamespacePrefix(namespace, wsdlComponent) == null) {
                                                String prefixName = NameGenerator.getInstance().generateNamespacePrefix(null, wsdlComponent);
                                                ((AbstractDocumentComponent) element).addPrefix(prefixName, namespace);
                                        }

                                        ((AbstractDocumentComponent) element).setAnyAttribute(attrQName, "");
                                        try {
                                            element.getModel().endTransaction();
                                        } catch (IOException e) {
                                            ErrorManager.getDefault().notify(e);
                                        }*/

                //}
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CommonAddExtensibilityAttributeAction.class, "CommonAddExtensibilityAttributeAction_DISPLAY_NAME");
    }

    protected boolean isNamespaceRequired() {
        return true;
    }

    protected Vector<String> getNamespaces(WSDLComponent wsdlComponent) {
        Map<String, String> prefixToNameSpaceMap = Utility.getPrefixes(wsdlComponent);
        Set<String> namespaceSet = new HashSet<String>(prefixToNameSpaceMap.values());
        namespaceSet.remove(Constants.WSDL_DEFAUL_NAMESPACE);

        return new Vector<String>(namespaceSet);
    }
}



