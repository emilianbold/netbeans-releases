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

package org.netbeans.spi.editor.mimelookup;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Lookup;

/**
 * Provides a <code>Lookup</code> for the specific <code>MimePath</code>.
 *
 * <p>The implementations of this interface should be registered among the services
 * in the default lookup, for details look at
 * <a href="http://openide.netbeans.org/lookup/index.html"> META-INF/services registration</a>.
 *
 *  @author Miloslav Metelka, Vita Stejskal
 */
public interface MimeDataProvider {

    /**
     * Retrieves a <code>Lookup</code> for the given <code>MimePath</code>.
     * 
     * <p>The <code>Lookup</code> returned by this method should hold a reference
     * to the <code>MimePath</code> it was created for.
     *
     * <p>The implementors should consider caching of the <code>Lookup</code> instances
     * returned by this method for performance reasons. The <code>MimePath</code>
     * object can be used as a stable key for such a cache, because it implements
     * its <code>equals</code> and <code>hashCode</code> method in the suitable way.
     *
     * @param mimePath The mime path to get the <code>Lookup</code> for. The mime
     * path passed in can't be <code>null</code>, but it can be the 
     * {@link MimePath#EMPTY} mime path.
     *
     * @return The <code>Lookup</code> for the given <code>MimePath</code> or
     * <code>null</code> if there is no lookup available for this mime path.
     */
    public Lookup getLookup(MimePath mimePath);

}
