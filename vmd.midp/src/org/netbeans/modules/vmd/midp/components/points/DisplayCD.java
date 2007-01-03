/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, InspectorFolderPresenter.class);
        DocumentSupport.removePresentersOfClass (presenters, InspectorPositionPresenter.class);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // code
            createSwitchDisplayCodePresenter (),
            CodeNamePresenter.fixed ("switchDisplayable", "getDisplay", "__previousDisplayables", "switchToPreviousDisplayable"),
            // delete
            DeletePresenter.createIndeliblePresenter ()
        );
    }

    private CodeClassLevelPresenter.Adapter createSwitchDisplayCodePresenter () {
        return new CodeClassLevelPresenter.Adapter() {
            protected void generateFieldSectionCode (MultiGuardedSection section) {
                if (isPreviousScreenEventHandlerUsed ())
                    section.getWriter ().write ("private java.util.Hashtable __previousDisplayables = new java.util.Hashtable ();\n");
            }

            protected void generateMethodSectionCode (MultiGuardedSection section) {
                if (isPreviousScreenEventHandlerUsed ()) {
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

            protected void generateClassBodyCode (StyledDocument document) {
                MultiGuardedSection section = MultiGuardedSection.create (document, getComponent ().getComponentID () + "-switchDisplayable"); // NOI18N

                section.getWriter ().write ("public void switchDisplayable (Alert alert, Displayable nextDisplayable) {\n").commit (); // NOI18N

                section.switchToEditable (getComponent ().getComponentID () + "-preSwitch"); // NOI18N
                section.getWriter ().write (" // write pre-switch user code here\n").commit (); // NOI18N

                section.switchToGuarded ();
                section.getWriter ().write ("Display display = getDisplay ();\n"); // NOI18N
                if (isPreviousScreenEventHandlerUsed ()) {
                    section.getWriter ().write ("Displayable __currentDisplayable = display.getCurrent ();\n"); // NOI18N
                    section.getWriter ().write ("if (__currentDisplayable != null  &&  nextDisplayable != null) {\n"); // NOI18N
                    section.getWriter ().write ("__previousDisplayables.put (__currentDisplayable, nextDisplayable);\n"); // NOI18N
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
                section.getWriter ().write ("}\n").commit (); // NOI18N

                section.close ();
            }

            private boolean isPreviousScreenEventHandlerUsed () {
                return ! DocumentSupport.gatherAllComponentsOfTypeID (getComponent ().getDocument (), PreviousScreenEventHandlerCD.TYPEID).isEmpty ();
            }

        };
    }

}
