/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

var PROPERTY_NONE      = 0;
var PROPERTY_FULL      = 1;
var PROPERTY_JAVAEE    = 2;
var PROPERTY_JAVAME    = 4;
var PROPERTY_JAVA      = 8;
var PROPERTY_RUBY      = 16;
var PROPERTY_CND       = 32;
var PROPERTY_HIDDEN    = 64;

function handle_keyup(event) {
    //if (event.keyCode == 13) {
    //    download('standard');
    //}
}

function write_components() {
    for (var i = 0; i < group_products.length; i++) {
        // skip the first group name as it goes to the title of the table
        if (i != 0) {
			document.write('<tr class="bottom_border top_border">');
			document.write('    <th class="left">' + group_display_names[i] + '</th>');
			document.write('    <th class="beige left_border"></th>');
			document.write('    <th class="left_border"></th>');            
			document.write('    <th class="beige left_border"></th>');
			document.write('    <th class="left_border"></th>');
			document.write('    <th class="beige left_border"></th>');
			document.write('    <th class="left_border"></th>');
			document.write('</tr>');
        }

        for (var j = 0; j < group_products[i].length; j++) {
            var index = group_products[i][j];
            
            if (product_properties[index] & PROPERTY_HIDDEN) {
                continue;
            }
            
			document.write('<tr' + (j % 2 ? ' class="even"' : '') + '>');
			
			document.write('    <td class="left">');
			document.write('<div id="product_' + index + '_description" class="pop_up">' + product_display_names[index] + '<br><br>' + product_descriptions[index] + '</div>');
			document.write('<span id="product_' + index + '_display_name" onmouseover="show_description(' + index + ');" onmouseout="hide_description(' + index + ');"><a class="product_display_name">' + product_display_names[index] + '</a></span>');
			
			if (product_notes[j] != '') {
				document.write('<br><span class="product_note">' + product_notes[index] + '</span>');
			}
			document.write('	</td>');

			document.write('    <td class="beige left_border" id="product_' + index + '_javaee"></td>');
			document.write('    <td class="left_border" id="product_' + index + '_javame"></td>');
			document.write('    <td class="beige left_border" id="product_' + index + '_java"></td>');
			document.write('    <td class="left_border" id="product_' + index + '_ruby"></td>');
			document.write('    <td class="beige left_border" id="product_' + index + '_cnd"></td>');
			document.write('    <td class="left_border" id="product_' + index + '_full"></td>');
			
			document.write('</tr>');
        }
    }
}

function write_table_header() {
    document.write('<tr>');
    document.write('<td class="no_border no_padding" id="javaee_link"></th>');
    document.write('<td class="no_border no_padding" colspan="6">');
    document.write('<table><tr>');
    document.write('	<td class="no_border no_padding" style="width: 50%;"><img src="img/1px-gray.png" style="width: 100%; height: 1px"/></td>');
    document.write('	<td class="no_border title bold" style="color:black"">NetBeans IDE 6.0 {build.number}</td>');
    document.write('	<td class="no_border no_padding" style="width: 50%;"><img src="img/1px-gray.png" style="width: 100%; height: 1px"/></td>');
    document.write('</tr></table>');
    document.write('</td>');
    document.write('</tr>');
    document.write('<br><br>');	
    document.write('<tr>');
    document.write('<th class="left no_border bottom_border wide bottom">NetBeans Packs<img src="img/bold_comment_badge_header.gif"/></th>');    
    document.write('<td class="no_border left_border bottom_border" id="javaee_link"><a href="javascript: download(\'javaee\')"><img src="img/download.gif" style="cursor: pointer; border: 0;"/><br/></a><a class="bundle_display_name">Web&nbsp;&amp;&nbsp;Java&nbsp;EE</a></td>');
    document.write('<td class="no_border left_border bottom_border" id="javame_link"><a href="javascript: download(\'mobility\')" id="javame_name"><img src="img/download.gif" style="cursor: pointer; border: 0;"/><br/></a><a class="bundle_display_name">Mobility</a></td>');
    document.write('<td class="no_border left_border bottom_border" id="java_link"><a href="javascript: download(\'javase\')"><img src="img/download.gif" style="cursor: pointer; border: 0;"/><br/></a><a class="bundle_display_name">Java SE</a></td>');
    document.write('<td class="no_border left_border bottom_border" id="ruby_link"><a href="javascript: download(\'ruby\')"><img src="img/download.gif" style="cursor: pointer; border: 0;"/><br/></a><a class="bundle_display_name">Ruby</a></td>');
    document.write('<td class="no_border left_border bottom_border" id="cnd_link"><a href="javascript: download(\'cpp\')"><img src="img/download.gif" style="cursor: pointer; border: 0;"/><br/></a><a class="bundle_display_name">C/C++</a></td>');
    document.write('<td class="no_border left_border bottom_border" id="full_link"><a href="javascript: download(\'all\')"><img src="img/download_h.gif" style="cursor: pointer; border: 0;"/><br/></a><a class="bundle_display_name">All</a></td>');
    document.write('</tr>');
}

