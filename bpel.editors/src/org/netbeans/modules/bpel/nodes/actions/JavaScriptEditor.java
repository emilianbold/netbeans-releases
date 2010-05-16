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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.bpel.nodes.actions;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;

import org.openide.DialogDescriptor;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.VariableUtil;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.03.25
 */
final class JavaScriptEditor extends Dialog {

    JavaScriptEditor(Assign assign) {
        myAssign = assign;
        getBpelVariables();
    }

    @Override
    protected final DialogDescriptor createDescriptor() {
        myDescriptor = new DialogDescriptor(
            createPanel(),
            i18n("LBL_JavaScript_Editor"), // NOI18N
            true,
            getButtons(),
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (myDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
                        return;
                    }
                    save();
                }
            }
        );
        return myDescriptor;
    }

    private Object[] getButtons() {
        return new Object[] {
              DialogDescriptor.OK_OPTION,
              createButton(
              new ButtonAction(i18n("LBL_Save"), i18n("TLT_Save")) { // NOI18N
                  public void actionPerformed(ActionEvent event) {
                      save();
                  }
              }),
              DialogDescriptor.CANCEL_OPTION
          };
    }

    @Override
    protected final void opened() {
        myTextArea.requestFocus();
    }

    @Override
    protected final void closed() {
        saveTemporaryFile();
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(LARGE_SIZE, TINY_SIZE, TINY_SIZE, SMALL_SIZE);

        // input button
        c.gridy++;
        c.weightx = 0.0;
        c.insets = new Insets(HUGE_SIZE, MEDIUM_SIZE, TINY_SIZE, MEDIUM_SIZE);
        c.fill = GridBagConstraints.HORIZONTAL;
        JButton button = createButton(
            new ButtonAction(i18n("LBL_Input"), i18n("TLT_Input")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    openInputDialog();
                }
            }
        );
        panel.add(button, c);

        // input text
        c.weightx = 1.0;
        c.insets = new Insets(HUGE_SIZE, MEDIUM_SIZE, TINY_SIZE, LARGE_SIZE);
        c.fill = GridBagConstraints.HORIZONTAL;
        myInputText = new JTextField();
        myInputText.setText(myAssign.getInput());
        panel.add(myInputText, c);

        // output button
        c.gridy++;
        c.weightx = 0.0;
        c.insets = new Insets(HUGE_SIZE, MEDIUM_SIZE, TINY_SIZE, MEDIUM_SIZE);
        c.fill = GridBagConstraints.HORIZONTAL;
        button = createButton(
            new ButtonAction(i18n("LBL_Output"), i18n("TLT_Output")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    openOutputDialog();
                }
            }
        );
        panel.add(button, c);

        // output text
        c.weightx = 1.0;
        c.insets = new Insets(HUGE_SIZE, MEDIUM_SIZE, TINY_SIZE, LARGE_SIZE);
        c.fill = GridBagConstraints.HORIZONTAL;
        myOutputText = new JTextField();
        myOutputText.setText(myAssign.getOutput());
        panel.add(myOutputText, c);

        // JavaScript
        c.gridy++;
        c.insets = new Insets(LARGE_SIZE, MEDIUM_SIZE, TINY_SIZE, MEDIUM_SIZE);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        createTextArea();
        myTextArea.setCaretPosition(0);
        setSize(myTextArea, TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT);
        panel.add(new JScrollPane(myTextArea), c);

        return panel;
    }

    private void createTextArea() {
        myTextArea = new JEditorPane();
        createTemporaryFile();

        try {
            Writer writer = null;

            try {
                writer = new BufferedWriter(new OutputStreamWriter(myFile.getOutputStream()));
                if (myAssign.getJavaScript() != null) {
                    writer.write(myAssign.getJavaScript());
                    writer.flush();
                }
            }
            finally {
                if (writer != null) {
                    writer.close();
                }
            }
            DataObject data = null;

            try {
                data = DataObject.find(myFile);
            }
            catch (DataObjectNotFoundException e) {
                e.printStackTrace();
            }
//out("DATA: " + data.getClass().getName());
            EditorCookie cookie = (EditorCookie) data.getCookie(EditorCookie.class);

            if (cookie == null) {
                return;
            }
//out("COOKIE: " + cookie.getClass().getName());
            Document document = cookie.openDocument();
//out("DOCUMENT: " + document.getClass().getName());
            myTextArea.setEditorKit(CloneableEditorSupport.getEditorKit("text/javascript")); // NOI18N
            myTextArea.setDocument(document);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTemporaryFile() {
        FileObject user = FileUtil.toFileObject(new File(System.getProperty("netbeans.user"))); // NOI18N
        FileObject config = user.getFileObject("config"); // NOI18N
        FileObject folder = config == null ? user : config;
        myFile = folder.getFileObject(JS_JS);

        if (myFile == null) {
            try {
                myFile = folder.createData(JS_JS);
//out("create myFile: " + myFile);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveTemporaryFile() {
        if (myFile == null) {
            return;
        }
        try {
            DataObject dataObject = DataObject.find(myFile);

            if (dataObject == null) {
                return;
            }
            SaveCookie cookie = (SaveCookie) dataObject.getCookie(SaveCookie.class);

            if (cookie == null) {
                return;
            }
            cookie.save();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
//out("SAVE");
    }

    private void getBpelVariables() {
        List<Object> variables = VariableUtil.getAllScopeVariables(myAssign);
        List<String> variableParts = new ArrayList<String>();

        for (Object object : variables) {
            collectParts(object, variableParts);
        }
        myBpelVariables = variableParts.toArray(new String[variableParts.size()]);
    }

    private void collectParts(Object object, List<String> list) {
        list.add(VariableUtil.getGoodVariableName(object));

        if ( !(object instanceof Variable)) {
            return;
        }
        Variable variable = (Variable) object;
        WSDLReference<Message> ref = variable.getMessageType();

        if (ref == null) {
            return;
        }
        Message message = ref.get();

        if (message == null) {
            return;
        }
        Collection<Part> parts = message.getParts();

        if (parts == null) {
            return;
        }
        Iterator<Part> iterator = parts.iterator();

        while (iterator.hasNext()) {
            list.add(VariableUtil.getGoodVariableName(variable) + "." + iterator.next().getName());
        }
    }

    private void openInputDialog() {
        new JavaScriptMap(myInputText,  true, myBpelVariables).show();
    }

    private void openOutputDialog() {
        new JavaScriptMap(myOutputText, false, myBpelVariables).show();
    }

    private void save() {
        BpelModel bpelModel = myAssign.getBpelModel();
        boolean wasInTransaction = false;
        if (bpelModel == null) {
            return;
        }
        wasInTransaction = bpelModel.isIntransaction();


        try {
            if (!wasInTransaction) {
                bpelModel.startTransaction();
            }
            
            myAssign.setInput(myInputText.getText().trim());
            myAssign.setOutput(myOutputText.getText().trim());
            myAssign.setJavaScript(myTextArea.getText().trim());
        } finally {
            if (bpelModel != null && !wasInTransaction) {
                bpelModel.endTransaction();
            }
        }
    }

    private Assign myAssign;
    private FileObject myFile;
    private JEditorPane myTextArea;
    private JTextField myInputText;
    private JTextField myOutputText;
    private String[] myBpelVariables;
    private DialogDescriptor myDescriptor;

    private static final int TEXT_AREA_WIDTH = 570;
    private static final int TEXT_AREA_HEIGHT = 450;
    private static final String JS_JS = ".js.js"; // NOI18N
}
