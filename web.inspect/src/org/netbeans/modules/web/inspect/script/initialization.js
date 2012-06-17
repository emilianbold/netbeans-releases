/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

// Check if the page is initialized already
if (!(typeof(NetBeans) === 'object'
    && typeof(NetBeans.GLASSPANE_ID) === 'string'
    && document.getElementById(NetBeans.GLASSPANE_ID) !== null)) {

NetBeans = new Object();

// Name of attribute used to store original URL of the stylesheet
NetBeans.ATTR_URL = ':netbeans_url';

// Name of attribute used to mark document elements created by NetBeans
NetBeans.ATTR_ARTIFICIAL = ':netbeans_generated';

// Name of attribute used to mark (temporarily) selected elements
NetBeans.ATTR_SELECTED = ':netbeans_selected';

// Name of attribute used to mark (temporarily) highlighted elements
NetBeans.ATTR_HIGHLIGHTED = ':netbeans_highlighted';

// ID of canvas element that serves as a glass-pane
NetBeans.GLASSPANE_ID = 'netbeans_glasspane';

// ID of check-box for selection mode
NetBeans.SELECTION_MODE_ID = 'netbeans_selection_mode';

// Name of the parameter (in query string)
// that is used to force reload of some resource
NetBeans.RELOAD_PARAM = 'netbeans_reload';

// Counter used to generate element IDs
NetBeans.nextId = 0;

// Selected elements
NetBeans.selection = [];

// Next selection (under construction)
NetBeans.nextSelection = [];

// Highlighted elements
NetBeans.highlight = [];

// Next highlight (under construction)
NetBeans.nextHighlight = [];

NetBeans.getMatchedCSSRulesAvailable = !!document.defaultView.getMatchedCSSRules;

// Clears element selection
NetBeans.clearSelection = function() {
    this.selection = [];
    this.repaintGlassPane();
};

// Initializes/clears the next selection
NetBeans.initNextSelection = function() {
    this.nextSelection = [];
}

// Initializes/clears the next highlight
NetBeans.initNextHighlight = function() {
    this.nextHighlight = [];
}

// Adds an element into the next selection
NetBeans.addElementToNextSelection = function(element) {
    if (this.nextSelection.indexOf(element) == -1) {
        this.nextSelection.push(element);
    }
}

// Adds an element into the next highlight
NetBeans.addElementToNextHighlight = function(element) {
    if (this.nextHighlight.indexOf(element) == -1) {
        this.nextHighlight.push(element);
    }
}

// Finishes the next selection, i.e., switches the next selection to current selection
NetBeans.finishNextSelection = function() {
    this.selection = this.nextSelection;
    this.repaintGlassPane();
}

// Finishes the next highlight, i.e., switches the next highlight to current highlight
NetBeans.finishNextHighlight = function() {
    this.highlight = this.nextHighlight;
    this.repaintGlassPane();
}

// (object instanceof Element) doesn't work in content scripts
// in Firefox (because of implicit wrapping by XPCNativeWrapper).
// Hence, we use this simple check.
NetBeans.isElement = function(object) {
    return object.nodeType && (object.nodeType === 1);
}

// Returns an element that corresponds to the handle
NetBeans.getElement = function(handle) {
    if (this.isElement(handle)) {
        return handle;
    }
    //var nearest;
    var self = this;
    var locate = function(handle) {
        var parentHandle = handle.parent;
        if (parentHandle) {
            var parentElement = locate(parentHandle);
            if (parentElement) {
                var index = 0;
                var name = handle.siblingTagNames[index].toLowerCase();
                var childNodes = parentElement.childNodes;
                for (var i=0; i<childNodes.length; i++) {
                    var child = childNodes[i];
                    if (self.isElement(child)) {
                        if (child.getAttribute(self.ATTR_ARTIFICIAL)) { // skip artificial elements
                            continue;
                        }
                        var childName = child.tagName.toLowerCase();
                        if (name === childName) {
                            if (index === handle.indexInParent) {
                                return child;
                            } else {
                                index++;
                                if (index === handle.siblingTagNames.length) {
                                    break; // Document doesn't match the handle
                                } else {
                                    name = handle.siblingTagNames[index].toLowerCase();
                                }
                            }
                        }
                    }
                }
                //nearest = parentElement;
            }
        } else {
            var elemName = handle.siblingTagNames[handle.indexInParent].toLowerCase();
            var documentElement = document.documentElement;
            var documentElementName = documentElement.tagName.toLowerCase();
            if (elemName === documentElementName) {
                return documentElement;
            } else {
                //nearest = documentElement;
            }
        }
        return null;        
    }

    return locate(handle);
};

// Returns an element handle that corresponds to the given element
NetBeans.getElementHandle = function(element) {
    var handle = new Object();
    if (element.id && element.id !== "") {
        handle.id = element.id;
    }
    if (element.className && element.className !== "") {
        handle.className = element.className;
    }
    var parent = element.parentNode;
    if (this.isElement(parent)) {
        handle.parent = this.getElementHandle(parent);
        handle.siblingTagNames = [];
        var childs = parent.childNodes;
        for (var i=0; i<childs.length; i++) {
            var child = childs[i];
            if (this.isElement(child)) {
                if (child.getAttribute(this.ATTR_ARTIFICIAL)) { // skip artificial elements
                    continue;
                }
                if (child === element) {
                    handle.indexInParent = handle.siblingTagNames.length;
                }
                handle.siblingTagNames.push(child.tagName);
            }
        }
    } else {
        handle.indexInParent = 0;
        handle.siblingTagNames = [element.tagName];
    }
    return handle;
}

// Adds the specified element into the selection.
NetBeans.addElementToSelection = function(handle) {
    var element = this.getElement(handle);
    if (element) {
        if (this.selection.indexOf(element) == -1) {
            this.selection.push(element);
            this.repaintGlassPane();
        }
    }
};

// Sets the element selection.
NetBeans.selectElements = function(handles) {
    this.clearSelection();
    for (var i=0; i<handles.length; i++) {
        var handle = handles[i];
        this.addElementToSelection(handle);
    }
    this.repaintGlassPane();
};

// Returns attributes of the specified element
NetBeans.getAttributes = function(handle) {
    var result = new Object();
    var element = this.getElement(handle);
    if (element) {
        var attrs = element.attributes;
        for (var i=0; i<attrs.length; i++) {
            var attr = attrs[i];
            if (attr.specified) {
                result[attr.name] = attr.value;
            }
        }
    }
    return result;
};

// Returns computed style of the specified element
NetBeans.getComputedStyle = function(handle) {
    var result = new Object();
    var element = this.getElement(handle);
    if (element) {
        var style;
        if (document.defaultView && document.defaultView.getComputedStyle) {
            style = document.defaultView.getComputedStyle(element);
        } else {
            // IE 9+ in quirks mode or IE 8-
            style = element.currentStyle;
        }
        for (var i=0; i<style.length; i++) {
            var name = style.item(i);
            var value = style.getPropertyValue(name);
            result[name] = value;
        }
    }
    return result;
};

// Returns information about resources (scripts, images, style sheets) used
// by the inspected page.
NetBeans.getResources = function() {
    var resources = [];
    // Location
    resources.push({
        type: 'html',
        url: document.URL
    });
    // Stylesheets
    var i;
    var url;
    for (i=0; i<document.styleSheets.length; i++) {
        url = document.styleSheets[i].href;
        if (url !== null && url.length !== 0) {
            resources.push({
                type: 'styleSheet',
                url: this.removeReloadParameter(url)
            });
        }
    }
    // Scripts
    for (i=0; i<document.scripts.length; i++) {
        url = document.scripts[i].src;
        if (url !== null && url.length !== 0) {
            resources.push({
                type: 'script',
                url: this.removeReloadParameter(url)
            });
        }
    }
    // Images
    for (i=0; i<document.images.length; i++) {
        url = document.images[i].src;
        if (url !== null && url.length !== 0) {
            resources.push({
                type: 'image',
                url: this.removeReloadParameter(url)
            });
        }
    }    
    
    return resources;
};

// Helper method that removes "reload" parameter from the specified URL
NetBeans.removeReloadParameter = function(url) {
    // We can assume that the reload parameter is the last one
    // because we are putting it at the end.
    var idx = url.lastIndexOf('?'+this.RELOAD_PARAM + '=');
    if (idx != -1) {
        url = url.substring(0,idx);
    }
    idx = url.lastIndexOf('&'+this.RELOAD_PARAM + '=');
    if (idx != -1) {
        url = url.substring(0,idx);
    }
    return url;
};

// Helper method that adds "reload" parameter to the specified URL
NetBeans.addReloadParameter = function(url) {
    // Add the reload parameter
    var idx = url.indexOf('?')
    if (idx == -1) {
        // No query string
        url += '?';
    } else {
        // Some query string
        url += '&';
    }
    url += this.RELOAD_PARAM + '=' + new Date().getTime();
    return url;
};

// Forces reload of the style sheet with the given URL
NetBeans.reloadCSS = function(url) {
    var found = false;
    url = this.removeReloadParameter(url);
    var newURL = this.addReloadParameter(url);
    for (var i=0; i<document.styleSheets.length; i++) {
        var sheet = document.styleSheets[i];
        var node = sheet.ownerNode;
        var href = node.href;
        if (href) {
            href = this.removeReloadParameter(href);
            if (href === url) {
                // This is the stylesheet that should be reloaded
                found = true;
                if (!sheet.disabled) {
                    node.href = newURL;
                }
            }
        } else {
            href = node.getAttribute(this.ATTR_URL);
            if (href) {
                href = this.removeReloadParameter(href);
                if (href && node.tagName.toLowerCase() === 'style' && href === url) {
                    // This replacement of cross-origin stylesheet should be reloaded
                    found = true;
                    var request = new XMLHttpRequest();
                    request.open('GET', newURL, false);
                    request.send(null);
                    var status = request.status;
                    if (status === 0 || status === 200) { // 0 for files, 200 for http(s)
                        node.innerText = request.responseText;
                    } else {
                        console.log('Unable to download stylesheet: '+url);
                        console.log(request.statusText);
                        console.log(request.responseText);
                    }
                }
            }
        }
    }
    if (!found) {
        console.log('Cannot find the style sheet to reload: '+url);
    }
};

// Forces reload of the images with the given URL
NetBeans.reloadImage = function(url) {
    var found = false;
    url = this.removeReloadParameter(url);
    for (var i=0; i<document.images.length; i++) {
        var node = document.images[i];
        var src = node.src;
        if (src != null) {
            src = this.removeReloadParameter(src);
            if (src === url) {
                // This is the image that should be reloaded
                found = true;
                var newURL = this.addReloadParameter(url);
                node.src = newURL;
            }
        }
    }
    if (!found) {
        console.log('Cannot find the image to reload: '+url);
    }
};

// Forces reload and execution of the script with the given URL
NetBeans.reloadScript = function(url) {
    var found = false;
    url = this.removeReloadParameter(url);
    for (var i=0; i<document.scripts.length; i++) {
        var node = document.scripts[i];
        var src = node.src;
        if (src != null) {
            src = this.removeReloadParameter(src);
            if (src === url) {
                // This is the script that should be reloaded
                found = true;
                
                // Simple url modification doesn't trigger script
                // execution. Happily, the execution is triggered
                // when a new script tag is added.

                // We are using our own cloning method because node.cloneNode()
                // seems to copy also some kind of flag determining whether
                // the script was executed already or not. In other words,
                // script cloned using standard cloneNode() is not executed
                // when it is added.
                var newNode = document.createElement(node.tagName);
                for (var j=0; j<node.attributes.length; j++) {
                    var attr = node.attributes[j];
                    newNode.setAttribute(attr.name, attr.value);
                }
                newNode.innerHTML = node.innerHTML;

                // Modify src attribute to avoid caching
                var newURL = this.addReloadParameter(url);
                newNode.src = newURL;

                // Replace the old node by a cloned one.
                // This should trigger script execution.
                node.parentNode.replaceChild(newNode, node);
            }
        }
    }
    if (!found) {
        console.log('Cannot find the script to reload: '+url);
    }
};

NetBeans.getMatchedRules = function(handle) {
    var matchedStyle = [];
    var element = this.getElement(handle);
    if (this.getMatchedCSSRulesAvailable) {
        var changes = this.preGetMatchedCSSRulesUpdates();
        var rules = document.defaultView.getMatchedCSSRules(element);
        this.postGetMatchedCSSRulesUpdates(changes);
        if (rules) {
            // getMatchedCSSRules returns the least specific rule first
            // => process them in the opposite order to have the most specific first
            for (var i=rules.length-1; i>=0; i--) {
                var ruleInfo = new Object();
                var rule = rules[i];
                var url = rule.parentStyleSheet.ownerNode.getAttribute(this.ATTR_URL);
                if (url) {
                    // Cross-origin stylesheet replaced by embedded stylesheet
                    ruleInfo.sourceURL = url;
                } else {
                    ruleInfo.sourceURL = rule.parentStyleSheet.href;
                }
                ruleInfo.selector = rule.selectorText;
                var styleInfo = new Object();
                ruleInfo.style = styleInfo;
                var style = rule.style;
                for (var j=0; j<style.length; j++) {
                    var stylename = style[j];
                    var stylevalue = style[stylename];
                    styleInfo[stylename] = stylevalue;
                }
                matchedStyle.push(ruleInfo);
            }
        }
    } else if (getFirefoxMatchedRules) {
        // Invoke helper method in our Firefox extension
        matchedStyle = getFirefoxMatchedRules(element);
    } else {
        console.log('getMatchedRules() method not available.');
    }
    return matchedStyle;
};

// getMatchedCSSRules refuses to return requested information in various cases.
// This method attempts to work around these cases, i.e., it modifies
// the document temporarily such that getMatchedCSSRules() doesn't refuse to work
NetBeans.preGetMatchedCSSRulesUpdates = function() {
    var changes = new Object();
    // getMatchedCSSRules refuses to return any data when <base>
    // element is present in a document with file:// scheme
    var fileScheme = (document.URL.indexOf('file://') == 0);
    var bases = document.getElementsByTagName('base');
    if (fileScheme && (bases.length > 0)) {
        var base = bases[0];
        var parent = base.parentNode;
        var placeholder = document.createElement('meta');
        parent.replaceChild(placeholder, base);
        changes.baseOriginal = base;
        changes.baseReplacement = placeholder;
    }
    // Use replacements of cross-origin stylesheets
    changes.enabledSheets = [];
    changes.disabledSheets = [];
    var i;
    var documentOrigin = this.getOrigin(document.URL);
    for (i=0; i<document.styleSheets.length; i++) {
        var sheet = document.styleSheets[i];
        var sheetOrigin = this.getOrigin(sheet.href);
        var crossOrigin = (sheetOrigin !== documentOrigin);
        if (crossOrigin) {
            if (!sheet.disabled) {
                sheet.disabled = true;
                changes.disabledSheets.push(sheet);
                // The previous sheet is the replacement
                // (unless the replace failed)
                var replacement = document.styleSheets[i-1];
                if (replacement.ownerNode.getAttribute(this.ATTR_ARTIFICIAL)) {
                    replacement.disabled = false;
                    changes.enabledSheets.push(replacement);
                }
            }
        }
    }
    return changes;
}

// Reverts the changes performed by preGetMatchedCSSRulesUpdates()
NetBeans.postGetMatchedCSSRulesUpdates = function(changes) {
    // Restore <base> tag (if it was removed)
    if (changes.baseOriginal) {
        var base = changes.baseOriginal;
        var placeholder = changes.baseReplacement;
        placeholder.parentElement.replaceChild(base, placeholder);
    }
    // Restore state of stylesheets
    var i;
    for (i=0; i<changes.enabledSheets.length; i++) {
        changes.enabledSheets[i].disabled = true;
    }
    for (i=0; i<changes.disabledSheets.length; i++) {
        changes.disabledSheets[i].disabled = false;
    }
}

// Returns string representing the origin of the given URL. The returned
// values are compared to identify cross-origin resources.
NetBeans.getOrigin = function(url) {
    var origin;
    if (url) {
        origin = url;
        url = url.toLowerCase();
        var idx1 = url.indexOf('://');
        var scheme = url.substring(0,idx1);
        var http = scheme === 'http';
        var https = scheme === 'https';
        idx1 += 3;
        if ((http || https) && (idx1 !== -1)) {
            var idx2 = url.indexOf('/', idx1);
            if (idx2 !== -1) {
                origin = url.substring(0, idx2);
                var idx3 = url.indexOf(':', idx1);
                if (idx3 === -1) {
                    // port not present => add the default port
                    if (http) {
                        origin += ':80';
                    } else {
                        origin += ':443';
                    }
                }
                origin += '/';
            }
        }
    } else {
        origin = this.getOrigin(document.URL);
    }
    return origin;
}

// Inserts embedded copies of cross-origin stylesheets. This is a preparation
// for a workaround for the limitation of getMatchedCSSRules(). It doesn't
// return any information about rules from cross-origin stylesheets.
// We use the copies instead of the originals whenever we invoke getMatchedCSSRules().
NetBeans.insertCopiesOfCrossOriginStylesheets = function() {
    var i;
    var documentOrigin = this.getOrigin(document.URL);
    for (i=0; i<document.styleSheets.length; i++) {
        var sheet = document.styleSheets[i];
        var url = sheet.href;
        var sheetOrigin = this.getOrigin(url);
        if (sheetOrigin !== documentOrigin) {
            // Cross-origin stylesheet => replace it with embedded stylesheet
            var request = new XMLHttpRequest();
            request.open('GET', url, false);
            request.send(null);
            var status = request.status;
            if (status === 0 || status === 200) { // 0 for files, 200 for http(s)
                var fake = document.createElement('style');
                fake.setAttribute('type', 'text/css');
                fake.setAttribute(this.ATTR_URL, url);
                fake.setAttribute(this.ATTR_ARTIFICIAL, true);
                fake.innerText = request.responseText;
                var original = sheet.ownerNode;
                var parent = original.parentNode;
                parent.insertBefore(fake, original);
                // Inserted stylesheet is available in document.styleSheets immediately.
                // We disable the copy until it is needed.
                sheet = document.styleSheets[i].disabled = true;
                i++;
            } else {
                console.log('Unable to download stylesheet: '+url);
                console.log(request.statusText);
                console.log(request.responseText);
            }
        }
    }
}

// Inserts a glass-pane into the inspected page
NetBeans.insertGlassPane = function() {
    var self = this;
    var zIndex = 50000;
    
    // Canvas
    var canvas = document.createElement('canvas');
    canvas.id = this.GLASSPANE_ID;
    canvas.setAttribute(this.ATTR_ARTIFICIAL, true);
    canvas.style.position = 'fixed';
    canvas.style.top = 0;
    canvas.style.left = 0;
    canvas.style.zIndex = zIndex;
    canvas.style.pointerEvents = 'none';
    var getElementForEvent = function(event) {
        canvas.style.visibility = 'hidden';
        var element = document.elementFromPoint(event.clientX, event.clientY);
        // Do not select helper elements introduced by page inspection
        while (element.getAttribute(self.ATTR_ARTIFICIAL)) { 
            element = element.parentNode;
        }
        canvas.style.visibility = 'visible';
        return element;
    };

    // Selection handler
    canvas.addEventListener('click', function(event) {
        var element = getElementForEvent(event);
        // HACK: notify NetBeans
        element.setAttribute(self.ATTR_SELECTED, 'true');
        element.removeAttribute(self.ATTR_SELECTED);
    });

    // Mouse-over highlight
    var lastHighlighted = null;
    canvas.addEventListener('mousemove', function(event) {
        var element = getElementForEvent(event);
        if (lastHighlighted !== element) {
            lastHighlighted = element;
            // HACK: notify NetBeans
            element.setAttribute(self.ATTR_HIGHLIGHTED, 'true');
            element.removeAttribute(self.ATTR_HIGHLIGHTED);
        }
    });

    // Clear highlight when the mouse leaves the window
    canvas.addEventListener('mouseout', function(e) {
        if (e.toElement === null) {
            lastHighlighted = null;
            // HACK notify NetBeans
            canvas.setAttribute(self.ATTR_HIGHLIGHTED, 'false');
            canvas.removeAttribute(self.ATTR_HIGHLIGHTED);
        }
    });

    document.body.appendChild(canvas);
    
    // Selection Mode checkbox
    var toolbox = document.createElement('div');
    toolbox.setAttribute(this.ATTR_ARTIFICIAL, true);
    toolbox.style.position = 'fixed';
    toolbox.style.top = 0;
    toolbox.style.right = 0;
    toolbox.style.zIndex = zIndex;
    var selectionMode = document.createElement('input');
    selectionMode.type = 'checkbox';
    selectionMode.id = this.SELECTION_MODE_ID;
    selectionMode.setAttribute(this.ATTR_ARTIFICIAL, true);
    selectionMode.addEventListener('click', this.switchSelectionMode);
    toolbox.appendChild(selectionMode);
    var selectionText = document.createTextNode('Selection Mode');
    toolbox.appendChild(selectionText);
    document.body.appendChild(toolbox);

    window.addEventListener('scroll', this.repaintGlassPane);
    window.addEventListener('resize', this.repaintGlassPane);
    this.repaintGlassPane();
}

// Updates the selection mode according to the 'Select Mode' check-box
NetBeans.switchSelectionMode = function(event) {
    var checked = event.currentTarget.checked;
    var canvas = document.getElementById(NetBeans.GLASSPANE_ID);
    // Notify IDE about the change
    canvas.setAttribute(':netbeans_selection_mode', checked);
}

NetBeans.setSelectionMode = function(selectionMode) {
    var value = selectionMode ? 'auto' : 'none';
    var canvas = document.getElementById(NetBeans.GLASSPANE_ID);
    canvas.style.pointerEvents = value;
    var checkbox = document.getElementById(NetBeans.SELECTION_MODE_ID);
    if (checkbox != null) {
        checkbox.checked = selectionMode;
    }
}

// Repaints the glass-pane
NetBeans.repaintGlassPane = function() {
    var canvas = document.getElementById(NetBeans.GLASSPANE_ID); 
    if (canvas.getContext) {
        var ctx = canvas.getContext('2d'); 
        var width = window.innerWidth;
        var height = window.innerHeight;
        ctx.canvas.width = width;
        ctx.canvas.height = height;
        ctx.globalAlpha = 0.5;
        ctx.fillStyle = "#0000FF";
        NetBeans.paintElements(ctx, NetBeans.selection);
        ctx.globalAlpha = 0.25;
        NetBeans.paintElements(ctx, NetBeans.highlight);
    } else {
        console.log('canvas.getContext not supported!');
    }
}

NetBeans.paintElements = function(ctx, elements) {
    for (var i=0; i<elements.length; i++) {
        var selectedElement = elements[i];
        var rects = selectedElement.getClientRects();
        for (var j=0; j<rects.length; j++) {
            var rect = rects[j];
            ctx.fillRect(rect.left, rect.top, rect.width, rect.height);
            ctx.stroke();
        }
    }
}

//if (NetBeans.getMatchedCSSRulesAvailable) {
//  // Insert copies of cross-origin stylesheets
//  NetBeans.insertCopiesOfCrossOriginStylesheets();
//}

// Insert glass-pane into the inspected page
NetBeans.insertGlassPane();

}
