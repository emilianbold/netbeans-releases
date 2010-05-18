/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.schema.ui.basic;

import java.io.IOException;
import java.io.ObjectInput;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Manages the schema editor options.
 *
 * @author  Nathan Fiedler
 */
public class SchemaSettings {
    /** Singleton instance of SchemaSettings */
    private static SchemaSettings INSTANCE = new SchemaSettings();
    /** Name of the connection timeout setting. */
    public static final String PROP_VIEW_MODE = "viewMode";
    /** Name of the user language selection setting. */
    public static final String PROP_LANGUAGE = "language";

    /**
     * The view mode of the editor (e.g. tree, column).
     */
    public static enum ViewMode {
        TREE, COLUMN
    };

    private SchemaSettings() {
        setDefaults();
    }
    
    public String displayName() {
        return NbBundle.getMessage(SchemaSettings.class,
                "CTL_SchemaSettings_name");
    }

    /**
     * Returns the single instance of this class.
     *
     * @return  the instance.
     */
    public static SchemaSettings getDefault() {
        return INSTANCE;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Retrieves the view mode value.
     *
     * @return  view mode.
     */
    public ViewMode getViewMode() {
        String mode = (String) getProperty(PROP_VIEW_MODE);
        if(mode == null)
            return ViewMode.COLUMN;
        
        return ViewMode.valueOf(mode);
    }

    public void readExternal(ObjectInput in) throws
            IOException, ClassNotFoundException {
        //super.readExternal(in);
        // Upgrade the restored instance to include the latest settings.
        setDefaults();
    }

    /**
     * For those properties that have null values, set them to the default.
     */
    private void setDefaults() {
        if (getProperty(PROP_VIEW_MODE) == null) {
            putProperty(PROP_VIEW_MODE, ViewMode.COLUMN.toString());
        }
    }

    /**
     * Sets the view mode value.
     *
     * @param  mode  new view mode value.
     */
    public void setViewMode(ViewMode mode) {
        // Store the enum as a String.
        putProperty(PROP_VIEW_MODE, mode.toString());
    }

    /**
     * Retrieves the language.
     *
     * @return  language as string.
     */
    public String getLanguage() {
        return (String) getProperty(PROP_LANGUAGE);
    }

    /**
     * Sets the language value.
     *
     * @param  language  new language value.
     */
    public void setLanguage(String language) {
        // Store the enum as a String.
        putProperty(PROP_LANGUAGE, language);
    }
    
    protected final String putProperty(String key, String value) {
        String retval = NbPreferences.forModule(SchemaSettings.class).get(key, null);
        if (value != null) {
            NbPreferences.forModule(SchemaSettings.class).put(key, value);
        } else {
            NbPreferences.forModule(SchemaSettings.class).remove(key);
        }
        return retval;
    }
    
    protected final String getProperty(String key) {
        return NbPreferences.forModule(SchemaSettings.class).get(key, ViewMode.COLUMN.toString());
    }
}
