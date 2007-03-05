function AddRemove(id, separator) { 

    // This is the separator used when parsing the all values string 
    // and when constructing the selected values string
    this.separator = separator; 
    
    // The select element from which selections are made 
    this.availableList = document.getElementById(id + "_available");

    // The options of the select element from which selections are made 
    this.availableOptions = this.availableList.options;

    // The select element in which selections are shown 
    this.selectedList = document.getElementById(id + "_selected"); 

    // The options of the select element in which selections are shown 
    this.selectedOptions = this.selectedList.options;

    this.addButton = document.getElementById(id + "_addButton"); 
    this.addAllButton = document.getElementById(id + "_addAllButton"); 
    this.removeButton = document.getElementById(id + "_removeButton"); 
    this.removeAllButton = document.getElementById(id + "_removeAllButton"); 
    this.moveUpButton = document.getElementById(id + "_moveUpButton"); 
    this.moveDownButton = document.getElementById(id + "_moveDownButton"); 
    this.selectedValues = document.getElementById(id + "_list_value"); 

    // Calculate the value indices
    var itemString = document.getElementById(id + "_item_list"); 
    if(itemString != null) { 
	var string = new String(itemString.value); 
	this.allValues = string.split(separator);	
    }
    else { 
	alert("Did not construct value array"); 
	this.allValues = new Array();
    }

    // attach AddRemove object methods
    this.add = addremove_add;
    this.addAll = addremove_addAll;
    this.remove = addremove_remove;
    this.removeAll = addremove_removeAll;
    this.moveUp = addremove_moveUp;
    this.moveDown = addremove_moveDown;
    this.updateButtons = addremove_updateButtons; 
    this.calculateIndex = addremove_calculateIndex; 
    this.moveOption = addremove_moveOption;
    this.updateValue = addremove_updateValue;
    this.allowMultipleAdditions = addremove_allowMultipleAdditions;
    this.availableOnChange = addremove_availableOnChange; 
    this.selectedOnChange = addremove_selectedOnChange; 
} 

function addremove_add() { 

    if(this.availableOptions.selectedIndex == -1) { 
	return;
    } 

    var sort = (this.moveUpButton == null); 
    // deselect everything in the selected list
    this.selectedList.selectedIndex = -1;
    return this.moveOption(this.availableOptions, 
                           this.selectedOptions, 
		           this.selectedList, 
                           sort); 
} 

function addremove_remove() { 

    if(this.selectedOptions.selectedIndex == -1) { 
	return;
    } 

    // deselect everything in the selected list
    this.availableList.selectedIndex = -1;
    return this.moveOption(this.selectedOptions, 
                           this.availableOptions, 
		           this.availableList, 
                           true); 
} 

function addremove_moveOption(moveFromOptions, moveToOptions, 
			      moveToList, sort) { 

    var index = moveFromOptions.selectedIndex; 

    if(index == -1) { 
	return;
    } 

    // keep moving selected items until there aren't any more valid ones
    while(index != -1 && index < moveFromOptions.length - 1) { 

	var lastOption = moveToOptions.length - 1; 
    
	// This is the option we're moving
	var curSelection = moveFromOptions[index];

	// This is the index where we insert the option...
	var insertionIndex = 0; 
	// ...and this is the option at that index
	var insertionOption; 

	// If there are no buttons to move the selected items up or
	// down, then we preserve the sorting order of the available
	// items. We calculate the index of the selected item (based 
	// on the indices assigned when parsing the allValues
	// variable), and then we check each selected item until we
	// reach an item with a higher index. 
	if(sort) { 
	    var itemIndex = this.calculateIndex(curSelection.value); 
	    for(var counter = 0; counter < lastOption + 1; ++counter) { 
		insertionOption = moveToOptions[counter];
		if(itemIndex < this.calculateIndex(insertionOption.value)) {
		    insertionIndex = counter;
		    break;
		} 
	    }
	}
	// If there are buttons to move the options around, then we
	// simply add the new items in the last position
	else { 
	    insertionIndex = lastOption; 
	    insertionOption = moveToOptions[lastOption]; 
	} 

	// To insert the item, Mozilla works different from Windows
	// and Opera. 

	// Case 1: Mozilla
	if(moveFromOptions.remove == null) { 
	    moveToList.add(curSelection, insertionOption); 
	} 
	// Case 2: Windows and Opera
	else { 
	    moveFromOptions.remove(index); 
	    moveToOptions.add(curSelection, insertionIndex); 
	} 
	
	// Make sure the item is selected (this is needed for Opera)
	moveToOptions[insertionIndex].selected = true; 

	// Update the options
	lastOption++; 

	// Get the next selected index. 
	index = moveFromOptions.selectedIndex; 
    } 

    this.updateValue(); 
    this.updateButtons(); 
    return false;
} 

