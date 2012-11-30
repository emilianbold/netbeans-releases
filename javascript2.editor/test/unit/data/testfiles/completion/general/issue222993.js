var slideIndex = {
    host: host,
    content: {
        title: "",
        numberOfSlide: 0,
        slides: {
            titles: [],
            simpleSlide: []

        }
    },
    sendResponse: function() {
        //this.content. // CC here does not offer title, numberOfSlide etc.
        this.content.slides.titles[0] = {};
    }
};