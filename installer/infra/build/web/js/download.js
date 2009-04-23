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

var PROPERTY_NONE      = 0;
var PROPERTY_FULL      = 1;
var PROPERTY_JAVA      = 2;
var PROPERTY_JAVASE    = 8;
var PROPERTY_RUBY      = 16;
var PROPERTY_CND       = 32;
var PROPERTY_PHP       = 64;
var PROPERTY_HIDDEN    = 128;

var INFO_ICON   = IMG_LOCATION + "info_icon.gif";
var INFO_ICON_H = IMG_LOCATION + "info_icon_h.gif";

var CHECKED_WHITE_SRC = IMG_LOCATION + "checked_badge_white.gif";
var CHECKED_BEIGE_SRC = IMG_LOCATION + "checked_badge_beige.gif";
var WARNING_WHITE_SRC = IMG_LOCATION + "warning_badge_white.gif";
var WARNING_BEIGE_SRC = IMG_LOCATION + "warning_badge_beige.gif";
var CHECKED_WHITE_NA_SRC = IMG_LOCATION + "checked_badge_notavailable.gif";

var IMAGE_CHECKED_WHITE_NOT_AVAILABLE = '<img src="' + CHECKED_WHITE_NA_SRC + '"/>';

var DOWNLOAD_BUTTON_NORMAL_SRC    = IMG_LOCATION + DOWNLOAD_BUTTON_NORMAL;
var DOWNLOAD_BUTTON_DISABLED_SRC  = IMG_LOCATION + DOWNLOAD_BUTTON_DISABLED;
var DOWNLOAD_BUTTON_HIGHLIGHT_SRC = IMG_LOCATION + DOWNLOAD_BUTTON_HIGHLIGHT;

var DOWNLOAD_IMG = '<img onmouseover="this.src=&quot;' + DOWNLOAD_BUTTON_HIGHLIGHT_SRC + '&quot;" onmouseout="this.src=&quot;' + DOWNLOAD_BUTTON_NORMAL_SRC + '&quot;" src="' + DOWNLOAD_BUTTON_NORMAL_SRC + '" style="cursor: pointer; border: 0;"/>';
var DOWNLOAD_IMG_DISABLED = '<img src="' + DOWNLOAD_BUTTON_DISABLED_SRC + '" style="border: 0;"/>';

    
var IMAGE_CHECKED_WHITE = '<img src="' + CHECKED_WHITE_SRC + '"/>';
var IMAGE_WARNING_WHITE = '<img src="' + WARNING_WHITE_SRC + '"/>';
var IMAGE_CHECKED_BEIGE = '<img src="' + CHECKED_BEIGE_SRC + '"/>';
var IMAGE_WARNING_BEIGE = '<img src="' + WARNING_BEIGE_SRC + '"/>';

var NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG_NUMBER = "1";

var download_tabs_number = 0;
var last_selected_lang = 0;
var MORE_LANG_ID = "more";

function handle_keyup(event) {
    //if (event.keyCode == 13) {
    //    download('standard');
    //}
}

function initialize() {
 if (document.images) {
     download_on = new Image();
     download_on.src  = DOWNLOAD_BUTTON_NORMAL_SRC; 
     download_off= new Image();
     download_off.src = DOWNLOAD_BUTTON_DISABLED_SRC;
     download_hl = new Image();
     download_hl.src  = DOWNLOAD_BUTTON_HIGHLIGHT_SRC;
     info = new Image();
     info.src  = INFO_ICON;
     info_h = new Image();
     info_h.src  = INFO_ICON_H;
     chw = new Image();
     chw.src  = CHECKED_WHITE_SRC;
     chb = new Image();
     chb.src  = CHECKED_BEIGE_SRC;
     ww = new Image();
     ww.src  = WARNING_WHITE_SRC;
     wb = new Image();
     wb.src  = WARNING_BEIGE_SRC;
     chwna = new Image();
     chwna.src = CHECKED_WHITE_NA_SRC;
 }
}

