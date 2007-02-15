var _COLLAPSED_ = "COLLAPSED_";
var _EXPANDED_ = "EXPANDED_";

var _IMAGES_ = "./images/";
var _IMG_COLLAPSED_ = _IMAGES_+"collapsed.gif";
var _IMG_FIRST_COLLAPSED_ = _IMAGES_+"firstCollapsed.gif";
var _IMG_LAST_COLLAPSED_ = _IMAGES_+"lastCollapsed.gif";
var _IMG_ROOT_COLLAPSED_ = _IMAGES_+"rootCollapsed.gif";
var _IMG_EXPANDED_ = _IMAGES_+"expanded.gif";
var _IMG_FIRST_EXPANDED_ = _IMAGES_+"firstExpanded.gif";
var _IMG_LAST_EXPANDED_ = _IMAGES_+"lastExpanded.gif";
var _IMG_ROOT_EXPANDED_ = _IMAGES_+"rootExpanded.gif";
var _IMG_ITEM_LINE = _IMAGES_+"itemLine.gif";
var _IMG_LAST_ITEM_LINE = _IMAGES_+"lastItemLine.gif";
var _IMG_OPENED_FOLDER_ = _IMAGES_+"openedFolder.gif";
var _IMG_CLOSED_FOLDER_ = _IMAGES_+"closedFolder.gif";
var _IMG_CHECKED_ = _IMAGES_+"checked.gif";
var _IMG_SOME_CHECKED_ = _IMAGES_+"someChecked.gif";
var _IMG_UNCHECKED_ = _IMAGES_+"unchecked.gif";

var ar1 = new Array();

function getRoot(treeNode){
	var parentNode = treeNode.parentNode;
	if (parentNode.className == "root"){
		return parentNode;
	}
	else{
		return getRoot(parentNode);
	}
}

function isLastInGroup(treeNode){
	var nextNode = null;
	var curNode = treeNode;
	while ( (nextNode=curNode.nextSibling) != null){
		if(nextNode.nodeName == "DIV"){
			return false;
		}
		curNode = nextNode;
	}
	return true;
}

function hideIt(treeNode, deep){
	for (var i=0; i<treeNode.childNodes.length; i++){
		var curN = treeNode.childNodes.item(i);
		var isLast = isLastInGroup(treeNode).toString();
		treeNode.setAttribute("isLast", isLast);
		var className = curN.className;
		var nodeName = curN.nodeName;
		if(nodeName == "DIV" && className!=""){
			deep++;
			hideIt(curN, deep);
			deep--;
		}
		if(className == "toggler"){
			if(isLast=="true"){
				curN.src = _IMG_LAST_COLLAPSED_;
			}
		}
		if(className == "itemLine"){
			if(isLast=="true"){
				curN.src = _IMG_LAST_ITEM_LINE;
			}
		}
		if(className == "treeLine"){
			var depth = curN.getAttribute("depth");
			if (isLast != null){
				if (isLast == "true"){
					ar1[deep] = "hidden";
				}
				else{
					ar1[deep] = "visible";
				}
			}
			if (depth != null){
				if(depth == "0"){
					curN.style.visibility="hidden";
				}
				else{
					var visibility = new String(ar1[depth]);
					curN.style.visibility=visibility;
				}
			}
		}
	}
}

function initTree(){
	var body = window.document.body;
	var root = body.getElementsByTagName("DIV").item(0);
	var deep = new Number(0);
	hideIt(root, deep);
}

function getToggledImage(treeNode, mode){
	var isLast = treeNode.getAttribute("isLast");
	if(treeNode.className == "root"){
		return eval("_IMG_ROOT_"+mode);
	}
	if (isLast=="true"){
		return eval("_IMG_LAST_"+mode);
	}
	return eval("_IMG_"+mode);
}

function toggleDisplay(node, display){
	for(var i=0; i<node.childNodes.length; i++){
		var curN = node.childNodes.item(i);
		if (curN.nodeName == "DIV"){
			curN.style.display=display;
		}
	}
}

