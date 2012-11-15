/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.platforms;

import java.io.IOException;
import org.netbeans.modules.web.clientproject.spi.ClientProjectExtender;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service = ClientProjectExtender.class)
public class MobileProjectExtender implements ClientProjectExtender {

    @Override
    public Panel<WizardDescriptor>[] createWizardPanels() {
        return new Panel[0];
    }

    @Override
    @NbBundle.Messages({
        "LBL_iPhoneSimulator=iPhone Simulator",
        "LBL_AndroidEmulator=Android Emulator",
        "LBL_AndroidDevice=Android Device"
    })

    public void apply(FileObject projectRoot, FileObject siteRoot, String librariesPath) {
        try {
            createMobileConfigs(projectRoot);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void createMobileConfigs(FileObject projectRoot) throws IOException {
        EditableProperties ios = new EditableProperties(true);
        ios.put("display.name", Bundle.LBL_iPhoneSimulator());//NOI18N
        ios.put("type", PlatformManager.IOS_TYPE);//NOI18N
        ios.put(Device.DEVICE_PROP, Device.EMULATOR);//NOI18N
        ConfigUtils.createConfigFile(projectRoot, PlatformManager.IOS_TYPE, ios);//NOI18N

        EditableProperties androide = new EditableProperties(true);
        androide.put("display.name", Bundle.LBL_AndroidEmulator());
        androide.put("type", PlatformManager.ANDROID_TYPE);//NOI18N
        androide.put(Device.DEVICE_PROP, Device.EMULATOR);//NOI18N
        ConfigUtils.createConfigFile(projectRoot, PlatformManager.ANDROID_TYPE, androide);//NOI18N

        EditableProperties androidd = new EditableProperties(true);
        androidd.put("display.name", Bundle.LBL_AndroidDevice());
        androidd.put("type", PlatformManager.ANDROID_TYPE);//NOI18N
        androidd.put(Device.DEVICE_PROP, Device.DEVICE);//NOI18N
        ConfigUtils.createConfigFile(projectRoot, PlatformManager.ANDROID_TYPE, androidd);//NOI18N
    }
}