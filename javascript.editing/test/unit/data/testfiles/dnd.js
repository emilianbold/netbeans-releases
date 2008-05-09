/**
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://woodstock.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://woodstock.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */


webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dnd");

// Dojo.lang.extend test
dojo.lang.extend(webui.Foo, {
	construct: function(){  
        }
});

/*
	Copyright (c) 2004-2007, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/book/dojo-book-0-9/introduction/licensing
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
*/

if(!webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.common"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.common"] = true;
webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dojo.dnd.common");

webui.suntheme4_2.dojo.dnd._copyKey = navigator.appVersion.indexOf("Macintosh") < 0 ? "ctrlKey" : "metaKey";

webui.suntheme4_2.dojo.dnd.getCopyKeyState = function(e) {
	// summary: abstracts away the difference between selection on Mac and PC,
	//	and returns the state of the "copy" key to be pressed.
	// e: Event: mouse event
	return e[webui.suntheme4_2.dojo.dnd._copyKey];	// Boolean
};

webui.suntheme4_2.dojo.dnd._uniqueId = 0;
webui.suntheme4_2.dojo.dnd.getUniqueId = function(){
	// summary: returns a unique string for use with any DOM element
	var id;
	do{
		id = "dojoUnique" + (++webui.suntheme4_2.dojo.dnd._uniqueId);
	}while(webui.suntheme4_2.dojo.byId(id));
	return id;
};

webui.suntheme4_2.dojo.dnd._empty = {};

webui.suntheme4_2.dojo.dnd.isFormElement = function(/*Event*/ e){
	// summary: returns true, if user clicked on a form element
	var t = e.target;
	if(t.nodeType == 3 /*TEXT_NODE*/){
		t = t.parentNode;
	}
	return " button textarea input select option ".indexOf(" " + t.tagName.toLowerCase() + " ") >= 0;	// Boolean
};

}

if(!webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.autoscroll"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.autoscroll"] = true;
webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dojo.dnd.autoscroll");

webui.suntheme4_2.dojo.dnd.getViewport = function(){
	// summary: returns a viewport size (visible part of the window)

	// FIXME: need more docs!!
	var d = webui.suntheme4_2.dojo.doc, dd = d.documentElement, w = window, b = webui.suntheme4_2.dojo.body();
	if(webui.suntheme4_2.dojo.isMozilla){
		return {w: dd.clientWidth, h: w.innerHeight};	// Object
	}else if(!webui.suntheme4_2.dojo.isOpera && w.innerWidth){
		return {w: w.innerWidth, h: w.innerHeight};		// Object
	}else if (!webui.suntheme4_2.dojo.isOpera && dd && dd.clientWidth){
		return {w: dd.clientWidth, h: dd.clientHeight};	// Object
	}else if (b.clientWidth){
		return {w: b.clientWidth, h: b.clientHeight};	// Object
	}
	return null;	// Object
};

webui.suntheme4_2.dojo.dnd.V_TRIGGER_AUTOSCROLL = 32;
webui.suntheme4_2.dojo.dnd.H_TRIGGER_AUTOSCROLL = 32;

webui.suntheme4_2.dojo.dnd.V_AUTOSCROLL_VALUE = 16;
webui.suntheme4_2.dojo.dnd.H_AUTOSCROLL_VALUE = 16;

webui.suntheme4_2.dojo.dnd.autoScroll = function(e){
	// summary:
	//		a handler for onmousemove event, which scrolls the window, if
	//		necesary
	// e: Event:
	//		onmousemove event

	// FIXME: needs more docs!
	var v = webui.suntheme4_2.dojo.dnd.getViewport(), dx = 0, dy = 0;
	if(e.clientX < webui.suntheme4_2.dojo.dnd.H_TRIGGER_AUTOSCROLL){
		dx = -webui.suntheme4_2.dojo.dnd.H_AUTOSCROLL_VALUE;
	}else if(e.clientX > v.w - webui.suntheme4_2.dojo.dnd.H_TRIGGER_AUTOSCROLL){
		dx = webui.suntheme4_2.dojo.dnd.H_AUTOSCROLL_VALUE;
	}
	if(e.clientY < webui.suntheme4_2.dojo.dnd.V_TRIGGER_AUTOSCROLL){
		dy = -webui.suntheme4_2.dojo.dnd.V_AUTOSCROLL_VALUE;
	}else if(e.clientY > v.h - webui.suntheme4_2.dojo.dnd.V_TRIGGER_AUTOSCROLL){
		dy = webui.suntheme4_2.dojo.dnd.V_AUTOSCROLL_VALUE;
	}
	window.scrollBy(dx, dy);
};

webui.suntheme4_2.dojo.dnd._validNodes = {"div": 1, "p": 1, "td": 1};
webui.suntheme4_2.dojo.dnd._validOverflow = {"auto": 1, "scroll": 1};

webui.suntheme4_2.dojo.dnd.autoScrollNodes = function(e){
	// summary:
	//		a handler for onmousemove event, which scrolls the first avaialble
	//		Dom element, it falls back to webui.suntheme4_2.dojo.dnd.autoScroll()
	// e: Event:
	//		onmousemove event

	// FIXME: needs more docs!
	for(var n = e.target; n;){
		if(n.nodeType == 1 && (n.tagName.toLowerCase() in webui.suntheme4_2.dojo.dnd._validNodes)){
			var s = webui.suntheme4_2.dojo.getComputedStyle(n);
			if(s.overflow.toLowerCase() in webui.suntheme4_2.dojo.dnd._validOverflow){
				var b = webui.suntheme4_2.dojo._getContentBox(n, s), t = webui.suntheme4_2.dojo._abs(n, true);
				// console.debug(b.l, b.t, t.x, t.y, n.scrollLeft, n.scrollTop);
				b.l += t.x + n.scrollLeft;
				b.t += t.y + n.scrollTop;
				var w = Math.min(webui.suntheme4_2.dojo.dnd.H_TRIGGER_AUTOSCROLL, b.w / 2), 
					h = Math.min(webui.suntheme4_2.dojo.dnd.V_TRIGGER_AUTOSCROLL, b.h / 2),
					rx = e.pageX - b.l, ry = e.pageY - b.t, dx = 0, dy = 0;
				if(rx > 0 && rx < b.w){
					if(rx < w){
						dx = -webui.suntheme4_2.dojo.dnd.H_AUTOSCROLL_VALUE;
					}else if(rx > b.w - w){
						dx = webui.suntheme4_2.dojo.dnd.H_AUTOSCROLL_VALUE;
					}
				}
				//console.debug("ry =", ry, "b.h =", b.h, "h =", h);
				if(ry > 0 && ry < b.h){
					if(ry < h){
						dy = -webui.suntheme4_2.dojo.dnd.V_AUTOSCROLL_VALUE;
					}else if(ry > b.h - h){
						dy = webui.suntheme4_2.dojo.dnd.V_AUTOSCROLL_VALUE;
					}
				}
				var oldLeft = n.scrollLeft, oldTop = n.scrollTop;
				n.scrollLeft = n.scrollLeft + dx;
				n.scrollTop  = n.scrollTop  + dy;
				// if(dx || dy){ console.debug(oldLeft + ", " + oldTop + "\n" + dx + ", " + dy + "\n" + n.scrollLeft + ", " + n.scrollTop); }
				if(oldLeft != n.scrollLeft || oldTop != n.scrollTop){ return; }
			}
		}
		try{
			n = n.parentNode;
		}catch(x){
			n = null;
		}
	}
	webui.suntheme4_2.dojo.dnd.autoScroll(e);
};

}

if(!webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Avatar"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Avatar"] = true;
webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dojo.dnd.Avatar");



webui.suntheme4_2.dojo.dnd.Avatar = function(manager){
	// summary: an object, which represents transferred DnD items visually
	// manager: Object: a DnD manager object
	this.manager = manager;
	this.construct();
};

webui.suntheme4_2.dojo.extend(webui.suntheme4_2.dojo.dnd.Avatar, {
	construct: function(){
		// summary: a constructor function;
		//	it is separate so it can be (dynamically) overwritten in case of need
		var a = webui.suntheme4_2.dojo.doc.createElement("table");
		a.className = "dojoDndAvatar";
		a.style.position = "absolute";
		a.style.zIndex = 1999;
		a.style.margin = "0px"; // to avoid webui.suntheme4_2.dojo.marginBox() problems with table's margins
		var b = webui.suntheme4_2.dojo.doc.createElement("tbody");
		var tr = webui.suntheme4_2.dojo.doc.createElement("tr");
		tr.className = "dojoDndAvatarHeader";
		var td = webui.suntheme4_2.dojo.doc.createElement("td");
		td.innerHTML = this._generateText();
		tr.appendChild(td);
		webui.suntheme4_2.dojo.style(tr, "opacity", 0.9);
		b.appendChild(tr);
		var k = Math.min(5, this.manager.nodes.length);
		var source = this.manager.source;
		for(var i = 0; i < k; ++i){
			tr = webui.suntheme4_2.dojo.doc.createElement("tr");
			tr.className = "dojoDndAvatarItem";
			td = webui.suntheme4_2.dojo.doc.createElement("td");
			var node = source.creator ?
				// create an avatar representation of the node
				node = source._normalizedCreator(source.getItem(this.manager.nodes[i].id).data, "avatar").node :
				// or just clone the node and hope it works
				node = this.manager.nodes[i].cloneNode(true);
			node.id = "";
			td.appendChild(node);
			tr.appendChild(td);
			webui.suntheme4_2.dojo.style(tr, "opacity", (9 - i) / 10);
			b.appendChild(tr);
		}
		a.appendChild(b);
		this.node = a;
	},
	destroy: function(){
		// summary: a desctructor for the avatar, called to remove all references so it can be garbage-collected
		webui.suntheme4_2.dojo._destroyElement(this.node);
		this.node = false;
	},
	update: function(){
		// summary: updates the avatar to reflect the current DnD state
		webui.suntheme4_2.dojo[(this.manager.canDropFlag ? "add" : "remove") + "Class"](this.node, "dojoDndAvatarCanDrop");
		// replace text
		var t = this.node.getElementsByTagName("td");
		for(var i = 0; i < t.length; ++i){
			var n = t[i];
			if(webui.suntheme4_2.dojo.hasClass(n.parentNode, "dojoDndAvatarHeader")){
				n.innerHTML = this._generateText();
				break;
			}
		}
	},
	_generateText: function(){
		// summary: generates a proper text to reflect copying or moving of items
		return this.manager.nodes.length.toString();
	}
});

}

