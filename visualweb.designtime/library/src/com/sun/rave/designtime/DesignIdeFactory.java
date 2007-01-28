/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime;

/**
 * <p><code>DesignIdeFactory</code> provides a simple discovery mechanism to
 * acquire the singleton <code>DesignIde</code> instance to use.</p>
 *
 * @since Mako
 */
public abstract class DesignIdeFactory {
    

    // -------------------------------------------------------- Instance Methods


    /**
     * <p>Return the singleton {@link DesignIde} instance for this IDE.</p>
     */
    public abstract DesignIde getIde();


    // ---------------------------------------------------------- Factory Method


    /**
     * <p>Return the {@link DesignIdeFactory} instance to use for acquiring
     * a reference to the singleton {@link DesignIde} instance for this IDE.</p>
     */
    public static DesignIdeFactory getFactory() {

        return null; // FIXME - flesh out implementation of getFactory()
    
    }


}
