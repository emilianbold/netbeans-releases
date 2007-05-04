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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.rave.designtime.ext;

import java.lang.reflect.Type;

/**
 * <P>The DesignBeanExt interface is an extension to DesignBean to provide additional
 * design-time functionality for the Designtime System. DesignBeanExt does not extends
 * DesignBean. So it acts as a <code>mixin</code>. </p>
 *
 * <p>The implementation class should also implement DesignBean <br/>
 *     Ex.   public class DesignBeanImpl implements DesignBean, DesignBeanExt{
 * </P>
 *
 * <p>The DsignBeanExt is meant to add support for Java Generics </p>
 * @author Winston Prakash
 */
public interface DesignBeanExt {
    
    /**
     * <p>Returns the Type Parameters of the DesignBean as an array</p>
     * <p>If the DesignBean is of type Map&lt;String, Iteger&gt; <br/>
     *    then getTypeParameters returs array of java.lang.reflect.Type <br/>
     *    (Ex: Class&lt;String&gt; and &lt;Iteger&gt; )
     * </P>
     *
     * @return  array of java.lang.reflect.Type
     */
    Type[] getTypeParameters();
}
