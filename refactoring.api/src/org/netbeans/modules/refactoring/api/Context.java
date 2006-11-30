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

package org.netbeans.modules.refactoring.api;

import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Context contains "enviroment" in which the refactoring was invoked
 * e.g. Java refactoring might put instance of ClasspathInfo here
 * @author Jan Becicka
 */
public final class Context extends AbstractLookup {

    private InstanceContent instanceContent;

    Context(InstanceContent instanceContent) {
        super(instanceContent);
        this.instanceContent = instanceContent;
    }

    public void add(Object value) {
        Object old = lookup(value.getClass());
        if (old!=null) {
            instanceContent.remove(old);
        }
        instanceContent.add(value);
    }
}
