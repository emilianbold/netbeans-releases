// Small extract to for unit testing their extension type
/**
* @class YAHOO.widget.Calendar2up
* @extends YAHOO.widget.CalendarGroup
* @deprecated The old Calendar2up class is no longer necessary, since CalendarGroup renders in a 2up view by default.
*/
YAHOO.widget.Calendar2up = function(id, containerId, config) {
	this.init(id, containerId, config);
};
// Make sure we model this inheritance
YAHOO.extend(YAHOO.widget.Calendar2up, YAHOO.widget.CalendarGroup);
YAHOO.widget.CalendarNavigator = function(cal) {
	this.init(cal);
};

// From yui event: why is there a method call here?
    YAHOO.util.Event = function() {
        some_stuff_here();
        return {
            POLL_RETRYS: 2000,
            onAvailable: function(p_id, p_fn, p_obj, p_override, checkContent) {
                var a = (YAHOO.lang.isString(p_id)) ? [p_id] : p_id;
                this.startInterval();
            },
            
        }        
    }();
     
