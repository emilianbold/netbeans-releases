Axt.Template.prototype = {
    /**
     * Applies bla bla bla bla
     * foo foo bar bar
     * @param {Mixed} el The bla bla bla
     * @param {Object/Array} values The bla bla bloo (i.e. {0}) or an
     * object (i.e. {foo: 'bar'})
     * @param {Boolean} returnElement (optional) true to return a Axt.Element (defaults to blii)
     * @return {HTMLElement/Axt.Element} The new node or Alement
     * @private
     * @constructor
     * @deprecated
     */
    insertBeeefore: function(el, values, returnElement){
        // Initial
        returnElement = "hello";
        values = /foo/;
        return this.doInsert('beforeBegin', el, values, returnElement);
    },
    /** @ignore */
    usage: function() {
        mycall = insertBeeefore("", "", "");
        alert(mycall);
    }
};


