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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam;

import java.util.Collection;
import java.util.List;

/**
 * A component in model.
 * 
 */
public interface Component<C extends Component> {
    
    /**
     * @return parent component.
     */
    C getParent();
    
    /**
     * @return the unmodifiable list of child components.
     */
    List<C> getChildren();

    /**
     * @param type Interested children type to return.
     * @return unmodifiable list of directly contained component of specified type.
     */
    <T extends C> List<T> getChildren(Class<T> type);
    
    /**
     * @param types Interested children type to return.
     * @return unmodifiable list of directly contained component of specified types.
     */
    List<C> getChildren(Collection<Class<? extends C>> types);
    
    /**
     * @return the model where this element is being used or null if not
     * currently part of a model. 
     */
    Model getModel();

    /**
     * Returns a copy of this component for adding into the given parent component.
     */
    Component copy(C parent);
    
    /**
     * Returns true if given component can be added as this component child.
     */
    boolean canPaste(Component child);

}
