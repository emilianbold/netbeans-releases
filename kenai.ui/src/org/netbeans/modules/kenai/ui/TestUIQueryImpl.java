/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.UIQuery.Type;
import org.netbeans.modules.kenai.ui.api.UIUtils;
import sun.awt.HorizBagLayout;

/**
 *
 * @author Jan Becicka
 */
public class TestUIQueryImpl implements UIQueryImpl {

    /**
     *
     * @param t
     * @param k
     * @return
     */
    public JComponent getComponent(Type t, KenaiProject k) {
        switch (t) {
            case BUILDS:
                JLabel title = new JLabel("Builds");
                title.setBackground(Color.WHITE);
                
                final JTextPane textPane = UIUtils.createHTMLPane();
                textPane.setText("<html><body> " +
                        "<table border=\"0\" borderwith=\"0\" width=\"80%\" align=\"left\" ><tbody>" +
                        "<tr>" +
                        "<td>" +
                        "MyApplication" +
                        "</td>" +
                        "<td class=\"green\">" +
                        "running" +
                        "</td>" +
                        "</tr>" +
                        "<tr>" +
                        "<td>" +
                        "SomeOtherApp" +
                        "</td>" +
                        "<td class=\"green\">" +
                        "stable" +
                        "</td>" +
                        "</tr>" +
                        "<tr>" +
                        "<td>" +
                        "AnotherApp" +
                        "</td>" +
                        "<td class=\"red\">" +
                        "failing" +
                        "</td>" +
                        "</tr>" +
                        "</tbody>" +
                        "</table>" +
                        "<a href=\"details\">See more...</a>" +
                        "</body>" +
                        "</html>");

                textPane.addHyperlinkListener(new HyperlinkListener() {
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                            JOptionPane.showMessageDialog(textPane, e.getDescription());
                    }
                });

                
                ExpandableWidget widget = new ExpandableWidget(title, textPane, true);
                return widget;

            case ISSUES:
            case REVIEWS:
            case SOURCES:
                return null;
        }
        return null;
    }

    /**
     *
     * @param t
     * @param k
     * @return
     */
    public Collection<LinkNode> getNodes(Type t, KenaiProject k) {
       ArrayList<LinkNode> result = new ArrayList<LinkNode>();
       result.add(new LinkNodeImpl("<html><b>MyApp</b>: running</html>"));
       result.add(new LinkNodeImpl("<html><b>OtherApp</b>: <a href=\"test\">failed</a></html>"));
       result.add(new LinkNodeImpl("<html>Issues <a href=\"68\">68</a>,<a href=\"new\">new</a></html>"));
       return result;
    }

    public class LinkNodeImpl implements LinkNode {
        private String html;

        public LinkNodeImpl(String text) {
            this.html=text;
        }

        public String getString() {
            return html;
        }

        public void handleLink(String link) {
            JOptionPane.showMessageDialog(null, link);
        }

        public Action[] getActions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String toString() {
            return html;
        }
    }
}
