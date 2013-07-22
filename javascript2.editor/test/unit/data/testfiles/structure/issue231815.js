/**
 * OvuFriend HTML 5 Graph
 *
 * @author Mariusz Buk <mariusz@freelancer.katowice.pl>
 * @link http://freelancer.katowice.pl
 */

/**
 * required options:
 * 'url' - graph json URL
 *
 * @param $ jQuery
 */
(function ($) { 
    "use strict";
      
    $.fn.ovuGraph = function (options) {

        var GRAPH_HEIGHT = 220, prv, settings, methods;

        settings = $.extend({
            /**
             * if graph is editable
             */
            editable: false
        }, options);

        /**
         * Private vars
         */  
        prv = {
            json: null,

            /**
             * Canvas
             */
            canvas: null,

            /**
             * Canvas context
             */
            context: null,

            /**
             * Column width
             */
            columnW: 18,

            /**
             * Graph scale settings
             */
            minValue: null,
            maxValue: null,
            scale: null,
            id: null, //canvas parent id

            layer: null,
            layer_id: null,
            ctx: null,

            /**
             * Load JSON file
             */
            _load: function () {
                $.ajax({
                    url: settings.url,
                    dataType: 'json',
                    //contentType: 'application/json',
                    crossDomain: true,
                    type: 'get',
                    error: function (jqXHR, textStatus, errorThrown) {
                        if (typeof console !== "undefined") {
                            console.log(jqXHR, textStatus, errorThrown);
                        }
                    },
                    success: function (data) {
                        prv.json = data;

                        prv.canvas.width = prv.json.objectSize;
                        prv.canvas.height = 220;
                        prv.context = prv.canvas.getContext('2d');

                        prv.columnW = prv.json.objectSize / prv.json.columns.length;
                        prv._drawGrid();
                        prv._calculateScale();
                        prv._drawWires();
                        prv._printValues();
                        prv._drawLines();
                        prv._drawPoints();
                        prv._highlightDays();

                        prv._createLayer();
                        prv._createEvents();
                    }
                });
            },

            /*
             * Zwraca numer kolumny, na której znajduje sie kursor
             */
            _checkColumn: function (pos) {
                var c = Math.ceil(pos / prv.columnW);
                return c;
            },

            /**
             * Tworzy warstwę "animacji"
             */
            _createLayer: function () {


                var c;
                c = document.createElement('canvas');
                if (typeof G_vmlCanvasManager !== 'undefined') {
                    c = G_vmlCanvasManager.initElement(c); //for IE < 9
                }
                c.id = prv.id + '_canvas_layer';
                prv.layer_id = c.id;
                $('#' + prv.id).append(c);
                $('#' + c.id).css({position: 'absolute', zIndex: 999,
                    top: 0, left: 0});
                prv.layer = document.getElementById(c.id);
                prv.layer.width = prv.json.objectSize;
                prv.layer.height = 220;
                prv.ctx = prv.layer.getContext('2d');


            },

            /**
             * oblicza względną pozycję kursora
             **/
            getMousePos: function (c, e) {
                var rect, mouseX, mouseY;

                rect = c.getBoundingClientRect();
                mouseX = e.clientX - rect.left;
                mouseY = e.clientY - rect.top;

                return {
                    x: parseInt(mouseX, 10),
                    y: parseInt(mouseY, 10)
                };
            },

            /**
             * "Animuje" punkty wykresu
             */
            _enlargePoint: function (c) {
                var color, g;

                if (options.editable) { /* && prv.json.columns[c].enabled*/
                    $('#' + prv.layer_id).css('cursor', 'pointer');
                } else {
                    $('#' + prv.layer_id).css('cursor', 'auto');
                }
                eval('g=' + prv.json.columns[c].onhover);
                g();

                color = prv._convertColor(prv.json.columns[c].colour);
                prv.ctx.clearRect(0, 0, prv.canvas.width, prv.canvas.height);

                if (prv.json.columns[c].dot === "full") {
                    prv.ctx.beginPath();
                    prv.ctx.fillStyle = color;
                    prv.ctx.arc(
                        c * prv.columnW + prv.columnW / 2,
                        prv._getY(prv.json.columns[c].temperature),
                        6, 0, Math.PI * 2, false
                    );
                    prv.ctx.fill();
                } else if (prv.json.columns[c].dot === "empty") {
                    prv.ctx.fillStyle = "#ffffff";
                    prv.ctx.lineWidth = 2;
                    prv.ctx.lineHeight = 2;
                    prv.ctx.strokeStyle = color;
                    prv.ctx.beginPath();
                    prv.ctx.arc(
                        c * prv.columnW + prv.columnW / 2,
                        prv._getY(prv.json.columns[c].temperature),
                        6, 0, Math.PI * 2, false
                    );
                    prv.ctx.stroke();
                    prv.ctx.fill();
                }
            },

            _createEvents: function () {

                if (prv.layer.addEventListener) {
                    prv.layer.addEventListener('mousemove', function (e) {

                        var mousePos, c;

                        mousePos = prv.getMousePos(prv.layer, e);
                        if (mousePos.x != 0) {
                            c = prv._checkColumn(mousePos.x);
                            prv._enlargePoint(c - 1);
                        }

                    });

                    prv.layer.addEventListener('mouseout', function () {
                        prv.ctx.clearRect(0, 0, prv.layer.width, prv.layer.height);
                        $('#' + prv.layer_id).css('cursor', 'auto');
                        app.graph.hint = '';
                    });

                    prv.layer.addEventListener('click', function (e) {
                        var mousePos, c, g;

                        mousePos = prv.getMousePos(prv.layer, e);
                        c = prv._checkColumn(mousePos.x);
                        if (options.editable && (prv.json.columns[c - 1].enabled || !prv.json.columns[c - 1].disabled)) {
                            // app.graph.editData(c-1);
                            eval('g=' + prv.json.columns[c - 1].onclick);
                            g();
                        }
                    });

                } else if (prv.layer.attachEvent) {
                    // IE < 9
                    prv.layer.attachEvent('onmousemove', function (e) {

                        var mousePos, c;

                        mousePos = prv.getMousePos(prv.layer, e);

                        if (mousePos.x != 0) {
                            c = prv._checkColumn(mousePos.x);
                            prv._enlargePoint(c - 1);
                        }

                    });

                    prv.layer.attachEvent('onmouseout', function () {
                        prv.ctx.clearRect(0, 0, prv.layer.width, prv.layer.height);
                        $('#' + prv.layer_id).css('cursor', 'auto');
                        app.graph.hint = '';
                    });

                    prv.layer.attachEvent('onclick', function (e) {

                        var mousePos, c, g;

                        mousePos = prv.getMousePos(prv.layer, e);
                        c = prv._checkColumn(mousePos.x);
                        if (options.editable && (prv.json.columns[c - 1].enabled || !prv.json.columns[c - 1].disabled)) {
                            eval('g=' + prv.json.columns[c - 1].onclick);
                            g();
                        }
                    });
                }

            },

            /**
             * Draw grid - horizontal and vertical lines
             */
            _drawGrid: function () {
                var i, length = prv.json.columns.length / 4 - 1, p, p2;

                prv.context.lineWidth = 1;
                prv.context.lineHeight = 1;
                prv.context.strokeStyle = 'rgba(0,0,0,0.1)';

                // vertical line
                for (i = 0; i <= length; i++) {
                    p = prv.columnW * 4 + prv.columnW * 4 * i + 0.5;
                    p2 = prv.columnW * 4 + prv.columnW * 4 * i + 0.5;

                    prv.context.beginPath();
                    prv._drawDashedLine(p, 0, p2, GRAPH_HEIGHT, 4);
                }

                // horizontal line
                for (i = 0; i <= 4; i++) {
                    p = GRAPH_HEIGHT / 5 * i + 22 + 0.5;
                    p2 = GRAPH_HEIGHT / 5 * i + 22 + 0.5;

                    prv.context.beginPath();
                    prv._drawDashedLine(0, p, prv.json.objectSize - 1, p2, 4);
                }
            },

            /**
             * Draw dashed line
             */
            _drawDashedLine: function (x1, y1, x2, y2, dashLength) {

                var deltaX, deltaY, numDashes, i;

                dashLength = dashLength === undefined ? 5 : dashLength;
                deltaX = x2 - x1;
                deltaY = y2 - y1;
                numDashes = Math.floor(Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) / dashLength);

                for (i = 0; i <= numDashes; i++) {
                    if (typeof G_vmlCanvasManager !== 'undefined') {
                        prv.context[i % 2 === 0 ? 'moveTo' : 'lineTo'](x1 + (deltaX / numDashes) * i, y1 + (deltaY / numDashes) * i);
                        //(parseInt(x1 + (deltaX / numDashes) * i), parseInt(y1 + (deltaY / numDashes) * i));
                    } else {
                        prv.context[i % 2 === 0 ? 'moveTo' : 'lineTo'](x1 + (deltaX / numDashes) * i, y1 + (deltaY / numDashes) * i);
                    }
                }

                prv.context.stroke();
            },

            /**
             * Calculate graph scale
             */
            _calculateScale: function () {
                var i, avg = 0, up_weight = 0, baseValue;

                if (prv.json.temperature === "celsius") {
                    avg = (37.2 - 36.0) / 2;
                    prv.minValue = prv.maxValue = baseValue = 36.6;
                } else if (prv.json.temperature === "farenheit") {
                    avg = (99.0 - 96.8) / 2;
                    prv.minValue = prv.maxValue = baseValue = 97.9;
                } else if (prv.json.temperature === "metric") {
                    avg = (70.0 - 45.0) / 2;
                    prv.maxValue = 45;
                    prv.minValue = baseValue = 70;
                    up_weight = 15;
                } else if (prv.json.temperature === "nonmetric") {
                    avg = (150.0 - 100.0) / 2;
                    prv.maxValue = 100;
                    prv.minValue = baseValue = 150;
                    up_weight = 33;
                }

                // find min and max values
                for (i = 0; i <= prv.json.columns.length - 1; i++) {
                    if (prv.json.columns[i].temperature != 0 &&
                        prv.json.columns[i].temperature < prv.minValue) {
                        prv.minValue = prv.json.columns[i].temperature;
                    }
                    if (prv.json.columns[i].temperature != 0 &&
                        prv.json.columns[i].temperature > prv.maxValue) {
                        prv.maxValue = prv.json.columns[i].temperature;
                    }
                }


                if (prv.json.temperature == "celsius" || prv.json.temperature == "farenheit") {
                    if (baseValue - prv.minValue < avg) {
                        prv.minValue = baseValue - avg;
                    }
                    if (prv.maxValue - baseValue < avg) {
                        prv.maxValue = baseValue + avg;
                    }
                } else if (prv.json.temperature == "metric" || prv.json.temperature == "nonmetric") {
                    // skoryguj wagi do domyślnych, jeżeli żadnych nie podano
                    if (prv.minValue >= prv.maxValue) {
                        if (prv.json.temperature == "metric") {
                            prv.minValue = 45;
                            prv.maxValue = 70;
                        } else {
                            prv.minValue = 100;
                            prv.maxValue = 150;
                        }
                    }
                    // jeżeli różnica w wagach jest zbyt mała - zwiększ rozpiętość do 15 kg
                    if (prv.maxValue - prv.minValue < up_weight) {
                        prv.maxValue = parseInt(prv.minValue, 10) + parseInt(up_weight, 10);

                    }
                }

                prv.scale = prv.maxValue - prv.minValue;

            },

            /**
             * Narysowanie linii odciętej (średnia 6 temperatur sprzed T(0))
             * Narysowanie linii pionowej w dniu owulacji
             */
            _drawWires: function () {

                var i;

                if (prv.json.horizontal_line.style === "solid") {
                    // draw solid horizontal line
                    prv.context.lineWidth = 2;
                    prv.context.lineHeight = 2;
                    prv.context.strokeStyle = prv._convertColor(prv.json.horizontal_line.colour);
                    prv.context.beginPath();

                    prv.context.moveTo(0, prv._getY(prv.json.horizontal_line.temperature));
                    prv.context.lineTo(
                        prv.json.columns.length * prv.columnW,
                        prv._getY(prv.json.horizontal_line.temperature)
                        );

                    prv.context.stroke();
                } else if (prv.json.horizontal_line.style === "dotted") {
                    // draw dotted horizontal line
                    prv.context.lineWidth = 2;
                    prv.context.lineHeight = 2;
                    prv.context.strokeStyle = prv._convertColor(prv.json.horizontal_line.colour);
                    prv.context.beginPath();

                    prv._drawDashedLine(
                        0, prv._getY(prv.json.horizontal_line.temperature),
                        prv.json.columns.length * prv.columnW, prv._getY(prv.json.horizontal_line.temperature),
                        4
                    );
                    prv.context.stroke();
                }

                // draw vertical line (ovulation)
                prv.context.lineWidth = 2;
                prv.context.lineHeight = 2;
                for (i = 0; i <= prv.json.columns.length - 1; i++) {
                    if (prv.json.columns[i].vertical_line !== undefined) {
                        if (prv.json.columns[i].vertical_line.style === "solid") {
                            prv.context.beginPath();
                            prv.context.strokeStyle = prv._convertColor(prv.json.columns[i].vertical_line.colour);
                            prv.context.moveTo(prv.columnW / 2 + prv.columnW * i, 0);
                            prv.context.lineTo(prv.columnW / 2 + prv.columnW * i, GRAPH_HEIGHT);
                            prv.context.stroke();
                        } else if (prv.json.columns[i].vertical_line.style === "dotted") {
                            prv.context.beginPath();
                            prv.context.strokeStyle = prv._convertColor(prv.json.columns[i].vertical_line.colour);

                            prv._drawDashedLine(
                                prv.columnW / 2 + prv.columnW * i, 0,
                                prv.columnW / 2 + prv.columnW * i, GRAPH_HEIGHT,
                                4
                            );
                            prv.context.stroke();
                        }
                    }
                }
            },

            /**
             * Print values under horizontal line
             */
            _printValues: function () {
                var i, color = "#000000", height = 110;

                /* find color line
                 for (i = 0; i <= prv.json.columns.length-1; i++)
                 {
                 if (prv.json.columns[i].vertical_line != undefined &&
                 prv.json.columns[i].vertical_line.colour != undefined)
                 {
                 color = prv._convertColor(prv.json.columns[i].vertical_line.colour);
                 break;
                 }
                 }*/

                prv.context.font = "11px Arial";
                prv.context.fillStyle = color;
                prv.context.textAlign = "center";
                prv.context.textBaseline = "top";

                // calculate text's vertical position
                if (prv.json.horizontal_line.temperature !== undefined) {
                    height = prv._getY(prv.json.horizontal_line.temperature) + 3;
                }

                // print values
                for (i = 0; i <= prv.json.columns.length - 1; i++) {
                    if (prv.json.columns[i].value !== undefined && prv.json.columns[i].value !== null) {
                        prv.context.fillText(
                            prv.json.columns[i].value,
                            prv.columnW * i + (prv.columnW / 2),
                            height
                        );
                    }
                }
            },

            /**
             * Draw temperature/weight lines
             */
            _drawLines: function () {
                var i, index, color;

                for (i = 0; i <= prv.json.columns.length - 1; i++) {
                    color = prv._convertColor(prv.json.columns[i].colour);

                    // search next not empty value
                    index = prv._searchNextValue(i);

                    if (prv.json.columns[i].dot === "full" || prv.json.columns[i].dot === "empty") {
                        if (i + 1 < prv.json.columns.length) {
                            if (index > 0) {
                                if (prv.json.columns[i].line === "solid") {
                                    prv.context.strokeStyle = color;
                                    prv.context.lineWidth = 1;
                                    prv.context.lineHeight = 1;

                                    prv.context.beginPath();
                                    prv.context.moveTo(
                                        i * prv.columnW + prv.columnW / 2,
                                        prv._getY(prv.json.columns[i].temperature)
                                    );
                                    prv.context.lineTo(
                                        index * prv.columnW + prv.columnW / 2,
                                        prv._getY(prv.json.columns[index].temperature)
                                    );
                                    prv.context.stroke();
                                } else if (prv.json.columns[i].line === "dotted") {
                                    prv.context.strokeStyle = color;
                                    prv.context.lineWidth = 1;
                                    prv.context.lineHeight = 1;

                                    prv.context.beginPath();
                                    prv._drawDashedLine(
                                        i * prv.columnW + prv.columnW / 2,
                                        prv._getY(prv.json.columns[i].temperature),
                                        index * prv.columnW + prv.columnW / 2,
                                        prv._getY(prv.json.columns[index].temperature),
                                        4
                                    );
                                }
                            }
                        }
                    }

                    // dla pozostałych przypadków prv.json.columns[i].dot == "none" nic nie rysujemy
                }
            },

            /**
             * Draw temperature/weight points
             */
            _drawPoints: function () {
                var i, color;

                for (i = 0; i <= prv.json.columns.length - 1; i++) {
                    color = prv._convertColor(prv.json.columns[i].colour);

                    if (prv.json.columns[i].dot === "full") {
                        prv.context.beginPath();
                        prv.context.fillStyle = color;
                        prv.context.arc(
                            i * prv.columnW + prv.columnW / 2,
                            prv._getY(prv.json.columns[i].temperature),
                            4, 0, Math.PI * 2, false
                        );
                        prv.context.fill();
                    } else if (prv.json.columns[i].dot === "empty") {
                        prv.context.fillStyle = "#ffffff";
                        prv.context.lineWidth = 2;
                        prv.context.lineHeight = 2;
                        prv.context.strokeStyle = color;
                        prv.context.beginPath();
                        prv.context.arc(
                            i * prv.columnW + prv.columnW / 2,
                            prv._getY(prv.json.columns[i].temperature),
                            3, 0, Math.PI * 2, false
                        );
                        prv.context.stroke();
                        prv.context.fill();
                    }

                    // dla pozostałych przypadków prv.json.columns[i].dot == "none" nic nie rysujemy
                }
            },

            /**
             * Search next temperature or weight
             *
             * @return int next day index (or 0 if no future values)
             */
            _searchNextValue: function (index) {
                var i, result = 0;

                for (i = index + 1; i < prv.json.columns.length; i++) {
                    if (prv.json.columns[i].temperature > 0) {
                        result = i;
                        break;
                    }
                }

                return result;
            },

            /**
             * Set current and disabled days. Do not set current day if graph is not editable.
             */
            _highlightDays: function () {
                var i;

                prv.context.lineWidth = 1;

                for (i = 0; i <= prv.json.columns.length - 1; i++) {
                    // highlight current day
                    if (prv.json.columns[i].current === true && options.editable) {
                        prv.context.fillStyle = "rgba(0,0,0,0.1)";
                        prv.context.fillRect(i * prv.columnW, 0, prv.columnW, GRAPH_HEIGHT);
                    }

                    // highlight disabled days
                    if (prv.json.columns[i].disabled !== undefined && prv.json.columns[i].disabled) {
                        prv.context.fillStyle = "rgba(250,188,189,0.5)";
                        prv.context.fillRect(i * prv.columnW, 0, prv.columnW, GRAPH_HEIGHT);
                    }
                }
            },

            /**
             * Get vertical postition based on temperature or weight
             */
            _getY: function (value) {
                // 220-22 - 220-44 * ...
                return Math.round(198 - 176 * (value - prv.minValue) / prv.scale);
            },

            /**
             * Convert color from hex (eg. 0x112233) to www (eg. #112233)
             */
            _convertColor: function (color) {

                var converted = '#' + color.match(/0x([\da-f]+)/i)[1];

                return converted;
            }
        };

        /**
         * Global methods
         */
        methods = {
            init: function (options) {

                this.each(function () {

                    var canvas = document.createElement('canvas');

                    if (typeof G_vmlCanvasManager !== 'undefined') {
                        canvas = G_vmlCanvasManager.initElement(canvas); //for IE < 9
                    }

                    prv.id = $(this).attr("id");
                    canvas.id = prv.id + "_canvas";
                    this.appendChild(canvas);
                    $('#' + prv.id + "_canvas").css('background-color', '#ffffff');
                    prv.canvas = document.getElementById(prv.id + "_canvas");

                    prv._load();
                });
            },
            alert: function () {
                alert(event.clientX + ' ' + event.clientY);
            },
            show: function () {
                // IS
            },
            hide: function () {
                // GOOD
            },
            update: function (content) {
                // !!!
            }
        };

        // Method calling logic
        if (methods[options]) {
            return methods[options].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof options === 'object' || !options) {
            return methods.init.apply(this, arguments);
        }
    };
}) (jQuery);