package org.netbeans.modules.cnd.antlr.debug;

import java.util.EventListener;

public interface ListenerBase extends EventListener {


	public void doneParsing(TraceEvent e);
	public void refresh();
}
