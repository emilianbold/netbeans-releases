/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class HintMetadata {

    public final String id;
    public final String displayName;
    public final String description;
    public final String category;
    public final boolean enabled;
    public final Kind kind;
    public final HintSeverity severity;
    public final Collection<? extends String> suppressWarnings;
    public final CustomizerProvider customizer;
    public final boolean showInTaskList = false;

    HintMetadata(String id, String displayName, String description, String category, boolean enabled, Kind kind, HintSeverity severity, CustomizerProvider customizer, Collection<? extends String> suppressWarnings) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.enabled = enabled;
        this.kind = kind;
        this.severity = severity;
        this.customizer = customizer;
        this.suppressWarnings = suppressWarnings;
    }

    public static HintMetadata create(String id, String displayName, String description, String category, boolean enabled, Kind kind, HintSeverity severity, Collection<? extends String> suppressWarnings) {
        return create(id, displayName, description, category, enabled, kind, severity, null, suppressWarnings);
    }

    public static HintMetadata create(String id, String displayName, String description, String category, boolean enabled, Kind kind, HintSeverity severity, CustomizerProvider customizer, Collection<? extends String> suppressWarnings) {
        return new HintMetadata(id, displayName, description, category, enabled, kind, severity, customizer, suppressWarnings);
    }

    public static HintMetadata create(String id, String bundleForFQN, String category, boolean enabled, HintSeverity severity, Kind kind, String... suppressWarnings) {
        return create(id, bundleForFQN, category, enabled, severity, kind, suppressWarnings);
    }

    public static HintMetadata create(String id, String bundleForFQN, String category, boolean enabled, HintSeverity severity, Kind kind, CustomizerProvider customizer, String... suppressWarnings) {
        ResourceBundle bundle;

        try {
            int lastDot = bundleForFQN.lastIndexOf('.');

            assert lastDot >= 0;

            bundle = NbBundle.getBundle(bundleForFQN.substring(0, lastDot + 1) + "Bundle");
        } catch (MissingResourceException mre) {
            Logger.getLogger(HintMetadata.class.getName()).log(Level.FINE, null, mre);
            bundle = null;
        }

        return create(id, bundle, category, enabled, severity, kind, customizer, suppressWarnings);
    }

    public static HintMetadata create(String id, ResourceBundle bundle, String category, boolean enabled, HintSeverity severity, Kind kind, CustomizerProvider customizer, String... suppressWarnings) {
        String displayName = lookup(bundle, "DN_" + id, "No Display Name");
        String description = lookup(bundle, "DESC_" + id, "No Description");

        return new HintMetadata(id, displayName, description, category, enabled, kind, severity, customizer, Arrays.asList(suppressWarnings));
    }

    private static String lookup(ResourceBundle bundle, String key, String def) {
        try {
            return bundle != null ? bundle.getString(key) : def;
        } catch (MissingResourceException mre) {
            Logger.getLogger(HintMetadata.class.getName()).log(Level.FINE, null, mre);
            return def;
        }
    }

    public enum Kind {
        HINT,
        HINT_NON_GUI,
        SUGGESTION,
        SUGGESTION_NON_GUI;
    }
}
