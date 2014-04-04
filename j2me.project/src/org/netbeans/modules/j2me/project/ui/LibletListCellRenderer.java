/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.ui;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.j2me.project.ui.customizer.LibletInfo;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.openide.awt.HtmlRenderer;
import org.openide.util.NbBundle;

/**
 *
 * @author rsvitanic
 */
public class LibletListCellRenderer implements ListCellRenderer {

    private final ListCellRenderer delegate;

    public LibletListCellRenderer() {
        delegate = HtmlRenderer.createRenderer();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String displayText = null;
        if (value instanceof LibletInfo) {
            LibletInfo li = (LibletInfo) value;
            String libletType = NbBundle.getMessage(LibletListCellRenderer.class, "TXT_Liblets_TypeUnknown"); //NOI18N
            if (li.getType() != LibletInfo.LibletType.LIBLET) {
                displayText = li.getName() + " (" + li.getType().toString().toLowerCase() + ")"; //NOI18N
            } else {
                switch (li.getItem().getType()) {
                    case ClassPathSupport.Item.TYPE_JAR:
                        libletType = NbBundle.getMessage(LibletListCellRenderer.class, "TXT_Liblets_TypeJar"); //NOI18N
                        break;
                    case ClassPathSupport.Item.TYPE_ARTIFACT:
                        libletType = NbBundle.getMessage(LibletListCellRenderer.class, "TXT_Liblets_TypeProject"); //NOI18N
                        break;
                }
                displayText = li.getName() + " (" + libletType + ")"; //NOI18N
            }
        }

        return delegate.getListCellRendererComponent(list, displayText, index, isSelected, cellHasFocus);
    }

}
