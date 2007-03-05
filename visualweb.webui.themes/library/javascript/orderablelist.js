function OrderableList(id, moveMessage) { 
    
    // The select element from which selections are made 
    this.list = document.getElementById(id + "_list");

    // The options of the select element from which selections are made 
    this.options = this.list.options;

    this.moveUpButton = document.getElementById(id + "_moveUpButton"); 
    this.moveDownButton = document.getElementById(id + "_moveDownButton"); 
    this.moveTopButton = document.getElementById(id + "_moveTopButton"); 
    this.moveBottomButton = document.getElementById(id + "_moveBottomButton"); 
    this.values = document.getElementById(id + "_list_value"); 

    // The messages

    this.moveMessage = moveMessage; 
    if(this.moveMessage == null) { 
	"Select at least one item to remove"; 
    } 
    
    // attach OrderableList object methods
    this.moveUp = orderablelist_moveUp;
    this.moveDown = orderablelist_moveDown;
    this.moveTop = orderablelist_moveTop;
    this.moveBottom = orderablelist_moveBottom;

    this.updateButtons = orderablelist_updateButtons; 
    this.updateValue = orderablelist_updateValue;
    this.onChange = orderablelist_updateButtons; 
} 

// The original allowed items to be moved on both lists. Surely we
// only sort items on the selected list? 
// This does not work on Mozilla

function orderablelist_moveUp() { 

    var numOptions = this.options.length; 
    
    // If there aren't at least two more selected items, then there is
    // nothing to move 
    if(numOptions < 2) { 
	return; 
    } 

    // Start by examining the first item 
    var index = 0; 

    // We're not going to move the first item. Instead, we will start
    // on the first selected item that is below an unselected
    // item. We identify the first unselected item on the list, and 
    // then we will start on next item after that
    while(this.options[index].selected) { 
	++index; 
	if(index == numOptions) { 
	    // We've reached the last item - no more items below it so
	    // we return
	    return; 
	} 
    } 

    // Start on the item below this one 
    ++index; 

    for(index; index < numOptions; ++index) { 

	if(this.options[index].selected == true) {

	    var curOption = this.options[index]; 
	    // For Mozilla
	    if(this.options.remove == null) { 
		this.options[index] = null; 
	        this.list.add(curOption, this.options[index - 1]); 
	    }
	    // Windows and Opera do 
	    else { 
		this.options.remove(index); 
	        this.options.add(curOption, index - 1); 
	    } 
	    
	    // This is needed for Opera only
	    this.options[index].selected = false; 
	    this.options[index - 1].selected = true; 
	}
    } 
    this.updateValue(); 
    this.updateButtons(); 
}

// The original allowed items to be moved on both lists. Surely we
// only sort items on the selected list? 
// This does not work on Mozilla
function orderablelist_moveTop() { 

    var numOptions = this.options.length; 
    // If there aren't at least two items, there is nothing to move  
    if(numOptions < 2) { 
	return; 
    } 

    // Find the first open spot 
    var openSpot = 0; 
    while (this.options[openSpot].selected) { 
	openSpot++; 
    } 

    // Find the first selected item below it
    var index = openSpot+1; 

    for(index; index < numOptions; ++index) { 

	if(this.options[index].selected == true) {

	    var curOption = this.options[index]; 
	    // For Mozilla
	    if(this.options.remove == null) { 
		this.options[index] = null; 
	        this.list.add(curOption, this.options[openSpot]); 
	    }
	    // Windows and Opera do 
	    else { 
		this.options.remove(index); 
	        this.options.add(curOption, openSpot); 
	    } 

	    // This is needed for Opera only
	    this.options[index].selected = false; 
	    this.options[openSpot].selected = true; 
	    openSpot++; 
	}
    } 
    this.updateValue(); 
    this.updateButtons(); 
} 


// The original allowed items to be moved on both lists. Surely we
// only sort items on the selected list? 
// This does not work on Mozilla
function orderablelist_moveDown() { 

    // Get the last item
    var index = this.options.length - 1; 
    
    // If this number is less than zero, there was nothing on the list
    // and we return
    if(index < 0) { 
	return; 
    } 

    if(!this.options[index].selected) { 
        --index; 
    }
    else {

        // We're not going to move the last item. Instead, we will start
        // on the last selected item that is above an unselected
        // item. We identify the last unselected item before the separator
        // and then we start with the item above that one. 
        while(this.options[index].selected) { 
	    --index; 
	    if(index == 0) { 
	        // We've reached the first item - no item above it so we
	        // return 
	        return; 
	    } 
        } 

        // Start on the item above this one 
        --index; 
    }

    for(index; index > -1; --index) { 
	if(this.options[index].selected == true) {

	    var curOption = this.options[index]; 

	    // For Mozilla
	    if(this.options.remove == null) { 
		this.options[index] = null; 
	        this.list.add(curOption, this.options[index + 1]); 
	    }
	    // Windows and Opera do 
	    else { 
		this.options.remove(index); 
	        this.options.add(curOption, index + 1); 
	    } 
	    
	    // This is needed for Opera only
	    this.options[index].selected = false; 
	    this.options[index + 1].selected = true; 
	}
    } 
    this.updateValue(); 
    this.updateButtons(); 
}

