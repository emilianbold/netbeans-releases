/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.xam;

/**
 *
 * @author rico
 * Interface representing a component that has a name property
 */

public interface Named<T extends Component> extends Component<T>{
    public static final String NAME_PROPERTY = "name";
    
    String getName();
    void setName(String name);
}
