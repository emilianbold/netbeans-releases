/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.node;

import java.util.concurrent.Callable;
import org.netbeans.modules.docker.ContainerStatus;
import org.netbeans.modules.docker.DockerContainer;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Hejl
 */
public abstract class AbstractContainerAction extends NodeAction {

    private final String name;
    
    private final boolean refresh;

    private final ContainerStatus result;

    public AbstractContainerAction(String name, boolean refresh, ContainerStatus result) {
        this.name = name;
        this.refresh = refresh;
        this.result = result;
    }

    protected abstract void performAction(DockerContainer container);

    protected boolean isEnabled(DockerContainer container) {
        return true;
    }

    @Override
    protected final void performAction(Node[] activatedNodes) {
        for (final Node node : activatedNodes) {
            final DockerContainer container = node.getLookup().lookup(DockerContainer.class);
            if (container != null) {
                UiUtils.performRemoteAction(name, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        performAction(container);
                        return null;
                    }
                }, refresh ? new Runnable() {
                    @Override
                    public void run() {
                        if (result != null) {
                            container.setStatus(result);
                        } else {
                            // XXX should we response to other nodes
                            Node parent = node.getParentNode();
                            if (parent != null) {
                                Refreshable refreshable = parent.getLookup().lookup(Refreshable.class);
                                if (refreshable != null) {
                                    refreshable.refresh();
                                }
                            }
                        }
                    }
                } : null);
            }
        }
    }

    @Override
    protected final boolean enable(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            DockerContainer container = node.getLookup().lookup(DockerContainer.class);
            if (container == null) {
                return false;
            } else {
                if (!isEnabled(container)) {
                    return false;
                }
            }
        }
        return true;
    }

    public final String getName() {
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