function write_languages() {
   for(var i=0; i < LANGUAGES.length; i++ ) {
   	document.write('<option value="' + LANGUAGES[i].id + '">' + LANGUAGES[i].name + '</option>');
   }
   if(ADD_MORE_REDIRECT_VALUE == 1) {
       document.write('<option value="' + MORE_LANG_ID + '">' + MORE_LANGUAGES + '</option>');
   } else if(SHOW_COMMUNITY_LANGUAGES == 1) {
       document.write('<option class="community_separator" value="' + MORE_LANG_ID + '">' + COMMUNITY_CONTRIBUTED_SEP + '</option>');
   }
}

function write_platforms() {
   for(var i=0; i < PLATFORM_IDS.length; i++ ) {
   	document.write('<option value="' + PLATFORM_IDS[i] + '">' + PLATFORM_LONG_NAMES[i] + '</option>');
   }
}

function write_components() {
    for (var i = 0; i < group_products.length; i++) {
        // skip the first group name as it goes to the title of the table
        if (i != 0) {
			document.write('<tr class="row_hover bottom_border top_border">');			
			document.write('    <th class="onhover_change left">' + ((i==1) ? BUNDLED_SERVERS_GROUP_NAME : group_display_names[i]) + '</th>');
			document.write('    <th class="onhover_change beige left_border"></th>');
			document.write('    <th class="onhover_change left_border"></th>');            
			document.write('    <th class="onhover_change beige left_border"></th>');
			document.write('    <th class="onhover_change left_border"></th>');
			document.write('    <th class="onhover_change beige left_border"></th>');
			//document.write('    <th class="onhover_change left_border"></th>');
			document.write('    <th class="onhover_change left_border right_border"></th>');
			document.write('</tr>');
        }

        for (var j = 0; j < group_products[i].length; j++) {
            var index = group_products[i][j];
            
            if (product_properties[index] & PROPERTY_HIDDEN) {
                continue;
            }
            
			document.write('<tr class="row_hover"' + (j % 2 ? ' class="even"' : '') + '>');
			
			document.write('    <td class="onhover_change left">');			

			document.write('<div id="product_' + index + '_description" class="pop_up">' + '<span style="font-weight:bold">'+ product_display_names[index] + '</span><br><br>' + product_descriptions[index] + '</div>');			
			document.write('<img src="' + INFO_ICON + '" onmouseover="this.src=&quot;' + INFO_ICON_H + '&quot;;show_description(' + index + ');" onmouseout="this.src=&quot;' + INFO_ICON +  '&quot;;hide_description(' + index + ');"></img>');
			//document.write('<span id="product_' + index + '_display_name" onmouseover="show_description(' + index + ');" onmouseout="hide_description(' + index + ');"><a class="product_display_name">' + product_display_names[index] + '</a></span>');
			document.write('<span id="product_' + index + '_display_name""><a class="product_display_name">' + product_display_names[index] + '</a></span>');
			if (product_notes[j] != '') {
				document.write('<br><span class="product_note">' + product_notes[index] + '</span>');
			}
			document.write('	</td>');

			document.write('    <td class="onhover_change beige left_border" id="product_' + index + '_javase"></td>');
			document.write('    <td class="onhover_change left_border" id="product_' + index + '_java"></td>');
			document.write('    <td class="onhover_change beige left_border" id="product_' + index + '_ruby"></td>');
			document.write('    <td class="onhover_change left_border" id="product_' + index + '_cnd"></td>');
			document.write('    <td class="onhover_change beige left_border" id="product_' + index + '_php"></td>');
			document.write('    <td class="onhover_change left_border right_border" id="product_' + index + '_full"></td>');
			document.write('</tr>');
        }
    }
}

