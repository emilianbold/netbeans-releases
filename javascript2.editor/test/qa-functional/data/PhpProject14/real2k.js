/* This notice must be untouched at all times.

Open-jACOB Draw2D
The latest version is available at
http://www.openjacob.org

Copyright (c) 2006 Andreas Herz. All rights reserved.
Created 5. 11. 2006 by Andreas Herz (Web: http://www.freegroup.de )

LICENSE: LGPL

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License (LGPL) as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA,
or see http://www.gnu.org/copyleft/lesser.html
*/

/**
 * Operation is the basic building block of the experiment designer
 * @constructor
 * @param    {String}  name           The name of operation, e.g. 'RetschMill'; there should be a image file available in the
 *                                     imageFolder with the same name, e.g. 'RetschMill.png'
 * @param    {object}  configObject   The configuration object, e.g.
 *
 *				config = {
 *				'title'       : 'myTitle', // the title used in the ui
 *				'description' : 'myDescription', // a html description used in the ui
 *				'type'        : 'method/operation', // determines the type, which influences the ui
 *				'subType'     : 'myInteger' //determines the subtype, which influences the ui
 *				'hasResults'  : 'myBoolean' //determines whether the students obtains results in this operation (default: false)
 *				'inputPorts' : [
 *					{
 *						'name': 'myPortName'
 *					}
 *				],
 *				'outputPorts': [
 *					{
 *						'name': 'myPortName'
 *					}
 *				],
 *				'deleteable' : true,
 *				'feedback' : {
 *					'form' : {
 *						'myFormElementId' // the name of the form element, see workflow.dialogWindow.setIOFormTemplate  :[
 *							{
 *								'triggerValue' : 'myValue', // the value the form element should have in order to give feedback
 *								'feedbackText' : 'myText'
 *							}
 *						]
 *					},
 *					'workflow': [
 *						{
 *								'operationNamesRegExp': 'myRegExpMatch', // all upstream/downstream operation names will be concatenated, starting from this operation, separated by underscores
 *																																 // e.g 'Brabender_HPLC_Soybeans', if 'myRegExpMatch' matches with this string, the feedback is given
 *
 *								'feedbackText'       : 'myText',
 *								'direction'          : 'upstream/downstream'
 *						}
 *					]
 *				},
 *				'wlmId'      : 'myId', // the id of this method/operation in the web-based lab manual
 *				'timeNeeded' : '60' //the time needed in minutes for this method
 *				'allowCircularReferences': true // can this operation be a part of a circular reference, e.g. operationA-->operationB-->operationB (default: false)
 *			}
 * @requires Mootools          The Mootools framework can be downloaded from http://mootools.net/
 **/
draw2d.Operation=function(name, configObject)
{
	var feedbackArray, i;

	/* General properties
	*/

	/* add event listener	 */
	this.eventListenerFactory = new EDEventListener();

	/* properties window object to communicate with */
	this.workflow         = workflow;
	this.propertiesWindow = this.workflow.dialogWindow;
	this.formParser       = this.propertiesWindow.formParser; //formparser
	this.form             = this.propertiesWindow.form;       //form

	/* the width of the port in px */
	this.portsTotalWidth = 90;

	/* the status of this operation, set this.setStatus */
	this.status = false;

	/* images */
	this.guiPath        = ED_UI_ELEMENTS_DIR;
	this.imageFolder    = ED_OPERATIONS_ELEMENTS_DIR;
	this.imageExtension = '.png';                      //all images should have the same extension

	/* assigned users book keeping (see draw2d.Operation.prototype.setAssignedUsers)  */
	this.assignedUserNrs = new Array();

	/* deleteability is set after parent class has been called */

	/* other */
	this.connectionColor    = new draw2d.Color(160,160,160); //color of the connections between operations
	this.locationDimensions = new Array();                   //stores location/dimension objects

	/* handle config object
  */
	this.config = configObject;

	//set default view nr
	this.config.viewNr = 1;

	//set default allowCircularReferences
	if (this.config.allowCircularReferences==undefined){
		this.config.allowCircularReferences = false;
	}

	/* name, title, description, type */
  this.setName(name)
	this.setTitle(configObject.title == undefined ?  name : configObject.title);
	this.setDescription(configObject.description);
	this.setType(configObject.type == undefined ? 'operation' : configObject.type);
	this.config.subType = this.config.subType == undefined ? 1 : this.config.subType;

	/* ports */
	this.inputPort  = null;
	this.outputPort = null;
	this.setInputPortDefinitions(configObject.inputPorts);
	this.setOutputPortDefinitions(configObject.outputPorts);


	/* form feedback */
	this.formFeedback = new feedback();                //feedback storage

	if (configObject.feedback!=undefined){
		if (configObject.feedback.form!=undefined){
			for (var formElementId in configObject.feedback.form){
				/* check whether object or array of objects given */
				if (configObject.feedback.form[formElementId].constructor.toString().indexOf("Array") != -1){
					//array given
					feedbackArray = configObject.feedback.form[formElementId];
				}else{
					//object given, put it in array
					feedbackArray = new Array(configObject.feedback.form[formElementId]);
				}

				/* loop over all elements in array and add form feedback */
				for (i=0; i<feedbackArray.length; i++){
					this.addFormFeedback(formElementId, {
						'triggerValue' : feedbackArray[i].triggerValue,
						'text'         : feedbackArray[i].feedbackText
					});
				}
			}
		}


		if (configObject.feedback.workflow!=undefined){
			this.workflowFeedback = configObject.feedback.workflow;
		}
	}

	/* settings which cannot be set without config object handled
	*/

	/* images */
	this.setImageURL(this.imageFolder+"operationImage.php?name="+this.name+"&type="+this.type+"&title="+encodeURIComponent(this.getTitle()))+"&showPicture=1";

	/* call parent class
  */
	draw2d.Node.call(this);

	/* do some settings after the parent class has been called */
	this.setDimension(80,80);
	this._setPorts();

	//set move/resizability restrictions
	this.setMoveRestrictions(false, false);
	this.setResizeableX(false);
	this.setResizeableY(true);
	this.setResizeable(false);

	//deleteability
	this.setDeleteable(configObject.deleteable == undefined ? true : configObject.deleteable);
}

/* call parent
*/
draw2d.Operation.prototype = new draw2d.Node;

/**
 * Returns a clone of the current object. NOTE: all new functions should be added to this function
 * @returns {Object}  The cloned object
 **/
draw2d.Operation.prototype.clone = function(){

	/* create clone */
	var clone = new draw2d.Operation(this.name, this.config);

	/* copy private properties */
	clone._setFormFeedbackObject(this._getFormFeedbackObject());

	/* copy all properties/functions not defined in the config (so: all private ones) */
	clone.setImageURL(this.getImageURL());
  clone.setSettingsFormTemplate(this.getSettingsFormTemplate());
	clone.setMoveRestrictions(this.getMoveRestrictions());
	clone.connectionColor = this.connectionColor;
	clone.guiPath         = this.guiPath;

	/* tell the newborn operation who is the mother */
	clone.setCloneMother(this);

	/* return */
	return clone;
}

/**
 * Sets the move restrictions
 * @param  boolean  restrictX  If set to true, the operation cannot be moved in the x direction
 * @param  boolean  restrictY  If set to true, the operation cannot be moved in the Y direction
 * @public
 **/

