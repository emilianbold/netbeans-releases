/*
 * ActivatedNodesControlAdapter.java
 *
 * Created on October 19, 2006, 2:03 PM
 *
 */

package org.netbeans.modules.xml.schema.refactoring.query.views;

import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

/**
 *
 * @author Ajit Bhate
 */
public class ActivatedNodesControlAdapter extends ControlAdapter {
    
    /** Creates a new instance of ActivatedNodesControlAdapter */
    public ActivatedNodesControlAdapter() {
    }
    
    public void itemReleased(VisualItem item, MouseEvent e) {
        super.itemReleased(item, e);
        setActivatedNodes(e, item);
    }

    private void setActivatedNodes(final MouseEvent e, final VisualItem item) {
        if (item.canGet(AnalysisConstants.OPENIDE_NODE, Node.class)) {
            Node node = (Node) item.get(AnalysisConstants.OPENIDE_NODE);
            if(node!=null) {
                Component c = e.getComponent();
                TopComponent tc = (TopComponent) SwingUtilities.
                        getAncestorOfClass(TopComponent.class,c);
                if (tc!=null) {
                    tc.setActivatedNodes(new Node[]{node});
                }
            }
        }
    }
    
}
