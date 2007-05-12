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

package com.sun.rave.designtime.faces;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.markup.MarkupDesignBean;

/**
 * The FacesDesignBean is a DesignBean for a JSF Bean.  This extension to the DesignBean interface
 * (and MarkupDesignBean interface) includes the ability to retrieve a child facet by name.  If a
 * particular DesignBean represents a JSF-specific Bean, it will be an 'instanceof' FacesDesignBean.
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignBean
 * @see FacesDesignContext
 */
public interface FacesDesignBean extends MarkupDesignBean {

    /**
     * Returns the DesignBean component that is currently connected to the specified facet of this
     * FacesDesignBean component.
     *
     * @param facet The desired facet (eg. "header", "footer", etc)
     * @return A DesignBean representing the component for this facet
     */
    public DesignBean getFacet(String facet);
}
