/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