function toggle(element){
	var treeNode = element.parentNode;
	var isExpanded = treeNode.getAttribute("expanded");
	var folderImg = getFolderImage(treeNode);
	if (isExpanded=="true"){
		element.src=getToggledImage(element.parentNode, _COLLAPSED_);
		treeNode.setAttribute("expanded", "false");
		toggleDisplay(treeNode, "none");
	}
	else{
		element.src=getToggledImage(element.parentNode, _EXPANDED_);
		treeNode.setAttribute("expanded", "true");
		toggleDisplay(treeNode, "block");
	}
}

function recheckNested(treeNode, checked, image){
	var children = treeNode.childNodes;
	for(var i=0; i<children.length; i++){
		var curC = children.item(i);
		var nodeName = curC.nodeName;
		if(nodeName == "DIV"){
			var images = curC.getElementsByTagName("img");
			for(var j=0; j<images.length; j++){
				var curI = images.item(j);
				if(curI.className == "checkbox"){
					recheckIt(curI, checked, image);
				}
			}
		}
	}
}

function getFolderImage(treeNode){
	var images = treeNode.getElementsByTagName("img");
	for(var i=0; i<images.length; i++){
		var curI = images.item(i);
		if(curI.className == "folder"){
			return curI;
		}
	}
	return null;
}

function getCheckbox(treeNode){
	var images = treeNode.getElementsByTagName("img");
	for(var i=0; i<images.length; i++){
		var curI = images.item(i);
		if(curI.className == "checkbox"){
			return curI;
		}
	}
	return null;
}

function recheckIt(checkbox, checked, image){
	checkbox.src = image;
	checkbox.setAttribute("checked", checked);
}

function isChecked(treeNode){
	var checked = false;
	var images = treeNode.getElementsByTagName("img");
	var checkbox = getCheckbox(treeNode);
	if (checkbox.getAttribute("checked") == "true"){
		checked = true;
	}
	else{
		checked = false;
	}
	return checked;
}

function recheckParents(currentNode){
	var fullyChecked = true;
	var someChecked = false;
	var parentNode = currentNode.parentNode;
	if (parentNode.nodeName == "BODY"){
		return;
	}
	var children = parentNode.getElementsByTagName("DIV");
	for(var i=0; i<children.length; i++){
		var curC = children.item(i);
		var checked = isChecked(curC);
		if (checked){
			someChecked = true;
		}
		fullyChecked = Boolean(fullyChecked & checked);
	}
	if(fullyChecked==false && someChecked==true){
		recheckIt(getCheckbox(parentNode), "false", _IMG_SOME_CHECKED_);
	}
	if(fullyChecked==true && someChecked==true){
		recheckIt(getCheckbox(parentNode), "true", _IMG_CHECKED_);
	}
	if(fullyChecked==false && someChecked==false){
		recheckIt(getCheckbox(parentNode), "false", _IMG_UNCHECKED_);
	}
	recheckParents(parentNode);
}

function recheck(element){
	var image = null;
	var treeNode = element.parentNode;
	var checkedClass = treeNode.className;	
	var checked = element.getAttribute("checked");
	if (checked == "true"){
		checked = "false";
		image = _IMG_UNCHECKED_;
	}
	else{
		checked = "true";
		image = _IMG_CHECKED_;
	}
	recheckIt(element, checked, image);
	if (checkedClass != "item"){
		recheckNested(treeNode, checked, image);
	}
	recheckParents(treeNode);
}

function selectThisTreeNode(element){
	var root = getRoot(element);
	recheckIt(getCheckbox(root), "false", _IMG_UNCHECKED_);
	recheckNested(root, "false", _IMG_UNCHECKED_);

	var treeNode = element.parentNode;
	recheckIt(getCheckbox(treeNode), "true", _IMG_CHECKED_);
	if (treeNode.className != "item"){
		recheckNested(treeNode, "true", _IMG_CHECKED_);
	}
	recheckParents(treeNode);
}
