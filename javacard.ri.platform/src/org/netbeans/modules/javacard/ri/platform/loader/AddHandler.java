/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.ri.platform.loader;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.validation.adapters.DialogBuilder;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.common.KeysAndValues;
import org.netbeans.modules.javacard.ri.platform.installer.KeysAndValuesEditablePropsAdapter;
import org.netbeans.modules.javacard.ri.platform.installer.NewDevicePanel;
import org.netbeans.modules.javacard.spi.AddCardHandler;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.JavacardDeviceKeyNames;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * AddDeviceHandler registered in the system filesystem for adding devices
 * to platforms with platform kind "RI"
 * @author Tim Boudreau
 */
public class AddHandler extends AddCardHandler {
    public AddHandler() {
        super(""); //NOI18N
    }

    @Override
    public Card showNewDeviceDialog(DataObject platformDob, JavacardPlatform target, CardCreatedCallback callback, FileObject targetFolder) {
        final FileObject fld = targetFolder;
        final NewDevicePanel panel = new NewDevicePanel(fld);
        final EditableProperties p = new EditableProperties(true);
        Properties props = target.toProperties();
        for (Map.Entry<Object,Object> e : props.entrySet()) {
            String key = (String) e.getKey();
            if (key.startsWith("prototype.")) { //NOI18N
                key = key.substring ("prototype.".length()); //NOI18N
                p.put(key, (String) e.getValue());
            }
        }
        if (!p.isEmpty()) {
            panel.read(new KeysAndValuesEditablePropsAdapter(p));
        }

        String title = NbBundle.getMessage(AddHandler.class,
                "TTL_NEW_DEVICE"); //NOI18N
        DialogBuilder builder = new DialogBuilder(AddHandler.class).setTitle(title).
                setContent(panel).
                setValidationGroup(panel.getValidationGroup()).
                setModal(true);

        if (builder.showDialog(DialogDescriptor.OK_OPTION)) {
            KeysAndValues<?> kv = new KeysAndValuesEditablePropsAdapter(p);
            panel.write(kv);
            if (p.getProperty(JavacardDeviceKeyNames.DEVICE_CAPABILITIES) == null) {
                p.setProperty(JavacardDeviceKeyNames.DEVICE_CAPABILITIES,
                        "START,STOP,RESUME,DEBUG,EPROM_FILE,CLEAR_EPROM,CONTENTS," + //NOI18N
                        "CUSTOMIZER,INTERCEPTOR,PORTS,URL,DELETE"); //NOI18N
            }
            final FileObject[] res = new FileObject[1];
            try {
                fld.getFileSystem().runAtomicAction(new AtomicAction() {

                    public void run() throws IOException {
                        res[0] = fld.createData(panel.getDeviceName().trim(),
                                JCConstants.JAVACARD_DEVICE_FILE_EXTENSION);
                        OutputStream out = new BufferedOutputStream(res[0].getOutputStream());
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
            if (res[0] != null) {
                try {
                    DataObject dob = DataObject.find(res[0]);
                    Card card = dob.getLookup().lookup(Card.class);
                    if (card != null) {
                        if (callback != null) {
                            callback.onCardCreated(card, res[0]);
                        }
                    }
                    return card;
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }
}
