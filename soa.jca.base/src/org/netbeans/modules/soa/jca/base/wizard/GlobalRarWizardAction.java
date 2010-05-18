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

package org.netbeans.modules.soa.jca.base.wizard;

import org.netbeans.modules.soa.jca.base.GlobalRarRegistry;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Properties;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.soa.jca.base.OutboundGenerator;
import org.netbeans.modules.soa.jca.base.generator.api.GeneratorUtil;
import org.netbeans.modules.soa.jca.base.generator.api.JavacTreeModel;
import org.netbeans.modules.soa.jca.base.spi.GlobalRarProvider;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 * Action invoked when drag-n-drop an icon from palette
 *
 * @author echou
 */
public final class GlobalRarWizardAction {

    public static final String BUSINESS_RULE_PROP = "BUSINESS_RULE"; // NOI18N
    public static final String RAR_NAME_PROP = "RAR_NAME"; // NOI18N
    public static final String JNDI_NAME_PROP = "JNDI_NAME"; // NOI18N
    public static final String ROLLBACK_PROP = "ROLLBACK"; // NOI18N
    public static final String LOG_EX_PROP = "LOG_EX"; // NOI18N
    public static final String RETHROW_PROP = "RETHROW"; // NOI18N
    public static final String DESCRIPTION_PROP = "DESCRIPTION"; // NOI18N
    public static final String AUTHENTICATION_PROP = "AUTHENTICATION"; // NOI18N
    public static final String SHAREABLE_PROP = "SHAREABLE"; // NOI18N
    public static final String LOCAL_VAR_NAME_PROP = "LOCAL_VAR_NAME"; // NOI18N
    public static final String OTD_TYPE_PROP = "OTD_TYPE"; // NOI18N
    public static final String ADDITIONAL_CONFIG_PROP = "ADDITIONAL_CONFIG"; // NOI18N
    public static final String RETURN_TYPE_PROP = "RETURN_TYPE"; // NOI18N

    private JTextComponent target;
    private String rarName;
    private JavacTreeModel javacTreeModel;
    private Project project;

    public GlobalRarWizardAction(JTextComponent target, String rarName) throws Exception {
        this.target = target;
        this.rarName = rarName;
        this.project = findProjectFromTextComponent(target);
        this.javacTreeModel = GeneratorUtil.createJavacTreeModel(target);
    }

    private Project findProjectFromTextComponent(JTextComponent comp) {
        JavaSource javaSource = JavaSource.forDocument(comp.getDocument());
        FileObject fo = javaSource.getFileObjects().iterator().next();
        return FileOwnerQuery.getOwner(fo);
    }

    public void invoke() throws Exception {
        WizardDescriptor.Iterator iterator = new GlobalRarOutboundWizard(javacTreeModel, project, rarName);
        WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        // set default selected rarName to descriptor
        if (rarName != null) {
            wizardDescriptor.putProperty(RAR_NAME_PROP, rarName);
        }

        GlobalRarProvider provider = GlobalRarRegistry.getInstance().getRar(rarName);

        if (provider.getOTDTypes() != null && provider.getOTDTypes().size() == 1) {
            wizardDescriptor.putProperty(OTD_TYPE_PROP, provider.getOTDTypes().get(0));
        }

        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("GlobalRar_Wizard"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            String businessRule = (String) wizardDescriptor.getProperty(BUSINESS_RULE_PROP);
            String myRarName = (String) wizardDescriptor.getProperty(RAR_NAME_PROP);
            String jndiName = (String) wizardDescriptor.getProperty(JNDI_NAME_PROP);
            Boolean rollbackTx = (Boolean) wizardDescriptor.getProperty(ROLLBACK_PROP);
            Boolean logException = (Boolean) wizardDescriptor.getProperty(LOG_EX_PROP);
            Boolean rethrowException = (Boolean) wizardDescriptor.getProperty(RETHROW_PROP);
            String description = (String) wizardDescriptor.getProperty(DESCRIPTION_PROP);
            String authentication = (String) wizardDescriptor.getProperty(AUTHENTICATION_PROP);
            String shareable = (String) wizardDescriptor.getProperty(SHAREABLE_PROP);
            String localVarName = (String) wizardDescriptor.getProperty(LOCAL_VAR_NAME_PROP);
            String otdType = (String) wizardDescriptor.getProperty(OTD_TYPE_PROP);
            Properties additionalConfig = (Properties) wizardDescriptor.getProperty(ADDITIONAL_CONFIG_PROP);
            String returnType = (String) wizardDescriptor.getProperty(RETURN_TYPE_PROP);
            OutboundGenerator outboundGenerator = new OutboundGenerator(target, javacTreeModel,
                    businessRule, myRarName, jndiName, rollbackTx.booleanValue(),
                    logException.booleanValue(), rethrowException.booleanValue(),
                    description, authentication,
                    shareable, localVarName, otdType, additionalConfig, returnType);

            RequestProcessor.getDefault().post(new OutboundRunAction(outboundGenerator));
        }
    }

    private static class OutboundRunAction implements Runnable {

        private OutboundGenerator generator;

        OutboundRunAction(OutboundGenerator generator) {
            this.generator = generator;
        }

        public void run() {
            ProgressHandle progressHandle =
                    ProgressHandleFactory.createHandle(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("Progress_bar_title"));
            try {
                progressHandle.start(2);
                generator.addLibraryDependency();
                progressHandle.progress(1);
                generator.generateFromTemplate();
            } catch (Exception e) {
                NotifyDescriptor d = new NotifyDescriptor.Exception(e);
                DialogDisplayer.getDefault().notifyLater(d);
                return;
            } finally {
                progressHandle.finish();
            }
        }
    }

}

