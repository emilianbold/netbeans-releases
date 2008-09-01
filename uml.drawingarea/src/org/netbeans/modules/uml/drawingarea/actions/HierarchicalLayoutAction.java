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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.netbeans.api.visual.animator.AnimatorEvent;
import org.netbeans.api.visual.animator.AnimatorListener;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GraphLayoutFactory;
import org.netbeans.api.visual.graph.layout.GraphLayoutListener;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.netbeans.modules.uml.drawingarea.support.ContainerAgnosticLayout;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Kris Richards
 */
public class HierarchicalLayoutAction extends AbstractAction implements GraphLayoutListener {

    private DesignerScene scene;
    private final int MAX_NODES_TO_ANIMATE = 50;
    private boolean animated = false;

    public HierarchicalLayoutAction(DesignerScene scene) {
        this.scene = scene;
        putValue(Action.SMALL_ICON, ImageUtil.instance().getIcon("hierarchical-layout.png")); // NOI18N
        putValue(Action.SHORT_DESCRIPTION,
        NbBundle.getMessage(HierarchicalLayoutAction.class, "LBL_HierarchicalLayoutAction")); // NOI18N
        
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl shift K"));
    }

    public void actionPerformed(ActionEvent e) {

        ContextPaletteManager man = scene.getLookup().lookup(ContextPaletteManager.class);
        if (man != null) {
            man.cancelPalette();
        }

        if ( ! (scene.getNodes().size() > 0 && askOkToLayoutDiagram())) 
        {
            return;
        }

        animated = scene.getNodes().size() < MAX_NODES_TO_ANIMATE ? true : false;
        GraphLayout gLayout = GraphLayoutFactory.createHierarchicalGraphLayout(scene, animated, true, 25, 35);

        gLayout.addGraphLayoutListener(this);

        scene.getSceneAnimator().getPreferredLocationAnimator().addAnimatorListener(new AnimatorListener() {

            public void animatorStarted(AnimatorEvent event) {}

            public void animatorReset(AnimatorEvent event) {}

            public void animatorFinished(AnimatorEvent event) {
                movePalette() ;
                scene.getSceneAnimator().getPreferredLocationAnimator().removeAnimatorListener(this);
                //set diagram dirty
                if ((scene != null) && (scene.getTopComponent() != null) && (scene.getTopComponent() instanceof UMLDiagramTopComponent))
                {
                    ((UMLDiagramTopComponent)scene.getTopComponent()).setDiagramDirty(true);
                }
            }

            public void animatorPreTick(AnimatorEvent event) {}

            public void animatorPostTick(AnimatorEvent event) {}

        });


        new ContainerAgnosticLayout(scene, gLayout);
        gLayout.layoutGraph(scene);

    }

    private void movePalette () {
        ContextPaletteManager man = scene.getLookup().lookup(ContextPaletteManager.class);
        if (man != null) {
            man.selectionChanged(null);
        }
    }
    
    public void graphLayoutStarted(UniversalGraph graph) {
    }

    public void graphLayoutFinished(UniversalGraph graph) {

        if (animated) {
            return;
        }
        movePalette();
    }

    public void nodeLocationChanged(UniversalGraph graph, Object node, Point previousPreferredLocation, Point newPreferredLocation) {
        //do nothing
    }
    
    

    @Override
    public boolean isEnabled()
    {
        return scene.isReadOnly() == false;
    }


    private boolean askOkToLayoutDiagram()
    {
        int resultKind = SimpleQuestionDialogResultKind.SQDRK_RESULT_NO;
        Preferences prefs = NbPreferences.forModule(DummyCorePreference.class);

        if (prefs.getBoolean("UML_Ask_Before_Layout", true))
        {
            IQuestionDialog dialog = new SwingQuestionDialogImpl();
            
            String title = NbBundle.getMessage(HierarchicalLayoutAction.class, "TITLE_LAYOUTQUESTION");
            String message = NbBundle.getMessage(HierarchicalLayoutAction.class, "LBL_CHANGELAYOUT");
            String checkbox = NbBundle.getMessage(HierarchicalLayoutAction.class, "LBL_DONTASKAGAIN");

            QuestionResponse result = 
                dialog.displaySimpleQuestionDialogWithCheckbox(
                    MessageDialogKindEnum.SQDK_YESNO,
                    MessageIconKindEnum.EDIK_ICONWARNING, 
                    message, checkbox, title,
                    MessageResultKindEnum.SQDRK_RESULT_YES, false);

            if (result.isChecked() == true)
            {
                prefs.putBoolean("UML_Ask_Before_Layout", false);
            }
            resultKind = result.getResult();
        } 
        else
        {
            resultKind = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
        }
        return resultKind == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
    }

}
