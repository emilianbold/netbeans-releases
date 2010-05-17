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

package org.netbeans.modules.vmd.midp.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.java.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.components.displayables.*;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.netbeans.modules.vmd.midp.components.resources.ResourceCD;
import org.netbeans.modules.vmd.midp.components.items.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public abstract class MidpComponentProducer extends ComponentProducer {

    public MidpComponentProducer(TypeID typeID, PaletteDescriptor paletteDescriptor) {
        super(typeID.toString(), typeID, paletteDescriptor);
    }

    public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
        if (useCachedValue) {
            return MidpJavaSupport.getCache(document).checkValidityCached(getMainComponentTypeID());
        }
        return MidpJavaSupport.checkValidity(document, getMainComponentTypeID());
    }

    public static final class Form extends MidpComponentProducer {
        public Form() {
            super(FormCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(MidpComponentProducer.class, "DISP_Form"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_Form"), FormCD.ICON_PATH, FormCD.ICON_LARGE_PATH)); // NOI18N
        }
    }

    public static final class Alert extends MidpComponentProducer {
        public Alert() {
            super(AlertCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(MidpComponentProducer.class, "DISP_Alert"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_Alert"), AlertCD.ICON_PATH, AlertCD.ICON_LARGE_PATH)); // NOI18N
        }
    }

    public static final class TextBox extends MidpComponentProducer {
        public TextBox() {
            super(TextBoxCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(MidpComponentProducer.class, "DISP_TextBox"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_TextBox"), TextBoxCD.ICON_PATH, TextBoxCD.ICON_LARGE_PATH)); // NOI18N
        }
    }

    public static final class ChoiceGroup extends MidpComponentProducer {
        public ChoiceGroup() {
            super(ChoiceGroupCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, NbBundle.getMessage(MidpComponentProducer.class, "DISP_ChoiceGroup"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_ChoiceGroup"), ItemCD.ICON_PATH, ItemCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class Gauge extends MidpComponentProducer {
        public Gauge() {
            super(GaugeCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, NbBundle.getMessage(MidpComponentProducer.class, "DISP_Gauge"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_Gauge"), ItemCD.ICON_PATH, ItemCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class Spacer extends MidpComponentProducer {
        public Spacer() {
            super(SpacerCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, NbBundle.getMessage(MidpComponentProducer.class, "DISP_Spacer"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_Spacer"), ItemCD.ICON_PATH, ItemCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class DateField extends MidpComponentProducer {
        public DateField() {
            super(DateFieldCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, NbBundle.getMessage(MidpComponentProducer.class, "DISP_DateField"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_DateField"), ItemCD.ICON_PATH, ItemCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class ImageItem extends MidpComponentProducer {
        public ImageItem() {
            super(ImageItemCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, NbBundle.getMessage(MidpComponentProducer.class, "DISP_ImageItem"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_ImageItem"), ItemCD.ICON_PATH, ItemCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class StringItem extends MidpComponentProducer {
        public StringItem() {
            super(StringItemCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, NbBundle.getMessage(MidpComponentProducer.class, "DISP_StringItem"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_StringItem"), ItemCD.ICON_PATH, ItemCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class CustomItem extends MidpComponentProducer {
        public CustomItem() {
            super(CustomItemCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, NbBundle.getMessage(MidpComponentProducer.class, "DISP_CustomItem"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_CustomItem"), ItemCD.ICON_PATH, ItemCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class Font extends MidpComponentProducer {
        public Font() {
            super(FontCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES, NbBundle.getMessage(MidpComponentProducer.class, "DISP_Font"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_Font"), ResourceCD.ICON_PATH, ResourceCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class Image extends MidpComponentProducer {
        public Image() {
            super(ImageCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES, NbBundle.getMessage(MidpComponentProducer.class, "DISP_Image"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_Image"), ResourceCD.ICON_PATH, ResourceCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class Ticker extends MidpComponentProducer {
        public Ticker() {
            super(TickerCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES, NbBundle.getMessage(MidpComponentProducer.class, "DISP_Ticker"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_Ticker"), ResourceCD.ICON_PATH, ResourceCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class TextField extends MidpComponentProducer {
        public TextField() {
            super(TextFieldCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, NbBundle.getMessage(MidpComponentProducer.class, "DISP_TextField"), NbBundle.getMessage(MidpComponentProducer.class, "TTIP_TextField"), ItemCD.ICON_PATH, ItemCD.LARGE_ICON_PATH)); // NOI18N
        }
    }
   
}
