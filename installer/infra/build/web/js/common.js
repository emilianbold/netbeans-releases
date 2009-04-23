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
var LANGUAGES   = new Array();

var PLATFORM_IDS         = new Array();
var PLATFORM_LONG_NAMES  = new Array();
var PLATFORM_SHORT_NAMES = new Array();

var BUNDLE_IDS   = new Array();
var BUNDLE_LONG_NAMES = new Array();
var BUNDLE_SHORT_NAMES = new Array();

var FILES = new Array();
var FILES_SET_NUMBER = 0;

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

BUNDLE_IDS [0] = "javase";
BUNDLE_IDS [1] = "java";
BUNDLE_IDS [2] = "ruby";
BUNDLE_IDS [3] = "cpp";
BUNDLE_IDS [4] = "php";
BUNDLE_IDS [5] = "all";

var DEFAULT_LANGUAGE = "DEFAULT";
var PAGELANG_SEP = "pagelang=";

var OMNITURE_CODE_JS = "http://www.netbeans.org/images/js/s_code_remote.js";
var GOOGLE_ANALYTICS_JS = "http://www.google-analytics.com/ga.js";

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
    var ids = new Array();
    var names = new Array();
    for(var i=0;i<LANGUAGES.length;i++) {
        ids.push(LANGUAGES[i].id);
        names.push(LANGUAGES[i].name);
    }
    return getNameById(id, ids, names);
}

function getBundleShortName(id) {
    return getNameById(id, BUNDLE_IDS, BUNDLE_SHORT_NAMES);
}
function getBundleLongName(id) {
    return getNameById(id, BUNDLE_IDS, BUNDLE_LONG_NAMES);
}

function get_overridden_language() {
    var url = "" + window.location;
    var idx = url.indexOf(PAGELANG_SEP);
    var langcode = DEFAULT_LANGUAGE;
    if(idx != -1) {
	langcode = url.substring(idx + PAGELANG_SEP.length, url.length);
    }
    return langcode;
    
}


function get_language_id(variants) {
    return get_language(variants, 0);
}

function get_language_suffix(variants) {
    return get_language(variants, 1);
}

function get_language(variants, option) {
    var resultLanguage = "";
    if(variants) {
        var lang = variants[0].id;

        var override = get_overridden_language();

        if (override != DEFAULT_LANGUAGE) lang = override;
        else if(navigator.userLanguage)  lang = navigator.userLanguage;
        else if(navigator.language) lang = navigator.language;
        lang = lang.replace("-", "_");        
        for(var i=0; i < variants.length; i++ ) {
            var value = (option == 0 ) ? variants[i].id : variants[i].suffix;
            if(value && lang.toLowerCase().indexOf(value.toLowerCase())!=-1) {
                if(value.length > resultLanguage.length) {
                    resultLanguage = value;		
                }
            }
        }    
    }
    return resultLanguage;
}

function load_js(script_filename) {
    document.write('<script language="javascript" type="text/javascript" src="' + script_filename + '"></script>');
} 

function load_page_js_locale(name,locale) {
    load_js_locale(JS_LOCATION + name, locale);
}

function load_js_locale(script_filename, extension) {  
    var suffix = "";
    var locale_suffix = "";
    locale_suffix = get_language_suffix(LANGUAGES);
    if(locale_suffix!="") {
	suffix = "_" + locale_suffix;
    }
     
    load_js(script_filename + suffix + extension);
}

function load_page_img(img,add) {
    if(add) {
        document.write('<img src="' + IMG_LOCATION + img + '" ' + add + '/>');
    } else {
        document.write('<img src="' + IMG_LOCATION + img + '"/>');
    }
}
function load_page_css(css) {
    document.write('<link rel="stylesheet" type="text/css" href="' + CSS_LOCATION + css + '" media="screen"/>');
}

function other_webpage_langs_available() {
    for(var i=0 ; i < LANGUAGES.length; i++) {
        if ( LANGUAGES[i].suffix && LANGUAGES[i].suffix.length > 0) {
            return true;
        }
    }    
    return false;
}

function write_page_languages() {    
    var locale_suffix = get_language_suffix(LANGUAGES);

    if(other_webpage_langs_available()) {
        document.getElementById("pagelanguagesbox").style.visibility = 'visible';
    }
    var url = "" + window.location;
    var qIndex = url.indexOf("?")!=-1 ? url.indexOf("?") : url.length;
    var aIndex = url.indexOf("&")!=-1 ? url.indexOf("&") : url.length;
    var page = url.substring(0, Math.min(qIndex, aIndex));
    
    var get_request = url.substring(url.indexOf(page) + page.length, url.length);
    if(get_request.indexOf(PAGELANG_SEP)==-1) { 
        if(get_request.indexOf("?")==-1) {
            get_request += "?";
        } else if(get_request.indexOf("&")) {
            get_request += "&";
        } 
        get_request += PAGELANG_SEP;
    } else {
        var regexp =  new RegExp(PAGELANG_SEP + "[a-zA-Z]+(_[a-zA-Z]+){0,2}","g");
	get_request = get_request.replace(regexp, PAGELANG_SEP);
    }
    for(var i=0;i<LANGUAGES.length;i++) {
	if(LANGUAGES[i].suffix && locale_suffix!=LANGUAGES[i].suffix) {
            document.write('<li><a href="' + page + get_request.replace(PAGELANG_SEP, PAGELANG_SEP + LANGUAGES[i].suffix) + '">' + LANGUAGES[i].webpagename + '</a></li>');
        }
    }
}

