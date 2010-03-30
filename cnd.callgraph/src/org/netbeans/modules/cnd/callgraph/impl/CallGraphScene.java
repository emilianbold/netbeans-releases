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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.callgraph.impl;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.CallModel;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * @author David Kaspar
 */
public class CallGraphScene extends GraphScene<Function,Call> {

    private static final Border BORDER_4 = BorderFactory.createLineBorder (4);
    private static final RequestProcessor RP = new RequestProcessor(CallGraphScene.class.getName(), 1);

    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;
    private Router router;
    private SceneLayout sceneLayout;

    private WidgetAction moveAction = ActionFactory.createMoveAction();
    private WidgetAction hoverAction = createWidgetHoverAction();
    private WidgetAction popupAction = ActionFactory.createPopupMenuAction(new MyPopupMenuProvider());
    private Font defaultItalicFont;
    private Action exportAction;

    
    private CallModel callModel;

    public CallGraphScene() {
        mainLayer = new LayerWidget (this);
        addChild(mainLayer);

        connectionLayer = new LayerWidget (this);
        addChild(connectionLayer);
        router = RouterFactory.createOrthogonalSearchRouter (mainLayer, connectionLayer);
        defaultItalicFont = new Font(getDefaultFont().getName(),
                              Font.ITALIC, getDefaultFont().getSize());
        getActions().addAction(popupAction);
        getActions().addAction(ActionFactory.createWheelPanAction());
    }
    
    public void setLayout(SceneLayout sceneLayout){
        this.sceneLayout = sceneLayout;
    }
    
    public void setModel(CallModel model){
        callModel = model;
    }
    
