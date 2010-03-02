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

package org.netbeans.modules.keyring.gnome;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.netbeans.modules.keyring.gnome.GnomeKeyringLibrary.*;
import org.netbeans.spi.keyring.KeyringProvider;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=KeyringProvider.class, position=100)
public class GnomeProvider implements KeyringProvider {

    private static final Logger LOG = Logger.getLogger(GnomeProvider.class.getName());
    private static final String KEY = "key"; // NOI18N

    private static GnomeKeyringPasswordSchema SCHEMA;

    public boolean enabled() {
        if (Boolean.getBoolean("netbeans.keyring.no.native")) {
            LOG.fine("native keyring integration disabled");
            return false;
        }
        if (System.getenv("GNOME_KEYRING_PID") == null) { // NOI18N
            // XXX is this going to be set on all Gnome platforms?
            LOG.fine("GNOME_KEYRING_PID not set");
            return false;
        }
        String appName;
        try {
            appName = MessageFormat.format(
                    NbBundle.getBundle("org.netbeans.core.windows.view.ui.Bundle").getString("CTL_MainWindow_Title_No_Project"),
                    /*System.getProperty("netbeans.buildnumber")*/"…");
        } catch (MissingResourceException x) {
            appName = "NetBeans"; // NOI18N
        }
        try {
            // Need to do this somewhere, or we get warnings on console.
            // Also used by confirmation dialogs to give the app access to the login keyring.
            LIBRARY.g_set_application_name(appName);
            if (!LIBRARY.gnome_keyring_is_available()) {
                return false;
            }
            SCHEMA = new GnomeKeyringPasswordSchema();
            SCHEMA.item_type = 0; // GNOME_KEYRING_ITEM_GENERIC_SECRET
            SCHEMA.attributes[0] = new GnomeKeyringPasswordSchemaAttribute();
            SCHEMA.attributes[0].name = KEY;
            SCHEMA.attributes[0].type = 0; // GNOME_KEYRING_ATTRIBUTE_TYPE_STRING
            SCHEMA.attributes[1] = null;
            // #178571: try to read some key just to make sure gnome_keyring_find_password_sync is bound:
            read("NoNeXiStEnT"); // NOI18N
            return true;
        } catch (Throwable t) {
            LOG.log(Level.FINE, null, t);
            return false;
        }
    }

    public char[] read(String key) {
        // XXX try to use the char[] directly; not sure how to do this with JNA
        String[] password = {null};
        error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_find_password_sync(SCHEMA, password, KEY, key));
        return password[0] != null ? password[0].toCharArray() : null;
    }

    public void save(String key, char[] password, String description) {
        error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_store_password_sync(
                SCHEMA, null, description != null ? description : key, new String(password), KEY, key));
    }

    public void delete(String key) {
        error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_delete_password_sync(SCHEMA, KEY, key));
    }

    private static String[] ERRORS = {
        "OK", // NOI18N
        "DENIED", // NOI18N
        "NO_KEYRING_DAEMON", // NOI18N
        "ALREADY_UNLOCKED", // NOI18N
        "NO_SUCH_KEYRING", // NOI18N
        "BAD_ARGUMENTS", // NOI18N
        "IO_ERROR", // NOI18N
        "CANCELLED", // NOI18N
        "KEYRING_ALREADY_EXISTS", // NOI18N
        "NO_MATCH", // NOI18N
    };
    private static void error(int code) {
        if (code != 0 && code != 9) {
            LOG.warning("gnome-keyring error: " + ERRORS[code]);
        }
    }

}
