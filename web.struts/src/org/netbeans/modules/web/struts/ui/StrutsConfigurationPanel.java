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

package org.netbeans.modules.web.struts.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.modules.web.struts.StrutsFrameworkProvider;
import org.openide.util.HelpCtx;

/**
 * Panel asking for web frameworks to use.
 * @author Radko Najman
 */
public final class StrutsConfigurationPanel extends WebModuleExtender {

    private final StrutsFrameworkProvider framework;
    private final ExtenderController controller;
    private StrutsConfigurationPanelVisual component;

    private boolean customizer;

    /** Create the wizard panel descriptor. */
    public StrutsConfigurationPanel(StrutsFrameworkProvider framework, ExtenderController controller, boolean customizer) {
        this.framework = framework;
        this.controller = controller;
        this.customizer = customizer;
        getComponent();
    }

    public StrutsConfigurationPanelVisual getComponent() {
        if (component == null) {
            component = new StrutsConfigurationPanelVisual(this, customizer);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(StrutsConfigurationPanel.class);
    }

    public void update() {
        // nothing to update
    }

    public boolean isValid() {
        getComponent();
        return component.valid();
    }
    
    public Set extend(WebModule webModule) {
        return framework.extendImpl(webModule);
    }

    public ExtenderController getController() {
        return controller;
    }

    private final Set listeners = new /*<ChangeListener>*/ HashSet(1);

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    public String getURLPattern() {
        return component.getURLPattern();
    }

    public void setURLPattern(String pattern) {
        component.setURLPattern(pattern);
    }

    public String getServletName() {
        return component.getServletName();
    }

    public void setServletName(String name) {
        component.setServletName(name);
    }

    public String getAppResource() {
        return component.getAppResource();
    }

    public void setAppResource(String resource) {
        component.setAppResource(resource);
    }

    public boolean addTLDs() {
        return component.addTLDs();
    }

    public boolean packageWars() {
        return component.packageWars();
    }
}
