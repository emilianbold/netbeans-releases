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
package org.netbeans.modules.visualweb.designer.jsf;

import java.beans.IntrospectionException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.nodes.BeanNode;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;


/** JSF Designer settings.
 *
 * @todo Possible new designer options: Show table borders, show named
 *       anchors, show &lt;br/&gt;'s, show tags
 *
 * @author Po-Ting Wu
 * @author Tor Norbye
 * @author Peter Zavadsky (migration to NB Preferences)
 */
public class JsfDesignerPreferences {

    // Constants for the page size
    private static final int CONSTRAINTS_UNINITIALIZED = -1;
    public static final int CONSTRAINTS_NONE = 0;
    public static final int CONSTRAINTS_640x480 = 1;
    public static final int CONSTRAINTS_800x600 = 2;
    public static final int CONSTRAINTS_1024x768 = 3;
    public static final int CONSTRAINTS_1280x1024 = 4;

    /** Value of the default font size used in designer. */
    private static final int DEFAULT_FONT_SIZE = 16;

    /** show grid */
    public static final String PROP_GRID_SHOW = "gridShow"; // NOI18N

    /** snap to grid */
    public static final String PROP_GRID_SNAP = "gridSnap"; // NOI18N

    /** grid width */
    public static final String PROP_GRID_WIDTH = "gridWidth"; // NOI18N

    /** grid height */
    public static final String PROP_GRID_HEIGHT = "gridHeight"; // NOI18N

    /** page resolution */
    public static final String PROP_PAGE_SIZE = "pageSize"; // NOI18N

    /** Property name of 'decorations' property. */
    public static final String PROP_SHOW_DECORATIONS = "showDecorations"; // NOI18N

    /** Default size of the font used in the designer.
     * XXX There might be needed also default font itself to be present. */
    public static final String PROP_DEFAULT_FONT_SIZE = "defaultFontSize"; // NOI18N

    /** XXX To speed up getPageWidht/getPageHeight. */
    private transient int constraints = CONSTRAINTS_UNINITIALIZED; // cache

    /** Default font size property. */
    private transient int defaultFontSize;

    
    private static final JsfDesignerPreferences INSTANCE = new JsfDesignerPreferences();

    // Default instance of this system option, for the convenience of associated classes.
    public static JsfDesignerPreferences getInstance() {
        return INSTANCE;
    }
    
    public static BeanNode createViewNode() throws IntrospectionException {
        return new BeanNode<JsfDesignerPreferences>(INSTANCE);
    }
    
    
    public void addWeakPreferenceChangeListener(PreferenceChangeListener l) {
        Preferences designerPreferences = getPreferences();
        designerPreferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, l, designerPreferences));
    }

//    // do NOT use constructore for setting default values
//    protected void initialize() {
//        // Set default values of properties
//        super.initialize();
//
//        putProperty(PROP_GRID_SHOW, Boolean.TRUE, true);
//        putProperty(PROP_GRID_SNAP, Boolean.TRUE, true);
//        putProperty(PROP_GRID_WIDTH, new Integer(24), true);
//        putProperty(PROP_GRID_HEIGHT, new Integer(24), true);
//
//        putProperty(PROP_SHOW_DECORATIONS, Boolean.FALSE, true);
//    }

