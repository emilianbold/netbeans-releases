/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 * This API displays the web service and client nodes in this project.
 * @author Ajit Bhate
 */
public abstract class ProjectWebServiceView {

    private static final Lookup.Result<ProjectWebServiceViewProvider> implementations;
    

    static {
        implementations = Lookup.getDefault().lookup(new Lookup.Template<ProjectWebServiceViewProvider>(ProjectWebServiceViewProvider.class));
    }

    /** 
     * View Type: Service or Client
     */
    public static enum ViewType {

        SERVICE, CLIENT
    };
    private ChangeSupport serviceListeners,  clientListeners;

    protected ProjectWebServiceView() {
        serviceListeners = new ChangeSupport(this);
        clientListeners = new ChangeSupport(this);
    }

    protected void addChangeListener(ChangeListener l, ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                serviceListeners.addChangeListener(l);
                break;
            case CLIENT:
                clientListeners.addChangeListener(l);
                break;
        }
    }

    protected void removeChangeListener(ChangeListener l, ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                if (serviceListeners != null) {
                    serviceListeners.removeChangeListener(l);
                }
                break;
            case CLIENT:
                if (clientListeners != null) {
                    clientListeners.removeChangeListener(l);
                }
                break;
        }
    }

    protected void fireChange(ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                serviceListeners.fireChange();
                return;
            case CLIENT:
                clientListeners.fireChange();
                return;
        }
    }

    /** 
     * Create view for given type (service or client)
     */
    protected abstract Node[] createView(ViewType viewType);

    /** 
     * If a view for given type (service or client) is empty.
     */
    protected abstract boolean isViewEmpty(ViewType viewType);

    /** 
     * Notify that this view is in use.
     * Subclasses may add listeners here
     */
    protected abstract void addNotify();

    /** 
     * Notify that this view is not in use.
     * Subclasses may remove listeners here.
     */
    protected abstract void removeNotify();

    /**
     * Returns lookup.result for ProjectWebServiceViewProviders
     * @return Lookup.Result<ProjectWebServiceViewProvider>.
     */
    static Lookup.Result<ProjectWebServiceViewProvider> getProviders() {
        return implementations;
    }

    /**
     * Creates WebServiceViews for given project.
     * @param project Project for which WebServiceViews are to be created.
     * @return list of WebServiceViews.
     */
    public static List<ProjectWebServiceView> createWebServiceViews(Project project) {
        Collection<? extends ProjectWebServiceViewProvider> providers = getProviders().allInstances();
        if (providers == null || providers.isEmpty()) {
            return Collections.<ProjectWebServiceView>emptyList();
        }
        List<ProjectWebServiceView> views = new ArrayList<ProjectWebServiceView>();
        for (ProjectWebServiceViewProvider provider : providers) {
            views.add(provider.createProjectWebServiceView(project));
        }
        return views;
    }

    /**
     * Get the web service nodes that are in the project. 
     * @param project Project that contains the web service nodes
     * @return Array of web service nodes in the project.
     */ 
    public Node[] getServiceNodes(Project project) {
        return createWebServiceNodes(project,ViewType.SERVICE);
    };
    
    /**
     * Get the web service client nodes that are in the project. 
     * @param project Project that contains the web service nodes
     * @return Array of web service client nodes in the project.
     */ 
    public Node[] getClientNodes(Project project) {
        return createWebServiceNodes(project,ViewType.CLIENT);
    };

    /**
     * Creates Web Service/client nodes for given project.
     * @param project Project for which Web service /client nodes to be created.
     * @param viewType type of nodes (service or client).
     * @return array of nodes representing Web Services/Clients in given project.
     */
    private static Node[] createWebServiceNodes(Project project, ViewType viewType) {
        List<ProjectWebServiceView> views = createWebServiceViews(project);
        List<Node> result = new ArrayList<Node>();
        for (ProjectWebServiceView view : views) {
            if (!view.isViewEmpty(viewType)) {
                result.addAll(Arrays.<Node>asList(view.createView(viewType)));
            }
        }
        return result.toArray(new Node[result.size()]);
    }
}