if(!webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Manager"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Manager"] = true;
webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dojo.dnd.Manager");





webui.suntheme4_2.dojo.dnd.Manager = function(){
	// summary: the manager of DnD operations (usually a singleton)
	this.avatar  = null;
	this.source = null;
	this.nodes = [];
	this.copy  = true;
	this.target = null;
	this.canDropFlag = false;
	this.events = [];
};

webui.suntheme4_2.dojo.extend(webui.suntheme4_2.dojo.dnd.Manager, {
	// avatar's offset from the mouse
	OFFSET_X: 16,
	OFFSET_Y: 16,
	// methods
	overSource: function(source){
		// summary: called when a source detected a mouse-over conditiion
		// source: Object: the reporter
		if(this.avatar){
			this.target = (source && source.targetState != "Disabled") ? source : null;
			this.avatar.update();
		}
		webui.suntheme4_2.dojo.publish("/dnd/source/over", [source]);
	},
	outSource: function(source){
		// summary: called when a source detected a mouse-out conditiion
		// source: Object: the reporter
		if(this.avatar){
			if(this.target == source){
				this.target = null;
				this.canDropFlag = false;
				this.avatar.update();
				webui.suntheme4_2.dojo.publish("/dnd/source/over", [null]);
			}
		}else{
			webui.suntheme4_2.dojo.publish("/dnd/source/over", [null]);
		}
	},
	startDrag: function(source, nodes, copy){
		// summary: called to initiate the DnD operation
		// source: Object: the source which provides items
		// nodes: Array: the list of transferred items
		// copy: Boolean: copy items, if true, move items otherwise
		this.source = source;
		this.nodes  = nodes;
		this.copy   = Boolean(copy); // normalizing to true boolean
		this.avatar = this.makeAvatar();
		webui.suntheme4_2.dojo.body().appendChild(this.avatar.node);
		webui.suntheme4_2.dojo.publish("/dnd/start", [source, nodes, this.copy]);
		this.events = [
			webui.suntheme4_2.dojo.connect(webui.suntheme4_2.dojo.doc, "onmousemove", this, "onMouseMove"),
			webui.suntheme4_2.dojo.connect(webui.suntheme4_2.dojo.doc, "onmouseup",   this, "onMouseUp"),
			webui.suntheme4_2.dojo.connect(webui.suntheme4_2.dojo.doc, "onkeydown",   this, "onKeyDown"),
			webui.suntheme4_2.dojo.connect(webui.suntheme4_2.dojo.doc, "onkeyup",     this, "onKeyUp")
		];
		var c = "dojoDnd" + (copy ? "Copy" : "Move");
		webui.suntheme4_2.dojo.addClass(webui.suntheme4_2.dojo.body(), c); 
	},
	canDrop: function(flag){
		// summary: called to notify if the current target can accept items
		var canDropFlag = this.target && flag;
		if(this.canDropFlag != canDropFlag){
			this.canDropFlag = canDropFlag;
			this.avatar.update();
		}
	},
	stopDrag: function(){
		// summary: stop the DnD in progress
		webui.suntheme4_2.dojo.removeClass(webui.suntheme4_2.dojo.body(), "dojoDndCopy");
		webui.suntheme4_2.dojo.removeClass(webui.suntheme4_2.dojo.body(), "dojoDndMove");
		webui.suntheme4_2.dojo.forEach(this.events, webui.suntheme4_2.dojo.disconnect);
		this.events = [];
		this.avatar.destroy();
		this.avatar = null;
		this.source = null;
		this.nodes = [];
	},
	makeAvatar: function(){
		// summary: makes the avatar, it is separate to be overwritten dynamically, if needed
		return new webui.suntheme4_2.dojo.dnd.Avatar(this);
	},
	updateAvatar: function(){
		// summary: updates the avatar, it is separate to be overwritten dynamically, if needed
		this.avatar.update();
	},
	// mouse event processors
	onMouseMove: function(e){
		// summary: event processor for onmousemove
		// e: Event: mouse event
		var a = this.avatar;
		if(a){
			//webui.suntheme4_2.dojo.dnd.autoScrollNodes(e);
			webui.suntheme4_2.dojo.dnd.autoScroll(e);
			webui.suntheme4_2.dojo.marginBox(a.node, {l: e.pageX + this.OFFSET_X, t: e.pageY + this.OFFSET_Y});
			var copy = Boolean(this.source.copyState(webui.suntheme4_2.dojo.dnd.getCopyKeyState(e)));
			if(this.copy != copy){ 
				this._setCopyStatus(copy);
			}
		}
	},
	onMouseUp: function(e){
		// summary: event processor for onmouseup
		// e: Event: mouse event
		if(this.avatar && (!("mouseButton" in this.source) || this.source.mouseButton == e.button)){
			if(this.target && this.canDropFlag){
				var params = [this.source, this.nodes, Boolean(this.source.copyState(webui.suntheme4_2.dojo.dnd.getCopyKeyState(e))), this.target];
				webui.suntheme4_2.dojo.publish("/dnd/drop/before", params);
				webui.suntheme4_2.dojo.publish("/dnd/drop", params);
			}else{
				webui.suntheme4_2.dojo.publish("/dnd/cancel");
			}
			this.stopDrag();
		}
	},
	// keyboard event processors
	onKeyDown: function(e){
		// summary: event processor for onkeydown:
		//	watching for CTRL for copy/move status, watching for ESCAPE to cancel the drag
		// e: Event: keyboard event
		if(this.avatar){
			switch(e.keyCode){
				case webui.suntheme4_2.dojo.keys.CTRL:
					var copy = Boolean(this.source.copyState(true));
					if(this.copy != copy){ 
						this._setCopyStatus(copy);
					}
					break;
				case webui.suntheme4_2.dojo.keys.ESCAPE:
					webui.suntheme4_2.dojo.publish("/dnd/cancel");
					this.stopDrag();
					break;
			}
		}
	},
	onKeyUp: function(e){
		// summary: event processor for onkeyup, watching for CTRL for copy/move status
		// e: Event: keyboard event
		if(this.avatar && e.keyCode == webui.suntheme4_2.dojo.keys.CTRL){
			var copy = Boolean(this.source.copyState(false));
			if(this.copy != copy){ 
				this._setCopyStatus(copy);
			}
		}
	},
	// utilities
	_setCopyStatus: function(copy){
		// summary: changes the copy status
		// copy: Boolean: the copy status
		this.copy = copy;
		this.source._markDndStatus(this.copy);
		this.updateAvatar();
		webui.suntheme4_2.dojo.removeClass(webui.suntheme4_2.dojo.body(), "dojoDnd" + (this.copy ? "Move" : "Copy"));
		webui.suntheme4_2.dojo.addClass(webui.suntheme4_2.dojo.body(), "dojoDnd" + (this.copy ? "Copy" : "Move"));
	}
});

// summary: the manager singleton variable, can be overwritten, if needed
webui.suntheme4_2.dojo.dnd._manager = null;

webui.suntheme4_2.dojo.dnd.manager = function(){
	// summary: returns the current DnD manager, creates one if it is not created yet
	if(!webui.suntheme4_2.dojo.dnd._manager){
		webui.suntheme4_2.dojo.dnd._manager = new webui.suntheme4_2.dojo.dnd.Manager();
	}
	return webui.suntheme4_2.dojo.dnd._manager;	// Object
};

}

if(!webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.date.stamp"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.date.stamp"] = true;
webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dojo.date.stamp");

// Methods to convert dates to or from a wire (string) format using well-known conventions

