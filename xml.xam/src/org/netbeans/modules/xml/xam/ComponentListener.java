/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.xam;

import java.util.EventListener;

/**
 * A component listener provides a course grained event stream based on 
 * values or children of the source. This is not intended to replace
 * property change events and only serves as a way to differentiate between
 * children and non children related events. 
 * @author Rico Cruz
 * @author Nam Nguyen
 * @author Chris Webster
 */
public interface ComponentListener extends EventListener {
    /**
     * invoked if a value other than children is changed.
     */
    void valueChanged(ComponentEvent evt);
    /**
     * invoked if a child has been added.
     */
    void childrenAdded(ComponentEvent evt);
    /**
     * invoked if a child has been removed. 
     */
    void childrenDeleted(ComponentEvent evt);
}
