/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.bpel.core.debugger.DebuggerHelper;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author ksorokin
 */
public class DebuggerHelperImpl implements DebuggerHelper {
    private BpelBreakpointListener myBreakpointAnnotationListener;
    
    public void toggleBreakpointEnabledState(Node node, BpelEntity entity) {
        final DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return;
        }
        
        final int lineNumber = EditorUtil.getLineNumber(node);
        if (lineNumber < 1) {
            return;
        }
        
        final String xpath = ModelUtil.getXpath(entity.getUID());
        if (xpath == null) {
            return;
        }
        
        final String url = FileUtil.toFile(
                dataObject.getPrimaryFile()).getPath().replace("\\", "/");
        
        final LineBreakpoint breakpoint = getBreakpointAnnotationListener().
                findBreakpoint(url, xpath, lineNumber);
        
        if (breakpoint != null) {
            if (breakpoint.isEnabled()) {
                breakpoint.disable();
            } else {
                breakpoint.enable();
            }
        }
    }
    
    private BpelBreakpointListener getBreakpointAnnotationListener () {
        if (myBreakpointAnnotationListener == null) {
            myBreakpointAnnotationListener = (BpelBreakpointListener) 
                    DebuggerManager.getDebuggerManager ().lookupFirst 
                    (null, BpelBreakpointListener.class);
        }
        return myBreakpointAnnotationListener;
    }
}
