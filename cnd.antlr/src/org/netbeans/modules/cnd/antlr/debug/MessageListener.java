package org.netbeans.modules.cnd.antlr.debug;

public interface MessageListener extends ListenerBase {


	public void reportError(MessageEvent e);
	public void reportWarning(MessageEvent e);
}
