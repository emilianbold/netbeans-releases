/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
