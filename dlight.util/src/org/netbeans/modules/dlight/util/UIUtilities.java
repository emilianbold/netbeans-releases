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

package org.netbeans.modules.dlight.util;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author mt154047
 */
public final class UIUtilities {
    
      public static JEditorPane createJEditorPane(String text, boolean needHTMLTags){
        JEditorPane editorPane = new JEditorPane("text/html", needHTMLTags || !text.startsWith("<html>") ?  "<html><center>" + text + "</center></html>" : text);//NOI18N
        editorPane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
                    HtmlBrowser.URLDisplayer.getDefault().showURL(e.getURL());
                }
            }
        });
         Font font = UIManager.getFont("Label.font");//NOI18N
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +//NOI18N
                "font-size: " + font.getSize() + "pt; }";//NOI18N
        ((HTMLDocument)editorPane.getDocument()).getStyleSheet().addRule(bodyRule);
        editorPane.setOpaque(false);
        editorPane.setEditable(false);
        return editorPane;
    }

      public static JEditorPane createJEditorPane(String text, boolean needHTMLTags, Color color){
        JEditorPane editorPane = new JEditorPane("text/html", needHTMLTags || !text.startsWith("<html>") ?  "<html><center>" + text + "</center></html>" : text);//NOI18N
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
                    HtmlBrowser.URLDisplayer.getDefault().showURL(e.getURL());
                }
            }
        });
         Font font = UIManager.getFont("Label.font");//NOI18N
        String bodyRule = "body { font-family: " + font.getFamily() + "; " + //NOI18N
                "font-size: " + font.getSize() + "pt; color: " + Integer.toHexString( color.getRGB() & 0x00ffffff )+ "; }"; //NOI18N
        ((HTMLDocument)editorPane.getDocument()).getStyleSheet().addRule(bodyRule);
        editorPane.setOpaque(false);
        editorPane.setEditable(false);
        return editorPane;
    }


}