draw2d.Operation.prototype.setMoveRestrictions = function(restrictX, restrictY){
	restrictX = restrictX == undefined ? false : restrictX;
	restrictY = restrictY == undefined ? false : restrictY;

	this.moveRestrictions = {x: restrictX, y: restrictY};
}

/**
 * Sets the move restrictions
 * @param  boolean  restrictX  If set to true, the operation cannot be moved in the x direction
 * @param  boolean  restrictY  If set to true, the operation cannot be moved in the Y direction
 * @public
 **/

draw2d.Operation.prototype.getMoveRestrictions = function(){
	return this.moveRestrictions;
}

/**
 * Will be called when drag starts
 * @param {int} x the x-coordinate of the click event
 * @param {int} y the y-coordinate of the click event
 * @type boolean
 */
draw2d.Operation.prototype.onDragstart = function(x, y){
	/* call parent class */
	draw2d.Figure.prototype.onDragstart.call(this, x, y);

	/* save the start position of the drag */
	this.setDragStartPosition(this.getX(), this.getY());

	/* tell the listeners */
	return this.fireEvent('ondragstart');
}

draw2d.Operation.prototype.onDragend = function(){

	/* call parent class */
	draw2d.Figure.prototype.onDragend.call(this);

	if (this.workflow.viewNr==2){
		this.setFormDateTime();
	}

	//store location and dimensions
	this.setLocationAndDimensions('view'+this.workflow.viewNr, this.getPosition().x, this.getPosition().y, this.width, this.height, true);

	/* tell the listeners */
	return this.fireEvent('ondragend');
}

/**
 * This function is called when the operation has been resized
 **/
draw2d.Operation.prototype.onResizeend = function(){
	if (this.workflow!=undefined){
		//store location and dimensions
		this.setLocationAndDimensions('view'+this.workflow.viewNr, this.getPosition().x, this.getPosition().y, this.width, this.height, true);

		if (this.workflow.viewNr==2){
			//update the form and set the date/time
			this.setFormDateTime();
		}
	}

	return this.fireEvent('onresizeend');
}

/**
 * Saves the position/size of this operation just before dragging
 * @param {integer} x The x position
 * @param {integer} y The y position
 **/
draw2d.Operation.prototype.setDragStartPosition = function(x, y){
	this.setLocationAndDimensions('dragStart', x, y, this.width, this.height);
}

/**
 * Returns the position/size of this operation just before dragging
 * @param {integer} x The x position
 * @param {integer} y The y position
 **/
draw2d.Operation.prototype.getDragStartPosition = function(){
	return this.getLocationAndDimensions('dragStart');
}

/**
 * Creates the HTML inside the operation, is called by Draw2d library
 **/
draw2d.Operation.prototype.createHTMLElement = function(){
	return this._initGUI();
}

/**
 * Initializes the graphical user interface
 * @returns  {object}  DOM object containing the UI
 **/
draw2d.Operation.prototype._initGUI = function(){
	var i, userDiv, users, numberOfUsers;

	var title = this.config.wlmId !=undefined ? 'Double click to highlight the methods connected to this method.' : '';
	var container = new Element('div', {
		"id"        : this.id,
		"title"     : title,
		"class"     : "operationContainer operationSubtype-"+this.config.subType,
		"styles"    : {
			"left"             : this.x+"px",
			"top"              : this.y+"px",
			"height"           : (this.height+60)+"px",
			"width"            : this.width+"px",
			"z-index"          : ""+draw2d.Figure.ZOrderBaseIndex
		}
	});

	var shadowDiv = new Element('div', {
		"class"     : "operationShadow"
	});

	var contentWrapper = new Element('div', {
		"class" : "operationContentWrapper"
	});

	this.toggleVisibilityLink = new Element('div',{
		"class" : "operationToggle",
		"styles" :{
			"background" : "url('"+this.guiPath+"accordeonClosed.png') no-repeat"
		},
		"backgroundAlt": "url('"+this.guiPath+"accordeonOpen.png') no-repeat",
		"html":  "&nbsp;"
	});
	this.toggleVisibilityLink.addEvent('click', function(){this.choseView(this.config.viewNr==1?2:1)}.bind(this));

	this.titleBar = new Element('div', {
		"html"  : this.getTitle(),
		"class" : "operationTitle"
	});

	this.content = new Element('div', {
		"html"  : "",
		"class" : "operationContent"
	});

	this.image = new Element('img', {
		"src" : this.getImageURL()+'&showPicture=1'
	});

	/* add users divs */
	users         = this.workflow.users;
	numberOfUsers = users.length;

	if (numberOfUsers!=undefined){
		this.userDivs  = new Array();

		for (i=0; i<numberOfUsers; i++){
			userDiv = new Element('div', {
				"html"  : "",
				"title" : users[i].name+" will carry out this method",
				"class" : "operationUser operationUser-"+i
			});
			this.userDivs[i] = userDiv;
			this.content.appendChild(userDiv);
		}
	}

	/* add event listener on setformvalues, to know when to class this.setFinishedGUI/setBusyGui */
	var setFinishBusyGuiFunction = function(){
		var formStatusValue = this.getOperationFormValue('dialogWhenStatus');

		formStatusValue = formStatusValue=='' ? false : formStatusValue;

		this.setStatus(formStatusValue);
	}.bind(this);

	this.addEventListener('onsetoperationformvalues', setFinishBusyGuiFunction);

	this.content.appendChild(this.titleBar);
	this.content.appendChild(this.image);

	contentWrapper.appendChild(this.content);
	//contentWrapper.appendChild(this.toggleVisibilityLink);
	shadowDiv.appendChild(contentWrapper);
	container.appendChild(shadowDiv);

	return container;
}

/**
* Defines the number and names of the output ports; these names will be used to
*  get the images for these ports.
* @param {Array} outputPortDefinitions  Array of objects, see draw2d.Operation.prototype.setInputPortDefinitions
**/
draw2d.Operation.prototype.setOutputPortDefinitions = function(outputPortDefinitions){
	if (outputPortDefinitions==undefined) outputPortDefinitions = new Array({name:'Out'});
	this.outputPortDefinitions = outputPortDefinitions;
  this.numberOut       = this.outputPortDefinitions.length;
}

/**
 * Returns output port names
 * @returns {Array}  List with names
 **/
draw2d.Operation.prototype.getOutputPortDefinitions = function(){
	return this.outputPortDefinitions;
}

/**
 * Removes the output ports
 * @public
 **/
draw2d.Operation.prototype.removeOutputPorts = function(){
	this.setOutputPortDefinitions(new Array({name : 'Out', hiddenOnOperation: true, hiddenOnForm: true}));
}

/**
* Defines the number and names of the input ports; these names will be used to
*  get the images for these ports.
* @param {Array} inputPortDefinitions  Array of objects with the port properties, the 'name' property being
*                                       mandatory; if not provided, the object will create an array with one entry 'In'.
*
*                                       The port definition object properties are:
*
*                                        name                 The name; Note: assigning multiple names which fit in other names can cause bugs, e.g. 'In' fits in 'Input'.*
*                                        hiddenOnOperation    Whether the port is visible on the operation
*                                        hiddenOnForm         Whether the port has a form
*
*                                       e.g. myOperation.setInputPortDefinitions({name: 'myFirstInput'},
*                                                                      {name: 'mySecondInput', hiddenOnForm: true});
**/
draw2d.Operation.prototype.setInputPortDefinitions = function(inputPortDefinitions){
	if (inputPortDefinitions==undefined) inputPortDefinitions = new Array({name:'In'});
	this.inputPortDefinitions = inputPortDefinitions;
  this.numberIn       = this.inputPortDefinitions.length;
}

