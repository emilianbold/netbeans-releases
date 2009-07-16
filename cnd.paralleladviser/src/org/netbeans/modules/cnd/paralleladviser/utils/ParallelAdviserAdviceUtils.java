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

package org.netbeans.modules.cnd.paralleladviser.utils;

import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import org.openide.awt.HtmlBrowser;

/**
 * Advice utils.
 *
 * @author Nick Krasilnikov
 */
public class ParallelAdviserAdviceUtils {

    private ParallelAdviserAdviceUtils() {
    }

    public static JComponent createAdviceComponent(URL icon, String text) {

        JEditorPane jEditorPane = createJEditorPane("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" + // NOI18N
                "        <tr bgcolor=\"#c7e3e8\">" + // NOI18N
                "            <td><img src=\"http://netbeans.org/images/v6/trails/trails-box-tl.png\" height=\"21\" width=\"7\"></td>" + // NOI18N
                "            <td colspan=\"2\" width=\"800\" style=\"font-size:1.1em;color:#0e1b55;\">&nbsp;&nbsp;<b>Loop for parallelization has been found</b></td>" + // NOI18N
                "            <td><img src=\"http://netbeans.org/images/v6/trails/trails-box-tr.png\" height=\"21\" width=\"7\"></td>" + // NOI18N
                "        </tr>" + // NOI18N
                "    </table>" + // NOI18N
                "    <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" + // NOI18N
                "        <tr>" + // NOI18N
                "            <td bgcolor=\"#c7e3e8\" width=\"1\"></td>" + // NOI18N
                "            <td width=\"50\" style=\"margin:4px;\">" + // NOI18N
                "                <img src=\"" + icon + "\">" + // NOI18N
                "            </td>" + // NOI18N
                "            <td width=\"762\" style=\"margin:4px;\">" + // NOI18N
                text +
                "            </td>" + // NOI18N
                "            <td bgcolor=\"#c7e3e8\" width=\"1\"></td>" + // NOI18N
                "        </tr>" + // NOI18N
                "    </table>" + // NOI18N
                "    <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" + // NOI18N
                "        <tr>" + // NOI18N
                "            <td><img src=\"http://netbeans.org/images/v6/trails/trails-box-bl.png\" height=\"6\" width=\"7\"></td>" + // NOI18N
                "            <td colspan=\"2\" width=\"800\"><img src=\"http://netbeans.org/images/v6/trails/trails-box-bm.png\" height=\"6\" width=\"800\"></td>" + // NOI18N
                "            <td><img src=\"http://netbeans.org/images/v6/trails/trails-box-br.png\" height=\"6\" width=\"7\"></td>" + // NOI18N
                "        </tr>" + // NOI18N
                "    </table>", true); // NOI18N

        jEditorPane.setBackground(new java.awt.Color(255, 255, 255));
        jEditorPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        return jEditorPane;
    }

    public static JEditorPane createJEditorPane(String text, boolean needHTMLTags) {
        JEditorPane editorPane = new JEditorPane("text/html", needHTMLTags || !text.startsWith("<html>") ? "<html> <body bgcolor=\"#ffffff\">" + text + "</body></html>" : text);//NOI18N
        editorPane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(e.getURL());
                }
            }
        });

//        Font font = UIManager.getFont("Label.font");//NOI18N
//        String bodyRule = "body { font-family: " + font.getFamily() + "; " +//NOI18N
//                "font-size: " + font.getSize() + "pt; }";//NOI18N

        String rule1 = "body {margin:6px 6px 6px 6px;}"; // NOI18N
        ((HTMLDocument) editorPane.getDocument()).getStyleSheet().addRule(rule1);

        editorPane.setOpaque(false);
        editorPane.setEditable(false);
        return editorPane;
    }

}
