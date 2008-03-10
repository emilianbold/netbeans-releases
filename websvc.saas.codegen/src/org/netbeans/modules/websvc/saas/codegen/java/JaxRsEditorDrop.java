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
package org.netbeans.modules.websvc.saas.codegen.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** JaxRsEditorDrop
 *
 * @author Ayub Khan, Nam Nguyen
 */
public class JaxRsEditorDrop implements ActiveEditorDrop {

    private WadlSaasMethod method;
    private FileObject targetFO;
    private RequestProcessor.Task generatorTask;

    public JaxRsEditorDrop(WadlSaasMethod method) {
        this.method = method;
    }

    private boolean isAcceptTarget(JTextComponent targetComponent) {
        Object mimeType = targetComponent.getDocument().getProperty("mimeType"); //NOI18N
        if (mimeType != null && ("text/x-java".equals(mimeType) || "text/x-jsp".equals(mimeType))) { //NOI18N
            return true;
        }
        return false;
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        if(isAcceptTarget(targetComponent))
            return doHandleTransfer(targetComponent);
        return false;
    }
    
    private boolean doHandleTransfer(final JTextComponent targetComponent) {
        FileObject targetSource = NbEditorUtilities.getFileObject(targetComponent.getDocument());
        Project targetProject = FileOwnerQuery.getOwner(targetSource);
        Method m = method.getWadlMethod();
        final String displayName = m.getName();
        
        targetFO = getTargetFile(targetComponent);

        if (targetFO == null) {
            return false;
        }

        final List<Exception> errors = new ArrayList<Exception>();
       
        final ProgressDialog dialog = new ProgressDialog(
                NbBundle.getMessage(JaxRsEditorDrop.class, "LBL_CodeGenProgress", 
                displayName));

        generatorTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                try {
                    JaxRsCodeGenerator codegen = 
                        JaxRsCodeGeneratorFactory.create(targetComponent, targetFO, method);
                
                    WadlSaasBean bean = codegen.getBean();
                    boolean showParams = codegen.canShowParam();
                    List<ParameterInfo> allParams = bean.filterParametersByAuth(
                            bean.filterParameters(new ParamFilter[]{ParamFilter.FIXED}));
                    if(codegen.canShowResourceInfo() || (showParams && !allParams.isEmpty())) {
                        JaxRsCodeSetupPanel panel = new JaxRsCodeSetupPanel(
                                codegen.getSubresourceLocatorUriTemplate(),
                                bean.getQualifiedClassName(), 
                                allParams,
                                codegen.canShowResourceInfo(), showParams);

                        DialogDescriptor desc = new DialogDescriptor(panel, 
                                NbBundle.getMessage(JaxRsEditorDrop.class,
                                "LBL_CustomizeSaasService", displayName));
                        Object response = DialogDisplayer.getDefault().notify(desc);

                        if (response.equals(NotifyDescriptor.YES_OPTION)) {
                            codegen.setSubresourceLocatorUriTemplate(panel.getUriTemplate());
                            codegen.setSubresourceLocatorName(panel.getMethodName());
                        } else {
                            // cancel
                            return;
                        }
                    }

                    try {
                        codegen.generate(dialog.getProgressHandle());
                    } catch(IOException ex) {
                        if(!ex.getMessage().equals(Util.SCANNING_IN_PROGRESS))
                            errors.add(ex);
                    }
                    try {
                        Util.showMethod(targetFO, codegen.getSubresourceLocatorName());
                    } catch(IOException ex) {//ignore
                    }
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
