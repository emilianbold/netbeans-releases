window.onload = function() {
    (function() {

        var selectedElements = [];

        function addListener(element, event, callback) {
            if (window.addEventListener) {
                element.addEventListener(event, callback);
            } else {
                element.attachEvent("on" + event, callback);
            }
        }

        function getElementXPath(element) {

            if (element.id !== "" && element.getAttribute("id") !== null)
                return 'id("' + element.id + '")';
            if (element === document.body)
                return element.tagName;

            var offset = 0;
            var siblings = element.parentNode.childNodes;
            for (var i = 0, max = siblings.length; i < max; i++) {
                if (siblings[i] === element)
                    return getElementXPath(element.parentNode) + '/' + element.tagName + '[' + (offset + 1) + ']';
                if (siblings[i].nodeType === 1 && siblings[i].tagName === element.tagName)
                    offset++;
            }
        }

        function init() {
            var links = document.querySelectorAll("a");
            for (var i = 0, max = links.length; i < max; i++) {
                addListener(links[i], "click", function(e) {
                    if (e.preventDefault)
                        e.preventDefault();
                    e.returnValue = false;
                    return false;
                });
            }
            var inputs = document.querySelectorAll("input");
            for (var i = 0, max = inputs.length; i < max; i++) {
                inputs[i].setAttribute("disabled", "disabled");
            }

            var allElements = document.querySelectorAll("body *");

            for (var i = 0, max = allElements.length; i < max; i++) {
                var el = allElements[i];
                if (isBlockLikeElement(el)) {
                    addListener(el, "click", function(e) {
                        if (e.currentTarget === e.target) {
                            var c = e.target.getAttribute("class");
                            if (c !== null && c.indexOf("sn_selected") > -1) {
                                e.target.setAttribute("class", c.replace("sn_selected", ""));
                                sendElementClicked(e.target, "removed");
                                selectedElements = selectedElements.filter(function(el) {
                                    return el !== e.target;
                                });
                            } else {
                                c === null ? e.target.setAttribute("class", "sn_selected") : e.target.setAttribute("class", c + " sn_selected");
                                sendElementClicked(e.target, "added");
                                selectedElements.push(e.target);
                            }
                        }
                    });
                }
            }
            window.console.log("... " + selectedElements.length);
        }




        function isBlockLikeElement(element) {
            var cStyle = element.currentStyle || window.getComputedStyle(element, "");
            return cStyle.display.length > 0 && cStyle.display !== "none";
        }

        function sendElementClicked(element, action) {
            var xpath = getElementXPath(element);
            parent.postMessage(JSON.stringify({"xpath": xpath.toLowerCase(), "action": action, "element": element.nodeName, "type": "tutorial"}), '*');
        }

        function clearSelection() {
            selectedElements.forEach(function(e) {
                var c = e.getAttribute("class");
                if (c !== null && c.indexOf("sn_selected") > -1) {
                    e.setAttribute("class", c.replace("sn_selected", ""));
                }
            });
            selectedElements = [];

        }

        function handleMessage(msg) {
            var _data = msg.data;
            if (typeof msg.data === "string") {
                _data = JSON.parse(msg.data);
            }

            switch (_data.action) {
                case "highlight":
                    clearSelection();
                    for (var i = 0, max = _data.elements.length; i < max; i++) {
                        var list = document.evaluate(_data.elements[i], document, null, XPathResult.ANY_TYPE, null);
                        var _e = list.iterateNext();
                        while (_e) {
                            if (_e) {
                                selectedElements.push(_e);
                                _e = list.iterateNext();
                            }
                        }



                    }
                    selectedElements.forEach(function(e) {
                        var c = e.getAttribute("class");
                        if (c === null || c.indexOf("sn_selected") < 0) {
                            c === null ? e.setAttribute("class", "sn_selected") : e.setAttribute("class", c + " sn_selected");
                        }
                    });
                    break;
                default: // clear
                    clearSelection();
                    break;
            }
        }

        init();
        addListener(window, "message", handleMessage);

        function countWords() {
            function getText(el) {
                var ret = "";
                if (!el) {
                    return "";
                }
                var length = el.childNodes.length;
                for (var i = 0; i < length; i++) {
                    var node = el.childNodes[i];
                    if (node.nodeType !== 8) {
                        ret += node.nodeType !== 1 ? node.nodeValue : getText(node);
                    }
                }
                return ret;
            }

            var words = getText(document.querySelector("#middle"));
            return words.split(/\s+/).length;

        }
        var attempts = 0;
        var intervalId = window.setInterval(function() {
            var words = countWords();
            attempts++;
            if (words > 0) {
                parent.postMessage(JSON.stringify({"words": words, "action": "wordCount"}), '*');
                window.clearInterval(intervalId);
                return;
            }
            if (attempts > 5) {
                window.clearInterval(intervalId);
                return;
            }
        }, 1000);
    })();
};
