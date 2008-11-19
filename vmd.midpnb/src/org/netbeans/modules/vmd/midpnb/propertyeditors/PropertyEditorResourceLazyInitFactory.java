/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.propertyeditors;

import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.PropertyEditorResourceLazyInit;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement;
import org.netbeans.modules.vmd.midpnb.components.displayables.WaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGWaitScreenCD;
import org.netbeans.modules.vmd.midpnb.propertyeditors.table.TableModelEditorElement;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class PropertyEditorResourceLazyInitFactory {

    public static final DesignPropertyEditor createTaskPropertyEditor() {
        return new PropertyEditorResourceLazyInit( SimpleCancellableTaskCD.TYPEID,
                NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_NEW"), // NOI18N
                NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_NONE"), // NOI18N
                NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_UCLABEL"), // NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {
                return new TaskEditorElement();
            }
        };
    }

    public static final DesignPropertyEditor createSVGTaskPropertyEditor() {
        return new PropertyEditorResourceLazyInit( SimpleCancellableTaskCD.TYPEID,
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_CANCELLABLETASK_NEW"), // NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_CANCELLABLETASK_NONE"), // NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_CANCELLABLETASK_UCLABEL"), //NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {
                return new TaskEditorElement();
            }
        };
    }

    public static final DesignPropertyEditor createTableModelPropertyEditor() {
        return new PropertyEditorResourceLazyInit(SimpleTableModelCD.TYPEID,
                NbBundle.getMessage(TableItemCD.class, "LBL_TABLEMODEL_NEW"), //NOI18N
                NbBundle.getMessage(TableItemCD.class, "LBL_TABLEMODEL_NONE"), //NOI18N
                NbBundle.getMessage(TableItemCD.class, "DISP_TableItem_TableModel_UCLABEL"), //NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {
                return new TableModelEditorElement();
            }
        };
    }

    public static final DesignPropertyEditor createSVGFormPropertyEditor() {
        return new PropertyEditorResourceLazyInit( SVGImageCD.TYPEID,
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NEW"), //NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NONE"), //NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_UCLABEL"), //NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {
                return new SVGFormEditorElement();
            }
        };
    }

    public static final DesignPropertyEditor createSVGImageEditorPropertyEditor() {
        return new PropertyEditorResourceLazyInit(SVGImageCD.TYPEID,
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NEW"), //NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NONE"), //NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_UCLABEL"), //NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {
                return new SVGImageEditorElement();
            }
        };
    }

}
