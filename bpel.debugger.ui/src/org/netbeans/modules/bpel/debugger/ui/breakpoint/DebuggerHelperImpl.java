/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.bpel.core.debugger.DebuggerHelper;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
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
    
    public void enableBreakpoints(
            final BpelEntity entity) {
        final List<LineBreakpoint> breakpoints = 
                getBreakpointAnnotationListener().getBreakpoints();
        
        final String entityXPath = ModelUtil.getXpath(entity.getUID());
        
        for (LineBreakpoint breakpoint: breakpoints) {
            if (breakpoint.isEnabled()) {
                continue;
            }
            
            final Object annotation = getBreakpointAnnotationListener().
                    findAnnotation(breakpoint);
            
            final String breakpointXPath = 
                    EditorContextBridge.getXpath(annotation);
            
            if (breakpointXPath.startsWith(entityXPath) ) {
                breakpoint.enable();
            }
        }
    }
    
    public void disableBreakpoints(
            final BpelEntity entity) {
        final List<LineBreakpoint> breakpoints = 
                getBreakpointAnnotationListener().getBreakpoints();
        
        final String entityXPath = ModelUtil.getXpath(entity.getUID());
        
        for (LineBreakpoint breakpoint: breakpoints) {
            if (!breakpoint.isEnabled()) {
                continue;
            }
            
            final Object annotation = getBreakpointAnnotationListener().
                    findAnnotation(breakpoint);
            
            final String breakpointXPath = 
                    EditorContextBridge.getXpath(annotation);
            
            if (breakpointXPath.startsWith(entityXPath) ) {
                breakpoint.disable();
            }
        }
    }
    
    public void deleteBreakpoints(
            final BpelEntity entity) {
        final List<LineBreakpoint> breakpoints = 
                getBreakpointAnnotationListener().getBreakpoints();
        
        final String entityXPath = ModelUtil.getXpath(entity.getUID());
        
        for (LineBreakpoint breakpoint: breakpoints) {
            final Object annotation = getBreakpointAnnotationListener().
                    findAnnotation(breakpoint);
            
            final String breakpointXPath = 
                    EditorContextBridge.getXpath(annotation);
            
            if (breakpointXPath.startsWith(entityXPath) ) {
                DebuggerManager.
                        getDebuggerManager().removeBreakpoint(breakpoint);
            }
        }
    }
    
    private BpelBreakpointListener getBreakpointAnnotationListener () {
        if (myBreakpointAnnotationListener == null) {
            myBreakpointAnnotationListener = DebuggerManager.getDebuggerManager().lookupFirst(null, BpelBreakpointListener.class);
        }
        
        return myBreakpointAnnotationListener;
    }
}
