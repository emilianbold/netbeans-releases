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

function install_now() {
    document.forms.Form.action = "install";
    document.forms.Form.submit();
}

function create_bundle() {
    document.forms.Form.action = "create-bundle";
    document.forms.Form.submit();
}

function update_target_platform() {
    var platformsSelect  = document.getElementById("platforms-select");
    
    var platform = platformsSelect.options[platformsSelect.selectedIndex].value;
    
    window.location = "create-bundle?platform=" + platform + "&" + document.forms.Form.registries.value;
}

function _expand(id) {
    var row = document.getElementById(id);
    
    if (row.style.display == "none") {
        row.style.display = "table-row";
    } else {
        row.style.display = "none";
    }
}