webui.suntheme4_2.dojo.date.stamp.fromISOString = function(/*String*/formattedString, /*Number?*/defaultTime){
	//	summary:
	//		Returns a Date object given a string formatted according to a subset of the ISO-8601 standard.
	//
	//	description:
	//		Accepts a string formatted according to a profile of ISO8601 as defined by
	//		RFC3339 (http://www.ietf.org/rfc/rfc3339.txt), except that partial input is allowed.
	//		Can also process dates as specified by http://www.w3.org/TR/NOTE-datetime
	//		The following combinations are valid:
	// 			* dates only
	//				yyyy
	//				yyyy-MM
	//				yyyy-MM-dd
	// 			* times only, with an optional time zone appended
	//				THH:mm
	//				THH:mm:ss
	//				THH:mm:ss.SSS
	// 			* and "datetimes" which could be any combination of the above
	//		timezones may be specified as Z (for UTC) or +/- followed by a time expression HH:mm
	//		Assumes the local time zone if not specified.  Does not validate.  Improperly formatted
	//		input may return null.  Arguments which are out of bounds will be handled
	// 		by the Date constructor (e.g. January 32nd typically gets resolved to February 1st)
	//
  	//	formattedString:
	//		A string such as 2005-06-30T08:05:00-07:00 or 2005-06-30 or T08:05:00
	//
	//	defaultTime:
	//		Used for defaults for fields omitted in the formattedString.
	//		Uses 1970-01-01T00:00:00.0Z by default.

	if(!webui.suntheme4_2.dojo.date.stamp._isoRegExp){
		webui.suntheme4_2.dojo.date.stamp._isoRegExp =
//TODO: could be more restrictive and check for 00-59, etc.
			/^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2}):(\d{2}))|Z)?)?$/;
	}

	var match = webui.suntheme4_2.dojo.date.stamp._isoRegExp.exec(formattedString);
	var result = null;

	if(match){
		match.shift();
		match[1] && match[1]--; // Javascript Date months are 0-based
		match[6] && (match[6] *= 1000); // Javascript Date expects fractional seconds as milliseconds

		if(defaultTime){
			// mix in defaultTime.  Relatively expensive, so use || operators for the fast path of defaultTime === 0
			defaultTime = new Date(defaultTime);
			webui.suntheme4_2.dojo.map(["FullYear", "Month", "Date", "Hours", "Minutes", "Seconds", "Milliseconds"], function(prop){
				return defaultTime["get" + prop]();
			}).forEach(function(value, index){
				if(match[index] === undefined){
					match[index] = value;
				}
			});
		}
		result = new Date(match[0]||1970, match[1]||0, match[2]||0, match[3]||0, match[4]||0, match[5]||0, match[6]||0);

		var offset = 0;
		var zoneSign = match[7] && match[7].charAt(0);
		if(zoneSign != 'Z'){
			offset = ((match[8] || 0) * 60) + (Number(match[9]) || 0);
			if(zoneSign != '-'){ offset *= -1; }
		}
		if(zoneSign){
			offset -= result.getTimezoneOffset();
		}
		if(offset){
			result.setTime(result.getTime() + offset * 60000);
		}
	}

	return result; // Date or null
}

webui.suntheme4_2.dojo.date.stamp.toISOString = function(/*Date*/dateObject, /*Object?*/options){
	//	summary:
	//		Format a Date object as a string according a subset of the ISO-8601 standard
	//
	//	description:
	//		When options.selector is omitted, output follows RFC3339 (http://www.ietf.org/rfc/rfc3339.txt)
	//		The local time zone is included as an offset from GMT, except when selector=='time' (time without a date)
	//		Does not check bounds.
	//
	//	dateObject:
	//		A Date object
	//
	//	object {selector: string, zulu: boolean, milliseconds: boolean}
	//		selector- "date" or "time" for partial formatting of the Date object.
	//			Both date and time will be formatted by default.
	//		zulu- if true, UTC/GMT is used for a timezone
	//		milliseconds- if true, output milliseconds

	var _ = function(n){ return (n < 10) ? "0" + n : n; }
	options = options || {};
	var formattedDate = [];
	var getter = options.zulu ? "getUTC" : "get";
	var date = "";
	if(options.selector != "time"){
		date = [dateObject[getter+"FullYear"](), _(dateObject[getter+"Month"]()+1), _(dateObject[getter+"Date"]())].join('-');
	}
	formattedDate.push(date);
	if(options.selector != "date"){
		var time = [_(dateObject[getter+"Hours"]()), _(dateObject[getter+"Minutes"]()), _(dateObject[getter+"Seconds"]())].join(':');
		var millis = dateObject[getter+"Milliseconds"]();
		if(options.milliseconds){
			time += "."+ (millis < 100 ? "0" : "") + _(millis);
		}
		if(options.zulu){
			time += "Z";
		}else if(options.selector != "time"){
			var timezoneOffset = dateObject.getTimezoneOffset();
			var absOffset = Math.abs(timezoneOffset);
			time += (timezoneOffset > 0 ? "-" : "+") + 
				_(Math.floor(absOffset/60)) + ":" + _(absOffset%60);
		}
		formattedDate.push(time);
	}
	return formattedDate.join('T'); // String
}

}

if(!webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.parser"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.parser"] = true;
webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dojo.parser");


webui.suntheme4_2.dojo.parser = new function(){

	var d = webui.suntheme4_2.dojo;

	function val2type(/*Object*/ value){
		// summary:
		//		Returns name of type of given value.

		if(d.isString(value)){ return "string"; }
		if(typeof value == "number"){ return "number"; }
		if(typeof value == "boolean"){ return "boolean"; }
		if(d.isFunction(value)){ return "function"; }
		if(d.isArray(value)){ return "array"; } // typeof [] == "object"
		if(value instanceof Date) { return "date"; } // assume timestamp
		if(value instanceof d._Url){ return "url"; }
		return "object";
	}

	function str2obj(/*String*/ value, /*String*/ type){
		// summary:
		//		Convert given string value to given type
		switch(type){
			case "string":
				return value;
			case "number":
				return value.length ? Number(value) : NaN;
			case "boolean":
				// for checked/disabled value might be "" or "checked".  interpret as true.
				return typeof value == "boolean" ? value : !(value.toLowerCase()=="false");
			case "function":
				if(d.isFunction(value)){
					// IE gives us a function, even when we say something like onClick="foo"
					// (in which case it gives us an invalid function "function(){ foo }"). 
					//  Therefore, convert to string
					value=value.toString();
					value=d.trim(value.substring(value.indexOf('{')+1, value.length-1));
				}
				try{
					if(value.search(/[^\w\.]+/i) != -1){
						// TODO: "this" here won't work
						value = d.parser._nameAnonFunc(new Function(value), this);
					}
					return d.getObject(value, false);
				}catch(e){ return new Function(); }
			case "array":
				return value.split(/\s*,\s*/);
			case "date":
				switch(value){
					case "": return new Date("");	// the NaN of dates
					case "now": return new Date();	// current date
					default: return d.date.stamp.fromISOString(value);
				}
			case "url":
				return d.baseUrl + value;
			default:
				return d.fromJson(value);
		}
	}

	var instanceClasses = {
		// map from fully qualified name (like "webui.suntheme4_2.dijit.Button") to structure like
		// { cls: webui.suntheme4_2.dijit.Button, params: {label: "string", disabled: "boolean"} }
	};
	
	function getClassInfo(/*String*/ className){
		// className:
		//		fully qualified name (like "webui.suntheme4_2.dijit.Button")
		// returns:
		//		structure like
		//			{ 
		//				cls: webui.suntheme4_2.dijit.Button, 
		//				params: { label: "string", disabled: "boolean"}
		//			}

		if(!instanceClasses[className]){
			// get pointer to widget class
			var cls = d.getObject(className);
			if(!d.isFunction(cls)){
				throw new Error("Could not load class '" + className +
					"'. Did you spell the name correctly and use a full path, like 'webui.suntheme4_2.dijit.form.Button'?");
			}
			var proto = cls.prototype;
	
			// get table of parameter names & types
			var params={};
			for(var name in proto){
				if(name.charAt(0)=="_"){ continue; } 	// skip internal properties
				var defVal = proto[name];
				params[name]=val2type(defVal);
			}

			instanceClasses[className] = { cls: cls, params: params };
		}
		return instanceClasses[className];
	}

	this._functionFromScript = function(script){
		var preamble = "";
		var suffix = "";
		var argsStr = script.getAttribute("args");
		if(argsStr){
			d.forEach(argsStr.split(/\s*,\s*/), function(part, idx){
				preamble += "var "+part+" = arguments["+idx+"]; ";
			});
		}
		var withStr = script.getAttribute("with");
		if(withStr && withStr.length){
			d.forEach(withStr.split(/\s*,\s*/), function(part){
				preamble += "with("+part+"){";
				suffix += "}";
			});
		}
		return new Function(preamble+script.innerHTML+suffix);
	}

	this.instantiate = function(/* Array */nodes){
		// summary:
		//		Takes array of nodes, and turns them into class instances and
		//		potentially calls a layout method to allow them to connect with
		//		any children		
		var thelist = [];
		d.forEach(nodes, function(node){
			if(!node){ return; }
			var type = node.getAttribute("dojoType");
			if((!type)||(!type.length)){ return; }
			var clsInfo = getClassInfo(type);
			var clazz = clsInfo.cls;
			var ps = clazz._noScript||clazz.prototype._noScript;

			// read parameters (ie, attributes).
			// clsInfo.params lists expected params like {"checked": "boolean", "n": "number"}
			var params = {};
			var attributes = node.attributes;
			for(var name in clsInfo.params){
				var item = attributes.getNamedItem(name);
				if(!item || (!item.specified && (!webui.suntheme4_2.dojo.isIE || name.toLowerCase()!="value"))){ continue; }
				var value = item.value;
				// Deal with IE quirks for 'class' and 'style'
				switch(name){
				case "class":
					value = node.className;
					break;
				case "style":
					value = node.style && node.style.cssText; // FIXME: Opera?
				}
				var _type = clsInfo.params[name];
				params[name] = str2obj(value, _type);
			}

			// Process <script type="dojo/*"> script tags
			// <script type="dojo/method" event="foo"> tags are added to params, and passed to
			// the widget on instantiation.
			// <script type="dojo/method"> tags (with no event) are executed after instantiation
			// <script type="dojo/connect" event="foo"> tags are webui.suntheme4_2.dojo.connected after instantiation
			if(!ps){
				var connects = [],	// functions to connect after instantiation
					calls = [];		// functions to call after instantiation

				d.query("> script[type^='dojo/']", node).orphan().forEach(function(script){
					var event = script.getAttribute("event"),
						type = script.getAttribute("type"),
						nf = d.parser._functionFromScript(script);
					if(event){
						if(type == "dojo/connect"){
							connects.push({event: event, func: nf});
						}else{
							params[event] = nf;
						}
					}else{
						calls.push(nf);
					}
				});
			}

			var markupFactory = clazz["markupFactory"];
			if(!markupFactory && clazz["prototype"]){
				markupFactory = clazz.prototype["markupFactory"];
			}
			// create the instance
			var instance = markupFactory ? markupFactory(params, node, clazz) : new clazz(params, node);
			thelist.push(instance);

			// map it to the JS namespace if that makes sense
			var jsname = node.getAttribute("jsId");
			if(jsname){
				d.setObject(jsname, instance);
			}

			// process connections and startup functions
			if(!ps){
				webui.suntheme4_2.dojo.forEach(connects, function(connect){
					webui.suntheme4_2.dojo.connect(instance, connect.event, null, connect.func);
				});
				webui.suntheme4_2.dojo.forEach(calls, function(func){
					func.call(instance);
				});
			}
		});

		// Call startup on each top level instance if it makes sense (as for
		// widgets).  Parent widgets will recursively call startup on their
		// (non-top level) children
		d.forEach(thelist, function(instance){
			if(	instance  && 
				(instance.startup) && 
				((!instance.getParent) || (!instance.getParent()))
			){
				instance.startup();
			}
		});
		return thelist;
	};

	this.parse = function(/*DomNode?*/ rootNode){
		// summary:
		//		Search specified node (or root node) recursively for class instances,
		//		and instantiate them Searches for
		//		dojoType="qualified.class.name"
		var list = d.query('[dojoType]', rootNode);
		// go build the object instances
		var instances = this.instantiate(list);
		return instances;
	};
}();

