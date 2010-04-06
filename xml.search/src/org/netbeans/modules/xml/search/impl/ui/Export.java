/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.xml.search.impl.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.openide.DialogDescriptor;
import org.openide.awt.HtmlBrowser;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.21
 */
public class Export extends Dialog {

    public void show(List<List<String>> items, String title) {
        myItems = items;
        myTitle = title;
        show();
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(LARGE_SIZE, TINY_SIZE, TINY_SIZE, SMALL_SIZE);

        // file name
        panel.add(createLabel(i18n("LBL_File_Name")), c); // NOI18N

        c.gridy++;
        c.weightx = 1.0;
        c.insets = new Insets(LARGE_SIZE, TINY_SIZE, TINY_SIZE, LARGE_SIZE);
        c.fill = GridBagConstraints.HORIZONTAL;
        myFileName = new JTextField(TEXT_WIDTH);
        myFileName.setText(getResultFolder());
        panel.add(myFileName, c);

        c.weightx = 0.0;
        c.insets = new Insets(LARGE_SIZE, 0, TINY_SIZE, MEDIUM_SIZE);
        c.fill = GridBagConstraints.NONE;
        JButton button = createButton(
            new ButtonAction(i18n("LBL_Browse"), i18n("TLT_Browse")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    selectFile();
                }
            }
        );
        Dimension dimension = myFileName.getPreferredSize();
        dimension.width = dimension.height;
        button.setPreferredSize(dimension);
        button.setMinimumSize(dimension);
        panel.add(button, c);

        // description
        c.gridy++;
        c.insets = new Insets(MEDIUM_SIZE, TINY_SIZE, SMALL_SIZE, MEDIUM_SIZE);
        panel.add(createLabel(i18n("LBL_Description")), c); // NOI18N

        c.insets = new Insets(MEDIUM_SIZE, TINY_SIZE, TINY_SIZE, MEDIUM_SIZE);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        myDescription = new JTextArea(TEXT_HEIGHT, 1);
        panel.add(new JScrollPane(myDescription), c);

        // []
        c.gridy++;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        panel.add(new JLabel(), c);

        // open in browser
        c.gridwidth = 2;
        c.insets = new Insets(TINY_SIZE, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        myRunBrowser = createCheckBox(
            new ButtonAction(i18n("LBL_Open_in_Browser")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                }
            }
        );
        myRunBrowser.setSelected(true);
        panel.add(myRunBrowser, c);

        return panel;
    }

    private void checkFolder(File folder) {
        if (folder == null || folder.exists()) {
            return;
        }
        checkFolder(folder.getParentFile());
        folder.mkdir();
    }

    private void createFile(String name) {
        File file = new File(name);

        if (file.exists()) {
            if (printWarning(i18n("LBL_File_Exists", name))) { // NOI18N
                exportFile(file);
            } else {
                show();
            }
        } else {
            exportFile(file);
        }
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {

            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(HTML_EXT) || file.isDirectory();
            }

            public String getDescription() {
                return i18n("LBL_HTML_File_Type"); // NOI18N
            }
        });
        chooser.setSelectedFile(new File(myFileName.getText()));

        if (chooser.showOpenDialog(getUIComponent()) == JFileChooser.APPROVE_OPTION) {
            myFileName.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void exportFile(File file) {
        // title
        StringBuilder html = new StringBuilder();
        html.append("<html>" + LS); // NOI18N

        if (myTitle != null) {
            html.append("<title>" + removeHtml(myTitle) + "</title>" + LS); // NOI18N
        }
        html.append("<body>" + LS); // NOI18N
        html.append("<h3>" + i18n("LBL_Search_Results") + "</h3>" + LS + LS); // NOI18N

        if (myTitle != null) {
            html.append(myTitle + LS + LS);
        }
        // description
        html.append("<p><b>" + i18n("LBL_Description") + "</b>" + LS); // NOI18N
        html.append("<pre>" + LS); // NOI18N
        html.append(myDescription.getText() + LS);
        html.append("</pre>" + LS); // NOI18N

        // items
        int count = 1;
        html.append(LS + "<p><table border=1>" + LS); // NOI18N

        for (List<String> item : myItems) {
            if (item == null) {
                html.append("</table>" + LS); // NOI18N
                html.append(LS + "<p><table border=1>" + LS); // NOI18N
                count = 1;
                continue;
            }
            html.append("<tr><td>" + (count++) + "</td>"); // NOI18N

            for (String value : item) {
                html.append(" <td>" + processBrackets(value) + "</td>"); // NOI18N
            }
            html.append("</tr>" + LS); // NOI18N
        }
        html.append("</table>" + LS + LS); // NOI18N
        html.append("</body>" + LS); // NOI18N
        html.append("</html>" + LS); // NOI18N

        writeToFile(file, html.toString());
    }

    private void writeToFile(File file, String text) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(text.getBytes());
            outputStream.close();
        } catch (IOException e) {
            printError(i18n("ERR_Can_not_Write_to_File", file.getAbsolutePath())); // NOI18N
            show();
            return;
        }
        if (myRunBrowser.isSelected()) {
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(file.toURI().toURL());
            } catch (MalformedURLException e) {
                printError(i18n("ERR_Can_not_Open_File", file.getAbsolutePath())); // NOI18N
            }
        }
    }

    @Override
    protected final DialogDescriptor createDescriptor() {
        myDescriptor = new DialogDescriptor(
            createPanel(),
            i18n("LBL_Export_Title"), // NOI18N
            true,
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    close();
                }
            }
        );
        return myDescriptor;
    }

    private String getResultFolder() {
        return UH + FS + SE + FS + RE;
    }

    @Override
    protected final void opened() {
        myFileName.requestFocus();
    }

    private void close() {
        if (myDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }
        String name = myFileName.getText().toLowerCase();

        if (!name.endsWith(HTM_EXT) && !name.endsWith(HTML_EXT)) {
            name += HTML_EXT;
        }
        checkFolder(new File(name).getParentFile());
        createFile(name);
    }

    private String processBrackets(String value) {
        return processLBrackets(processRBrackets(value));
    }

    private String processBrackets(String value, String text, String replace) {
        if (value == null) {
            return null;
        }
        int index = value.indexOf(text);

        if (index == -1) {
            return value;
        }
        return value.substring(0, index) + replace + processBrackets(value.substring(index + 1), text, replace);
    }

    private String processLBrackets(String value) {
        return processBrackets(value, "<", "&lt;"); // NOI18N
    }

    private String processRBrackets(String value) {
        return processBrackets(value, ">", "&gt;"); // NOI18N
    }

    private String myTitle;
    private JTextField myFileName;
    private JCheckBox myRunBrowser;
    private JTextArea myDescription;
    private List<List<String>> myItems;
    private DialogDescriptor myDescriptor;
    private static final String SE = "search"; // NOI18N
    private static final String RE = "result.html"; // NOI18N
    private static final String HTM_EXT = ".htm"; // NOI18N
    private static final String HTML_EXT = ".html"; // NOI18N
    private static final int TEXT_WIDTH = 30;
    private static final int TEXT_HEIGHT = 10;
}
