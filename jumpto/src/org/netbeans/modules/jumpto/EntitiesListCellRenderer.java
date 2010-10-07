/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jumpto;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;

/**
 * An abstraction that helps to define a list cell renderer used for rendering
 * info about the entities (i.e. files, types, symbols etc.) that
 * belong to the projects.
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 */
public abstract class EntitiesListCellRenderer  extends DefaultListCellRenderer
                                                implements ChangeListener {
    
    // PENDING: Move common functionality of the renders into this class

    private final
            String mainProjectName = EntityComparator.getMainProjectName();

    protected void setProjectName(JLabel jlPrj, String projectName) {
        if(isMainProject(projectName)) {
            jlPrj.setText(getBoldText(projectName));
        } else {
            jlPrj.setText(projectName);
        }
    }

    private String getBoldText(String text) {
        StringBuilder sb = new StringBuilder("<html><b>"); // NOI18N
        sb.append(text);
        sb.append("</b></html>"); // NOI18N
        return sb.toString();
    }

    private boolean isMainProject(String projectName) {
        return projectName != null && projectName.equals(mainProjectName) ?
            true : false;
    }

}