function write_table_header() {
    document.write('<tr class="no_hover">');    
    document.write('<td class="no_border no_padding"></td>');    
    document.write('<td class="no_border no_padding" colspan="6">');    
    document.write('<table class="components_table">');
    document.write('<tr class="no_hover">');
    document.write('	<td class="no_hover header" colspan="6">' + (COMMUNITY_BUILD == 1 ? (NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG + '<span class="community_note_number">' + NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG_NUMBER + '</span>') : NETBEANS_DOWNLOAD_BUNDLES_MSG) + '</td>');
    document.write('</tr></table>');
    document.write('</td>');
    document.write('</tr>');
    document.write('<br><br>');	
    document.write('<tr class="no_hover">');
    document.write('<th class="left no_border bottom_border wide bottom">' + NETBEANS_PACKS_MSG + '<a class="star">*</a></th>');    
    document.write('<td class="no_border bottom_border" id="javase_bundle_name"> <a class="bundle_display_name">' + getBundleLongName("javase") + '</a></td>');
    document.write('<td class="no_border bottom_border" id="java_bundle_name">   <a class="bundle_display_name">' + getBundleLongName("java") + '</a></td>');
    document.write('<td class="no_border bottom_border" id="ruby_bundle_name">   <a class="bundle_display_name">' + getBundleLongName("ruby") + '</a></td>');
    document.write('<td class="no_border bottom_border" id="cnd_bundle_name">    <a class="bundle_display_name">' + getBundleLongName("cpp") + '</a></td>');
    document.write('<td class="no_border bottom_border" id="php_bundle_name">    <a class="bundle_display_name">' + getBundleLongName("php") + '</a></td>');
    document.write('<td class="no_border bottom_border" id="full_bundle_name">   <a class="bundle_display_name">' + getBundleLongName("all") + '</a></td>');
    document.write('</tr>');
}

function write_table_footer() {
    document.write('<tr class="column_hover">');
    document.write('<th class="no_hover left no_border  wide bottom">&nbsp;</th>');
    document.write('<td class="no_border download_button"  id="javase_link"><a href="javascript: download(\'javase\')"     id="javase_name"> ' + DOWNLOAD_IMG + '</a></td>');
    document.write('<td class="no_border download_button"  id="java_link">  <a href="javascript: download(\'java\')"       id="java_name"> '   + DOWNLOAD_IMG + '</a></td>');
    document.write('<td class="no_border download_button"  id="ruby_link">  <a href="javascript: download(\'ruby\')"       id="ruby_name"> '   + DOWNLOAD_IMG + '</a></td>');
    document.write('<td class="no_border download_button"  id="cnd_link">   <a href="javascript: download(\'cpp\')"        id="cnd_name"> '    + DOWNLOAD_IMG + '</a></td>');
    document.write('<td class="no_border download_button"  id="php_link">   <a href="javascript: download(\'php\')"        id="php_name"> '    + DOWNLOAD_IMG + '</a></td>');
    document.write('<td class="no_border download_button"  id="full_link">  <a href="javascript: download(\'all\')"        id="full_name"> '   + DOWNLOAD_IMG + '</a></td>');
    document.write('</tr>');
}

function write_components_sizes() {
    document.write('<tr class="no-hover">');
    document.write('<td class="no_border"></td>');
    document.write('<td class="no_border" id="javase_size"></td>');
    document.write('<td class="no_border" id="java_size"></td>');
    document.write('<td class="no_border" id="ruby_size"></td>');
    document.write('<td class="no_border" id="cnd_size"></td>');
    document.write('<td class="no_border" id="php_size"></td>');
    document.write('<td class="no_border" id="full_size"></td>');
    document.write('</tr>');
}

function show_description(index) {
    document.getElementById('product_' + index + '_description').style.visibility = 'visible';
}

function hide_description(index) {
    document.getElementById('product_' + index + '_description').style.visibility = 'hidden';
}

<!-- HIGHLIGHTING STARTS --> 
function highlight(){
	var table = document.getElementById("components_table");
	var tr = table.getElementsByTagName("tr");
	var cells =  new Array();
        for (var i=0;i<tr.length;i++){
		var arr = new Array();
		for(var j=0;j<tr[i].childNodes.length;j++){
			var node = tr[i].childNodes[j]; 
			if(node.nodeType==1) arr.push(node);
		}
		cells.push(arr);
	}

	for (var i=0;i<tr.length;i++){
		var arr = cells[i];
		for (var j=0;j<arr.length;j++){				
			arr[j].row = i;
			arr[j].col = j;
			if(arr[j].innerHTML == "&nbsp;" || arr[j].innerHTML == "") arr[j].className += " empty";					
			arr[j].css = arr[j].className;
			arr[j].hlCol = false;
			arr[j].hlRow = false;
			var classname = tr[i].className;
			if( classname.indexOf("column_hover") != -1)  {
				arr[j].hlCol = true;
			}
			if(classname.indexOf("row_hover") != -1) {
				arr[j].hlRow = true;
			}
                        if(arr[j].className.indexOf("no_hover") != -1) {
				arr[j].hlRow = false;
				arr[j].hlCol = false;
			}
			if(arr[j].hlRow || arr[j].hlCol) {
				arr[j].onmouseover = function(){
					over(table,cells,this);
				}
				arr[j].onmouseout = function(){
					out(table,cells,this);
				}
			}
		}
	}
}

