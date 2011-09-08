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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.common.method.FieldCustomizer;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.action.CmFieldGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.omg.CORBA.Request;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import sun.print.RasterPrinterJob;

/**
 * Action that can always be invoked and work procedurally.
 * @author Chris Webster
 */
public class AddCmpFieldAction extends NodeAction {

    private final String NAME = NbBundle.getMessage(AddCmpFieldAction.class, "LBL_AddCmpFieldAction");

    public String getName() {
        return NAME;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length < 1) {
            return false;
        }
        EjbMethodController ejbMethodController;
        ElementHandle<TypeElement> elementHandle = null;
        try {
             elementHandle = _RetoucheUtil.getJavaClassFromNode(activatedNodes[0]);
            if (elementHandle == null) {
                return false;
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
        FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        return activatedNodes.length == 1 &&
               (ejbMethodController = EjbMethodController.createFromClass(fileObject, elementHandle.getQualifiedName())) != null &&
               ejbMethodController instanceof EntityMethodController &&
               ((EntityMethodController) ejbMethodController).isCMP();
    }

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length < 1) {
            return;
        }
        try {
            ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(activatedNodes[0]);
            if (elementHandle != null) {
                FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);
                FileObject ddFile = EjbJar.getEjbJar(fileObject).getDeploymentDescriptor();
                EntityMethodController emc = (EntityMethodController) EjbMethodController.createFromClass(fileObject, elementHandle.getQualifiedName());
                MethodModel.Variable field = MethodModel.Variable.create("java.lang.String", "cmpField");
                addCmpField(emc, ddFile, field);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public static boolean addCmpField(FileObject fileObject, String className, FileObject ddFile) {
        MethodModel.Variable field = MethodModel.Variable.create("java.lang.String", "cmpField");
        EntityMethodController emc = (EntityMethodController) EntityMethodController.createFromClass(fileObject, className);
        try {
            return addCmpField(emc, ddFile, field);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return false;
    }

    private static boolean addCmpField(EntityMethodController emc, FileObject ddFile, MethodModel.Variable field) throws IOException {

        final String ejbClass = emc.getBeanClass();
        final Entity[] entity = new Entity[1];
        final FileObject[] ejbClassFO = new FileObject[1];

        MetadataModel<EjbJarMetadata> metadataModel = EjbJar.getEjbJar(ddFile).getMetadataModel();
        metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) {
                entity[0] = (Entity) metadata.findByEjbClass(ejbClass);
                ejbClassFO[0] = metadata.findResource(Utils.toResourceName(ejbClass));
                return null;
            }
        });

        final FieldCustomizer customizer = new FieldCustomizer(entity[0], field, "",
                emc.getLocal() != null, emc.getRemote() != null, true, true, false, false);
        if (customizer.customizeField()) {
            final MethodModel.Variable customizedField = customizer.getField();
            final CmFieldGenerator generator = CmFieldGenerator.create(emc.getBeanClass(), ejbClassFO[0]);

            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        generator.addCmpField(customizedField, customizer.isLocalGetter(), customizer.isLocalSetter(),
                            customizer.isRemoteGetter(), customizer.isRemoteSetter(), customizer.getDescription());
                    } catch (IOException ioe) {
                        Logger.getLogger(AddCmpFieldAction.class.getName()).log(Level.WARNING, null, ioe);
                    }
                }
            });

            return true;
        }
        return false;
    }

    public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
        return enable ? super.createContextAwareInstance(actionContext) : null;
    }

}
