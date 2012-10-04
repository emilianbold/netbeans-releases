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

// ID of canvas element that serves as a glass-pane
NetBeans.GLASSPANE_ID = 'netbeans_glasspane';

// Selected elements
NetBeans.selection = [];

// Next selection (under construction)
NetBeans.nextSelection = [];

// Highlighted elements
NetBeans.highlight = [];

// Next highlight (under construction)
NetBeans.nextHighlight = [];

// Initializes/clears the next selection
NetBeans.initNextSelection = function() {
    this.nextSelection = [];
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
        var element = getElementForEvent(event);
        if (self.lastHighlighted !== element) {
            self.lastHighlighted = element;
            // HACK: notify NetBeans
            element.setAttribute(self.ATTR_HIGHLIGHTED, 'set');
            element.removeAttribute(self.ATTR_HIGHLIGHTED);
        }
    });

    // Clear highlight when the mouse leaves the window
    canvas.addEventListener('mouseout', function(e) {
        if (e.toElement === null) {
            self.lastHighlighted = null;
            // HACK notify NetBeans
            canvas.setAttribute(self.ATTR_HIGHLIGHTED, 'clear');
            canvas.removeAttribute(self.ATTR_HIGHLIGHTED);
        }
    });

    document.body.appendChild(canvas);

    window.addEventListener('scroll', this.repaintGlassPane);
    window.addEventListener('resize', this.repaintGlassPane);
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

NetBeans.setSelectionMode = function(selectionMode) {
    var value = selectionMode ? 'auto' : 'none';
    var canvas = document.getElementById(NetBeans.GLASSPANE_ID);
    canvas.style.pointerEvents = value;
    this.lastHighlighted = null;
};

// Repaints the glass-pane
NetBeans.repaintGlassPane = function() {
    var canvas = document.getElementById(NetBeans.GLASSPANE_ID); 
    if (canvas === null) {
        console.log("canvas not found!");
    } else if (canvas.getContext) {
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
        ctx.fillStyle = "#0000FF";
        NetBeans.paintSelectedElements(ctx, NetBeans.selection);
        ctx.globalAlpha = 0.25;
        NetBeans.paintHighlightedElements(ctx, NetBeans.highlight);
    } else {
        console.log('canvas.getContext not supported!');
    }
};

NetBeans.paintSelectedElements = function(ctx, elements) {
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
            ctx.strokeStyle = '#0000FF';
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

NetBeans.paintHighlightedElements = function(ctx, elements) {
    for (var i=0; i<elements.length; i++) {
        var highlightedElement = elements[i];
        var rects = highlightedElement.getClientRects();
        for (var j=0; j<rects.length; j++) {
            var rect = rects[j];
            ctx.fillRect(rect.left, rect.top, rect.width, rect.height);
            ctx.stroke();
        }
    }
};

// Insert glass-pane into the inspected page
NetBeans.insertGlassPane();

}
