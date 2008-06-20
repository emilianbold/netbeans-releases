package org.netbeans.modules.uml.drawingarea.view;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import org.netbeans.api.visual.widget.Widget;

public class MoveDropTargetDropEvent extends DropTargetDropEvent
{

    private MoveWidgetTransferable widgetTransferable = null;

    public MoveDropTargetDropEvent(Widget dropWidget, Point pt)
    {
        super((new DropTarget()).getDropTargetContext(), pt, 0, 0);
        widgetTransferable = new MoveWidgetTransferable(dropWidget);
    }

    @Override
    public Transferable getTransferable()
    {
        return new Transferable()
        {

            public DataFlavor[] getTransferDataFlavors()
            {
                return new DataFlavor[]{MoveWidgetTransferable.FLAVOR};
            }

            public boolean isDataFlavorSupported(DataFlavor flavor)
            {
                return MoveWidgetTransferable.FLAVOR.equals(flavor);
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
            {
                if (isDataFlavorSupported(flavor) == true)
                {
                    return widgetTransferable;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };
    }
}