function over(table,cells,obj){
	if (!obj.hlCol && !obj.hlRow) obj.className = obj.css + " over";  
	if(!(obj.col == 0 && obj.className.indexOf("empty") != -1)) {		
		if(obj.hlRow) highlightRow(table,cells,obj.row);		
		if(obj.hlCol) highlightCol(table,cells,obj.col);
	}
}
function out(table,cells,obj){
	if (!obj.hlCol && !obj.hlRow) obj.className = obj.css; 
	unhighlightRow(table,cells,obj.row);
	unhighlightCol(table,cells,obj.col);
}

function highlightCol(table,cells,col){
	var css = "over";
	for (var i=0;i<cells.length;i++){	
		var arr = cells[i];
		if(col<arr.length) {
			var obj = arr[col];
			if (obj.className.indexOf("onhover_change")!=-1) { 
				obj.className = obj.css + " " + css;
			}
		}
	}
}

function unhighlightCol(table,cells,col){
	for (var i=0;i<cells.length;i++){
		var arr = cells[i];
		if(col<arr.length) {				
		    var obj = arr[col];
		    obj.className = obj.css;
		}
	}
}

function highlightRow(table,cells,row){
	var css =  "over";
	var tr = table.getElementsByTagName("tr")[row];		
	for (var i=0;i<tr.childNodes.length;i++){		
		var obj = tr.childNodes[i];
		obj.className = obj.css + " " + css; 		
	}
}

function unhighlightRow(table,cells,row){
	var tr = table.getElementsByTagName("tr")[row];		
	for (var i=0;i<tr.childNodes.length;i++){
		var obj = tr.childNodes[i];			
		obj.className = obj.css; 			
	}
}
<!-- HIGHLIGHTING END --> 


function detect_platform() {
    var agent = navigator.userAgent;

    if (agent.indexOf("Windows") != -1) {
        document.getElementById("platform_select").selectedIndex = 0;
    }
    if (agent.indexOf("Linux") != -1) {
        document.getElementById("platform_select").selectedIndex = 1;
    }
    if (agent.indexOf("SunOS i86pc") != -1) {
        document.getElementById("platform_select").selectedIndex = 2;
    }
    if (agent.indexOf("SunOS sun4") != -1) {
        document.getElementById("platform_select").selectedIndex = 3;
    }
    if (agent.indexOf("Intel Mac OS") != -1) {
        document.getElementById("platform_select").selectedIndex = 4;
    }
    if (agent.indexOf("PPC Mac OS") != -1) {
        document.getElementById("platform_select").selectedIndex = 4;
    }
}

function select_language() {
    var language = get_language_id(LANGUAGES);
    var select = document.getElementById("language_select");
    var languageOptions = select.options;
    for(var i=0;i<languageOptions.length;i++) {
        if(languageOptions[i].value == language) select.selectedIndex = i;
    }
    last_selected_lang = select.selectedIndex;
}

