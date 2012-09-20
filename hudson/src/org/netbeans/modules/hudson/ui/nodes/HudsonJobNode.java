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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.ui.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.constants.HudsonInstanceConstants;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.netbeans.modules.hudson.ui.actions.LogInAction;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.ui.actions.ProjectAssociationAction;
import org.netbeans.modules.hudson.ui.actions.StartJobAction;
import org.netbeans.modules.hudson.ui.actions.ViewConfigAction;
import org.netbeans.modules.hudson.ui.interfaces.OpenableInBrowser;
import static org.netbeans.modules.hudson.ui.nodes.Bundle.*;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Describes HudsonJob in the RuntimeTab
 *
 * @author Michal Mocnak
 */
public class HudsonJobNode extends AbstractNode {
    
    private String htmlDisplayName;
    private HudsonJob job;
    private PropertyChangeListener watchedListener;
    
    public HudsonJobNode(HudsonJob job) {
        super(makeChildren(job), Lookups.singleton(job));
        setName(job.getName());
        setHudsonJob(job);
        setWatchedListener();
    }

    private static Children makeChildren(final HudsonJob job) {
        if (job.getColor() == Color.secured) {
            return Children.LEAF;
        }
        return Children.create(new ChildFactory<Object>() {
            final Object WORKSPACE = new Object();
            protected @Override boolean createKeys(List<Object> toPopulate) {
                // XXX would be nicer to avoid adding this in case there is no remote workspace...
                toPopulate.add(WORKSPACE);
                for (HudsonJobBuild b : job.getBuilds()) {
                    // Processing one build at a time, make sure its result is known (blocking call).
                    b.getResult();
                    toPopulate.add(b);
                }
                return true;
            }
            protected @Override Node createNodeForKey(Object key) {
                if (key == WORKSPACE) {
                    return new HudsonWorkspaceNode(job);
                } else {
                    return new HudsonJobBuildNode((HudsonJobBuild) key);
                }
            }
        }, true);
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        if (job.getColor() == Color.secured) {
            return new Action[] {new LogInAction((HudsonInstanceImpl) job.getInstance())};
        }
        List<Action> actions = new ArrayList<Action>();
        actions.add(SystemAction.get(StartJobAction.class));
        actions.add(new ProjectAssociationAction(job));
        actions.add(null);
        if (job instanceof OpenableInBrowser) {
            actions.add(OpenUrlAction.forOpenable((OpenableInBrowser) job));
        }
        actions.add(new ViewConfigAction(job));
        actions.add(SystemAction.get(PropertiesAction.class));
        return actions.toArray(new Action[actions.size()]);
    }
    
    @Override
    protected Sheet createSheet() {
        // Create a property sheet
        Sheet s = super.createSheet();
        
        // Put properties in
        s.put(((HudsonJobImpl) job).getSheetSet()); // XXX is cast necessary?
        
        return s;
    }
    
    private void setWatchedListener() {
        if (job instanceof HudsonJobImpl) {
            final HudsonJobImpl jobImpl = (HudsonJobImpl) job;
            watchedListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(
                            HudsonInstanceConstants.INSTANCE_SUPPRESSED_JOBS)) {
                        setHudsonJob(HudsonJobNode.this.job);
                    }
                }
            };
            jobImpl.getInstance().getProperties().addPropertyChangeListener(
                    WeakListeners.propertyChange(watchedListener,
                    jobImpl.getInstance().getProperties()));
        }
    }

    @Messages({
        "HudsonJobNode.running=(running)",
        "HudsonJobNode.in_queue=(in queue)",
        "HudsonJobNode.secured=(secured)",
        "HudsonJobNode.not_watched=(not watched)"
    })
    private void setHudsonJob(HudsonJob job) {
        this.job = job;
        Color color = job.getColor();
        setShortDescription(job.getUrl());

        // XXX #159836: tooltips
        setIconBaseWithExtension(color.iconBase());

        String oldHtmlDisplayName = getHtmlDisplayName();
        try {
            String escapedName = XMLUtil.toElementContent(job.getDisplayName());
            htmlDisplayName = job.isSalient()
                    ? color.colorizeDisplayName(escapedName)
                    : toGray(escapedName);
        } catch (CharConversionException ex) {
            assert false : ex;
            return;
        }
        if (!job.isSalient()) {
            htmlDisplayName += toGrayWithSpace(HudsonJobNode_not_watched());
        }
        if (color.isRunning()) {
            htmlDisplayName += toGrayWithSpace(HudsonJobNode_running());
        }
        if (color == Color.secured) {
            htmlDisplayName += toGrayWithSpace(HudsonJobNode_secured());
        }
        if (job.isInQueue()) {
            htmlDisplayName += toGrayWithSpace(HudsonJobNode_in_queue());
        }
        fireDisplayNameChange(oldHtmlDisplayName, htmlDisplayName);
    }

    private String toGray(String text) {
        return "<font color='!controlShadow'>" + text + "</font>";      //NOI18N
    }

    private String toGrayWithSpace(String text) {
        return " " + toGray(text);                                      //NOI18N
    }
}