function show_description(index) {
    document.getElementById('product_' + index + '_description').style.visibility = 'visible';
}

function hide_description(index) {
    document.getElementById('product_' + index + '_description').style.visibility = 'hidden';
}

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
    if (agent.indexOf("SunOS sun4u") != -1) {
        document.getElementById("platform_select").selectedIndex = 3;
    }
    if (agent.indexOf("Intel Mac OS") != -1) {
        document.getElementById("platform_select").selectedIndex = 4;
    }
    if (agent.indexOf("PPC Mac OS") != -1) {
        document.getElementById("platform_select").selectedIndex = 5;
    }
}

function update() {
    var select = document.getElementById("platform_select");
    var platform = select.options[select.selectedIndex].value;
    var platform_display_name = select.options[select.selectedIndex].text;
    
    // update the "checks" and generate error messages, if any
    var product_messages = new Array();
    for (var i = 0; i < product_uids.length; i++) {
        if (product_properties[i] & PROPERTY_HIDDEN) {
            continue;
        }
        
        // enter the default value
        product_messages[i] = null;
        
        if (!is_compatible(i, platform)) {
            //product_messages[i] = '<tr><td class="no_padding no_border"><img src="img/warning_badge_text_' + platform + '.gif"/></td><td class="no_padding no_border left"><span class="warning">' + product_display_names[i] + ' is not available for ' + platform_display_name + '.</span></td></tr>';
	     product_messages[i] = product_display_names[i];
        }
		
        if (product_properties[i] & PROPERTY_FULL) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_full").innerHTML = '<img src="img/checked_badge_beige.gif"/>';
            } else {
                document.getElementById("product_" + i + "_full").innerHTML = '<img src="img/warning_badge_beige_' + platform + '.gif"/>';
            }
        } else {
            document.getElementById("product_" + i + "_full").innerHTML = '';
        }
		
        if (product_properties[i] & PROPERTY_JAVAEE) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_javaee").innerHTML = '<img src="img/checked_badge_beige.gif"/>';
            } else {
                document.getElementById("product_" + i + "_javaee").innerHTML = '<img src="img/warning_badge_beige_' + platform + '.gif"/>';
            }
        } else {
            document.getElementById("product_" + i + "_javaee").innerHTML = '';
        }        
		
	if (product_properties[i] & PROPERTY_JAVAME) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_javame").innerHTML = '<img src="img/checked_badge_beige.gif"/>';
            } else {
                document.getElementById("product_" + i + "_javame").innerHTML = '<img src="img/warning_badge_beige_' + platform + '.gif"/>';
            }
        } else {
            document.getElementById("product_" + i + "_javame").innerHTML = '';
        }
		
        if (product_properties[i] & PROPERTY_JAVA) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_java").innerHTML = '<img src="img/checked_badge_beige.gif"/>';
            } else {
                document.getElementById("product_" + i + "_java").innerHTML = '<img src="img/warning_badge_beige_' + platform + '.gif"/>';
            }
        } else {
            document.getElementById("product_" + i + "_java").innerHTML = '';
        }
		
		if (product_properties[i] & PROPERTY_RUBY) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_ruby").innerHTML = '<img src="img/checked_badge_beige.gif"/>';
            } else {
                document.getElementById("product_" + i + "_ruby").innerHTML = '<img src="img/warning_badge_beige_' + platform + '.gif"/>';
            }
        } else {
            document.getElementById("product_" + i + "_ruby").innerHTML = '';
        }
		
        if (product_properties[i] & PROPERTY_CND) {
            if (product_messages[i] == null) {
                document.getElementById("product_" + i + "_cnd").innerHTML = '<img src="img/checked_badge_beige.gif"/>';
            } else {
                document.getElementById("product_" + i + "_cnd").innerHTML = '<img src="img/warning_badge_beige_' + platform + '.gif"/>';
            }
        } else {
            document.getElementById("product_" + i + "_cnd").innerHTML = '';
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
	
	error_message = '<td class="no_padding no_border left">' + 
			'<span class="warning">';
	error_message += 'Note: ';
	
    	for (var j = 0; j < product_uids.length; j++) {
        	if (product_messages[j] != null) {
			
			if ( messages_counter == 0) {
            			error_message += product_messages[j];
			} else if (messages_counter == (messages_number-1) ){
				error_message += ' and ' + product_messages[j];
			} else {
				error_message += ', ' + product_messages[j];
			}
			
			messages_counter += 1;			
        	}
    	}
	if (messages_number == 1 ) {
		error_message += ' is not available for';
        } else {
		error_message += ' are not available for';
	}
	error_message += ' ' + platform_display_name;
	
	error_message += '.</span></td>';
    } else {
	error_message = '<br>';
    }

    //error_message += '</table>';
    
    document.getElementById("error_message").innerHTML = error_message;
    
    // update the sizes 
    var full_size   = 0;
    var javaee_size = 0;
    var javame_size = 0;
    var java_size   = 0;    
    var ruby_size   = 0;
    var cnd_size    = 0;
    

    for (var i = 0; i < product_uids.length; i++) {
        if (!is_compatible(i, platform)) {
            continue;
        }
		
		if (product_properties[i] & PROPERTY_FULL) {
            full_size += new Number(product_download_sizes[i]);
        }
        
		if (product_properties[i] & PROPERTY_JAVAEE) {
            javaee_size += new Number(product_download_sizes[i]);
        }        	
		
		if (product_properties[i] & PROPERTY_JAVAME) {
            javame_size += new Number(product_download_sizes[i]);
        }
		
        if (product_properties[i] & PROPERTY_JAVA) {
            java_size += new Number(product_download_sizes[i]);
        }
		
        if (product_properties[i] & PROPERTY_RUBY) {
            ruby_size += new Number(product_download_sizes[i]);
        }
        
		if (product_properties[i] & PROPERTY_CND) {
            cnd_size += new Number(product_download_sizes[i]);
        }        
    }
	
    full_size = Math.ceil(full_size / 1024.0);
    java_size = Math.ceil(java_size / 1024.0);
    javaee_size = Math.ceil(javaee_size / 1024.0);
    javame_size = Math.ceil(javame_size / 1024.0);
    ruby_size = Math.ceil(ruby_size / 1024.0);
    cnd_size = Math.ceil(cnd_size / 1024.0);

    if ((platform == "solaris-x86") || (platform == "solaris-sparc") || (platform == "macosx-ppc") || (platform == "macosx-x86")) {
		javame_size = "--";
    }   

    document.getElementById("full_size").innerHTML = "Free, " + full_size + " MB";
    document.getElementById("javaee_size").innerHTML = "Free, " + javaee_size + " MB";    
    document.getElementById("javame_size").innerHTML = "Free, " + javame_size + " MB";
    document.getElementById("java_size").innerHTML = "Free, " + java_size + " MB";
    document.getElementById("ruby_size").innerHTML = "Free, " + ruby_size + " MB";
    document.getElementById("cnd_size").innerHTML = "Free, " + cnd_size + " MB";
    
    // no Mobility for Solaris and MacOS
    if ((platform == "solaris-x86") || (platform == "solaris-sparc") || (platform == "macosx-ppc") || (platform == "macosx-x86")) {
    	document.getElementById("javame_link").innerHTML = '<img src="img/download_d.gif" style="border: 0;"/><br><a class="bundle_display_name_no">Mobility</a>';
    } else {
    	document.getElementById("javame_link").innerHTML = '<a href="javascript: download(\'mobility\')"><img src="img/download.gif" style="cursor: pointer; border: 0;"/><br/></a><a class="bundle_display_name">Mobility</a>';
    }
}

function is_compatible(index, platform) {
    for (var i = 0; i < product_platforms[index].length; i++) {
        if (product_platforms[index][i] == platform) {
            return true;
        }
    }
    
    return false;
}

function download(option) {
    var select = document.getElementById("platform_select");
    var platform = select.options[select.selectedIndex].value;

    var basename  = "";
	

    //if ((platform == "macosx-x86") || (platform != "macosx-ppc")) {
    //  basename  = "{nb.files.prefix}-{build.number}-";
    //} else {
	basename  = "{nb.files.prefix}-{build.number}-";
    //}

    var file_name = "start.html?" + basename;

    if (option != "all" ) {
    	file_name += option + "-";
    }

    //if ((platform != "macosx-x86") && (platform != "macosx-ppc")) {
    	file_name += platform;
    //}

    if (platform == "windows") {
        file_name += ".exe";
    } else if ((platform == "macosx-x86") || (platform == "macosx-ppc")) {
        file_name += ".tgz";
	//file_name += ".dmg";
    } else {
        file_name += ".sh";
    }

    window.location = file_name;
}
