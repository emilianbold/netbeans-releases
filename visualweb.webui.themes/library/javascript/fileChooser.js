/* functions to handle filechooser interactions */

// The filechooser has several intermediate actions in addition to 
// the selection of one or more files of folders.
// There are actions that are initiated by buttons 
// and actions initiated by mouse clicks and key presses.
// Some events generated from key presses and mouse clicks
// behave like accelerators for the button actions.
//
// Some actions are client side only actions, such as placing a
// selected file or folder into the selected file or folder field.
//
// The server side events and how they are generated.
//
// 1. moveup - the moveup button is clicked
// 2. openfolder - select a folder from the list and the openfolder
//    button is clicked or press the return key with the selection in
//    focus or double click the selection
// 3. sort - a sort selection is made from the sort dropdown menu
// 4. refresh the list - set focus in the look in field and press the return key
// 5. change the directory listing - type a new value in the look in field
//    and press the return key
//    The directory listing is also changed when a folder is typed into
//    the selected file field, and submitted.
// 6. filter the list - the focus is placed in the filter field and the
//    return key is pressed
// 7. change filter value - a new value is entered into the filter field 
//    and the return key is pressed
// 8. commit the changes - the button assigned as the chooseButton is
//    is clicked, or programmtically clicked when the return key is 
//    pressed in the select file field or a file selection is double clicked.
//
// 
// Mouse clicks and return key activations.
//
// - The mouse clicks and key presses that correspond to actions are
//   exectuted by "clicking" the corresponding button action programmatically.
//   For example, action #2 when activated by double clicking or the
//   return key, programmatically clicks the open folder button.
//
// - Action #4 and #6 explcitly submit the form
//
// - Action #8 programmatically clicks the assigned chooserButton.
//
// Selections are made and submitted in the following ways
//
//   File chooser or folder chooser
//
//   - One or more absolute or relative paths are typed or placed
//   into the select file or folder field the return key is pressed.
//
//   - An external submit button submits the form and there are selections
//   in the selected file or folder field.
//
//   File chooser 
//
//   - A file selection is double clicked.
//
// Client side selections
// 
// - A file or folder is single clicked and it is entered into the
//   selected file or folders field, depending on whether the chooser
//   is a file or folder chooser.
//
// - When a directory is selected and the open folder button is clicked
//   the entry is set as the look in field value and the form is submitted.
// 
// - When the move up button is clicked the parent directory is placed
//   into the look in field and the form is submitted.
//

