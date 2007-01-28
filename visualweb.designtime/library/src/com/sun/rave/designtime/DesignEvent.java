/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime;

/**
 * <p>A DesignEvent represents a single event listener method (and possibly handler) on a single
 * instance of a DesignBean at design-time.</p>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DesignEvent {

    /**
     * Returns the EventDescriptor that defines the meta-data for this DesignEvent
     *
     * @return The EventDescriptor that defines teh meta-data for this DesignEvent
     */
    public EventDescriptor getEventDescriptor();

    /**
     * Returns the DesignBean that owns this DesignEvent
     *
     * @return the DesignBean that owns this DesignEvent
     */
    public DesignBean getDesignBean();

    /**
     * Returns the default event handler method name.  For example on a Button component's 'click'
     * event, the default handler name might be "button1_click".
     *
     * @return the default event handler method name, same as setHandlerName would use if passed null
     */
    public String getDefaultHandlerName();

    /**
     * Sets the method name for this DesignEvent.  If the event is not currently 'hooked', this will
     * 'hook' it and add the required wiring to direct the event handler code to this method name.
     * If 'null' is passed as the handlerName, then the default event handler method name will be
     * used.
     *
     * @param handlerMethodName The desired event handler method name - may be null to use default
     *        event handler method name
     * @return <code>true</code> if the event was successfully 'hooked', and the specified name was unique
     */
    public boolean setHandlerName(String handlerMethodName);

    /**
     * Returns the current event method handler name, or null if the event is currently not 'hooked'
     *
     * @return the current event method handler name, or null if the event is currently not 'hooked'
     */
    public String getHandlerName();

    /**
     * Returns <code>true</code> if this DesignEvent is currently 'hooked', or <code>false</code>
     * if it is not.
     *
     * @return <code>true</code> if this DesignEvent is currently 'hooked', or <code>false</code>
     *         if it is not
     */
    public boolean isHandled();

    /**
     * Removes and unwires an event handler method from this DesignEvent, if one exists.  Returns
     * <code>true</code> if successful, <code>false</code> if not.
     *
     * @return <code>true</code> if successful, <code>false</code> if not
     */
    public boolean removeHandler();

    /**
     * Sets the Java source for the method body of the handler method.  This is expected to be valid
     * Java source to be injected into the body of this event handler method.  If it is not, an
     * IllegalArgumentException is thrown.
     *
     * @param methodBody The Java source for the method body of this event handler method
     * @throws IllegalArgumentException thrown if the Java source is invalid
     */
    public void setHandlerMethodSource(String methodBody) throws IllegalArgumentException;

    /**
     * Returns the Java source code from the body of the handler method
     *
     * @return the Java source code from the body of the handler method
     */
    public String getHandlerMethodSource();
}
