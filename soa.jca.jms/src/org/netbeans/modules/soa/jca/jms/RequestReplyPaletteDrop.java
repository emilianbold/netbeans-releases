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

package org.netbeans.modules.soa.jca.jms;

import org.netbeans.modules.soa.jca.base.generator.api.GeneratorUtil;
import org.netbeans.modules.soa.jca.base.generator.api.JavacTreeModel;
import org.netbeans.modules.soa.jca.jms.ui.RequestReplyPanel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.HelpCtx;

/**
 *
 * @author echou
 */
public class RequestReplyPaletteDrop implements ActiveEditorDrop {

    /**
    * A method called from the drop target that supports the artificial DataFlavor.
    * @param target a Component where drop operation occured
    * @return true if implementor allowed a drop operation into the targetComponent
    */
    public boolean handleTransfer(JTextComponent target) {
        try {
            JavaSource javaSource = JavaSource.forDocument(target.getDocument());
            FileObject fo = javaSource.getFileObjects().iterator().next();
            Project project = FileOwnerQuery.getOwner(fo);
            JavacTreeModel javacTreeModel = GeneratorUtil.createJavacTreeModel(target);
            final RequestReplyPanel panel = new RequestReplyPanel(project, javacTreeModel, target.getCaretPosition());
            final DialogDescriptor d = new DialogDescriptor(
                    panel,  // innerPane
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/jms/ui/Bundle").getString("Request_Reply_Dialog"),  // title
                    true,  // modal
                    NotifyDescriptor.OK_CANCEL_OPTION,  // optionType
                    NotifyDescriptor.OK_OPTION,  // initialValue
                    DialogDescriptor.BOTTOM_ALIGN,  // align
                    new HelpCtx("org.netbeans.modules.soa.jca.jms.about"),  // helpctx
                    null  // actionListener
                    );

            panel.setDialogDescriptor(d);

            DialogDisplayer.getDefault().notify(d);
            if (d.getValue() == NotifyDescriptor.OK_OPTION) {
                RequestReplyTask<WorkingCopy> requestReplyTask = new RequestReplyTask<WorkingCopy> (panel);
                GeneratorUtil.runModificationTask(target, requestReplyTask);
            }

        } catch(Throwable t) {
            t.printStackTrace();
            NotifyDescriptor d = new NotifyDescriptor.Exception(t);
            DialogDisplayer.getDefault().notifyLater(d);
        }

        return true;
    }

}
