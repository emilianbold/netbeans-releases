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
