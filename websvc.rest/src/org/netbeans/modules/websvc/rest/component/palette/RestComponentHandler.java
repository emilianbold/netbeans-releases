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

package org.netbeans.modules.websvc.rest.component.palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.rest.codegen.CustomComponentGenerator;
import org.netbeans.modules.websvc.rest.codegen.WsdlComponentGenerator;
import org.netbeans.modules.websvc.rest.codegen.RestComponentGenerator;
import org.netbeans.modules.websvc.rest.codegen.WadlComponentGenerator;
import org.netbeans.modules.websvc.rest.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.rest.codegen.model.RestComponentBean;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.netbeans.modules.websvc.rest.wizard.ProgressDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.xml.sax.SAXException;

/**
 *
 * @author Owner
 */
public class RestComponentHandler implements ActiveEditorDrop {

    private FileObject targetFO;
    private RestComponentData data;
    private RequestProcessor.Task generatorTask;

    public RestComponentHandler() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        targetFO = getTargetFile(targetComponent);

        if (targetFO == null) {
            return false;
        }

        final List<Exception> errors = new ArrayList<Exception>();
        final Project project = FileOwnerQuery.getOwner(targetFO);
        Lookup pItem = RestPaletteFactory.getCurrentPaletteItem();
        final Node n = pItem.lookup(Node.class);
        if(n == null) {
            Exceptions.printStackTrace(new IOException(NbBundle.getMessage(RestComponentHandler.class, 
                    "LBL_RestComponentNodeNull")));
            return false;
        }
        data = RestPaletteFactory.getRestComponentData(n); 
        if(data == null) {//if cannot find data, then try to create it using node's information
            try {
                List<FileObject> files = RestPaletteFactory.getAllRestComponentFiles();
                for (FileObject fo : files) {
                    if (fo.getName().equals(n.getName())) {
                        data = RestPaletteFactory.getComponentData(fo);
                        break;
                    }
                }
                if(data == null) {
                    Exceptions.printStackTrace(new IOException(
                            NbBundle.getMessage(RestComponentHandler.class, 
                                "LBL_RestComponentNotFound", n.getName())));
                    return false;
                }
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "handleTransfer", e);
            }
        }
        if(data.getService() == null) {
            Exceptions.printStackTrace(new IOException(NbBundle.getMessage(RestComponentHandler.class, 
                    "LBL_RestComponentServiceNotFound", n.getName())));
            return false;
        }
        if(data.getService().getMethods() == null || data.getService().getMethods().size() == 0) {
            Exceptions.printStackTrace(new IOException(NbBundle.getMessage(RestComponentHandler.class, 
                    "LBL_RestComponentMethodNotFound", n.getName())));
            return false;
        }
        final String type = data.getService().getMethods().get(0).getType();
       
        final ProgressDialog dialog = new ProgressDialog(
                NbBundle.getMessage(RestComponentHandler.class, "LBL_RestComponentProgress", 
                n.getName()));

        generatorTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                try {
                    RestComponentGenerator codegen = null;

                    if (RestComponentData.isWSDL(type)) {
                        codegen = new WsdlComponentGenerator(targetFO, data);
                    } else if (RestComponentData.isWADL(type)) {
                        codegen = new WadlComponentGenerator(targetFO, data);
                    } else if (RestComponentData.isCustom(type)) {
                        codegen = new CustomComponentGenerator(targetFO, data);
                    }
                
                    RestComponentBean bean = codegen.getBean();
                    boolean wrapperResourceExists = codegen.wrapperResourceExists();
                    List<ParameterInfo> allParams = new ArrayList<ParameterInfo>(bean.getHeaderParameters());
                    if (! wrapperResourceExists) {
                        allParams.addAll(bean.getInputParameters());
                    }
                    RestComponentSetupPanel panel = new RestComponentSetupPanel(
                            codegen.getSubresourceLocatorUriTemplate(),
                            bean.getQualifiedClassName(), 
                            allParams,
                            wrapperResourceExists);

                    DialogDescriptor desc = new DialogDescriptor(panel, 
                            NbBundle.getMessage(RestComponentHandler.class,
                            "LBL_CustomizeComponent", n.getName()));
                    Object response = DialogDisplayer.getDefault().notify(desc);
                    
                    if (response.equals(NotifyDescriptor.YES_OPTION)) {
                        codegen.setSubresourceLocatorUriTemplate(panel.getUriTemplate());
                        codegen.setSubresourceLocatorName(panel.getMethodName());
                    } else {
                        // cancel
                        return;
                    }

                    codegen.generate(dialog.getProgressHandle());
                    Utils.showMethod(targetFO, codegen.getSubresourceLocatorName());
                } catch (Exception ioe) {
                    errors.add(ioe);
                } finally {
                    dialog.close();
                }
            }
        });

        generatorTask.schedule(50);

        dialog.open();

        if (errors.size() > 0) {
            Exceptions.printStackTrace(errors.get(0));
            return false;
        }
        return true;
    }

    public static FileObject getTargetFile(JTextComponent targetComponent) {
        if (targetComponent == null) {
            return null;
        }
        DataObject d = NbEditorUtilities.getDataObject(targetComponent.getDocument());
        if (d == null) {
            return null;
        }
        EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
        if (ec == null || ec.getOpenedPanes() == null) {
            return null;
        }
        return d.getPrimaryFile();
    }
    
    
}