// FIXME: Note that the dependence on literal client id's is not sufficient
// if these components are developer defined facets. The actual
// literal id's cannot be guaranteed.
//
function FileChooser(clientid, chooserType, parentFolder, separatorChar, escapeChar, delimiter) {

    // process arguments
    //
    this.escapeChar = escapeChar;
    this.separatorChar = separatorChar;
    this.delimiter = delimiter;
    this.parentFolder = parentFolder;

    // boolean identifying the chooser mode.
    //
    this.folderChooser = (chooserType == "folderChooser");
    this.fileAndFolderChooser = (chooserType == "fileAndFolderChooser");
    this.chooseButton = null;

    // FIXME: This encoding needs to be generalized if this code is to
    // become more generic chooser-like.
    // In fact encoding entries this way in not ideal.
    // A more explicit typing needs to be developed if it is 
    // necessary, possible a data structure that maps type to entry.
    //
    if (this.folderChooser) {
	this.chooser = 'folder';
    } else {
	this.chooser = 'file';
    }

    // This is not a user defined facet. It is created dynamically
    // if the enter key is pressed in the selected file field.
    // This is the expected form of the request paramter name
    // in the renderers decode method.
    //
    // Consider passing the name as a parameter, or some other
    // more well defined manner.
    //
    this.selectionsId = clientid + "_selections";

    this.lookinfield = field_getInputElement(clientid + "_lookinField");
    this.filterfield = field_getInputElement(clientid + "_filterField");
    this.selectedfield = field_getInputElement(clientid + "_selectedField");

    this.upButton = document.getElementById(clientid + "_upButton");

    this.openFolderButton = document.getElementById(clientid + "_openButton");

    this.listentries = listbox_getSelectElement(clientid + "_listEntries");
    this.listOptions = this.listentries.options;

    //this.fchiddenfield = field_getInputElement(clientid + "_hiddenField");
    //this.hiddenButton = document.getElementById(clientid + "_hiddenButton");

    // this.sortmenu = document.getElementById(clientid + "_sortMenu_list");
    this.sortmenu = dropDown_getSelectElement(clientid + "_sortMenu");

    // Define the methods.
    //

    this.enterKeyPressed = _enterKeyPressed;
    this.handleDblClick = _handleDblClick;
    this.handleOnChange = _handleOnChange;
    this.openFolderClicked = _openFolderClicked;
    this.moveUpButtonClicked = _moveUpButtonClicked;
    this.setChooseButton = _setChooseButton;

    this.getCurrentDirectory = _getCurrentDirectory;
    this.getOptionElements = _getOptionElements;
    this.getSelectedOptions = _getSelectedOptions;

    this.isFolderChooser = _isFolderChooser;
    this.isFolderSelected = _isFolderSelected;
    this.getSelectionValue = _getSelectionValue;
    this.getSelectionValueByIndex = _getSelectionValueByIndex;
    this.getSelectionType = _getSelectionType;
    this.getSelectionTypeByIndex = _getSelectionTypeByIndex;
    this.getValueType = _getValueType;

    this.itemSelected = _itemSelected;
    this.getSelectedFolders = _getSelectedFolders;
    this.getSelectedFiles = _getSelectedFiles;
    this.getSelectedValuesByType = _getSelectedValuesByType;
    this.setSelectedFieldValue = _setSelectedFieldValue;
    this.clearSelections = _clearSelections;
    this.deselectFolders = _deselectFolders;
    this.deselectSelectionsByType = _deselectSelectionsByType;
    this.setSelected = _setSelected;
    this.clearSelectedField = _clearSelectedField;
    this.armChooseButton = _armChooseButton;
    this.getFileNameOnly = _getFileNameOnly;


    this.setChooseButtonDisabled = _setChooseButtonDisabled;

    // For supporting valid entries in look in field and filter field.
    //
    // It is imperative that the look in field and filter field
    // are never submitted if the value does not imply a valid action.
    // Not currently used.
    //
    this.onFocus = _onFocus;
    this.onBlur = _onBlur;

    this.lookinCommitted = false;
    this.lastLookInValue = null;
    this.filterCommitted = false;
    this.lastFilterValue = null;

    // Define method within the prototype defintion to place the
    // names only within the scope of the prototype and because
    // they can only be referenced through a FileChooser prototype
    // instance, since they reference "this".
    // 

    /**
     * Handler for enter key presses.
     * - Enter key in LookInField
     * - Enter key in FilterField
     * - Enter key in SelectedFileField
     * - Enter key in Listbox with folder selection.
     * Submit the chooser from the various mouse clicks
     * key presses.
     * Handles doubleclick on a file selection in the list box.
     * This is equivalent to an enter key press in the selected file field.
     */
    function _enterKeyPressed(element) {

	// Return pressed in the list
	//
	if (element.id == this.listentries.id) {

	    // If the selected item is a folder call the click method
	    // on the openFolderButton
	    //
	    if (this.isFolderSelected()) {
		// Done in openFolderClicked
		//
		//this.lookinfield.value = this.getSelectionValue();
		this.openFolderButton.click();
	    }
	    return false;
	}

	// The FileChooser's value must only change when selections
	// have been made, not from just intermediate operations.
	//
	// Enter key pressed in the selectedFileField
	// or dbl click in the list.
	//
	if (this.selectedfield && element.id == this.selectedfield.id) {
	    var escapedSelections = this.selectedfield.value;

	    var selections = common_unescapeStrings(escapedSelections,
		    this.delimiter, this.escapeChar);

	    // If a choose button has been defined call its click method
	    // otherwise do nothing. This behavior allows the filechooser
	    // to behave like any other component on a page.
	    //
	    if (this.chooseButton) {
		// Make sure its enabled.
		//
		this.chooseButton.setDisabled(false);
		this.chooseButton.click();
	    }
	    return false;
	}

	// Enter key pressed in the filter field
	// Call the open folder button's click method.
	// Since there is no JSF action for mouse clicks or key presses
	// overload the open folder action to ensure that the 
	// sort value is updated.
	//
	if (element.id == this.filterfield.id) {
	    this.filterCommitted = true;
	    this.lastFilterValue = this.filterfield.value;
	    this.clearSelections();
	    element.form.submit();
	    return false;
	}

	// Enter key pressed in the LookIn field.
	// Call the open folder button's click method.
	// Since there is no JSF action for mouse clicks or key presses
	// overload the open folder action to ensure that the 
	// look in value is updated. This is needed anyway to display
	// the new folder's content.
	//
	if (element.id == this.lookinfield.id) {

	    this.lookinCommitted = true;
	    this.lastLookInValue = this.lookinfield.value;
	    this.clearSelections();
	    element.form.submit();
	    return false;
	}
	return false;
    }

    /**
     * In file chooser mode
     *    - a file selection, call enterKeyPressed with selected file field
     *    - a folder selection, call open folder click handler
     * In folder chooser mode
     *    - a folder selection, call open folder click handler
     */
    function _handleDblClick() {

	// Nothing selected. Not sure if this can happen since
	// doubleclick implies selection.
	//
	if (!this.itemSelected()) {
	    return false; 
	}

	var fldrSelected = this.isFolderSelected();

	// If the selected item is a folder call the click method
	// on the openFolderButton
	//
	if (fldrSelected) {
	    // Set the look in field, since the selected folder will be
	    // the new look in field value. Done in openFolderClicked.
	    // This only works now because values are full paths.
	    //
	    this.openFolderButton.click();
	    return true;
	}

	// doubleclick is not valid for file selections in a
	// folder chooser.
	// If a file chooser, this is equivalent to a return key
	// in the selected file field.
	//
	if (this.isFolderChooser()) {
	    if (!fldrSelected) {
		return false;
	    }
	} else {
	    // file chooser
	    // double click a file in file chooser mode
	    //
	    if (!fldrSelected) {
		if (this.selectedfield) {
		    return this.enterKeyPressed(this.selectedfield);
		}

		// If a choose button has been defined call its click method
		// otherwise do nothing. This behavior allows the filechooser
		// to behave like any other component on a page.
		//
		if (this.chooseButton) {
		    // Make sure its enabled.
		    //
		    this.chooseButton.setDisabled(false);
		    return this.chooseButton.click();
		}
	    }
	}
	return true;
    }

    function _setChooseButtonDisabled(disabled) {
	if (this.chooseButton) {
	    this.chooseButton.setDisabled(disabled);
	}
    }

    // Replaces entries in selectedFileField
    // Get the selected entries from the list box and place
    // them in the selected file field, as comma separated entries.
    //
    // Note that the listbox values are full paths and encoded with
    // a "folder" or "file" designation. They probably should
    // be relative paths. The open folder action now depends on the
    // fact that the value is a full path.
    // This will have an effect on the open folder
    // action when the selected value is placed into the look in field.
    // If relative paths are used for the values then
    // the relative path would need to be appended to the look in
    // field value.
    //
    // However it may be the case that the full paths are edited to
    // take off the last element in the full path and keep the 
    // full path list box entries. Full paths are generally more
    // convenient.
    //
    // Note that this handler should call any required handlers
    // needed by the list box, vs. placing the required listbox
    // handlers in a javascript statement as the value of the
    // onChange attribute.
    //
    // Note also that the SWAED guidelines say to place relavtive
    // paths into the selected file field, this probably means
    // just using the display name vs. the value. However note the
    // dependencies on the full paths as described above.
    //
    /**
     * Handler placed on the list box onchange enent.
     *
     * Place all currently selected entries in to the
     * selected file field. If the chooser mode is file, only
     * files are placed into the selected file field.
     * If the chooser mode is folder, only folders are placed in the
     * selected folder field.
     * If multiple selections are allowed the entries are separated
     * by the specified delimiter. Enteries are escaped appropriately
     * with the specified escape character.
     */
    function _handleOnChange() {

	// If nothing is selected disable buttons.
	// 
	if (!this.itemSelected()) {
	    this.openFolderButton.setDisabled(true); 
	    if (this.selectedfield &&
		(this.selectedfield.value == null ||
		    this.selectedfield.value == '')) {
		this.setChooseButtonDisabled(true);
	    }
	    return false;
	}

	// This may not be sufficient.
	// The issue is, should a file be selectable in a folder
	// chooser, period. Are they disabled or read only ?
	// And ditto for a multiple selection in a file chooser.
	// Should a folder be selectable as a multiple selection
	// in a file chooser ?
	//
	// This could be made more efficient
	// by return both arrays at once and making only
	// one pass
	//
	var folders = this.getSelectedFolders();
	var files = this.getSelectedFiles();
	var selections = null;

	// If a file chooser, deselect folders when mixed
	// with file selections and disable the openFolder button);
	//
	if (this.fileAndFolderChooser) {
	    selections = new Array(files.length + folders.length);
	    var i = 0;
	    for (; i< files.length; i++) {
	        selections[i] = files[i];
	    } 
	    for (j=0; j< folders.length; j++) {
	        selections[i+j] = folders[j];
	    } 
	    if (files.length > 0) {
		this.openFolderButton.setDisabled(true);
	    } else if (folders.length > 1) {
		this.openFolderButton.setDisabled(true);
	    } else if ((files.length == 0) || (folders.length == 1)) {
		this.openFolderButton.setDisabled(false);
	    }
	} else 
	if (!this.isFolderChooser()) {
	    if (files.length > 0) {
		this.openFolderButton.setDisabled(true);
		this.deselectFolders();
	    } else
	    if (folders.length > 1) {
		this.openFolderButton.setDisabled(false);
		var index = this.listentries.selectedIndex;
		this.deselectFolders();
		this.setSelected(index, true);
		listbox_changed(this.listentries.id);
		this.clearSelectedField();
	    } else 
	    if (folders.length == 1) {
		// Only allow one folder to be selected
		//
		this.openFolderButton.setDisabled(false);
		this.clearSelectedField();
	    } else {
		this.openFolderButton.setDisabled(true);
		this.clearSelectedField();
	    }
	    selections = files;
	} else {
	    // If a folder chooser allow more than one folder
	    // to be selected
	    //
	    selections = folders;
	    if (selections.length == 1) {
		this.openFolderButton.setDisabled(false);
	    } else {
		this.openFolderButton.setDisabled(true);
	    }
	}

	// Make sure the hidden select option array is up
	// to date in case there isn't a selectedFileField.
	//
	if (!this.setSelectedFieldValue(selections)) {
	    common_createSubmittableArray(this.selectionsId, 
		    this.listentries.form, null, selections);
	}

	var flag = ((selections!= null) && (selections.length > 0));
	this.armChooseButton(flag);

	return false;
    }

    function _clearSelectedField() {
	if (this.selectedfield) {
	    this.selectedfield.value = '';
	}
    }

    // This function is the event handler for the onclick event
    // of the openFolder button.
    //
    function _openFolderClicked() {

	if (!this.isFolderSelected()) {
	    return false;
	}
	this.clearSelectedField();

	// Only works because the value is a full path.
	// 
	this.lookinfield.value = this.getSelectionValue();

	return true;
    }

    function _isFolderSelected() {
	return this.getSelectionType() == 'folder';
    }

    // This function is the event handler for the moveUp button.
    // Set the look in field to contain the parent or move up directory.
    // This is imperative. Be careful of the corner case when the 
    // look in field is already the root directory.
    //
    function _moveUpButtonClicked() {


	this.clearSelections();
	this.lookinfield.value = this.parentFolder;
    }

    // The values of the list options are encoded as
    //
    // <type>=<value>
    //
    // Where type is one of "file" or "folder"
    //
    function _getSelectionValue() {
	var index = this.listentries.selectedIndex;
	return this.getSelectionValueByIndex(index);
    }

    function _getSelectionValueByIndex(index) {

	var selection = this.listOptions[index].value;
	var i = selection.indexOf('=', 0);
	if (i < 0) {
	    return null;
	}
	if (i != 0) {
	    i = i + 1;
	}
	var value = selection.substring(i, selection.length); 
	return value;
    }

    function _getSelectionType() {
	var index = this.listentries.selectedIndex;
	return this.getSelectionTypeByIndex(index);

    }

    function _getSelectionTypeByIndex(index) {

	var selection = this.listOptions[index].value;
	return this.getValueType(selection);
    }

    function _getValueType(value) {
	var i = value.indexOf('=', 0);
	if (i <= 0) {
	    return null;
	}
	var type = value.substring(0, i); 
	return type;
    }

    function _isFolderChooser() {
	return this.folderChooser;
    }

    function _itemSelected() {
	return (this.listentries.selectedIndex != -1);
    }

    function _getSelectedFolders() {
	return this.getSelectedValuesByType('folder');
    }

    function _getSelectedFiles() {
	return this.getSelectedValuesByType('file');
    }

    // Return all selected options by type, file or folder
    //
    function _getSelectedValuesByType(type) {

	var selections = new Array();
	var i = 0;
	for (var j = 0; j < this.listOptions.length; j++) {
	    if (this.listOptions[j].selected){
		if (this.getSelectionTypeByIndex(j) == type) {
		    selections[i++] = this.getSelectionValueByIndex(j);
		} 
	    } 
	} 
	return selections;
    }

    // Format the selected file field as a comma separated list.
    //
    function _setSelectedFieldValue(selections) {

	var value;

	if (this.selectedfield == null) {
	    return false;
	}

	if (selections == null || selections.length == 0) {
	    return false;
	} else {
	    
	    value = common_escapeString(this.getFileNameOnly(selections[0]), 
		this.delimiter, this.escapeChar);
	}

	for (var j = 1; j < selections.length; j++) {
	    value = value + ',' + 
		    common_escapeString(this.getFileNameOnly(selections[j]),
		        this.delimiter, this.escapeChar);
	} 

	if (value != null && value != '') { 
	    this.selectedfield.value = value;
	} else { 
	    this.selectedfield.value = '';
	} 
	return true;
    }

    function _onFocus(element) {

	if (element.id == this.lookinfield.id) {
	    this.lookinCommitted = false;
	    this.lastLookInValue = this.lookinfield.value;
	} else 
	if (element.id == this.filterfield.id) {
	    this.filterCommitted = false;
	    this.lastFilterValue = this.filterfield.value;
	}
	return true;
    }

    function _onBlur(element) {

	if (element.id == this.lookinfield.id) {
	    if (this.lookinCommitted == false) {
		this.lookinfield.value = this.lastLookInValue;
	    }
	} else 
	if (element.id == this.filterfield.id) {
	    if (this.filterCommitted == false) {
		this.filterfield.value = this.lastFilterValue;
	    }
	}
	return true;
    }

    // Clear the selections whenever the selectedFileField is cleared.

    function _clearSelections() {

	var i = 0;
	for (var j = 0; j < this.listOptions.length; j++) {
	    if (this.listOptions[j].selected){
		this.listOptions[j].selected = false;
	    } 
	} 
	// call listbox_changed to update the
	// private state
	//
	listbox_changed(this.listentries.id);

	if (this.selectedfield != null) {
	    this.selectedfield.value = "";
	}
    }

    function _setSelected(index, torf) {
	this.listOptions[index].selected = torf;
	listbox_changed(this.listentries.id);
    }

    function _deselectFolders() {
	this.deselectSelectionsByType('folder');
    }

    function _deselectSelectionsByType(type) {

	for (var j = 0; j < this.listOptions.length; j++) {
	    if (this.listOptions[j].selected &&
		    this.getValueType(this.listOptions[j].value) == type) {
		this.listOptions[j].selected = false;
	    } 
	} 
	listbox_changed(this.listentries.id);
    }

    function _armChooseButton(flag) {
	var disabled = true;
	if (this.selectedfield == null) {
	    disabled = flag;
	} else if (this.selectedfield.value != null 
	    && this.selectedfield.value != '') {
	        disabled = false;
	} 
	this.setChooseButtonDisabled(disabled);
    }

    // Note that this is the only way that the file chooser can control
    // the submit of selected values. If this button is not set then
    // only an external submit button can submit the selections.
    // That means that if there is no chooser button assigned, double clicking
    // a file entry or hitting the return key in the selected file field
    // will NOT submit the values.
    //
    // This "feature" may become configurable.

    /*
     * convenience function to allow developers to disable their
     * chooser button when no entries from the filechooser are
     * selected. This function is not yet complete.
     */
    function _setChooseButton(buttonId) {

	this.chooseButton = document.getElementById(buttonId);
	// See if there are selections and if so 
	// enable the button. Needs to be after the assignment
	//
	var selections = document.getElementById(this.selectionsId);
	var disabled = true;
	if ((selections != null) && (selections.length > 0)) {
	    disabled = false;
	}
	this.armChooseButton(disabled);
    }

    /*
     * Convenience function to get the current directory without 
     * going to the server
     */
    function _getCurrentDirectory() {
	if (this.lookinfield) {
	    return this.lookinfield.value;
	}
    }

    /*
     * Convenience function returning the list of option elements
     */
    function _getOptionElements() {
	return this.listOptions;
    }

    /*
     * Convenience function to get the list of selected option elements
     * Return an array of selected values or a 0 length array if there
     * are no selections.
     */
    function _getSelectedOptions() {

	var selections = new Array();
	var i = 0;
	for (var j = 0; j < this.listOptions.length; j++) {
	    if (this.listOptions[j].selected){
		selections[i++] = this.getSelectionValueByIndex(j);
	    } 
	} 
	return selections;
    }

    /*
     * Convenience function to get the file or folde name when 
     * the entire path name is supplied.
     */
    function _getFileNameOnly(absoluteFileName) {
        arrayOfPaths = absoluteFileName.split(this.separatorChar);
	justTheFileName = arrayOfPaths[arrayOfPaths.length -1];
        return justTheFileName;
    }
}
