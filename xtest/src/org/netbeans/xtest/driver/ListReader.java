/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * ListReader.java
 *
 * Created on November 8, 2001, 5:23 PM
 */

package org.netbeans.xtest.driver;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.StringTokenizer;

/** This task reads list devided by ',' from attribute 'list' and sets properties
 * which name is 'prefix' attribute + item from list. Value of those properties
 * will "true" or value of attribute 'value'.
 *
 * Example:
 *
 * <list list="first,second,thirdecute" prefix="myproperty"/>
 *
 * will set properties: myproperty.first=true, myproperty.second=true, myproperty.third=true, 
 *
 * @author lm97939
 */
public class ListReader extends Task {

    private String list;
    private String prefix;
    private String value = "true";
    
    public void setList(String s) {
        list = s;
    }
    
    public void setPrefix(String p) {
        prefix = p;
    }
    
    public void setValue(String v) {
        value = v;   
    }

    public void execute() throws BuildException {
        
        if (list == null) throw new BuildException("Property list is empty!");
        if (prefix == null) throw new BuildException("Property prefix is empty!");
        
        StringTokenizer tokens = new StringTokenizer(list,",");
        while (tokens.hasMoreTokens()) {
            String property = prefix+"."+tokens.nextElement();
            getProject().setProperty(property,value);
        }
    }

}
