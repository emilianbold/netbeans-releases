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
package org.netbeans.modules.javafx2.scenebuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.javafx2.editor.spi.FXMLOpener;
import org.openide.loaders.DataObject;

import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=FXMLOpener.class)
/**
 * Opens an FXML file in SceneBuilder instance if available.
 */
public final class SceneBuilderFXMLOpener extends FXMLOpener {
    final private static Logger LOG = Logger.getLogger(SceneBuilderFXMLOpener.class.getName());
    
    final private ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true).controllable(true);
    
    private Settings settings = Settings.getInstance();
 
    @Override
    public boolean isEnabled(Lookup context) {
        return true;
    }

    @Override
    @Messages("LBL_SceneBuilder_Out=SceneBuilder")
    public boolean open(Lookup context) {
        String execPath = getExecutablePath();
        if (execPath == null) {
            return false;
        }
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(getExecutablePath());

        boolean allSaved = true;
        Collection<? extends DataObject> dobjs = context.lookupAll(DataObject.class);
        for (DataObject dataObject : dobjs) {
            try {
                SaveCookie sc = dataObject.getLookup().lookup(SaveCookie.class);
                if (sc != null) sc.save();
                
            } catch (IOException e) {
                allSaved = false;
                LOG.log(Level.SEVERE, null, e);
                return false;
            }
        }

        if (allSaved) {
            String firstPath = null;
            for (DataObject dataObject : dobjs) {
                if (firstPath == null) {
                    firstPath = dataObject.getPrimaryFile().getPath();
                }
                processBuilder = processBuilder.addArgument(dataObject.getPrimaryFile().getPath());
            }
            if (firstPath != null) {
                ExecutionService service = ExecutionService.newService(processBuilder, descriptor, firstPath);
                service.run();
            }
        }
        return true;
    }
    
    private String getExecutablePath() {
        Home home = settings.getSelectedHome();
        if (home != null && home.isValid()) {
            String postfix = ".sh"; // NOI18N
            if (Utilities.isWindows()) {
                postfix = ".exe"; // NOI18N
            }
            return home.getPath() + File.separator + "bin" + File.separator + "scenebuilder" + postfix; // NOI18N
        }
        return null;
    }
}
