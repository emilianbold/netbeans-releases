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
    private final boolean restartable;
    private final boolean hupOnClose;

    public Config(
		String command,
		Provider containerProvider,
		Provider ioProvider,
		AllowClose allowClose,
		DispatchThread dispatchThread,
		Execution execution,
		IOShuttling ioShuttling,
		boolean restartable,
		boolean hupOnClose
	    ) {
	this.command  = command;
	this.containerProvider  = containerProvider;
	this.ioProvider  = ioProvider;
	this.allowClose  = allowClose;
	this.dispatchThread  = dispatchThread;
	this.execution  = execution;
	this.ioShuttling  = ioShuttling;
	this.restartable  = restartable;
	this.hupOnClose  = hupOnClose;
    }

    public static Config getShellConfig() {
	return new Config("/bin/bash",
	                  null,
	                  null,
	                  AllowClose.ALWAYS,
	                  null,
	                  null,
	                  IOShuttling.INTERNAL,
	                  false,	// restartable
	                  true);	// hupOnClose
    }

    public static Config getCmdConfig(String command) {
	return new Config(command,
	                  null,
	                  null,
	                  AllowClose.ALWAYS,
	                  null,
	                  null,
	                  IOShuttling.INTERNAL,
	                  true,		// restartable
	                  true);	// hupOnClose
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

    public IOShuttling getIOShuttling() {
	return ioShuttling;
    }
}