function addremove_addAll() { 
    
    var numOptions = this.availableOptions.length - 1; 
    for(var index = 0; index < numOptions; ++index) { 
	if(this.availableOptions[index].disabled == false) { 
	    this.availableOptions[index].selected = true;
	}
    } 
    return this.add(); 
} 


function addremove_removeAll() { 
    
    var numOptions = this.selectedOptions.length - 1; 
    for(var index = 0; index < numOptions; ++index) { 
	if(this.selectedOptions[index].disabled == false) { 
	    this.selectedOptions[index].selected = true;
	}
    } 
    return this.remove(); 
} 

// The original allowed items to be moved on both lists. Surely we
// only sort items on the selected list? 
// This does not work on Mozilla
function addremove_moveUp() { 

    // We will not move the last item - it's the separator
    var numOptions = this.selectedOptions.length - 1; 
    
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
    while(this.selectedOptions[index].selected) { 
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

	if(this.selectedOptions[index].selected == true) {

	    var curOption = this.selectedOptions[index]; 
	    // For Mozilla
	    if(this.selectedOptions.remove == null) { 

		this.selectedOptions[index] = null; 
	        this.selectedList.add(curOption, 
				      this.selectedOptions[index - 1]); 
	    }
	    // Windows and Opera do 
	    else { 
		this.selectedOptions.remove(index); 
	        this.selectedOptions.add(curOption, index - 1); 
	    } 
	    
	    // This is needed for Opera only
	    this.selectedOptions[index - 1].selected = true; 
	}
    } 
    this.updateValue(); 
    this.updateButtons(); 
    return false; 
}

// The original allowed items to be moved on both lists. Surely we
// only sort items on the selected list? 
// This does not work on Mozilla
function addremove_moveDown() { 

    // Last option is numOption -1. That is the separator and we don't
    // move it. We start by examining the second to last item. 
    var index = this.selectedOptions.length - 2; 
    
    // If this number is less than zero, there was nothing on the list
    // and we return
    if(index < 0) { 
	return; 
    } 

    // We're not going to move the last item. Instead, we will start
    // on the last selected item that is above an unselected
    // item. We identify the last unselected item before the separator
    // and then we start with the item above that one. 
    while(this.selectedOptions[index].selected) { 
	--index; 
	if(index == 0) { 
	    // We've reached the first item - no item above it so we
	    // return 
	    return; 
	} 
    } 

    // Start on the item above this one 
    --index; 

    for(index; index > -1; --index) { 
	if(this.selectedOptions[index].selected == true) {

	    var curOption = this.selectedOptions[index]; 

	    // For Mozilla
	    if(this.selectedOptions.remove == null) { 
		this.selectedOptions[index] = null; 
	        this.selectedList.add(curOption, 
				      this.selectedOptions[index + 1]); 
	    }
	    // Windows and Opera do 
	    else { 
		this.selectedOptions.remove(index); 
	        this.selectedOptions.add(curOption, index + 1); 
	    } 
	    
	    // This is needed for Opera only
	    this.selectedOptions[index + 1].selected = true; 
	}
    } 
    this.updateValue(); 
    this.updateButtons(); 
    return false; 
}

