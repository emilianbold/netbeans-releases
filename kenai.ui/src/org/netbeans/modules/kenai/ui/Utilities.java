/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiService;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kubec
 */
public class Utilities {

    public static File getDefaultRepoFolder() {
        File defaultDir = FileSystemView.getFileSystemView().getDefaultDirectory();
        if (defaultDir != null && defaultDir.exists() && defaultDir.isDirectory()) {
            String nbPrjDirName = NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "DIR_NetBeansProjects");
            File nbPrjDir = new File(defaultDir, nbPrjDirName);
            if (nbPrjDir.exists() && nbPrjDir.canWrite()) {
                return nbPrjDir;
            }
        }
        return FileUtil.normalizeFile(new File(System.getProperty("user.home")));
    }

    private static HashMap<String, Boolean> chatSupported = new HashMap();

    public static boolean isChatSupported(Kenai kenai) {
        String kenaiHost = kenai.getUrl().getHost();
        Boolean b = chatSupported.get(kenaiHost);
        if (b==null) {
            b=Boolean.FALSE;
            try {
                for (KenaiService service : kenai.getServices()) {
                    if (service.getType() == KenaiService.Type.CHAT) {
                        b = Boolean.TRUE;
                        break;
                    }
                }
            } catch (KenaiException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                return false;
            }
            chatSupported.put(kenaiHost, b);
        }
        return b;
    }

    public static void assertJid(String name) {
        assert name!=null;
        assert name.contains("@"): "name must be FQN";
        assert !name.contains("/"): "name cannot contain '/'";
    }

    public static Kenai getPreferredKenai() {
        Collection<Kenai> kenais = KenaiManager.getDefault().getKenais();
        Kenai kenai = null;
        for (Kenai k:kenais) {
            if (k.getUrl().getHost().equals("kenai.com")) { //NOI!18N
                kenai = k;
            }
            if (k.getUrl().getHost().endsWith("java.net")) { //NOI!18N
                return k;
            }
        }
        if (kenai!=null)
            return kenai;
        if (!kenais.isEmpty()) {
            return kenais.iterator().next();
        }
        return null;
    }
}