//Register the parser callback. It should be the first callback
//after the a11y test.

(function(){
	var parseRunner = function(){ 
		if(webui_suntheme4_2.djConfig["parseOnLoad"] == true){
			webui.suntheme4_2.dojo.parser.parse(); 
		}
	};

	// FIXME: need to clobber cross-dependency!!
	if(webui.suntheme4_2.dojo.exists("webui.suntheme4_2.dijit.wai.onload") && (webui.suntheme4_2.dijit.wai.onload === webui.suntheme4_2.dojo._loaders[0])){
		webui.suntheme4_2.dojo._loaders.splice(1, 0, parseRunner);
	}else{
		webui.suntheme4_2.dojo._loaders.unshift(parseRunner);
	}
})();

//TODO: ported from 0.4.x Dojo.  Can we reduce this?
webui.suntheme4_2.dojo.parser._anonCtr = 0;
webui.suntheme4_2.dojo.parser._anon = {}; // why is this property required?
webui.suntheme4_2.dojo.parser._nameAnonFunc = function(/*Function*/anonFuncPtr, /*Object*/thisObj){
	// summary:
	//		Creates a reference to anonFuncPtr in thisObj with a completely
	//		unique name. The new name is returned as a String. 
	var jpn = "$joinpoint";
	var nso = (thisObj|| webui.suntheme4_2.dojo.parser._anon);
	if(webui.suntheme4_2.dojo.isIE){
		var cn = anonFuncPtr["__dojoNameCache"];
		if(cn && nso[cn] === anonFuncPtr){
			return anonFuncPtr["__dojoNameCache"];
		}
	}
	var ret = "__"+webui.suntheme4_2.dojo.parser._anonCtr++;
	while(typeof nso[ret] != "undefined"){
		ret = "__"+webui.suntheme4_2.dojo.parser._anonCtr++;
	}
	nso[ret] = anonFuncPtr;
	return ret; // String
}

}

if(!webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Container"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Container"] = true;
webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dojo.dnd.Container");




/*
	Container states:
		""		- normal state
		"Over"	- mouse over a container
	Container item states:
		""		- normal state
		"Over"	- mouse over a container item
*/

webui.suntheme4_2.dojo.declare("webui.suntheme4_2.dojo.dnd.Container", null, {
	// summary: a Container object, which knows when mouse hovers over it, 
	//	and know over which element it hovers
	
	// object attributes (for markup)
	skipForm: false,
	
	constructor: function(node, params){
		// summary: a constructor of the Container
		// node: Node: node or node's id to build the container on
		// params: Object: a dict of parameters, recognized parameters are:
		//	creator: Function: a creator function, which takes a data item, and returns an object like that:
		//		{node: newNode, data: usedData, type: arrayOfStrings}
		//	skipForm: Boolean: don't start the drag operation, if clicked on form elements
		//	_skipStartup: Boolean: skip startup(), which collects children, for deferred initialization
		//		(this is used in the markup mode)
		this.node = webui.suntheme4_2.dojo.byId(node);
		if(!params){ params = {}; }
		this.creator = params.creator || null;
		this.skipForm = params.skipForm;
		this.defaultCreator = webui.suntheme4_2.dojo.dnd._defaultCreator(this.node);

		// class-specific variables
		this.map = {};
		this.current = null;

		// states
		this.containerState = "";
		webui.suntheme4_2.dojo.addClass(this.node, "dojoDndContainer");
		
		// mark up children
		if(!(params && params._skipStartup)){
			this.startup();
		}

		// set up events
		this.events = [
			webui.suntheme4_2.dojo.connect(this.node, "onmouseover", this, "onMouseOver"),
			webui.suntheme4_2.dojo.connect(this.node, "onmouseout",  this, "onMouseOut"),
			// cancel text selection and text dragging
			webui.suntheme4_2.dojo.connect(this.node, "ondragstart",   this, "onSelectStart"),
			webui.suntheme4_2.dojo.connect(this.node, "onselectstart", this, "onSelectStart")
		];
	},
	
	// object attributes (for markup)
	creator: function(){},	// creator function, dummy at the moment
	
	// abstract access to the map
	getItem: function(/*String*/ key){
		// summary: returns a data item by its key (id)
		return this.map[key];	// Object
	},
	setItem: function(/*String*/ key, /*Object*/ data){
		// summary: associates a data item with its key (id)
		this.map[key] = data;
	},
	delItem: function(/*String*/ key){
		// summary: removes a data item from the map by its key (id)
		delete this.map[key];
	},
	forInItems: function(/*Function*/ f, /*Object?*/ o){
		// summary: iterates over a data map skipping members, which 
		//	are present in the empty object (IE and/or 3rd-party libraries).
		o = o || webui.suntheme4_2.dojo.global;
		var m = this.map, e = webui.suntheme4_2.dojo.dnd._empty;
		for(var i in this.map){
			if(i in e){ continue; }
			f.call(o, m[i], i, m);
		}
	},
	clearItems: function(){
		// summary: removes all data items from the map
		this.map = {};
	},
	
	// methods
	getAllNodes: function(){
		// summary: returns a list (an array) of all valid child nodes
		return webui.suntheme4_2.dojo.query("> .dojoDndItem", this.parent);	// NodeList
	},
	insertNodes: function(data, before, anchor){
		// summary: inserts an array of new nodes before/after an anchor node
		// data: Array: a list of data items, which should be processed by the creator function
		// before: Boolean: insert before the anchor, if true, and after the anchor otherwise
		// anchor: Node: the anchor node to be used as a point of insertion
		if(!this.parent.firstChild){
			anchor = null;
		}else if(before){
			if(!anchor){
				anchor = this.parent.firstChild;
			}
		}else{
			if(anchor){
				anchor = anchor.nextSibling;
			}
		}
		if(anchor){
			for(var i = 0; i < data.length; ++i){
				var t = this._normalizedCreator(data[i]);
				this.setItem(t.node.id, {data: t.data, type: t.type});
				this.parent.insertBefore(t.node, anchor);
			}
		}else{
			for(var i = 0; i < data.length; ++i){
				var t = this._normalizedCreator(data[i]);
				this.setItem(t.node.id, {data: t.data, type: t.type});
				this.parent.appendChild(t.node);
			}
		}
		return this;	// self
	},
	destroy: function(){
		// summary: prepares the object to be garbage-collected
		webui.suntheme4_2.dojo.forEach(this.events, webui.suntheme4_2.dojo.disconnect);
		this.clearItems();
		this.node = this.parent = this.current;
	},

	// markup methods
	markupFactory: function(params, node){
		params._skipStartup = true;
		return new webui.suntheme4_2.dojo.dnd.Container(node, params);
	},
	startup: function(){
		// summary: collects valid child items and populate the map
		
		// set up the real parent node
		this.parent = this.node;
		if(this.parent.tagName.toLowerCase() == "table"){
			var c = this.parent.getElementsByTagName("tbody");
			if(c && c.length){ this.parent = c[0]; }
		}

		// process specially marked children
		webui.suntheme4_2.dojo.query("> .dojoDndItem", this.parent).forEach(function(node){
			if(!node.id){ node.id = webui.suntheme4_2.dojo.dnd.getUniqueId(); }
			var type = node.getAttribute("dndType"),
				data = node.getAttribute("dndData");
			this.setItem(node.id, {
				data: data ? data : node.innerHTML,
				type: type ? type.split(/\s*,\s*/) : ["text"]
			});
		}, this);
	},

	// mouse events
	onMouseOver: function(e){
		// summary: event processor for onmouseover
		// e: Event: mouse event
		var n = e.relatedTarget;
		while(n){
			if(n == this.node){ break; }
			try{
				n = n.parentNode;
			}catch(x){
				n = null;
			}
		}
		if(!n){
			this._changeState("Container", "Over");
			this.onOverEvent();
		}
		n = this._getChildByEvent(e);
		if(this.current == n){ return; }
		if(this.current){ this._removeItemClass(this.current, "Over"); }
		if(n){ this._addItemClass(n, "Over"); }
		this.current = n;
	},
	onMouseOut: function(e){
		// summary: event processor for onmouseout
		// e: Event: mouse event
		for(var n = e.relatedTarget; n;){
			if(n == this.node){ return; }
			try{
				n = n.parentNode;
			}catch(x){
				n = null;
			}
		}
		if(this.current){
			this._removeItemClass(this.current, "Over");
			this.current = null;
		}
		this._changeState("Container", "");
		this.onOutEvent();
	},
	onSelectStart: function(e){
		// summary: event processor for onselectevent and ondragevent
		// e: Event: mouse event
		if(!this.skipForm || !webui.suntheme4_2.dojo.dnd.isFormElement(e)){
			webui.suntheme4_2.dojo.stopEvent(e);
		}
	},
	
	// utilities
	onOverEvent: function(){
		// summary: this function is called once, when mouse is over our container
	},
	onOutEvent: function(){
		// summary: this function is called once, when mouse is out of our container
	},
	_changeState: function(type, newState){
		// summary: changes a named state to new state value
		// type: String: a name of the state to change
		// newState: String: new state
		var prefix = "dojoDnd" + type;
		var state  = type.toLowerCase() + "State";
		//webui.suntheme4_2.dojo.replaceClass(this.node, prefix + newState, prefix + this[state]);
		webui.suntheme4_2.dojo.removeClass(this.node, prefix + this[state]);
		webui.suntheme4_2.dojo.addClass(this.node, prefix + newState);
		this[state] = newState;
	},
	_addItemClass: function(node, type){
		// summary: adds a class with prefix "dojoDndItem"
		// node: Node: a node
		// type: String: a variable suffix for a class name
		webui.suntheme4_2.dojo.addClass(node, "dojoDndItem" + type);
	},
	_removeItemClass: function(node, type){
		// summary: removes a class with prefix "dojoDndItem"
		// node: Node: a node
		// type: String: a variable suffix for a class name
		webui.suntheme4_2.dojo.removeClass(node, "dojoDndItem" + type);
	},
	_getChildByEvent: function(e){
		// summary: gets a child, which is under the mouse at the moment, or null
		// e: Event: a mouse event
		var node = e.target;
		if(node){
			for(var parent = node.parentNode; parent; node = parent, parent = node.parentNode){
				if(parent == this.parent && webui.suntheme4_2.dojo.hasClass(node, "dojoDndItem")){ return node; }
			}
		}
		return null;
	},
	_normalizedCreator: function(item, hint){
		// summary: adds all necessary data to the output of the user-supplied creator function
		var t = (this.creator ? this.creator : this.defaultCreator)(item, hint);
		if(!webui.suntheme4_2.dojo.isArray(t.type)){ t.type = ["text"]; }
		if(!t.node.id){ t.node.id = webui.suntheme4_2.dojo.dnd.getUniqueId(); }
		webui.suntheme4_2.dojo.addClass(t.node, "dojoDndItem");
		return t;
	}
});

