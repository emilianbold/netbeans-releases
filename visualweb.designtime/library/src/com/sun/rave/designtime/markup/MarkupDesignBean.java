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
