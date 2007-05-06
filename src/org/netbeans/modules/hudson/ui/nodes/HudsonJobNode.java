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

package org.netbeans.modules.hudson.ui.nodes;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.ui.actions.ShowJobDetailAction;
import org.netbeans.modules.hudson.ui.actions.StartJobAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
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
    private HudsonJobImpl job;
    
    public HudsonJobNode(HudsonJobImpl job) {
        super(Children.LEAF, Lookups.singleton(job));
        
        setHudsonJob(job);
    }
    
    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action [] {
            SystemAction.get(ShowJobDetailAction.class),
            SystemAction.get(StartJobAction.class),
            null,
            SystemAction.get(OpenUrlAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }
    
    @Override
    public Action getPreferredAction() {
        return SystemAction.get(ShowJobDetailAction.class);
    }
    
    @Override
    public Transferable drag() throws IOException {
        return NodeTransfer.transferable(this, NodeTransfer.DND_COPY);
    }
    
    @Override
    public PasteType getDropType(Transferable arg0, int arg1, int arg2) {
        return super.getDropType(arg0, arg1, arg2);
    }
    
    @Override
    protected Sheet createSheet() {
        // Create a property sheet
        Sheet s = super.createSheet();
        
        // Put properties in
        s.put(job.getSheetSet());
        
        return s;
    }
    
    private void refreshState() {
        // Store old html name
        String oldHtmlDisplayName = getHtmlDisplayName();
        
        // Set new node data
        htmlDisplayName = job.getDisplayName();
        color = job.getColor();
        setShortDescription(job.getUrl());
        
        // Decorate node
        switch(color) {
        case red:
            setIconBaseWithExtension(ICON_BASE_RED);
            htmlDisplayName = "<font color=\"#A40000\">"+job.getDisplayName()+"</font>";
            break;
        case red_anime:
            setIconBaseWithExtension(ICON_BASE_RED_RUN);
            htmlDisplayName = "<b><font color=\"#A40000\">"+job.getDisplayName()+"</font></b>";
            break;
        case blue:
            setIconBaseWithExtension(ICON_BASE_BLUE);
            break;
        case blue_anime:
            setIconBaseWithExtension(ICON_BASE_BLUE_RUN);
            htmlDisplayName = "<b>"+job.getDisplayName()+"</b>";
            break;
        case yellow:
            setIconBaseWithExtension(ICON_BASE_YELLOW);
            break;
        case yellow_anime:
            setIconBaseWithExtension(ICON_BASE_YELLOW_RUN);
            htmlDisplayName = "<b>"+job.getDisplayName()+"</b>";
            break;
        case grey:
            setIconBaseWithExtension(ICON_BASE_GREY);
            break;
        case grey_anime:
            setIconBaseWithExtension(ICON_BASE_GREY_RUN);
            htmlDisplayName = "<b>"+job.getDisplayName()+"</b>";
            break;
        }
        
        // Fire changes if any
        fireDisplayNameChange(oldHtmlDisplayName, getHtmlDisplayName());
    }
    
    public void setHudsonJob(HudsonJobImpl job) {
        this.job = job;
        
        // Refresh
        refreshState();
    }
    
    public HudsonJobImpl getJob() {
        return job;
    }
}