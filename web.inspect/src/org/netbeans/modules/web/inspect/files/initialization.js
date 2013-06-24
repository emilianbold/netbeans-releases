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

// Name of attribute used to mark document elements created by NetBeans
NetBeans.ATTR_ARTIFICIAL = ':netbeans_generated';

// Name of attribute used to mark (temporarily) selected elements
NetBeans.ATTR_SELECTED = ':netbeans_selected';

// Name of attribute used to mark (temporarily) highlighted elements
NetBeans.ATTR_HIGHLIGHTED = ':netbeans_highlighted';

// Name of the class used to simulate hovering
NetBeans.CLASS_HOVER = '-netbeans-hover';

// ID of canvas element that serves as a glass-pane
NetBeans.GLASSPANE_ID = 'netbeans_glasspane';

// Selected elements
NetBeans.selection = [];

// Next selection (under construction)
NetBeans.nextSelection = [];

// Selected elements that match the selected rule
NetBeans.ruleSelection = [];

// Next selection (under construction)
NetBeans.nextRuleSelection = [];

// Highlighted elements
NetBeans.highlight = [];

// Next highlight (under construction)
NetBeans.nextHighlight = [];

// Determines whether the enclosing browser window is active
NetBeans.windowActive = true;

// Initializes/clears the next selection
NetBeans.initNextSelection = function() {
    this.nextSelection = [];
};

// Initializes/clears the next selection
NetBeans.initNextRuleSelection = function() {
    this.nextRuleSelection = [];
};

// Initializes/clears the next highlight
NetBeans.initNextHighlight = function() {
    this.nextHighlight = [];
    this.lastHighlighted = null;
};

// Adds an element into the next selection
NetBeans.addElementToNextSelection = function(element) {
    if (this.nextSelection.indexOf(element) === -1) {
        this.nextSelection.push(element);
    }
};

// Adds an element into the next selection
NetBeans.addElementToNextRuleSelection = function(element) {
    if (this.nextRuleSelection.indexOf(element) === -1) {
        this.nextRuleSelection.push(element);
    }
};

// Adds an element into the next highlight
NetBeans.addElementToNextHighlight = function(element) {
    if (this.nextHighlight.indexOf(element) === -1) {
        this.nextHighlight.push(element);
        this.lastHighlighted = element;
    }
};

// Finishes the next selection, i.e., switches the next selection to current selection
NetBeans.finishNextSelection = function() {
    this.selection = this.nextSelection;
    this.repaintGlassPane();
};

// Finishes the next selection, i.e., switches the next selection to current selection
NetBeans.finishNextRuleSelection = function() {
    this.ruleSelection = this.nextRuleSelection;
    this.repaintGlassPane();
};

// Finishes the next highlight, i.e., switches the next highlight to current highlight
NetBeans.finishNextHighlight = function() {
    this.highlight = this.nextHighlight;
    this.repaintGlassPane();
};

// The last element the mouse was hovering over
NetBeans.lastHighlighted = null;

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
    var iOS = (navigator.userAgent.match(/(iPad|iPhone|iPod)/g) ? true : false) ;
    var getElementForEvent = function(event) {
        canvas.style.visibility = 'hidden';
        var element = iOS ? 
            document.elementFromPoint(event.pageX - window.pageXOffset, event.pageY - window.pageYOffset) :
            document.elementFromPoint(event.clientX, event.clientY);
        // Do not select helper elements introduced by page inspection
        while (element.getAttribute(self.ATTR_ARTIFICIAL)) { 
            element = element.parentNode;
        }
        canvas.style.visibility = 'visible';
        return element;
    };

    //Click event does not work on iOS
    var eventname = ( iOS ? 'touchstart' : 'click' );

    // Selection handler
    canvas.addEventListener(eventname, function(event) {
        var element = getElementForEvent(event);
        var ctrl = event.ctrlKey;
        var meta = event.metaKey;
        var value;
        if (ctrl || meta) {
            var index = NetBeans.selection.indexOf(element);
            if (index === -1) {
                value = 'add';
            } else {
                value = 'remove';
            }
        } else {
            value = 'set';
        }
        // HACK: notify NetBeans
        element.setAttribute(self.ATTR_SELECTED, value);
        element.removeAttribute(self.ATTR_SELECTED);
    });

    // Mouse-over highlight
    canvas.addEventListener('mousemove', function(event) {
        if (self.windowActive) {
            var element = getElementForEvent(event);
            if (self.lastHighlighted !== element) {
                self.lastHighlighted = element;
                // HACK: notify NetBeans
                element.setAttribute(self.ATTR_HIGHLIGHTED, 'set');
                element.removeAttribute(self.ATTR_HIGHLIGHTED);
            }
        }
    });

    // Clear highlight when the mouse leaves the window
    window.addEventListener('mouseout', function(e) {
        if (e.toElement === null) {
            NetBeans.clearHighlight();
        }
    });

    // Clear highlight when a context menu is shown
    window.addEventListener('contextmenu', function() {
        // Some mouse move events are fired shortly after
        // this event => postpone processing of this event a bit
        setTimeout(NetBeans.clearHighlight, 100);
    });

    document.body.appendChild(canvas);

    window.addEventListener('scroll', this.paintGlassPane);
    window.addEventListener('resize', this.paintGlassPane);
    var MutationObserver = window.MutationObserver || window.WebKitMutationObserver;
    if (MutationObserver) {
        var observer = new MutationObserver(function(mutations) {
            var importantChange = false;
            for (var i=0; i<mutations.length; i++) {
                var target = mutations[i].target;
                // Ignore changes in elements injected by NetBeans
                if (!target.hasAttribute(self.ATTR_ARTIFICIAL)) {
                    importantChange = true;
                    break;
                }
            }
            if (importantChange) {
                self.repaintGlassPane();
            }
        });
        observer.observe(document, { childList: true, subtree: true, attributes: true });
    } else {
        window.setInterval(this.repaintGlassPane, 500);
    }
    this.repaintGlassPane();
};