function addremove_updateButtons() { 

    var index = this.availableOptions.selectedIndex; 
    var numOptions = this.availableOptions.length-1; 
    var setting; 

    // The Add button is enabled if there is at least one option
    // to select from and at least one item is selected
    if(this.addButton != null) { 
	setting = numOptions < 1 || index == -1; 
 	if(this.addButton.setDisabled != null) { 
	    this.addButton.setDisabled(setting); 
	} 
	else { 
	    this.addButton.disabled = setting; 
	} 
    } 

    // The Add All button is enabled if there is at least one option
    // to select from, and disabled otherwise
    if(this.addAllButton != null) { 

        var counter = 0; 
        // If available item list is disabled then AddAll button should be disabled 
        // irrespective of options element in list.
        if(this.availableList.disabled == false ) {
            for(index = 0; index < numOptions; ++index) { 
                if(this.availableOptions[index].disabled == false) { 
                    ++counter; 
                }
            } 
        }
	setting = (counter < 1); 
 	if(this.addAllButton.setDisabled != null) { 
	    this.addAllButton.setDisabled(setting); 
	} 
	else { 
	    this.addAllButton.disabled = setting; 
	} 
    } 

    // The remaining buttons are enabled/disabled based on the 
    // items on the selected list

    index = this.selectedOptions.selectedIndex; 
    numOptions = this.selectedOptions.length - 1; 

    if(this.removeAllButton != null) { 

        var counter = 0; 
        // If selected item list is disabled then RemoveAll button should be disabled 
        // irrespective of options element in list. 
        if(this.selectedList.disabled == false) {
            for(index = 0; index < numOptions; ++index) { 
	            if(this.selectedOptions[index].disabled == false) { 
	                ++counter; 
	        }
            } 
        }
	setting = (counter < 1); 
	if(this.removeAllButton.setDisabled != null) { 
	    this.removeAllButton.setDisabled(setting); 
	} 
	else { 
	    this.removeAllButton.disabled = setting; 
	} 
    } 

    // If there are no selected items or if none of them are selected,
    // we disable Remove, Move Up, Move Down
    index = this.selectedOptions.selectedIndex; 
    var noItems = numOptions < 1 || index == -1; 
    if(this.removeButton != null) { 
 	if(this.removeButton.setDisabled != null) { 
	    this.removeButton.setDisabled(noItems); 
	} 
	else { 
	    this.removeButton.disabled = noItems; 
	} 
    } 

    // The Move Up button is enabled (setting = false) provided that
    // there is at least one selected item that is below an unselected
    // item 
    if(this.moveUpButton != null) { 

        setting = true; 

	if(noItems != true) { 

	  // Find the first un-selected option, then see if there is
	  // a selected option below that one
	  var found = false; 
	  var unselected = -1; 
	  for(index = 0; index < numOptions; ++index) { 
	      if(unselected == -1) { 
		  if(this.selectedOptions[index].selected == false) { 
		    unselected = index; 
		  }
	      } 
	      else { 
		  if(this.selectedOptions[index].selected == true) { 
		      setting = false; 
		      break; 
		  }
	      }
	  }
	}
	if(this.moveUpButton.setDisabled != null) { 
	    this.moveUpButton.setDisabled(setting); 
	} 
	else { 
	    this.moveUpButton.disabled = setting; 
	} 
    } 

    // The Move Down button is enabled (setting = false) provided that
    // there is at least one unselected item below a selected item.
    if(this.moveDownButton != null) { 

        setting = true; 

        if(noItems != true) { 
	     
	    for(index = this.selectedOptions.selectedIndex; 
		index < numOptions; 
		++index) { 
	        if(this.selectedOptions[index].selected == false) { 
		    setting = false; 
		}
	    } 
	}
	if(this.moveDownButton.setDisabled != null) { 
	    this.moveDownButton.setDisabled(setting); 
	} 
	else { 
	    this.moveDownButton.disabled = setting; 
	} 
    } 
    return false; 
}

function addremove_calculateIndex(value, lastIndex) { 

    var string = new String(value); 
    for(var counter=0; counter < this.allValues.length; counter++) { 
	if(string == this.allValues[counter]) { 
	    return counter; 
	} 
    } 
    // Something went wrong. Return the index before the separator 
    return this.allValues.length - 2; 
} 

function addremove_updateValue() { 

    // Remove the options from the select that holds the actual
    // selected values
    while(this.selectedValues.length > 0) { 
        this.selectedValues.remove(0);
    }

    // Create a new array consisting of the options marked as selected
    // on the official list
    var newOptions = new Array();

    var cntr = 0; 
    var newOption; 

    while(cntr < this.selectedOptions.length-1) { 
        newOption = document.createElement("option"); 
	if(this.selectedOptions[cntr].text != null) { 
	  newOption.text = this.selectedOptions[cntr].text; 
	} 
	if(this.selectedOptions[cntr].value != null) { 
	  newOption.value = this.selectedOptions[cntr].value; 
	} 
	newOption.selected = true; 
        newOptions[newOptions.length] = newOption; 
    	++ cntr;
    }

    cntr = 0; 
    // For Mozilla
    if(this.selectedOptions.remove == null) { 
        while(cntr < newOptions.length) { 
	  this.selectedValues.add(newOptions[cntr], null); 
	    ++cntr;
	}
    }
    // Windows and Opera do 
    else { 
        while(cntr < newOptions.length) { 
  	    this.selectedValues.add(newOptions[cntr], cntr); 
	    ++cntr;
	}
    } 
    return true;
}

