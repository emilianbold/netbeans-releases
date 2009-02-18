/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * NewJPanel.java
 *
 * Created on Dec 8, 2008, 3:24:21 PM
 */

package org.netbeans.modules.kenai.collab.notifications;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.html.HTMLDocument;
import org.netbeans.modules.notifications.spi.Notification;
import org.openide.util.ImageUtilities;

/**
 * Panel showing Notifications
 * @see Notification
 * @author Jan Becicka
 */
public class NotificationPanel extends javax.swing.JPanel {

    GridLayout layout = new GridLayout();

    /** Creates new form NewJPanel */
    public NotificationPanel() {
        layout.setColumns(1);
        layout.setRows(0);
        setLayout(layout);
    }

    public void addNotification(final Notification n) {
        layout.setRows(layout.getRows()+1);
        final JEditorPane pane = new JEditorPane();
        final JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BorderLayout());

        pane.setOpaque(false);
        pane.setContentType("text/html");

        // add a CSS rule to force body tags to use the default label font
        // instead of the value in javax.swing.text.html.default.csss
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)pane.getDocument()).getStyleSheet().addRule(bodyRule);

        pane.setBorder(null);
        pane.setEditable(false);

        pane.putClientProperty(BasicHTML.documentBaseKey, NotificationPanel.class.getResource("/org/netbeans/modules/kenai/collab/resources"));
        String desc= n.getDescription();
        String description = desc!=null&&desc.length()!=0?"<br>" + desc:"";
        pane.setText("<html>"+n.getTitle() + description + "<br><a href=\""+n.getLinkTitle()+"\">"+n.getLinkTitle()+"</a><br><br></html>");
        pane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    n.showDetails();
                }
            }
        });
        row.add(pane, BorderLayout.CENTER);
        final JButton close = new JButton(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/close-normal.gif")));
        close.setOpaque(false);
        close.setBorder(null);
        close.setMargin(new Insets(0, 0, 0, 0));
        close.setRolloverEnabled(true);
        close.setRolloverIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/close-rollover.gif")));
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NotificationPanel.this.remove(row);
                layout.setRows(layout.getRows() - 1);
                NotificationPanel.this.getParent().validate();
                n.remove();
            }
        });
        JPanel closePane = new JPanel();
        closePane.setOpaque(false);
        closePane.setLayout(new BorderLayout());
        closePane.add(close, BorderLayout.NORTH);
        final JPanel fake = new JPanel();
        fake.setOpaque(false);
        closePane.add(fake, BorderLayout.CENTER);
        row.add(closePane, BorderLayout.EAST);
        row.add(new JLabel(" ",n.getIcon(), JLabel.HORIZONTAL), BorderLayout.WEST);
        this.add(row);
        validate();

    }

}