function startList() {
    // source: http://www.netbeans.org/branding/scripts/lang-pulldown.js
    if (document.all&&document.getElementById) {
        navRoot = document.getElementById("nav");
        if (navRoot!=null) { //if the language panel is active
            for (i=0; i<navRoot.childNodes.length; i++) {
                node = navRoot.childNodes[i];
                if (node.nodeName=="LI") {
                    node.onmouseover=function() {
                        this.className+=" over";
                    }
                    node.onmouseout=function() {
                        this.className=this.className.replace(" over", "");
                    }
                }	
	    }
	}
    }
}

function get_file_list(dir) {	
	lst = new Array();
	if(FILES.length > 0) {
            for (var i = 0; i < FILES.length; i++) {		
		if(FILES[i].name.indexOf(dir)==0) {
			var stripped = FILES[i].name.substring(dir.length, FILES[i].name.length);
			if(stripped.indexOf('/')==-1) {
			    lst[lst.length] = stripped;
			}
		}
            }
	}
	return lst;
}

function getSize(filename) {
	var size = "";
	if(FILES.length > 0) {
            for (var i = 0; i < FILES.length; i++) {		
		if(FILES[i].name == filename) {		
			size = FILES[i].size;
			break;
		}
            }
	}
	return size;
}

function get_file_name(platform, option) {
    var fn = "";
    if(platform=="zip") {
        fn += "zip/";
    } else {
        fn += "bundles/";
    }
    return fn + get_file_name_short(platform, option);
}


function get_file_name_short(platform, option) {
    var file_name = "";
    if(platform=="zip") {
        if(option == "javadoc" || option == "platform-src" || option == "src") {
            file_name += ZIP_FILES_PREFIX.replace("-ml","");
        } else {
            file_name += ZIP_FILES_PREFIX;
        }
    } else {
        file_name += BUNDLE_FILES_PREFIX;
    }
    if (option != "all") {
    	file_name += "-" + option;
    }

    if ( platform != "zip" ) {
   	file_name += "-" + platform;
    }
    if (platform == "windows") {
        file_name += ".exe";
    } else if ((platform == "macosx-x86") || (platform == "macosx-ppc")) {
        file_name += ".tgz";
    } else if (platform == "macosx") {
	file_name += ".dmg";
    } else if(platform == "zip"){
	file_name += ".zip"        
    } else {
        file_name += ".sh";
    }
    return file_name;
}

function get_file_url(filename) {
    var url  = BUILD_LOCATION;
    url += filename;    
    return url;
}


function get_file_bouncer_url(platform, option) {
    var url = BOUNCER_URL;
    url += "?" + "product=" + BOUNCER_PRODUCT_PREFIX;
    if(option != "all") {
        url += "-" + option;
    }
    url += "&" + "os=" + platform;
    return url;
}

function message(msg) {
    document.write(msg);
}
function writeUrl(url,msg) {
    document.write('<a href="' + url + '">' + msg + '</a>');
}
function set_page_title(title) {
    document.title = title;
    var titleElement = document.getElementsByTagName("title");
    if(titleElement.length == 1) {
        titleElement[0].innerHTML = title;       
    } else {
        document.write('<title>' + title + '</title>');
    }
}

function set_page_description(desc) {
    document.write('<meta name="description" content="' + desc + '"/>');
}

function add_file(name, size, md5, locales) {
    var index = FILES.length;
    FILES[index] = new Object;
    FILES[index].name = name;
    FILES[index].size = size;
    FILES[index].md5  = md5;
    FILES[index].set  = FILES_SET_NUMBER;
    FILES[index].locales = locales;
}

function load_files_information(name) {
    load_page_js(name);
    FILES_SET_NUMBER++;
}

function add_language(id, name, suffix, webpagename) {
    var index = LANGUAGES.length;
    LANGUAGES[index] = new Object;
    LANGUAGES[index].name        = name;
    LANGUAGES[index].id          = id;
    if(suffix) 
       LANGUAGES[index].suffix      = suffix;
    if(webpagename) 
       LANGUAGES[index].webpagename = webpagename;
}
