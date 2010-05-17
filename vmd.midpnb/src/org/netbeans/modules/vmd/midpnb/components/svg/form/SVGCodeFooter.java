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
package org.netbeans.modules.vmd.midpnb.components.svg.form;

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.codegen.CodeClassInitHeaderFooterPresenter;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.components.handlers.ExitMidletEventHandlerCD;


class SVGCodeFooter extends CodeClassInitHeaderFooterPresenter {
    
    SVGCodeFooter( TypeID typeId ){
        myTypeId = typeId;
    }

    @Override
    public void generateClassInitializationHeader(MultiGuardedSection section) {
    }

    @Override
    public void generateClassInitializationFooter(MultiGuardedSection section) {
        DesignComponent svgForm = getComponent().getParentComponent();
        DesignComponent eventHandler = null;
        for (DesignComponent component : svgForm.getComponents()) {
            if (!component.getType().equals(myTypeId)) {
                continue;
            }
            if (component.readProperty(SVGComponentEventSourceCD.PROP_SVGCOMPONENT).
                    getComponent() != getComponent()) {
                continue;
            }
            eventHandler = component.readProperty(EventSourceCD.PROP_EVENT_HANDLER).getComponent();
            if (eventHandler != null) {
                break;
            }
        }
        if (eventHandler == null) {
            return;
        }
        section.getWriter().write(CodeReferencePresenter.generateDirectAccessCode(getComponent()) + ".addActionListener(new SVGActionListener() {\n"); //NOI18N
        section.getWriter().write("public void actionPerformed(SVGComponent svgComponent) {\n");// NOI18N                
        if (eventHandler.getType() != ExitMidletEventHandlerCD.TYPEID) {
            section.getWriter().commit();
            section.switchToEditable(getComponent().getComponentID() + "beforeSwitch"); //NOI18N
            section.getWriter().write("//Some action before switch\n"); // NOI18N
            section.getWriter().commit();
            section.switchToGuarded();
            CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode(section, eventHandler);
            section.getWriter().commit();
            section.switchToEditable(getComponent().getComponentID() + "afterSwitch"); //NOI18N
            section.getWriter().write("//Some action after switch\n"); // NOI18N
            section.getWriter().commit();
            section.switchToGuarded();
        } else {
            section.getWriter().commit();
            section.switchToEditable(getComponent().getComponentID() + "beforeSwitch"); //NOI18N
            section.getWriter().write("//Some action before exit Midlet\n"); // NOI18N
            section.getWriter().commit();
            section.switchToGuarded();
            CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode(section, eventHandler);
        }
        section.getWriter().write("}\n"); // NOI18N
        section.getWriter().write("});\n"); //NOI18N
    }
    
    private TypeID myTypeId;
}
