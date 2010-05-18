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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.util.Map;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/**
 * Abstract base class for external reference customizers.
 *
 * @author  Ajit Bhate
 * @author  Nathan Fiedler
 */
public abstract class AbstractReferenceCustomizer<T extends Component>
        extends AbstractComponentCustomizer<T> {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The explorer manager for the node view. */
    protected ExplorerManager explorerManager;

    /**
     * Creates new form ExternalReferenceCustomizer
     *
     * @param  component  external reference to customize.
     */
    public AbstractReferenceCustomizer(T component) {
        super(component);
    }

    /**
     * Create an ExternalReferenceNode with the given delegate node.
     *
     * @param  node  delegate Node.
     * @return  new ExternalReferenceNode.
     */
    public abstract ExternalReferenceDataNode createExternalReferenceNode(
            Node original);

    /**
     * Creates the root node of the file selection tree.
     */
    protected abstract Node createRootNode();

    /**
     * Return the target namespace of the given model.
     *
     * @param  model  the model for which to get the namespace.
     * @return  target namespace, or null if none.
     */
    protected abstract String getTargetNamespace(Model model);

    /**
     * Return the model of the component being customized.
     *
     * @return  component model.
     */
    public Model getComponentModel() {
        return getModelComponent().getModel();
    }

    /**
     * Return the target namespace of the model that contains the
     * component being customized.
     *
     * @return  target namespace, or null if none.
     */
    public String getTargetNamespace() {
        return getTargetNamespace(getModelComponent().getModel());
    }

    /**
     * Return the existing external reference prefixes for the given model.
     *
     * @param  model  the model for which to get the namespace.
     * @return  set of prefixes; empty if none.
     */
    protected abstract Map<String, String> getPrefixes(Model model);

    /**
     * Returns the NodeDecorator for this customizer, if any.
     *
     * @return  node decorator for files nodes, or null if none.
     */
    protected abstract ExternalReferenceDecorator getNodeDecorator();

    /**
     * Load the component values into the interface widgets. Do not, under
     * any circumstances, create interface components and add them to the
     * customizer. This instance is cached and re-used over and over again.
     */
    protected abstract void initializeUI();

    /**
     * Indicates if the namespace value must be different than that of
     * the model containing the component being customized. If false,
     * then the opposite must hold - the namespace must be the same.
     * The one exception is if the namespace is not defined at all.
     *
     * @return  true if namespace must differ, false if same.
     */
    public abstract boolean mustNamespaceDiffer();

    public void reset() {
        initializeUI();
        // Rebuild the node tree and view to ensure we display the
        // latest available files in the project.
        Node root = createRootNode();
        explorerManager.setRootContext(root);
        setSaveEnabled(false);
        setResetEnabled(false);
        showMessage(null);
    }

    /**
     * Display the given message, or reset the message label to blank.
     *
     * @param  msg  message to show, or null to hide messages.
     */
    protected abstract void showMessage(String msg);
}