function addremove_allowMultipleAdditions() { 

    // Replace the add and remove functions with functions which 
    // leave the available items as they are
    this.add = addremove_multipleAdd; 
    this.remove = addremove_multipleRemove; 
} 

function addremove_multipleAdd() { 

    this.selectedList.selectedIndex = -1;
    var index = this.availableOptions.selectedIndex; 

    if(index == -1) { 
	return;
    } 
    
    // keep moving selected items until there aren't any more valid ones
    while(index != -1 && index < this.availableOptions.length - 1) { 

	var lastOption = this.selectedOptions.length - 1; 

	// This is the option we're moving
	var curSelection = this.availableOptions[index];
	curSelection.selected = false; 
	var addSelection = new Option(); 
	addSelection.text = curSelection.text;
	addSelection.value = curSelection.value; 

	// This is the index where we insert the option...
	var insertionIndex = 0; 
	// ...and this is the option at that index
	var insertionOption; 

	// If there are no buttons to move the selected items up or
	// down, then we preserve the sorting order of the available
	// items. We calculate the index of the selected item (based 
	// on the indices assigned when parsing the allValues
	// variable), and then we check each selected item until we
	// reach an item with a higher index. 

	// We sort if there are no move buttons
	var sort = (this.moveUpButton == null); 

	if(sort) { 
	    var itemIndex = this.calculateIndex(curSelection.value); 
	    for(var counter = 0; counter < lastOption + 1; ++counter) { 
		insertionOption = this.selectedOptions[counter];
		if(itemIndex < this.calculateIndex(insertionOption.value)) {
		    insertionIndex = counter;
		    break;
		} 
	    }
	}
	// If there are buttons to move the options around, then we
	// simply add the new items in the last position
	else { 
	    insertionIndex = lastOption; 
	    insertionOption = this.selectedOptions[lastOption]; 
	} 

	// To insert the item, Mozilla works different from Windows
	// and Opera. 

	// Case 1: Mozilla
	if(this.selectedOptions.remove == null) { 
	    this.selectedList.add(addSelection, insertionOption); 
	} 
	// Case 2: Windows and Opera
	else { 
	    this.selectedOptions.add(addSelection, insertionIndex); 
	} 
	
	// Make sure the item is selected (this is needed for Opera)
	this.selectedOptions[insertionIndex].selected = true; 

	// Update the options
	lastOption++; 

	// Get the next selected index. 
	index = this.availableOptions.selectedIndex; 
    } 

    this.updateValue(); 
    this.updateButtons(); 
    return false; 
} 

function addremove_multipleRemove() { 

    this.availableList.selectedIndex = -1;

    var index = this.selectedOptions.selectedIndex; 

    if(index == -1) { 
	return;
    } 
    
    //  <RAVE>
    //  for(index; index < this.selectedOptions.length - 1; ++index) {
    //      if(this.selectedOptions[index].selected) {
    //          // Case 1: Mozilla
    //          if(this.selectedOptions.remove == null) {
    //             this.selectedOptions[index] = null;
    //          }
    //          // Case 2: Windows and Opera
    //          else {
    //             this.selectedOptions.remove(index);
    //          } 
    //      } 
    //  }     
    while(index < this.selectedOptions.length - 1) {
        if(this.selectedOptions[index].selected) { 
        // Case 1: Mozilla
            if(this.selectedOptions.remove == null) {
                this.selectedOptions[index] = null;
            } 
        // Case 2: Windows and Opera
            else {
                this.selectedOptions.remove(index);
            } 
        } else {
            index++;
        } 
    } 
    // </RAVE>


    this.updateValue(); 
    this.updateButtons(); 
    return false; 
} 

function addremove_availableOnChange() { 
    this.selectedList.selectedIndex = -1; 
    this.updateButtons(); 
    return false; 
} 

function addremove_selectedOnChange() { 
    this.availableList.selectedIndex = -1; 
    this.updateButtons(); 
    return false; 
} 
