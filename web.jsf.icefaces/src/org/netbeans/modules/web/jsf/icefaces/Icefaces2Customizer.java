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
package org.netbeans.modules.web.jsf.icefaces;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.web.jsf.icefaces.ui.Icefaces2CustomizerPanelVisual;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Icefaces2Customizer implements JsfComponentCustomizer {

    private static final Logger LOGGER = Logger.getLogger(Icefaces2Customizer.class.getName());
    private Icefaces2CustomizerPanelVisual panel;
    private ChangeSupport changeSupport = new ChangeSupport(this);

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
            panel = new Icefaces2CustomizerPanelVisual(new PanelChangeListener());
            panel.initLibraries(true);
        }
        return panel;
    }

    @Override
    public boolean isValid() {
        Preferences preferences = Icefaces2Implementation.getIcefacesPreferences();
        String icefacesLibrary = preferences.get(Icefaces2Implementation.ICEFACES_CORE_CLASS, ""); //NOI18N
        if (LibraryManager.getDefault().getLibrary(icefacesLibrary) != null) {
            return true;
        }

        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(library.getType())) { // NOI18N
                continue;
            }

            List<URL> content = library.getContent("classpath"); //NOI18N
            if (isValidIcefacesLibrary(content)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getErrorMessage() {
        return panel.getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return panel.getWarningMessage();
    }

    @Override
    public void saveConfiguration() {
        Preferences preferences = Icefaces2Implementation.getIcefacesPreferences();
        if (panel.getIcefacesLibrary() != null) {
            preferences.put(Icefaces2Implementation.PREF_LIBRARY_NAME, panel.getIcefacesLibrary());
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Returns {@code List} of all ICEfaces2 libraries registered in the IDE.
     *
     * @return {{@code List} of libraries
     */
    public static List<Library> getIcefacesLibraries() {
        List<Library> libraries = new ArrayList<Library>();
        List<URL> content;
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(library.getType())) { //NOI18N
                continue;
            }

            content = library.getContent("classpath"); //NOI18N
            if (isValidIcefacesLibrary(content)) {
                libraries.add(library);
            }
        }
        return libraries;
    }

    /**
     * Checks if given library content contains mandatory class in cases of ICEfaces2 library.
     *
     * @param libraryContent library content
     * @return {@code true} if the given content contains ICEfaces2 library, {@code false} otherwise
     */
    public static boolean isValidIcefacesLibrary(List<URL> libraryContent) {
        try {
            return Util.containsClass(libraryContent, Icefaces2Implementation.ICEFACES_CORE_CLASS);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return false;
        }
    }

    /**
     * Listener for listening changes on the {@link Icefaces2CustomizerPanelVisual).
     */
    private class PanelChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }
    }
}
