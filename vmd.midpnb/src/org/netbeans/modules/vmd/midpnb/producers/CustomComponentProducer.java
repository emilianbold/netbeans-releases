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
package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.java.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midpnb.components.handlers.SVGMenuEventHandlerCD;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.CancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.netbeans.modules.vmd.midpnb.components.resources.TableModelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGPlayerCD;
import org.netbeans.modules.vmd.midpnb.palette.MidpNbPaletteProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public abstract class CustomComponentProducer extends ComponentProducer {

    public CustomComponentProducer(TypeID typeID, PaletteDescriptor paletteDescriptor) {
        super(typeID.toString(), typeID, paletteDescriptor);
    }

    public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
        Boolean isValid1;
        Boolean isValid2;
        if (useCachedValue) {
            isValid1 = MidpJavaSupport.getCache(document).checkValidityCached("javax.microedition.m2g.SVGImage"); // NOI18N
            isValid2 = MidpJavaSupport.getCache(document).checkValidityCached("javax.microedition.lcdui.Canvas"); // NOI18N
        } else {
            isValid1 = MidpJavaSupport.checkValidity(document, "javax.microedition.m2g.SVGImage"); // NOI18N
            isValid2 = MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
        }
        
        return isValid1 != null && isValid2 != null ? isValid1 && isValid2 : null;
    }

    public static final class SVGPlayerProducer extends CustomComponentProducer {

        public SVGPlayerProducer() {
            super(SVGPlayerCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_SVG_Player"), NbBundle.getMessage(CustomComponentProducer.class, "TTIP_SVG_Player"), SVGPlayerCD.ICON_PATH, SVGPlayerCD.ICON_LARGE_PATH)); // NOI18N
        }
    }

    public static final class SVGFormProducer extends CustomComponentProducer {

        public SVGFormProducer() {
            super(SVGFormCD.TYPEID, 
                    new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG, 
                        NbBundle.getMessage(SVGFormProducer.class, "DISP_SVG_Form"), //NOI18N
                        NbBundle.getMessage(SVGFormProducer.class, "TTIP_SVG_Form"), //NOI18N
                        SVGFormCD.ICON_PATH, 
                        SVGFormCD.ICON_LARGE_PATH));
        }

    }

    public static final class SVGImageProducer extends CustomComponentProducer {

        public SVGImageProducer() {
            super(SVGImageCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_SVG_Image"), NbBundle.getMessage(CustomComponentProducer.class, "TTIP_SVG_Image"), SVGImageCD.ICON_PATH, SVGImageCD.ICON_LARGE_PATH)); // NOI18N
        }
    }

    public static final class SVGMenuEventHandlerProducer extends CustomComponentProducer {

        public SVGMenuEventHandlerProducer() {
            super(SVGMenuEventHandlerCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_SVG_Menu_Action"), NbBundle.getMessage(CustomComponentProducer.class, "TTIP_SVG_Menu_Action"), SVGMenuEventHandlerCD.ICON_PATH, SVGMenuEventHandlerCD.LARGE_ICON_PATH)); // NOI18N
        }
    }

    public static final class TableItemProducer extends CustomComponentProducer {

        public TableItemProducer() {
            super(TableItemCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_Table_Item"), NbBundle.getMessage(CustomComponentProducer.class, "TTIP_Table_Item"), TableItemCD.ICON_PATH, TableItemCD.ICON_LARGE_PATH)); // NOI18N
        }

        @Override
        public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
            if (useCachedValue) {
                return MidpJavaSupport.getCache(document).checkValidityCached("javax.microedition.lcdui.Item"); // NOI18N
            }
            return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Item"); // NOI18N
        }
    }

    public static final class SimpleCancellableTaskProducer extends CustomComponentProducer {

        public SimpleCancellableTaskProducer() {
            super(SimpleCancellableTaskCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_Simple_Cancellable_Task"), NbBundle.getMessage(CustomComponentProducer.class, "TTIP_Simple_Cancellable_Task"), CancellableTaskCD.ICON_PATH, CancellableTaskCD.ICON_LARGE_PATH)); // NOI18N
        }

        @Override
        public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
            if (useCachedValue) {
                return MidpJavaSupport.getCache(document).checkValidityCached("java.lang.Runnable"); // NOI18N
            }
            return MidpJavaSupport.checkValidity(document, "java.lang.Runnable"); // NOI18N
        }
    }

    public static final class SimpleTableModelProducer extends CustomComponentProducer {

        public SimpleTableModelProducer() {
            super(SimpleTableModelCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_Simple_Table_Model"), NbBundle.getMessage(CustomComponentProducer.class, "TTIP_Simple_Table_Model"), TableModelCD.ICON_PATH, TableModelCD.ICON_LARGE_PATH)); // NOI18N
        }

        @Override
        public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
            if (useCachedValue) {
                return MidpJavaSupport.getCache(document).checkValidityCached("javax.microedition.lcdui.Form"); // NOI18N
            }
            return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Form"); // NOI18N
        }
    }
}
