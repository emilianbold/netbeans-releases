/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.richfaces;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.web.jsf.richfaces.ui.Richfaces4CustomizerPanelVisual;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Richfaces4Customizer implements JsfComponentCustomizer {

    Richfaces4CustomizerPanelVisual panel;
    private ChangeSupport changeSupport = new ChangeSupport(this);
    boolean initialize = true;

    public static final Logger LOGGER = Logger.getLogger(Richfaces4Customizer.class.getName());

    public Richfaces4Customizer() {
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public JComponent getComponent() {
        if (panel == null) {
            panel = new Richfaces4CustomizerPanelVisual(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    changeSupport.fireChange();
                }
            });

            panel.addAncestorListener(new AncestorListener() {

                @Override
                public void ancestorAdded(AncestorEvent event) {
                    Container component = event.getAncestor();
                    if (component instanceof Dialog) {
                        Dialog ancestorDialog = (Dialog)component;
                        ancestorDialog.addWindowFocusListener(new WindowFocusListener() {

                            @Override
                            public void windowGainedFocus(WindowEvent e) {
                                if (initialize) {
                                    panel.initLibraries(true);
                                    initialize = false;
                                } else {
                                    changeSupport.fireChange();
                                }
                            }

                            @Override
                            public void windowLostFocus(WindowEvent e) {
                            }
                        });
                    }
                }

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                }
            });
        }
        return panel;
    }

    @Override
    public boolean isValid() {
        Preferences preferences = NbPreferences.forModule(Richfaces4Customizer.class).node(Richfaces4Implementation.PREF_RICHFACES_NODE);
        String richfacesLibrary = preferences.get(Richfaces4Implementation.PREF_RICHFACES_LIBRARY, "");
        if (LibraryManager.getDefault().getLibrary(richfacesLibrary) != null) {
            return true;
        }

        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(library.getType())) { // NOI18N
                continue;
            }

            List<URL> content = library.getContent("classpath"); //NOI18N
            if (isValidRichfacesLibrary(content)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getWarningMessage() {
        return panel.getWarningMessage();
    }

    @Override
    public String getErrorMessage() {
        return panel.getErrorMessage();
    }

    @Override
    public void saveConfiguration() {
        Preferences preferences = NbPreferences.forModule(Richfaces4Customizer.class).node(Richfaces4Implementation.PREF_RICHFACES_NODE);
        preferences.put(Richfaces4Implementation.PREF_RICHFACES_LIBRARY, panel.getRichFacesLibrary());
    }

    public static List<Library> getRichfacesLibraries() {
        List<Library> libraries = new ArrayList<Library>();
        List<URL> content;
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(library.getType())) { // NOI18N
                continue;
            }

            content = library.getContent("classpath"); //NOI18N
            if (Richfaces4Customizer.isValidRichfacesLibrary(content)) {
                libraries.add(library);
            }
        }
        return libraries;
    }

    public static boolean isValidRichfacesLibrary(List<URL> libraryContent) {
        Set<Entry<String, String>> entrySet = Richfaces4Implementation.RF_LIBRARIES.entrySet();
        for (Entry<String, String> entry : entrySet) {
            try {
                if (!Util.containsClass(libraryContent, entry.getKey())) {
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                return false;
            }
        }
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return panel.getHelpCtx();
    }

}
