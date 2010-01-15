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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

/**
 * @see <a href="http://library.gnome.org/devel/gnome-keyring/stable/">gnome-keyring API Reference</a>
 */
public interface GnomeKeyringLibrary extends Library {

    GnomeKeyringLibrary LIBRARY = (GnomeKeyringLibrary) Native.loadLibrary("gnome-keyring", GnomeKeyringLibrary.class);

    boolean gnome_keyring_is_available();

    int gnome_keyring_store_password_sync(GnomeKeyringPasswordSchema schema,
                                                         String keyring,
                                                         String display_name,
                                                         String password,
                                                         String... attrs);

    int gnome_keyring_find_password_sync(GnomeKeyringPasswordSchema schema,
                                         String[] password,
                                         String... attrs);

    int gnome_keyring_delete_password_sync(GnomeKeyringPasswordSchema schema,
                                           String... attrs);

    void g_set_application_name(String name);

    class GnomeKeyringPasswordSchema extends Structure {
        public int item_type;
        public GnomeKeyringPasswordSchemaAttribute[] attributes = new GnomeKeyringPasswordSchemaAttribute[32];
    }

    class GnomeKeyringPasswordSchemaAttribute extends Structure {
        public String name;
        public int type;
    }

}
