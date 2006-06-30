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

package org.netbeans.spi.debugger;

import java.util.List;


/**
 * Abstract ancestor of classes providing lookup.
 *
 * @author Jan Jancura
 */
public interface ContextProvider {

    /**
     * Returns list of services of given type from given folder.
     *
     * @param folder a folder name or null
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public abstract List lookup (String folder, Class service);

    /**
     * Returns one service of given type from given folder.
     *
     * @param folder a folder name or null
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public abstract Object lookupFirst (String folder, Class service);
}