function update() {
    var langselect = document.getElementById("language_select");
    if(langselect.options[langselect.selectedIndex].value == MORE_LANG_ID) {
        langselect.selectedIndex = last_selected_lang;
        window.location = MORE_LANGUAGES_REDIRECT_URL;
    }
    last_selected_lang = langselect.selectedIndex;

    var select = document.getElementById("platform_select");
    var platform = select.options[select.selectedIndex].value;
    var platform_display_name = select.options[select.selectedIndex].text;


    /*For NetBeans we have native mac installer*/
    
    if(platform=="macosx") {
	platform = "macosx-x86";
    }

    // update the "checks" and generate error messages, if any
    var product_messages = new Array();
    for (var i = 0; i < product_uids.length; i++) {
        if (product_properties[i] & PROPERTY_HIDDEN) {
            continue;
        }
        
        // enter the default value
        product_messages[i] = null;
        
        if (!is_compatible(i, platform)) {
	     product_messages[i] = product_display_names[i];
        }
		
        if (product_properties[i] & PROPERTY_FULL) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_full").innerHTML = IMAGE_CHECKED_WHITE;
            } else {
                document.getElementById("product_" + i + "_full").innerHTML = IMAGE_WARNING_WHITE;
            }
        } else {
            document.getElementById("product_" + i + "_full").innerHTML = '';
        }
		
        if (product_properties[i] & PROPERTY_JAVA) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_java").innerHTML = IMAGE_CHECKED_BEIGE;
            } else {
                document.getElementById("product_" + i + "_java").innerHTML = IMAGE_WARNING_BEIGE;
            }
        } else {
            document.getElementById("product_" + i + "_java").innerHTML = '';
        }

        if (product_properties[i] & PROPERTY_JAVASE) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_javase").innerHTML = IMAGE_CHECKED_BEIGE;
            } else {
                document.getElementById("product_" + i + "_javase").innerHTML = IMAGE_WARNING_BEIGE;
            }
        } else {
            document.getElementById("product_" + i + "_javase").innerHTML = '';
        }	

		
	if (product_properties[i] & PROPERTY_RUBY) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_ruby").innerHTML = IMAGE_CHECKED_WHITE;
            } else {
                document.getElementById("product_" + i + "_ruby").innerHTML = IMAGE_WARNING_WHITE;
            }
        } else {
            document.getElementById("product_" + i + "_ruby").innerHTML = '';
        }
		
        if (product_properties[i] & PROPERTY_CND) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_cnd").innerHTML = IMAGE_CHECKED_BEIGE;
            } else {
                document.getElementById("product_" + i + "_cnd").innerHTML = IMAGE_WARNING_BEIGE;
            }
        } else {
            document.getElementById("product_" + i + "_cnd").innerHTML = '';
        }
        if (product_properties[i] & PROPERTY_PHP) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_php").innerHTML = IMAGE_CHECKED_BEIGE;
            } else {
                document.getElementById("product_" + i + "_php").innerHTML = IMAGE_WARNING_BEIGE;
            }
        } else {
            document.getElementById("product_" + i + "_php").innerHTML = '';
        }

	if (product_messages[i] == null) {
		document.getElementById("product_" + i + "_display_name").innerHTML = '<a class="product_display_name">' + product_display_names[i] + "</a>";
	} else {
		document.getElementById("product_" + i + "_display_name").innerHTML = '<a class="product_display_name_no">' + product_display_names[i] + "</a>";
	}
    }
    
    // update the error message
    var error_message = "";
    var messages_number = 0;
    
    for (var i = 0; i < product_uids.length; i++) {
        if (product_messages[i] != null) {
        messages_number += 1;
        }
    }
    if (messages_number != 0 ) {
	
	var messages_counter = 0;	
	
	error_message += NOTE_PREFIX;
	
    	for (var j = 0; j < product_uids.length; j++) {
        	if (product_messages[j] != null) {
			
			if ( messages_counter == 0) {
            			error_message += product_messages[j];
			} else if (messages_counter == (messages_number-1) ){
				error_message = NOTE_AND_SEP.replace('{0}', error_message).replace('{1}', product_messages[j]);
			} else {
				error_message = NOTE_COMMA_SEP.replace('{0}', error_message).replace('{1}', product_messages[j]);
			}
			
			messages_counter += 1;			
        	}
    	}
	var na = '';
	if (messages_number == 1 ) {
		if ( platform == "zip" ) {
			na = SINGLE_NOT_AVAILABLE_ZIP;
		} else {
			na = SINGLE_NOT_AVAILABLE_BUNDLE;
		}		
        } else {
		if ( platform == "zip" ) {
			na = MULTIPLE_NOT_AVAILABLE_ZIP;
		} else {
			na = MULTIPLE_NOT_AVAILABLE_BUNDLE;
		}
	}
	error_message = na.replace('{0}', error_message).replace('{1}', platform_display_name);;	
    } else {
	error_message = '<br>';
    }

    // use positive wording instead of negative
    
    if ( platform == "zip" ) {
        error_message = NOTE_ZIP;    
    } else if(platform.indexOf("macosx")!=-1) {
	error_message = NOTE_MACOSX;
    } else if(platform.indexOf("solaris")!=-1) {
	error_message = NOTE_SOLARIS;
    } else {
        error_message = NOTE_OTHER;
    }


    document.getElementById("error_message").innerHTML = error_message;
    
    // update the sizes 
    var full_size   = 0;
    var java_size = 0;
    var javase_size   = 0;    
    var ruby_size   = 0;
    var cnd_size    = 0;
    var php_size    = 0;
    

    for (var i = 0; i < product_uids.length; i++) {
        if (!is_compatible(i, platform)) {
            continue;
        }
		
	if (product_properties[i] & PROPERTY_FULL) {
            full_size += new Number(product_download_sizes[i]);
        }
        
	if (product_properties[i] & PROPERTY_JAVA) {
            java_size += new Number(product_download_sizes[i]);
        }        	
		
        if (product_properties[i] & PROPERTY_JAVASE) {
            javase_size += new Number(product_download_sizes[i]);
        }
		
        if (product_properties[i] & PROPERTY_RUBY) {
            ruby_size += new Number(product_download_sizes[i]);
        }
        
	if (product_properties[i] & PROPERTY_CND) {
            cnd_size += new Number(product_download_sizes[i]);
        }        
	if (product_properties[i] & PROPERTY_PHP) {
            php_size += new Number(product_download_sizes[i]);
        }        
    }
	
    full_size   = Math.ceil(full_size / 1024.0);
    javase_size = Math.ceil(javase_size / 1024.0);
    java_size   = Math.ceil(java_size / 1024.0);
    ruby_size   = Math.ceil(ruby_size / 1024.0);
    cnd_size    = Math.ceil(cnd_size / 1024.0);
    php_size    = Math.ceil(php_size / 1024.0);

    if( platform == "zip") {       
       full_size   = 191;
       javase_size =  71;
       java_size   = 149;
       ruby_size   =  60;
       cnd_size    =  36;
       php_size    =  37;
    } 

    if(platform.indexOf("macosx")!=-1) {
	platform = "macosx";
    }


    full_size   = get_file_size_mb(get_file_name(platform, "all"),    full_size);
    javase_size = get_file_size_mb(get_file_name(platform, "javase"), javase_size);
    java_size   = get_file_size_mb(get_file_name(platform, "java"),   java_size);
    ruby_size   = get_file_size_mb(get_file_name(platform, "ruby"),   ruby_size);
    cnd_size    = get_file_size_mb(get_file_name(platform, "cpp"),    cnd_size);
    php_size    = get_file_size_mb(get_file_name(platform, "php"),    php_size);

    document.getElementById("full_size").innerHTML   = FREE_SIZE_MESSAGE.replace('{0}', full_size  );
    document.getElementById("java_size").innerHTML = FREE_SIZE_MESSAGE.replace('{0}', java_size);    
    document.getElementById("javase_size").innerHTML   = FREE_SIZE_MESSAGE.replace('{0}', javase_size  );
    document.getElementById("ruby_size").innerHTML   = FREE_SIZE_MESSAGE.replace('{0}', ruby_size  );
    document.getElementById("cnd_size").innerHTML    = FREE_SIZE_MESSAGE.replace('{0}', cnd_size   );
    document.getElementById("php_size").innerHTML    = FREE_SIZE_MESSAGE.replace('{0}', php_size   );
    
    if (platform.indexOf("macosx")!=-1) {
        document.getElementById("jdk_note").innerHTML = JDK_NOTE_MACOSX;
    }
    else {
        document.getElementById("jdk_note").innerHTML = JDK_NOTE_ALL.replace('{0}',JAVA_COM_LINK).replace('{1}',JDK_DOWNLOAD_LINK).replace('{2}',NBJDK_DOWNLOAD_LINK);
    }

    if (COMMUNITY_BUILD == 1) {
	document.getElementById("community_number").innerHTML  = "<a class=\"special_message_number\">" + NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG_NUMBER + "</a>";
	document.getElementById("community_message").innerHTML = "<a class=\"special_message_text\">" + COMMUNITY_MESSAGE + "</a>";
    }
    else {
	document.getElementById("community_message").innerHTML = "";
	document.getElementById("community_number").innerHTML  = "";
    }
}

