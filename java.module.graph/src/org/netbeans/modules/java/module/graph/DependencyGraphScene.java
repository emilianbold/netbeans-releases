/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.ListWidget;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class DependencyGraphScene extends GraphScene<ModuleNode, DependencyEdge> {

    private final GraphTopComponent owner;
    private final LayerWidget mainLayer;
    private final LayerWidget connectionLayer;
    private JScrollPane pane;
    private FitToViewLayout fitViewLayout;
    private FruchtermanReingoldLayout layout;
    private ModuleNode root;
    private boolean animated;

    //Actions
    private final Action zoomToFitAction = Actions.zoomToFit(this);
    private final WidgetAction popupMenuAction = ActionFactory.createPopupMenuAction(new PopupMenuProviderImpl());

    DependencyGraphScene(
        @NonNull final GraphTopComponent owner) {
        Parameters.notNull("owner", owner); //NOI18N
        this.owner = owner;
        mainLayer = new LayerWidget(this);
        addChild(mainLayer);
        connectionLayer = new LayerWidget(this);
        addChild(connectionLayer);
        addChild(new GlassPaneWidget(this));
        getActions().addAction(popupMenuAction);
    }

    @Override
    protected Widget attachNodeWidget(ModuleNode node) {
        if (root == null) {
            root = node;
        }
        final ListWidget res = new ListWidget(this);
        res.setLabel(node.getName());
        mainLayer.addChild(res);
        return res;
    }

    @Override
    protected Widget attachEdgeWidget(DependencyEdge edge) {
        final EdgeWidget con = new EdgeWidget(this, edge);
        connectionLayer.addChild(con);
        return con;
    }

    @Override
    protected void attachEdgeSourceAnchor(DependencyEdge edge, ModuleNode oldSource, ModuleNode source) {
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(findWidget(source)));
    }

    @Override
    protected void attachEdgeTargetAnchor(DependencyEdge edge, ModuleNode oldTarget, ModuleNode target) {
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(findWidget(target)));
    }

    void setSurroundingScrollPane(final JScrollPane pane) {
        this.pane=pane;
    }

    @NonNull
    SceneLayout getDefaultLayout() {
        if (layout == null) {
            layout =  new FruchtermanReingoldLayout(this, pane);
        }
        return layout;
    }

    void initialLayout() {
        //start using default layout
        getDefaultLayout().invokeLayout();
        addSceneListener(new SceneListener() {
            @Override
            public void sceneRepaint() {
            }
            @Override
            public void sceneValidating() {
            }
            @Override
            public void sceneValidated() {
                if (!animated) {
                    zoomToFitAction.actionPerformed(null);
                    animated = true;
                }
            }
        });
    }

    @NonNull
    GraphTopComponent getOwner() {
        return owner;
    }

    @CheckForNull
    ModuleNode getRootNode() {
        return root;
    }

    boolean isAnimated () {
        return animated;
    }

    void setMyZoomFactor(double zoom) {
        setZoomFactor(zoom);
        ArrayList<Widget> arr = new ArrayList<Widget>();
        arr.addAll(mainLayer.getChildren());
        arr.addAll(connectionLayer.getChildren());
        arr.stream()
                .filter((wid) -> (wid instanceof Zoomable))
                .forEach((wid) -> ((Zoomable)wid).updateReadableZoom());
    }

    @NonNull
    FitToViewLayout getFitToViewLayout () {
        if (fitViewLayout == null) {
            fitViewLayout = new FitToViewLayout(this);
        }
        return fitViewLayout;
    }

    private final class  GlassPaneWidget extends Widget {

        GlassPaneWidget(@NonNull final Scene scene) {
            super(scene);
        }

        @Override
        protected void paintChildren() {
            if (isCheckClipping()) {
                Rectangle clipBounds = DependencyGraphScene.this.getGraphics().getClipBounds();
                for (Widget child : mainLayer.getChildren()) {
                    Point location = child.getLocation();
                    Rectangle bounds = child.getBounds();
                    bounds.translate(location.x, location.y);
                    if (clipBounds == null || bounds.intersects(clipBounds)) {
                        child.paint();
                    }
                }
            } else {
                for (Widget child : mainLayer.getChildren()) {
                    child.paint();
                }
            }
        }
    }

    private final class PopupMenuProviderImpl implements PopupMenuProvider {

        @NbBundle.Messages({"LBL_LayoutSubMenu=Layout"})
        @Override
        public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
            JPopupMenu popupMenu = new JPopupMenu();
            if (widget == DependencyGraphScene.this) {
                popupMenu.add(zoomToFitAction);
                final JMenu layoutMenu = new JMenu(Bundle.LBL_LayoutSubMenu());
                layoutMenu.add(Actions.fruchtermanReingoldLayout(DependencyGraphScene.this));
                layoutMenu.add(new JSeparator());
                layoutMenu.add(Actions.hierarchicalGraphLayout(DependencyGraphScene.this));
                layoutMenu.add(Actions.treeGraphVerticalLayout(DependencyGraphScene.this));
                layoutMenu.add(Actions.treeGraphHorizontalLayout(DependencyGraphScene.this));
                popupMenu.add(layoutMenu);
            } else {
                //Todo:
            }
            return popupMenu;
        }
    }
}
