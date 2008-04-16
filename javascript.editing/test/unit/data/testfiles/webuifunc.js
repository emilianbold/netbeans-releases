/**
 * This function is used to set common properties for the given domNode.
 * 
 * @param {Node} domNode The DOM node to assign properties to.
 * @param {Object}   props Key-Value pairs of properties.
 * @config {String} accessKey Shortcut key.
 * @config {String} dir Specifies the directionality of text.
 * @config {String} lang Specifies the language of attribute values and content.
 * @config {int} tabIndex Position in tabbing order.
 * @config {String} title Provides a title for element.
 * @return {boolean} true if successful; otherwise, false.
 * @param {Object}  thirdparam
 */
webui.suntheme4_2.widget.widgetBase.prototype.setCommonProps = function(domNode, props,
       thirdparam) {
    if (domNode == null || props == null) {
        return false;
    }
    if (props.accessKey) {  
        domNode.accessKey = props.accessKey;
    }
    if (props.dir) {
        domNode.dir = props.dir;
    }
    if (props.lang) {
        domNode.lang = props.lang;
    }
    if (props.tabIndex > -1 && props.tabIndex < 32767) {
        domNode.tabIndex = props.tabIndex;
    }
    if (props.title) {
        domNode.title = props.title;
    }
    return true;
};

/**
 * Document without a function
 * @param foo
 */
 x = 5;

x = {
   /**
    * @param testparam Doc for testparam
    * @param
    */
   foobar: function(foo, testparam) {
   }
}

/**
 * new @param
 */
