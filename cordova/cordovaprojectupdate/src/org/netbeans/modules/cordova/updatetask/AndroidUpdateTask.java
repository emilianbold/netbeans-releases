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
package org.netbeans.modules.cordova.updatetask;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;

/**
 *
 * @author Jan Becicka
 */
public class AndroidUpdateTask extends CordovaTask {

    @Override
    public void execute() throws BuildException {
        File manifestFile = new File(
                getProject().getBaseDir().getAbsolutePath() + 
                "/" + getProperty("cordova.platforms") + 
                "/android/AndroidManifest.xml");
        File configFile = new File(
                getProject().getBaseDir().getAbsolutePath() + 
                "/" + getProperty("site.root") + 
                "/config.xml");
        File androidConfigFile = new File(
                getProject().getBaseDir().getAbsolutePath() + 
                "/" + getProperty("cordova.platforms") + 
                "/android/res/xml/config.xml");
        try {
            AndroidManifest androidManifest = new AndroidManifest(manifestFile);
            updateAndroidManifest(androidManifest);
            androidManifest.save();
            
            DeviceConfig androidConfig = new DeviceConfig(androidConfigFile);
            SourceConfig config = new SourceConfig(configFile);
            
            updateAndroidConfig(config, androidConfig);
            androidConfig.save();
            updateResources(config);
            
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void updateAndroidManifest(AndroidManifest manifest) {
        manifest.setName(getProperty("project.name"));
        manifest.setPackage(getProperty("android.project.package"));
    }

    private void updateAndroidConfig(SourceConfig config, DeviceConfig androidConfig) {
        androidConfig.setAccess(config.getAccess());
    }
    
    private void updateResources(SourceConfig config) throws IOException {
        String icon = config.getIcon("android", 36, 36);
        copy(icon, "drawable-ldpi/icon");

        icon = config.getIcon("android", 48, 48);
        copy(icon, "drawable-mdpi/icon");
        
        icon = config.getIcon("android", 72, 72);
        copy(icon, "drawable-hdpi/icon");

        icon = config.getIcon("android", 96, 96);
        copy(icon, "drawable-xhdpi/icon");

        copy(icon, "drawable/icon");
        
        String splash = config.getSplash("android", 320, 200);
        copy(splash, "drawable-ldpi/splash_landscape");

        splash = config.getSplash("android", 200, 320);
        copy(splash, "drawable-ldpi/splash_portrait");
        
        splash = config.getSplash("android", 480, 320);
        copy(splash, "drawable-mdpi/splash_landscape");

        splash = config.getSplash("android", 320, 480);
        copy(splash, "drawable-mdpi/splash_portrait");
        
        
        splash = config.getSplash("android", 800, 480);
        copy(splash, "drawable-hdpi/splash_landscape");

        splash = config.getSplash("android", 480, 800);
        copy(splash, "drawable-hdpi/splash_portrait");
        
        splash = config.getSplash("android", 1280, 720);
        copy(splash, "drawable-xhdpi/splash_landscape");

        splash = config.getSplash("android", 720, 1280);
        copy(splash, "drawable-xhdpi/splash_portrait");

        splash = config.getSplash("android", 1280, 720);
        copy(splash, "drawable/splash_landscape");

        splash = config.getSplash("android", 720, 1280);
        copy(splash, "drawable/splash_portrait");
        
    }
    
    private void copy(String source, String dest) throws IOException {
        if (source==null) {
            return;
        }
        String ext = source.substring(source.indexOf("."));
        final String prjPath = getProject().getBaseDir().getPath();
        FileUtils.getFileUtils().copyFile(
                prjPath + "/" + getProperty("site.root") + "/" + source, 
                prjPath + "/" + getProject().getProperty("cordova.platforms") + "/android/res/" + dest + ext);
    }
            
}