webui.suntheme4_2.dojo.dnd._createNode = function(tag){
	// summary: returns a function, which creates an element of given tag 
	//	(SPAN by default) and sets its innerHTML to given text
	// tag: String: a tag name or empty for SPAN
	if(!tag){ return webui.suntheme4_2.dojo.dnd._createSpan; }
	return function(text){	// Function
		var n = webui.suntheme4_2.dojo.doc.createElement(tag);
		n.innerHTML = text;
		return n;
	};
};

webui.suntheme4_2.dojo.dnd._createTrTd = function(text){
	// summary: creates a TR/TD structure with given text as an innerHTML of TD
	// text: String: a text for TD
	var tr = webui.suntheme4_2.dojo.doc.createElement("tr");
	var td = webui.suntheme4_2.dojo.doc.createElement("td");
	td.innerHTML = text;
	tr.appendChild(td);
	return tr;	// Node
};

webui.suntheme4_2.dojo.dnd._createSpan = function(text){
	// summary: creates a SPAN element with given text as its innerHTML
	// text: String: a text for SPAN
	var n = webui.suntheme4_2.dojo.doc.createElement("span");
	n.innerHTML = text;
	return n;	// Node
};

// webui.suntheme4_2.dojo.dnd._defaultCreatorNodes: Object: a dicitionary, which maps container tag names to child tag names
webui.suntheme4_2.dojo.dnd._defaultCreatorNodes = {ul: "li", ol: "li", div: "div", p: "div"};

webui.suntheme4_2.dojo.dnd._defaultCreator = function(node){
	// summary: takes a container node, and returns an appropriate creator function
	// node: Node: a container node
	var tag = node.tagName.toLowerCase();
	var c = tag == "table" ? webui.suntheme4_2.dojo.dnd._createTrTd : webui.suntheme4_2.dojo.dnd._createNode(webui.suntheme4_2.dojo.dnd._defaultCreatorNodes[tag]);
	return function(item, hint){	// Function
		var isObj = webui.suntheme4_2.dojo.isObject(item) && item;
		var data = (isObj && item.data) ? item.data : item;
		var type = (isObj && item.type) ? item.type : ["text"];
		var t = String(data), n = (hint == "avatar" ? webui.suntheme4_2.dojo.dnd._createSpan : c)(t);
		n.id = webui.suntheme4_2.dojo.dnd.getUniqueId();
		return {node: n, data: data, type: type};
	};
};

}

if(!webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Selector"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Selector"] = true;
webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dojo.dnd.Selector");




/*
	Container item states:
		""			- an item is not selected
		"Selected"	- an item is selected
		"Anchor"	- an item is selected, and is an anchor for a "shift" selection
*/