/**
 * Returns input port names
 * @returns {Array}  List with names
 **/
draw2d.Operation.prototype.getInputPortDefinitions = function(){
	return this.inputPortDefinitions;
}

/**
 * Removes the input ports
 * @public
 **/
draw2d.Operation.prototype.removeInputPorts = function(){
	this.setInputPortDefinitions(new Array({name : 'In', hiddenOnOperation: true, hiddenOnForm: true}));
}

/**
 * Makes all ports invisible
 * @public
 **/
draw2d.Operation.prototype.hidePorts = function(){
	var ports, numberOfPorts, i, port;

	ports         = this.getPorts();
	numberOfPorts = ports.getSize();


	for (i=0; i<numberOfPorts; i++){
		port = ports.get(i);
		port.currentUIRepresentation.html.parentNode.style.display = 'none';
	}
}

/**
 * Makes all ports visible
 * @public
 **/
draw2d.Operation.prototype.showPorts = function(){
	var ports, numberOfPorts, i, port;

	ports         = this.getPorts();
	numberOfPorts = ports.getSize();

	for (i=0; i<numberOfPorts; i++){
		port = ports.get(i);
		port.currentUIRepresentation.html.parentNode.style.display = 'block';
	}
}

/**
 * Makes all labels on outgoing connection labels invisible
 * @public
 **/
draw2d.Operation.prototype.hideOutputConnectionsLabels = function(){
	var j, ports, numberOfPorts, i, port;
	var connection, label, connections, numberOfOutConnections;

	ports         = this.getPorts();
	numberOfPorts = ports.getSize();

	for (i=0; i<numberOfPorts; i++){
		port = ports.get(i);
		if (port.type == 'OutputPort'){
			//get connections to this port
			connections            = port.getConnections();
			numberOfOutConnections = connections.getSize();

			//loop over connections
			for (j=0; j<numberOfOutConnections; j++){
				connection = connections.get(j);

				//hide label
				connection.label.html.style.display = 'none';
			}
		}
	}
}


/**
 * Makes all labels on outgoing connection labels visible
 * @public
 **/
draw2d.Operation.prototype.showOutputConnectionsLabels = function(){
	var j, ports, numberOfPorts, i, port;
	var connection, label, connections, numberOfOutConnections;

	ports         = this.getPorts();
	numberOfPorts = ports.getSize();

	for (i=0; i<numberOfPorts; i++){
		port = ports.get(i);
		if (port.type == 'OutputPort'){
			//get connections to this port
			connections            = port.getConnections();
			numberOfOutConnections = connections.getSize();

			//loop over connections
			for (j=0; j<numberOfOutConnections; j++){
				connection = connections.get(j);

				//show label
				connection.label.html.style.display = 'block';
			}
		}
	}
}

/**
 * Disables all outgoing connections, so user can't select etc them.
 * @public
 **/
draw2d.Operation.prototype.disableOutputConnections = function(){
	var j, ports, numberOfPorts, i, port;
	var connection, connections, numberOfOutConnections;

	ports         = this.getPorts();
	numberOfPorts = ports.getSize();

	for (i=0; i<numberOfPorts; i++){
		port = ports.get(i);
		if (port.type == 'OutputPort'){
			//get connections to this port
			connections            = port.getConnections();
			numberOfOutConnections = connections.getSize();

			//loop over connections
			for (j=0; j<numberOfOutConnections; j++){
				connection = connections.get(j);

				//disable connection and hide resize handles
				connection.setSelectable(false);
				connection.getWorkflow().hideLineResizeHandles(connection);
			}
		}
	}
}


/**
 * Enables all outgoing connections, so user can select etc them.
 * @public
 **/
draw2d.Operation.prototype.enableOutputConnections = function(){
	var j, ports, numberOfPorts, i, port;
	var connection, connections, numberOfOutConnections;

	ports         = this.getPorts();
	numberOfPorts = ports.getSize();

	for (i=0; i<numberOfPorts; i++){
		port = ports.get(i);
		if (port.type == 'OutputPort'){
			//get connections to this port
			connections            = port.getConnections();
			numberOfOutConnections = connections.getSize();

			//loop over connections
			for (j=0; j<numberOfOutConnections; j++){
				connection = connections.get(j);

				//enable connection
				connection.setSelectable(true);
			}
		}
	}
}

/**
 * Sets the workflow and adds ports
 * @param {Object}  workflow  A draw2d workflow object
 **/
draw2d.Operation.prototype.setWorkflow = function(workflow)
{
	var outputPortId, outputPortCaption;

	/* call parent class */
  draw2d.ImageFigure.prototype.setWorkflow.call(this,workflow);


}

/**
 * Adds all the ports, based on the inputPortDefinitions and the outputPortDefinitions
 * @private
 **/
