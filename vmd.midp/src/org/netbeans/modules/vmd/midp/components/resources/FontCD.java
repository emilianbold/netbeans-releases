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
package org.netbeans.modules.vmd.midp.components.resources;

import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;

import java.util.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBoxNoUserCode;
import org.openide.util.NbBundle;

/**
 * 
 */

public final class FontCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Font"); // NOI18N

    public static final int VALUE_FACE_SYSTEM = 0;
    public static final int VALUE_FACE_MONOSPACE = 32;
    public static final int VALUE_FACE_PROPORTIONAL = 64;

    public static final int VALUE_FONT_STATIC_TEXT = 0;
    public static final int VALUE_FONT_INPUT_TEXT = 1;

    public static final int VALUE_SIZE_MEDIUM = 0;
    public static final int VALUE_SIZE_SMALL = 8;
    public static final int VALUE_SIZE_LARGE = 16;

    public static final int VALUE_STYLE_PLAIN = 0;
    public static final int VALUE_STYLE_BOLD = 1;
    public static final int VALUE_STYLE_ITALIC = 2;
    public static final int VALUE_STYLE_UNDERLINED = 4;

    public static final int VALUE_KIND_DEFAULT = 0;
    public static final int VALUE_KIND_CUSTOM = 1;
    public static final int VALUE_KIND_STATIC = 2;
    public static final int VALUE_KIND_INPUT = 3;

    public static final String PROP_FACE = "face"; // NOI18N
    public static final String PROP_STYLE = "style"; // NOI18N
    public static final String PROP_SIZE = "bold"; // NOI18N
    public static final String PROP_FONT_KIND = "fontKind"; // NOI18N
    
    public static final String LABEL_KIND_DEFAULT = "Default"; // NOI18N
    public static final String LABEL_KIND_CUSTOM = "Custom"; // NOI18N
    public static final String LABEL_KIND_STATIC = "Static"; // NOI18N
    public static final String LABEL_KIND_INPUT = "Input"; // NOI18N

    public static final String LABEL_FACE_SYSTEM = "SYSTEM"; // NOI18N
    public static final String LABEL_FACE_MONOSPACE = "MONOSPACE"; // NOI18N
    public static final String LABEL_FACE_PROPORTIONAL = "PROPORTIONAL"; // NOI18N

    public static final String LABEL_SIZE_MEDIUM = "MEDIUM"; // NOI18N
    public static final String LABEL_SIZE_LARGE = "LARGE"; // NOI18N
    public static final String LABEL_SIZE_SMALL = "SMALL"; // NOI18N

    public static final String LABEL_STYLE_PLAIN = "PLAIN"; // NOI18N
    public static final String LABEL_STYLE_BOLD = "BOLD"; // NOI18N
    public static final String LABEL_STYLE_ITALIC = "ITALIC"; // NOI18N
    public static final String LABEL_STYLE_UNDERLINED = "UNDERLINED"; // NOI18N

    private static Map<String, PropertyValue> kindTypes;
    private static Map<String, PropertyValue> faceTypes;
    private static Map<String, PropertyValue> sizeTypes;
    private static Map<String, PropertyValue> styleTypes;

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (ResourceCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return  Arrays.asList(
            new PropertyDescriptor(PROP_FONT_KIND, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(VALUE_KIND_DEFAULT), false, false, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_FACE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(VALUE_FACE_SYSTEM), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_SIZE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(VALUE_SIZE_MEDIUM), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_STYLE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(VALUE_STYLE_PLAIN), false, true, MidpVersionable.MIDP)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty(NbBundle.getMessage(FontCD.class, "DISP_Font_Kind"), // NOI18N
                        PropertyEditorComboBoxNoUserCode.createInstance(getKindTypes(), TYPEID), PROP_FONT_KIND)
                    .addProperty(NbBundle.getMessage(FontCD.class, "DISP_Font_Face"), // NOI18N
                        PropertyEditorComboBox.createInstance(getFaceTypes(), TYPEID, TYPEID,
                            NbBundle.getMessage(FontCD.class, "DISP_Font_Face_RB_LABEL"), // NOI18N
                            NbBundle.getMessage(FontCD.class, "DISP_Font_Face_UCLABEL")), PROP_FACE) // NOI18N
                    .addProperty(NbBundle.getMessage(FontCD.class, "DISP_Font_Size"), // NOI18N
                        PropertyEditorComboBox.createInstance(getSizeTypes(), TYPEID, TYPEID,
                            NbBundle.getMessage(FontCD.class, "DISP_Font_Size_RB_LABEL"), // NOI18N
                            NbBundle.getMessage(FontCD.class, "DISP_Font_Size_UCLABEL")), PROP_SIZE) // NOI18N
                    .addProperty(NbBundle.getMessage(FontCD.class, "DISP_Font_Style"), // NOI18N
                        PropertyEditorComboBox.createInstance(getStyleTypes(), TYPEID, TYPEID,
                            NbBundle.getMessage(FontCD.class, "DISP_Font_Style_RB_LABEL"), // NOI18N
                            NbBundle.getMessage(FontCD.class, "DISP_Font_Style_UCLABEL")), PROP_STYLE); // NOI18N
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addSetters (new FontSetter ());
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter(),
            // delete
            DeletePresenter.createRemoveComponentReferences()
        );
    }

    public static Map<String, PropertyValue> getKindTypes() {
        if (kindTypes == null || kindTypes.isEmpty()) {
            kindTypes = new TreeMap<String, PropertyValue>();
            kindTypes.put(LABEL_KIND_DEFAULT, MidpTypes.createIntegerValue(VALUE_KIND_DEFAULT)); // NOI18N
            kindTypes.put(LABEL_KIND_CUSTOM, MidpTypes.createIntegerValue(VALUE_KIND_CUSTOM)); // NOI18N
            kindTypes.put(LABEL_KIND_STATIC, MidpTypes.createIntegerValue(VALUE_KIND_STATIC)); // NOI18N
            kindTypes.put(LABEL_KIND_INPUT, MidpTypes.createIntegerValue(VALUE_KIND_INPUT)); // NOI18N
        }
        return kindTypes;
    }
    
    public static Map<String, PropertyValue> getFaceTypes() {
        if (faceTypes == null || kindTypes.isEmpty()) {
            faceTypes = new TreeMap<String, PropertyValue>();
            faceTypes.put(LABEL_FACE_SYSTEM, MidpTypes.createIntegerValue(VALUE_FACE_SYSTEM));
            faceTypes.put(LABEL_FACE_MONOSPACE, MidpTypes.createIntegerValue(VALUE_FACE_MONOSPACE));
            faceTypes.put(LABEL_FACE_PROPORTIONAL, MidpTypes.createIntegerValue(VALUE_FACE_PROPORTIONAL));
        }
        return faceTypes;
    }

    public static Map<String, PropertyValue>  getSizeTypes() {
        if (sizeTypes == null || kindTypes.isEmpty()) {
            sizeTypes = new TreeMap<String, PropertyValue>();
            sizeTypes.put(LABEL_SIZE_MEDIUM, MidpTypes.createIntegerValue(VALUE_SIZE_MEDIUM));
            sizeTypes.put(LABEL_SIZE_SMALL, MidpTypes.createIntegerValue(VALUE_SIZE_SMALL));
            sizeTypes.put(LABEL_SIZE_LARGE, MidpTypes.createIntegerValue(VALUE_SIZE_LARGE));
        }
        return sizeTypes;
    }

    public static Map<String, PropertyValue>  getStyleTypes() {
        if (styleTypes == null || styleTypes.isEmpty()) {
            styleTypes = new TreeMap<String, PropertyValue>();
            styleTypes.put(LABEL_STYLE_PLAIN, MidpTypes.createIntegerValue(VALUE_STYLE_PLAIN));
            styleTypes.put(LABEL_STYLE_BOLD, MidpTypes.createIntegerValue(VALUE_STYLE_BOLD));
            styleTypes.put(LABEL_STYLE_ITALIC, MidpTypes.createIntegerValue(VALUE_STYLE_ITALIC));
            styleTypes.put(LABEL_STYLE_UNDERLINED, MidpTypes.createIntegerValue(VALUE_STYLE_UNDERLINED));
        }
        return styleTypes;
    }

    public static String getKindByCode(int code) {
        switch (code) {
            case VALUE_KIND_DEFAULT:
                return LABEL_KIND_DEFAULT;
            case VALUE_KIND_CUSTOM:
                return LABEL_KIND_CUSTOM;
            case VALUE_KIND_STATIC:
                return LABEL_KIND_STATIC;
            case VALUE_KIND_INPUT:
                return LABEL_KIND_INPUT;
        }
        return LABEL_KIND_DEFAULT;
    }

    public static String getFaceByCode(int code) {
        switch (code) {
            case VALUE_FACE_SYSTEM:
                return LABEL_FACE_SYSTEM;
            case VALUE_FACE_MONOSPACE:
                return LABEL_FACE_MONOSPACE;
            case VALUE_FACE_PROPORTIONAL:
                return LABEL_FACE_PROPORTIONAL;
        }
        return LABEL_FACE_SYSTEM;
    }

    public static String getStyleByCode(int code) {
        switch (code) {
            case VALUE_STYLE_PLAIN:
                return LABEL_STYLE_PLAIN;
            case VALUE_STYLE_BOLD:
                return LABEL_STYLE_BOLD;
            case VALUE_STYLE_ITALIC:
                return LABEL_STYLE_ITALIC;
            case VALUE_STYLE_UNDERLINED:
                return LABEL_STYLE_UNDERLINED;
        }
        return LABEL_STYLE_PLAIN;
    }

    public static String getSizeByCode(int code) {
        switch (code) {
            case VALUE_SIZE_SMALL:
                return LABEL_SIZE_SMALL;
            case VALUE_SIZE_MEDIUM:
                return LABEL_SIZE_MEDIUM;
            case VALUE_SIZE_LARGE:
                return LABEL_SIZE_LARGE;
        }
        return LABEL_SIZE_MEDIUM;
    }

    public static int getKindByValue(Object value) {
        if (value == LABEL_KIND_DEFAULT) {
            return VALUE_KIND_DEFAULT;
        } else if (value == LABEL_KIND_CUSTOM) {
            return VALUE_KIND_CUSTOM;
        } else if (value == LABEL_KIND_STATIC) {
            return VALUE_KIND_STATIC;
        } else if (value == LABEL_KIND_INPUT) {
            return VALUE_KIND_INPUT;
        }
        return VALUE_KIND_DEFAULT;
    }

    public static int getFaceByValue(Object value) {
        if (value == LABEL_FACE_SYSTEM) {
            return VALUE_FACE_SYSTEM;
        } else if (value == LABEL_FACE_MONOSPACE) {
            return VALUE_FACE_MONOSPACE;
        } else if (value == LABEL_FACE_PROPORTIONAL) {
            return VALUE_FACE_PROPORTIONAL;
        }
        return VALUE_FACE_SYSTEM;
    }

    public static int getStyleByValue(Object value) {
        if (value == LABEL_STYLE_PLAIN) {
            return VALUE_STYLE_PLAIN;
        } else if (value == LABEL_STYLE_BOLD) {
            return VALUE_STYLE_BOLD;
        } else if (value == LABEL_STYLE_ITALIC) {
            return VALUE_STYLE_ITALIC;
        } else if (value == LABEL_STYLE_UNDERLINED) {
            return VALUE_STYLE_UNDERLINED;
        }
        return VALUE_STYLE_PLAIN;
    }

    public static int getSizeByValue(Object value) {
        if (value == LABEL_SIZE_SMALL) {
            return VALUE_SIZE_SMALL;
        } else if (value == LABEL_SIZE_MEDIUM) {
            return VALUE_SIZE_MEDIUM;
        } else if (value == LABEL_SIZE_LARGE) {
            return VALUE_SIZE_LARGE;
        }
        return VALUE_SIZE_MEDIUM;
    }

    private static class FontSetter implements Setter {

        public boolean isConstructor () {
            return true;
        }

        public TypeID getConstructorRelatedTypeID () {
            return TYPEID;
        }

        public int getPriority() {
            return 0;
        }

        public String getSetterName () {
            return "Font.getFont"; // NOI18N
        }

        public Versionable getVersionable () {
            return MidpVersionable.MIDP;
        }

        public void generateSetterCode (MultiGuardedSection section, DesignComponent component, Map<String, Parameter> name2parameter) {
            CodeWriter writer = section.getWriter ();
            writer.write (CodeReferencePresenter.generateDirectAccessCode (component)).write (" = "); // NOI18N

            PropertyValue propertyValue = component.readProperty (PROP_FONT_KIND);
            switch (MidpTypes.getInteger (propertyValue)) {
                case VALUE_KIND_DEFAULT:
                    writer.write ("Font.getDefaultFont ()"); // NOI18N
                    break;
                case VALUE_KIND_INPUT:
                    writer.write ("Font.getFont (Font.FONT_INPUT_TEXT)"); // NOI18N
                    break;
                case VALUE_KIND_STATIC:
                    writer.write ("Font.getFont (Font.FONT_STATIC_TEXT)"); // NOI18N
                    break;
                case VALUE_KIND_CUSTOM:

                    writer.write ("Font.getFont ("); // NOI18N

                    propertyValue = component.readProperty (PROP_FACE);
                    if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                        switch (MidpTypes.getInteger (propertyValue)) {
                            case VALUE_FACE_SYSTEM: writer.write ("Font.FACE_SYSTEM"); break; // NOI18N
                            case VALUE_FACE_MONOSPACE: writer.write ("Font.FACE_MONOSPACE"); break; // NOI18N
                            case VALUE_FACE_PROPORTIONAL: writer.write ("Font.FACE_PROPORTIONAL"); break; // NOI18N
                            default: throw Debug.illegalState ();
                        }
                    } else
                        MidpCodeSupport.generateCodeForPropertyValue (writer, propertyValue);

                    writer.write (", "); // NOI18N

                    propertyValue = component.readProperty (PROP_STYLE);
                    if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                        int code = MidpTypes.getInteger(propertyValue);
                        if (VALUE_STYLE_PLAIN == code) {
                            writer.write("Font.STYLE_PLAIN"); // NOI18N
                        } else {
                            boolean needOR = false;
                            if ((code & VALUE_STYLE_BOLD) != 0) {
                                writer.write("Font.STYLE_BOLD"); // NOI18N
                                needOR = true;
                            }
                            if ((code & VALUE_STYLE_ITALIC) != 0) {
                                if (needOR) {
                                    writer.write(" | "); // NOI18N
                                }
                                writer.write("Font.STYLE_ITALIC"); // NOI18N
                                needOR = true;
                            }
                            if ((code & VALUE_STYLE_UNDERLINED) != 0) {
                                if (needOR) {
                                    writer.write(" | "); // NOI18N
                                }
                                writer.write("Font.STYLE_UNDERLINED"); // NOI18N
                            }
                        }
                    } else
                        MidpCodeSupport.generateCodeForPropertyValue (writer, propertyValue);

                    writer.write (", "); // NOI18N

                    propertyValue = component.readProperty (PROP_SIZE);
                    if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                        switch (MidpTypes.getInteger (propertyValue)) {
                            case VALUE_SIZE_SMALL: writer.write ("Font.SIZE_SMALL"); break; // NOI18N
                            case VALUE_SIZE_MEDIUM: writer.write ("Font.SIZE_MEDIUM"); break; // NOI18N
                            case VALUE_SIZE_LARGE: writer.write ("Font.SIZE_LARGE"); break; // NOI18N
                            default: throw Debug.illegalState ();
                        }
                    } else
                        MidpCodeSupport.generateCodeForPropertyValue (writer, propertyValue);

                    writer.write (")"); // NOI18N
                    break;
                default:
                    throw Debug.illegalState ();
            }

            writer.write (";\n"); // NOI18N
        }

        public List<String> getParameters () {
            return Collections.emptyList ();
        }

    }

}