webui.suntheme4_2.dojo.declare("webui.suntheme4_2.dojo.dnd.Selector", webui.suntheme4_2.dojo.dnd.Container, {
	// summary: a Selector object, which knows how to select its children
	
	constructor: function(node, params){
		// summary: a constructor of the Selector
		// node: Node: node or node's id to build the selector on
		// params: Object: a dict of parameters, recognized parameters are:
		//	singular: Boolean: allows selection of only one element, if true
		//	the rest of parameters are passed to the container
		if(!params){ params = {}; }
		this.singular = params.singular;
		// class-specific variables
		this.selection = {};
		this.anchor = null;
		this.simpleSelection = false;
		// set up events
		this.events.push(
			webui.suntheme4_2.dojo.connect(this.node, "onmousedown", this, "onMouseDown"),
			webui.suntheme4_2.dojo.connect(this.node, "onmouseup",   this, "onMouseUp"));
	},
	
	// object attributes (for markup)
	singular: false,	// is singular property
	
	// methods
	getSelectedNodes: function(){
		// summary: returns a list (an array) of selected nodes
		var t = new webui.suntheme4_2.dojo.NodeList();
		var e = webui.suntheme4_2.dojo.dnd._empty;
		for(var i in this.selection){
			if(i in e){ continue; }
			t.push(webui.suntheme4_2.dojo.byId(i));
		}
		return t;	// Array
	},
	selectNone: function(){
		// summary: unselects all items
		return this._removeSelection()._removeAnchor();	// self
	},
	selectAll: function(){
		// summary: selects all items
		this.forInItems(function(data, id){
			this._addItemClass(webui.suntheme4_2.dojo.byId(id), "Selected");
			this.selection[id] = 1;
		}, this);
		return this._removeAnchor();	// self
	},
	deleteSelectedNodes: function(){
		// summary: deletes all selected items
		var e = webui.suntheme4_2.dojo.dnd._empty;
		for(var i in this.selection){
			if(i in e){ continue; }
			var n = webui.suntheme4_2.dojo.byId(i);
			this.delItem(i);
			webui.suntheme4_2.dojo._destroyElement(n);
		}
		this.anchor = null;
		this.selection = {};
		return this;	// self
	},
	insertNodes: function(addSelected, data, before, anchor){
		// summary: inserts new data items (see Container's insertNodes method for details)
		// addSelected: Boolean: all new nodes will be added to selected items, if true, no selection change otherwise
		// data: Array: a list of data items, which should be processed by the creator function
		// before: Boolean: insert before the anchor, if true, and after the anchor otherwise
		// anchor: Node: the anchor node to be used as a point of insertion
		var oldCreator = this._normalizedCreator;
		this._normalizedCreator = function(item, hint){
			var t = oldCreator.call(this, item, hint);
			if(addSelected){
				if(!this.anchor){
					this.anchor = t.node;
					this._removeItemClass(t.node, "Selected");
					this._addItemClass(this.anchor, "Anchor");
				}else if(this.anchor != t.node){
					this._removeItemClass(t.node, "Anchor");
					this._addItemClass(t.node, "Selected");
				}
				this.selection[t.node.id] = 1;
			}else{
				this._removeItemClass(t.node, "Selected");
				this._removeItemClass(t.node, "Anchor");
			}
			return t;
		};
		webui.suntheme4_2.dojo.dnd.Selector.superclass.insertNodes.call(this, data, before, anchor);
		this._normalizedCreator = oldCreator;
		return this;	// self
	},
	destroy: function(){
		// summary: prepares the object to be garbage-collected
		webui.suntheme4_2.dojo.dnd.Selector.superclass.destroy.call(this);
		this.selection = this.anchor = null;
	},

	// markup methods
	markupFactory: function(params, node){
		params._skipStartup = true;
		return new webui.suntheme4_2.dojo.dnd.Selector(node, params);
	},

	// mouse events
	onMouseDown: function(e){
		// summary: event processor for onmousedown
		// e: Event: mouse event
		if(!this.current){ return; }
		if(!this.singular && !webui.suntheme4_2.dojo.dnd.getCopyKeyState(e) && !e.shiftKey && (this.current.id in this.selection)){
			this.simpleSelection = true;
			webui.suntheme4_2.dojo.stopEvent(e);
			return;
		}
		if(!this.singular && e.shiftKey){
			if(!webui.suntheme4_2.dojo.dnd.getCopyKeyState(e)){
				this._removeSelection();
			}
			var c = webui.suntheme4_2.dojo.query("> .dojoDndItem", this.parent);
			if(c.length){
				if(!this.anchor){
					this.anchor = c[0];
					this._addItemClass(this.anchor, "Anchor");
				}
				this.selection[this.anchor.id] = 1;
				if(this.anchor != this.current){
					var i = 0;
					for(; i < c.length; ++i){
						var node = c[i];
						if(node == this.anchor || node == this.current){ break; }
					}
					for(++i; i < c.length; ++i){
						var node = c[i];
						if(node == this.anchor || node == this.current){ break; }
						this._addItemClass(node, "Selected");
						this.selection[node.id] = 1;
					}
					this._addItemClass(this.current, "Selected");
					this.selection[this.current.id] = 1;
				}
			}
		}else{
			if(this.singular){
				if(this.anchor == this.current){
					if(webui.suntheme4_2.dojo.dnd.getCopyKeyState(e)){
						this.selectNone();
					}
				}else{
					this.selectNone();
					this.anchor = this.current;
					this._addItemClass(this.anchor, "Anchor");
					this.selection[this.current.id] = 1;
				}
			}else{
				if(webui.suntheme4_2.dojo.dnd.getCopyKeyState(e)){
					if(this.anchor == this.current){
						delete this.selection[this.anchor.id];
						this._removeAnchor();
					}else{
						if(this.current.id in this.selection){
							this._removeItemClass(this.current, "Selected");
							delete this.selection[this.current.id];
						}else{
							if(this.anchor){
								this._removeItemClass(this.anchor, "Anchor");
								this._addItemClass(this.anchor, "Selected");
							}
							this.anchor = this.current;
							this._addItemClass(this.current, "Anchor");
							this.selection[this.current.id] = 1;
						}
					}
				}else{
					if(!(this.current.id in this.selection)){
						this.selectNone();
						this.anchor = this.current;
						this._addItemClass(this.current, "Anchor");
						this.selection[this.current.id] = 1;
					}
				}
			}
		}
		webui.suntheme4_2.dojo.stopEvent(e);
	},
	onMouseUp: function(e){
		// summary: event processor for onmouseup
		// e: Event: mouse event
		if(!this.simpleSelection){ return; }
		this.simpleSelection = false;
		this.selectNone();
		if(this.current){
			this.anchor = this.current;
			this._addItemClass(this.anchor, "Anchor");
			this.selection[this.current.id] = 1;
		}
	},
	onMouseMove: function(e){
		// summary: event processor for onmousemove
		// e: Event: mouse event
		this.simpleSelection = false;
	},
	
	// utilities
	onOverEvent: function(){
		// summary: this function is called once, when mouse is over our container
		this.onmousemoveEvent = webui.suntheme4_2.dojo.connect(this.node, "onmousemove", this, "onMouseMove");
	},
	onOutEvent: function(){
		// summary: this function is called once, when mouse is out of our container
		webui.suntheme4_2.dojo.disconnect(this.onmousemoveEvent);
		delete this.onmousemoveEvent;
	},
	_removeSelection: function(){
		// summary: unselects all items
		var e = webui.suntheme4_2.dojo.dnd._empty;
		for(var i in this.selection){
			if(i in e){ continue; }
			var node = webui.suntheme4_2.dojo.byId(i);
			if(node){ this._removeItemClass(node, "Selected"); }
		}
		this.selection = {};
		return this;	// self
	},
	_removeAnchor: function(){
		if(this.anchor){
			this._removeItemClass(this.anchor, "Anchor");
			this.anchor = null;
		}
		return this;	// self
	}
});

}