draw2d.Operation.prototype._setPorts = function(){
	var outputPortId, outputPortCaption;

	/* add ports */
  if(workflow!=null && this.inputPort==null && this.outputPort==null)
  {
  	/* add output ports
		 */
  	for (var i=0; i<this.numberOut; i++){
			if (this.outputPortDefinitions[i].hiddenOnOperation!=true){
				/* create output port */
				outputPortId       = 'outputPort'+i;
				outputPortCaption  = this.outputPortDefinitions[i].name;
	  		this[outputPortId] = new draw2d.OutputPort(new draw2d.ImageFigure(ED_UI_ELEMENTS_DIR+'operationPort.php?title='+outputPortCaption+'&type=Out'));
				this[outputPortId].setWorkflow(workflow);
				this[outputPortId].setName(this.outputPortDefinitions[i].name);

				/* do some settings */
				this[outputPortId].setDimension(40,10);

				//this[outputPortId].setMaxFanOut(1); 				//it is only possible to add 1 connection to this port

				//onDrop function: add connection when this output port is dropped on another port
				this[outputPortId].onDrop = function(port){
					var outputDescription, command, connection, portName;

					/* find out whether connection is allowed */

					if (this.type==port.type){
						this.getParent().throwUserError('You cannot connect two outgoing streams.');
						return;
					}else if(this.parentNode.id == port.parentNode.id)
					{
						return;
					}

					//check for circular references
					if (this.getParent().config.allowCircularReferences==false || port.getParent().config.allowCircularReferences==false){
						if (port.getParent().findConnectedOperation(this.getParent())==true){
							this.getParent().throwUserError('You cannot connect to this operation, because it would create a circulair reference.\n\nE.g. This method -> Method A --> ... --> Method B --> This method');
							return;
						}
					}

					//get description of the output
					portName          = this.getName();
					outputDescription = this.getParent().getOperationFormValue(portName+'Description');

					command    = new draw2d.CommandConnect(workflow, port, this);

					connection = new draw2d.OperationConnection(outputDescription, this.getParent().connectionColor);
					command.setConnection(connection);
					workflow.getCommandStack().execute(command);

					//set focus to this operation and update the other operation
					this.workflow.setCurrentSelection(this.parentNode);
					this.getParent().update();
				}

				/* add the port to the operation */
				console.log(outputPortId+ ' ' + ((this.width/2)-(this.portsTotalWidth/2)+(i*(this.portsTotalWidth/this.numberOut))+((this.portsTotalWidth/this.numberOut)/2))+ ' ' + this.height);
				this.addPort(this[outputPortId], (this.width/2)-(this.portsTotalWidth/2)+(i*(this.portsTotalWidth/this.numberOut))+((this.portsTotalWidth/this.numberOut)/2), this.height);
			}
		}

  	/* add input ports */
  	for (i=0; i<this.numberIn; i++){
			if (this.inputPortDefinitions[i].hiddenOnOperation!=true){

				/* create input port */
				var inputPortCaption  = this.inputPortDefinitions[i].name;
	  		var inputPortId       = 'inputPort'+i;

	    	this[inputPortId]  = new draw2d.InputPort(new draw2d.ImageFigure(ED_UI_ELEMENTS_DIR+'operationPort.php?title='+inputPortCaption+'&type=In'));
		    this[inputPortId].setWorkflow(workflow);
		    this[inputPortId].setName(this.inputPortDefinitions[i].name);

				/* do some settings */
				this[inputPortId].setDimension(40,10);

				/* if there are no outputport definitions (so this would be an end step), hide the input port if connected,
				 * so in principle, all measuring operations can only be connected to one other operation */
				//if (this.outputPortDefinitions == undefined || this.outputPortDefinitions.length==0){
				this[inputPortId].setHideIfConnected(true);
				//}

				//onDrop function: add connection when this output port is dropped on another port
				this[inputPortId].onDrop = function(port){
					var outputDescription, command, connection, portName;

					/* find out whether connection is allowed */
					if (this.type==port.type){
						this.getParent().throwUserError('You cannot connect two ingoing streams.');
						return;
					}else if (this.parentNode.id == port.parentNode.id){
						return;
					}

					//check for circular references
					if (this.getParent().config.allowCircularReferences==false || port.getParent().config.allowCircularReferences==false){
						if (port.getParent().findConnectedOperation(this.getParent(), {'direction': 'upstream'})==true){
							this.getParent().throwUserError('You cannot connect to this operation, because it would create a circulair reference.\n\nE.g. This method -> Method A --> ... --> Method B --> This method');
							return;
						}
					}
					//get description of the output from the port on which this input port is dropped
					portName          = port.getName();
					outputDescription = port.getParent().getOperationFormValue(portName+'Description');

					//make the connection
					command    = new draw2d.CommandConnect(workflow,port, this);
					connection = new draw2d.OperationConnection(outputDescription, this.getParent().connectionColor);

					command.setConnection(connection);
					workflow.getCommandStack().execute(command);

					//set focus to this operation and update the other operation
					this.workflow.setCurrentSelection(this.parentNode);
					port.getParent().update();
				}



				/* add the port to the operation */
				this.addPort(this[inputPortId], (this.width/2)-(this.portsTotalWidth/2)+(i*(this.portsTotalWidth/this.numberIn))+((this.portsTotalWidth/this.numberIn)/2), 0);
			}
		}
	}
}

/**
 * Finds out whether a certain operation is connected to this operation
 * @param   {object/string}  operationReference  The operation object to look for, or its name
 * @param   {object}         config              See draw2d.Operation.prototype.getConnectedOperations
 * @returns {boolean}
 **/
draw2d.Operation.prototype.findConnectedOperation = function(operationReference, config){
	var i, connectedOperations, numberOfConnectedOperations, compareFunction;

	/* define the function which will compare the operationReference with the connected operations */
	if (typeof operationReference == 'string'){
		/* compare string with object */
		compareFunction = function(operationAName, operationB){
			if (operationAName==operationB.getName()) return true;
			return false;
		}
	}else{
		/* compare object with object */
		compareFunction = function(operationA, operationB){
			if (operationA==operationB) return true;
			return false;
		}
	}

	/* get the connected operations */
	connectedOperations         = this.getConnectedOperations(config).stack;
	numberOfConnectedOperations = connectedOperations.length;

	for (i=0; i<numberOfConnectedOperations; i++){
		/* call the compare function */
		if (compareFunction.call(this, operationReference, connectedOperations[i].operation)==true){
			return true;
		}
	}

	return false;
}

/**
 * Stores the form values string
 * @param {Array}  formValues  The formvalues
 **/
draw2d.Operation.prototype.setOperationFormValues = function (formValues){
	this.setProperty('formValues', formValues);

	this.fireEvent('onsetoperationformvalues');
}

/**
 * Returns the form values string
 * @returns {Array} The formvalues
 **/
draw2d.Operation.prototype.getOperationFormValues = function (){
	var returnArray = this.getProperty('formValues');

	if (typeof returnArray!='object') returnArray = new Array();

	return returnArray;
}

/**
 * Clears the form elements of which the name give a match with a regular expression
 * @public
 * @param  {RegExp}  regularExpression  The regular expression, e.g. /myName/
 */
draw2d.Operation.prototype.clearOperationFormValuesOnNameMatch = function(regularExpression){
	var i, elementName;
	var formValues = this.getOperationFormValues();

	//loop through all form values
	for (elementName in formValues){
		if (elementName.search(regularExpression)!=-1){
			delete formValues[elementName];
		}
	}

	//save the array
	this.setOperationFormValues(formValues);

}

/**
 * Something is changed: this function handles the update of this operation AND
 *  the update of the operation connected to this operation's output ports
 *  1) Update input
 *  2) Update output
 *
 *  @param  {object}  config  JSON object with configuration parameters: {'updateInputOutputForm': boolean, //if true (default) the I/O form is updated
 *                                                                        'updateAssignmentForm' : boolean, //if true (default) the assignment form is updated}
 **/
