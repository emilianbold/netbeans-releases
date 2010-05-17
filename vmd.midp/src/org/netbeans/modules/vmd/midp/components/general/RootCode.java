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
package org.netbeans.modules.vmd.midp.components.general;

import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.openide.util.Utilities;
import org.openide.util.TopologicalSortException;

import javax.swing.text.StyledDocument;
import java.util.*;

/**
 * @author David Kaspar
 */
public final class RootCode {

    private RootCode () {
    }

    public static Presenter createInitializePresenter () {
        return new CodeInitializeMethodPresenter ();
    }

    public static abstract class CodeComponentDependencyPresenter extends Presenter {

        protected abstract void collectRequiredComponents (Collection<DesignComponent> requiredComponents);

    }

    public static void collectRequiredComponents (DesignComponent component, Collection<DesignComponent> requiredComponents) {
        for (CodeComponentDependencyPresenter presenter : component.getPresenters (CodeComponentDependencyPresenter.class))
            presenter.collectRequiredComponents (requiredComponents);
    }

    private static class CodeInitializeMethodPresenter extends CodeClassLevelPresenter.Adapter {

        @Override
        protected void generateClassBodyCode (StyledDocument document) {
            MultiGuardedSection section = MultiGuardedSection.create (document, getComponent ().getComponentID () + "-initialize"); // NOI18N
            section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: initialize \">\n"); // NOI18N
            section.getWriter ().write ("/**\n * Initilizes the application.\n * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.\n */\n"); // NOI18N
            section.getWriter ().write ("private void initialize () {\n").commit (); // NOI18N
            section.switchToEditable (getComponent ().getComponentID () + "-preInitialize"); // NOI18N
            section.getWriter ().write (" // write pre-initialize user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            List<DesignComponent> components = performTopologicalSort (getComponent ().getDocument ());
            for (DesignComponent component : components)
                for (CodeClassLevelPresenter presenter : component.getPresenters (CodeClassLevelPresenter.class))
                    presenter.generateInitializeSectionCode (section);
            section.getWriter ().commit ();

            section.switchToEditable (getComponent ().getComponentID () + "-postInitialize"); // NOI18N
            section.getWriter ().write (" // write post-initialize user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            section.getWriter ().write ("}\n"); // NOI18N
            section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N
            section.close ();
        }
    }

    public static List<DesignComponent> performTopologicalSort (DesignDocument document) {
        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> ();
        HashMap<DesignComponent, HashSet<DesignComponent>> map = new HashMap<DesignComponent, HashSet<DesignComponent>> ();

        for (DesignComponent component : DocumentSupport.gatherAllComponentsOfTypeID (document, ClassCD.TYPEID)) {
            if (MidpTypes.getBoolean (component.readProperty (ClassCD.PROP_LAZY_INIT)))
                continue;

            list.add (component);

            ArrayList<DesignComponent> requiredComponents = new ArrayList<DesignComponent> ();
            collectRequiredComponents (component, requiredComponents);
            for (DesignComponent requiredComponent : requiredComponents) {
                HashSet<DesignComponent> edges = map.get (requiredComponent);
                if (edges == null) {
                    edges = new HashSet<DesignComponent> ();
                    map.put (requiredComponent, edges);
                }
                edges.add (component);
            }
        }


        try {
            return Utilities.topologicalSort (list, map);
        } catch (TopologicalSortException e) {
            Debug.warning ("Topological sort failed", "UnsortableSets", e.unsortableSets ()); // NOI18N
            return (List<DesignComponent>) e.partialSort ();
        }

//        IOUtils.runInAWTBlocking (new Runnable() {
//            public void run () {
//                // TODO - implement notification about changed components
//            }
//        });
    }

}
