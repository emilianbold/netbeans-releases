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
package com.sun.rave.web.ui.util;

import java.io.*;
import java.util.*;

/**
 *  <p>	An object that can convert a value to a different type.</p>
 *
 *  @author	Todd Fast, todd.fast@sun.com
 *  @author	Mike Frisino, michael.frisino@sun.com
 */
public interface TypeConversion
{
    /**
     *	<p> Converts the provided value to the type represented by the
     *	    implementor if this interface.</p>
     */
    public Object convertValue(Object value);
}
