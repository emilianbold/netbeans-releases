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
* Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.gravy;

import org.netbeans.jemmy.Waitable;
import org.netbeans.modules.visualweb.gravy.MainWindowOperator;

/**
 * This class is used for observation of status of some component.
 */
public class StatusWaitable implements Waitable {
    private String[] variants;
    private String valid;
    private boolean exactMatch;
    private boolean thread;

    /**
     * Creates an instance of this class.
     * @param variants an array of Strings, which could appear in an observable component.
     */
    public StatusWaitable(String[] variants) {
	this.variants = variants;
	this.exactMatch = true;
	this.thread = false;
    }

    /**
     * Creates an instance of this class.
     * @param variants an array of Strings, which could appear in an observable component.
     * @param isExact a boolean value, which defines a mode of String comparison: 
     * true means using of the method equals(), false means using of the method indexOf().
     */
    public StatusWaitable(String[] variants, boolean isExact) {
	this.variants = variants;
	this.exactMatch = isExact;
	this.thread = false;
    }

    /**
     * Creates an instance of this class.
     * @param variant1 a string, which could appear in an observable component.
     * @param variant2 a string, which could appear in an observable component.
     */
    public StatusWaitable(String variant1, String variant2) {
	this.variants = new String[] {variant1, variant2};
	this.exactMatch = false;
	this.thread = true;
    }

    /**
     * Creates an instance of this class.
     * @param variant1 a string, which could appear in an observable component.
     * @param variant2 a string, which could appear in an observable component.
     * @param variant3 a string, which could appear in an observable component.
     */
    public StatusWaitable(String variant1, String variant2, String variant3) {
	this.variants = new String[] {variant1, variant2, variant3};
	this.exactMatch = false;
	this.thread = true;
    }

    /**
     * Checks if wait criteria have been met.
     * @param o optional waiting parameter.
     * @return null if criteria has not been met.
     */    
    public Object actionProduced(Object o) {
        if(exactMatch){
            for(int i=0;i<variants.length;i++) {
                if (variants[i].equals(getStatusText())) {
                    valid = variants[i];
                    return Boolean.TRUE;
                }
            }
	}else{
	    if(thread){
	        String status = getStatusText();
	        if(status.startsWith(variants[0])){
	           int index = status.lastIndexOf(variants[1]);
	           int index1 = 0;
                   if(variants.length==3) index1 = status.lastIndexOf(variants[2]);
	           if(index+variants[1].length() == status.length()){
	               return Boolean.TRUE;
                   }
	           if(variants.length==3) if(index1+variants[2].length() == status.length()){
	               return Boolean.TRUE;
                   }
                }
            }else{
                for(int i=0;i<variants.length;i++) {
                    if (getStatusText().indexOf(variants[i])!=-1) {
                        valid = variants[i];
                        return Boolean.TRUE;
                    }
                }
            }
        }
	return null;
    }

    /**
     * Returns description.
     * @return a text description of the wait criteria.
     */
    public String getDescription() {
	String dsc = "";
	for(int i=0;i<variants.length;i++) {
	    dsc+=variants[i] + "|";
	}
	return "Variants = " + dsc;
    }

    /**
     * Returns current status of an observable component.
     * @return a text corresponding to current status.
     */
    public String getStatus() {
	return valid;
    }

    /**
     * Returns current text from status line of the IDE's main window.
     * @return a text from IDE's status line.
     */
    private String getStatusText() {
	String s = (Util.getMainWindow()).getStatusText();
	return (s == null) ? "" : s; 
    }
}
