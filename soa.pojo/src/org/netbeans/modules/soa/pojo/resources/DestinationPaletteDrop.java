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

package org.netbeans.modules.soa.pojo.resources;

import com.sun.source.tree.Scope;
import java.awt.Dialog;
import javax.lang.model.element.ExecutableElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.soa.pojo.model.api.JavaModel;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.MethodExistenceCheckUtil;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * Base class for Pallete Drop in Java Editor
 * @author Sreenivasan Genipudi
 */
public abstract class DestinationPaletteDrop {
    /**
     * Enum of Destination Type.
     */
    public enum DestinationType { PROVIDER, CONSUMER,
        BINDINGCONSUMER;
    };
    //Current instance destination type
    protected DestinationType destType;
    //Wizard descriptor.
    protected WizardDescriptor mWizDesc = null;
    //Wizard Iterator
    protected WizardDescriptor.ProgressInstantiatingIterator wizInstItr = null;
    protected TemplateWizard.Iterator wizTempItr= null;

    protected void destinationAction(JTextComponent target) {
        CompilationInfo compInfo;
        try {
            JavaSource javaSource = JavaSource.forDocument(target.getDocument());
            FileObject fo = javaSource.getFileObjects().iterator().next();
            Project project = FileOwnerQuery.getOwner(fo);

            
            JavaModel javacTreeModel = GeneratorUtil.createJavacTreeModel(target);
            compInfo = javacTreeModel.getCompilationInfo();
            int cursorLoc = Integer.valueOf(target.getCaretPosition());
            this.mWizDesc.putProperty(GeneratorUtil.PROJECT_INSTANCE, project);
            this.mWizDesc.putProperty(GeneratorUtil.CURSOR_LOC, cursorLoc);
            this.mWizDesc.putProperty(GeneratorUtil.POJO_JAVAC_MODEL, javacTreeModel);
             mWizDesc.putProperty(GeneratorUtil.POJO_FILE_LOCATION, fo.getPath());
             mWizDesc.putProperty(GeneratorUtil.POJO_JAVA_SOURCE_INSTANCE, javaSource);
            
            if ( this.destType == DestinationType.PROVIDER) {
                MethodExistenceCheckUtil methodUtil = new MethodExistenceCheckUtil(javaSource, GeneratorUtil.POJO_DEFAULT_RECEIVE_OPERATION, null, false);
                if ( methodUtil.containsPOJO()) {
                     NotifyDescriptor d = new NotifyDescriptor.Message(org.openide.util.NbBundle.getMessage(this.getClass(), "POJO_ANNOTATION_ALREADY_EXISTS"));
                     DialogDisplayer.getDefault().notify(d);
                     return;
                }
                
                if  ( methodUtil.containsOperation()) {
                     NotifyDescriptor d = new NotifyDescriptor.Message(org.openide.util.NbBundle.getMessage(this.getClass(), "POJO_OPN_ALREADY_EXISTS"));
                     DialogDisplayer.getDefault().notify(d);
                     return;
                }
                if ( methodUtil.containsMethod()) {
                    this.mWizDesc.putProperty(GeneratorUtil.POJO_METHOD_NAME, methodUtil.recommendedMethodName(GeneratorUtil.POJO_DEFAULT_RECEIVE_OPERATION));
                } else {
                    this.mWizDesc.putProperty(GeneratorUtil.POJO_METHOD_NAME, GeneratorUtil.POJO_DEFAULT_RECEIVE_OPERATION);
                }

                String qualifiedName = javacTreeModel.getQualifiedName();

                int inx = qualifiedName.lastIndexOf(".");
                String packageName = "";
                String className = qualifiedName;
                if ( inx != -1) {
                    className = qualifiedName.substring(inx+1);
                    packageName = qualifiedName.substring(0,inx);
                }

                WizardDescriptor wd = this.mWizDesc;
                 wd.putProperty(GeneratorUtil.POJO_CLASS_NAME, className);
                 wd.putProperty(GeneratorUtil.POJO_PACKAGE_NAME, packageName);
                 wd.putProperty(GeneratorUtil.POJO_PROJECT_NAME,  ProjectUtils.getInformation(project).getDisplayName());
                 wd.putProperty(GeneratorUtil.POJO_ENDPOINT_NAME, className);
                 wd.putProperty(GeneratorUtil.POJO_INTERFACE_NAME, className);             
                 wd.putProperty(GeneratorUtil.POJO_SERVICE_NAME,  className+GeneratorUtil.POJO_SERVICE_SUFFIX );
                 String defaultNS = GeneratorUtil.getNamespace(packageName, className);             
                 wd.putProperty(GeneratorUtil.POJO_INTERFACE_NS,defaultNS);
                 wd.putProperty(GeneratorUtil.POJO_SERVICE_NS,defaultNS);
                 wd.putProperty(GeneratorUtil.POJO_OPERATION_METHOD_NAME,GeneratorUtil.POJO_METHOD_NAME);

                 //wd.putProperty(GeneratorUtil.PROJECT_INSTANCE, project);
                 wd.putProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NS,defaultNS);
                 wd.putProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NAME,className+GeneratorUtil.POJO_OUT_MESSAGE_SUFFIX);
                 Templates.setTargetFolder(wd,fo);
            } else {
                MethodExistenceCheckUtil methodUtil = new MethodExistenceCheckUtil(javaSource, GeneratorUtil.POJO_DEFAULT_RECEIVE_OPERATION, null, true);
                if (! ( methodUtil.containsPOJO() && methodUtil.containsOperation())) {
                     NotifyDescriptor d = new NotifyDescriptor.Message(org.openide.util.NbBundle.getMessage(this.getClass(), "CONSUMER_INVOKE_ONLY_ON_POJO_PROVIDER"));//NOI18N
                     DialogDisplayer.getDefault().notify(d);
                     return;
                }
                this.mWizDesc.putProperty(GeneratorUtil.POJO_GET_METHOD_LIST, methodUtil.getMethodList());
                
              //  int cursorLoc = -1;
                Scope scope = javacTreeModel.getCompilationInfo().getTreeUtilities().scopeFor(cursorLoc);
                if (scope != null) {
                    ExecutableElement enclosingMethod = scope.getEnclosingMethod();
                    this.mWizDesc.putProperty(GeneratorUtil.POJO_SELECTED_METHOD, enclosingMethod);
                }
            }
             
