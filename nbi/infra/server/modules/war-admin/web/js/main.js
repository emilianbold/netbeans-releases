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

function show_form_registry() {
    document.forms["Form"].registry.value = "";
    document.getElementById("form-registry").style.display = "block";
}

function close_form_registry() {
    document.getElementById("form-registry").style.display = "none";
    
    update_current_registry();
}

function show_form_codebase() {
    document.forms["Form"].codebase.value = "";
    document.getElementById("form-codebase").style.display = "block";
}

function close_form_registry() {
    document.getElementById("form-codebase").style.display = "none";
}

function show_form_archive() {
    document.getElementById("form-archive").style.display = "block";
}

function close_form_archive() {
    document.getElementById("form-archive").style.display = "none";
}

function add_registry() {
    document.forms["Form"].action = "add-registry";

    show_form_registry();
}

function remove_registry() {
    var select = document.getElementById("registries-select");
    var registry = select.options[select.selectedIndex].value;

    document.forms["Form"].registry.value = registry;
    document.forms["Form"].action = "remove-registry";
    document.forms["Form"].submit();
}

function update_engine() {
    document.forms["Form"].action = "update-engine";

    show_form_archive();
}

function update_current_registry() {
    var platformsSelect  = document.getElementById("platforms-select");
    var registriesSelect = document.getElementById("registries-select");
    
    if ((platformsSelect != null) && (registriesSelect != null)) {
        var platform = platformsSelect.options[platformsSelect.selectedIndex].value;
        var registry = registriesSelect.options[registriesSelect.selectedIndex].value;
        
        document.forms["Form"].registry.value = registry;
        
        for (i = 0; i < registriesSelect.options.length; i++) {
            var value = registriesSelect.options[i].value;
            document.getElementById("registry-" + value).style.display = "none";
        }
        
        document.getElementById("registry-" + registry).style.display = "block";
        
        document.forms["Form"].fallback.value = document.forms["Form"].fallback_base.value + "?registry=" + registry + "&platform=" + platform;
    } else {
        document.forms["Form"].fallback.value = document.forms["Form"].fallback_base.value;
    }
}

function update_target_platform() {
    var platformsSelect  = document.getElementById("platforms-select");
    var registriesSelect = document.getElementById("registries-select");
    
    var platform = platformsSelect.options[platformsSelect.selectedIndex].value;
    var registry = registriesSelect.options[registriesSelect.selectedIndex].value;
    
    window.location = document.forms["Form"].fallback_base.value + "?registry=" + registry + "&platform=" + platform;
}

function remove_component(uid, version, platforms) {
    var action;
    if (version == "null") {
        action = "remove-group";
    } else {
        action = "remove-product";
    }
    
    document.forms["Form"].action          = action;
    document.forms["Form"].uid.value       = uid;
    document.forms["Form"].version.value   = version;
    document.forms["Form"].platforms.value = platforms;
    document.forms["Form"].submit();
}

function add_package(uid, version, platforms) {
    document.forms["Form"].action          = "add-package";
    document.forms["Form"].uid.value       = uid;
    document.forms["Form"].version.value   = version;
    document.forms["Form"].platforms.value = platforms;
    
    show_form_archive();
}

function delete_bundles() {
    var select = document.getElementById("registries-select");
    var registry = select.options[select.selectedIndex].value;
    
    document.forms["Form"].registry.value = registry;
    document.forms["Form"].action = "delete-bundles";
    document.forms["Form"].submit();
}

function generate_bundles() {
    var select = document.getElementById("registries-select");
    var registry = select.options[select.selectedIndex].value;
    
    document.forms["Form"].registry.value = registry;
    document.forms["Form"].action = "generate-bundles";
    document.forms["Form"].submit();
}

function export_registry() {
    var select = document.getElementById("registries-select");
    var registry = select.options[select.selectedIndex].value;
    
    document.forms["Form"].registry.value = registry;
    document.forms["Form"].action = "export-registry";
    
    show_form_codebase();
}

function _expand(id) {
    var row = document.getElementById(id);
    
    if (row.style.display == "none") {
        row.style.display = "table-row";
    } else {
        row.style.display = "none";
    }
}
