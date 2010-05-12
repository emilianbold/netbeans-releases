/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.terminal.example;

/**
 *
 * @author ivan
 */
public final class Config {
    public static enum Provider {
	DEFAULT,
	TERM,
    }

    public static enum DispatchThread {
	EDT,
	RP,
    }

    public static enum Execution {
	RICH,
	NATIVE,
    }

    public static enum IOShuttling {
	INTERNAL,
	EXTERNAL,
    }

    public static enum ContainerStyle {
	TABBED,
	MUXED,
    }

    public enum AllowClose {
	 /**
	  * Tab is unclosable.
	  * This will control IOVisibility.setClosable()
	  */
	NEVER,

	/**
	 * Tab is closable. a vetoableChange() will always be called on
	 * IOVisibility.VISIBILITY.
	 */
	ALWAYS,
	/**
	 * Tab is closable. a vetoableChange() will always be called on
	 * IOVisibility.VISIBILITY and it's supposed to allow closing
	 * w/o confirmation if IOConnect.isConnected() is false.
	 */
	DISCONNECTED
    }

    private final String command;
    private final Provider containerProvider;
    private final Provider ioProvider;
    private final AllowClose allowClose;
    private final DispatchThread dispatchThread;
    private final Execution execution;
    private final IOShuttling ioShuttling;
    private final ContainerStyle containerStyle;
    private final boolean restartable;
    private final boolean hupOnClose;
    private final boolean keep;

    public Config(
		String command,
		Provider containerProvider,
		Provider ioProvider,
		AllowClose allowClose,
		DispatchThread dispatchThread,
		Execution execution,
		IOShuttling ioShuttling,
		ContainerStyle containerStyle,
		boolean restartable,
		boolean hupOnClose,
		boolean keep
	    ) {
	this.command  = command;
	this.containerProvider  = containerProvider;
	this.ioProvider  = ioProvider;
	this.allowClose  = allowClose;
	this.dispatchThread  = dispatchThread;
	this.execution  = execution;
	this.ioShuttling  = ioShuttling;
	this.containerStyle = containerStyle;
	this.restartable  = restartable;
	this.hupOnClose  = hupOnClose;
	this.keep = keep;
    }

    public static Config getShellConfig() {
	return new Config("/bin/bash",
	                  null,
	                  null,
	                  AllowClose.ALWAYS,
	                  null,
	                  null,
	                  IOShuttling.INTERNAL,
			  ContainerStyle.TABBED,
	                  false,	// restartable
	                  true,		// hupOnClose
			  false		// keep
			  );
    }

    public static Config getCmdConfig(String command) {
	return new Config(command,
	                  null,
	                  null,
	                  AllowClose.ALWAYS,
	                  null,
	                  null,
	                  IOShuttling.INTERNAL,
			  ContainerStyle.TABBED,
	                  true,		// restartable
	                  true,		// hupOnClose
			  false		// keep
			  );
    }

    public String getCommand() {
	return command;
    }

    public Provider getContainerProvider() {
	return containerProvider;
    }

    public Provider getIOProvider() {
	return ioProvider;
    }

    public AllowClose getAllowClose() {
	return allowClose;
    }

    public DispatchThread getThread() {
	return dispatchThread;
    }

    public Execution getExecution() {
	return execution;
    }

    public boolean isRestartable() {
	return restartable;
    }

    public boolean isHUPOnClose() {
	return hupOnClose;
    }

    public boolean isKeep() {
	return keep;
    }

    public IOShuttling getIOShuttling() {
	return ioShuttling;
    }

    public ContainerStyle getContainerStyle() {
	return containerStyle;
    }
}
