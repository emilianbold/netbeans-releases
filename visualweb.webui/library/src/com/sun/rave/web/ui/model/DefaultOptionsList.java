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
package com.sun.rave.web.ui.model;

import com.sun.rave.web.ui.util.MessageUtil;
import java.util.ArrayList;
import java.io.Serializable;

/** A default list of options, pre-populated with three default items.
 *
 * @author gjmurphy
 */
public class DefaultOptionsList extends OptionsList {

    public DefaultOptionsList() {
        String bundle = DefaultOptionsList.class.getPackage().getName() + ".Bundle";
        Option[] options = new Option[] {
            new Option("item1", MessageUtil.getMessage(bundle, "item1")), //NOI18N
            new Option("item2", MessageUtil.getMessage(bundle, "item2")), //NOI18N
            new Option("item3", MessageUtil.getMessage(bundle, "item3"))  //NOI18N
        };
        this.setOptions(options);
    }

}
