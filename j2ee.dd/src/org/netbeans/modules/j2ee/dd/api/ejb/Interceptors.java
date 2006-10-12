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

package org.netbeans.modules.j2ee.dd.api.ejb;

/**
 *
 * @author Martin Adamek
 */
public interface Interceptors {
    
    int addDescription(String value);
    int addInterceptor(Interceptor value);
    String[] getDescription();
    String getDescription(int index);
    Interceptor[] getInterceptor();
    Interceptor getInterceptor(int index);
    Interceptor newInterceptor();
    int removeDescription(String value);
    int removeInterceptor(Interceptor value);
    void setDescription(int index, String value);
    void setDescription(String[] value);
    void setInterceptor(int index, Interceptor value);
    void setInterceptor(Interceptor[] value);
    int sizeDescription();
    int sizeInterceptor();
    
}
