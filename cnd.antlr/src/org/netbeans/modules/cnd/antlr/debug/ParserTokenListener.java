package org.netbeans.modules.cnd.antlr.debug;

public interface ParserTokenListener extends ListenerBase {


	public void parserConsume(ParserTokenEvent e);
	public void parserLA(ParserTokenEvent e);
}
