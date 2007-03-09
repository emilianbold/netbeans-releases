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
