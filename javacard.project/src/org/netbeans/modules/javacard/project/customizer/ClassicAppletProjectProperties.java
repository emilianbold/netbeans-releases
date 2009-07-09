/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.customizer;

import com.sun.javacard.AID;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Boudreau
 */
public final class ClassicAppletProjectProperties extends AppletProjectProperties {

    private AID originalPackageAID;
    private AID packageAID;

    public ClassicAppletProjectProperties(JCProject project) {
        super(project);
    }

    public AID getPackageAID() {
        return packageAID;
    }

    public void setPackageAID(AID aid) {
        packageAID = aid;
    }

    private void rewriteManifest() throws IOException {
        FileObject manifestFo = project.getProjectDirectory().getFileObject(JCConstants.MANIFEST_PATH); //NOI18N
        Manifest manifest = null;
        if (manifestFo == null) {
            Logger.getLogger(ClassicAppletProjectProperties.class.getName()).log(
                    Level.INFO, "Manifest missing for project " +
                    project.getProjectDirectory().getPath() + ".  Recreating.");
            manifestFo = project.getProjectDirectory().createData(JCConstants.MANIFEST_PATH); //NOI18N
            manifest = new Manifest();
            Attributes a = manifest.getMainAttributes();
            a.putValue (JCConstants.MANIFEST_ENTRY_CLASSIC_RUNTIME_DESCRIPTOR_VERSION, "3.0");
            a.putValue (JCConstants.MANIFEST_APPLICATION_TYPE, project.kind().getManifestApplicationType());
        } else {
            InputStream in = manifestFo.getInputStream();
            try {
                manifest = new Manifest (in);
            } finally {
                in.close();
            }
        }
        manifest.getMainAttributes().putValue(
                JCConstants.MANIFEST_ENTRY_CLASSIC_PACKAGE_AID, packageAID.toString());
        FileLock lock = manifestFo.lock();
        OutputStream out = manifestFo.getOutputStream(lock);
        try {
            manifest.write(out);
        } finally {
            out.close();
            lock.releaseLock();
        }
    }

    @Override
    protected boolean doStoreProperties(EditableProperties props) throws IOException {
        if (packageAID != null && !packageAID.equals(originalPackageAID)) {
            props.setProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_PACKAGE_AID, packageAID.toString());
            rewriteManifest();
        }
        return true;
    }

    @Override
    protected void onInit(PropertyEvaluator eval) {
        String aidString = project.evaluator().getProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_PACKAGE_AID);
        if (aidString != null) {
            try {
                packageAID = AID.parse(aidString);
                originalPackageAID = packageAID;
            } catch (IllegalArgumentException e) {
                Logger.getLogger(ClassicAppletProjectProperties.class.getName()).log(Level.INFO,
                        "Bad classic package aid in " +
                        project.getProjectDirectory().getPath() + ": " + aidString, e);
            }
        }
    }
}
