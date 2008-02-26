Draggable.prototype = {
    scroll: function() {
        var optionTag;
        collection.each(function(e,i) {
                optionTag = document.createElement("option");
                optionTag.value = (e instanceof Array) ? e[0] : e;
                if((typeof this.options.value == 'undefined') &&
                        ((e instanceof Array) ? this.element.innerHTML == e[1] : e == optionTag.value)) optionTag.selected = true;
                if(this.options.value==optionTag.value) optionTag.selected = true;
                optionTag.appendChild(document.createTextNode((e instanceof Array) ? e[1] : e));
                selectTag.appendChild(optionTag);
            }.bind(this));
        this.cached_selectTag = selectTag;
    }
}

