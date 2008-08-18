package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.datatransfer.DataFlavor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;

public class FeatureMoveAction extends WidgetAction.Adapter
{
    public static final DataFlavor FLAVOR = createDataFlavor();

    private IFeature feature = null;

    private static DataFlavor createDataFlavor()
    {
//        try 
//        {
//            return new DataFlavor("model/Move_Feature_Action;class=org.netbeans.modules.uml.drawingarea.dataobject.PaletteItem", // NOI18N
//                                  "Modeling Palette Item", // XXX missing I18N!
//                                  FeatureMoveAction.class.getClassLoader());
            return new DataFlavor(FeatureMoved.class,
                                  "Feature Moved Action");
//        } 
//        catch (ClassNotFoundException e)
//        {
//            throw new AssertionError(e);
//        }
        
    }
    
    @Override
    public State mouseDragged(Widget widget, WidgetMouseEvent event)
    {
        State retVal = State.REJECTED;
        
//        if (feature != null)
//        {
//            if (widget.getScene() instanceof ObjectScene)
//            {
//                ObjectScene scene = (ObjectScene) widget.getScene();
//                Object model = scene.findObject(widget);
//                if (model instanceof IPresentationElement)
//                {
//                    IPresentationElement element = (IPresentationElement) model;
//                    if (element.getFirstSubject() instanceof IFeature)
//                    {
//                        feature = (IFeature) element.getFirstSubject();
//                    }
//                }
//            }
//            
//            JComponent c = widget.getScene().getView();
//            if (SwingUtilities2.canAccessSystemClipboard()) 
//            {
//                ExTransferable.Single t = new ExTransferable.Single(FLAVOR)
//                {
//                    public Object getData () 
//                    {
//                        return new FeatureMoved();
//                    }
//                };
//
//		Clipboard clipboard = c.getToolkit().getSystemClipboard();
//                clipboard.setContents(t, null);
//                retVal = State.CONSUMED;
//	    }
//        }

        return retVal;
    }

    @Override
    public State mousePressed(Widget widget, WidgetMouseEvent event)
    {
//        return State.createLocked(widget, this);
        return widget.getState().isSelected() == true ? State.CHAIN_ONLY : State.REJECTED;
    }

    @Override
    public State mouseReleased(Widget widget, WidgetMouseEvent event)
    {
//        System.out.println("Mouse Released");
        return State.REJECTED;
    }
    
    public class FeatureMoved
    {
        
    }
}
