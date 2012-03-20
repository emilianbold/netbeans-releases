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

self.on('message', function(message) {
    var result = new Object();
    result.message = 'eval';
    result.id = message.id;
    var type = message.message;
    if (type === 'eval') {
        // Do not remove this variable - it serves as an API.
        // Scripts that want to send some message back to IDE call this method.
        var postMessageToNetBeans = function(message) {
            try {
                self.postMessage(message);
            } catch (e) {
                // This page has been destroyed/detached
            }
        };
        // Do not remove this function - it serves as an API.
        // Scripts that want to get matched rules call this method.
        var matchedRulesRequest = 'netbeansMatchedRulesRequest';
        var getFirefoxMatchedRules = function(element) {
            // Throwing the exception avoids posting the result of this
            // script back to IDE (the result of this script is not
            // important, the real result is produced by the main script).
            throw {
                name: matchedRulesRequest,
                element: simpleElementHandle(element)
            };
        };
        try {
            result.result = eval(message.script);
            result.status = 'ok';
            postMessageToNetBeans(result);
        } catch (err) {
            if (err.name && err.name === matchedRulesRequest) {
                // Evaluation failed intentionally - matched rules must be
                // obtained by the main script of the addon => passing
                // the request there
                postMessageToNetBeans({
                    message: 'matchedRules',
                    element: err.element,
                    id: message.id
                });
            } else {
                result.status = 'error';
                console.log('Problem during script evaluation!');
                console.log(JSON.stringify(message));
                if (err.name && err.message) {
                    result.result = err.name + ': ' + err.message;
                    console.log(result.result);
                } else {
                    result.result = err;
                    console.log(err);
                }
                postMessageToNetBeans(result);
            }
        }
    } else {
        console.log('Ignoring unexpected message from the background page!');
        console.log(message);
    }
});

// Notify the main script that this script is ready
self.postMessage({
    message: 'ready'
});

// Produces simple description of how to locate the element in the DOM.
// Unfortunately, we cannot pass the reference to the element to
// main script directly. Hence, we use this method to let the main
// script know how to locate the element.
var simpleElementHandle = function(element) {
    var handle = [];
    var parent = element.parentNode;
    while (parent && parent.nodeType === 1) {
        var index = 0;
        var child = parent.firstChild;
        while (child) {
            if (child.nodeType === 1) {
                if (child === element) {
                    handle.push(index);
                    break;
                } else {
                    index++;
                }
            }
            child = child.nextSibling;
        }
        element = parent;
        parent = parent.parentNode;
    }
    return handle.reverse();
}
