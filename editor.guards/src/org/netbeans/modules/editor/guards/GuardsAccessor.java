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

package org.netbeans.modules.editor.guards;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;

/**
 *
 * @author Jan Pokorsky
 */
public abstract class GuardsAccessor {
    
    public static GuardsAccessor DEFAULT;
    
    static {
        Class clazz = GuardedSectionManager.class;
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }
    
    public abstract GuardedSectionManager createGuardedSections(GuardedSectionsImpl impl);
    
    public abstract SimpleSection createSimpleSection(SimpleSectionImpl impl);
    
    public abstract InteriorSection createInteriorSection(InteriorSectionImpl impl);
    
    public abstract GuardedSectionImpl getImpl(GuardedSection gs);
    
}