NetBeans.clearHighlight = function() {
    NetBeans.lastHighlighted = null;
    // Notify NetBeans
    var canvas = document.getElementById(NetBeans.GLASSPANE_ID);
    if (canvas !== null) {
        canvas.setAttribute(NetBeans.ATTR_HIGHLIGHTED, 'clear');
        canvas.removeAttribute(NetBeans.ATTR_HIGHLIGHTED);
    }
};

NetBeans.setSelectionMode = function(selectionMode) {
    var value = selectionMode ? 'auto' : 'none';
    var canvas = document.getElementById(NetBeans.GLASSPANE_ID);
    canvas.style.pointerEvents = value;
    this.lastHighlighted = null;
    var element = NetBeans.lastHoveredElement;
    this.selectionMode = selectionMode;
    this.handleBlockedEvents();

    // Add/remove the class that simulates hovering
    if (selectionMode) {
        while (element !== null) {
            element.classList.add(NetBeans.CLASS_HOVER);
            element = element.parentElement;
        }
    } else {
        while (element !== null) {
            element.classList.remove(NetBeans.CLASS_HOVER);
            if (element.classList.length === 0) {
                // add() + remove() may result in class="" attribute
                // that breaks our source-browser matching algorithm
                element.removeAttribute('class');
            }
            element = element.parentElement;
        }
    }
};

// Repaints the glass-pane
NetBeans.repaintRequested = false;
NetBeans.repaintGlassPane = function() {
    if (!NetBeans.repaintRequested) {
        NetBeans.repaintRequested = true;
        setTimeout(NetBeans.paintGlassPane, 100);
    }
};

NetBeans.paintGlassPane = function() {
    NetBeans.repaintRequested = false;
    var canvas = document.getElementById(NetBeans.GLASSPANE_ID); 
    if (canvas !== null && canvas.getContext) {
        var ctx = canvas.getContext('2d'); 
        var width = window.innerWidth;
        var height = window.innerHeight;
        if (ctx.canvas.width === width && ctx.canvas.height === height) {
            ctx.clearRect(0, 0, width, height);
        } else {
            ctx.canvas.width = width;
            ctx.canvas.height = height;
        }
        ctx.globalAlpha = 0.5;
        NetBeans.paintSelectedElements(ctx, NetBeans.ruleSelection, '#00FF00');
        NetBeans.paintSelectedElements(ctx, NetBeans.selection, '#0000FF');
        ctx.globalAlpha = 0.25;
        NetBeans.paintHighlightedElements(ctx, NetBeans.highlight);
    }
};

NetBeans.paintSelectedElements = function(ctx, elements, color) {
    ctx.fillStyle = color;
    ctx.lineWidth = 2;
    var dash = 3;
    var dashedLine = function(x, y, dx, dy, length) {
        var d = Math.max(dx,dy);
        var i;
        for (i=0; i<length/(2*d); i++) {
            ctx.moveTo(x+Math.min(2*i*dx,length),y+Math.min(2*i*dy,length));
            ctx.lineTo(x+Math.min(2*i*dx+dx,length),y+Math.min(2*i*dy+dy,length));
        }
    };
    for (var i=0; i<elements.length; i++) {
        var selectedElement = elements[i];
        var rects = selectedElement.getClientRects();
        for (var j=0; j<rects.length; j++) {
            var rect = rects[j];
            ctx.strokeStyle = color;
            ctx.beginPath();
            dashedLine(rect.left,rect.top,dash,0,rect.width);
            dashedLine(rect.left,rect.top+rect.height,dash,0,rect.width);
            dashedLine(rect.left,rect.top,0,dash,rect.height);
            dashedLine(rect.left+rect.width,rect.top,0,dash,rect.height);
            ctx.stroke();

            ctx.strokeStyle = '#FFFFFF';
            ctx.beginPath();
            dashedLine(rect.left+dash,rect.top,dash,0,rect.width-dash);
            dashedLine(rect.left+dash,rect.top+rect.height,dash,0,rect.width-dash);
            dashedLine(rect.left,rect.top+dash,0,dash,rect.height-dash);
            dashedLine(rect.left+rect.width,rect.top+dash,0,dash,rect.height-dash);
            ctx.stroke();
            
            ctx.beginPath();
        }
    }
};

