/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

/* Languages and their ids which NetBeans is available*/
var LANGUAGE_IDS   = new Array();
var LANGUAGE_NAMES = new Array();

var PLATFORM_IDS         = new Array();
var PLATFORM_LONG_NAMES  = new Array();
var PLATFORM_SHORT_NAMES = new Array();

var BUNDLE_IDS   = new Array();
var BUNDLE_LONG_NAMES = new Array();
var BUNDLE_SHORT_NAMES = new Array();

PLATFORM_IDS   	     [0] = "windows";
PLATFORM_IDS   	     [1] = "linux";
PLATFORM_IDS         [2] = "solaris-x86";
PLATFORM_IDS         [3] = "solaris-sparc";
PLATFORM_IDS         [4] = "macosx";
PLATFORM_IDS         [5] = "zip";
/*
// Commented since NBI installers are not used for now
PLATFORM_IDS         [4] = "macosx-x86";
PLATFORM_IDS         [5] = "macosx-ppc";
*/

BUNDLE_IDS [0] = "javaee";
BUNDLE_IDS [1] = "mobility";
BUNDLE_IDS [2] = "javase";
BUNDLE_IDS [3] = "ruby";
BUNDLE_IDS [4] = "cpp";
BUNDLE_IDS [5] = "all";


function getNameById(id,ids,names) {
    for(var i = 0 ; i < ids.length; i++) {
	if(ids[i] == id) {
		return names[i];
	}
    }
    return "";
}

function getPlatformShortName(id) {
    return getNameById(id, PLATFORM_IDS, PLATFORM_SHORT_NAMES);
}
function getPlatformLongName(id) {
    return getNameById(id, PLATFORM_IDS, PLATFORM_LONG_NAMES);
}

function getLanguageName(id) {
    return getNameById(id, LANGUAGE_IDS, LANGUAGE_NAMES);
}

function getBundleShortName(id) {
    return getNameById(id, BUNDLE_IDS, BUNDLE_SHORT_NAMES);
}
function getBundleLongName(id) {
    return getNameById(id, BUNDLE_IDS, BUNDLE_LONG_NAMES);
}

