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
package org.netbeans.modules.web.inspect.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.openide.filesystems.FileObject;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * Class responsible for opening and closing of DOM Tree view.
 *
 * @author Jan Stola
 */
public class DomTCController implements PropertyChangeListener {
    /** MIME types of files for which DOM Tree view should be opened. */
    private static final Collection<String> DOM_TC_MIME_TYPES = new HashSet(
            Arrays.asList(new String[]{"text/x-jsp", "text/x-php5"})); // NOI18N
    /** MIME types of files for which we have special support in Navigator. */
    private static final Collection<String> NAVIGATOR_MIME_TYPES = new HashSet(
            Arrays.asList(new String[]{"text/html", "text/xhtml"})); // NOI18N
    /** Default instance of this class. */
    private static final DomTCController DEFAULT = new DomTCController();
    /** MIME type of the last active file in editor. */
    private String lastMimeType;
    /** Determines whether some inspection is active. */
    private boolean inspectionActive;

    /**
     * Creates a new {@code DOMTCController}.
     */
    @SuppressWarnings("LeakingThisInConstructor") // NOI18N
    private DomTCController() {
        PageInspector inspector = PageInspector.getDefault();
        inspector.addPropertyChangeListener(this);
        TopComponent.Registry registry = WindowManager.getDefault().getRegistry();
        registry.addPropertyChangeListener(this);
        initActiveComponent();
    }

    /**
     * Initializes the information about the active editor {@code TopComponent}.
     */
    private void initActiveComponent() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                WindowManager manager = WindowManager.getDefault();
                TopComponent.Registry registry = manager.getRegistry();
                TopComponent active = registry.getActivated();
                if ((active != null) && manager.isOpenedEditorTopComponent(active)) {
                    componentActivated(active);
                } else {
                    for (Mode mode : manager.getModes()) {
                        if (manager.isEditorMode(mode)) {
                            active = mode.getSelectedTopComponent();
                            if (active != null) {
                                componentActivated(active);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Returns the default instance of this class.
     * 
     * @return default instance of this class.
     */
    public static DomTCController getDefault() {
        return DEFAULT;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (TopComponent.Registry.PROP_ACTIVATED.equals(propName)) {
            TopComponent tc = (TopComponent)evt.getNewValue();
            componentActivated(tc);
        } else if (PageInspector.PROP_MODEL.equals(propName)) {
            pageInspected();
        }
    }

    /** The last active editor {@code TopComponent}. */
    private Reference<TopComponent> lastTC;
    private void componentActivated(TopComponent tc) {
        if (!WindowManager.getDefault().isOpenedEditorTopComponent(tc)) {
            synchronized (this) {
                if (lastTC != null) {
                    // Check if lastTC is still valid
                    TopComponent active = lastTC.get();
                    if (active == null || !WindowManager.getDefault().isOpenedEditorTopComponent(active)) {
                        lastTC = null;
                        lastMimeType = null;
                        updateDomTC0();
                    }
                }
            }
            return;
        }
        FileObject fob = tc.getLookup().lookup(FileObject.class);
        synchronized (this) {
            if (fob == null) {
                lastTC = null;
                lastMimeType = null;
            } else {
                lastTC = new WeakReference<TopComponent>(tc);
                lastMimeType = fob.getMIMEType();
            }
        }
        updateDomTC();
    }

    /**
     * Invoked when {@code PageInspector} starts/stops to inspect a page.
     */
    private void pageInspected() {
        PageInspector inspector = PageInspector.getDefault();
        synchronized (this) {
            inspectionActive = (inspector.getPage() != null);
        }
        updateDomTC();
    }

    /**
     * Updates the state of DOM Tree view. This method can be called from
     * any thread.
     */
    private void updateDomTC() {
        if (EventQueue.isDispatchThread()) {
            updateDomTC0();
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateDomTC0();
                }
            });
        }
    }

    /**
     * Updates the state of DOM Tree view. This method can be called
     * from event-dispatch thread only.
     */
    private void updateDomTC0() {
        synchronized (this) {
            TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup("DomTree"); // NOI18N
            TopComponent tc = WindowManager.getDefault().findTopComponent(DomTC.ID);
            if (inspectionActive) {
                if (DOM_TC_MIME_TYPES.contains(lastMimeType)) {
                    // Open DOM Tree view
                    boolean wasOpened = tc.isOpened();
                    group.open();
                    if (!wasOpened && tc.isOpened()) {
                        tc.requestActive();
                    }
                }
                if ((lastMimeType == null) || NAVIGATOR_MIME_TYPES.contains(lastMimeType)) {
                    TopComponent navigator = WindowManager.getDefault().findTopComponent("navigatorTC"); // NOI18N
                    if (navigator != null && navigator.isOpened()) {
                        // Close DOM Tree view and activate Navigator instead
                        group.close();
                        navigator.requestActive();
                    }
                }
            } else {
                // Close DOM Tree view
                group.close();
            }
        }
    }

}
