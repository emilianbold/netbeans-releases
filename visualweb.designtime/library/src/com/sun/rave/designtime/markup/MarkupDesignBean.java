/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.markup;

import org.w3c.dom.Element;
import com.sun.rave.designtime.DesignBean;

/**
 * <P>A MarkupDesignBean represents an instance of a markup (JSF/JSP/etc) JavaBean class at design-
 * time.  There is one MarkupDesignBean instance 'wrapping' each instance of a component class in a
 * bean design tool.  All access to properties and events should be done via the MarkupDesignBean
 * interface at design-time, so that the tool is able to track changes and persist them.</p>
 *
 * <P>MarkupDesignBean extends the DesignBean interface, adding access to the DOM Element that
 * represents this portion of source markup</P>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface MarkupDesignBean extends DesignBean {

    /**
     * Returns the DOM Element representing the source markup for this component.
     *
     * @return Element
     */
    public Element getElement();
}
