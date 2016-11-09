package org.netbeans.modules.cnd.debugger.common2.ui.processlist;


import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfoDescriptor;
import org.netbeans.modules.cnd.debugger.common2.utils.ProcessListSupport;
import org.openide.nodes.AbstractNode;

public interface ProcessPanelCustomizer extends Comparator<AbstractNode> {

    String getDisplayName(ProcessInfo info);

    List<ProcessInfoDescriptor> getValues(ProcessInfo info);

    List<ProcessInfoDescriptor> getHeaders(ProcessListSupport.Provider provider);

    String getOutlineHeaderName();
    
}
