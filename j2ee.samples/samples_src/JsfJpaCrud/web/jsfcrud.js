/*
 * Copyright (c) 2008, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
var jsfcrud = {};
jsfcrud.busyImagePath = '/JsfJpaCrud/faces/busy.gif';
jsfcrud.isDynamicFacesAvailable = typeof DynaFaces != 'undefined';
jsfcrud.canAjaxEnableForm = true;
if (!jsfcrud.isDynamicFacesAvailable) {
    jsfcrud.canAjaxEnableForm = false;
}
if (jsfcrud.isDynamicFacesAvailable) {
    Form.Element.Serializers.selectOne = function(element) {
        var value = '', opt, index = element.selectedIndex;
        if (index >= 0) {
            opt = element.options[index];
            value = opt.value;
        }
        return [element.name, value];
    };
}
jsfcrud.postReplace = function(element, markup) {
    markup.evalScripts();
    setTimeout(function(){jsfcrud.ajaxEnableForm({options: {postReplace:jsfcrud.postReplace}});}, 20);
}
jsfcrud.ajaxEnableForm = function(args) {
    if (!jsfcrud.canAjaxEnableForm) {
        return;
    }
    
    if (typeof args == undefined || args == null) {
        args = {};
    }
    
    if (typeof args.options == 'undefined') {
        args.options = {};
    }
    
    var sourceElement = null;
    if (typeof args.sourceElementId != 'undefined' && args.sourceElementId != null) {
        sourceElement = document.getElementById(args.sourceElementId);
    }
    
    if (typeof args.formId == 'undefined' || args.formId == null) {
        args.formId = 0;
    }
    
    //insert busy image we'll display when sending an Ajax request
    jsfcrud.insertBusyImage();
    
    document.forms[args.formId].submit = function() {
        var busyImage = document.getElementById('busyImage');
        if (busyImage) {
            busyImage.style.display = 'block';
        }
        DynaFaces.fireAjaxTransaction(sourceElement, args.options);
    };
};

jsfcrud.insertBusyImage = function() {
    var busyImage = document.createElement('img');
    busyImage.id = 'busyImage';
    busyImage.src = jsfcrud.busyImagePath;
    busyImage.style.display = 'none';
    document.body.insertBefore(busyImage, document.forms[0]);
}

setTimeout(function(){jsfcrud.ajaxEnableForm({options: {postReplace:jsfcrud.postReplace}});}, 20);
