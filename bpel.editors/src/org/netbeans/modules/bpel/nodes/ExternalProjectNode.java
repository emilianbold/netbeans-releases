/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * This node is intended to represent a project.
 *
 * @author Nikita Krjukov
 */
public class ExternalProjectNode extends BpelNode<Project> {

    public ExternalProjectNode(Project proj, Children children, Lookup lookup) {
        super(proj, children, lookup);
    }

    @Override
    public Image getIcon(int type) {
        ProjectInformation info = 
                getReference().getLookup().lookup(ProjectInformation.class);
        if (info != null) {
            Icon icon = info.getIcon();
            if (icon != null && icon instanceof ImageIcon) {
                return ((ImageIcon)icon).getImage();

            }
        }
        return null;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getName() {
        ProjectInformation info =
                getReference().getLookup().lookup(ProjectInformation.class);
        if (info != null) {
            String name = info.getName();
            return name;
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        ProjectInformation info =
                getReference().getLookup().lookup(ProjectInformation.class);
        if (info != null) {
            String name = info.getDisplayName();
            return name;
        }
        return null;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.PROJECT;
    }



}
