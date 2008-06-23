/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.deprecated.pre61settings;

import java.awt.event.InputEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseSettingsInitializer;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsInitializer;
import org.netbeans.modules.editor.options.OptionUtilities;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.impl.KitsTracker;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.Utilities;

/**
* Customized settings for NetBeans editor
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorSettingsInitializer extends Settings.AbstractInitializer {

    // -J-Dorg.netbeans.modules.editor.NbEditorSettingsInitializer.level=FINE
    private static final Logger LOG = Logger.getLogger(NbEditorSettingsInitializer.class.getName());
    
    public static final String NAME = "nb-editor-settings-initializer"; // NOI18N

    private static boolean mainInitDone;

    public static void init() {
        if (!mainInitDone) {
            mainInitDone = true;
            Settings.addInitializer(new BaseSettingsInitializer(), Settings.CORE_LEVEL);
            Settings.addInitializer(new ExtSettingsInitializer(), Settings.CORE_LEVEL);
            Settings.addInitializer(new NbEditorSettingsInitializer());

            Settings.reset();
        }
    }

    public NbEditorSettingsInitializer() {
        super(NAME);
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {

        if (kitClass == BaseKit.class) {
            settingsMap.put(BaseOptions.TOOLBAR_VISIBLE_PROP, Boolean.TRUE);
            settingsMap.put(BaseOptions.LINE_NUMBER_VISIBLE_PROP, SettingsDefaults.defaultLineNumberVisible);
            
	    //Fix for IZ bug #53744:
	    //On MAC OS X, Ctrl+left click has the same meaning as the right-click.
	    //The hyperlinking should be enabled for the Command key on MAC OS X, for Ctrl on others:
            int activationMask;
            
            activationMask = Utilities.isMac()? InputEvent.META_MASK: InputEvent.CTRL_DOWN_MASK;
            settingsMap.put(SettingsNames.HYPERLINK_ACTIVATION_MODIFIERS, Integer.valueOf(activationMask));
        }

        if (kitClass == NbEditorKit.class) {
            // init popup menu items from layer folder
            if (AllOptionsFolder.getDefault().baseInitialized()){
                // put to the settings map only if base options has been initialized. See #19470
                settingsMap.put(ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST,
                    OptionUtilities.getPopupStrings(OptionUtilities.getGlobalPopupMenuItems())
                );
            }
        }

        List mimeTypes = KitsTracker.getInstance().getMimeTypesForKitClass(kitClass);
        for(Iterator i = mimeTypes.iterator(); i.hasNext(); ) {
            String mimeType = (String) i.next();
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Initializing settings for '" + mimeType + "', " + kitClass); //NOI18N
            }
            
            // Lookup BaseOptions for the given mime type so that it can hook up its
            // own settings initializer.
            MimePath mimePath = MimePath.parse(mimeType);
            BaseOptions bo = (BaseOptions) MimeLookup.getLookup(mimePath).lookup(BaseOptions.class);
            if (bo == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Top level mime type '" + mimeType + "' with no BaseOptions."); //NOI18N
                }
            }
        }
    }
}
