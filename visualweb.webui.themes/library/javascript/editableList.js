
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// editable list functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


function EditableList(id) { 

    // child elements
    this.list = document.getElementById(id + "_list");
    this.field = document.getElementById(id + "_field");
    this.addButton = document.getElementById(id + "_addButton"); 
    this.removeButton = document.getElementById(id + "_removeButton"); 
    
    // attach methods
    this.add = editableList_add;
    this.enableAdd = editableList_enableAdd;
    this.enableRemove = editableList_enableRemove;
    this.setAddDisabled = editableList_setAddDisabled;
    this.setRemoveDisabled = editableList_setRemoveDisabled; 

    this.updateButtons = editableList_updateButtons;
    this.setDisabled = editableList_setDisabled;
} 

function editableList_add(elementId) { 
    this.enableAdd(); 
    this.addButton.click();
}

function editableList_enableAdd() { 
    var disabled = (this.field.value == ""); 
    this.setAddDisabled(disabled); 
}

function editableList_setAddDisabled(disabled) { 
    if(this.addButton.setDisabled != null) { 
        this.addButton.setDisabled(disabled); 
    } 
    else { 
        this.addButton.disabled = disabled; 
    } 
}

function editableList_enableRemove() { 
    var disabled = (this.list.selectedIndex == -1); 
    this.setRemoveDisabled(disabled); 
} 

function editableList_setRemoveDisabled(disabled) { 
    if(this.removeButton.setDisabled != null) { 
        this.removeButton.setDisabled(disabled); 
    } 
    else { 
        this.removeButton.disabled = disabled; 
    } 
}

function editableList_updateButtons() { 
    this.enableAdd(); 
    this.enableRemove(); 
}

function editableList_setDisabled(disabled) { 

    if(this.addButton.setDisabled != null) { 
        this.addButton.setDisabled(disabled); 
    } 
    else { 
        this.addButton.disabled = disabled; 
    } 
    if(this.removeButton.setDisabled != null) { 
        this.removeButton.setDisabled(disabled); 
    } 
    else { 
        this.removeButton.disabled = disabled; 
    } 
    this.field.disabled = disabled; 
    this.list.disabled = disabled; 
}