function is_compatible(index, platform) {
    if ( platform == "zip" ) {
         for (var i = 0; i < group_products.length; i++) {
          for (var j = 0; j < group_products[i].length; j++) {
              var idx = group_products[i][j];
              if((idx==index) && (i == 0) ) {
                   return true;
              } 
          }
        }
    } else {
        for (var i = 0; i < product_platforms[index].length; i++) {
            if (product_platforms[index][i] == platform) {
                return true;
            }
        }
    }    
    
    return false;
}

function get_file_size_mb(name,defaultValue) {
   var size = getSize(name);
   if(size=="") {
       size = defaultValue;
   } else {
       size = Math.ceil(size / (1024 * 1024));
   }
   return size;
}

function add_download_tab(name, url) {
   if(download_tabs_number!=0) {
       document.write(" | ");
   }
   if(url) {
	writeUrl(url,name);
   } else { 
	message('<span class="download_tab_active">' + name + '</span>');
   }
   download_tabs_number++;
}

function write_files_list(title,directory) {
    document.write('<h1>' + title + '</h1>');
    document.write('<ul>');
    var lst = get_file_list(directory);
    for(var i=0;i<lst.length;i++) {
        var item_display_name = lst[i];
        var item_link = "javascript: download_file(\'" + directory + lst[i] + "\')";
        document.write('<li><a href="' + item_link  + '">' + item_display_name + '</a></li>');
    }
    document.write('</ul><br>');
}

