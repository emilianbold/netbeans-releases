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
package com.sun.jsfcl.std.reference;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import com.sun.jsfcl.util.LoggerUtil;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ReferenceDataManager {
    public static final String CHARACTER_SETS = "character-sets"; //NOI18N
    public static final String COMMAND_BUTTON_TYPES = "command-button-types"; // NOI18N
    public static final String COMPONENT_IDS = "component-ids"; //NOI18N
    public static final String CONCURRENCY_TYPES = "concurrency-types"; //NOI18N
    public static final String DATASOURCE_NAMES = "datasource-names"; //NOI18N
    public static final String DATETIME_STYLES = "datetime-style"; //NOI18N
    public static final String DATETIME_TYPES = "datetime-type"; //NOI18N
    public static final String FRAME_TARGETS = "frame-targets"; //NOI18N
    public static final String IMAGE_MAP_NAMES = "image-map-names"; //NOI18N
    public static final String LANGUAGE_CODES = "language-code"; //NOI18N
    public static final String LINK_TYPES = "link-types"; //NOI18N
    public static final String LOCALES = "locales"; //NOI18N
    public static final String MANY_CHECKBOX_LAYOUT_STYLES = "many-chekbox-layout-styles"; //NOI18N
    public static final String MEDIA_TYPES = "media-types"; //NOI18N
    public static final String MESSAGES_LAYOUTS = "messages-layouts"; //NOI18N
    public static final String REGION_SHAPES = "region-shape"; // NOI18N
    public static final String STYLE_CLASSES = "style-classes"; // NOI18N
    public static final String TABLE_RULES = "table-rules"; // NOI18N
    public static final String TEXT_DIRECTIONS = "text-direction"; // NOI18N
    public static final String TFRAMES = "tframes"; //NOI18N
    public static final String TIME_ZONES = "time-zones"; //NOI18N

    protected static ReferenceDataManager instance;
    public static LoggerUtil loggerUtil;
    protected static WeakHashMap projectRelatedReferenceDataMap = new WeakHashMap();

    protected Map definersByName;
    protected Map referenceDataByName;
    protected Map referenceDataByProperty;

    static {

        instance = new ReferenceDataManager();
        loggerUtil = LoggerUtil.getLogger(ReferenceDataManager.class.getName());
    }

    public static ReferenceDataManager getInstance() {

        return instance;
    }

    protected static void setInstance(ReferenceDataManager instance) {

        ReferenceDataManager.instance = instance;
    }

    /**
     *
     */
    protected ReferenceDataManager() {

        super();
        referenceDataByName = new HashMap();
        referenceDataByProperty = new WeakHashMap();
    }

    public BaseReferenceData getBaseReferenceData(String name) {
        BaseReferenceData result;

        result = (BaseReferenceData)referenceDataByName.get(name);
        if (result == null) {
            ReferenceDataDefiner definer;

            definer = getDefiner(name);
            if (definer == null) {
                throw new RuntimeException("No definer defined for: " + name); //NOI18N
            }
            if (!definer.definesBaseItems()) {
                return null;
            }
            result = new BaseReferenceData(this, definer, name);
            referenceDataByName.put(name, result);
        }
        return result;
    }

    public ReferenceDataDefiner getDefiner(String name) {
        ReferenceDataDefiner result;

        result = (ReferenceDataDefiner)getDefinersByName().get(name);
        if (result == null) {
            assert loggerUtil.warning("Found no ReferenceDataDefiner registered under: " + name); //NOI18N
        }
        return result;
    }

    protected Map getDefinersByName() {

        if (definersByName == null) {
            initializeDefinersByName();
        }
        return definersByName;
    }

    public DesignPropertyAttachedReferenceData getDesignPropertyAttachedReferenceData(String name,
        DesignProperty property) {
        DesignPropertyAttachedReferenceData result;
        Map byName;

        byName = (Map)referenceDataByProperty.get(property);
        if (byName == null) {
            byName = new HashMap();
            referenceDataByProperty.put(property, byName);
        }
        result = (DesignPropertyAttachedReferenceData)byName.get(name);
        if (result == null) {
            ReferenceDataDefiner definer;

            definer = getDefiner(name);
            if (definer == null) {
                throw new RuntimeException("No definer defined for: " + name); //NOI18N
            }
            if (!definer.definesDesignPropertyItems()) {
                return null;
            }
            result = new DesignPropertyAttachedReferenceData(this, definer, name, property);
            byName.put(name, result);
        }
        return result;
    }

    /**
     * I return a brand new instance of me for every call.
     *
     * @param name
     * @param project
     * @param liveProperty
     * @return
     */
    public CompositeReferenceData getCompositeReferenceData(String name, DesignProject project,
        DesignProperty liveProperty) {

        return new CompositeReferenceData(
            this,
            name,
            getDefiner(name),
            getBaseReferenceData(name),
            getProjectAttachedReferenceData(name, project),
            getDesignPropertyAttachedReferenceData(name, liveProperty));
    }

    public ProjectAttachedReferenceData getProjectAttachedReferenceData(String name,
        DesignProject project) {

        HashMap byNameMap = (HashMap)projectRelatedReferenceDataMap.get(project);
        if (byNameMap == null) {
            byNameMap = new HashMap();
            projectRelatedReferenceDataMap.put(project, byNameMap);
        }
        ProjectAttachedReferenceData referenceData = (ProjectAttachedReferenceData)byNameMap.get(
            name);
        if (referenceData != null) {
            return referenceData;
        }

        ReferenceDataDefiner definer = getDefiner(name);
        if (definer == null) {
            throw new RuntimeException("No definer defined for: " + name); //NOI18N
        }
        if (!definer.definesProjectItems()) {
            return null;
        }
        referenceData = new ProjectAttachedReferenceData(this, definer, name, project);
        byNameMap.put(name, referenceData);
        return referenceData;
    }

    protected void initializeDefinersByName() {

        definersByName = new HashMap();
        definersByName.put(CHARACTER_SETS, new CharacterSetsReferenceDataDefiner());
        definersByName.put(COMMAND_BUTTON_TYPES, new CommandButtonTypesReferenceDataDefiner());
        definersByName.put(COMPONENT_IDS, new ComponentIdsReferenceDataDefiner());
        definersByName.put(CONCURRENCY_TYPES, new ConcurrencyTypesReferenceDataDefiner());
        definersByName.put(DATASOURCE_NAMES, new DataSourceNamesReferenceDataDefiner());
        definersByName.put(DATETIME_STYLES, new DateTimeStylesReferenceDataDefiner());
        definersByName.put(DATETIME_TYPES, new DateTimeTypesReferenceDataDefiner());
        definersByName.put(FRAME_TARGETS, new FrameTargetsReferenceDataDefiner());
        definersByName.put(IMAGE_MAP_NAMES, new ImageMapNamesReferenceDataDefiner());
        definersByName.put(LANGUAGE_CODES, new LanguageCodesReferenceDataDefiner());
        definersByName.put(LINK_TYPES, new LinkTypesReferenceDataDefiner());
        definersByName.put(LOCALES, new LocalesReferenceDataDefiner());
        definersByName.put(MANY_CHECKBOX_LAYOUT_STYLES,
            new ManyCheckboxLayoutStylesReferenceDataDefiner());
        definersByName.put(MEDIA_TYPES, new MediaTypesReferenceDataDefiner());
        definersByName.put(MESSAGES_LAYOUTS, new MessagesLayoutsReferenceDataDefiner());
        definersByName.put(REGION_SHAPES, new RegionShapesReferenceDataDefiner());
        definersByName.put(STYLE_CLASSES, new StyleClassesReferenceDataDefiner());
        definersByName.put(TABLE_RULES, new TableRulesReferenceDataDefiner());
        definersByName.put(TEXT_DIRECTIONS, new TextDirectionsReferenceDataDefiner());
        definersByName.put(TFRAMES, new TFramesReferenceDataDefiner());
        definersByName.put(TIME_ZONES, new TimeZonesReferenceDataDefiner());
    }

}