//    // This method must be overriden. It returns display name of this options.
//    public String displayName() {
//        return NbBundle.getBundle(DesignerSettings.class).getString("CTL_DesignerSettings");
//    }
//
//    public HelpCtx getHelpCtx() {
//        return HelpCtx.findHelp("projrave_ui_elements_options_visual_editor"); // NOI18N
//    }

    public void setGridShow(boolean set) {
//        putProperty(PROP_GRID_SHOW, set ? Boolean.TRUE : Boolean.FALSE, true);
        getPreferences().putBoolean(PROP_GRID_SHOW, set);
    }
    
    public boolean getGridShow() {
//        return ((Boolean)getProperty(PROP_GRID_SHOW)).booleanValue();
        return getPreferences().getBoolean(PROP_GRID_SHOW, true);
    }

    public void setGridSnap(boolean set) {
//        putProperty(PROP_GRID_SNAP, set ? Boolean.TRUE : Boolean.FALSE, true);
        getPreferences().putBoolean(PROP_GRID_SNAP, set);
    }
    
    public boolean getGridSnap() {
//        return ((Boolean)getProperty(PROP_GRID_SNAP)).booleanValue();
        return getPreferences().getBoolean(PROP_GRID_SNAP, true);
    }

    public void setGridWidth(int width) {
        if (width < 4) {
            width = 4;
        }

//        putProperty(PROP_GRID_WIDTH, new Integer(width), true);
        getPreferences().putInt(PROP_GRID_WIDTH, width);
    }
    
    public int getGridWidth() {
//        return ((Integer)getProperty(PROP_GRID_WIDTH)).intValue();
        return getPreferences().getInt(PROP_GRID_WIDTH, 24);
    }

    public void setGridHeight(int height) {
        if (height < 4) {
            height = 4;
        }

//        putProperty(PROP_GRID_HEIGHT, new Integer(height), true);
        getPreferences().putInt(PROP_GRID_HEIGHT, height);
    }

    public int getGridHeight() {
//        return ((Integer)getProperty(PROP_GRID_HEIGHT)).intValue();
        return getPreferences().getInt(PROP_GRID_HEIGHT, 24);
    }

    public void setPageSize(int size) {
        // XXX Speeding up the getPageSizeWidth/getPageSizeHeight methods.
        constraints = size;
        // if CONSTRAINTS_NONE, remove it?
//        putProperty(PROP_PAGE_SIZE, new Integer(size), true);
        getPreferences().putInt(PROP_PAGE_SIZE, size);
    }

    public int getPageSize() {
//        Object o = getProperty(PROP_PAGE_SIZE);
//
//        if (o == null) {
//            return CONSTRAINTS_NONE;
//        }
//
//        return ((Integer)o).intValue();
        return getPreferences().getInt(PROP_PAGE_SIZE, CONSTRAINTS_NONE);
    }

    /** Synthesized from the page size property: get the resolution width. -1 means no constraint. */
    public int getPageSizeWidth() {
        if (constraints == CONSTRAINTS_UNINITIALIZED) {
            constraints = getPageSize();
        }

        switch (constraints) {
        case CONSTRAINTS_640x480: // 600x300
            return 600;

        case CONSTRAINTS_800x600: // 760x420
            return 760;

        case CONSTRAINTS_1024x768: // 955x600
            return 955;

        case CONSTRAINTS_1280x1024: // 1210x856
            return 1210;

        case CONSTRAINTS_NONE:default:
            return -1;
        }
    }

    /** Synthesized from the page size property: get the resolution height. -1 means no constraint. */
    public int getPageSizeHeight() {
        if (constraints == CONSTRAINTS_UNINITIALIZED) {
            constraints = getPageSize();
        }

        switch (constraints) {
        case CONSTRAINTS_640x480: // 600x300
            return 300;

        case CONSTRAINTS_800x600: // 760x420
            return 420;

        case CONSTRAINTS_1024x768: // 955x600
            return 600;

        case CONSTRAINTS_1280x1024: // 1210x856
            return 856;

        case CONSTRAINTS_NONE:default:
            return -1;
        }
    }
    
    public void setShowDecorations(boolean showDecorations) {
//        putProperty(PROP_SHOW_DECORATIONS, Boolean.valueOf(showDecorations), true);
        getPreferences().putBoolean(PROP_SHOW_DECORATIONS, showDecorations);
    }

    public boolean isShowDecorations() {
//        return ((Boolean)getProperty(PROP_SHOW_DECORATIONS)).booleanValue();
        return getPreferences().getBoolean(PROP_SHOW_DECORATIONS, false);
    }
    
    /** Sets default font size used in the designer.
     * @exception IllegalArgumentException in case the specified font size is zero or negative */
    public void setDefaultFontSize(int defaultFontSize) {
        if (defaultFontSize <= 0) {
            throw new IllegalArgumentException("Zero or negative font size is not allowed, size=" + defaultFontSize); // NOI18N
        }
        this.defaultFontSize = defaultFontSize;
//        putProperty(PROP_DEFAULT_FONT_SIZE, new Integer(defaultFontSize));
        getPreferences().putInt(PROP_DEFAULT_FONT_SIZE, defaultFontSize);
    }
    
    public int getDefaultFontSize() {
        if (defaultFontSize > 0) {
            return defaultFontSize;
        }
        
//        Integer size = (Integer)getProperty(PROP_DEFAULT_FONT_SIZE);
//        if (size != null) {
//            defaultFontSize = size.intValue();
//        }
//        
//        if (defaultFontSize <= 0) {
//            defaultFontSize = DEFAULT_FONT_SIZE;
//        }
        defaultFontSize = getPreferences().getInt(PROP_DEFAULT_FONT_SIZE, DEFAULT_FONT_SIZE);
        return defaultFontSize;
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(JsfDesignerPreferences.class);
    }
}
