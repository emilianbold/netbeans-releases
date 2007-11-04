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
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.palette.PaletteSupport;
import org.netbeans.modules.vmd.midp.java.JavaClassNameResolver;
import org.netbeans.modules.vmd.midp.java.ResolveListener;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midpnb.components.handlers.SVGMenuEventHandlerCD;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.CancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.netbeans.modules.vmd.midpnb.components.resources.TableModelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGPlayerCD;
import org.netbeans.modules.vmd.midpnb.palette.MidpNbPaletteProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public abstract class CustomComponentProducer extends ComponentProducer implements ResolveListener {

    public CustomComponentProducer(TypeID typeID, PaletteDescriptor paletteDescriptor) {
        super(typeID.toString(), typeID, paletteDescriptor);
    }

    public boolean checkValidity(DesignDocument document) {
        return true;
    }

    public void resolveFinished() {
        PaletteSupport.schedulePaletteRefresh();
    }
    
    public void resolveExpired() {
        PaletteSupport.schedulePaletteRefresh();
    }

    public static final class SVGPlayerProducer extends CustomComponentProducer {

        public SVGPlayerProducer() {
            super(SVGPlayerCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_SVG_Player"), // NOI18N
                    NbBundle.getMessage(CustomComponentProducer.class, "TTIP_SVG_Player"), // NOI18N
                    SVGPlayerCD.ICON_PATH, SVGPlayerCD.ICON_LARGE_PATH));
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            JavaClassNameResolver resolver = JavaClassNameResolver.getInstance(document);
            resolver.addResolveListenerIfNotRegistered(this);
            Boolean isValid1 = resolver.isValid("javax.microedition.m2g.SVGImage"); // NOI18N
            Boolean isValid2 = resolver.isValid("javax.microedition.lcdui.Canvas"); // NOI18N
            return isValid1 != null && isValid2 != null ? isValid1 && isValid2 : true;
        }
    }

    public static final class SVGImageProducer extends CustomComponentProducer {

        public SVGImageProducer() {
            super(SVGImageCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_SVG_Image"), // NOI18N
                    NbBundle.getMessage(CustomComponentProducer.class, "TTIP_SVG_Image"), // NOI18N
                    SVGImageCD.ICON_PATH, SVGImageCD.ICON_LARGE_PATH));
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            JavaClassNameResolver resolver = JavaClassNameResolver.getInstance(document);
            resolver.addResolveListenerIfNotRegistered(this);
            Boolean isValid1 = resolver.isValid("javax.microedition.m2g.SVGImage"); // NOI18N
            Boolean isValid2 = resolver.isValid("javax.microedition.lcdui.Canvas"); // NOI18N
            return isValid1 != null && isValid2 != null ? isValid1 && isValid2 : true;
        }
    }

    public static final class SVGMenuEventHandlerProducer extends CustomComponentProducer {

        public SVGMenuEventHandlerProducer() {
            super(SVGMenuEventHandlerCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_SVG_Menu_Action"), // NOI18N
                    NbBundle.getMessage(CustomComponentProducer.class, "TTIP_SVG_Menu_Action"), // NOI18N
                    SVGMenuEventHandlerCD.ICON_PATH, SVGMenuEventHandlerCD.LARGE_ICON_PATH));
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            JavaClassNameResolver resolver = JavaClassNameResolver.getInstance(document);
            resolver.addResolveListenerIfNotRegistered(this);
            Boolean isValid1 = resolver.isValid("javax.microedition.m2g.SVGImage"); // NOI18N
            Boolean isValid2 = resolver.isValid("javax.microedition.lcdui.Canvas"); // NOI18N
            return isValid1 != null && isValid2 != null ? isValid1 && isValid2 : true;
        }
    }

    public static final class TableItemProducer extends CustomComponentProducer {

        public TableItemProducer() {
            super(TableItemCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_Table_Item"), // NOI18N
                    NbBundle.getMessage(CustomComponentProducer.class, "TTIP_Table_Item"), // NOI18N
                    TableItemCD.ICON_PATH, TableItemCD.ICON_LARGE_PATH));
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            JavaClassNameResolver resolver = JavaClassNameResolver.getInstance(document);
            resolver.addResolveListenerIfNotRegistered(this);
            Boolean isValid = resolver.isValid("javax.microedition.lcdui.Item"); // NOI18N
            return isValid != null ? isValid : true;
        }
    }

    public static final class SimpleCancellableTaskProducer extends CustomComponentProducer {

        public SimpleCancellableTaskProducer() {
            super(SimpleCancellableTaskCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_Simple_Cancellable_Task"), // NOI18N
                    NbBundle.getMessage(CustomComponentProducer.class, "TTIP_Simple_Cancellable_Task"), // NOI18N
                    CancellableTaskCD.ICON_PATH, CancellableTaskCD.ICON_LARGE_PATH));
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            JavaClassNameResolver resolver = JavaClassNameResolver.getInstance(document);
            resolver.addResolveListenerIfNotRegistered(this);
            Boolean isValid = resolver.isValid("java.lang.Runnable"); // NOI18N
            return isValid != null ? isValid : true;
        }
    }

    public static final class SimpleTableModelProducer extends CustomComponentProducer {

        public SimpleTableModelProducer() {
            super(SimpleTableModelCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES,
                    NbBundle.getMessage(CustomComponentProducer.class, "DISP_Simple_Table_Model"), // NOI18N
                    NbBundle.getMessage(CustomComponentProducer.class, "TTIP_Simple_Table_Model"), // NOI18N
                    TableModelCD.ICON_PATH, TableModelCD.ICON_LARGE_PATH));
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            JavaClassNameResolver resolver = JavaClassNameResolver.getInstance(document);
            resolver.addResolveListenerIfNotRegistered(this);
            Boolean isValid = resolver.isValid("javax.microedition.lcdui.Form"); // NOI18N
            return isValid != null ? isValid : true;
        }
    }
}