             Dialog consumerDialog = null;
             if ( wizInstItr != null) {
                this.wizInstItr.initialize(this.mWizDesc);
             } else if ( wizTempItr != null) {
                 
             }
             consumerDialog =  org.openide.DialogDisplayer.getDefault().createDialog(this.mWizDesc);                
             if (  destType == DestinationType.CONSUMER) {
                 int width = consumerDialog.getWidth();
                 int ht = consumerDialog.getHeight();
                 consumerDialog.setSize(width+120,ht+40);
                 consumerDialog.setTitle(NbBundle.getMessage(this.getClass(), "ttl_consumer_wizard"));//NOI18N
             } else if ( destType == DestinationType.BINDINGCONSUMER) {
                 int width = consumerDialog.getWidth();
                 int ht = consumerDialog.getHeight();
                 consumerDialog.setSize(width,ht+160);
                 consumerDialog.setTitle(NbBundle.getMessage(this.getClass(), "ttl_binding_consumer_wizard"));//NOI18N
             }
             consumerDialog.setVisible(true);
               
             //org.openide.DialogDisplayer.getDefault().createDialog(this.mWizDesc).setVisible(true);
            
            
        } catch(Throwable t) {
            t.printStackTrace();
            NotifyDescriptor d = new NotifyDescriptor.Exception(t);
            DialogDisplayer.getDefault().notifyLater(d);
            return;
        }
    }

}