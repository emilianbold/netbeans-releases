/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 *//*
 * CheckForJFreeChart.java
 *
 * Created on October 5, 2002, 2:14 AM
 */

package org.netbeans.performance.antext;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author Tim Boudreau
 */
public class CheckForJFreeChart extends Task {
    
    public void execute() throws BuildException {
        boolean foundit = false;
        try {
            Class c = Class.forName("org.jrefactory.chart.ChartFactory"); //NOI18N
            foundit = true;
        } catch (ClassNotFoundException cnfe) {
            //do nothing
        }
        //XXX Could be really clever and download it for them...
        StringBuffer sb = new StringBuffer ("Drawing charts requires that you have JFreeChart installed\n");
        sb.append ("It is not distributed with this package due to license incompatibility (it can't be\n");
        sb.append("hosted on netbeans.org).  Download it from \nhttp://prdownloads.sourceforge.net/jfreechart/jfreechart-0.9.3.tar.gz?download\n");
        sb.append("And put the chart and common jars in the lib/ext directory below this build script.\n");
        throw new BuildException (sb.toString());
    }
    
}
