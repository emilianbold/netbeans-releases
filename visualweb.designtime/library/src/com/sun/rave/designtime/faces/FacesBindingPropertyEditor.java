/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.faces;

import com.sun.rave.designtime.PropertyEditor2;

/**
 * <p>A FacesBindingPropertyEditor is the only property editor type that is *allowed* to edit
 * 'bound' properties - meaning any FacesDesignProperty that is currently 'bound'.</p>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface FacesBindingPropertyEditor extends PropertyEditor2 {
}
