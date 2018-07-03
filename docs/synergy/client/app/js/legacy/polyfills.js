"use strict";

if (typeof Object.create !== "function") { // hello there old browser
    (function() {
        var F = function() {
        };
        Object.create = function(o) {
            if (arguments.length > 1) {
                throw Error("Second argument not supported");
            }
            if (o === null) {
                throw Error("Cannot set a null [[Prototype]]");
            }
            if (typeof o !== "object") {
                throw new TypeError("Argument must be an object");
            }
            F.prototype = o;
            return new F;
        };
    })();
}
if (!Array.prototype.filter)
{
    Array.prototype.filter = function(fun /*, thisArg */)
    {

        if (this === void 0 || this === null) {
            throw new TypeError();
        }

        var t = Object(this);
        var len = t.length >>> 0;
        if (typeof fun !== "function") {
            throw new TypeError();
        }

        var res = [];
        var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
        for (var i = 0; i < len; i++)
        {
            if (i in t)
            {
                var val = t[i];

                // NOTE: Technically this should Object.defineProperty at
                //       the next index, as push can be affected by
                //       properties on Object.prototype and Array.prototype.
                //       But that method"s new, and collisions should be
                //       rare, so use the more-compatible alternative.
                if (fun.call(thisArg, val, i, t)) {
                    res.push(val);
                }
            }
        }

        return res;
    };
}

Date.prototype.toMysqlFormat = function() {

    function twoDigits(d) {
        if (0 <= d && d < 10) {
            return "0" + d.toString();
        }
        if (-10 < d && d < 0) {
            return "-0" + (-1 * d).toString();
        }
        return d.toString();
    }

    return this.getUTCFullYear() + "-" + twoDigits(1 + this.getUTCMonth()) + "-" + twoDigits(this.getUTCDate()) + " " + twoDigits(this.getUTCHours()) + ":" + twoDigits(this.getUTCMinutes()) + ":" + twoDigits(this.getUTCSeconds());
};

String.prototype.endsWith = function (s) {
    return this.length >= s.length && this.substr(this.length - s.length) == s;
};