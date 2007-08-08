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
