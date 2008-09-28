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
/*
 * MethodNode.java
 *
 * Created on May 3, 2004, 6:37 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.logging.Level;

import javax.swing.Action;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import org.netbeans.modules.visualweb.ejb.load.EjbLoaderHelper;
import org.netbeans.modules.visualweb.ejb.util.InvalidParameterNameException;
import org.netbeans.modules.visualweb.ejb.util.MethodParamValidator;
import org.netbeans.modules.visualweb.ejb.util.Util;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;

import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.impl.BasicBeanCreateInfo;

/**
 * The node representing a business method in an EJB
 * 
 * @author cao
 */
public class MethodNode extends AbstractNode implements Node.Cookie {
    private MethodInfo methodInfo;

    private EjbGroup ejbGroup;

    public MethodNode(EjbGroup ejbGrp, MethodInfo mInfo, EjbInfo ejbInfo) {
        super(Children.LEAF);

        ejbGroup = ejbGrp;
        methodInfo = mInfo;
        setName(methodInfo.getName());
        setDisplayName(methodInfo.getName());
        setShortDescription(methodInfo.toString());
    }

    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] {
                SystemAction.get(PropertiesAction.class) };
    }

    public Action getPreferredAction() {
        // Whatever is most relevant to a user:
        return SystemAction.get(PropertiesAction.class);
    }

    public Image getIcon(int type) {
        return getMethodIcon();
    }

    public Image getOpenedIcon(int type) {
        return getMethodIcon();
    }

    private Image getMethodIcon() {
        if (!methodInfo.getReturnType().isVoid()) {
            Image image1 = ImageUtilities
                    .loadImage("org/netbeans/modules/visualweb/ejb/resources/methodPublic.gif");
            Image image2 = ImageUtilities
                    .loadImage("org/netbeans/modules/visualweb/ejb/resources/table_dp_badge.png");
            int x = image1.getWidth(null) - image2.getWidth(null);
            int y = image1.getHeight(null) - image2.getHeight(null);
            return ImageUtilities.mergeImages(image1, image2, x, y);
        } else
            return ImageUtilities
                    .loadImage("org/netbeans/modules/visualweb/ejb/resources/methodPublic.gif");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_ejb_node");
    }

    public MethodInfo getMethodInfo() {
        return this.methodInfo;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("methodInfo"); // NOI18N

        if (ss == null) {
            ss = new Set();
            ss.setName("methodInfo"); // NOI18N
            ss.setDisplayName(NbBundle.getMessage(MethodNode.class, "METHOD_INFORMATION"));
            ss.setShortDescription(NbBundle.getMessage(MethodNode.class, "METHOD_INFORMATION"));
            sheet.put(ss);
        }

        // Method Return
        Set returnSet = sheet.get("methodReturn"); // NOI18N
        if (returnSet == null) {
            returnSet = new Sheet.Set();
            returnSet.setName("methodReturn"); // NOI18N
            returnSet.setDisplayName(NbBundle.getMessage(MethodNode.class, "METHOD_RETURN"));
            returnSet.setShortDescription(NbBundle.getMessage(MethodNode.class, "METHOD_RETURN"));
            sheet.put(returnSet);
        }

        returnSet.put(new PropertySupport.ReadOnly("returnType", // NOI18N
                String.class, NbBundle.getMessage(MethodNode.class, "RETURN_TYPE"), NbBundle
                        .getMessage(MethodNode.class, "RETURN_TYPE")) {

            public Object getValue() {
                return methodInfo.getReturnType().getClassName();
            }
        });

        if (methodInfo.getReturnType().isCollection()) {
            returnSet.put(new PropertySupport.ReadWrite(
                    "elementType", // NOI18N
                    String.class, NbBundle.getMessage(MethodNode.class, "RETURN_COL_ELEM_TYPE"),
                    NbBundle.getMessage(MethodNode.class, "RETURN_COL_ELEM_TYPE")) {

                public PropertyEditor getPropertyEditor() {
                    // TODO
                    return null;

                }

                public Object getValue() {
                    String className = methodInfo.getReturnType().getElemClassName();
                    if (className == null)
                        className = NbBundle.getMessage(MethodNode.class,
                                "RETURN_COL_ELEM_TYPE_NOT_SPECIFIED");

                    return className;
                }

                public void setValue(Object val) {
                    String className = (String) val;

                    // Make sure it is not the original <not specified> or a
                    // bunch of space or nothing
                    if (className == null
                            || className.trim().length() == 0
                            || className.equals(NbBundle.getMessage(MethodNode.class,
                                    "RETURN_COL_ELEM_TYPE_NOT_SPECIFIED")))
                        className = null;

                    // Make sure that the element class specified by the user is
                    // a valid one
                    try {
                        URLClassLoader classloader = EjbLoaderHelper
                                .getEjbGroupClassLoader(ejbGroup);
                        Class c = Class.forName(className, true, classloader);
                    } catch (java.lang.ClassNotFoundException ce) {
                        NotifyDescriptor d = new NotifyDescriptor.Message("Class " + className
                                + " not found", /*
                                                 * NbBundle.getMessage(MethodNode.class,
                                                 * "PARAMETER_NAME_NOT_UNIQUE", name ),
                                                 */
                        NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                        return;
                    }

                    methodInfo.getReturnType().setElemClassName(className);
                    EjbDataModel.getInstance().touchModifiedFlag();
                }
            });
        }

        // Signature
        ss.put(new PropertySupport.ReadOnly("signature", // NOI18N
                String.class, NbBundle.getMessage(EjbGroupNode.class, "METHOD_SIGNATURE"), NbBundle
                        .getMessage(EjbGroupNode.class, "METHOD_SIGNATURE")) {
            public Object getValue() {
                return methodInfo.toString();
            }
        });

        // Method name
        ss.put(new PropertySupport.ReadOnly("name", // NOI18N
                String.class, NbBundle.getMessage(EjbGroupNode.class, "METHOD_NAME"), NbBundle
                        .getMessage(EjbGroupNode.class, "METHOD_NAME")) {
            public Object getValue() {
                return methodInfo.getName();
            }
        });

        // Exceptions
        ss.put(new PropertySupport.ReadOnly("exceptions", // NOI18N
                String.class, NbBundle.getMessage(EjbGroupNode.class, "EXCEPTIONS"), NbBundle
                        .getMessage(EjbGroupNode.class, "EXCEPTIONS")) {
            public Object getValue() {
                return methodInfo.getExceptionsAsOneStr();
            }
        });

        // Parameters tree
        Set paramSet = sheet.get("parameters"); // NOI18N
        if (paramSet == null) {
            paramSet = new Sheet.Set();
            paramSet.setName("parameters"); // NOI18N
            paramSet.setDisplayName(NbBundle.getMessage(MethodNode.class, "METHOD_PARAMETERS")); // NOI18N
            paramSet
                    .setShortDescription(NbBundle.getMessage(MethodNode.class, "METHOD_PARAMETERS")); // NOI18N
            sheet.put(paramSet);
        }

        if (methodInfo.getParameters() != null && methodInfo.getParameters().size() != 0) {
            for (int i = 0; i < methodInfo.getParameters().size(); i++) {
                final MethodParam p = (MethodParam) methodInfo.getParameters().get(i);
                final int row = i;

                // Parameter name
                paramSet.put(new PropertySupport.ReadWrite(p.getName(), String.class, NbBundle
                        .getMessage(MethodNode.class, "PARAMETER_NAME"), NbBundle.getMessage(
                        MethodNode.class, "PARAMETER_NAME")) {
                    public Object getValue() {
                        return p.getName();
                    }

                    public void setValue(Object val) {

                        String name = (String) val;

                        if (name == null || name.trim().length() == 0)
                            return;
                        else
                            name = name.trim();

                        // Make sure it is a legal parameter name
                        try {
                            MethodParamValidator.validate(name, methodInfo, row);
                        } catch (InvalidParameterNameException e) {
                            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                                    NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(d);

                            return;
                        }

                        // Possible that the user didn't change at all
                        if (name.equals(p.getName()))
                            return;
                        else {
                            // If the user did change the name, then need to
                            // make sure
                            // the name is not used by the other parameters for
                            // the same method
                            if (!methodInfo.isParamNameUnique(name)) {
                                NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle
                                        .getMessage(MethodNode.class, "PARAMETER_NAME_NOT_UNIQUE",
                                                name), NotifyDescriptor.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(d);
                                return;
                            }
                        }

                        p.setName(name);
                        EjbDataModel.getInstance().touchModifiedFlag();
                    }
                });

                // Parameter type
                paramSet.put(new PropertySupport.ReadOnly(
                        p.getName() + "ParameteType", // NOI18N
                        String.class, NbBundle.getMessage(MethodNode.class, "PARAMETER_TYPE"),
                        NbBundle.getMessage(MethodNode.class, "PARAMETER_TYPE")) {
                    public Object getValue() {
                        return p.getType();
                    }
                });
            }
        }

        return sheet;
    }

    // Methods for Drag and Drop (not used for copy / paste at this point)

    public boolean canCopy() {
        return isMethodDroppable();
    }

    public boolean canCut() {
        return false;
    }

    private boolean isMethodDroppable() {
        if (methodInfo.getReturnType().isVoid())
            return false;
        else
            return true;
    }

    public Transferable clipboardCopy() {

        if (!isMethodDroppable())
            return null;

        // // If the bean palette item is not initialized, lets create one and
        // add the lib references needed by the palette item to the project
        // if( beanPaletteItem == null ) {
        // beanPaletteItem = new DataProviderPaletteItem( ejbGroup, methodInfo
        // );
        // }
        if (ejbGroup == null || methodInfo == null) {
            try {
                return super.clipboardCopy();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        // Add to, do not replace, the default node copy flavor:
        try {
            ExTransferable transferable = ExTransferable.create(super.clipboardCopy());
            transferable.put(
            // XXX TODO Shouldn't be used this flavor directly, rather providing
                    // specific flavors of objects transfered, not PaletteItem
                    // -> get rid of the dep.
                    // new
                    // ExTransferable.Single(PaletteItemTransferable.FLAVOR_PALETTE_ITEM)
                    // {
                    new ExTransferable.Single(FLAVOR_METHOD_DISPLAY_ITEM) {
                        protected Object getData() {
                            // return beanPaletteItem;
                            return new MethodBeanCreateInfo(ejbGroup, methodInfo);
                        }
                    });

            // // Register with the designer so that it can be dropped/linked to
            // the appropriate component
            // DesignerServiceHack.getDefault().registerTransferable(transferable);

            return transferable;
        } catch (Exception ioe) {
            System.err.println("MethodNode.clipboardCopy: Error");
            ioe.printStackTrace();
            return null;
        }
    }

    private static final DataFlavor FLAVOR_METHOD_DISPLAY_ITEM = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
            "Ejb Method Display Item"); // XXX Localize

    private static class MethodBeanCreateInfo extends BasicBeanCreateInfo {

        private final EjbGroup ejbGroup;

        private final MethodInfo methodInfo;

        public MethodBeanCreateInfo(EjbGroup ejbGroup, MethodInfo methodInfo) {
            this.ejbGroup = ejbGroup;
            this.methodInfo = methodInfo;
        }

        public String getBeanClassName() {
            // XXX Hack Jar ref adding.
            try {
                EjbLibReferenceHelper.addEjbGroupToActiveProject(ejbGroup);
            } catch (Exception e) {
                Util.getLogger().log(Level.SEVERE, "Unable to add EJB Set to Project", e);
            }
            return methodInfo.getDataProvider();
        }

        public String getDisplayName() {
            return methodInfo.getDataProvider();
        }
    } // End of MethodBeanCreateInfo.
}
