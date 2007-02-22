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
 * Created on Jun 24, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.actions;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.cookies.WSDLAttributeCookie;
import org.netbeans.modules.xml.wsdl.ui.cookies.WSDLOtherAttributeCookie;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl.ExtensibilityUtils;
import org.netbeans.modules.xml.wsdl.ui.model.StringAttribute;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.RemoveAttributesDialog;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RemoveAttributesAction extends CommonNodeAction {

    /**
     *
     */
    private static final long serialVersionUID = -2981770394497321880L;
/*    private static final ImageIcon ICON  = new ImageIcon
    (Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/remove.png"));*/

    public RemoveAttributesAction() {
  //      this.setIcon(ICON);
        this.putValue(Action.SHORT_DESCRIPTION, this.getName());
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {WSDLAttributeCookie.class, WSDLOtherAttributeCookie.class};
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ANY;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if(activatedNodes.length != 0) {
            for(int i = 0; i < activatedNodes.length; i++) {
                final Node node = activatedNodes[i];
                WSDLAttributeCookie cookie = (WSDLAttributeCookie) node.getCookie(WSDLAttributeCookie.class);
                WSDLOtherAttributeCookie woaCookie = (WSDLOtherAttributeCookie) node.getCookie(WSDLOtherAttributeCookie.class);
                final List<QName> list = new ArrayList<QName>();
                final WSDLComponent comp = (cookie != null) ? cookie.getWSDLComponent() : ((woaCookie != null) ? woaCookie.getWSDLComponent() : null);
                if(cookie != null) {
                    list.addAll(Utility.getExtensionAttributes(comp));
                }
                if (woaCookie != null) {
                    Element elem = ExtensibilityUtils.getElement((ExtensibilityElement) comp);
                    list.addAll(Utility.getOptionalAttributes(comp, elem));
                }
                if (comp!= null && list.size() > 0) {
                    Runnable run = new Runnable() {
                        public void run() {
                            RemoveAttributesDialog dialog = new RemoveAttributesDialog(WindowManager.getDefault().getMainWindow(), true);
                            dialog.setTitle(NbBundle.getMessage(getClass(), "RemoveAttributesAction_DISPLAY_NAME"));

                            Dimension dimension = dialog.getSize();
                            int dialogWidth = dimension.width;
                            int dialogHeight = dimension.height;
                            int windowWidth = WindowManager.getDefault().getMainWindow().getWidth();
                            int windowHeight = WindowManager.getDefault().getMainWindow().getHeight();

                            int dialogX = (windowWidth - dialogWidth) / 2;
                            int dialogY = (windowHeight - dialogHeight) / 2;

                            dialog.setLocation(dialogX, dialogY);

                            dialog.setAttributes(list);
                            dialog.setVisible(true);

                            Object[] attributes = dialog.getAttributes();
                            if (attributes != null) {
                                for (Object element : attributes) {
                                    String attrName = (String) element;
                                    comp.getModel().startTransaction();
                                    comp.setAttribute(attrName, new StringAttribute(attrName), null);
                                        comp.getModel().endTransaction();
                                }
                            }
                        }
                    };
                    SwingUtilities.invokeLater(run);
                }
            }
        }


    }


    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RemoveAttributesAction.class, "RemoveAttributesAction_DISPLAY_NAME");
    }
}


