/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.hudson.ui.nodes;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.ui.actions.ShowBuildConsole;
import org.netbeans.modules.hudson.ui.actions.ShowChanges;
import org.netbeans.modules.hudson.ui.actions.ShowFailures;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

class HudsonJobBuildNode extends AbstractNode {

    private static final String ICON_BASE_RED = "org/netbeans/modules/hudson/ui/resources/red.png";
    private static final String ICON_BASE_RED_RUN = "org/netbeans/modules/hudson/ui/resources/red_run.png";
    private static final String ICON_BASE_BLUE = "org/netbeans/modules/hudson/ui/resources/blue.png";
    private static final String ICON_BASE_BLUE_RUN = "org/netbeans/modules/hudson/ui/resources/blue_run.png";
    private static final String ICON_BASE_YELLOW = "org/netbeans/modules/hudson/ui/resources/yellow.png";
    private static final String ICON_BASE_YELLOW_RUN = "org/netbeans/modules/hudson/ui/resources/yellow_run.png";
    private static final String ICON_BASE_GREY = "org/netbeans/modules/hudson/ui/resources/grey.png";

    private final HudsonJobBuild build;
    private final String htmlDisplayName;

    public HudsonJobBuildNode(HudsonJobBuild build) {
        super(makeChildren(build), Lookups.singleton(build));
        setName(Integer.toString(build.getNumber()));
        setDisplayName("#" + build.getNumber());
        Result result;
        if (build.isBuilding()) {
            switch (build.getJob().getColor()) {
            case blue:
            case blue_anime:
                result = Result.SUCCESS;
                break;
            case yellow:
            case yellow_anime:
                result = Result.UNSTABLE;
                break;
            case red:
            case red_anime:
                result = Result.FAILURE;
                break;
            default:
                result = Result.NOT_BUILT;
            }
        } else {
            result = build.getResult();
        }
        switch (result) {
        case SUCCESS:
            setIconBaseWithExtension(build.isBuilding() ? ICON_BASE_BLUE_RUN : ICON_BASE_BLUE);
            htmlDisplayName = getDisplayName();
            break;
        case UNSTABLE:
            setIconBaseWithExtension(build.isBuilding() ? ICON_BASE_YELLOW_RUN : ICON_BASE_YELLOW);
            htmlDisplayName = "<font color='#989800'>" + getDisplayName() + "</font>"; // NOI18N
            break;
        case FAILURE:
            setIconBaseWithExtension(build.isBuilding() ? ICON_BASE_RED_RUN : ICON_BASE_RED);
            htmlDisplayName = "<font color='#A40000'>" + getDisplayName() + "</font>"; // NOI18N
            break;
        default:
            setIconBaseWithExtension(ICON_BASE_GREY);
            htmlDisplayName = getDisplayName();
        }
        this.build = build;
    }

    public @Override String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public @Override Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new ShowChanges(build));
        actions.add(new ShowBuildConsole(build));
        if (build.getResult() == HudsonJobBuild.Result.UNSTABLE) {
            actions.add(new ShowFailures(build));
        }
        actions.add(null);
        actions.add(SystemAction.get(OpenUrlAction.class));
        return actions.toArray(new Action[actions.size()]);
    }

    private static Children makeChildren(final HudsonJobBuild build) {
        return Children.create(new ChildFactory<Object>() {
            final Object ARTIFACTS = new Object();
            protected boolean createKeys(List<Object> toPopulate) {
                // XXX is it possible to cheaply check in advance if the build has any artifacts?
                toPopulate.add(ARTIFACTS);
                return true;
            }
            protected @Override Node createNodeForKey(Object key) {
                assert key == ARTIFACTS : key;
                return new HudsonArtifactsNode(build);
            }
        }, false);
    }

}
