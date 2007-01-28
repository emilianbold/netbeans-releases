/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync.live;

import com.sun.rave.designtime.*;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignEvent;

/**
 * Abstract base partial DesignEvent implementation which manages a EventDescriptor and other basic
 * beans and designtime stuff and ties into the rest of the SourceLive* classes.
 *
 * @author Carl Quinn
 */
public abstract class SourceDesignEvent implements DesignEvent {

    public static final DesignEvent[] EMPTY_ARRAY = {};

    protected final EventDescriptor descriptor;
    protected final SourceDesignBean liveBean;

    /**
     * Construct a new SourceDesignEvent for a given descriptor and live bean.
     *
     * @param descriptor The EventDescriptor that defines this event.
     * @param liveBean The SourceDesignBean that owns this event.
     */
    public SourceDesignEvent(EventDescriptor descriptor, SourceDesignBean liveBean) {
        this.descriptor = descriptor;
        this.liveBean = liveBean;
    }

    /*
     * @see com.sun.rave.designtime.DesignEvent#getEventDescriptor()
     */
    public EventDescriptor getEventDescriptor() {
        return descriptor;
    }

    /*
     * @see com.sun.rave.designtime.DesignEvent#getDesignBean()
     */
    public DesignBean getDesignBean() {
        return liveBean;
    }

    /**
     * @return the default event handler method name, same as setHandlerName would use if passed
     *         null
     * @see com.sun.rave.designtime.DesignEvent#getDefaultHandlerName()
     */
    public String getDefaultHandlerName() {
        String handlerName = liveBean.getInstanceName() + "_" +
            getEventDescriptor().getListenerMethodDescriptor().getName();
        return handlerName;
    }

    /**
    * ClipImage for events.
    */
   public static class ClipImage {
       String name;
       Object handler;
       ClipImage(String name, Object handler) { this.name = name; this.handler = handler; }

       public String toString() {
           StringBuffer sb = new StringBuffer();
           toString(sb);
           return sb.toString();
       }

       public void toString(StringBuffer sb) {
           sb.append("[DesignEvent.ClipImage");
           sb.append(" name=" + name);
           sb.append(" value=" + handler);
           sb.append("]");
       }
   }

   /**
    * @return
    */
   public ClipImage getClipImage() {
       return new ClipImage(descriptor.getName(), getHandlerName());
   }
}
