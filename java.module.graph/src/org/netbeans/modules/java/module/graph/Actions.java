/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.visual.animator.AnimatorEvent;
import org.netbeans.api.visual.animator.AnimatorListener;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GraphLayoutFactory;
import org.netbeans.api.visual.graph.layout.GraphLayoutSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class Actions {
    private Actions() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    @NonNull
    static Action zoomToFit(@NonNull final DependencyGraphScene scene) {
        return new SceneZoomToFitAction(scene);
    }

    @NonNull
    static Action hierarchicalGraphLayout(@NonNull final DependencyGraphScene scene) {
        return new HierarchicalGraphLayoutAction(scene);
    }

    @NonNull
    static Action treeGraphVerticalLayout(@NonNull final DependencyGraphScene scene) {
        return new TreeGraphLayoutVerticalAction(scene);
    }

    @NonNull
    static Action treeGraphHorizontalLayout(@NonNull final DependencyGraphScene scene) {
        return new TreeGraphLayoutHorizontalAction(scene);
    }

    @NonNull
    static Action fruchtermanReingoldLayout(@NonNull final DependencyGraphScene scene) {
        return new FruchtermanReingoldLayoutAction(scene);
    }

    private static class SceneZoomToFitAction extends AbstractAction {
        private final DependencyGraphScene scene;

        @NbBundle.Messages("ACT_ZoomToFit=Zoom To Fit")
        SceneZoomToFitAction(@NonNull final DependencyGraphScene scene) {
            Parameters.notNull("scene", scene); //NOI18N
            this.scene = scene;
            putValue(NAME, Bundle.ACT_ZoomToFit());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            scene.getFitToViewLayout().fitToView(null);
        }
    };

    private static abstract class AbstractLayoutAction extends AbstractAction {
        final DependencyGraphScene scene;

        AbstractLayoutAction(@NonNull final DependencyGraphScene scene) {
            Parameters.notNull("scene", scene); //NOI18N
            this.scene = scene;
        }

        void fitToZoomAfterLayout() {
            scene.getSceneAnimator().getPreferredLocationAnimator().addAnimatorListener(new AnimatorListener() {
                @Override
                public void animatorStarted(AnimatorEvent event) {
                }
                @Override
                public void animatorReset(AnimatorEvent event) {
                }
                @Override
                public void animatorFinished(AnimatorEvent event) {
                    scene.getSceneAnimator().getPreferredLocationAnimator().removeAnimatorListener(this);
                    zoomToFit(scene).actionPerformed(null);
                }
                @Override
                public void animatorPreTick(AnimatorEvent event) {
                }
                @Override
                public void animatorPostTick(AnimatorEvent event) {
                }
            });
        }
    }

    private static class HierarchicalGraphLayoutAction  extends  AbstractLayoutAction {

        @NbBundle.Messages("LBL_Layout_HierarchicalGraphLayout=Hierarchical")
        HierarchicalGraphLayoutAction(@NonNull final DependencyGraphScene scene) {
            super(scene);
            putValue(NAME, Bundle.LBL_Layout_HierarchicalGraphLayout());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final GraphLayout layout = GraphLayoutFactory.createHierarchicalGraphLayout(
                   scene,
                   scene.isAnimated(),
                   false);
            layout.layoutGraph(scene);
            fitToZoomAfterLayout();
        }
   };

    private static class TreeGraphLayoutVerticalAction extends AbstractLayoutAction {
       @NbBundle.Messages("LBL_Layout_TreeGraphLayoutVertical=Vertical Tree")
       TreeGraphLayoutVerticalAction(@NonNull final DependencyGraphScene scene) {
           super(scene);
           putValue(NAME, Bundle.LBL_Layout_TreeGraphLayoutVertical());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final GraphLayout layout = GraphLayoutFactory.createTreeGraphLayout(10, 10, 50, 50, true);
            GraphLayoutSupport.setTreeGraphLayoutRootNode(layout, scene.getRootNode());
            layout.layoutGraph(scene);
            fitToZoomAfterLayout();
        }
    };


    private static class TreeGraphLayoutHorizontalAction extends AbstractLayoutAction {
        @NbBundle.Messages("LBL_Layout_TreeGraphLayoutHorizontal=Horizontal Tree")
        TreeGraphLayoutHorizontalAction(@NonNull final DependencyGraphScene scene) {
            super(scene);
            putValue(NAME, Bundle.LBL_Layout_TreeGraphLayoutHorizontal());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final GraphLayout layout = GraphLayoutFactory.createTreeGraphLayout(10, 10, 50, 50, false);
            GraphLayoutSupport.setTreeGraphLayoutRootNode(layout, scene.getRootNode());
            layout.layoutGraph(scene);
            fitToZoomAfterLayout();
        }
    };


    private static class FruchtermanReingoldLayoutAction extends AbstractLayoutAction {
       @NbBundle.Messages("LBL_Layout_FruchtermanReingoldLayout=Default Layout")
       FruchtermanReingoldLayoutAction(
               @NonNull final DependencyGraphScene scene) {
           super(scene);
           putValue(NAME, Bundle.LBL_Layout_FruchtermanReingoldLayout());
       }
       @Override public void actionPerformed(ActionEvent e) {
           scene.getDefaultLayout().invokeLayout();
           fitToZoomAfterLayout();
       }
   };
}
