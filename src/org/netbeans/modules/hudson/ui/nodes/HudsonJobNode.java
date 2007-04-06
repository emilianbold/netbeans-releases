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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.ui.nodes;

import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Describes HudsonJob in the RuntimeTab
 * 
 * @author Michal Mocnak
 */
public class HudsonJobNode extends AbstractNode {
    
    private static final String ICON_BASE_RED = "org/netbeans/modules/hudson/ui/resources/red.png";
    private static final String ICON_BASE_RED_RUN = "org/netbeans/modules/hudson/ui/resources/red_run.png";
    private static final String ICON_BASE_BLUE = "org/netbeans/modules/hudson/ui/resources/blue.png";
    private static final String ICON_BASE_BLUE_RUN = "org/netbeans/modules/hudson/ui/resources/blue_run.png";
    private static final String ICON_BASE_YELLOW = "org/netbeans/modules/hudson/ui/resources/yellow.png";
    private static final String ICON_BASE_YELLOW_RUN = "org/netbeans/modules/hudson/ui/resources/yellow_run.png";
    private static final String ICON_BASE_GREY = "org/netbeans/modules/hudson/ui/resources/grey.png";
    private static final String ICON_BASE_GREY_RUN = "org/netbeans/modules/hudson/ui/resources/grey_run.png";
    
    private String htmlDisplayName;
    private Color color;
    
    public HudsonJobNode(HudsonJobImpl job) {
        super(Children.LEAF, Lookups.singleton(job));
        
        setShortDescription(job.getUrl());
        
        htmlDisplayName = job.getName();
        color = job.getColor();
        
        switch(color) {
        case RED:
            setIconBaseWithExtension(ICON_BASE_RED);
            htmlDisplayName = "<font color=\"#A40000\">"+job.getName()+"</font>";
            break;
        case RED_ANIME:
            setIconBaseWithExtension(ICON_BASE_RED_RUN);
            htmlDisplayName = "<b><font color=\"#A40000\">"+job.getName()+"</font></b>";
            break;    
        case BLUE:
            setIconBaseWithExtension(ICON_BASE_BLUE);
            break;
        case BLUE_ANIME:
            setIconBaseWithExtension(ICON_BASE_BLUE_RUN);
            htmlDisplayName = "<b>"+job.getName()+"</b>";
            break;    
        case YELLOW:
            setIconBaseWithExtension(ICON_BASE_YELLOW);
            break;
        case YELLOW_ANIME:
            setIconBaseWithExtension(ICON_BASE_YELLOW_RUN);
            htmlDisplayName = "<b>"+job.getName()+"</b>";
            break;    
        case GREY:
            setIconBaseWithExtension(ICON_BASE_GREY);
            break;
        case GREY_ANIME:
            setIconBaseWithExtension(ICON_BASE_GREY_RUN);
            htmlDisplayName = "<b>"+job.getName()+"</b>";
            break;
        }
    }
    
    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action [] {
            SystemAction.get(OpenUrlAction.class)
        };
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenUrlAction.class);
    }
    
    public Color getColor() {
        return color;
    }
}