draw2d.Operation.prototype.update = function (config){
	var i, j, port, portName, portDescription, connections, connection, label, numberOfOutConnections, numberOfInConnections;
	var sourceOperation, sourcePort, targetPort, sourcePortName, formElementName, sourcePortNameLength, sourceOperationAllFormValues;
	var portKey, match, property, value, oldValue, newValue, newFormValues;

	var downStreamOperations, operationDialogAssignmentValues, numberOfDownStreamOperations, downStreamOperation;
	var downStreamOperationDialogAssignmentValue, downStreamOperationDialogAssignmentValues, numberOfdownStreamOperationDialogAssignmentValues;

	config = config == undefined ? {} : config;

	var thisFormValues            = this.getOperationFormValues(); //get all form values of this operation

	if (config.updateInputOutputForm == true || config.updateInputOutputForm==undefined){

		var ports                     = this.getPorts();
		var numberOfPorts             = ports.getSize();

		/* 1) Update input
		*/
		for (i=0; i<numberOfPorts; i++){
			port = ports.get(i);
			if (port.type == 'InputPort'){
				portName            = port.getName();

				/* clear for the 'In' form elements
					 - the form values (if this operation is currently selected)
					 - the internal array in which the values are stored
				*/
				match = new RegExp(portName)

				if (this.propertiesWindow.selectedOperation == this){
					//this operation is selected in dialog window
					this.formParser.clearFormValuesOnNameMatch(match);
				}
				this.clearOperationFormValuesOnNameMatch(match)

				/* get connections to this port */
				connections           = port.getConnections();
				numberOfInConnections = connections.getSize();

				/*
					Loop over connections to update this operation's form values; This is done
					contributionally e.g. when a certain checkbox is checked in one of the
					ingoing connected operations, it will be checked in this operation
				 */
				for (j=0; j<numberOfInConnections; j++){
					connection                   = connections.get(j);

					//get source port
					sourcePort                   = connection.getSource() == port ? connection.getTarget() : connection.getSource(); //sourceport depends on whether the user connected
																																																													 //operation1.outputPort to operation2.inputPort or vice versa
					sourcePortName               = sourcePort.getName();
					sourcePortNameLength         = sourcePortName.length;

					sourceOperation              = sourcePort.getParent();
					sourceOperationAllFormValues = sourceOperation.getOperationFormValues();

					/* loop over source operation form values */
					for (formElementName in sourceOperationAllFormValues){
						/* filter keys for sourceport related form values */
						if (formElementName.substring(0,sourcePortNameLength) == sourcePortName){
							if (sourceOperationAllFormValues[formElementName]!=''){

								property = formElementName.substring(sourcePortNameLength); //get property name
								portKey  = portName+property;

								if (property =='Description' && thisFormValues[portKey]!=undefined){
									/* sum descriptions, separated by ';' */
									oldValue = thisFormValues[portKey];
									newValue = sourceOperationAllFormValues[formElementName];
									value = [oldValue+'; '+newValue];
								}else{
									/* checkboxes elements */
									value = sourceOperationAllFormValues[formElementName];
								}

								thisFormValues[portKey]            = value; //change the array with all form values of this Operation, so we can save it
							}
						}
					}
				}
			}
		}

		/* 2. Update output
				a. the labels of this operation's connections
				b. the operations connected to the output ports
		*/
		if (typeof thisFormValues=='object'){
			for (i=0; i<numberOfPorts; i++){
				port = ports.get(i);
				if (port.type == 'OutputPort'){
					portName            = port.getName();
					try{
						portDescription     = decodeURI(thisFormValues[portName+'Description']);
					}
					catch(err){
						portDescription     = thisFormValues[portName+'Description'];
					}


					//get connections to this port
					connections         = port.getConnections();
					numberOfOutConnections = connections.getSize();

					//loop over connections
					for (j=0; j<numberOfOutConnections; j++){
						connection = connections.get(j);
						label      = connection.label;

						/* a. update labels */

						//if empty, change description to label's default one
						portDescription     = portDescription=='' || portDescription==undefined ? label.getProperty('defaultDescription') : portDescription;

						//set label text and redraw the connection
						label.setStyledText(portDescription);
						connection.paint();

						/* b. update operations connected to this port */
						//targetPort = connection.getTarget() == port ? connection.getSource() : connection.getTarget(); //sourceport depends on whether the user connected
						//targetPort.getParent().update();
					}
				}
			}
		}
	}

	if (config.updateAssignmentForm == true || config.updateAssignmentForm==undefined){
		/* Update assignments
		 *  a. Get downstream operations' assignments
		 *  b. Set form values
		 */

		/* set the form values for the operation
		*/

		/* auto-set the dialogAssigment[], based on the downstream operations of the assigned operation */
		var operationDialogAssignmentSetByUserValues = new Array();
		if ($type(thisFormValues['dialogAssignmentCheckedByUser[]']) == 'string'){
			operationDialogAssignmentSetByUserValues = thisFormValues['dialogAssignmentCheckedByUser[]'].split(',');
		}else if ($type(thisFormValues['dialogAssignmentCheckedByUser[]']) == 'array'){
			operationDialogAssignmentSetByUserValues = thisFormValues['dialogAssignmentCheckedByUser[]'];
		}
		operationDialogAssignmentValues = $A(operationDialogAssignmentSetByUserValues);

	 //get downstream operations
		downStreamOperations = this.getConnectedOperations({'direction': 'downstream'}).stack;
		numberOfDownStreamOperations = downStreamOperations.length;

		for (i=0; i<numberOfDownStreamOperations; i++){
			/* get down stream operation's dialogAssigment[] value */
			downStreamOperation                      = downStreamOperations[i].operation;
			downStreamOperationDialogAssignmentValue = downStreamOperation.getOperationFormValue('dialogAssignment[]');

			if ($type(downStreamOperationDialogAssignmentValue) == "string" && downStreamOperationDialogAssignmentValue.length>0){
				downStreamOperationDialogAssignmentValues = downStreamOperationDialogAssignmentValue.split(',');

				operationDialogAssignmentValues = operationDialogAssignmentValues.extend(downStreamOperationDialogAssignmentValues);
			}
		}


		thisFormValues['dialogAssignment[]'] = operationDialogAssignmentValues.clean().unique();
	}

	/* set new values in dialog if this operation is currently selected */
	if (this.propertiesWindow.selectedOperation == this){
		this.formParser.setFormValues(thisFormValues, true);
	}

	/* save the (new) form values to this operation */
	this.setOperationFormValues(thisFormValues);

	/* let the world know we finished */
	this.fireEvent('onupdate');
}

/**
 * Returns a specified form value
 * @param   {String} name  the form value name
 * @returns {Array}        the value/values (e.g. array['varA'][0] = 1)
 **/
draw2d.Operation.prototype.getOperationFormValue = function(name){
	var formValuesArray = this.getProperty('formValues');

	if (formValuesArray!=undefined){
		return decodeURI(formValuesArray[name]);
	}else{
		return false;
	}
}

/**
 * Sets a specified form value
 * @param   {String}  name   the form value name
 * @param   {variant} value  its value
 **/
draw2d.Operation.prototype.setOperationFormValue = function(name, value){
	var formValuesArray = this.getProperty('formValues');

	if (formValuesArray!=undefined){
		formValuesArray[name] = encodeURI(value);
		this.setOperationFormValues(formValuesArray);
	}

	this.fireEvent('onsetoperationformvalues');
}


/**
* Sets the description
* @public
* @param {String} description Description HTML
*/
draw2d.Operation.prototype.setDescription = function (description){
	if (description==undefined) return;

	this.config.description = description;
	this.description        = description;
}

/**
* Gets the description
* @public
* @returns {String}  Description HTML
*/
draw2d.Operation.prototype.getDescription = function (){
		return this.description;
}

/**
* Sets the title
* @public
* @param {String} title Title
*/
draw2d.Operation.prototype.setTitle = function (title){
		this.title = title;
}

/**
* Gets the title
* @public
* @returns {String}  Title
*/
draw2d.Operation.prototype.getTitle = function (){
		return this.title;
}

/**
* Sets the name
* @public
* @param {String} name Name
*/
draw2d.Operation.prototype.setName = function (name){
		this.name = name;
}

/**
* Gets the name
* @public
* @returns {String}  Name
*/
draw2d.Operation.prototype.getName = function (){
		return this.name;
}

/**
* Sets the type
* @public
* @param {String} type Type
*/
draw2d.Operation.prototype.setType = function (type){
		this.type = type;
}

/**
* Gets the type
* @public
* @returns {String}  Type
*/
draw2d.Operation.prototype.getType = function (){
		return this.type;
}

/**
* Sets the URL of the image representing this operation
* @public
* @param {String} URL  The image URL
*/
draw2d.Operation.prototype.setImageURL = function (url){
		this.imageURL = url;
}

/**
* Gets the URL of the image representing this operation
* @public
* @returns {String}  The image URL
*/
draw2d.Operation.prototype.getImageURL = function (){
		return this.imageURL;
}

