/*
 * HiddenFramesNodeActionsProvider.java
 *
 * Created on 16. prosinec 2004, 13:21
 */

package org.netbeans.modules.j2ee.debug.callstackviewfilterring.actions;

import javax.swing.Action;
import org.netbeans.modules.j2ee.debug.callstackviewfilterring.CallStackFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author lk155162
 */
public class HiddenFramesNodeActionsProvider implements NodeActionsProvider {
    
    /** Creates a new instance of HiddenFramesNodeActionsProvider */
    public HiddenFramesNodeActionsProvider() {
    }

    public void performDefaultAction(Object node) throws UnknownTypeException {
    }

    public Action[] getActions(Object node) throws UnknownTypeException {
        if (!(node instanceof CallStackFilter.HiddenFrames))
            throw new UnknownTypeException (node);
        
        return new Action[0];
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    public void addTreeModelListener(TreeModelListener l) {
    }
     
}