    public void doLayout(){
        Runnable run = new Runnable() {
            @Override
            public void run() {
                sceneLayout.invokeLayout();
                validate();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    public void hideNode(final Function element) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                removeNodeWithEdges(element);
                doLayout();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    public void clean() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                List<Function> list = new ArrayList<Function>(getNodes());
                for (Function f : list) {
                    removeNodeWithEdges(f);
                }
                doLayout();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    public void addCallToScene(final Call element) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                boolean isDoLayout = false;
                Function toFunction = element.getCallee();
                Widget to = findWidget(toFunction);
                if (to == null) {
                    to = addNode(toFunction);
                    to.setPreferredLocation(new Point(100, 100));
                    isDoLayout = true;
                }
                if (element.getCaller() != null) {
                    Function fromFunction = element.getCaller();
                    Widget from = findWidget(fromFunction);
                    if (from == null) {
                        from = addNode(fromFunction);
                        from.setPreferredLocation(new Point(10, 10));
                        isDoLayout = true;
                    }
                    if (findEdgesBetween(fromFunction, toFunction).isEmpty()) {
                        if (toFunction.equals(fromFunction)) {
                            addLoopEdge(element, toFunction);
                        } else {
                            addEdge(element);
                            setEdgeSource(element, fromFunction);
                            setEdgeTarget(element, toFunction);
                        }
                        isDoLayout = true;
                    }
                }
                if (isDoLayout) {
                    doLayout();
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    public void addFunctionToScene(final Function element) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                Widget to = findWidget(element);
                if (to == null) {
                    to = addNode(element);
                    to.setPreferredLocation(new Point(100, 100));
                    doLayout();
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    private void addLoopEdge(Call edge, Function targetNode) {
        ConnectionWidget connection = (ConnectionWidget)addEdge(edge);
        Widget w = findWidget(targetNode);
        connection.setRouter(router);
        MyVMDNodeAnchor anchor = new MyVMDNodeAnchor(w);
        setEdgeSource(edge, targetNode);
        connection.setSourceAnchor(anchor);
        setEdgeTarget(edge, targetNode);
        connection.setTargetAnchor(anchor);
    }


    @Override
    protected Widget attachNodeWidget(Function node) {
        String name = node.getName();
        String scope = node.getScopeName();
        Widget label;
        if (scope != null && scope.length() > 0){
            label = new MyMemberLabelWidget(this, scope, name);
        } else {
            label = new MyLabelWidget(this, name);
        }
        if (node.isVurtual()) {
            label.setFont(defaultItalicFont);
        }
        label.setToolTipText(node.getDescription());
        label.setBorder(BORDER_4);
        label.getActions().addAction(moveAction);
        label.getActions().addAction(hoverAction);
        label.getActions().addAction(ActionFactory.createEditAction(new NodeEditProvider(node)));
        label.getActions().addAction(popupAction);
        mainLayer.addChild(label);
        return label;
    }

    @Override
    protected Widget attachEdgeWidget(Call edge) {
        ConnectionWidget connection = new ConnectionWidget(this);
        connection.setToolTipText(edge.getDescription());
        connection.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        connection.getActions().addAction(hoverAction);
        connection.getActions().addAction(ActionFactory.createEditAction(new EdgeEditProvider(edge)));
        connectionLayer.addChild(connection);
        return connection;
    }

    @Override
    protected void attachEdgeSourceAnchor(Call edge, Function oldSourceNode, Function sourceNode) {
        Widget w = sourceNode != null ? findWidget(sourceNode) : null;
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(w));
    }

    @Override
    protected void attachEdgeTargetAnchor(Call edge, Function oldTargetNode, Function targetNode) {
        Widget w = targetNode != null ? findWidget(targetNode) : null;
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(w));
    }

    public void setExportAction(Action exportAction) {
        this.exportAction = exportAction;
    }

    private static class NodeEditProvider implements EditProvider {
        private Function node;
        private NodeEditProvider(Function node){
            this.node = node;
        }
        
        @Override
        public void edit(Widget widget) {
            node.open();
        }
    }

    private static class EdgeEditProvider implements EditProvider {
        private Call call;
        private EdgeEditProvider(Call call){
            this.call = call;
        }
        
        @Override
        public void edit(Widget widget) {
            call.open();
        }
    }

    private static final class MyLabelWidget extends LabelWidget {
        public MyLabelWidget (Scene scene, String label) {
            super(scene);
            setFont(scene.getFont().deriveFont(Font.BOLD));
            setLabel(label);
        }

        @Override
        protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
            if (previousState.isHovered() == state.isHovered()) {
                return;
            }
            setForeground(getScene().getLookFeel().getLineColor(state));
            repaint();
        }
    }

    private static final class MyMemberLabelWidget extends Widget  {
        private String scope;
        private String label;

        public MyMemberLabelWidget (Scene scene, String scope, String label) {
            super (scene);
            this.scope = scope;
            this.label = label;
            setOpaque(false);
            revalidate();
            setCheckClipping(true);
        }

        @Override
        protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
            if (previousState.isHovered() == state.isHovered()) {
                return;
            }
            setForeground(getScene().getLookFeel().getLineColor(state));
            repaint();
        }

        @Override
        protected Rectangle calculateClientArea () {
            Graphics2D gr = getGraphics();
            Rectangle2D stringBounds1 = gr.getFontMetrics(getFont()).getStringBounds(scope, gr);
            Rectangle2D stringBounds2 = gr.getFontMetrics(getFont().deriveFont(Font.BOLD)).getStringBounds(label, gr);
            Rectangle2D stringBounds = new Rectangle2D.Double(stringBounds1.getX(), stringBounds1.getY(),
                    Math.max(stringBounds1.getWidth(), stringBounds2.getWidth()), stringBounds1.getHeight()+stringBounds2.getHeight());
            return roundRectangle(stringBounds);
        }

        private static Rectangle roundRectangle (Rectangle2D rectangle) {
            int x1 = (int) Math.floor(rectangle.getX());
            int y1 = (int) Math.floor(rectangle.getY());
            int x2 = (int) Math.ceil(rectangle.getMaxX());
            int y2 = (int) Math.ceil(rectangle.getMaxY());
            return new Rectangle (x1, y1, x2 - x1, y2 - y1);
        }
    
        @Override
        protected void paintWidget () {
            if (label == null) {
                return;
            }
            Graphics2D gr = getGraphics();
            gr.setFont(getFont());

            FontMetrics fontMetrics = gr.getFontMetrics();
            Rectangle clientArea = getClientArea();

            int x = clientArea.x;
            int y = 0;
            AffineTransform previousTransform = gr.getTransform ();
            gr.translate (x, y);
            gr.setColor(getForeground());
            gr.drawString (scope, 0, 0);
            gr.setFont(getFont().deriveFont(Font.BOLD));
            gr.drawString (label, 0, fontMetrics.getHeight());
            gr.setTransform(previousTransform);
        }
    }

    private static class MyVMDNodeAnchor extends Anchor {

        private boolean requiresRecalculation = true;
        private HashMap<Entry, Result> results = new HashMap<Entry, Result>();
        private final boolean vertical;

        public MyVMDNodeAnchor(Widget widget) {
            super(widget);
            this.vertical = true;
        }

        /**
         * Notifies when an entry is registered
         * @param entry the registered entry
         */
        @Override
        protected void notifyEntryAdded(Entry entry) {
            requiresRecalculation = true;
        }

        /**
         * Notifies when an entry is unregistered
         * @param entry the unregistered entry
         */
        @Override
        protected void notifyEntryRemoved(Entry entry) {
            results.remove(entry);
            requiresRecalculation = true;
        }

        /**
         * Notifies when the anchor is going to be revalidated.
         * @since 2.8
         */
        @Override
        protected void notifyRevalidate() {
            requiresRecalculation = true;
        }

        private void recalculate() {
            if (!requiresRecalculation) {
                return;
            }

            Widget widget = getRelatedWidget();
            Point relatedLocation = getRelatedSceneLocation();

            Rectangle bounds = widget.convertLocalToScene(widget.getBounds());

            HashMap<Entry, Float> topmap = new HashMap<Entry, Float>();
            HashMap<Entry, Float> bottommap = new HashMap<Entry, Float>();

            for (Entry entry : getEntries()) {
                Point oppositeLocation = getOppositeSceneLocation(entry);
                if (oppositeLocation == null || relatedLocation == null) {
                    results.put(entry, new Result(new Point(bounds.x, bounds.y), DIRECTION_ANY));
                    continue;
                }

                int dy = oppositeLocation.y - relatedLocation.y;
                int dx = oppositeLocation.x - relatedLocation.x;

                if (vertical) {
                    if (dy > 0) {
                        bottommap.put(entry, (float) dx / (float) dy);
                    } else if (dy < 0) {
                        topmap.put(entry, (float) -dx / (float) dy);
                    } else {
                        topmap.put(entry, dx < 0 ? Float.MAX_VALUE : Float.MIN_VALUE);
                    }
                } else {
                    if (dx > 0) {
                        bottommap.put(entry, (float) dy / (float) dx);
                    } else if (dy < 0) {
                        topmap.put(entry, (float) -dy / (float) dx);
                    } else {
                        topmap.put(entry, dy < 0 ? Float.MAX_VALUE : Float.MIN_VALUE);
                    }
                }
            }

            Entry[] topList = toArray(topmap);
            Entry[] bottomList = toArray(bottommap);

            int pinGap = 0;
            int y = bounds.y - pinGap;
            int x = bounds.x - pinGap;
            int len = topList.length;

            for (int a = 0; a < len; a++) {
                Entry entry = topList[a];
                if (vertical) {
                    x = bounds.x + (a + 1) * bounds.width / (len + 1);
                } else {
                    y = bounds.y + (a + 1) * bounds.height / (len + 1);
                }
                results.put(entry, new Result(new Point(x, y), vertical ? Direction.TOP : Direction.LEFT));
            }

            y = bounds.y + bounds.height + pinGap;
            x = bounds.x + bounds.width + pinGap;
            len = bottomList.length;

            for (int a = 0; a < len; a++) {
                Entry entry = bottomList[a];
                if (vertical) {
                    x = bounds.x + (a + 1) * bounds.width / (len + 1);
                } else {
                    y = bounds.y + (a + 1) * bounds.height / (len + 1);
                }
                results.put(entry, new Result(new Point(x, y), vertical ? Direction.BOTTOM : Direction.RIGHT));
            }

            requiresRecalculation = false;
        }

        private Entry[] toArray(final HashMap<Entry, Float> map) {
            Set<Entry> keys = map.keySet();
            Entry[] entries = keys.toArray(new Entry[keys.size()]);
            Arrays.sort(entries, new Comparator<Entry>() {

                @Override
                public int compare(Entry o1, Entry o2) {
                    float f = map.get(o1) - map.get(o2);
                    if (f > 0.0f) {
                        return 1;
                    } else if (f < 0.0f) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            return entries;
        }

        /**
         * Computes a result (position and direction) for a specific entry.
         * @param entry the entry
         * @return the calculated result
         */
        @Override
        public Result compute(Entry entry) {
            recalculate();
            return results.get(entry);
        }
    }

    private class MyPopupMenuProvider implements PopupMenuProvider {
        @Override
        public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
            JPopupMenu menu = null;
            Object node = findObject(widget);
            if (node instanceof Function){
                final Function f = (Function) node;
                menu = new JPopupMenu();
                menu.add(new GoToReferenceAction(f,0).getPopupPresenter());
                menu.add(new ExpandCallees(f).getPopupPresenter());
                menu.add(new ExpandCallers(f).getPopupPresenter());
                menu.add(new JSeparator());
                menu.add(new RemoveNode(f).getPopupPresenter());
            } else if (widget instanceof CallGraphScene) {
                menu = new JPopupMenu();
                menu.add(((Presenter.Popup)exportAction).getPopupPresenter());
            }
            return menu;
        }
    }

    private class ExpandCallees extends AbstractAction implements Presenter.Popup {
        private JMenuItem menuItem;
        private Function function;
        public ExpandCallees(Function function) {
            this.function = function;
            putValue(Action.NAME, NbBundle.getMessage(CallGraphScene.class, "ExpandCallees"));  // NOI18N
            putValue(Action.SMALL_ICON, new javax.swing.ImageIcon(
                    CallGraphScene.class.getResource("/org/netbeans/modules/cnd/callgraph/resources/who_is_called.png"))); // NOI18N
            menuItem = new JMenuItem(this); 
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    for(Call call : callModel.getCallees(function)){
                        addCallToScene(call);
                    }
                }
            });
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }

    private class ExpandCallers extends AbstractAction implements Presenter.Popup {
        private JMenuItem menuItem;
        private Function function;
        public ExpandCallers(Function function) {
            this.function = function;
            putValue(Action.NAME, NbBundle.getMessage(CallGraphScene.class, "ExpandCallers"));  // NOI18N
            putValue(Action.SMALL_ICON, new javax.swing.ImageIcon(
                    CallGraphScene.class.getResource("/org/netbeans/modules/cnd/callgraph/resources/who_calls.png"))); // NOI18N
            menuItem = new JMenuItem(this); 
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    for(Call call : callModel.getCallers(function)){
                        addCallToScene(call);
                    }
                }
            });
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }

    private class RemoveNode extends AbstractAction implements Presenter.Popup {
        private JMenuItem menuItem;
        private Function function;
        public RemoveNode(Function function) {
            this.function = function;
            putValue(Action.NAME, NbBundle.getMessage(CallGraphScene.class, "RemoveNode"));  // NOI18N
            menuItem = new JMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    hideNode(function);
                }
            });
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }
}
