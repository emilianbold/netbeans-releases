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

package org.netbeans.modules.cnd.completion.csm;
import java.util.List;

/**
 * completion resolver for the file
 * file should be passed somehow in constructor
 * using of resolver:
 *  resolver = createResolver(file);
 * if reusing => resolver.refresh();
 *  if (resolver.resolve(...)) {
 *   result = resolver.getResult();
 *  }
 *
 * @author vv159170
 */
public interface CompletionResolver {

    /**
     * init resolver before using
     * or reinit
     */
    public boolean refresh();

    /**
     * resolve code completion on specified position
     * items should start with specified prefix
     * or must exactly correspond to input prefix string
     */
    public boolean resolve(int offset, String strPrefix, boolean match);

    /**
     * get result of resolving
     */
    public List/*<CsmObject>*/ getResult();
}
