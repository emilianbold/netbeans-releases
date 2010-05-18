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

public final class ItemStateListenerCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#ItemStateListener"); // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventListenerCD.TYPEID, ItemStateListenerCD.TYPEID, true, false);
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
                new CodeReferencePresenter () {
                    protected String generateAccessCode () { return "this"; } // NOI18N
                    protected String generateDirectAccessCode () { return "this"; } // NOI18N
                    protected String generateTypeCode () { throw Debug.illegalState (); }
                },
//                new ItemStateListenerCD.CodeCommandActionPresenter ()
                new EventListenerCode.CodeImplementsPresenter ("javax.microedition.lcdui.ItemStateListener", "itemStateChanged", "javax.microedition.lcdui.Item"), // NOI18N
                CodeNamePresenter.fixed ("itemStateChanged") // NOI18N
        );
    }
/*
    public static class CodeCommandActionPresenter extends CodeClassLevelPresenter {

        protected void generateFieldSectionCode (CodeWriter writer) {
        }

        protected void generateMethodSectionCode (CodeWriter writer) {
        }

        protected void generateInitializeSectionCode (CodeWriter writer) {
        }

        protected void generateClassBodyCode (StyledDocument document) {
            List<DesignComponent> sources = DocumentSupport.gatherAllComponentsOfTypeID (getComponent ().getDocument (), CommandEventSourceCD.TYPEID);
            if (sources.size () == 0)
                return;

            MultiGuardedSection section = MultiGuardedSection.create (document, getComponent ().getComponentID () + "-commandAction"); // NOI18N
            section.getWriter ().write ("public void commandAction (Command command, Displayable displayable) {\n").commit (); // NOI18N

            section.switchToEditable (getComponent ().getComponentID () + "-preCommandAction"); // NOI18N
            section.getWriter ().write (" // write pre-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            resolveFirstLevel (section, sources);

            section.switchToEditable (getComponent ().getComponentID () + "-postCommandAction"); // NOI18N
            section.getWriter ().write (" // write post-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            section.getWriter ().write ("}\n").commit (); // NOI18N
        }

        private void resolveFirstLevel (MultiGuardedSection section, List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> displayables2sources = gatherDisplayables (sources);
            ArrayList<String> displayables = new ArrayList<String> (displayables2sources.keySet ());
            Collections.sort (displayables);

            for (int i = 0; i < displayables.size (); i ++) {
                String displayable = displayables.get (i);
                if (i > 0)
                    section.getWriter ().write ("else "); // NOI18N
                section.getWriter ().write ("if (displayable == " + displayable + ") {\n"); // NOI18N

                resolveSecondLevel (section, displayables2sources.get (displayable));

                assert section.isGuarded ();
                if (i < displayables.size () - 1)
                    section.getWriter ().write ("} "); // NOI18N
                else
                    section.getWriter ().write ("}\n"); // NOI18N
            }

            section.getWriter ().commit ();
        }

        private void resolveSecondLevel (MultiGuardedSection section, List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> commands2sources = gatherCommands (sources);
            ArrayList<String> commands = new ArrayList<String> (commands2sources.keySet ());
            Collections.sort (commands);

            for (int i = 0; i < commands.size (); i ++) {
                String command = commands.get (i);
                if (i > 0)
                    section.getWriter ().write ("else "); // NOI18N
                section.getWriter ().write ("if (command == " + command + ") {\n"); // NOI18N

                for (DesignComponent source : commands2sources.get (command))
                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, source);

                assert section.isGuarded ();
                if (i < commands.size () - 1)
                    section.getWriter ().write ("} "); // NOI18N
                else
                    section.getWriter ().write ("}\n"); // NOI18N
            }
        }

        private HashMap<String, ArrayList<DesignComponent>> gatherDisplayables (List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> d2s = new HashMap<String, ArrayList<DesignComponent>> ();
            for (DesignComponent source : sources) {
                String displayable = CodeReferencePresenter.generateDirectAccessCode (source.readProperty (CommandEventSourceCD.PROP_DISPLAYABLE).getComponent ());
                ArrayList<DesignComponent> s = d2s.get (displayable);
                if (s == null) {
                    s = new ArrayList<DesignComponent> ();
                    d2s.put (displayable, s);
                }
                s.add (source);
            }
            return d2s;
        }

        private HashMap<String, ArrayList<DesignComponent>> gatherCommands (List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> c2s = new HashMap<String, ArrayList<DesignComponent>> ();
            for (DesignComponent source : sources) {
                String command = CodeReferencePresenter.generateDirectAccessCode (source.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ());
                ArrayList<DesignComponent> s = c2s.get (command);
                if (s == null) {
                    s = new ArrayList<DesignComponent> ();
                    c2s.put (command, s);
                }
                s.add (source);
            }
            return c2s;
        }

    }
*/
}
