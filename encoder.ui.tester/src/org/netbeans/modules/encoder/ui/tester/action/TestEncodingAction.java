/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.encoder.ui.tester.action;

import com.sun.encoder.EncoderConfigurationException;
import com.sun.encoder.EncoderFactory;
import com.sun.encoder.EncoderType;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.netbeans.modules.encoder.ui.basic.EncodingMark;
import org.netbeans.modules.encoder.ui.basic.ModelUtils;
import org.netbeans.modules.encoder.ui.tester.EncoderTestPerformer;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

/**
 * Action that tests an encoder (such as decode/encode etc.)
 *
 * @author Jun Xu
 */
public class TestEncodingAction extends NodeAction {

    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/ui/tester/action/Bundle");

    protected void performAction(Node[] node) {
        SchemaModel model;
        try {
            model = ModelUtils.getSchemaModelFromNode(node[0]);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return;
        }
        String xsdPath = ModelUtils.getFilePath(model);
        if (xsdPath == null) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR,
                    new NullPointerException(
                        _bundle.getString("test_encoding.exp.unable_retrieve_path")));
            return;
        }
        ModelUtils.ModelStatus modelStatus;
        try {
            modelStatus =
                    ModelUtils.getModelStatus(model, true);
        } catch (CatalogModelException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
        if (modelStatus.isModified()) {
            String[] dataObjs = modelStatus.getModifiedFiles();
            StringBuffer sb = new StringBuffer();
            sb.append(_bundle.getString("test_encoding.exp.file_not_saved"));
            for (int i = 0; i < dataObjs.length; i++) {
                sb.append(dataObjs[i]).append("\n");  //NOI18N
            }
            sb.append(_bundle.getString("test_encoding.exp.please_save_file"));
            JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(), sb.toString());
            return;
        }

        EncodingMark mark = ModelUtils.getEncodingMark(model);
        EncoderType encoderType;
        try {
            encoderType = EncoderFactory.newInstance().makeType(mark.getStyle());
        } catch (EncoderConfigurationException ex) {
            String msg =
                    NbBundle.getMessage(TestEncodingAction.class,
                        "test_encoding.exp.style_not_recognized", mark.getStyle()); //NOI18N
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg);
            return;
        }

        EncoderTestPerformer testPerformer = EncoderTestPerformer.Factory.getDefault();
        testPerformer.performTest(new File(xsdPath), encoderType);
    }

    public String getName() {
        return _bundle.getString("test_encoding.lbl.test_enc_menu_item");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] node) {
        //Check if it is writable
        if (node == null || node.length == 0 || node.length > 1) {
            return false;
        }
        SchemaModel sm;
        try {
            sm = ModelUtils.getSchemaModelFromNode(node[0]);
        } catch (IOException e) {
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.toString());
            return false;
        }
        if (sm == null) {
            return false;
        }
        return ModelUtils.hasEncodingMark(sm);
    }
}