// Fills the area/frame between the given outer and inner rectangles
NetBeans.paintFrame = function(ctx, inner, outer) {
    var height = inner.top-outer.top;
    if (height > 0) {
        ctx.fillRect(outer.left, outer.top, outer.width, height);
    }
    height = outer.top+outer.height-inner.top-inner.height;
    if (height > 0) {
        ctx.fillRect(outer.left, inner.top+inner.height, outer.width, height);
    }
    var width = inner.left-outer.left;
    if (width > 0) {
        ctx.fillRect(outer.left, inner.top, width, inner.height);
    }
    width = outer.left+outer.width-inner.left-inner.width;
    if (width > 0) {
        ctx.fillRect(inner.left+inner.width, inner.top, width, inner.height);
    }
};

NetBeans.paintHighlightedElements = function(ctx, elements) {
    ctx.save();
    var fontSize = 10;
    ctx.font = fontSize + 'pt sans-serif';
    ctx.lineWidth = 1;
    for (var i=0; i<elements.length; i++) {
        var highlightedElement = elements[i];
        var rects = highlightedElement.getClientRects();
        var style = window.getComputedStyle(highlightedElement);

        var inline = (style.display === 'inline');
        var marginTop = inline ? 0 : parseInt(style.marginTop);
        var marginBottom = inline ? 0 : parseInt(style.marginBottom);

        for (var j=0; j<rects.length; j++) {
            // Box model
            var first = (j === 0) || !inline;
            var last = (j === rects.length-1) || !inline;
            
            var borderRect = rects[j];

            var marginLeft = first ? parseInt(style.marginLeft) : 0;
            var marginRight = last ? parseInt(style.marginRight) : 0;

            if ((borderRect.width === 0 || borderRect.height === 0)
                    && marginLeft === 0 && marginRight === 0) {
                continue;
            }

            var borderLeft = first ? parseInt(style.borderLeftWidth) : 0;
            var borderRight = last ? parseInt(style.borderRightWidth) : 0;

            var paddingLeft = first ? parseInt(style.paddingLeft) : 0;
            var paddingRight = last ? parseInt(style.paddingRight) : 0;

            var marginRect = {
                left: borderRect.left - marginLeft,
                top: borderRect.top - marginTop,
                height: borderRect.height + marginTop + marginBottom,
                width: borderRect.width + marginLeft + marginRight
            };
            var paddingRect = {
                left: borderRect.left + borderLeft,
                top: borderRect.top + parseInt(style.borderTopWidth),
                height: borderRect.height - parseInt(style.borderTopWidth) - parseInt(style.borderBottomWidth),
                width: borderRect.width - borderLeft - borderRight
            };
            var contentRect = {
                left: paddingRect.left + paddingLeft,
                top: paddingRect.top + parseInt(style.paddingTop),
                height: paddingRect.height - parseInt(style.paddingTop) - parseInt(style.paddingBottom),
                width: paddingRect.width - paddingLeft - paddingRight
            };

            ctx.fillStyle = '#FF8800';
            this.paintFrame(ctx, borderRect, marginRect);

            ctx.fillStyle = '#FFFF00';
            this.paintFrame(ctx, paddingRect, borderRect);

            ctx.fillStyle = '#00FF00';
            this.paintFrame(ctx, contentRect, paddingRect);

            ctx.fillStyle = '#0000FF';
            ctx.fillRect(contentRect.left, contentRect.top, contentRect.width, contentRect.height);

            // Label
            var oldAlpha = ctx.globalAlpha;
            ctx.globalAlpha = 1;
            var text = highlightedElement.tagName.toLowerCase();
            var id = highlightedElement.id;
            if (id !== '') {
                text += '#' + id;
            }
            var classList = highlightedElement.classList;
            if (classList.length !== 0) {
                for (var k=0; k<classList.length; k++) {
                    var clazz = classList[k];
                    // Do not show the class that simulates hovering
                    if (clazz !== NetBeans.CLASS_HOVER) {
                        text += '.' + clazz;
                    }
                }
            }
            var text = ' ' + text + ' ' + borderRect.width + 'px \xD7 ' + borderRect.height + 'px ';

            var width = ctx.measureText(text).width;
            var x = marginRect.left;
            var y = marginRect.top + marginRect.height + 2*fontSize;

            ctx.strokeStyle = '#000000';
            ctx.strokeRect(x, y-1.5*fontSize, width, 2*fontSize);
            ctx.fillStyle = '#FFFF88';
            ctx.fillRect(x, y-1.5*fontSize, width, 2*fontSize);
            ctx.fillStyle = '#000000';
            ctx.fillText(text, x, y);
            ctx.globalAlpha = oldAlpha;

            ctx.stroke();
        }
    }
    ctx.restore();
};