/**
 * Sets the template for the settings form
 * @public
 * @param {String} formHTML  The form HTML
 **/
draw2d.Operation.prototype.setSettingsFormTemplate = function(formHTML){
	this.settingsFormElements = formHTML;
}

/**
 * Gets the template for the settings form
 * @public
 * @returns {String}  The form HTML
 **/
draw2d.Operation.prototype.getSettingsFormTemplate = function(){
	return this.settingsFormElements;
}

/**
 * Add feedback for a certain form element
 * @public
 * @param {String}  formElementName  The name of the form element
 * @param {Array}   feedbackHash    Array with objects describing the feedback, see feedback.prototype.addFeedback.
 */
draw2d.Operation.prototype.addFormFeedback = function(formElementName, feedbackHash){
	this.formFeedback.addFeedback(formElementName, feedbackHash);
}

/**
 * Returns feedback for a certain form element
 * @public
 * @param {String}    formElementName  The name of the form element
 * @param {Mixed}     value            The value on which the feedback applies
 * @returns {Array}   Array of hashes, see feedback.prototype.getFeedback
 */
draw2d.Operation.prototype.getFormFeedback = function(formElementName, value){
	var port, portName;
	var ports                     = this.getPorts();
	var numberOfPorts             = ports.getSize();

	/* Filter feedback:
	   - no feedback is given for form elements related to input ports without connections
	*/
	for (i=0; i<numberOfPorts; i++){
		port     = ports.get(i);
		portName = port.getName();

		if (port.type == 'InputPort'){
			if (port.getConnections().getSize()==0){
				if (formElementName.substring(0,portName.length)==portName){
					return new Array();
				}
			}
		}
	}

	return this.formFeedback.getFeedback(formElementName, value);
}

/**
 * Returns the form feedback object
 * @private
 * @returns {Object}  The form feedback object of this operation, see feedback.js
 */
draw2d.Operation.prototype._getFormFeedbackObject = function(){
	return this.formFeedback;
}


/**
 * Sets the form feedback object
 * @private
 * @param {Object}  formFeedback The form feedback object, see feedback.js
 */
draw2d.Operation.prototype._setFormFeedbackObject = function(formFeedback){
	this.formFeedback = formFeedback;
}

/**
 * This function is called before this object is deleted, if the function returns false,
 *  the deletion is aborted.
 * @public
 * @returns {Boolean}
 **/
draw2d.Operation.prototype.onBeforeDelete = function(){
	return this.fireEvent('onbeforedelete');
}

/**
 * This function is called before this object is deleted, if the function returns false,
 *  the deletion is aborted.
 * @public
 * @returns {Boolean}
 **/
draw2d.Operation.prototype.onDelete = function(){
	if (this.onBeforeDelete==true){

	}
	this.fireEvent('ondelete');
}


/**
 * This function is called when this operation is selected
 * @public
 **/
draw2d.Operation.prototype.onSelect = function(){
	this.fireEvent('onselect');
}

/**
 * This function is called when this operation is double clicked
 * @returns  {boolean}
 **/
draw2d.Operation.prototype.onDoubleClick = function(){
	/* tell the listeners */
	return this.fireEvent('ondoubleclick');
}

/**
 * Sets the operation of which this operation is a clone
 * @public
 * @param {Object}  operation  Operation object
 */
draw2d.Operation.prototype.setCloneMother = function(operation){
	this.cloneMother = operation;
}

/**
 * Returns the operation of which this operation is cloned
 * @public
 * @returns  {Object|undefined}  The operation object (if available)
 */
draw2d.Operation.prototype.getCloneMother = function(){
	return this.cloneMother;
}

/**
 * Sets the function for the onKeyDown event, this function can have two parameters keyCode and ctrl,
 *  see draw2d.Figure
 */
draw2d.Operation.prototype.onKeyDown = function(keyCode, ctrl){
	if (keyCode == 46){
		if (this.onBeforeDelete()==true){
			draw2d.Figure.prototype.onKeyDown.call(this, keyCode, ctrl);
			this.onDelete();
		}
	}else{
		draw2d.Figure.prototype.onKeyDown.call(this, keyCode, ctrl);
	}
}

/**
 * Sets the dimensions of the operation, and changes the positions of the ports accordingly
 * @param  {integer}  w         The width
 * @param  {integer}  h         The height
 * @param  {boolean}  animated  If set to true, the dimensions will be set in an animated fashion
 **/
draw2d.Operation.prototype.setDimension=function(/*:int*/ w, /*:int*/ h, animated)
{
	/* first resize the operation */
	this.width = Math.max(this.getMinWidth(),w);
  this.height= Math.max(this.getMinHeight(),h);

  // Falls das Element noch nie gezeichnet wurde, dann braucht aus das HTML nicht
  // aktualisiert werden
  //
  if(this.html==null)
    return;

	if (animated==undefined || animated==false){
		this.html.style.width  = this.width+"px";
		this.html.style.height = this.height+"px";
	}else{
		//let mootools make something smooth
		document.id(this.html).morph({"width": this.width, "height": this.height});
	}

  this.fireMoveEvent();

  // Update the resize handles if the user change the dimension via an API call
  //
  if(this.workflow!=null && this.workflow.getCurrentSelection()==this)
  {
     this.workflow.showResizeHandles(this);
  }

	/* move the ports */
	var i, port;
	var inputPortCounter  = 0;
	var outputPortCounter = 0;
	var ports         = this.getPorts();

	var numberOfPorts = ports.getSize();

	for (i=0; i<numberOfPorts; i++){
		port = ports.get(i);
		if (port.type == 'OutputPort'){
				port.setPosition((this.width/2)-(this.portsTotalWidth/2)+(outputPortCounter*(this.portsTotalWidth/this.numberOut))+((this.portsTotalWidth/this.numberOut)/2), this.height);
				outputPortCounter++;
		}else{
			  port.setPosition((this.width/2)-(this.portsTotalWidth/2)+(inputPortCounter*(this.portsTotalWidth/this.numberIn))+((this.portsTotalWidth/this.numberIn)/2), 0);
				inputPortCounter++;
		}
	}

	/* call event */
	this.onResizeend();
}

/**
 * Stores a location/dimension object
 * @param  {string}  locationType  The name of the location-object
 * @param  {string}  x             The x position
 * @param  {string}  y             The y position
 * @param  {string}  width         The width
 * @param  {string}  height        The height
 * @param  {boolean} storeOnly     Whether the operations dimensions/location should be set in the HTML DOM. Default: false;
 * @return Nothing
 */
draw2d.Operation.prototype.setLocationAndDimensions = function(locationType, x, y, width, height, storeOnly){

	this.locationDimensions[locationType] = {"x"      : x,
	                                         "y"      : y,
																					 "width"  : width,
																					 "height" : height
																				  };

	/* set position and dimensions */
	if (storeOnly == undefined || storeOnly==false){
		this.setPosition(parseInt(x), parseInt(y));
		this.setDimension(parseInt(width), parseInt(height));
	}

	this.setProperty('locationDimension_'+locationType, this.locationDimensions[locationType]);
}

/**
 * Returns a location/dimension object
 * @param  {string}   locationType  The name of the location-object
 * @see #draw2d.Operation.prototype.setLocationAndDimension
 * @return  A location/dimension {"x": string, "y": string, "width": string, "height": string}
 * @type  object
 */
