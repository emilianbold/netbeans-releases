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

package org.netbeans.modules.team.server.ui.common;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.openide.util.NbBundle;

/**
 * The one and only one selected project
 *
 * @author Tomas Stupka
 */
public class SelectedProjectNode extends LeafNode {

    private JPanel component;
    private String categoryName;
    private Icon icon;
    private JLabel lbl = null;
    private LinkButton btnPick;
    private final Action switchAction;
    
    public SelectedProjectNode( String name, Icon icon, Action switchAction) {
        super( null );
        this.categoryName = name;
        this.icon = icon;
        this.switchAction = switchAction;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int maxWidth) {
        if( null == component ) {
            component = new JPanel( new GridBagLayout() );
            component.setOpaque(false);
            lbl = new TreeLabel(categoryName);
            lbl.setIcon(icon);
            component.add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,3), 0,0) );

            btnPick = new LinkButton(getExpandedIcon(), switchAction); 
            btnPick.setToolTipText((String) switchAction.getValue(Action.NAME));
            btnPick.setRolloverEnabled(true);
            component.add( btnPick, new GridBagConstraints(1,0,1,1,1.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
        }
        lbl.setForeground(foreground);
        return component;
    }
    
    @Override
    protected Type getType() {
        return Type.NORMAL;
    }

    /**
     * Get the icon displayed by a expanded set. Typically this is just the
     * icon the look and feel supplies for trees
     */
    private static final org.netbeans.modules.team.commons.treelist.ColorManager colorManager = org.netbeans.modules.team.commons.treelist.ColorManager.getDefault();
    static Icon getExpandedIcon() {
        Icon expandedIcon = UIManager.getIcon(colorManager.isGtk() ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon"); //NOI18N
        assert expandedIcon != null : "no Tree.expandedIcon found"; //NOI18N
        return expandedIcon;
    }    
    
}
