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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.spi.webmodule;

import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 * Provides support for extending a web module with a web framework, that is,
 * it allows to modify the web module to make use of the framework.
 *
 * @author Andrei Badea
 *
 * @since 1.9
 */
public abstract class WebModuleExtender {

    /**
     * Attaches a change listener that is to be notified of changes
     * in the extender (e.g., the result of the {@link #isValid} method
     * has changed.
     *
     * @param  listener a listener.
     */
    public abstract void addChangeListener(ChangeListener listener);

    /**
     * Removes a change listener.
     *
     * @param  listener a listener.
     */
    public abstract void removeChangeListener(ChangeListener listener);

    /**
     * Returns a UI component used to allow the user to customize this extender.
     *
     * @return a component or null if this extender does not provide a configuration UI.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    public abstract JComponent getComponent();

    /**
     * Returns a help context for {@link #getComponent}.
     *
     * @return a help context; can be null.
     */
    public abstract HelpCtx getHelp();

    /**
     * Called when the component returned by {@link #getComponent} needs to be filled
     * with external data.
     */
    public abstract void update();

    /**
     * Checks if this extender is valid (e.g., if the configuration set
     * using the UI component returned by {@link #getComponent} is valid).
     *
     * @return true if the configuration is valid, false otherwise.
     */
    public abstract boolean isValid();

    /**
     * Called to extend the given web module with the web framework
     * corresponding to this extender.
     *
     * @param  webModule the web module to be extender; never null.
     * @return the set of newly created files in the web module.
     */
    public abstract Set<FileObject> extend(WebModule webModule);
}