function open_zip_link() {
    var overriden_language=get_overridden_language();
    var zip_url = ZIP_PAGE;
    if(overriden_language!=DEFAULT_LANGUAGE) {
        zip_url += "?" + PAGELANG_SEP + overriden_language;
    }
    window.location = zip_url; 
}

function download_file(filename) {
    var download_url = START_PAGE;
    download_url += "?filename=" + filename;

    var overriden_language=get_overridden_language();
    if(overriden_language!=DEFAULT_LANGUAGE) {
        download_url += "&" + PAGELANG_SEP + overriden_language;
    }
    if(ADD_VERSION_INFO_TO_URL == 1) {
        download_url += "&version=" + BUILD_DISPLAY_VERSION_SHORT;
    }
    window.location = download_url;
}

function download(option) {
    var select = document.getElementById("platform_select");
    var platform = select.options[select.selectedIndex].value;

    var download_url = START_PAGE;
    download_url += "?platform=" + platform;

    var language_select = document.getElementById("language_select");
    var language = language_select.options[language_select.selectedIndex].value;
    download_url += "&lang=" + language;
    download_url += "&option=" + option;
    var email = document.getElementById("emailfield").value;
    if(email!="" && email.indexOf(".")!=-1 && email.indexOf("@")!=-1 && email.indexOf("&")==-1 && email.indexOf("?")==-1) {
	var monthly = (document.getElementById("monthlycb").checked ? 1 : 0);
	var weekly  = (document.getElementById("weeklycb").checked ? 1 : 0);
	var contact = (document.getElementById("contactcb").checked ? 1 : 0);
	if(monthly==1 || weekly ==1 || contact==1) {
		download_url+= "&email="   + email;
		download_url+= "&monthly=" + monthly;
		download_url+= "&weekly="  + weekly;
		download_url+= "&contact=" + contact;
        }
    }
    var overriden_language=get_overridden_language();
    if(overriden_language!=DEFAULT_LANGUAGE) {
        download_url += "&" + PAGELANG_SEP + overriden_language;
    }
    if(ADD_VERSION_INFO_TO_URL == 1) {
        download_url += "&version=" + BUILD_DISPLAY_VERSION_SHORT;
    }
    window.location = download_url;
}