function orderablelist_moveBottom() { 

    var numOptions = this.options.length - 1; 

    // If there aren't at least two items, there is nothing to move  
    if(numOptions < 1) { 
	return; 
    } 

    // Find the last open spot 
    var openSpot = numOptions; 
    while (this.options[openSpot].selected) { 
	openSpot--; 
    } 

    // Find the first selected item above it
    var index = openSpot-1; 

    for(index; index > -1; --index) { 

	if(this.options[index].selected == true) {

	    var curOption = this.options[index]; 
	    // For Mozilla
	    if(this.options.remove == null) { 
		this.options[index] = null; 
	        this.list.add(curOption, this.options[openSpot+1]); 
	    }
	    // Windows and Opera do 
	    else { 
		this.options.remove(index); 
	        this.options.add(curOption, openSpot); 
	    } 

	    // This is needed for Opera only
	    this.options[index].selected = false; 
	    this.options[openSpot].selected = true; 
	    openSpot--; 
	}
    } 
    this.updateValue(); 
    this.updateButtons(); 
} 

function orderablelist_updateButtons() { 


    var numOptions = this.options.length; 
    var selectedIndex = this.options.selectedIndex; 
    var disabled = true; 
    var index; 


    // First, check if move down and move to bottom should be
    // enabled. These buttons should be enabled if and only if at
    // least one of the items are selected and there is at least one
    // open spot below a selected item. 
    if(selectedIndex > -1 && selectedIndex < numOptions -1) { 
	index = selectedIndex+1; 
	while(index < numOptions) { 
	    if(this.options[index].selected == false) {
		disabled = false; 
		break; 
	    }
	    index++;
	}
    } 

    if(this.moveDownButton != null) { 
	if(this.moveDownButton.setDisabled != null) { 
	    this.moveDownButton.setDisabled(disabled); 
	} 
	else { 
	    this.moveDownButton.disabled = disabled; 
	} 
    } 

    if(this.moveBottomButton != null) { 

	if(this.moveBottomButton.setDisabled != null) { 
	    this.moveBottomButton.setDisabled(disabled); 
	} 
	else { 
	    this.moveBottomButton.disabled = disabled; 
	} 
    } 

    // First, check if move up and move to top should be
    // enabled. These buttons should be enabled if and only if at
    // least one of the items is selected and there is at least one
    // open spot above a selected item. 

    disabled = true; 

    if(selectedIndex > -1) { 
	index = numOptions - 1; 
	while(index > 0) { 
	    if(this.options[index].selected) { 
		break; 
	    }
	    index--; 
	}
	index--; 
	while(index > -1) { 
	    if(this.options[index].selected == false) { 
		disabled = false; 
		break; 
	    }
	    index--; 
	}
    } 

    if(this.moveUpButton != null) { 
	if(this.moveUpButton.setDisabled != null) { 
	    this.moveUpButton.setDisabled(disabled); 
	} 
	else { 
	    this.moveUpButton.disabled = disabled; 
	} 
    } 

    if(this.moveTopButton != null) { 
	if(this.moveTopButton.setDisabled != null) { 
	    this.moveTopButton.setDisabled(disabled); 
	} 
	else { 
	    this.moveTopButton.disabled = disabled; 
	} 
    } 

}

function orderablelist_updateValue() { 

    // Remove the options from the select that holds the actual
    // selected values
    while(this.values.length > 0) { 
        this.values.remove(0);
    }

    // Create a new array consisting of the options marked as selected
    // on the official list
    var newOptions = new Array();

    var cntr = 0; 
    var newOption; 

    while(cntr < this.options.length) { 
        newOption = document.createElement("option"); 
	if(this.options[cntr].text != null) { 
	  newOption.text = this.options[cntr].text; 
	} 
	if(this.options[cntr].value != null) { 
	  newOption.value = this.options[cntr].value; 
	} 
	newOption.selected = true; 
        newOptions[newOptions.length] = newOption; 
    	++ cntr;
    }

    cntr = 0; 
    // For Mozilla
    if(this.options.remove == null) { 
        while(cntr < newOptions.length) { 
	  this.values.add(newOptions[cntr], null); 
	    ++cntr;
	}
    }
    // Windows and Opera do 
    else { 
        while(cntr < newOptions.length) { 
  	    this.values.add(newOptions[cntr], cntr); 
	    ++cntr;
	}
    } 
    return true;
} 

