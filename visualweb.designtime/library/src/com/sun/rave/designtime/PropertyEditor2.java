/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime;

import java.beans.PropertyEditor;

/**
 * <p>This interface extends the standard PropertyEditor interface and adds a hook to give the
 * implementor access to the design-time context of the property being edited via the DesignProperty
 * interface.  This is useful if a PropertyEditor author wishes to display a list of instances
 * within scope, or wishes to drill-in to the object that this property is being set on.</p>
 *
 * <p>NOTE: It is important to only use the passed-in DesignProperty for context purposes.  This
 * DesignProperty should not be directly manipulated, or the IDE could get into a recursive loop.</p>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface PropertyEditor2 extends PropertyEditor {

    /**
     * <p>When the PropetyEditor is being invoked, the matching DesignProperty will be passed in for
     * context.  This can be used to dig into the DesignBean being edited and its surrounding context.
     * </p>
     *
     * <p>NOTE: It is important to only use the passed-in DesignProperty for context purposes.  This
     * DesignProperty should not be directly manipulated, or the IDE could get into a recursive loop.
     * </p>
     *
     * @param prop The DesignProperty currently being edited by this PropertyEditor2 - this may be
     *        used for context purposes only, and should not be used to manipulate the property.
     */
    public void setDesignProperty(DesignProperty prop);
}
