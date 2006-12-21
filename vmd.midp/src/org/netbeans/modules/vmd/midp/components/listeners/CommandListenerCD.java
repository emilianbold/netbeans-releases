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
package org.netbeans.modules.vmd.midp.components.listeners;

import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
// TODO - file reference property
public final class CommandListenerCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#CommandListener"); // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventListenerCD.TYPEID, CommandListenerCD.TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
                // code
                new EventListenerCode.CodeCommandListenerPresenter (),
                new CodeReferencePresenter () {
                    protected String generateAccessCode () { return "this"; } // NOI18N
                    protected String generateDirectAccessCode () { return "this"; } // NOI18N
                    protected String generateTypeCode () { throw Debug.illegalState (); }
                },
                new EventListenerCode.CodeImplementsPresenter ("javax.microedition.lcdui.CommandListener", "commandAction", "javax.microedition.lcdui.Command", "javax.microedition.lcdui.Displayable"), // NOI18N
                CodeNamePresenter.fixed ("commandAction") // NOI18N
        );
    }

}
