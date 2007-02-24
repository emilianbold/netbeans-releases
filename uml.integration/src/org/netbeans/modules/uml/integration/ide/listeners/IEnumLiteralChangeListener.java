/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.integration.ide.listeners;

import org.netbeans.modules.uml.integration.ide.events.LiteralInfo;

/**
 * Interface for classes that need to be notified of changes to enumeration's literals
 * in the Describe model.
 *
 * @author  Daniel Prusa
 * @version 1.0
 */
public interface IEnumLiteralChangeListener {
    public boolean enumLiteralAdded(LiteralInfo lit, boolean beforeChange);

    public boolean enumLiteralChanged(LiteralInfo oldC, LiteralInfo newC, boolean beforeChange);

    public boolean enumLiteralDeleted(LiteralInfo clazz, boolean beforeChange);
    
    public boolean enumLiteralDuplicated(LiteralInfo oldMember, LiteralInfo newMember, boolean before);
}
