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

package org.netbeans.modules.kenai.collab.chat;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class LinkOtherFileAction extends AbstractAction {
    
    private JTextPane out;
    
    public LinkOtherFileAction(JTextPane out) {
        super(NbBundle.getMessage(LinkOtherFileAction.class, "LINK_OTHER_FILE"));
        this.out=out;
    }

    public void actionPerformed(ActionEvent e) {
        String outText = "FILE:foo/bar/Test.java:23";
        try {
            out.requestFocus();
            int caretPosition = out.getCaretPosition();
            out.getDocument().insertString( caretPosition, " " + outText + " ", null);
            out.setSelectionStart(caretPosition+6);
            out.setSelectionEnd(caretPosition+outText.length()+1);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