if(!webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Source"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
webui.suntheme4_2.dojo._hasResource["webui.suntheme4_2.dojo.dnd.Source"] = true;
webui.suntheme4_2.dojo.provide("webui.suntheme4_2.dojo.dnd.Source");




/*
	Container property:
		"Horizontal"- if this is the horizontal container
	Source states:
		""			- normal state
		"Moved"		- this source is being moved
		"Copied"	- this source is being copied
	Target states:
		""			- normal state
		"Disabled"	- the target cannot accept an avatar
	Target anchor state:
		""			- item is not selected
		"Before"	- insert point is before the anchor
		"After"		- insert point is after the anchor
*/

webui.suntheme4_2.dojo.declare("webui.suntheme4_2.dojo.dnd.Source", webui.suntheme4_2.dojo.dnd.Selector, {
	// summary: a Source object, which can be used as a DnD source, or a DnD target
	
	// object attributes (for markup)
	isSource: true,
	horizontal: false,
	copyOnly: false,
	skipForm: false,
	withHandles: false,
	accept: ["text"],
	
	constructor: function(node, params){
		// summary: a constructor of the Source
		// node: Node: node or node's id to build the source on
		// params: Object: a dict of parameters, recognized parameters are:
		//	isSource: Boolean: can be used as a DnD source, if true; assumed to be "true" if omitted
		//	accept: Array: list of accepted types (text strings) for a target; assumed to be ["text"] if omitted
		//	horizontal: Boolean: a horizontal container, if true, vertical otherwise or when omitted
		//	copyOnly: Boolean: always copy items, if true, use a state of Ctrl key otherwise
		//	withHandles: Boolean: allows dragging only by handles
		//	the rest of parameters are passed to the selector
		if(!params){ params = {}; }
		this.isSource = typeof params.isSource == "undefined" ? true : params.isSource;
		var type = params.accept instanceof Array ? params.accept : ["text"];
		this.accept = null;
		if(type.length){
			this.accept = {};
			for(var i = 0; i < type.length; ++i){
				this.accept[type[i]] = 1;
			}
		}
		this.horizontal = params.horizontal;
		this.copyOnly = params.copyOnly;
		this.withHandles = params.withHandles;
		// class-specific variables
		this.isDragging = false;
		this.mouseDown = false;
		this.targetAnchor = null;
		this.targetBox = null;
		this.before = true;
		// states
		this.sourceState  = "";
		if(this.isSource){
			webui.suntheme4_2.dojo.addClass(this.node, "dojoDndSource");
		}
		this.targetState  = "";
		if(this.accept){
			webui.suntheme4_2.dojo.addClass(this.node, "dojoDndTarget");
		}
		if(this.horizontal){
			webui.suntheme4_2.dojo.addClass(this.node, "dojoDndHorizontal");
		}
		// set up events
		this.topics = [
			webui.suntheme4_2.dojo.subscribe("/dnd/source/over", this, "onDndSourceOver"),
			webui.suntheme4_2.dojo.subscribe("/dnd/start",  this, "onDndStart"),
			webui.suntheme4_2.dojo.subscribe("/dnd/drop",   this, "onDndDrop"),
			webui.suntheme4_2.dojo.subscribe("/dnd/cancel", this, "onDndCancel")
		];
	},
	
	// methods
	checkAcceptance: function(source, nodes){
		// summary: checks, if the target can accept nodes from this source
		// source: Object: the source which provides items
		// nodes: Array: the list of transferred items
		if(this == source){ return true; }
		for(var i = 0; i < nodes.length; ++i){
			var type = source.getItem(nodes[i].id).type;
			// type instanceof Array
			var flag = false;
			for(var j = 0; j < type.length; ++j){
				if(type[j] in this.accept){
					flag = true;
					break;
				}
			}
			if(!flag){
				return false;	// Boolean
			}
		}
		return true;	// Boolean
	},
	copyState: function(keyPressed){
		// summary: Returns true, if we need to copy items, false to move.
		//		It is separated to be overwritten dynamically, if needed.
		// keyPressed: Boolean: the "copy" was pressed
		return this.copyOnly || keyPressed;	// Boolean
	},
	destroy: function(){
		// summary: prepares the object to be garbage-collected
		webui.suntheme4_2.dojo.dnd.Source.superclass.destroy.call(this);
		webui.suntheme4_2.dojo.forEach(this.topics, webui.suntheme4_2.dojo.unsubscribe);
		this.targetAnchor = null;
	},

	// markup methods
	markupFactory: function(params, node){
		params._skipStartup = true;
		return new webui.suntheme4_2.dojo.dnd.Source(node, params);
	},

	// mouse event processors
	onMouseMove: function(e){
		// summary: event processor for onmousemove
		// e: Event: mouse event
		if(this.isDragging && this.targetState == "Disabled"){ return; }
		webui.suntheme4_2.dojo.dnd.Source.superclass.onMouseMove.call(this, e);
		var m = webui.suntheme4_2.dojo.dnd.manager();
		if(this.isDragging){
			// calculate before/after
			var before = false;
			if(this.current){
				if(!this.targetBox || this.targetAnchor != this.current){
					this.targetBox = {
						xy: webui.suntheme4_2.dojo.coords(this.current, true),
						w: this.current.offsetWidth,
						h: this.current.offsetHeight
					};
				}
				if(this.horizontal){
					before = (e.pageX - this.targetBox.xy.x) < (this.targetBox.w / 2);
				}else{
					before = (e.pageY - this.targetBox.xy.y) < (this.targetBox.h / 2);
				}
			}
			if(this.current != this.targetAnchor || before != this.before){
				this._markTargetAnchor(before);
				m.canDrop(!this.current || m.source != this || !(this.current.id in this.selection));
			}
		}else{
			if(this.mouseDown && this.isSource){
				var nodes = this.getSelectedNodes();
				if(nodes.length){
					m.startDrag(this, nodes, this.copyState(webui.suntheme4_2.dojo.dnd.getCopyKeyState(e)));
				}
			}
		}
	},
	onMouseDown: function(e){
		// summary: event processor for onmousedown
		// e: Event: mouse event
		if(this._legalMouseDown(e) && (!this.skipForm || !webui.suntheme4_2.dojo.dnd.isFormElement(e))){
			this.mouseDown = true;
			this.mouseButton = e.button;
			webui.suntheme4_2.dojo.dnd.Source.superclass.onMouseDown.call(this, e);
		}
	},
	onMouseUp: function(e){
		// summary: event processor for onmouseup
		// e: Event: mouse event
		if(this.mouseDown){
			this.mouseDown = false;
			webui.suntheme4_2.dojo.dnd.Source.superclass.onMouseUp.call(this, e);
		}
	},
	
	// topic event processors
	onDndSourceOver: function(source){
		// summary: topic event processor for /dnd/source/over, called when detected a current source
		// source: Object: the source which has the mouse over it
		if(this != source){
			this.mouseDown = false;
			if(this.targetAnchor){
				this._unmarkTargetAnchor();
			}
		}else if(this.isDragging){
			var m = webui.suntheme4_2.dojo.dnd.manager();
			m.canDrop(this.targetState != "Disabled" && (!this.current || m.source != this || !(this.current.id in this.selection)));
		}
	},
	onDndStart: function(source, nodes, copy){
		// summary: topic event processor for /dnd/start, called to initiate the DnD operation
		// source: Object: the source which provides items
		// nodes: Array: the list of transferred items
		// copy: Boolean: copy items, if true, move items otherwise
		if(this.isSource){
			this._changeState("Source", this == source ? (copy ? "Copied" : "Moved") : "");
		}
		var accepted = this.accept && this.checkAcceptance(source, nodes);
		this._changeState("Target", accepted ? "" : "Disabled");
		if(accepted && this == source){
			webui.suntheme4_2.dojo.dnd.manager().overSource(this);
		}
		this.isDragging = true;
	},
	onDndDrop: function(source, nodes, copy){
		// summary: topic event processor for /dnd/drop, called to finish the DnD operation
		// source: Object: the source which provides items
		// nodes: Array: the list of transferred items
		// copy: Boolean: copy items, if true, move items otherwise
		do{ //break box
			if(this.containerState != "Over"){ break; }
			var oldCreator = this._normalizedCreator;
			if(this != source){
				// transferring nodes from the source to the target
				if(this.creator){
					// use defined creator
					this._normalizedCreator = function(node, hint){
						return oldCreator.call(this, source.getItem(node.id).data, hint);
					};
				}else{
					// we have no creator defined => move/clone nodes
					if(copy){
						// clone nodes
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							var n = node.cloneNode(true);
							n.id = webui.suntheme4_2.dojo.dnd.getUniqueId();
							return {node: n, data: t.data, type: t.type};
						};
					}else{
						// move nodes
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							source.delItem(node.id);
							return {node: node, data: t.data, type: t.type};
						};
					}
				}
			}else{
				// transferring nodes within the single source
				if(this.current && this.current.id in this.selection){ break; }
				if(this.creator){
					// use defined creator
					if(copy){
						// create new copies of data items
						this._normalizedCreator = function(node, hint){
							return oldCreator.call(this, source.getItem(node.id).data, hint);
						};
					}else{
						// move nodes
						if(!this.current){ break; }
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							return {node: node, data: t.data, type: t.type};
						};
					}
				}else{
					// we have no creator defined => move/clone nodes
					if(copy){
						// clone nodes
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							var n = node.cloneNode(true);
							n.id = webui.suntheme4_2.dojo.dnd.getUniqueId();
							return {node: n, data: t.data, type: t.type};
						};
					}else{
						// move nodes
						if(!this.current){ break; }
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							return {node: node, data: t.data, type: t.type};
						};
					}
				}
			}
			this._removeSelection();
			if(this != source){
				this._removeAnchor();
			}
			if(this != source && !copy && !this.creator){
				source.selectNone();
			}
			this.insertNodes(true, nodes, this.before, this.current);
			if(this != source && !copy && this.creator){
				source.deleteSelectedNodes();
			}
			this._normalizedCreator = oldCreator;
		}while(false);
		this.onDndCancel();
	},
	onDndCancel: function(){
		// summary: topic event processor for /dnd/cancel, called to cancel the DnD operation
		if(this.targetAnchor){
			this._unmarkTargetAnchor();
			this.targetAnchor = null;
		}
		this.before = true;
		this.isDragging = false;
		this.mouseDown = false;
		delete this.mouseButton;
		this._changeState("Source", "");
		this._changeState("Target", "");
	},
	
	// utilities
	onOverEvent: function(){
		// summary: this function is called once, when mouse is over our container
		webui.suntheme4_2.dojo.dnd.Source.superclass.onOverEvent.call(this);
		webui.suntheme4_2.dojo.dnd.manager().overSource(this);
	},
	onOutEvent: function(){
		// summary: this function is called once, when mouse is out of our container
		webui.suntheme4_2.dojo.dnd.Source.superclass.onOutEvent.call(this);
		webui.suntheme4_2.dojo.dnd.manager().outSource(this);
	},
	_markTargetAnchor: function(before){
		// summary: assigns a class to the current target anchor based on "before" status
		// before: Boolean: insert before, if true, after otherwise
		if(this.current == this.targetAnchor && this.before == before){ return; }
		if(this.targetAnchor){
			this._removeItemClass(this.targetAnchor, this.before ? "Before" : "After");
		}
		this.targetAnchor = this.current;
		this.targetBox = null;
		this.before = before;
		if(this.targetAnchor){
			this._addItemClass(this.targetAnchor, this.before ? "Before" : "After");
		}
	},
	_unmarkTargetAnchor: function(){
		// summary: removes a class of the current target anchor based on "before" status
		if(!this.targetAnchor){ return; }
		this._removeItemClass(this.targetAnchor, this.before ? "Before" : "After");
		this.targetAnchor = null;
		this.targetBox = null;
		this.before = true;
	},
	_markDndStatus: function(copy){
		// summary: changes source's state based on "copy" status
		this._changeState("Source", copy ? "Copied" : "Moved");
	},
	_legalMouseDown: function(e){
		// summary: checks if user clicked on "approved" items
		// e: Event: mouse event
		if(!this.withHandles){ return true; }
		for(var node = e.target; node && !webui.suntheme4_2.dojo.hasClass(node, "dojoDndItem"); node = node.parentNode){
			if(webui.suntheme4_2.dojo.hasClass(node, "dojoDndHandle")){ return true; }
		}
		return false;	// Boolean
	}
});

webui.suntheme4_2.dojo.declare("webui.suntheme4_2.dojo.dnd.Target", webui.suntheme4_2.dojo.dnd.Source, {
	// summary: a Target object, which can be used as a DnD target
	
	constructor: function(node, params){
		// summary: a constructor of the Target --- see the Source constructor for details
		this.isSource = false;
		webui.suntheme4_2.dojo.removeClass(this.node, "dojoDndSource");
	},

	// markup methods
	markupFactory: function(params, node){
		params._skipStartup = true;
		return new webui.suntheme4_2.dojo.dnd.Target(node, params);
	}
});

}

