/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.api.jstesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.jstesting.JsTestingProviderAccessor;
import org.netbeans.modules.web.clientproject.jstesting.SelectProviderPanel;
import org.netbeans.modules.web.clientproject.spi.jstesting.JsTestingProviderImplementation;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered JS testing providers. The path
 * for registration is "{@value #JS_TESTING_PATH}" on SFS.
 * <p>
 * This class is thread safe.
 * @since 1.49
 */
public final class JsTestingProviders {

    /**
     * Path on SFS for JS testing providers registrations.
     */
    public static final String JS_TESTING_PATH = "JS/Testing"; // NOI18N

    private static final Lookup.Result<JsTestingProviderImplementation> JS_TESTING_PROVIDERS = Lookups.forPath(JS_TESTING_PATH)
            .lookupResult(JsTestingProviderImplementation.class);
    private static final JsTestingProviders INSTANCE = new JsTestingProviders();

    private final List<JsTestingProvider> jsTestingProviders = new CopyOnWriteArrayList<>();

    static {
        JS_TESTING_PROVIDERS.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                INSTANCE.reinitProviders();
            }
        });
    }


    private JsTestingProviders() {
        initProviders();
    }

    /**
     * Get JsTestingProviders instance.
     * @return JsTestingProviders instance
     */
    public static JsTestingProviders getDefault() {
        return INSTANCE;
    }

    /**
     * Get list of all registered JS testing providers.
     * @return list of all registered JS testing providers, can be empty but never {@code null}
     */
    public List<JsTestingProvider> getJsTestingProviders() {
        return new ArrayList<>(jsTestingProviders);
    }

    /**
     * Show dialog for JS testing provider selection.
     * @return selected JS testing provider or {@code null} if none selected
     */
    @CheckForNull
    public JsTestingProvider selectJsTestingProvider() {
        return SelectProviderPanel.open();
    }

    public NodeFactory createJsTestingProvidersNodeFactory() {
        return new NodeFactory() {
            @Override
            public NodeList<?> createNodes(Project project) {
                return new ProxyNodeList(project);
            }
        };
    }

    private void initProviders() {
        assert jsTestingProviders.isEmpty() : "Empty providers expected but: " + jsTestingProviders;
        jsTestingProviders.addAll(map(JS_TESTING_PROVIDERS.allInstances()));
    }

    void reinitProviders() {
        synchronized (jsTestingProviders) {
            clearProviders();
            initProviders();
        }
    }

    private void clearProviders() {
        jsTestingProviders.clear();
    }

    //~ Mappers

    private Collection<JsTestingProvider> map(Collection<? extends JsTestingProviderImplementation> providers) {
        List<JsTestingProvider> result = new ArrayList<>();
        for (JsTestingProviderImplementation provider : providers) {
            result.add(JsTestingProviderAccessor.getDefault().create(provider));
        }
        return result;
    }

    //~ Inner classes

    private static final class ProxyNodeList implements NodeList<Node> {

        private final Project project;
        private final List<NodeList<Node>> nodeList = new CopyOnWriteArrayList<>();


        private ProxyNodeList(Project project) {
            assert project != null;
            this.project = project;
            // XXX listen on provider changes
            nodeList.addAll(initNodeList(project));
        }

        private List<NodeList<Node>> initNodeList(Project project) {
            List<NodeList<Node>> result = new ArrayList<>();
            for (JsTestingProvider provider : JsTestingProviders.getDefault().getJsTestingProviders()) {
                NodeList<Node> providerNodeList = provider.createNodeList(project);
                if (providerNodeList != null) {
                    result.add(providerNodeList);
                }
            }
            return result;
        }

        @Override
        public List<Node> keys() {
            List<Node> nodes = new ArrayList<>();
            for (NodeList<Node> list : nodeList) {
                nodes.addAll(list.keys());
            }
            return nodes;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            for (NodeList<Node> list : nodeList) {
                list.addChangeListener(listener);
            }
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            for (NodeList<Node> list : nodeList) {
                list.removeChangeListener(listener);
            }
        }

        @Override
        public Node node(Node node) {
            return node;
        }

        @Override
        public void addNotify() {
            for (NodeList<Node> list : nodeList) {
                list.addNotify();
            }
        }

        @Override
        public void removeNotify() {
            for (NodeList<Node> list : nodeList) {
                list.removeNotify();
            }
        }

    }

}
