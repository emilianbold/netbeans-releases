function openclose(id) {
    var row = document.getElementById(id);
    
    if (row.style.display == "none") {
        row.style.display = "table-row";
    } else {
        row.style.display = "none";
    }
}

function remove(registry, uid, version) {
    var form = document.forms["Form"];
    
    if (version == "null") {
        form.action = "remove-group";
    } else {
        form.action = "remove-component";
    }
    
    setFormValues(form, registry, uid, version);
    
    form.submit();
}

function addComponent(registry, uid, version) {
    var form = document.forms["Form"];
    var div  = document.getElementById("form-div");
    
    form.action = "update-component";
    
    setFormValues(form, registry, uid, version);
    
    div.style.display = "block";
}

function addGroup(registry, uid, version) {
    var form = document.forms["Form"];
    var div  = document.getElementById("form-div");
    
    form.action = "update-group";
    
    setFormValues(form, registry, uid, version);
    
    div.style.display = "block";
}

function setFormValues(form, registry, uid, version) {
    form.registry.value = registry;
    form.uid.value      = uid;
    form.version.value  = version;
}