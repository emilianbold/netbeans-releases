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
/*
 * DateTimeConverterDesignInfo.java
 *
 * Created on September 30, 2005, 3:02 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.visualweb.faces.dt.converter;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDesignInfo;
import javax.faces.convert.DateTimeConverter;

/**
 *
 * @author cao
 */
public class DateTimeConverterDesignInfo extends BasicDesignInfo {

    /** Creates a new instance of DateTimeConverterDesignInfo */
    public DateTimeConverterDesignInfo() {
        super( DateTimeConverter.class );
    }

    public Result beanCreatedSetup( DesignBean designBean ) {

        // We want to set the time zone to null so that the user's time zone will be used
        designBean.getProperty( "timeZone").setValue( null );
        return Result.SUCCESS;
    }
}
