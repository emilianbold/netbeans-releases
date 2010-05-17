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

package org.netbeans.modules.websvc.rest.wadl.design.view.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class AddMethodAction extends AbstractAction implements Node.Cookie {
    public static final String ADD_METHOD = "ADD_METHOD";
    
    private ResourceType resource;
    private WadlModel model;
    private String resourcePath;

    /**
     * Creates a new instance of AddMethodAction
     * @param implementationClass fileobject of service implementation class
     */
    public AddMethodAction(ResourceType resource, String resourcePath, WadlModel model) {
        super(getName());
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/websvc/rest/wadl/design/view/resources/method.png", false));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddMethodAction.class, "Hint_AddMethod"));
        putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(AddMethodAction.class, "LBL_AddMethod_mnem_pos")));
        this.resource=resource;
        this.resourcePath = resourcePath;
        this.model = model;
    }
    
    private static String getName() {
        return NbBundle.getMessage(AddMethodAction.class, "LBL_AddMethod");
    }
    
    public void actionPerformed(ActionEvent arg0) {
        final AddMethodPanel panel = new AddMethodPanel(resource, resourcePath);
        boolean closeDialog = false;
        DialogDescriptor desc = new DialogDescriptor(panel,
                NbBundle.getMessage(AddMethodAction.class, "TTL_AddMethod"));
        while (!closeDialog) {
            DialogDisplayer.getDefault().notify(desc);
            if (desc.getValue() == DialogDescriptor.OK_OPTION) {
                    closeDialog = true;
                    final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.
                            getMessage(AddMethodAction.class, "MSG_AddingMethod", panel.getMethodName())); //NOI18N
//                    Task task = new Task(new Runnable() {
//                        public void run() {
                            try{
                                handle.start();
                                addMethod(panel.getMethodName(), panel.getMethodType(), panel.getMimeType());
                            }catch(Exception e){
                                handle.finish();
                                ErrorManager.getDefault().notify(e);
                            }finally{
                                handle.finish();
                            }
//                        }
//                    });
//                    RequestProcessor.getDefault().post(task);
            } else {
                closeDialog = true;
            }
        }
    }

    public void addMethod(String methodName, String methodType, String mimeType) throws IOException {
        Method method = null;
        try {
            model.startTransaction();
            method = model.getFactory().createMethod();
            method.setId(methodName);
            method.setName(methodType);
            Request request = model.getFactory().createRequest();
    //        Collection<Param> params = request.getParam();
    //        for(ParamModel p:parameters) {
    //            Param param = model.getFactory().createParam();
    //            param.setName(p.getParamName());
    //            String type = p.getParamType();
    //            if(type.indexOf(":") != -1) {
    //                param.setType(new QName(WadlModel.XML_SCHEMA_NS,
    //                        type.substring(type.indexOf(":")+1), type.substring(0, type.indexOf(":"))));
    //            } else {
    //                param.setType(new QName(WadlModel.XML_SCHEMA_NS, type));
    //            }
    //            param.setStyle(p.getParamStyle().value());
    //            param.setDefault(p.getParamDefault());
    //            request.addParam(param);
    //        }
            method.addRequest(request);
            Response response = model.getFactory().createResponse();
            Representation rep = model.getFactory().createRepresentation();
            rep.setMediaType(mimeType);
            response.addRepresentation(rep);
            method.addResponse(response);
            resource.addMethod(method);
        } finally {
            model.endTransaction();
        }
        
        if(method != null) {
            PropertyChangeListener[] listeners = getPropertyChangeListeners();
            for(PropertyChangeListener l:listeners) {
                l.propertyChange(new PropertyChangeEvent(resource, ADD_METHOD, null, method));
            }
        }
    }

    private WadlModel getModel() {
        return this.model;
    }
}
