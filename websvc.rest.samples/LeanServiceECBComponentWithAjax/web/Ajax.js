/*
 * Copyright (c) 2009, Sun Microsystems, Inc. All rights reserved.
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

/* An Ajax client sending HTTP requests to get, create, delete or update REST resources specified by the application.
 * The response, obtained in JSON format, is used to modify orders' table in index.html page.
 *
 author: Milan Kuchtiak */

getXmlHttpRequest = function() {
    var req = null;
    try {
        // Firefox, Safari, IE7
        req = new XMLHttpRequest();
    } catch (e) {
        try {
            // IE
            req = new ActiveXObject('MSXML2.XMLHTTP');
        } catch (e) {
            try {
                // IE
                req = new ActiveXObject('Microsoft.XMLHTTP');
            } catch (e) {
                alert("Your browser does not support AJAX!");
                return null;
            }
        }

    }
    return req;
}

createOrder = function(amount, productId, cust) {
    var req = getXmlHttpRequest();
    var method = 'PUT';
    var url="resources/orders";
    req.open(method, url, true);
    req.setRequestHeader('Accept', "application/json");
    req.setRequestHeader('Content-Type', "application/xml");
    req.onreadystatechange = function() {
        if (req.readyState == 4) {
            addJSONOrder(req.responseText);
        }
    }
    req.send("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+
        "<order><amount>"+amount+"</amount><productId>"+productId+"</productId><customer>"+cust+"</customer></order>");
}

showOrders = function() {
    var req = getXmlHttpRequest();
    var method = 'GET';
    var url="resources/orders";

    req.open(method, url, true);
    req.setRequestHeader('Accept', "application/json");
    req.onreadystatechange = function() {
        if (req.readyState == 4) {
            showJSONResponse(req.responseText);
        }
    }
    req.send(null);
}

deliver = function(orderId) {
    var req = getXmlHttpRequest();
    var method = 'POST';
    var url="resources/orders/"+orderId+"/deliver";

    req.open(method, url, true);
    req.onreadystatechange = function() {
        if (req.readyState == 4) {
            setDelivered(orderId);
        }
    }
    req.send(null);
}

pay = function(orderId) {
    var req = getXmlHttpRequest();
    var method = 'POST';
    var url="resources/orders/"+orderId+"/pay";

    req.open(method, url, true);
    req.onreadystatechange = function() {
        if (req.readyState == 4) {
            setPaid(orderId);
        }
    }
    req.send(null);
}

deleteOrder = function(orderId) {
    var req = getXmlHttpRequest();
    var method = 'DELETE';
    var url="resources/orders/"+orderId;

    req.open(method, url, true);
    req.onreadystatechange = function() {
        if (req.readyState == 4) {
            deleteLine(orderId);
        }
    }
    req.send(null);
}

showJSONResponse = function(response) {

    var output = document.getElementById("output");

    if (output.firstChild) {
        output.removeChild(output.firstChild)
    }

    if (!response || response == 'null') {
        var status = document.getElementById("status");
        status.innerHTML = 'No Orders';
        return;
    }

    var resp = '('+response+')';
    var json = eval(resp);

    var table = createTable(output);

    for (var i=0; i<json.order.length; i++) {
        appendLine(table, json.order[i]);
    }
}

addJSONOrder = function(response) {
    var output = document.getElementById("output");

    var table = output.firstChild;
    if (!table) {
        table = createTable(output);
        var status = document.getElementById("status");
        status.innerHTML = "";
    }

    var resp = '('+response+')';
    var json = eval(resp);

    appendLine(table, json);
}

createTable = function(documentElement) {
    var table = document.createElement("table");
    table.className = "tabMain";
    table.id = "orderTab";

    var tr = document.createElement("tr");

    var th = document.createElement("th");
    th.appendChild(document.createTextNode('Order Id'));
    tr.appendChild(th);

    th = document.createElement("th");
    th.appendChild(document.createTextNode('Product Id'));
    tr.appendChild(th);

    th = document.createElement("th");
    th.appendChild(document.createTextNode('Amount'));
    tr.appendChild(th);

    th = document.createElement("th");
    th.appendChild(document.createTextNode('Customer'));
    tr.appendChild(th);

    th = document.createElement("th");
    th.appendChild(document.createTextNode('Delivered'));
    tr.appendChild(th);

    th = document.createElement("th");
    th.appendChild(document.createTextNode('Paid'));
    tr.appendChild(th);

    table.appendChild(tr);
    documentElement.appendChild(table);

    return table;
}

appendLine = function(table, order) {
    var tr = document.createElement("tr");
    tr.id = 'order_'+order.id;
    
    var td = document.createElement("td");
    var span = document.createElement("span");
    span.innerHTML = order.id;
    td.appendChild(span);
    tr.appendChild(td);

    td = document.createElement("td");
    span = document.createElement("span");
    span.innerHTML = order.productId;
    td.appendChild(span);
    tr.appendChild(td);

    td = document.createElement("td");
    span = document.createElement("span");
    span.innerHTML = order.amount;
    td.appendChild(span);
    tr.appendChild(td);

    td = document.createElement("td");
    span = document.createElement("span");
    span.innerHTML = order.customer;
    td.appendChild(span);
    tr.appendChild(td);

    td = document.createElement("td");
    span = document.createElement("span");
    span.id = 'deliver'+order.id;
    span.innerHTML = order.delivered;
    td.appendChild(span);
    tr.appendChild(td);

    td = document.createElement("td");
    span = document.createElement("span");
    span.id = 'pay'+order.id;
    span.innerHTML = order.paid;
    td.appendChild(span);
    tr.appendChild(td);

    td = document.createElement("td");
    var button = document.createElement("input");
    button.type = 'button';
    button.value='Deliver';
    button.onclick = function() {
        deliver(order.id);
    }
    td.appendChild(button);
    tr.appendChild(td);

    td = document.createElement("td");
    button = document.createElement("input");
    button.type = 'button';
    button.value='Pay';
    button.onclick = function() {
        pay(order.id);
    }
    td.appendChild(button);
    tr.appendChild(td);

    td = document.createElement("td");
    button = document.createElement("input");
    button.type = 'button';
    button.value='Delete';
    button.onclick = function() {
        deleteOrder(order.id);
    }
    td.appendChild(button);
    tr.appendChild(td);

    table.appendChild(tr);
}

setDelivered = function(orderId) {
    var span = document.getElementById("deliver"+orderId);
    span.innerHTML = true;
}

setPaid = function(orderId) {
    var span = document.getElementById("pay"+orderId);
    span.innerHTML = true;
}

deleteLine = function(orderId) {
    var table = document.getElementById("orderTab");
    if (table) {
        var tr = document.getElementById("order_"+orderId);
        if (tr) {
            table.removeChild(tr);
        }
        if (table.getElementsByTagName('tr').length < 2) {
            var output = document.getElementById("output");
            output.removeChild(table);
            var status = document.getElementById("status");
            status.innerHTML = 'No Orders';
        }
    }
}
