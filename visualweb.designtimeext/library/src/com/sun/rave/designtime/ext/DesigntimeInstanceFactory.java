package com.sun.rave.designtime.ext;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;

/**
 * Implementations of this factory are used to create instances of Design Beans
 * at design time. The created object's type should be compatible with the type
 * of the bean represented by the DesignBean.  
 */
public interface DesigntimeInstanceFactory {
    /**
     * This creates and returns instances of bean that is wrapped by the Design Beans.  
     */
    public Object createInstance(DesignContext designContext, DesignBean designBean);
}