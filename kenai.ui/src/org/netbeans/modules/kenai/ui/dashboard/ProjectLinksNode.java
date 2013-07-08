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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.dashboard;

import org.netbeans.modules.team.ui.common.LinkButton;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.chat.MessagingAccessorImpl;
import org.netbeans.modules.kenai.ui.ProjectAccessorImpl;
import org.openide.util.ImageUtilities;
import org.netbeans.modules.team.ui.spi.MessagingAccessor;
import org.netbeans.modules.team.ui.spi.MessagingHandle;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.util.treelist.AsynchronousNode;
import org.netbeans.modules.team.ui.util.treelist.TreeLabel;
import org.netbeans.modules.team.ui.util.treelist.TreeListNode;
import org.openide.util.NbBundle;

/**
 * Node showing links to project's wiki, downloads and messages.
 *
 * @author S. Aubrecht
 * @author Jan Becicka
 */
public class ProjectLinksNode extends AsynchronousNode<MessagingHandle>  {

    private final ProjectHandle<KenaiProject> project;
    private ProjectLinksPanel panel;
    private final Object LOCK = new Object();

    public ProjectLinksNode( TreeListNode parent, ProjectHandle<KenaiProject> project ) {
        super(false, parent, null);
        this.project = project;
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        if( panel == component ) {
            synchronized( LOCK ) {
                panel.configure(component, foreground, background, isSelected, hasFocus, rowWidth);
            }
        }
    }

    @Override
    protected JComponent createComponent( MessagingHandle data ) {
        synchronized ( LOCK ) {
            panel = new ProjectLinksPanel(project, this);
            return panel;
        }
    }

    void refreshNode() {
        super.refresh();
    }
    
    @Override
    protected MessagingHandle load() {
        return MessagingAccessorImpl.getDefault().getMessaging(project);
    }

    @Override
    protected void dispose() {
        super.dispose();
        panel.dispose();
    }

    @Override
    protected List<TreeListNode> createChildren() {
        return Collections.emptyList();
    }
}
