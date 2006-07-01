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

package org.netbeans.modules.javahelp;

import java.beans.*;

/**
 * This class provides information about getter/setter methods within
 * BrowserDisplayer. It is usefull for reflection.
 * @see BrowserDisplayer
 *
 * @author Marek Slama
 *
 */
public class BrowserDisplayerBeanInfo extends SimpleBeanInfo {

    public BrowserDisplayerBeanInfo () {
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
	PropertyDescriptor back[] = new PropertyDescriptor[7];
	try {
	    back[0] = new PropertyDescriptor("content", BrowserDisplayer.class);
	    back[1] = new PropertyDescriptor("text", BrowserDisplayer.class);
	    back[2] = new PropertyDescriptor("textFontFamily", BrowserDisplayer.class);
	    back[3] = new PropertyDescriptor("textFontSize", BrowserDisplayer.class);
	    back[4] = new PropertyDescriptor("textFontWeight", BrowserDisplayer.class);
	    back[5] = new PropertyDescriptor("textFontStyle", BrowserDisplayer.class);
	    back[6] = new PropertyDescriptor("textColor", BrowserDisplayer.class);
	    return back;
	} catch (Exception ex) {
	    return null;
	}
    }
}
