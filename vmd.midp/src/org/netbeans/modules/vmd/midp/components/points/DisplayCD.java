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
package org.netbeans.modules.vmd.midp.components.points;

import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.handlers.PreviousScreenEventHandlerCD;

import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Karol Harezlak
 */
public final class DisplayCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#Display"); // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (PointCD.TYPEID, TYPEID, true, true);
    }
    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, InspectorFolderPresenter.class);
        DocumentSupport.removePresentersOfClass (presenters, InspectorPositionPresenter.class);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // code
            createSwitchDisplayCodePresenter (),
            CodeNamePresenter.fixed ("switchDisplayable", "getDisplay", "__previousDisplayables", "switchToPreviousDisplayable"), // NOI18N
            // delete
            DeletePresenter.createIndeliblePresenter ()
        );
    }

    private CodeClassLevelPresenter.Adapter createSwitchDisplayCodePresenter () {
        return new CodeClassLevelPresenter.Adapter() {
            @Override
            protected void generateFieldSectionCode (MultiGuardedSection section) {
                if (isPreviousScreenEventHandlerUsed ())
                    section.getWriter ().write ("private java.util.Hashtable __previousDisplayables = new java.util.Hashtable ();\n"); // NOI18N
            }

            @Override
            protected void generateMethodSectionCode (MultiGuardedSection section) {
                if (isPreviousScreenEventHandlerUsed ()) {
                    section.getWriter ().write ("/**\n * Switches a display to previous displayable of the current displayable.\n * The <code>display</code> instance is obtain from the <code>getDisplay</code> method.\n */\n"); // NOI18N
                    section.getWriter ().write ("private void switchToPreviousDisplayable () {\n"); // NOI18N
                    section.getWriter ().write ("Displayable __currentDisplayable = getDisplay ().getCurrent ();\n"); // NOI18N
                    section.getWriter ().write ("if (__currentDisplayable != null) {\n"); // NOI18N
                    section.getWriter ().write ("Displayable __nextDisplayable = (Displayable) __previousDisplayables.get (__currentDisplayable);\n"); // NOI18N
                    section.getWriter ().write ("if (__nextDisplayable != null) {\n"); // NOI18N
                    section.getWriter ().write ("switchDisplayable (null, __nextDisplayable);\n"); // NOI18N
                    section.getWriter ().write ("}\n"); // NOI18N
                    section.getWriter ().write ("}\n"); // NOI18N
                    section.getWriter ().write ("}\n"); // NOI18N
                }
            }

            @Override
            protected void generateClassBodyCode (StyledDocument document) {
                MultiGuardedSection section = MultiGuardedSection.create (document, getComponent ().getComponentID () + "-switchDisplayable"); // NOI18N

                section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: switchDisplayable \">\n"); // NOI18N
                section.getWriter ().write ("/**\n * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.\n * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately\n * @param nextDisplayable the Displayable to be set\n */\n"); // NOI18N
                section.getWriter ().write ("public void switchDisplayable (Alert alert, Displayable nextDisplayable) {\n").commit (); // NOI18N

                section.switchToEditable (getComponent ().getComponentID () + "-preSwitch"); // NOI18N
                section.getWriter ().write (" // write pre-switch user code here\n").commit (); // NOI18N

                section.switchToGuarded ();
                section.getWriter ().write ("Display display = getDisplay ();\n"); // NOI18N
                if (isPreviousScreenEventHandlerUsed ()) {
                    section.getWriter ().write ("Displayable __currentDisplayable = display.getCurrent ();\n"); // NOI18N
                    section.getWriter ().write ("if (__currentDisplayable != null  &&  nextDisplayable != null) {\n"); // NOI18N
                    section.getWriter ().write ("__previousDisplayables.put (nextDisplayable, __currentDisplayable);\n"); // NOI18N
                    section.getWriter ().write ("}\n"); // NOI18N
                }
                section.getWriter ().write ("if (alert == null) {\n"); // NOI18N
                section.getWriter ().write ("display.setCurrent (nextDisplayable);\n"); // NOI18N
                section.getWriter ().write ("} else {\n"); // NOI18N
                section.getWriter ().write ("display.setCurrent (alert, nextDisplayable);\n"); // NOI18N
                section.getWriter ().write ("}\n").commit (); // NOI18N

                section.switchToEditable (getComponent ().getComponentID () + "-postSwitch"); // NOI18N
                section.getWriter ().write (" // write post-switch user code here\n").commit (); // NOI18N

                section.switchToGuarded ();
                section.getWriter ().write ("}\n"); // NOI18N
                section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N

                section.close ();
            }

            private boolean isPreviousScreenEventHandlerUsed () {
                return ! DocumentSupport.gatherAllComponentsOfTypeID (getComponent ().getDocument (), PreviousScreenEventHandlerCD.TYPEID).isEmpty ();
            }

        };
    }

}
