chrome.devtools.inspectedWindow.onResourceContentCommitted.addListener(
    function(resource, content) {
        chrome.extension.sendMessage({event: "onResourceContentCommitted", resource : resource, content: content}, function(response) {
          // nothing to be done
        });
    });