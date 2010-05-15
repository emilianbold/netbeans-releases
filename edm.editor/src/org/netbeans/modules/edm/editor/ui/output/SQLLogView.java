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
package org.netbeans.modules.edm.editor.ui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import java.util.logging.Logger;
import org.netbeans.modules.edm.editor.graph.components.BasicToolBar;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Scrollable view for text output. Useful for log traces, etc.
 * 
 * @author Ritesh Adval
 */
public class SQLLogView extends TopComponent implements IMessageView {

    private JTextArea textArea;
    private JButton refreshButton;
    private ActionListener aListener;
    private JButton[] btn = new JButton[1];
    private static transient final Logger mLogger = Logger.getLogger(SQLLogView.class.getName());

    /** Creates a new instance of SQLLogView */
    public SQLLogView() {
        String viewLabel = NbBundle.getMessage(SQLLogView.class, "LBL_logview_tab");
        this.setName(viewLabel);
        this.setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());

        //add refresh button
        URL url = getClass().getResource("/org/netbeans/modules/edm/editor/resources/rerun.png");
        refreshButton = new JButton(new ImageIcon(url));
        refreshButton.setToolTipText(NbBundle.getMessage(SQLLogView.class, "TOOLTIP_Refresh_Log"));
        refreshButton.getAccessibleContext().setAccessibleName(viewLabel);
        refreshButton.addActionListener(aListener);
        BasicToolBar.processButton(refreshButton);
        btn[0] = refreshButton;

        ActionListener refreshListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object src = e.getSource();
                if (src.equals(refreshButton)) {
                    validate();
                }
            }
        };
        textArea = new JTextArea();
        textArea.setBackground(Color.white);
        textArea.setBorder(BorderFactory.createEmptyBorder());
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        textArea.setText("");

        JScrollPane sPane = new JScrollPane(textArea);
        this.add(sPane, BorderLayout.CENTER);
    }

    public void appendToView(String logMessage) {
        append(logMessage);
    }

    public synchronized void clearView() {
        textArea.setText("");
        adjustViewport();
    }

    /**
     * Refreshes view with the given string.
     * 
     * @param newStr String to refresh with
     */
    public synchronized void refreshView(String newStr) {
        textArea.setText(newStr);
        adjustViewport();
    }

    /**
     * Adjusts viewport to show data at bottom of screen.
     */
    private void adjustViewport() {
        // setCaretPosition() and scrollRectToVisible() are not thread-safe,
        // so make sure they are executed on the event dispatch thread.
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    adjustViewport();
                }
            });
        } else {
            textArea.setCaretPosition(textArea.getText().length());
            textArea.scrollRectToVisible(new Rectangle(0, Math.max(0, textArea.getHeight() - 2), 1, 1));
        }
    }

    /**
     * Appends given string to the text area, and adjusts viewport if necessary to ensure
     * its visibility.
     * 
     * @param msg String to be appended to text area.
     */
    private synchronized void append(String msg) {
        textArea.append(msg);
        adjustViewport();
    }

    public JButton[] getVerticalToolBar() {
        return btn;
    }
}

