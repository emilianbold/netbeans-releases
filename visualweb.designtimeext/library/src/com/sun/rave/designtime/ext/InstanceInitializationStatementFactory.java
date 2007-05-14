package com.sun.rave.designtime.ext;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;

/**
 * Implementations of this factory are used to create a statement text used
 * to initialize the instance of a bean. The statement should be syntacticaly
 * correct.  
 */
public interface InstanceInitializationStatementFactory {
    /**
     * This creates and returns a statement text used to initialize the instance
     * of the bean in the source. The name returned by <code>getInstanceName()</code>
     * method should be used in place of the instance name in the initialization
     * statement.
     */
    public String getInitializationStatement(DesignContext designContext, DesignBean designBean);
}
