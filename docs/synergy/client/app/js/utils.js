"use strict";

angular.module("synergy.utils", [])
        .factory("SynergyUtils", [function () {

                var Util = {
                };
                

                Util.monthsNames = {
                    "fullName": ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
                    "shortName": ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
                };
                /**
                 * Returns string date in local form
                 * @param {String} d string representing UTC date in form of "2014-02-21 00:00:00" UTC or "18 Feb 2014 09:07:40 UTC"
                 * @param {boolean} shortMonthName true to display February, false to display Feb
                 * @returns {String}
                 */
                Util.UTCToLocal = function (d, shortMonthName) {
                    try {
                        var _d = new Date();
                        var _split = d.split(" "); // [2014-02-21, 00:00:00, UTC]
                        var _yearMonthDay;
                        var _time;
                        switch (_split.length) {
                            case 2:
                            case 3:
                                _yearMonthDay = _split[0].split("-");
                                _yearMonthDay.forEach(function (item, index) {
                                    _yearMonthDay[index] = parseInt(item, 10);
                                });

                                _time = _split[1].split(":");
                                _time.forEach(function (item, index) {
                                    _time[index] = parseInt(item, 10);
                                });
                                break;
                            case 5:
                                var _m = Util.monthsNames.shortName.indexOf(_split[1]);
                                if (_m < 0) {
                                    _m = Util.monthsNames.fullName.indexOf(_split[1]);
                                }
                                _yearMonthDay = [parseInt(_split[2], 10), _m, parseInt(_split[0], 10)];
                                _yearMonthDay[1]++;
                                _split = _split[3].split(":");
                                _time = [parseInt(_split[0], 10), parseInt(_split[1], 10), parseInt(_split[2], 10)];
                                break;
                            default:
                                return d;

                        }

                        _d.setUTCFullYear(_yearMonthDay[0]);
                        _d.setUTCMonth(_yearMonthDay[1] - 1);
                        _d.setUTCDate(_yearMonthDay[2]);
                        _d.setUTCHours(_time[0]);
                        _d.setUTCMinutes(_time[1]);
                        _d.setUTCSeconds(_time[2]);

                        return  _d.getDate() + " " + (shortMonthName ? Util.monthsNames.shortName[_d.getMonth()] : Util.monthsNames.fullName[_d.getMonth()]) + " " + _d.getFullYear() + " " + ((_d.getHours() < 10) ? "0" + _d.getHours() : _d.getHours()) + ":" + ((_d.getMinutes() < 10) ? "0" + _d.getMinutes() : _d.getMinutes()) + ":" + ((_d.getSeconds() < 10) ? "0" + _d.getSeconds() : _d.getSeconds());
                    } catch (e) {
                        return d;
                    }
                };
                Util.UTCToDate = function (d) {
                    try {
                        var _d = new Date();
                        var _split = d.split(" "); // [2014-02-21, 00:00:00, UTC]
                        var _yearMonthDay;
                        var _time;
                        switch (_split.length) {
                            case 2:
                            case 3:
                                _yearMonthDay = _split[0].split("-");
                                _yearMonthDay.forEach(function (item, index) {
                                    _yearMonthDay[index] = parseInt(item, 10);
                                });

                                _time = _split[1].split(":");
                                _time.forEach(function (item, index) {
                                    _time[index] = parseInt(item, 10);
                                });
                                break;
                            case 5:
                                var _m = Util.monthsNames.shortName.indexOf(_split[1]);
                                if(_m < 0){
                                    _m = Util.monthsNames.fullName.indexOf(_split[1]);
                                }
                                _yearMonthDay = [parseInt(_split[2], 10), _m , parseInt(_split[0], 10)];
                                _yearMonthDay[1]++;
                                _split = _split[3].split(":");
                                _time = [parseInt(_split[0], 10), parseInt(_split[1], 10), parseInt(_split[2], 10)];
                                break;
                            default:
                                return d;

                        }

                        _d.setUTCFullYear(_yearMonthDay[0]);
                        _d.setUTCMonth(_yearMonthDay[1] - 1);
                        _d.setUTCDate(_yearMonthDay[2]);
                        _d.setUTCHours(_time[0]);
                        _d.setUTCMinutes(_time[1]);
                        _d.setUTCSeconds(_time[2]);

                        return  _d;
                    } catch (e) {
                        return null;
                    }
                };
                /**
                 * Returns string date in local form
                 * @param {String} d string representing UTC date in form of "2014-02-21 00:00:00" UTC or "18 Feb 2014 09:07:40 UTC"
                 * @param {boolean} shortMonthName true to display February, false to display Feb
                 * @returns {String}
                 */
                Util.UTCToLocalDateTime = function (d, shortMonthName) {
                    try {
                        var _d = new Date();
                        var _split = d.split(" "); // [2014-02-21, 00:00:00, UTC]
                        var _yearMonthDay;
                        var _time;
                        switch (_split.length) {
                            case 2:
                            case 3:
                                _yearMonthDay = _split[0].split("-");
                                _yearMonthDay.forEach(function (item, index) {
                                    _yearMonthDay[index] = parseInt(item, 10);
                                });

                                _time = _split[1].split(":");
                                _time.forEach(function (item, index) {
                                    _time[index] = parseInt(item, 10);
                                });
                                break;
                            case 5:
                                var _m = Util.monthsNames.shortName.indexOf(_split[1]);
                                if(_m < 0){
                                    _m = Util.monthsNames.fullName.indexOf(_split[1]);
                                }
                                _yearMonthDay = [parseInt(_split[2], 10),  _m , parseInt(_split[0], 10)];
                                _yearMonthDay[1]++;
                                _split = _split[3].split(":");
                                _time = [parseInt(_split[0], 10), parseInt(_split[1], 10), parseInt(_split[2], 10)];
                                break;
                            default:
                                return d;

                        }

                        _d.setUTCFullYear(_yearMonthDay[0]);
                        _d.setUTCMonth(_yearMonthDay[1] - 1);
                        _d.setUTCDate(_yearMonthDay[2]);
                        _d.setUTCHours(_time[0]);
                        _d.setUTCMinutes(_time[1]);
                        _d.setUTCSeconds(_time[2]);

                        return  _d.getFullYear() + "-" + ((_d.getMonth() + 1 < 10) ? "0" + (_d.getMonth() + 1) : (_d.getMonth() + 1)) + "-" + _d.getDate() + " " + ((_d.getHours() < 10) ? "0" + _d.getHours() : _d.getHours()) + ":" + ((_d.getMinutes() < 10) ? "0" + _d.getMinutes() : _d.getMinutes()) + ":" + ((_d.getSeconds() < 10) ? "0" + _d.getSeconds() : _d.getSeconds());
                    } catch (e) {
                        return d;
                    }
                };

                /**
                 * Returns local string date in UTC form
                 * @param {String} d string representing local date in form of "2014-02-21 00:00:00"
                 * @returns {String}
                 */
                Util.localToUTC = function (d) {
                    try {
                        var _d = new Date();
                        var _split = d.split(" "); // [2014-02-21, 00:00:00,]

                        var _yearMonthDay = _split[0].split("-");
                        _yearMonthDay.forEach(function (item, index) {
                            _yearMonthDay[index] = parseInt(item, 10);
                        });

                        var _time = _split[1].split(":");
                        _time.forEach(function (item, index) {
                            _time[index] = parseInt(item, 10);
                        });

                        _d.setFullYear(_yearMonthDay[0]);
                        _d.setMonth(_yearMonthDay[1] - 1);
                        _d.setDate(_yearMonthDay[2]);
                        _d.setHours(_time[0]);
                        _d.setMinutes(_time[1]);
                        _d.setSeconds(_time[2]);

                        return  _d.getUTCFullYear() + "-" + (((_d.getUTCMonth() + 1) < 10) ? "0" + (_d.getUTCMonth() + 1) : (_d.getUTCMonth() + 1)) + "-" + ((_d.getUTCDate() < 10) ? "0" + _d.getUTCDate() : _d.getUTCDate()) + " " + ((_d.getUTCHours() < 10) ? "0" + _d.getUTCHours() : _d.getUTCHours()) + ":" + ((_d.getUTCMinutes() < 10) ? "0" + _d.getUTCMinutes() : _d.getUTCMinutes()) + ":" + ((_d.getUTCSeconds() < 10) ? "0" + _d.getUTCSeconds() : _d.getUTCSeconds());
                    } catch (e) {
                        return d;
                    }
                };
                Util.localToUTCTimestamp = function (d) {
                    try {
                        var _d = new Date();
                        var _split = d.split(" "); // [2014-02-21, 00:00:00,]
                        var _yearMonthDay = _split[0].split("-");
                        _yearMonthDay.forEach(function (item, index) {
                            _yearMonthDay[index] = parseInt(item, 10);
                        });
                        var _time = _split[1].split(":");
                        _time.forEach(function (item, index) {
                            _time[index] = parseInt(item, 10);
                        });
                        _d.setFullYear(_yearMonthDay[0]);
                        _d.setMonth(_yearMonthDay[1] - 1);
                        _d.setDate(_yearMonthDay[2]);
                        _d.setHours(_time[0]);
                        _d.setMinutes(_time[1]);
                        _d.setSeconds(_time[2]);
                        return  _d.getTime();
                    } catch (e) {
                        return d;
                    }
                };
                Util.shallowClone = function (original) {
                    var _n = {};
                    for (var i in original) {
                        if (original.hasOwnProperty(i)) {
                            _n[i] = original[i];
                        }
                    }
                    return _n;
                };
                Util.shallowCloneArray = function (originalArray) {
                    var _n = [];
                    for (var i = 0,
                            max = originalArray.length; i < max; i++) {
                        _n[i] = Util.shallowClone(originalArray[i]);
                    }
                    return _n;
                };
                Util.definedNotNull = function (obj) {
                    return (typeof obj !== "undefined" && obj !== null);
                };


                Util.ProgressChart = function (data, colors, labels, canvasId) {

                    function addLegend(context) {
                        var topOffset = 20;
                        var totalOffset = topOffset;
                        var leftOffset = (dimensions.width * wRation * 1.1) + radius;
                        context.textAlign = "left";
                        context.font = "1em Helvetica";
                        context.fillStyle = "#333";
                        for (var i = 0, max = data.length; i < max; i++) {
                            if (labels[i].length > 0 && !isNaN(data[i])) {
                                context.fillText(data[i] + "% " + labels[i], leftOffset, (dimensions.height / 2 - radius) + totalOffset);
                                totalOffset += topOffset;
                            }
                        }
                    }

                    function drawOuterCircle(context) {
                        var counter = 0;
                        for (var i = 0; i < data.length; i++) {
                            if (isNaN(data[i])) {
                                continue;
                            }
                            context.fillStyle = colors[i];
                            context.beginPath();
                            context.moveTo(dimensions.width * wRation, dimensions.height / 2);
                            context.arc(dimensions.width * wRation, dimensions.height / 2, radius, counter, counter + (Math.PI * 2 * (data[i] / 100)), false);
                            context.fill();
                            counter += Math.PI * 2 * (data[i] / 100);
                        }
                    }

                    function drawInnerCircle(context) {
                        context.moveTo(dimensions.width * wRation, dimensions.height / 2);
                        context.beginPath();
                        context.arc(dimensions.width * wRation, dimensions.height / 2, radius * 0.7, 0, Math.PI * 2, true);
                        context.fillStyle = "#ffffff";
                        context.fill();
                    }

                    var canvas = window.document.getElementById(canvasId);
                    var dimensions = {
                        width: canvas.width,
                        height: canvas.height
                    };
                    var wRation = 0.25;
                    var ratio = 0.4;
                    var radius = ((ratio) * dimensions.width) / 2;


                    if (canvas.getContext) {
                        var context = canvas.getContext("2d");
                        for (var i = 0, max = data.length; i < max; i++) {
                            data[i] = Math.round(10 * data[i]) / 10;
                        }

                        context.clearRect(0, 0, canvas.width, canvas.height);
                        drawOuterCircle(context);
                        drawInnerCircle(context);
                        addLegend(context);
                    } else {
                        canvas.style.display = "none";
                        return;
                    }

                };

                Util.versionToNumber = function (version) {
                    var VER_REG = /(^\d+\.)(.*)/;
                    var match = version.match(VER_REG);
                    var _p;
                    if (match) {
                        _p = match[2].replace(/\./g, "");
                        return parseFloat(version.replace(VER_REG, match[1] + _p));
                    }
                    return parseFloat(version);
                };

                return Util;

            }]);