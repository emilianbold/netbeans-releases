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


var redirect_delay = 1000;

var lang_id="";
var option_id="";
var platform_id="";
var url = "";
var filename = "";	    
var string = "";
var parent_folder = "";

function initialize() {
	    string = "" + window.location;	    
            var query    = string.substring(string.indexOf("?") + 1, string.length);
	    var sep = "&";	    
	    var email = "";
            var monthly = "0";
	    var weekly  = "0";
	    var contact = "0";
	    var email_sep    = "email=";
            var monthly_sep  = "monthly=";
            var weekly_sep   = "weekly=";
            var contact_sep  = "contact=";
            var start_page_string = (string.indexOf("?")==-1) ? string : string.substring(0, string.indexOf("?"));            
            parent_folder  = start_page_string.substring(0, start_page_string.lastIndexOf("/") + 1);

	    if(query!="" && query != string && query.indexOf(sep)!=-1)  {
	            url      = query.substring(0, query.indexOf(sep));	                
		    filename = url.substring(url.lastIndexOf("/") + 1, url.length);
		    query = query.substring(query.indexOf(sep) + 1, query.length);
		    
		    while(query!="") {
		            var lang_sep     = "lang=";
			    var platform_sep = "platform=";
			    var option_sep   = "option=";
                            

			    if(query.indexOf(lang_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					lang_id = query.substring(lang_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
        	        	 } else {	
					lang_id = query.substring(lang_sep.length, query.length);
					query = "";
				 }		 
		            } else if(query.indexOf(platform_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					platform_id = query.substring(platform_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					platform_id = query.substring(platform_sep.length, query.length);
					query = "";
				 }		 
			   } else if(query.indexOf(option_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					option_id = query.substring(option_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					option_id = query.substring(option_sep.length, query.length);
					query = "";
				 }		 
		           } else if(query.indexOf(email_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					email = query.substring(email_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					email = query.substring(email_sep.length, query.length);
					query = "";
				 }		 
		           } else if(query.indexOf(monthly_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					monthly = query.substring(monthly_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					monthly = query.substring(monthly_sep.length, query.length);
					query = "";
				 }		 
		            } else if(query.indexOf(weekly_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					weekly = query.substring(weekly_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					weekly = query.substring(weekly_sep.length, query.length);
					query = "";
				 }		 
		            } else if(query.indexOf(contact_sep)==0) { 
				 if(query.indexOf(sep)!=-1) {
					contact = query.substring(contact_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {	
					contact = query.substring(contact_sep.length, query.length);
					query = "";
				 }		 
		            } else {
				query = "";
			    }
	            }
		    if(email!="") {			
			var phpRequest = SUBSCRIPTION_PHP_URL;
			phpRequest += "?" + email_sep   + email;
			phpRequest += "&" + monthly_sep + monthly;
			phpRequest += "&" + weekly_sep  + weekly;
			phpRequest += "&" + contact_sep + contact;
			phpRequest += "&timestamp=" + new Date().getTime();
			var image = new Image();
			image.src = phpRequest;
			image.style.display="none";
		    } 
            	    window.onload = delayedredirect;
            }
}

function delayedredirect() {
     setTimeout("redirect()",redirect_delay);
}
function redirect() {
     window.location = url;
}

function write_download_header() {
	document.write('<p id="download_link_p">');
	document.write(AUTOMATIC_DOWNLOAD_MESSAGE.replace('{0}',url));
	document.write('</p>');
}

function getMD5(name) {
	var md5 = "";
        for (var i = 0; i < file_names.length; i++) {		
		if(file_names[i] == filename) {		
			md5 = file_md5s[i];
			break;
		}
        }
	return md5;
}

function getSize(name) {
	var size = "";
        for (var i = 0; i < file_names.length; i++) {		
		if(file_names[i] == filename) {		
			size = file_sizes[i];
			break;
		}
        }
	return size;
}

function write_download_info() {
	var size = getSize(filename);
	var md5 = getMD5(filename);		
	var platform_display_name = getPlatformShortName(platform_id);
	var lang_display_name     = getLanguageName(lang_id);
        var option_display_name   = getBundleShortName(option_id);

	
	/* format size */
	mb = Math.floor(size / (1024 * 1024));
	mb_dec = Math.floor((size - (mb * 1024 * 1024))/ (1024 * 102));		
        size = mb + ((mb_dec>0) ? ('.' + mb_dec) : '');

	document.write('<br>');
        document.write('<p class="file_information">');

	if (platform_display_name!="" && lang_display_name!="" && filename!="") {
		 var info = INFO_MESSAGE.
				replace('{0}', PRODUCT_NAME).
		 		replace('{1}', ((option_display_name != "") ? (' ' + option_display_name) : '')).
		 		replace('{2}', ((platform_id == 'zip') ? (platform_display_name) : (INSTALLER_MESSAGE.replace('{0}',platform_display_name)))).
		 		replace('{3}', lang_display_name).
		 		replace('{4}', lang_id).
		 		replace('{5}', filename).
				replace('{6}', size).
		 		replace('{7}', md5);
		 document.write(info);
    	} else {
		document.write(NOFILE_MESSAGE);
	}
	document.write('</p>');
}

function omniture_download_link() {
    var anchorElement = document.getElementById("download_link_p").getElementsByTagName("a")[0];//there is only one anchor in that paragraph
    if(option_id!="" && platform_id!="" && lang_id!="") {
    if(s_siteid) { // this variable is defined only if s_code_remote.js was downloaded
        s_linkType = "d";
        s_linkName = s_siteid + option_id;
        s_events   = "event7";
        s_products = "Downloaded Products;NetBeans IDE " + BUILD_DISPLAY_VERSION_SHORT + " | " + platform_id + " | " + option_id;
        s_eVar3    = "netbeans";
        s_eVar8    = option_id;
        s_prop19   = platform_id;
        s_prop20   = lang_id;
        s_prop21   = BUILD_DISPLAY_VERSION_SHORT;
        s_linkTrackVars   = "events,products,eVar3,eVar8,prop19,prop20,prop21";
        s_linkTrackEvents = "event7";
        s_lnk=s_co(anchorElement);
        s_gs(s_account); 
    }
    }
}
