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

package org.netbeans.modules.ruby.spi.project.support.rake;

import java.util.Map;
import javax.swing.event.ChangeListener;

/**
 * Provides a set of Ant property definitions that might be evaluated in
 * some context.
 * <p>
 * This interface defines no independent thread safety, but in typical usage
 * it will be used with the project manager mutex. Changes should be fired
 * synchronously.
 * @author Jesse Glick
 */
public interface PropertyProvider {

    /**
     * Get all defined properties.
     * The values might contain Ant-style property references.
     * @return all properties defined in this block
     */
    Map<String,String> getProperties();
    
    /**
     * Add a change listener.
     * When the set of available properties, or some of the values, change,
     * this listener should be notified.
     * @param l a listener to add
     */
    void addChangeListener(ChangeListener l);
    
    /**
     * Remove a change listener.
     * @param l a listener to remove
     */
    void removeChangeListener(ChangeListener l);
    
}
