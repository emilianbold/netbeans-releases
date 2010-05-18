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

package org.netbeans.modules.visualweb.designer.jsf.palette;

import org.netbeans.modules.visualweb.complib.api.ComplibEvent;
import org.netbeans.modules.visualweb.complib.api.ComplibListener;
import org.netbeans.modules.visualweb.complib.api.ComplibService;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.designer.jsf.palette.JsfPaletteActions;
import org.netbeans.modules.visualweb.designer.jsf.palette.MergedPaletteActions;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Factory for creating JSF <code>PaletteController</code>.
 *
 * @author Peter Zavadsky
 */
public final class PaletteControllerFactory {


    private static final String PALETTE_DIRECTORY_1_4 = "CreatorDesignerPalette"; // NOI18N

    private static final String PALETTE_DIRECTORY_5   = "CreatorDesignerPalette5"; // NOI18N

    private static final PaletteControllerFactory INSTANCE = new PaletteControllerFactory();


    /** Creates a new instance of JsfPaletteControllerFactory */
    private PaletteControllerFactory() {
    }


    public static PaletteControllerFactory getDefault() {
        return INSTANCE;
    }
    
    /**
     * The sole purpose of this method is to grab the controller 
     * for to get the palette customizer (palette manager) for 
     * J2EE 5 projects. 
     **/
    public static PaletteController getJsfPaletteController_5(){
        return getJsfPaletteController(PALETTE_DIRECTORY_5);
    }
    
    /**
     * The sole purpose of this method is to grab the controller 
     * for to get the palette customizer (palette manager) for 
     * J2EE 1.4 projects. 
     **/
    public static PaletteController getJsfPaletteController_1_4(){
        return getJsfPaletteController(PALETTE_DIRECTORY_1_4);
    }
    
    
    /**
     * Only to be used by "getJsfPaletteController_1_4()" and "getJsfPaletteController_5()"
     * @param paletteDirectory can either be "CreatorDesignerPalette" or "CreatorDesignerPalette5"
     **/
    private static PaletteController getJsfPaletteController(String paletteDirectory ){
        try {
            PaletteActions paletteActions = new JsfPaletteActions(paletteDirectory);
            PaletteController controller = PaletteFactory.createPalette(paletteDirectory, paletteActions, null, null);
            return controller;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

    }

    public PaletteController createJsfPaletteController(Project project) {
        if (project == null) {
            // XXX
            return null;
        }

        String paletteDirectory;
        if (JsfProjectUtils.JAVA_EE_5.equals(JsfProjectUtils.getJ2eePlatformVersion(project))) {
            paletteDirectory = PALETTE_DIRECTORY_5;
        } else {
            //Later to be renamed with a 1.4
            paletteDirectory = PALETTE_DIRECTORY_1_4;
        }

        // XXX PaletteController
        PaletteController controller;
        try {
            ComplibService complibService = getComplibService();
            PaletteFilter complibPaletteFilter;
            PaletteActions allPaletteActions;
            if (complibService == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new NullPointerException("There is no ComplibService available!")); // NOI18N
                complibPaletteFilter = null;
                allPaletteActions = null;
            } else {
                complibPaletteFilter = complibService.createComplibPaletteFilter(project);

                // Merge in complib PaletteActions
                PaletteActions jsfPaletteActions = new JsfPaletteActions(paletteDirectory);
                PaletteActions complibPaletteActions = complibService.createComplibPaletteActions();
                if (complibPaletteActions != null) {
                    allPaletteActions = new MergedPaletteActions(jsfPaletteActions, complibPaletteActions);
                } else {
                    allPaletteActions = jsfPaletteActions;
                }
            }

            controller = PaletteFactory.createPalette(paletteDirectory,
                    allPaletteActions, complibPaletteFilter, null);

            // XXX #6466711 Listening to changes of complib to refresh the palette.
            JsfComplibListener.getDefault().install();
            JsfComplibListener.getDefault().setPaletteController(controller);

            return controller;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            controller = null;
        }

        return controller;
    }

    private static ComplibService getComplibService() {
        return Lookup.getDefault().lookup(ComplibService.class);
    }


    private static class JsfComplibListener implements ComplibListener {
        private static JsfComplibListener INSTANCE = new JsfComplibListener();

        private WeakReference<PaletteController> paletteControllerWRef = new WeakReference<PaletteController>(null);
        
        private boolean installed;
        
        public static JsfComplibListener getDefault() {
            return INSTANCE;
        }
        
        public void install() {
            if (installed) {
                return;
            }
            ComplibService complibService = getComplibService();
            if (complibService == null) {
                return;
            }
            complibService.addComplibListener(this);
            installed = true;
        }
        
        public void uninstall() {
            ComplibService complibService = getComplibService();
            if (complibService == null) {
                return;
            }
            complibService.removeComplibListener(this);
            installed = false;
        }
        
        public void setPaletteController(PaletteController paletteController) {
            paletteControllerWRef = new WeakReference<PaletteController>(paletteController);
        }
        
        public void paletteChanged(ComplibEvent evt) {
            PaletteController paletteController = paletteControllerWRef.get();
            if (paletteController == null) {
                return;
            }
            paletteController.refresh();
        }
    } // End of JsfComplibListener        
        



}