draw2d.Operation.prototype.getLocationAndDimensions = function(locationType){

	return this.locationDimensions[locationType];
}



/**
 * Determines whether operation is resizeable in x direction
 * @param  {boolean}  value  If true, the operation is resizeable in x direction
 **/
draw2d.Operation.prototype.setResizeableX = function(value){
	this.resizableX = value;
}

/**
 * Determines whether operation is resizeable in y direction
 * @param  {boolean}  value  If true, the operation is resizeable in y direction
 **/
draw2d.Operation.prototype.setResizeableY = function(value){
	this.resizableY = value;
}

/**
 * Determines whether operation is resizeable in x direction
 * @returns  {boolean}  true or false
 **/
draw2d.Operation.prototype.isResizeableX = function(){
	return this.resizableX;
}

/**
 * Determines whether operation is resizeable in y direction
 * @returns  {boolean}  true or false
 **/
draw2d.Operation.prototype.isResizeableY = function(){
	return this.resizableY;
}

/**
 * Sets the start and end dates of this operation in the property window form
 **/
draw2d.Operation.prototype.setFormDateTime = function(){
	var format = '%a %d %b %H:%M'; //see http://mootools.net/docs/more/Native/Date#Date:format

	/* calculate the new date/time span of this operation */
	var startDateTime = this.workflow.dateTimeBar.getDateTimeFromPos(this.getPosition().y, format);
	var endDateTime   = this.workflow.dateTimeBar.getDateTimeFromPos(this.getPosition().y+this.getHeight(), format);

	/* set the form value */
	this.propertiesWindow.setFormWhen(startDateTime,endDateTime);
}

/**
 * Change the appearance of the operation
 * @param  {Integer}  viewNr  The appearance type
 **/
draw2d.Operation.prototype.choseView = function(viewNr){
	return;

	/* shows property form inside operation */


	var background, backgroundAlt, j,k,portDivs;
	var numberOfPorts, port, ports, portContent, backgroundImageURL;

	switch (viewNr){
		case 1:
			/* shrink */
			this.portsTotalWidth = 90;
			this.setDimension(80, 80, true);
			this.setLocationAndDimensions ('locationType', this.getPosition().x, this.getPosition().y, 80, 80, true);
			if (this.smallContent!=undefined){
				this.content.innerHTML = this.smallContent;
			}

			/* change ports */
			ports         = this.getPorts();
			numberOfPorts = ports.getSize();

			for (j=0; j<numberOfPorts; j++){
				port = ports.get(j);
				portContent = document.id(port.html).getChildren()[0];
				portContent.innerHTML = '';
				portContent.parentNode.morph({'width': 40, 'height':10});
				portContent.morph({'width': 40, 'height':10});
				portContent.setStyle('opacity', '1');
				portContent.removeEvents('click');
			}

			break;

		case 2:
			/* grow */
			//contentWrapper = document.id(this.html);
			//contentWrapper.morph({'height': 400, 'width': 300, 'opacity': .7});

			/* increase the ports total width, so the ports will be nicely positioned while dragging */
			this.portsTotalWidth = 250;

			/* smoothly set the new dimensions of the operation */
			this.setDimension(400, 300, true);
			this.setLocationAndDimensions ('locationType', this.getPosition().x, this.getPosition().y, 400, 300, true);

			/* change the innerHTML */

			//save current innerHTML
			this.smallContent = this.content.innerHTML;

			//set new one
			//document.id('dialogOperationHeader').innerHTML = this.getName()+' properties';
			this.content.innerHTML = document.id('temp').innerHTML;

			//document.id('operationPropertiesTitle').innerHTML = this.title;


			/* change ports */
			ports         = this.getPorts();
			numberOfPorts = ports.getSize();

			for (j=0; j<numberOfPorts; j++){
				port = ports.get(j);

				/* get port content html */
				portContent = document.id(port.html).getChildren()[0];

				//give it a new size
				portContent.parentNode.morph({'width': 80, 'height':20});
				portContent.morph({'width': 80, 'height':20});
				portContent.setStyle('opacity', '0.25');
				portContent.parentNode.setStyle('background-color', 'gray');

				//add an image == background image of port
				backgroundImageURL = portContent.style.backgroundImage.match(new RegExp("("+ED_UI_ELEMENTS_DIR+"[^\)]+)"))[0];
				portContent.innerHTML = '<img src="'+backgroundImageURL+'" width="80" height="20" />';

				//add an onclick event which toggles the tab for this port
				portContent.addEvent('click', function(){
					document.id('operationPortProperties').style.display = 'block';
					portDivs = document.id('operationPortProperties').getChildren('div');
					for (k=0; k<portDivs.length; k++){
						portDivs[k].style.display = 'none';

						ports         = this.parentNode.getPorts();
						numberOfPorts = ports.getSize();
						for (var j=0; j<numberOfPorts; j++){
							port = ports.get(j);
							portContent = document.id(port.html).getChildren()[0];
							if (port!=this){
								portContent.setStyle('opacity', '0.25');
								portContent.parentNode.setStyle('background-color', 'gray');
							}else{
								portContent.setStyle('opacity', '1');
							}
						}

						//blur operation Tab
						document.id('operationTab').style.backgroundColor = '#7698B4';

					}

					document.id('operationPortProperties'+this.getName()).style.display = 'block';
					document.id('operationProperties').style.display = 'none';

				}.bind(port));

				/* add form for each port */
				document.id('operationPortProperties').innerHTML += '<div style="display: none" id="operationPortProperties'+port.getName()+'">'+this.propertiesWindow.IOFormTemplate.replace(/%direction%/ig, port.getName())+'</div>';

			}


			break;
	}

	/* change the appearance of the link  which triggers this function */
	background    = this.toggleVisibilityLink.getStyle('background');
	backgroundAlt = this.toggleVisibilityLink.getProperty('backgroundAlt');

	this.toggleVisibilityLink.setStyle('background', backgroundAlt);
	this.toggleVisibilityLink.setProperty('backgroundAlt', background);

	this.config.viewNr = viewNr;
}

draw2d.Operation.prototype.setId = function(id){
	this.id=id;
  if(this.html!=null){
     this.html.id = id;
	}
}



/**
 * Lets the user know something went wrong
 * @param {mixed}  error  The error message as a string, or an object {
 *                                                                      errorType: int
 *                                                                      (other properties)
 *                                                                    }
 **/
draw2d.Operation.prototype.throwUserError = function(error){
	var i, message, stackString;

	if (typeof error == 'string'){
		/* normal error */
		message = error;
	}else{
		/* more complex error, generate message based on type */
		switch (error.errorType){
			case 1:
				/* user wants to create circular reference */
				stackString = 'This method -> Method A --> ... --> Method B --> This method';
				for (i=0; i<error.stack.length; i++){
					if (i!=0){
						stackString += ' -> ';
					}
					stackString += error.stack[i].getTitle().replace(/[\n\r\t]/g, ' ');
				}

				stackString += ' -> This method';

				message = 'You cannot connect to this operation, because it would create a circulair reference.\n\n'+stackString;
			break;
		}
	}

	alert(message);
}

/**
 * Adds a event listener
 * @param   {string}  type            The event, e.g. 'onclick', 'onselect' etc
 * @param   {object}  functionObject  The function to call when the event is fired
 * @returns {object} The functionObject
 **/