// The first blocked mouseout event (that will be redispatched once
// the selection mode is turned off).
NetBeans.blockedMouseOut = null;

// Handles blocked events. This method is invoked when
// the selection mode is switched.
NetBeans.handleBlockedEvents = function() {
    if (this.selectionMode) {
        this.blockedMouseOut = null;
    } else {
        if (this.blockedMouseOut !== null) {
            var event = document.createEvent('MouseEvents');
            event.initMouseEvent('mouseout', true, true, window,
                0, 0, 0, 0, 0, false, false, false, false, 0, null);
            this.blockedMouseOut.target.dispatchEvent(event);
        }
    }
};

/** The last element we were hovering over. */
NetBeans.lastHoveredElement = null;

// Filters/blocks some mouse events when Select/Inspect mode is turned on.
// This, for example, allows design/selection of JavaScript-based menus.
NetBeans.installMouseEventFilters = function() {
    var blockingListener = function(e) {
        if (NetBeans.selectionMode && (e.target.id !== NetBeans.GLASSPANE_ID)) {
            e.stopImmediatePropagation();
            e.preventDefault();
            if (e.type === 'mouseout' && NetBeans.blockedMouseOut === null) {
                // Save the first mouseout event
                NetBeans.blockedMouseOut = e;
            }
        }
    };
    var mousedownListener = function(e) {
        if ((e.clientX < document.documentElement.clientWidth)
                && (e.clientY < document.documentElement.clientHeight)) {
            blockingListener(e);
        }
    };
    var mousemoveListener = function(e) {
        if (!NetBeans.selectionMode) {
            NetBeans.lastHoveredElement = e.target;
        }
        blockingListener(e);
    };
    var mouseoutListener = function(e) {
        if (!NetBeans.selectionMode && (e.relatedTarget === null)) {
            NetBeans.lastHoveredElement = null;
        }
        blockingListener(e);
    };
    document.documentElement.addEventListener('click', blockingListener, true);
    document.documentElement.addEventListener('contextmenu', blockingListener, true);
    document.documentElement.addEventListener('dblclick', blockingListener, true);
    document.documentElement.addEventListener('mousedown', mousedownListener, true);
    document.documentElement.addEventListener('mouseenter', blockingListener, true);
    document.documentElement.addEventListener('mouseleave', blockingListener, true);
    document.documentElement.addEventListener('mousemove', mousemoveListener, true);
    document.documentElement.addEventListener('mouseout', mouseoutListener, true);
    document.documentElement.addEventListener('mouseover', blockingListener, true);
    document.documentElement.addEventListener('mouseup', blockingListener, true);
    document.documentElement.addEventListener('mousewheel', blockingListener, true);
};

NetBeans.setWindowActive = function(active) {
    this.windowActive = active;
    if (!active) {
        this.clearHighlight();
    }
};

// Replaces all occurences of oldString by newString
// in all CSS rules in all style-sheets in the document
NetBeans.replaceInCSSSelectors = function(oldString, newString) {
    var re = new RegExp(oldString, 'g');
    var styleSheets = document.styleSheets;
    var i;
    for (i=0; i<styleSheets.length; i++) {
        var rules = styleSheets[i].cssRules;
        var j;
        for (j=0; j<rules.length; j++) {
            var rule = rules[j];
            var oldSelector = rule.selectorText;
            var newSelector = oldSelector.replace(re, newString);
            if (oldSelector !== newSelector) {
                rule.selectorText = newSelector;
            }
        }
    }
};

// Cancels the inspection of the page
NetBeans.releasePage = function() {
    NetBeans.setSelectionMode(false);
    var canvas = document.getElementById(NetBeans.GLASSPANE_ID); 
    canvas.parentNode.removeChild(canvas);
};

// Insert glass-pane into the inspected page
NetBeans.insertGlassPane();

NetBeans.installMouseEventFilters();

}
