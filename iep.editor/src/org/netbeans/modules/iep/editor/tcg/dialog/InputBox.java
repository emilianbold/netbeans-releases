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

package org.netbeans.modules.iep.editor.tcg.dialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.iep.editor.designer.JTextFieldFilter;

/**
 *  DOCUMENT ME!
 *
 * @author    Bing Lu
 */
public class InputBox
         implements OKCancelDialogInterface, ActionListener {

    
    private String mMsg;

    private String mTitle;

    private JTextField mTextBox;

    private boolean mOk = false;

    private JPanel mPanel; 

    /**
     *  DOCUMENT ME!
     *
     * @param  title  The title
     * @param  msg    The message
     */
    public InputBox(String title, String msg, JTextFieldFilter filer) {
        mMsg = msg;
        mTitle = title;
        mPanel = new JPanel();
        mPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        mTextBox = new JTextField();
        int pH = (int)mTextBox.getPreferredSize().getHeight();
        mTextBox.setPreferredSize(new Dimension(180, pH));
        mTextBox.setDocument(filer);
        mPanel.add(mTextBox, c);
        if (msg != null && !msg.trim().equals("")) {
            c.gridy = 1;
            c.insets.top = 0;
            JLabel label = new JLabel(mMsg);
            mPanel.add(label, c);
        }
    }

    public InputBox(String title, String msg) {
        this(title, msg, JTextFieldFilter.newAlphaNumeric());
    }

    /**
     *  DOCUMENT ME!
     *
     * @return    The actionListener value
     */
    public ActionListener getActionListener() {
        return this;
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The innerPane value
     */
    public Component getInnerPane() {
        return mPanel;
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The ok value
     */
    public boolean isOk() {
        return mOk;
    }


    /**
     *  gets the text
     *
     * @return    the text.
     */
    public String getText() {
        return mTextBox.getText();
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The title value
     */
    public String getTitle() {
        return mTitle;
    }


    /**
     *  DOCUMENT ME!
     *
     * @param  e  the action event
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("OK")) {
            mOk = true;

        } else {
            mOk = false;
        }
    }
}

