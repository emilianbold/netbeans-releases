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