webui.suntheme4_2.dojo.require("webui.suntheme4_2.dojo.dnd.Manager");
webui.suntheme4_2.dojo.require("webui.suntheme4_2.dojo.dnd.Source");


/**
 * @name webui.suntheme4_2.dnd.Manager
 * @extends webui.suntheme4_2.dojo.dnd.Manager
 * @class This class extends webui.suntheme4_2.dojo.dnd.Manager to support additional features of
 * Woodstock drag and drop.
 */
webui.suntheme4_2.dojo.declare("webui.suntheme4_2.dnd.Manager", webui.suntheme4_2.dojo.dnd.Manager);

/** 
 * Processes startDrag event to insert dragging styles.
 *
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.dnd.Manager.prototype.startDrag = function(source, nodes, copy) {
    webui.suntheme4_2.dojo.forEach(nodes,
        function(node) {
            webui.suntheme4_2.dojo.addClass(node, "dojoDndWebuiItemDragged");
        }
    );
    return this.inherited("startDrag", arguments);    
};

/**
 * Processes stopDrag event to cleanup dragging styles.
 *
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.dnd.Manager.prototype.stopDrag = function() {
    webui.suntheme4_2.dojo.forEach(this.nodes,
        function(node) {
            webui.suntheme4_2.dojo.removeClass(node, "dojoDndWebuiItemDragged");
        }
    );
    return this.inherited("stopDrag", arguments);    
};

/**
 * @name webui.suntheme4_2.dnd.Source
 * @extends webui.suntheme4_2.dojo.dnd.Source
 * @class This class extends webui.suntheme4_2.dojo.dnd.Source to support additional features of
 * Woodstock drag and drop.
 * @constructor This function is used to construct a dnd source.
 * @param {Object} props Key-Value pairs of properties.
 * @config {boolean} isSource Can be used as a DnD source, if true; assumed to
 * be "true" if omitted.
 * @config {Array} accept List of accepted types (text strings) for a target; 
 * assumed to be ["text"] if omitted.
 * @config {boolean} horizontal A horizontal container, if true, vertical 
 * otherwise or when omitted.
 * @config {boolean} copyOnly Always copy items, if true, use a state of Ctrl 
 * key otherwise.
 * @config {boolean} skipForm Don't start the drag operation, if clicked on 
 * form elements.
 * @config {boolean} singular Allows selection of only one element, if true.
 * @config {Function} creator Function A creator function, which takes a data
 * item, and returns an object like that: {node: newNode, data: usedData, type: 
 * arrayOfStrings}.
 * @config {boolean} _skipStartup Skip startup(), which collects children, for
 * deferred initialization (used in the markup mode).
 * @config {Function} onDropFunction User defined onDrop function with 
 * signature function(source, nodes, copy){..}.
 */
webui.suntheme4_2.dojo.declare("webui.suntheme4_2.dnd.Source", webui.suntheme4_2.dojo.dnd.Source, {
    defaultTypes: [ "default" ], // default types for the source
    
    /**
     * Constructor
     *
     * @param {Node} node DOM node
     * @param {Object} props Key-Value pairs of properties as described above.
     * @return {boolean} true if successful; otherwise, false.
     * @private
     */  
    constructor: function(node, props) {
        // Replace the drag manager
        if (webui.suntheme4_2.dojo.dnd._manager == null) {
            webui.suntheme4_2.dojo.dnd._manager = new webui.suntheme4_2.dnd.Manager();
        }

        // Disable source functionality
        if (props.isSource && props.isSource == false) {
            this.isSource = false;
            webui.suntheme4_2.dojo.removeClass(node, "dojoDndSource");
        }

        // Set user's onDrop function
        this.onDropFunction  = props.onDropFunction 
            ? props.onDropFunction : null;

        return true;
    }
});

/**
 * This helper method will create a node using _normalizedCreator (which in turn 
 * will use user's creator function, if supplied) and will add it to 
 * this source container. This method allows:
 * <p><pre>
 * - explicitely provide drag item type and data to overcome limitation of dojo
 *   _normalizedCreator
 * - unlike another helper function here ( makeNodeDraggable) allow to add 
 *   items to the container uniformly, wrapping the type of item added ( i.e. 
 *   nested items may be span, div, img, etc.)
 * </pre></p>
 * @param {String} nodeContent A fragment that will be inserted into a newly 
 * created draggable node.
 * @param {Array} dragType An array of types with no spaces ( TRIMMED!).
 * @param {Object} dragData Payload data to be associated with the drag item.
 * @return {Node} The created node.
 */
webui.suntheme4_2.dnd.Source.prototype.addItem = function(nodeContent, dragType, dragData) { 
    var t = this._normalizedCreator([nodeContent]);        
    this.setItem(t.node.id, {
        data: dragData, 
        type: dragType   
    });
    this.parent.appendChild(t.node);
    return t.node;
};

/**
 * Dojo implementation relies either on html markup to describe which items are
 * to be draggable or on insertNodes mehods that creates new nodes within a 
 * container. This function adds a programmatic way to make existing elements of
 * the container draggable.
 *
 * @param {Node} node DOM node or id of the element within this container to be
 * a draggable element.
 * @param (Array) dragType Array of types.
 * @param (String) dragData Data associated with dragItem.
 * @return {boolean} true if successful; otherwise, false.
 */
webui.suntheme4_2.dnd.Source.prototype.makeNodeDraggable = function(node, dragType, dragData) {
    if (webui.suntheme4_2.dojo.byId(node)) {
        node = webui.suntheme4_2.dojo.byId(node);  
    } else { 
        if (!node.nodeType) {
            // this is not a DOM node
            return false;
        }
    }
    if (!node.id) {
        node.id = webui.suntheme4_2.dojo.dnd.getUniqueId();    
    }
    var type = dragType ? dragType : node.getAttribute("dndType");
    if (!type) {
        type = this.DEFAULT_TYPES;
    }
    type = (type instanceof Array) ? type : type = type.split(',');
    webui.suntheme4_2.dojo.forEach(type, this.trim);

    var data = dragData ? dragData : node.getAttribute("dndData");
    this.setItem(node.id, {
        data: data ? data : node.innerHTML,
        type: type  
    });
    this._addItemClass(node, "");
    return true;
};

/** 
 * Makes use of webui.@THEME.dnd.Source for markup processing.
 *
 * @param {Object} props Key-Value pairs of properties.
 * @param {Node} node The DOM node.
 * @return {webui.suntheme4_2.dnd.Source} The Source object.
 * @private
 */
webui.suntheme4_2.dnd.Source.prototype.markupFactory = function(props, node) {
    props._skipStartup = true;
    return new webui.suntheme4_2.dnd.Source(node, props);
};

/** 
 * Processes dndDrop event by providing transparency treatment for source
 * elements.
 *
 * @param (Object) source The drag source.
 * @param (Object) nodes Array of nodes to be dropped.
 * @param (boolean) copy A flag indicating copy is desired.
 * @return {boolean} The result of user's onDropFunction.
 */
webui.suntheme4_2.dnd.Source.prototype.onDndDrop = function(source, nodes, copy) {
    this.inherited("onDndDrop", arguments);
    
    // We have to remove class onDndDrop here as well as in mgr
    // because onDndDrop is called before mgr.stopDrag, and transparency 
    // needs to be removed before clone is made.
    webui.suntheme4_2.dojo.forEach(nodes,
        function(node) {
            webui.suntheme4_2.dojo.removeClass(node, "dojoDndWebuiItemDragged");
        }
    );

    var ret = true;
    if (this.onDropFunction && 
        this != source && 
        this.containerState == "Over"
        ) {
        try {
            ret = this.onDropFunction(source, nodes, copy);
        } catch (err) {}
    }
    return ret; // Return from this method is actually ignored.
};

/**
 * This creator-wrapper function ensures that user provided creator function
 * results in providing all neccessary information for the newly created node.
 * Specifically, if type is not provided, it sets a default type on the item.
 *
 * @param (Object) data data to be used for node creation.
 * @param (String) hint hint that takes value of "avatar" when avatar is 
 * created, null otherwise.
 * @return {Node} The created node.
 */ 
webui.suntheme4_2.dnd.Source.prototype._normalizedCreator = function(data, hint) {
    // Adds all necessary data to the output of user-supplied creator function.
    var t = (this.creator ? this.creator : this.defaultCreator)(data, hint);
    if (!webui.suntheme4_2.dojo.isArray(t.type)) {
        t.type = this.DEFAULT_TYPES;    
    }
    if (!t.node.id) {
        t.node.id = webui.suntheme4_2.dojo.dnd.getUniqueId();    
    }
    webui.suntheme4_2.dojo.addClass(t.node, "dojoDndItem");           
    return t;
};

/** 
 * Util method to trim the string. 
 * 
 * @param (String) str string to process.
 * @return {String} The trimmed string.
 * @private
 */    
webui.suntheme4_2.dnd.Source.prototype.trim = function(str){ 
    // TODO make a String.prototype in common.js out of this.
    str = str.replace(/^\s\s*/, '').replace(/\s\s*$/, ''); 
    return str;
};
