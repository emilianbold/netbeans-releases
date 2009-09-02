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
package org.netbeans.modules.javacard.platform.loader;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.api.validation.adapters.DialogBuilder;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.platform.KeysAndValues;
import org.netbeans.modules.javacard.platform.NewDevicePanel;
import org.netbeans.spi.actions.Single;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public class AddDeviceAction extends Single<JavacardPlatform> {

    public AddDeviceAction() {
        super(JavacardPlatform.class, NbBundle.getMessage(AddDeviceAction.class,
                "ACTION_ADD_DEVICE"), null); //NOI18N
    }

    @Override
    protected void actionPerformed(JavacardPlatform target) {
        final FileObject fld = Utils.sfsFolderForDeviceConfigsForPlatformNamed(target.getSystemName(), true);
        final NewDevicePanel panel = new NewDevicePanel(fld);
        String title = NbBundle.getMessage(AddDeviceAction.class,
                "TTL_NEW_DEVICE"); //NOI18N
        DialogBuilder builder = new DialogBuilder(AddDeviceAction.class).setTitle(title).
                setContent(panel).
                setValidationGroup(panel.getValidationGroup()).
                setModal(true);

        if (builder.showDialog(DialogDescriptor.OK_OPTION)) {
            final EditableProperties p = new EditableProperties(true);
            KeysAndValues kv = new KeysAndValues.EditablePropertiesAdapter(p);
            panel.write(kv);
            try {
                fld.getFileSystem().runAtomicAction(new AtomicAction() {

                    public void run() throws IOException {
                        FileObject data = fld.createData(panel.getDeviceName().trim(),
                                JCConstants.JAVACARD_DEVICE_FILE_EXTENSION);
                        OutputStream out = new BufferedOutputStream(data.getOutputStream());
                        try {
                            p.store(out);
                        } finally {
                            out.close();
                        }
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
