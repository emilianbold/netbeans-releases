/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.documentation;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ToolTipManager;

import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

import org.netbeans.modules.bpel.design.decoration.components.AbstractGlassPaneButton;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.08.15
 */
final class DocumentationButton extends AbstractGlassPaneButton {

    public DocumentationButton(final UniqueId id, String text) {
        super(ICON, text, true, new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                String text = event.getSource().toString().trim();
                String documentation = getExtensibleElement(id).getDocumentation();
//out();
//out("text: '" + text + "'");

                try {
                    if (documentation == null || !text.equals(documentation.trim())) {
                        getExtensibleElement(id).setDocumentation(text);
                    }
//out("get: '" + getExtensibleElement(id).getDocumentation() + "'");
                } catch (VetoException e) {
                    e.printStackTrace();
                }
            }
        });
        myID = id;
        addTitle(ICON, TITLE, Color.BLUE);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public String getToolTipText() {
        String text = getExtensibleElement(myID).getDocumentation();

        if (text != null) {
            text = text.trim();

            if (text.length() > MAX_LENGHT) {
                text = text.substring(0, MAX_LENGHT) + "..."; // NOI18N
            }
            return "<html>" + text + "</html>"; // NOI18N
        }
        return null;
    }

    private static ExtensibleElements getExtensibleElement(UniqueId id) {
        return (ExtensibleElements) id.getModel().getEntity(id);
    }

    private UniqueId myID;
    private static final int MAX_LENGHT = 60;
    private static final String TITLE = i18n(DocumentationButton.class, "LBL_Documentation"); // NOI18N
    private static final Icon ICON = icon(DocumentationButton.class, "documentation"); // NOI18N
}
