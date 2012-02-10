/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.ui;

import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays DOM Tree.
 * 
 * @author Jan Stola
 */
@TopComponent.Description(
        preferredID = DomTC.ID,
        persistenceType = TopComponent.PERSISTENCE_ALWAYS,
        iconBase = ElementNode.ICON_BASE)
@TopComponent.Registration(
        mode = "navigator", // NOI18N
        position = 600,
        openAtStartup = false)
@ActionID(
        category = "Window", // NOI18N
        id = "org.netbeans.modules.web.inspect.ui.DomTC") // NOI18N
@ActionReference(
        path = "Menu/Window/Navigator", // NOI18N
        position = 600)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DomAction", // NOI18N
        preferredID = "DomTC") // NOI18N
@NbBundle.Messages({
    "CTL_DomAction=DOM Tree", // NOI18N
    "CTL_DomTC=DOM Tree", // NOI18N
    "HINT_DomTC=This window shows a DOM Tree" // NOI18N
})
public final class DomTC extends TopComponent {
    /** TopComponent ID. */
    public static final String ID = "DomTC"; // NOI18N
    /** Panel shown in this {@code TopComponent}. */
    private DomPanel domPanel;

    /**
     * Creates a new {@code DomTC}.
     */
    public DomTC() {
        initComponents();
        setName(Bundle.CTL_DomTC());
        setToolTipText(Bundle.HINT_DomTC());
        Lookup lookup = ExplorerUtils.createLookup(domPanel.getExplorerManager(), getActionMap());
        associateLookup(lookup);
    }

    /**
     * Initializes the components in this {@code TopComponent}.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        domPanel = new DomPanel();
        add(domPanel);
    }

}
