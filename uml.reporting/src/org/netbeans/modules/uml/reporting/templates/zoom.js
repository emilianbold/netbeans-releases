/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

function getEvent(event)
{
    if(event == undefined) {
        return window.event;
    }

    return event;
}

function prepareViewer(imageViewer, imgDir)
{
    for(var child = imageViewer.firstChild; child; child = child.nextSibling) {
        if(child.className == 'surface') {
            imageViewer.activeSurface = child;
            child.imageViewer = imageViewer;

        } else if(child.className == 'well') {
            imageViewer.tileWell = child;
            child.imageViewer = imageViewer;

        } else if(child.className == 'status') {
            imageViewer.status = child;
            child.imageViewer = imageViewer;
        
        }
    }
    
    var width = imageViewer.offsetWidth;
    var height = imageViewer.offsetHeight;
    var zoomLevel = fit_scale_index; // set initial image to the one that fits to the viewing area

    var center = {'x': 0, 'y': 0}; 

    imageViewer.style.width = width+'px';
    imageViewer.style.height = height+'px';
    
    var top = 0;
    var left = 0;
    for(var node = imageViewer; node; node = node.offsetParent) {
        top += node.offsetTop;
        left += node.offsetLeft;
    }

    imageViewer.dimensions = {

         // width and height of the viewer in pixels
         'width': width, 'height': height,

         // position of the viewer in the document, from the upper-left corner
         'top': top, 'left': left,

         // location and height of each tile; they're always square
         'imgDir': imgDir, 

         // zero or higher; big number == big image, lots of tiles
         'zoomLevel': zoomLevel,

         // initial viewer position
         // defined as window-relative x,y coordinate of upper-left hand corner of complete image
         // usually negative. constant until zoomLevel changes
         'x': center.x, 'y': center.y

    };

    imageViewer.start = {'x': 0, 'y': 0}; // this is reset each time that the mouse is pressed anew
    imageViewer.pressed = false;

    if(document.body.imageViewers == undefined) {
        document.body.imageViewers = [imageViewer];
        document.body.onmouseup = releaseViewer;

    } else {
        document.body.imageViewers.push(imageViewer);
    
    }
    prepareImage(imageViewer);
}

function prepareImage(imageViewer) {
    var activeSurface = imageViewer.activeSurface;
    var tileWell = imageViewer.tileWell;
    var dim = imageViewer.dimensions;

    tileWell.img = document.createElement('img');
    tileWell.img.className = 'tile';
    //imageViewer.img.style.width = dim.tileSize+'px';
    //imageViewer.img.style.height = dim.tileSize+'px';
    tileWell.appendChild(tileWell.img);
    setImage(imageViewer);
    
    activeSurface.onmousedown = pressViewer;
    positionImage(imageViewer, {'x': 0, 'y': 0}); // x, y should match imageViewer.start x, y
}

function positionImage(imageViewer, mouse) {
    var tileWell = imageViewer.tileWell;
    var dim = imageViewer.dimensions;
    var start = imageViewer.start;
    
    var image = imageViewer.tileWell.img;
    
    // check if we should restrict panning
    // x, y, x+w, y+h

    var x0 = dim.x + mouse.x - start.x;
    var y0 = dim.y + mouse.y - start.y;

    image.style.left = x0+'px';
    image.style.top = y0+'px';
}

function setImage(imageViewer) {
    var dim = imageViewer.dimensions;
    var src = dim.imgDir+'/'+g_images[dim.zoomLevel].path;
    imageViewer.tileWell.img.src = src;
    imageViewer.tileWell.img.width = g_images[dim.zoomLevel].width;
    imageViewer.tileWell.img.height = g_images[dim.zoomLevel].height;
}
function moveViewer(event)
{
    var imageViewer = this.imageViewer;
    var ev = getEvent(event);
    var mouse = localizeCoordinates(imageViewer, {'x': ev.clientX, 'y': ev.clientY});

    positionImage(imageViewer, {'x': mouse.x, 'y': mouse.y});
}

function localizeCoordinates(imageViewer, client)
{
    var local = {'x': client.x, 'y': client.y};

    for(var node = imageViewer; node; node = node.offsetParent) {
        local.x -= node.offsetLeft;
        local.y -= node.offsetTop;
    }
    
    return local;
}

function pressViewer(event)
{
    var imageViewer = this.imageViewer;
    var dim = imageViewer.dimensions;
    var ev = getEvent(event);
    var mouse = localizeCoordinates(imageViewer, {'x': ev.clientX, 'y': ev.clientY});

    imageViewer.pressed = true;
    imageViewer.tileWell.style.cursor = imageViewer.activeSurface.style.cursor = 'move';
    
    imageViewer.start = {'x': mouse.x, 'y': mouse.y};
    this.onmousemove = moveViewer;

}

function releaseViewer(event)
{
    var ev = getEvent(event);
    
    for(var i = 0; i < document.body.imageViewers.length; i += 1) {
        var imageViewer = document.body.imageViewers[i];
        var mouse = localizeCoordinates(imageViewer, {'x': ev.clientX, 'y': ev.clientY});
        var dim = imageViewer.dimensions;

        if(imageViewer.pressed) {
            imageViewer.activeSurface.onmousemove = null;
            //imageViewer.tileWell.style.cursor = imageViewer.activeSurface.style.cursor = 'default';
            imageViewer.pressed = false;

            dim.x += (mouse.x - imageViewer.start.x);
            dim.y += (mouse.y - imageViewer.start.y);
        }

    }
}

function zoomImage(imageViewer, mouse, direction)
{
    var dim = imageViewer.dimensions;
    
    if(mouse == undefined) {
        var mouse = {'x': dim.width / 2, 'y': dim.height / 2};
    }
    
    var before = dim.zoomLevel;
   
    dim.zoomLevel += direction;

    var zf = 0;
    if (dim.zoomLevel > 2) {
        dim.zoomLevel = 2;
        zf = 1;
    }
    if (dim.zoomLevel < 0) {
        dim.zoomLevel = 0;
        zf = 1;
    }
    if (!zf) {
	var after = dim.zoomLevel;
        zf = g_images[after].width/g_images[before].width;
    }
    dim.x = mouse.x - (mouse.x - dim.x)*zf ;
    dim.y = mouse.y - (mouse.y - dim.y)*zf ;

    imageViewer.start = mouse;
    setImage(imageViewer);
    positionImage(imageViewer, mouse);
}

function fitToWindow(imageViewer, mouse)
{
    var dim = imageViewer.dimensions;
    dim.zoomLevel = fit_scale_index;
    if(mouse == undefined) {
	var mouse = {'x': 0, 'y': 0};
    }
    dim.x = 0;
    dim.y= 0;
    imageViewer.start = mouse;
    setImage(imageViewer);
    positionImage(imageViewer, mouse);
}

function zoomImageUp(imageViewer, mouse)
{
    zoomImage(imageViewer, mouse, 1);
}

function zoomImageDown(imageViewer, mouse)
{
    zoomImage(imageViewer, mouse, -1);
}

function zoomImageFit(imageViewer, mouse)
{
    fitToWindow(imageViewer, mouse);
}