draw2d.Operation.prototype.addEventListener = function(type, functionObject){
	return this.eventListenerFactory.add(type, functionObject);
}

/**
 * Tells all listeners an event has occured
 * @param    {string}  type            The event, e.g. 'onclick', 'onselect' etc
 * @returns  {boolean} True if all assigned functions return undefined or true, false if one of them
 *                       returns false
 **/
draw2d.Operation.prototype.fireEvent = function(type){
	return this.eventListenerFactory.fire(type,  arguments);
}

/**
 * Returns connected operations
 * @param  {object}  config            A JSON object: {'direction': 'upstream' or 'downstream',
 *                                                     'depth'    : integer}
 * @param  {object}  _iterationObject  (private) A JSON object: {'stack'     : [{'operation' : operation object,
 *                                                                               'distance'  : integer //distance from start operation}],
 *                                                               '_distances': array(operationId: operation distance)}
 * @returns {obect}  The iteration object
 **/
draw2d.Operation.prototype.getConnectedOperations = function(config, _iterationObject){
	var ports, numberOfPorts, port, j, connections, nextPort, numberOfConnections;
	var connection, nextOperation, k;
	_iterationObject = _iterationObject == undefined ? {} : _iterationObject;
	config           = config           == undefined ? {} : config;

	/* create stack array */
	_iterationObject.stack = _iterationObject.stack == undefined ? new Array() : _iterationObject.stack;

	/* create distance array */
	_iterationObject._distances               = _iterationObject._distances               == undefined ? new Array() : _iterationObject._distances;
	_iterationObject._distances[this.getId()] = _iterationObject._distances[this.getId()] == undefined ? 0           : _iterationObject._distances[this.getId()];

	/* in what direction to look? */
	var direction = config.direction==undefined ? 'downstream' : config.direction;

	//add this operation to the stack
	if (_iterationObject.iterationCount!=undefined){
		_iterationObject.stack[_iterationObject.iterationCount] = {'operation':this, 'distance':_iterationObject._distances[this.getId()]};
	}

	//determine the distance between the starting operation and this operation
	_iterationObject.iterationCount = _iterationObject.iterationCount == undefined ? 0 : _iterationObject.iterationCount+1;

	/* loop over all ports, get connections and operations connected to connections */
	ports         = this.getPorts();
	numberOfPorts = ports.getSize();

	for (j=0; j<numberOfPorts; j++){
		/* get connections */
		port                = ports.get(j);
		connections         = port.getConnections();
		numberOfConnections = connections.getSize();

		for (k=0; k<numberOfConnections; k++){
			nextPort   = false;
			connection = connections.get(k);

			if (direction=='upstream' && port.type == 'InputPort'){
				/* get the port to which this connection leads */
				nextPort = connection.getTarget() == port ? connection.getSource() : connection.getTarget(); //sourceport depends on whether the user connected operation1.outputPort to operation2.inputPort or vice versa
			}

			if(direction=='downstream' && port.type == 'OutputPort'){
				/* get the port to which this connection leads */
				nextPort = connection.getTarget() == port ? connection.getSource() : connection.getTarget(); //sourceport depends on whether the user connected operation1.outputPort to operation2.inputPort or vice versa
			}

			/* get operations connected to next operation */
			if (nextPort!=false){
				nextOperation = nextPort.getParent();

				//set distance between starter operation and next operation
				_iterationObject._distances[nextOperation.getId()] = _iterationObject._distances[this.getId()]+1;

				if (config.depth==undefined || _iterationObject._distances[nextOperation.getId()]<=config.depth){
					//see whether next operation also has connected operations
					_iterationObject = nextOperation.getConnectedOperations(config, _iterationObject);
				}
			}
		}
	}

	/* reset distance */
	return _iterationObject;
}

/**
 * Removes a event listener
 * @param  {string}  type            The event, e.g. 'onclick', 'onselect' etc
 * @param  {string}  functionObject  The functionObject assigned in addEventListener
 **/
draw2d.Operation.prototype.removeEventListener = function(type, functionObject){
	return this.eventListenerFactory.remove(type, functionObject);
}

/**
 * Sets the users assigned to this operation
 * @param {array}  assignedUserNrs     The indexes of the assigned users in this.worfklow.users
 **/
draw2d.Operation.prototype.setAssignedUsers = function(assignedUserNrs){
	/* each user has a div inside the operation html, this div should be showed when
	 * the user is assigned to this operation
	 **/
	var i;
	var assignedUser          = 0;
	var numberOfAssignedUsers = assignedUserNrs.length;
	var numberOfUsers         = this.workflow.users.length;

	/* set user divs for assigned users */
	for (i=0; i<numberOfUsers; i++){
		if (assignedUserNrs.contains(i)){
			/* user is assigned, show its div */
			this.userDivs[i].setStyles({
				'display' : 'block',
				'opacity' : '0.4',
				'width'   : 10/numberOfAssignedUsers,
				'left'   : assignedUser*(10/numberOfAssignedUsers)-1
			});
			assignedUser++;
		}else{
			/* user is not assigned, hide its div */
			this.userDivs[i].setStyles({
				'display' : 'none'
			});
		}
	}

	this.assignedUserNrs = assignedUserNrs;
}

draw2d.Operation.prototype.addAssignedUser = function(assignedUserNr){
	if (!this.assignedUserNrs.contains(assignedUserNr)){
		this.assignedUserNrs[this.assignedUserNrs.length] = assignedUserNr;
	}

	/* update ui */
	this.setAssignedUsers(this.assignedUserNrs);
}

draw2d.Operation.prototype.removeAssignedUser = function(assignedUserNr){
	var index = this.assignedUserNrs.contains(assignedUserNr);
	if (!this.assignedUserNrs.contains(assignedUserNr)){
		delete this.assignedUserNrs[index];
	}

	/* update ui */
	this.setAssignedUsers(this.assignedUserNrs);
}

/**
 * Changes the GUI according to the status of the operation
 * @param  {string}   status  The status, or false when going back to the default GUI
 */

draw2d.Operation.prototype.setStatus = function(status){
	if (!(status == false || status==undefined || status=='undefined') && status==this.status) return; //only set new status

	status = (status == false || status==undefined || status=='undefined') ? 'notstarted' : status;

	var operationContentWrapper = this.html.getElements('.operationContentWrapper');
	var operationContent        = this.html.getElements('.operationContent');
	this.status = status;


	if (status!='notstarted'){
		operationContentWrapper.setStyle('background-image', 'url('+this.guiPath+status+'.png)');
		operationContentWrapper.setStyle('background-repeat', 'no-repeat');
		operationContentWrapper.setStyle('background-position', 'center center');
		operationContent.setStyle('opacity', '0.5');
	}else{
		operationContentWrapper.setStyle('background-image', '');
		operationContent.setStyle('opacity', '1.0');
	}

	this.fireEvent('onsetstatus');
}

/**
 * Returns the status of the operation, e.g. 'busy',  'finished', see draw2d.Operation.prototype.setStatus
 * @returns {string} The status
 */
draw2d.Operation.prototype.getStatus = function(){
	if (this.status==undefined) this.setStatus(); //set default status

	return this.status;
}

draw2d.Operation.prototype.getTimeNeeded = function(){
	var value = (this.config.timeNeeded == undefined || this.config.timeNeeded=='') ? 0 : parseInt(this.config.timeNeeded);
	return value;
}
