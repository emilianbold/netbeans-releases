/*
 * Copyright (c) 2010, Oracle. All rights reserved.
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
 * * Neither the name of Oracle nor the names of its contributors
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

var DEFAULT_VALUE_CLASSNAME = "DefaultValue";

function switchClaimAndReassign() {
    var claim = document.getElementById("taskInputMenuClaim");
    var reassign = document.getElementById("taskInputMenuReassign");

    if (claim.style.display != "none") {
        claim.style.display = "none";
        reassign.style.display = "";
    } else {
        claim.style.display = "";
        reassign.style.display = "none";
    }
}

function clearDefaultValue(element) {
    if (element.className.indexOf(DEFAULT_VALUE_CLASSNAME) != -1) {
        element.className = element.className.
                replace(new RegExp("\\s*" + DEFAULT_VALUE_CLASSNAME + "\\s*"), " ").
                replace(/^\s+/, "").
                replace(/\s+$/, "");
        element.value = "";
    }
}

function restoreDefaultValue(element, value) {
    if (element.value == "") {
        element.className = (element.className + " " + DEFAULT_VALUE_CLASSNAME).
                replace(/^\s+/, "").
                replace(/\s+$/, "");
        element.value = value;
    }
}

function clearDefaultValues(elementIds) {
    for (var i = 0; i < elementIds.length; i++) {
        var element = document.getElementById(elementIds[i]);

        if (element.className.indexOf(DEFAULT_VALUE_CLASSNAME) != -1) {
            element.value = "";
        }
    }
}

function removeDefaultValue(elementId, defaultValue) {
    var element = document.getElementById(elementId);
    if (element.value == defaultValue) {
        element.value = "";
    }
}