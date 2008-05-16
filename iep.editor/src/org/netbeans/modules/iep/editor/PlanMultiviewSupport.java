package org.netbeans.modules.iep.editor;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;

import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class PlanMultiviewSupport implements ViewComponentCookie, ShowCookie {

     /** The data object */
    private PlanDataObject dobj;
    
    /**
     * Creates a new instance of WSDLMultiViewSupport.
     *
     * @param  dobj  the data object.
     */
    public PlanMultiviewSupport(PlanDataObject dobj) {
        this.dobj = dobj;
    }
    
    public void view(final View view, final Component component,
            final Object... parameters) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    viewInSwingThread(view, component, parameters);
                }
            });
        } else {
            viewInSwingThread(view, component, parameters);
        }
    }

    // see schemamultiviewsupport for implementation
    private void viewInSwingThread(View view, Component component,
            Object... parameters) {
        if (canView(view,component)) {
            PlanEditorSupport editor = dobj.getPlanEditorSupport();
            editor.open();
            if (view != null) {
                switch (view) {
                    case SOURCE:
                        PlanMultiViewFactory.requestMultiviewActive(
                                PlanSourceMultiviewDesc.PREFERRED_ID);
                        break;
                    case DESIGN:
                        PlanMultiViewFactory.requestMultiviewActive(
                                PlanDesignViewMultiViewDesc.PREFERRED_ID);
                        break;
                }
            }
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            ShowCookie showCookie = activeTC.getLookup().lookup(ShowCookie.class);
            ResultItem resultItem = null;
            if (parameters != null && parameters.length != 0) {
                for (Object o : parameters) {
                    if (o instanceof ResultItem) {
                        resultItem = (ResultItem) o;
                        break;
                    }
                }
            }
            if (showCookie != null && component != null) {
                if (resultItem == null) {
                    resultItem = new ResultItem(null, null, component, null);
                }
                showCookie.show(resultItem);
            }
        }
    }

    // see schemamultiviewsupport for implementation
    public boolean canView(ViewComponentCookie.View view, Component component) {
        if (view != null) {
            switch (view) {
            case SOURCE:
                if (!PlanSourceMultiviewDesc.PREFERRED_ID.equals(
                        getMultiviewActive()) ||
                        !getActiveComponents().contains(component)) {
                    return true;
                }
                break;
            case CURRENT:
            case SUPER:
                return true;
            case DESIGN:
                if (!(component instanceof IEPComponent)) {
                    return false;
                }
                
                if (PlanDesignViewMultiViewDesc.PREFERRED_ID.equals(
                        getMultiviewActive()) &&
                        getActiveComponents().contains(component)) {
                    TopComponent activeTC = TopComponent.getRegistry().getActivated();
                    PlanDataObject wdobj = activeTC.getLookup().lookup(PlanDataObject.class);
                    return wdobj != dobj;
                }
                return true;
//                if (PlanDesignViewMultiViewDesc.PREFERRED_ID.equals(
//                        getMultiviewActive())) {
//                    return false;
//                }
//                // Determine if this type of component is displayed
//                // in the partner view or not.
//                boolean okay = false;
//                for (Class type : DESIGNABLE_COMPONENTS) {
//                    if (type.isInstance(component)) {
//                        return true;
//                    }
//                }
//                break;
            }
        }
        return false;
    }
        
    public void show(ResultItem resultItem) {
        Component component = resultItem.getComponents();
        if (component == null || component.getModel() == null ||
                component.getModel().getState() == IEPModel.State.NOT_WELL_FORMED) {
            view(View.SOURCE,component,resultItem);
        } else {
            if (component instanceof DocumentComponent) {
                UIUtilities.annotateSourceView(dobj, (DocumentComponent) component,
                        resultItem.getDescription(), false);
            }

            TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
            MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);

            if (mvh == null) {
                return;
            }

            MultiViewPerspective mvp = mvh.getSelectedPerspective();
            if (mvp.preferredID().equals(PlanSourceMultiviewDesc.PREFERRED_ID)) {
                view(View.SOURCE, component, resultItem);
            } else if (mvp.preferredID().equals(PlanDesignViewMultiViewDesc.PREFERRED_ID)) {
                view(View.DESIGN, component, resultItem);
            }
        }
    }
    
    /**
     * Finds the preferredID of active multiview element. If activated
     * TopComponent is not MultiViewTopComponent, returns null.
     *
     * @return  identifier of the active multiview element.
     */
    private static String getMultiviewActive() {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(activeTC);
        if (handler != null) {
            return handler.getSelectedPerspective().preferredID();
        }
        return null;
    }

    /**
     * Finds the activated components of active TopComponent.
     *
     * @return  collection of the active components.
     */
    private static Collection<Component> getActiveComponents() {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        Collection<Component> activeComponents = Collections.emptySet();
        for (Node node : activeTC.getActivatedNodes()) {
            Component component = node.getLookup().lookup(Component.class);
            if (component != null) {
                if (activeComponents.isEmpty()) {
                    activeComponents = new HashSet<Component>();
                }
                activeComponents.add(component);
            }
        }
        return activeComponents;
    }

}
