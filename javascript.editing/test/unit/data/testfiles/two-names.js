var g_modules=[];

var YAHOO_config = {
    listener: function g_mycallback(info) {
        g_modules.push(info.name);
    }
};
