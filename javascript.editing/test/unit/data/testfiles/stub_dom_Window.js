/**
* <p>This section provides a brief reference for all of the methods, properties, and events available through the DOM <code>window</code> object. The <code>window</code> object implements the <code>Window</code> interface, which in turn inherits from the <code><a href="http://www.w3.org/TR/DOM-Level-2-Views/views.html#Views-AbstractView" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Views/views.html#Views-AbstractView">AbstractView</a></code> interface.
* </p><p>The <code>window</code> object represents the window itself. The <code>document</code> property of a <code>window</code> points to the <a href="document" shape="rect" title="DOM:document">DOM document</a> loaded in that window. A window for a given document can be obtained using the <code><a href="document.defaultView" shape="rect" title="DOM:document.defaultView">document.defaultView</a></code> property.
* </p><p>In a tabbed browser, such as Firefox, each tab contains its own <code>window</code> object (and if you're writing an extension, the browser window itself is a separate window too - see <a href="http://developer.mozilla.org/en/docs/Working_with_windows_in_chrome_code#Content_windows" shape="rect" title="Working with windows in chrome code">Working with windows in chrome code</a> for more information). That is, the <code>window</code> object is not shared between tabs in the same window. Some methods, namely <code><a href="window.resizeTo" shape="rect" title="DOM:window.resizeTo">window.resizeTo</a></code> and <code><a href="window.resizeBy" shape="rect" title="DOM:window.resizeBy">window.resizeBy</a></code> apply to the whole window and not to the specific tab the <code>window</code> object belongs to. Generally, anything that can't reasonably pertain to a tab pertains to the window instead.
* </p>
* <h2> <span> Properties </span></h2>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/Components_object" shape="rect" title="Components object">window.Components</a>
* </dt><dd> The entry point to many <a href="http://developer.mozilla.org/en/docs/XPCOM" shape="rect" title="XPCOM">XPCOM</a> features. Some properties, e.g. <a href="http://developer.mozilla.org/en/docs/Components.classes" shape="rect" title="Components.classes">classes</a>, are only available to sufficiently privileged code.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.content" shape="rect" title="DOM:window.content">window.content</a> and window._content
* </dt><dd> Returns a reference to the content element in the current window. The variant with underscore is deprecated.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.closed" shape="rect" title="DOM:window.closed">window.closed</a>
* </dt><dd> This property indicates whether the current window is closed or not.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.controllers" shape="rect" title="DOM:window.controllers">window.controllers</a>
* </dt><dd> Returns the XUL controller objects for the current chrome window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.crypto" shape="rect" title="DOM:window.crypto">window.crypto</a>
* </dt><dd> Returns the browser crypto object.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.defaultStatus" shape="rect" title="DOM:window.defaultStatus">window.defaultStatus</a>
* </dt><dd> Gets/sets the status bar text for the given window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.dialogArguments&amp;action=edit" shape="rect" title="DOM:window.dialogArguments">window.dialogArguments</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> Gets the arguments passed to the window (if it's a dialog box) at the time <code><a href="window.showModalDialog" shape="rect" title="DOM:window.showModalDialog">window.showModalDialog()</a></code> was called.  This is an <code><a href="http://developer.mozilla.org/en/docs/nsIArray" shape="rect" title="nsIArray">nsIArray</a></code>.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.directories" shape="rect" title="DOM:window.directories">window.directories</a>
* </dt><dd> Returns a reference to the directories toolbar in the current chrome.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.document" shape="rect" title="DOM:window.document">window.document</a>
* </dt><dd> Returns a reference to the document that the window contains.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.frameElement" shape="rect" title="DOM:window.frameElement">window.frameElement</a>
* </dt><dd> Returns the element in which the window is embedded, or null if the window is not embedded.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.frames" shape="rect" title="DOM:window.frames">window.frames</a>
* </dt><dd> Returns an array of the subframes in the current window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.fullScreen" shape="rect" title="DOM:window.fullScreen">window.fullScreen</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> This property indicates whether the window is displayed in full screen or not.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="Storage#globalStorage" shape="rect" title="DOM:Storage">window.globalStorage</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_2_for_developers" shape="rect" title="Firefox 2 for developers">Firefox 2</a></span>
* </dt><dd> Multiple storage objects that are used for storing data across multiple pages.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.history" shape="rect" title="DOM:window.history">window.history</a>
* </dt><dd> Returns a reference to the history object.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.innerHeight" shape="rect" title="DOM:window.innerHeight">window.innerHeight</a>
* </dt><dd> Gets the height of the content area of the browser window including, if rendered, the horizontal scrollbar.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.innerWidth" shape="rect" title="DOM:window.innerWidth">window.innerWidth</a>
* </dt><dd> Gets the width of the content area of the browser window including, if rendered, the vertical scrollbar.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.length" shape="rect" title="DOM:window.length">window.length</a>
* </dt><dd> Returns the number of frames in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.location" shape="rect" title="DOM:window.location">window.location</a>
* </dt><dd> Gets/sets the location, or current URL, of the window object.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.locationbar" shape="rect" title="DOM:window.locationbar">window.locationbar</a>
* </dt><dd> Returns the locationbar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.menubar" shape="rect" title="DOM:window.menubar">window.menubar</a>
* </dt><dd> Returns the menubar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.name" shape="rect" title="DOM:window.name">window.name</a>
* </dt><dd> Gets/sets the name of the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.navigator" shape="rect" title="DOM:window.navigator">window.navigator</a>
* </dt><dd> Returns a reference to the navigator object.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.opener" shape="rect" title="DOM:window.opener">window.opener</a>
* </dt><dd> Returns a reference to the window that opened this current window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.outerHeight" shape="rect" title="DOM:window.outerHeight">window.outerHeight</a>
* </dt><dd> Gets the height of the outside of the browser window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.outerWidth" shape="rect" title="DOM:window.outerWidth">window.outerWidth</a>
* </dt><dd> Gets the width of the outside of the browser window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.pageXOffset" shape="rect" title="DOM:window.pageXOffset">window.pageXOffset</a>
* </dt><dd> Gets the amount of content that has been hidden by scrolling to the right.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.pageYOffset" shape="rect" title="DOM:window.pageYOffset">window.pageYOffset</a>
* </dt><dd> Gets the amount of content that has been hidden by scrolling down.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.parent" shape="rect" title="DOM:window.parent">window.parent</a>
* </dt><dd> Returns a reference to the parent of the current window or subframe.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.personalbar" shape="rect" title="DOM:window.personalbar">window.personalbar</a>
* </dt><dd> Returns the personalbar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.pkcs11" shape="rect" title="DOM:window.pkcs11">window.pkcs11</a>
* </dt><dd> Returns the pkcs11 object, which can be used to install drivers and other software associated with the pkcs11 protocol.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.returnValue&amp;action=edit" shape="rect" title="DOM:window.returnValue">window.returnValue</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> The return value to be returned to the function that called <code><a href="window.showModalDialog" shape="rect" title="DOM:window.showModalDialog">window.showModalDialog()</a></code> to display the window as a modal dialog.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen" shape="rect" title="DOM:window.screen">window.screen</a>
* </dt><dd> Returns a reference to the screen object associated with the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availTop" shape="rect" title="DOM:window.screen.availTop">window.screen.availTop</a>
* </dt><dd> Specifies the y-coordinate of the first pixel that is not allocated to permanent or semipermanent user interface features.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availLeft" shape="rect" title="DOM:window.screen.availLeft">window.screen.availLeft</a>
* </dt><dd> Returns the first available pixel available from the left side of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availHeight" shape="rect" title="DOM:window.screen.availHeight">window.screen.availHeight</a>
* </dt><dd> Specifies the height of the screen, in pixels, minus permanent or semipermanent user interface features displayed by the operating system, such as the Taskbar on Windows.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availWidth" shape="rect" title="DOM:window.screen.availWidth">window.screen.availWidth</a>
* </dt><dd> Returns the amount of horizontal space in pixels available to the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.colorDepth" shape="rect" title="DOM:window.screen.colorDepth">window.screen.colorDepth</a>
* </dt><dd> Returns the color depth of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.height" shape="rect" title="DOM:window.screen.height">window.screen.height</a>
* </dt><dd> Returns the height of the screen in pixels.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.left" shape="rect" title="DOM:window.screen.left">window.screen.left</a>
* </dt><dd> Returns the current distance in pixels from the left side of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.pixelDepth" shape="rect" title="DOM:window.screen.pixelDepth">window.screen.pixelDepth</a>
* </dt><dd> Gets the bit depth of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.top" shape="rect" title="DOM:window.screen.top">window.screen.top</a>
* </dt><dd> Returns the distance from the top of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.width" shape="rect" title="DOM:window.screen.width">window.screen.width</a>
* </dt><dd> Returns the width of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screenX" shape="rect" title="DOM:window.screenX">window.screenX</a>
* </dt><dd> Returns the horizontal distance of the left border of the user's browser from the left side of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screenY" shape="rect" title="DOM:window.screenY">window.screenY</a>
* </dt><dd> Returns the vertical distance of the top border of the user's browser from the top side of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollbars" shape="rect" title="DOM:window.scrollbars">window.scrollbars</a>
* </dt><dd> Returns the scrollbars object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollMaxX" shape="rect" title="DOM:window.scrollMaxX">window.scrollMaxX</a>
* </dt><dd> The maximum offset that the window can be scrolled to horizontally.
* </dd><dd> (i.e., the document width minus the viewport width)
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollMaxY" shape="rect" title="DOM:window.scrollMaxY">window.scrollMaxY</a>
* </dt><dd> The maximum offset that the window can be scrolled to vertically.
* </dd><dd> (i.e., the document height minus the viewport height)
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollX" shape="rect" title="DOM:window.scrollX">window.scrollX</a>
* </dt><dd> Returns the number of pixels that the document has already been scrolled horizontally.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollY" shape="rect" title="DOM:window.scrollY">window.scrollY</a>
* </dt><dd> Returns the number of pixels that the document has already been scrolled vertically.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.self" shape="rect" title="DOM:window.self">window.self</a>
* </dt><dd> Returns an object reference to the window object itself.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="Storage#sessionStorage" shape="rect" title="DOM:Storage">window.sessionStorage</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> A storage object for storing data within a single page session.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.sidebar" shape="rect" title="DOM:window.sidebar">window.sidebar</a>
* </dt><dd> Returns a reference to the window object of the sidebar.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.status" shape="rect" title="DOM:window.status">window.status</a>
* </dt><dd> Gets/sets the text in the statusbar at the bottom of the browser.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.statusbar" shape="rect" title="DOM:window.statusbar">window.statusbar</a>
* </dt><dd> Returns the statusbar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.toolbar" shape="rect" title="DOM:window.toolbar">window.toolbar</a>
* </dt><dd> Returns the toolbar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.top" shape="rect" title="DOM:window.top">window.top</a>
* </dt><dd> Returns a reference to the topmost window in the window hierarchy.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.window" shape="rect" title="DOM:window.window">window.window</a>
* </dt><dd> Returns a reference to the current window.
* </dd></dl>
* <h2> <span> Methods </span></h2>
* <dl><dt style="font-weight:bold"> <a href="DOM:window.alert" shape="rect" title="DOM:window.alert">window.alert</a>
* </dt><dd> Displays an alert dialog.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.addEventListener" shape="rect" title="DOM:element.addEventListener">window.addEventListener</a>
* </dt><dd> Register an event handler to a specific event type on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.atob" shape="rect" title="DOM:window.atob">window.atob</a>
* </dt><dd> Decodes a string of data which has been encoded using base-64 encoding.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.back" shape="rect" title="DOM:window.back">window.back</a>
* </dt><dd> Moves back one in the window history.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.blur" shape="rect" title="DOM:window.blur">window.blur</a>
* </dt><dd> Sets focus away from the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.btoa" shape="rect" title="DOM:window.btoa">window.btoa</a>
* </dt><dd> Creates a base-64 encoded ASCII string from a string of binary data.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.captureEvents" shape="rect" title="DOM:window.captureEvents">window.captureEvents</a> <span style="border: 1px solid #FF9999; background-color: #FFDBDB; font-size: 9px; vertical-align: text-top;">Obsolete</span>
* </dt><dd> Registers the window to capture all events of the specified type.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.clearInterval" shape="rect" title="DOM:window.clearInterval">window.clearInterval</a>
* </dt><dd> Cancels the repeated execution set using <code>setInterval</code>.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.clearTimeout" shape="rect" title="DOM:window.clearTimeout">window.clearTimeout</a>
* </dt><dd> Clears a delay that's been set for a specific function.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.close" shape="rect" title="DOM:window.close">window.close</a>
* </dt><dd> Closes the current window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.confirm" shape="rect" title="DOM:window.confirm">window.confirm</a>
* </dt><dd> Displays a dialog with a message that the user needs to respond to.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.dump" shape="rect" title="DOM:window.dump">window.dump</a>
* </dt><dd> Writes a message to the console.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.escape" shape="rect" title="DOM:window.escape">window.escape</a>
* </dt><dd> Encodes a string.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.find" shape="rect" title="DOM:window.find">window.find</a>
* </dt><dd> Searches for a given string in a window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.focus" shape="rect" title="DOM:window.focus">window.focus</a>
* </dt><dd> Sets focus on the current window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.forward" shape="rect" title="DOM:window.forward">window.forward</a>
* </dt><dd> Moves the window one document forward in the history.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.getAttention" shape="rect" title="DOM:window.getAttention">window.getAttention</a>
* </dt><dd> Flashes the application icon.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.getComputedStyle" shape="rect" title="DOM:window.getComputedStyle">window.getComputedStyle</a>
* </dt><dd> Gets computed style for the specified element. Computed style indicates the computed values of all CSS properties of the element.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.getSelection" shape="rect" title="DOM:window.getSelection">window.getSelection</a>
* </dt><dd> Returns the selection object representing the selected item(s).
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.home" shape="rect" title="DOM:window.home">window.home</a>
* </dt><dd> Returns the browser to the home page.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.moveBy" shape="rect" title="DOM:window.moveBy">window.moveBy</a>
* </dt><dd> Moves the current window by a specified amount.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.moveTo" shape="rect" title="DOM:window.moveTo">window.moveTo</a>
* </dt><dd> Moves the window to the specified coordinates.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.open" shape="rect" title="DOM:window.open">window.open</a>
* </dt><dd> Opens a new window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.openDialog" shape="rect" title="DOM:window.openDialog">window.openDialog</a>
* </dt><dd> Opens a new dialog window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.print" shape="rect" title="DOM:window.print">window.print</a>
* </dt><dd> Opens the Print Dialog to print the current document.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.prompt" shape="rect" title="DOM:window.prompt">window.prompt</a>
* </dt><dd> Returns the text entered by the user in a prompt dialog.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.releaseEvents" shape="rect" title="DOM:window.releaseEvents">window.releaseEvents</a> <span style="border: 1px solid #FF9999; background-color: #FFDBDB; font-size: 9px; vertical-align: text-top;">Obsolete</span>
* </dt><dd> Releases the window from trapping events of a specific type.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.removeEventListener" shape="rect" title="DOM:element.removeEventListener">window.removeEventListener</a>
* </dt><dd> Removes an event listener from the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.resizeBy" shape="rect" title="DOM:window.resizeBy">window.resizeBy</a>
* </dt><dd> Resizes the current window by a certain amount.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.resizeTo" shape="rect" title="DOM:window.resizeTo">window.resizeTo</a>
* </dt><dd> Dynamically resizes window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scroll" shape="rect" title="DOM:window.scroll">window.scroll</a>
* </dt><dd> Scrolls the window to a particular place in the document.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollBy" shape="rect" title="DOM:window.scrollBy">window.scrollBy</a>
* </dt><dd> Scrolls the document in the window by the given amount.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollByLines" shape="rect" title="DOM:window.scrollByLines">window.scrollByLines</a>
* </dt><dd> Scrolls the document by the given number of lines.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollByPages" shape="rect" title="DOM:window.scrollByPages">window.scrollByPages</a>
* </dt><dd> Scrolls the current document by the specified number of pages.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollTo" shape="rect" title="DOM:window.scrollTo">window.scrollTo</a>
* </dt><dd> Scrolls to a particular set of coordinates in the document.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.setInterval" shape="rect" title="DOM:window.setInterval">window.setInterval</a>
* </dt><dd> Execute a function each X milliseconds.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.setTimeout" shape="rect" title="DOM:window.setTimeout">window.setTimeout</a>
* </dt><dd> Sets a delay for executing a function.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.showModalDialog" shape="rect" title="DOM:window.showModalDialog">window.showModalDialog</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> Displays a modal dialog.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.sizeToContent" shape="rect" title="DOM:window.sizeToContent">window.sizeToContent</a>
* </dt><dd> Sizes the window according to its content.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.stop" shape="rect" title="DOM:window.stop">window.stop</a>
* </dt><dd> This method stops window loading.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.unescape" shape="rect" title="DOM:window.unescape">window.unescape</a>
* </dt><dd> Unencodes a value that has been encoded in hexadecimal (e.g. a cookie).
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.updateCommands" shape="rect" title="DOM:window.updateCommands">window.updateCommands</a>
* </dt><dd> Updates the state of commands of the current chrome window (UI).
* </dd></dl>
* <h2> <span> Event Handlers </span></h2>
* <dl><dt style="font-weight:bold"> <a href="DOM:window.onabort" shape="rect" title="DOM:window.onabort">window.onabort</a>
* </dt><dd> An event handler property for abort events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.onbeforeunload&amp;action=edit" shape="rect" title="DOM:window.onbeforeunload">window.onbeforeunload</a>
* </dt><dd> An event handler property for before-unload events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onblur" shape="rect" title="DOM:window.onblur">window.onblur</a>
* </dt><dd> An event handler property for blur events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onchange" shape="rect" title="DOM:window.onchange">window.onchange</a>
* </dt><dd> An event handler property for change events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onclick" shape="rect" title="DOM:window.onclick">window.onclick</a>
* </dt><dd> An event handler property for click events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onclose" shape="rect" title="DOM:window.onclose">window.onclose</a>
* </dt><dd> An event handler property for handling the window close event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.oncontextmenu&amp;action=edit" shape="rect" title="DOM:window.oncontextmenu">window.oncontextmenu</a>
* </dt><dd> An event handler property for right-click events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.ondragdrop" shape="rect" title="DOM:window.ondragdrop">window.ondragdrop</a>
* </dt><dd> An event handler property for drag and drop events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onerror" shape="rect" title="DOM:window.onerror">window.onerror</a>
* </dt><dd> An event handler property for errors raised on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onfocus" shape="rect" title="DOM:window.onfocus">window.onfocus</a>
* </dt><dd> An event handler property for focus events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onkeydown" shape="rect" title="DOM:window.onkeydown">window.onkeydown</a>
* </dt><dd> An event handler property for keydown events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onkeypress" shape="rect" title="DOM:window.onkeypress">window.onkeypress</a>
* </dt><dd> An event handler property for keypress events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onkeyup" shape="rect" title="DOM:window.onkeyup">window.onkeyup</a>
* </dt><dd> An event handler property for keyup events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onload" shape="rect" title="DOM:window.onload">window.onload</a>
* </dt><dd> An event handler property for window loading.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmousedown" shape="rect" title="DOM:window.onmousedown">window.onmousedown</a>
* </dt><dd> An event handler property for mousedown events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmousemove" shape="rect" title="DOM:window.onmousemove">window.onmousemove</a>
* </dt><dd> An event handler property for mousemove events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmouseout" shape="rect" title="DOM:window.onmouseout">window.onmouseout</a>
* </dt><dd> An event handler property for mouseout events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmouseover" shape="rect" title="DOM:window.onmouseover">window.onmouseover</a>
* </dt><dd> An event handler property for mouseover events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmouseup" shape="rect" title="DOM:window.onmouseup">window.onmouseup</a>
* </dt><dd> An event handler property for mouseup events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onpaint" shape="rect" title="DOM:window.onpaint">window.onpaint</a>
* </dt><dd> An event handler property for paint events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onreset" shape="rect" title="DOM:window.onreset">window.onreset</a>
* </dt><dd> An event handler property for reset events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onresize" shape="rect" title="DOM:window.onresize">window.onresize</a>
* </dt><dd> An event handler property for window resizing.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onscroll" shape="rect" title="DOM:window.onscroll">window.onscroll</a>
* </dt><dd> An event handler property for window scrolling.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onselect" shape="rect" title="DOM:window.onselect">window.onselect</a>
* </dt><dd> An event handler property for window selection.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onsubmit" shape="rect" title="DOM:window.onsubmit">window.onsubmit</a>
* </dt><dd> An event handler property for submits on window forms.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onunload" shape="rect" title="DOM:window.onunload">window.onunload</a>
* </dt><dd> An event handler property for unload events on the window.
* </dd></dl>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var Window = {
  // This is just a stub for a builtin native JavaScript object.
/**
* <h2> <span> Summary </span></h2>
* <p><code>addEventListener</code> allows the registration of event listeners on the event target. An event target may be a node in a document, the <a href="document" shape="rect" title="DOM:document">document</a> itself, a <a href="window" shape="rect" title="DOM:window">window</a>, or an <a href="http://developer.mozilla.org/en/docs/XMLHttpRequest" shape="rect" title="XMLHttpRequest">XMLHttpRequest</a>.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>target</i>.addEventListener(<i>type</i>, <i>listener</i>, <i>useCapture</i>);
* </pre>
* <dl><dt style="font-weight:bold"> type </dt><dd> A string representing the event type to listen for.
* </dd><dt style="font-weight:bold"> listener </dt><dd> The object that receives a notification when an event of the specified type occurs. This must be an object implementing the <a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventListener" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventListener"><code>EventListener</code></a> interface, or simply a JavaScript <a href="http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Guide:Functions" shape="rect" title="Core JavaScript 1.5 Guide:Functions">function</a>.
* </dd><dt style="font-weight:bold"> useCapture </dt><dd> If <code>true</code>, <code>useCapture</code> indicates that the user wishes to initiate capture. After initiating capture, all events of the specified type will be dispatched to the registered <code>listener</code> before being dispatched to any <code>EventTarget</code>s beneath it in the DOM tree. Events which are bubbling upward through the tree will not trigger a listener designated to use capture. See <a href="http://www.w3.org/TR/DOM-Level-3-Events/events.html#Events-flow" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-3-Events/events.html#Events-flow">DOM Level 3 Events</a> for a detailed explanation.
* </dd></dl>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;DOM Event Example&lt;/title&gt;
* &lt;style type="text/css"&gt;
* #t { border: 1px solid red }
* #t1 { background-color: pink; }
* &lt;/style&gt;
* &lt;script type="text/javascript"&gt;
* 
* // Function to change the content of t2
* function modifyText() {
* var t2 = document.getElementById("t2");
* t2.firstChild.nodeValue = "three";
* }
* 
* // Function to add event listener to t
* function load() {
* var el = document.getElementById("t");
* el.addEventListener("click", modifyText, false);
* }
* 
* &lt;/script&gt;
* &lt;/head&gt;
* &lt;body onload="load();"&gt;
* &lt;table id="t"&gt;
* &lt;tr&gt;&lt;td id="t1"&gt;one&lt;/td&gt;&lt;/tr&gt;
* &lt;tr&gt;&lt;td id="t2"&gt;two&lt;/td&gt;&lt;/tr&gt;
* &lt;/table&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <p>In the above example, <code>modifyText()</code> is a listener for <code>click</code> events registered using <code>addEventListener()</code>.  A click anywhere on the table will bubble up to the handler and run <code>modifyText()</code>.
* </p>
* <h2> <span> Notes </span></h2>
* <h3> <span> Why use <code>addEventListener</code>? </span></h3>
* <p><code>addEventListener</code> is the way to register an event listener as specified in W3C DOM. Its benefits are as follows:
* </p>
* <ul><li> It allows adding more than a single handler for an event. This is particularly useful for <a href="http://developer.mozilla.org/en/docs/DHTML" shape="rect" title="DHTML">DHTML</a> libraries or <a href="http://developer.mozilla.org/en/docs/Extensions" shape="rect" title="Extensions">Mozilla extensions</a> that need to work well even if other libraries/extensions are used.
* </li><li> It gives you finer-grained control of the phase when the listener gets activated (capturing vs. bubbling)
* </li><li> It works on any DOM element, not just HTML elements.
* </li></ul>
* <p>The alternative, older way to register event handlers is <a href="element.addEventListener#Older_way_to_attach_events" shape="rect" title="">described below</a>.
* </p>
* <h3> <span> Adding a listener during event dispatch </span></h3>
* <p>If an <code>EventListener</code> is added to an <code>EventTarget</code> while it is processing an event, it will not be triggered by the current actions but may be triggered during a later stage of event flow, such as the bubbling phase.
* </p>
* <h3> <span> Multiple identical event listeners </span></h3>
* <p>If multiple identical <code>EventListener</code>s are registered on the same <code>EventTarget</code> with the same parameters, the duplicate instances are discarded. They do not cause the <code>EventListener</code> to be called twice, and since the duplicates are discarded, they do not need to be removed manually with the <a href="element.removeEventListener" shape="rect" title="DOM:element.removeEventListener">removeEventListener</a> method.
* </p>
* <h3> <span> The value of <code>this</code> </span></h3>
* <p>Attaching a function using <code>addEventListener()</code> changes the value of <code>this</code>—note that the value of <code>this</code> is passed to a function from the caller.
* </p><p>In the example above, the value of <code>this</code> within <code>modifyText()</code> when called from the onclick event is a reference to the table 't'.  If the onclick handler is added in the HTML source:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;table id="t" onclick="modifyText();"&gt;
* ...
* &lt;/table&gt;
* </pre>
* <p>then value of <code>this</code> within <code>modifyText()</code> when called from the onclick event will be a reference to the global (window) object.
* </p>
* <h3> <span> Internet Explorer </span></h3>
* <p>In IE you have to use <code>attachEvent</code> rather than the standard <code>addEventListener</code>. To support IE, the example above can be modified to:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* if (el.addEventListener){
* el.addEventListener('click', modifyText, false);
* } else if (el.attachEvent){
* el.attachEvent('onclick', modifyText);
* }
* </pre>
* <h3> <span>Older way to attach events</span></h3>
* <p><code>addEventListener()</code> was introduced with the DOM 2 <a href="http://www.w3.org/TR/DOM-Level-2-Events" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events">Events</a> specification. Before then, events were attached as follows:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // Using a function reference—note lack of '()'
* el.onclick = modifyText;
* 
* // Using a function expression
* element.onclick = function(){
* ...statements...
* };
* </pre>
* <p>This method replaces the existing <code>onclick</code> event handler(s) on the element if there are any.  Similarly for other 'on' events such as <code>onblur</code>, <code>onkeypress</code>, and so on.
* </p><p>Because it was essentially part of DOM 0, this method is very widely supported and requires no special cross–browser code; hence it is normally used to attach events dynamically unless the extra features of <code>addEventListener()</code> are needed.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventTarget-addEventListener" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventTarget-addEventListener">DOM Level 2 Events: addEventListener</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
addEventListener: function(type, listener, useCapture) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Display an alert dialog with the specified text.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.alert(<i>message</i>);
* </pre>
* <ul><li> <code>message</code> is a string of text you want to display in the alert dialog.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.alert("Hello world!");
* // the "window." prefix <a href="window.window" shape="rect" title="DOM:window.window">is optional</a>, so
* // alert("Hello world!"); would have the same effect
* </pre>
* <p>produces:
* </p><p><a href="http://developer.mozilla.org/en/docs/Image:AlertHelloWorld.png" shape="rect" title="Image:AlertHelloWorld.png"/>
* </p>
* <h2> <span> Notes </span></h2>
* <p>The alert dialog should be used for messages which do not require any response on the part of the user, other than the acknowledgement of the message.
* </p><p>Dialog boxes are modal windows - they prevent the user from accessing the rest of the program's interface until the dialog box is closed. For this reason, you should not overuse any function that creates a dialog box (or modal window).
* </p><p>Chrome users (e.g. extensions) should use methods of <a href="http://developer.mozilla.org/en/docs/nsIPromptService" shape="rect" title="nsIPromptService">nsIPromptService</a> instead.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* <h2> <span> See also </span></h2>
* <p><a href="window.confirm" shape="rect" title="DOM:window.confirm">confirm</a>, <a href="window.prompt" shape="rect" title="DOM:window.prompt">prompt</a>
* </p><p>For chrome see <a href="http://developer.mozilla.org/en/docs/nsIPromptService#alert" shape="rect" title="nsIPromptService">alert()</a> and <a href="http://developer.mozilla.org/en/docs/nsIPromptService#alert" shape="rect" title="nsIPromptService">alertCheck()</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
alert: function(message) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Decodes a string of data which has been encoded using base-64 encoding.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var decodedData = window.atob(<i>encodedData</i>);
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var encodedData = window.btoa("Hello, world"); // encode a string
* var decodedData = window.atob(encodedData); // decode the string
* </pre>
* <h2> <span> Notes </span></h2>
* <p>You can use the <code><a href="DOM:window.btoa" shape="rect" title="DOM:window.btoa">window.btoa()</a></code> method to encode and transmit data which may otherwise cause communication problems, then transmit it and use the <code>window.atob()</code> method to decode the data again. For example, you can encode, transmit, and decode characters such as ASCII values 0 through 31.
* </p><p><code>atob()</code> is also available to XPCOM components implemented in JavaScript, even though <code><a href="window" shape="rect" title="DOM:window">window</a></code> is not the global object in components.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* <h2> <span> See also </span></h2>
* <p><a href="window.btoa" shape="rect" title="DOM:window.btoa">btoa</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
atob: function(encodedData) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the amount of vertical space available to the window on the screen.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">iAvail = window.screen.availHeight
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if window.screen.availHeight != window.screen.height {
* // something's in the way!
* // use available to size window
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p><i>no notes</i>
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
availHeight: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the first available pixel available from the left side of the screen.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">iAvail = window.screen.availLeft
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">setY = window.screen.height - window.screen.availTop;
* setX = window.screen.width - window.screen.availLeft;
* window.moveTo(setX, setY);
* </pre>
* <h2> <span> Notes </span></h2>
* <p>In most cases, this property returns 0.
* </p><p>If you work with two screens this property evaluated on the right screen returns width of the left one in pixels. This way you can detect whether there is a screen available on the left. (Similar analogy aplies to screen.availTop property.)
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
availLeft: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Specifies the y-coordinate of the first pixel that is not allocated to permanent or semipermanent user interface features.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">iAvail = window.screen.availTop
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">setY = window.screen.height - window.screen.availTop;
* setX = window.screen.width - window.screen.availLeft;
* window.moveTo(setX, setY);
* </pre>
* <h2> <span> Notes </span></h2>
* <p>In most cases, this property returns 0.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
availTop: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the amount of horizontal space in pixels available to the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">iAvail = window.screen.availWidth
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// example code here
* </pre>
* <h2> <span> Notes </span></h2>
* <p><i>no notes</i>
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
availWidth: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the window to the previous item in the history.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.back()
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function goBack() {
* if ( canGoBack )
* window.back();
* }
* </pre>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
back: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Shifts focus away from the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.blur()
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.blur();
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The window.blur() method is the programmatic equivalent of the user shifting focus away from the current window.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
blur: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Creates a base-64 encoded ASCII string from a string of binary data.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var encodedData = window.btoa(<i>stringToEncode</i>);
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var encodedData = window.btoa("Hello, world"); // encode a string
* var decodedData = atob(encodedData); // decode the string
* </pre>
* <h2> <span> Notes </span></h2>
* <p>You can use this method to encode data which may otherwise cause communication problems, transmit it, then use the <a href="DOM:window.atob" shape="rect" title="DOM:window.atob">window.atob</a> method to decode the data again. For example, you can encode characters such as ASCII values 0 through 31.
* </p><p><code>btoa()</code> is also available to XPCOM components implemented in JavaScript, even though <code><a href="window" shape="rect" title="DOM:window">window</a></code> is not the global object in components.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* <h2> <span> See also </span></h2>
* <p><a href="window.atob" shape="rect" title="DOM:window.atob">atob</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
btoa: function(stringToEncode) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <div style="border: 1px solid #FF5151; background-color: #FEBCBC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Obsolete</p></div>
* <p>Registers the window to capture all events of the specified type.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.captureEvents(<i>eventType</i>)
* </pre>
* <p><code>eventType</code> is one  of the following values: <code>Event.ABORT</code>, <code>Event.BLUR</code>, <code>Event.CLICK</code>, <code>Event.CHANGE</code>, <code>Event.DBLCLICK</code>, <code>Event.DRAGDDROP</code>, <code>Event.ERROR</code>, <code>Event.FOCUS</code>, <code>Event.KEYDOWN</code>, <code>Event.KEYPRESS</code>, <code>Event.KEYUP</code>, <code>Event.LOAD</code>, <code>Event.MOUSEDOWN</code>, <code>Event.MOUSEMOVE</code>, <code>Event.MOUSEOUT</code>, <code>Event.MOUSEOVER</code>, <code>Event.MOUSEUP</code>, <code>Event.MOVE</code>, <code>Event.RESET</code>, <code>Event.RESIZE</code>, <code>Event.SELECT</code>, <code>Event.SUBMIT</code>, <code>Event.UNLOAD</code>.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;script&gt;
* function reg() {
* window.captureEvents(Event.CLICK);
* window.onclick = page_click;
* }
* 
* function page_click() {
* alert('page click event detected!');
* }
* &lt;/script&gt;
* 
* &lt;body onload="reg();"&gt;
* &lt;p&gt;click anywhere on this page.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <div>
* <p>This method is obsolete as of Gecko 1.9, in favor of W3C DOM Events methods (see <a href="DOM:element.addEventListener" shape="rect" title="DOM:element.addEventListener">addEventListener</a>). The support for this method <a href="http://developer.mozilla.org/en/docs/Gecko_1.9_Changes_affecting_websites" shape="rect" title="Gecko 1.9 Changes affecting websites">has been removed</a> from <a href="http://developer.mozilla.org/en/docs/Gecko" shape="rect" title="Gecko">Gecko</a> 1.9.
* </p>
* </div>
* <p>Events raised in the DOM by user activity (such as clicking buttons or shifting focus away from the current document) generally pass through the high-level <a href="Gecko_DOM_Reference:window" shape="rect" title="Gecko DOM Reference:window">window</a> and <a href="Gecko_DOM_Reference:document" shape="rect" title="Gecko DOM Reference:document">document</a> objects first before arriving at the object that initiated the event.
* </p><p>When you call the <code>captureEvents()</code> method on the <a href="window" shape="rect" title="DOM:window">window</a>, events of the type you specify (for example, <code>Event.CLICK</code>) no longer pass through to "lower" objects in the hierarchy. In order for events to "bubble up" in the way that they normally do, you must call <a href="window.releaseEvents" shape="rect" title="DOM:window.releaseEvents">window.releaseEvents()</a> (<span style="border: 1px solid #FF9999; background-color: #FFDBDB; font-size: 9px; vertical-align: text-top;">Obsolete</span>) on the window to keep it from trapping events.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
captureEvents: function(eventType) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary</span></h2>
* <p>Cancels repeated action which was set up using <code><a href="window.setInterval" shape="rect" title="DOM:window.setInterval">setInterval()</a></code>.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>window</i>.clearInterval(<i>intervalID</i>)
* </pre>
* <p><code>intervalID</code> is the identifier of the repeated action you want to cancel. This ID is returned from <code>setInterval()</code>.
* </p>
* <h2> <span> Example </span></h2>
* <p>See the <a href="window.setInterval#Example" shape="rect" title="DOM:window.setInterval"><code>setInterval()</code> example</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
clearInterval: function(intervalID) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Clears the delay set by <a href="window.setTimeout" shape="rect" title="DOM:window.setTimeout">window.setTimeout()</a>.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.clearTimeout(<i>timeoutID</i>)
* </pre>
* <p>where <code>timeoutID</code> is the ID of the timeout you wish you clear, as returned by <a href="DOM:window.setTimeout" shape="rect" title="DOM:window.setTimeout">window.setTimeout()</a>.
* </p>
* <h2> <span> Example </span></h2>
* <p>Run the script below in the context of a web page and click on the page once. You'll see a message popping up in a second. If you keep clicking on the page once in a second, the alert never appears.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var alarm = {
* remind: function(aMessage) {
* alert(aMessage);
* delete this.timeoutID;
* },
* 
* setup: function() {
* this.cancel();
* var self = this;
* this.timeoutID = window.setTimeout(function(msg) {self.remind(msg);}, 1000, "Wake up!");
* },
* 
* cancel: function() {
* if(typeof this.timeoutID == "number") {
* window.clearTimeout(this.timeoutID);
* delete this.timeoutID;
* }
* }
* };
* window.onclick = function() { alarm.setup() };
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Passing an invalid ID to <code>clearTimeout</code> does not have any effect (and doesn't throw an exception).
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
clearTimeout: function(timeoutID) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Closes the current window, or a referenced window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>window</i>.close();
* </pre>
* <h2> <span> Description </span></h2>
* <p>When this method is called, the referenced window is closed.
* </p><p>This method is only allowed to be called for windows that were opened by a script using the <a href="window.open" shape="rect" title="DOM:window.open">window.open</a> method. If the window was not opened by a script, the following error appears in the JavaScript Console: <code>Scripts may not close windows that were not opened by script.</code>
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Closing a window opened with <code>window.open()</code> </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* //Global var to store a reference to the opened window
* var openedWindow;
* 
* function openWindow()
* {
* openedWindow = window.open('moreinfo.htm');
* }
* function closeOpenedWindow()
* {
* openedWindow.close();
* }
* &lt;/script&gt;
* </pre>
* <h3> <span> Closing the current window </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* function closeCurrentWindow()
* {
* window.close();
* }
* &lt;/script&gt;
* </pre>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* <h2> <span> Additional Reference </span></h2>
* <p><a href="http://msdn2.microsoft.com/en-us/library/ms536367.aspx" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/ms536367.aspx">MSDN window.close()</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
close: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>This property indicates whether the referenced window is closed or not.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>isClosed</var> = <var>windowRef</var>.closed;
* </pre>
* <p>
* This property is read-only.
* </p>
* <h2> <span> Return Value </span></h2>
* <dl><dt style="font-weight:bold"> <code>isClosed</code> </dt><dd> A boolean. Possible Values:
* </dd></dl>
* <ul><li> <code>true</code>: The window has been closed.
* </li><li> <code>false</code>: The window is open.
* </li></ul>
* <h2> <span> Examples </span></h2>
* <h3> <span> Change the URL of a window from a popup </span></h3>
* <p>The following example demonstrates how a popup window can change the URL of the window that opened it. Before attempting to change the URL, it checks that the current window has an opener using the <a href="window.opener" shape="rect" title="DOM:window.opener">window.opener</a> property and that the opener isn't closed:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // Check that an opener exists and is not closed
* if (window.opener &amp;&amp; !window.opener.closed) {
* window.opener.location.href = "http://www.mozilla.org";
* }
* </pre>
* <p>Note that popups can only access the window that opened them.
* </p>
* <h3> <span> Refreshing a previously opened popup </span></h3>
* <p>In this example the function <code>refreshPopupWindow()</code> calls the <code>reload</code> method of the popup's location object to refresh its data. If the popup hasn't been opened yet or the user has closed it a new window is opened.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var popupWindow = null;
* 
* function refreshPopupWindow() {
* if (popupWindow &amp;&amp; !popupWindow.closed) {
* // popupWindow is open, refresh it
* popupWindow.location.reload(true);
* } else {
* // Open a new popup window
* popupWindow = window.open("popup.html","dataWindow");
* }
* }
* </pre>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. <code>window.closed</code> is not part of any W3C specification or technical recommendation.
* </p>
* <h2> <span> Additional Reference </span></h2>
* <p><a href="http://msdn2.microsoft.com/en-us/library/ms533574.aspx" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/ms533574.aspx">MSDN window.closed</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
closed: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the color depth of the screen.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">bitDepth = window.screen.colorDepth
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// check the color depth of the screen
* if ( window.screen.colorDepth &lt; 8) {
* // use low-color version of page
* } else {
* // use regular, colorful page
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>See also <a href="DOM:window.screen.pixelDepth" shape="rect" title="DOM:window.screen.pixelDepth">window.screen.pixelDepth</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
colorDepth: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Displays a modal dialog with a message and two buttons, OK and Cancel.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>result</i> = window.confirm(<i>message</i>);
* </pre>
* <ul><li> <code>message</code> is the string to be displayed in the dialog.
* </li><li> <code>result</code> is a boolean value indicating whether OK or Cancel was selected (<code>true</code> means OK).
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (window.confirm("Want to see my mood ring?")) {
* window.open("mood.html", "mood ring", "");
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Dialog boxes are modal windows - they prevent the user from accessing the rest of the program's interface until the dialog box is closed. For this reason, you should not overuse any function that creates a dialog box (or modal window).
* </p><p>Chrome users (e.g. extensions) should use methods of <a href="http://developer.mozilla.org/en/docs/nsIPromptService" shape="rect" title="nsIPromptService">nsIPromptService</a> instead.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* <h2> <span> See also </span></h2>
* <p><a href="window.alert" shape="rect" title="DOM:window.alert">alert</a>, <a href="window.prompt" shape="rect" title="DOM:window.prompt">prompt</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
confirm: function(message) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a <a href="window" shape="rect" title="DOM:window">Window</a> object for the primary content window. This is useful in XUL windows that have a <code>&lt;browser&gt;</code> (or <code>tabbrowser</code> or <code>&lt;iframe&gt;</code>) with <code>type="content-primary"</code> attribute on it - the most famous example is Firefox main window, <code>browser.xul</code>. In such cases, <code>content</code> returns a reference to the <code>Window</code> object for the document currently displayed in the browser. It is a shortcut for <code><var>browserRef</var>.contentWindow</code>.
* </p><p>In unprivileged content (webpages), <code>content</code> is normally equivalent to <a href="window.top" shape="rect" title="DOM:window.top">top</a> (except in the case of a webpage loaded in a sidebar, <code>content</code> still refers to the <code>Window</code> of the currently selected tab).
* </p><p>Some examples use <code>_content</code> instead of <code>content</code>. The former has been deprecated for a long time, and you should use <code>content</code> in the new code.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>windowObject</var> = window.content;
* </pre>
* <h2> <span> Example </span></h2>
* <p>Executing the following code in a chrome XUL window with a <code>&lt;browser type="content-primary"/&gt;</code> element in it draws a red border around the first div on the page currently displayed in the browser:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">content.document.getElementsByTagName("div")[0].style.border = "solid red 1px";
* </pre>
* <h2> <span> Specification </span></h2>
* <p>Not part of any W3C specification.
* </p>
* <h2> <span> See also </span></h2>
* <ul><li> <a href="http://developer.mozilla.org/en/docs/Working_with_windows_in_chrome_code" shape="rect" title="Working with windows in chrome code">Working with windows in chrome code</a>
* </li><li> When accessing content documents from privileged code, be aware of <a href="http://developer.mozilla.org/en/docs/XPCNativeWrapper" shape="rect" title="XPCNativeWrapper">XPCNativeWrappers</a>.
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
content: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the XUL controllers of the chrome window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>controllers</i> = window.controllers
* </pre>
* <ul><li> <code>controllers</code> is an object of type <code><a href="http://developer.mozilla.org/en/docs/index.php?title=XULControllers&amp;action=edit" shape="rect" title="XULControllers">XULControllers</a></code> (<code><a href="http://developer.mozilla.org/en/docs/index.php?title=nsIControllers&amp;action=edit" shape="rect" title="nsIControllers">nsIControllers</a></code>).
* </li></ul>
* <h2> <span> Specification </span></h2>
* <p>XUL-specific. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
controllers: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the browser crypto object, which can then be used to manipulate various browser security features.
* </p><p>See <a href="http://developer.mozilla.org/en/docs/JavaScript_crypto" shape="rect" title="JavaScript crypto">JavaScript crypto</a> for details.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
crypto: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Gets/sets the status bar text for the given window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>sMsg</i> = window.defaultStatus
* window.defaultStatus = <i>sMsg</i>
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>sMsg</code> is a string containing the text to be displayed by default in the statusbar.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;body onload="window.defaultStatus='hello!';"/&gt;
* &lt;button onclick="window.confirm('Are you sure you want to quit?');"&gt;confirm&lt;/button&gt;
* &lt;/body&gt;
* &lt;/htm&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>To set the status once the window has been opened, use <a href="http://developer.mozilla.org/en/docs/index.php?title=Gecko_DOM_Reference:window:status&amp;action=edit" shape="rect" title="Gecko DOM Reference:window:status">window.status</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
defaultStatus: undefined,
dialogArguments: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the window directories toolbar object.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>dirBar</i> = window.directories
* </pre>
* <h2> <span>Parameters </span></h2>
* <p><i>dirBar</i> is an object of the type <code>barProp</code>.
* </p>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script&gt;
* function dirs() {
* alert(window.directories);
* }
* &lt;/script&gt;
* </pre>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
directories: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns a reference to the document contained in the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>doc</i> = window.document
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>doc</code> is an object reference to a <a href="document" shape="rect" title="DOM:document">document</a>.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">&lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;Hello, World!&lt;/title&gt;
* &lt;/head&gt;
* &lt;body&gt;
* 
* &lt;script type="text/javascript"&gt;
* var doc = window.document;
* alert( doc.title);    // alerts: Hello, World!
* &lt;/script&gt;
* 
* &lt;/body&gt;
* &lt;/html&gt;</pre>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
document: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Prints messages to the console.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">dump(<i>message</i>);
* </pre>
* <ul><li> <code>message</code> is the string message to log.
* </li></ul>
* <h2> <span> Notes </span></h2>
* <p><code>dump</code> is commonly used to debug JavaScript. Privileged code can also use <code><a href="http://developer.mozilla.org/en/docs/Components.utils.reportError" shape="rect" title="Components.utils.reportError">Components.utils.reportError</a></code> and <code><a href="http://developer.mozilla.org/en/docs/nsIConsoleService" shape="rect" title="nsIConsoleService">nsIConsoleService</a></code> to log messages to the <a href="http://developer.mozilla.org/en/docs/Error_Console" shape="rect" title="Error Console">Error Console</a>.
* </p><p>In <a href="http://developer.mozilla.org/en/docs/Gecko" shape="rect" title="Gecko">Gecko</a> <code>dump</code> is disabled by default – it doesn't do anything but doesn't raise an error either. To see the <code>dump</code> output you have to enable it by setting the preference <code>browser.dom.window.dump.enabled</code> to <code>true</code>. You can set the preference in <a href="http://kb.mozillazine.org/About:config" rel="nofollow" shape="rect" title="http://kb.mozillazine.org/About:config">about:config</a> or in a <a href="http://kb.mozillazine.org/User.js_file" rel="nofollow" shape="rect" title="http://kb.mozillazine.org/User.js_file">user.js file</a>. Note: this preference is not listed in <tt>about:config</tt> by default, you may need to create it (right-click the content area -&gt; New -&gt; Boolean).
* </p><p>On Windows, you will need a console to actually see anything. If you don't have one already, closing the application and re-opening it with the command line parameter <tt>-console</tt> should create the console. On other operating systems, it's enough to launch the application from a terminal.
* </p><p><code>dump</code> is also available to XPCOM components implemented in JavaScript, even though <code><a href="window" shape="rect" title="DOM:window">window</a></code> is not the global object in components.  However, this use of <code>dump</code> is not affected by the aforementioned preference -- it will always be shown. It is therefore advisable to either check this preference yourself or use a debugging preference of your own to make sure you don't send lots of debugging content to a user's console when they might not be interested in it at all.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
dump: function(message) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Encodes a string, replacing certain characters with a hexadecimal escape sequence.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>escaped</i> = escape(<i>regular</i>);
* </pre>
* <ul><li><code>escaped</code> is the encoded string.
* </li><li><code>regular</code> is a regular string.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">alert(escape("http://www.cnn.com")); // displays: http://www.cnn.com
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The <code>escape()</code> method converts special characters (any characters that are not regular text or numbers) into hexadecimal characters, which is especially necessary for setting the values of cookies. Also useful when passing <i>name=value</i> pairs in the URL of a GET request, or an AJAX GET/POST request.
* </p><p>See also <a href="DOM:window.unescape" shape="rect" title="DOM:window.unescape">unescape</a>, <a href="http://developer.mozilla.org/en/docs/encodeURIComponent" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:encodeURIComponent">encodeURIComponent</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard. Mentioned in a non-normative section of ECMA-262.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
escape: function(regular) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Finds a string in a window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>window</i>.find(<i>aString</i>, <i>aCaseSensitive</i>, <i>aBackwards</i>, <i>aWrapAround</i>,
* <i>aWholeWord</i>, <i>aSearchInFrames</i>, <i>aShowDialog</i>);
* </pre>
* <ul><li>aString
* </li><li>aCaseSensitive
* </li><li>aBackwards
* </li><li>aWrapAround
* </li><li>aWholeWord
* </li><li>aSearchInFrames
* </li><li>aShowDialog
* </li></ul>
* <h2> <span> Example </span></h2>
* <h2> <span> Notes </span></h2>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard. This was added by with <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=9550" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=9550">bug 9550</a>.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
find: function(aString, aCaseSensitive, aBackwards, aWrapAround,aWholeWord, aSearchInFrames, aShowDialog) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Sets focus on the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.focus()
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (clicked) { window.focus(); }
* </pre>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
focus: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Moves the window one document forward in the history.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.forward()
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function goForward() { if ( canGoForward) window.forward(); }
* </pre>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
forward: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the element (such as <code>&lt;iframe&gt;</code> or <code>&lt;object&gt;</code>) in which the window is embedded, or <code>null</code> if the window is top-level.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>frameEl</i> = window.frameElement;
* </pre>
* <ul><li> <code>frameEl</code> is the element which the window is embedded into, or <code>null</code> if the window is top-level.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var frameEl = window.frameElement;
* // if we are inside a frame, then change it's URL to 'http://mozilla.org/'
* if (frameEl)
* frameEl.src = 'http://mozilla.org/';
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Note that despite its name, the property also works for documents inside <code>&lt;object&gt;</code> and other embedding points.
* </p><p>See also <code><a href="DOM:window.parent" shape="rect" title="DOM:window.parent">window.parent</a></code>, which returns the parent window, which is the window containing the <code>frameElement</code> of the child window.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
frameElement: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns an array-like object, listing the direct sub-frames of the current window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>frameList</i> = window.frames;
* </pre>
* <ul><li> <code>frameList</code> is a list of frame objects. It is similar to an array in that it has a <code>length</code> property and its items can be accessed using the <code>[i]</code> notation.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var frames = window.frames; // or // var frames = window.parent.frames;
* for (var i = 0; i &lt; frames.length; i++) {
* // do something with each subframe as frames[i]
* frames[i].document.body.style.background = "red";
* }
* </pre>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
frames: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>This property indicates whether the window is displayed in full screen mode or not. It is only reliable in Gecko 1.9 (Firefox 3) and later, see the Notes below.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>isInFullScreen</var> = <var>windowRef</var>.fullScreen;
* </pre>
* <p>
* With chrome privileges, the property is read-write, otherwise it is read-only. Bear in mind that if you try to set this property without chrome privileges, it will not throw and instead just silently fail. This is to prevent scripts designed to set this property in Internet Explorer from breaking.
* </p>
* <h2> <span> Return Value </span></h2>
* <dl><dt style="font-weight:bold"> <code>isInFullScreen</code> </dt><dd> A boolean. Possible Values:
* </dd></dl>
* <ul><li> <code>true</code>: The window is in full screen mode.
* </li><li> <code>false</code>: The window is not in full screen mode.
* </li></ul>
* <h2> <span> Examples </span></h2>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. <code>window.fullScreen</code> is not part of any W3C specification or technical recommendation.
* </p>
* <h2> <span> Notes </span></h2>
* <p>This property is only reliable in Mozilla 1.9 (Firefox 3) and later. Mozilla 1.8 and earlier do have this property, but it always returns <code>false</code>, even when the window is in full screen mode (<a href="https://bugzilla.mozilla.org/show_bug.cgi?id=127013" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=127013">bug 127013</a>).
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
fullScreen: undefined,
/**
* <h2> <span> Summary</span></h2>
* <p>Attempts to get the user's attention. How this happens varies based on OS and window manager.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.getAttention();
* </pre>
* <h2> <span> Notes </span></h2>
* <p>On Windows, the taskbar button for the window flashes, if this hasn't been disabled by the user.
* </p><p>On Linux, the behaviour varies from window manager to window manager - some flash the taskbar button, others focus the window immediately. This may be configurable as well.
* </p><p>On Macintosh, the icon in the upper right corner of the desktop flashes.
* </p><p>The function is disabled for web content. Neither Gecko nor Internet Explorer supports this feature now for web content. <code>getAttention</code> will still work when used from <a href="http://developer.mozilla.org/en/docs/chrome" shape="rect" title="chrome">chrome</a> in a Gecko application.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getAttention: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns computed style of the element. Computed style represents the final <a href="http://www.w3.org/TR/1998/REC-CSS2-19980512/cascade.html#computed-value" rel="nofollow" shape="rect" title="http://www.w3.org/TR/1998/REC-CSS2-19980512/cascade.html#computed-value">computed</a> values of all CSS properties for the element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>style</i> = window.getComputedStyle(<i>element</i>, <i>pseudoElt</i>);
* </pre>
* <ul><li> <code>element</code> is an <a href="DOM:element" shape="rect" title="DOM:element">element</a>.
* </li><li> <code>pseudoElt</code> is a string specifying the pseudo-element to match. Should be an empty string for regular elements.
* </li><li> <code>style</code> is a <a href="http://www.w3.org/TR/DOM-Level-2-Style/css.html#CSS-CSSview-getComputedStyle" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Style/css.html#CSS-CSSview-getComputedStyle"><code>CSSStyleDeclaration</code></a> object.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var element = document.getElementById(‚ÄúelemId‚Äù);
* var <i>style</i> = document.defaultView.getComputedStyle(<i>element</i>, <i>pseudoElt</i>);
* </pre>
* <h2> <span> Description </span></h2>
* <p>The returned object is of the same type that the object returned from the element's <a href="element.style" shape="rect" title="DOM:element.style">style</a> property,  however the two objects have different purpose. The object returned from <code>getComputedStyle</code> is read-only and can be used to inspect the element's style. The <code>elt.style</code> object should be used to set styles on a specific element.
* </p><p>The first argument must be an Element, not a Node (as in a #text Node).
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Style/css.html#CSS-CSSview-getComputedStyle" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Style/css.html#CSS-CSSview-getComputedStyle">DOM Level 2 Style: getComputedStyle</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getComputedStyle: function(element, pseudoElt) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary</span></h2>
* <p>Returns a selection object representing the range of text selected by the user.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>selection</i> = <i>window</i>.getSelection() ;
* </pre>
* <ul><li> <code>selection</code> is a <a href="DOM:Selection" shape="rect" title="DOM:Selection">Selection</a> object.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function foo() {
* var selObj = window.getSelection();
* alert(selObj);
* var selRange = selObj.getRangeAt(0);
* // do stuff with the range
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>In JavaScript, when a selection object is passed to a function expecting a string (like <code><a href="window.alert" shape="rect" title="DOM:window.alert">window.alert</a></code> or <code><a href="document.write" shape="rect" title="DOM:document.write">document.write</a></code>), a string representation of it (i.e. the selected text) is passed instead. This makes the selection object appear like a string, when it is really an object with its own properties and methods. Specifically, the return value of calling the <code><a href="Selection:toString" shape="rect" title="DOM:Selection:toString">toString()</a></code> method of the Selection object is passed.
* </p><p>In the above example, <code>selObj</code> is automatically "converted" when passed to <a href="window.alert" shape="rect" title="DOM:window.alert">window.alert</a>. However, to use a JavaScript <a href="http://developer.mozilla.org/en/docs/index.php?title=JS:String&amp;action=edit" shape="rect" title="JS:String">String</a> property or method such as <code><a href="http://developer.mozilla.org/en/docs/index.php?title=JS:String.prototype.length&amp;action=edit" shape="rect" title="JS:String.prototype.length">length</a></code> or <code><a href="http://developer.mozilla.org/en/docs/index.php?title=JS:String.prototype.substr&amp;action=edit" shape="rect" title="JS:String.prototype.substr">substr</a></code>, you must manually call the <code>toString</code> method.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* <h2> <span> See also </span></h2>
* <p><a href="Selection" shape="rect" title="DOM:Selection">Selection</a>, <a href="range" shape="rect" title="DOM:range">Range</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getSelection: function() {
  // This is just a stub for a builtin native JavaScript object.
},
globalStorage: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the height of the screen in pixels.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">iHeight = window.screen.height
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (window.screen.availHeight != window.screen.height) {
* // something is occupying some screen real estate!
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Note that not all of the height given by this property may be available to the window itself. Widgets such as taskbars or other special application windows that integrate with the OS (e.g., the Spinner player minimized to act like an additional toolbar on windows) may reduce the amount of space available to browser windows and other applications.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
height: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns a reference to the history object, which provides an interface for manipulating the browser history.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>historyObj</i> = window.history
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>historyObject</code> is an object reference.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* h = window.history;
* if ( h.length ) { // if there is a history
* h.back();     // equivalent to clicking back button
* }
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The <code>history</code> object provides the following interface: current, length, next, previous, back(), forward(), go().
* </p><p>You can call access to this interface from the <code>window</code> object by calling, for example, <code>window.history.back()</code>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
history: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the window to the home page.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.home()
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function goHome() {
* window.home();
* }
* </pre>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
home: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary</span></h2>
* <p>Height of the browser window viewport including, if rendered, the horizontal scrollbar.
* </p>
* <h2> <span> Syntax </span></h2>
* <p>var <var>intViewportHeight</var> = window.innerHeight;
* </p>
* <h2> <span> Value </span></h2>
* <p><var>intViewportHeight</var> stores the window.innerHeight property value.
* </p><p>The window.innerHeight property is read-only; it has no default value.
* window.innerHeight property stores an integer representing a number of pixels.
* </p>
* <h2> <span> Notes </span></h2>
* <p>The innerHeight property will be supported in any window object like a window, a frame, a frameset or a secondary window.
* </p>
* <h3> <span> Example </span></h3>
* <p><span>Assuming a frameset</span>
* <span>---------------------</span>
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var intFrameHeight = window.innerHeight; // or
* var intFrameHeight = self.innerHeight; / * will return the height of the
* frame viewport within the frameset * /
* var intFramesetHeight = parent.innerHeight; / * will return the height of
* the viewport of the closest frameset * /
* var intOuterFramesetHeight = top.innerHeight; / * will return the height
* of the viewport of the outermost frameset * /
* </pre>
* <p>xxx To do: link to an interactive demo here xxx
* </p><p>See also <a href="http://developer.mozilla.org/en/docs/window.innerWidth" shape="rect" title="window.innerWidth">window.innerWidth</a>, <a href="http://developer.mozilla.org/en/docs/window.outerHeight" shape="rect" title="window.outerHeight">window.outerHeight</a> and <a href="http://developer.mozilla.org/en/docs/window.outerWidth" shape="rect" title="window.outerWidth">window.outerWidth</a>.
* </p>
* <h2> <span> Graphical example </span></h2>
* <p style="margin-left: -32px;"><a href="http://developer.mozilla.org/en/docs/Image:FirefoxInnerVsOuterHeight2.png" shape="rect" title="innerHeight vs outerHeight illustration"/></p>
* <h2> <span> Standards information </span></h2>
* <p>DOM Level 0. Not part of any <abbr title="World Wide Web Consortium">W3C</abbr> technical specification or recommendation.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
innerHeight: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Width of the browser window viewport including, if rendered, the vertical scrollbar.
* </p>
* <h2> <span> Syntax </span></h2>
* <p>var <var>intViewportWidth</var> = window.innerWidth;
* </p>
* <h2> <span> Value </span></h2>
* <p><var>intViewportWidth</var> stores the window.innerWidth property value.
* </p><p>The window.innerWidth property is read-only; it has no default value.
* window.innerWidth property stores an integer representing a number of pixels.
* </p>
* <h2> <span> Notes </span></h2>
* <p>The innerWidth property does not include the sidebar. So when the sidebar is expanded, the innerWidth property value diminishes.
* </p><p>The innerWidth property will be supported in any window object like a window, a frame, a frameset or a secondary window.
* </p>
* <h3> <span> Example </span></h3>
* <p><span>Assuming a frameset</span>
* <span>---------------------</span>
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var intFrameWidth = window.innerWidth; // or
* var intFrameWidth = self.innerWidth; / * will return the width of the
* frame viewport within the frameset * /
* var intFramesetWidth = parent.innerWidth; / * will return the width of
* the viewport of the closest frameset * /
* var intOuterFramesetWidth = top.innerWidth; / * will return the width
* of the viewport of the outermost frameset * /
* </pre>
* <p>xxx To do: link to an interactive demo here xxx
* </p><p>See also <a href="http://developer.mozilla.org/en/docs/window.outerWidth" shape="rect" title="window.outerWidth">window.outerWidth</a>, <a href="http://developer.mozilla.org/en/docs/window.innerHeight" shape="rect" title="window.innerHeight">window.innerHeight</a> and <a href="http://developer.mozilla.org/en/docs/window.outerHeight" shape="rect" title="window.outerHeight">window.outerHeight</a>.
* </p>
* <h2> <span> Standards information </span></h2>
* <p>DOM Level 0. Not part of any <abbr title="World Wide Web Consortium">W3C</abbr> technical specification or recommendation.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
innerWidth: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the distance in pixels from the left side of the main screen to the left side of the current screen.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>left</i> = <i>window</i>.screen.left;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>See also <a href="window.screen.top" shape="rect" title="DOM:window.screen.top">window.screen.top</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
left: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the number of frames (either <code>frame</code> or <code>iframe</code> elements) in the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>framesCount</i> = window.length;
* </pre>
* <ul><li> <code>framesCount</code> is the number of frames.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (window.length)
* // this is a document with subframes
* </pre>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
length: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a <code>Location</code> object, which contains information about the URL of the document and provides methods for changing that URL. You can also assign to this property to load another URL.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>locationObj</i> = window.location;
* window.location = <i>newLocation</i>;
* </pre>
* <p>where
* </p>
* <ul><li> <i>locationObj</i> is an object of type <code>Location</code>, providing information about the current URL and methods to change it. Its properties and methods are described below.
* </li><li> <i>newLocation</i> is a <code>Location</code> object or a string, specifying the URL to navigate to.
* </li></ul>
* <h2> <span> <code>Location</code> object </span></h2>
* <p><code>Location</code> objects have a <code>toString</code> method returning the current URL. You can also assign a string to <code>document.location</code>. This means that you can work with <code>document.location</code> as if it were a string in most cases. Sometimes, for example when you need to call a <a href="http://developer.mozilla.org/en/docs/String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a> method on it, you have to explicitly call <code>toString</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">alert(document.location.toString().charAt(17))
* </pre>
* <h3> <span> Properties </span></h3>
* <p>All of the following properties are strings. You can read them to get information about the current URL or set them to navigate to another URL.
* </p><p>The "Example" column contains the values of the properties of the following URL:
* </p>
* <ul><li>http://www.google.com:80/search?q=devmo#test
* </li></ul>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Property</th>
* <th colspan="1" rowspan="1">Description</th>
* <th colspan="1" rowspan="1">Example</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>hash</code></td>
* <td colspan="1" rowspan="1">the part of the URL that follows the # symbol.</td>
* <td colspan="1" rowspan="1">#test</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>host</code></td>
* <td colspan="1" rowspan="1">the host name and port number.</td>
* <td colspan="1" rowspan="1">www.google.com:80</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>hostname</code></td>
* <td colspan="1" rowspan="1">the host name (without the port number).</td>
* <td colspan="1" rowspan="1">www.google.com</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>href</code></td>
* <td colspan="1" rowspan="1">the entire URL.</td>
* <td colspan="1" rowspan="1">http://www.google.com:80/search?q=devmo#test</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>pathname</code></td>
* <td colspan="1" rowspan="1">the path (relative to the host).</td>
* <td colspan="1" rowspan="1">/search</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>port</code></td>
* <td colspan="1" rowspan="1">the port number of the URL.</td>
* <td colspan="1" rowspan="1">80</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>protocol</code></td>
* <td colspan="1" rowspan="1">the protocol of the URL.</td>
* <td colspan="1" rowspan="1">http:</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>search</code></td>
* <td colspan="1" rowspan="1">the part of the URL that follows the ? symbol, including the ? symbol.</td>
* <td colspan="1" rowspan="1">?q=devmo</td>
* </tr>
* </table>
* <p>If the hash part of the URL contains encoded characters (see <a href="http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Functions:encodeURIComponent" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:encodeURIComponent">Core_JavaScript_1.5_Reference:Global_Functions:encodeURIComponent</a>), <code>hash</code> returns the decoded URL part. This is a <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=378962" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=378962">bug</a> in Firefox. <code>href</code>, <code>search</code> and <code>pathname</code> return the correct, encoded URL parts.
* For example:
* </p>
* <ul><li>http://www.google.com/search?q=Fire%20fox#Fire%20fox
* </li></ul>
* <p>results in:
* </p>
* <ul><li>hash=#Fire fox
* </li><li>search=?q=Fire%20fox
* </li></ul>
* <h3> <span> Methods </span></h3>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Method</th>
* <th colspan="1" rowspan="1">Description</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>assign(url)</code></td>
* <td colspan="1" rowspan="1">Load the document at the provided URL.</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>reload(forceget)</code></td>
* <td colspan="1" rowspan="1">Reload the document from the current URL. <code>forceget</code> is a boolean, which, when it is <code>true</code>, causes the page to always be reloaded from the server. If it is <code>false</code> or not specified, the browser may reload the page from its cache.</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>replace(url)</code></td>
* <td colspan="1" rowspan="1">Replace the current document with the one at the provided URL. The difference from the <code>assign()</code> method is that after using <code>replace()</code> the current page will not be saved in session history, meaning the user won't be able to use the Back button to navigate to it.</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>toString()</code></td>
* <td colspan="1" rowspan="1">Returns the string representation of the <code>Location</code> object's URL. See the <a href="http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">JavaScript reference</a> for details.</td>
* </tr>
* </table>
* <h2> <span> Example </span></h2>
* <p>Whenever a property of the location object is modified, a document will be loaded using the URL as if <code>window.location.assign()</code> had been called with the modified URL.
* </p><p>Replace the current document with the one at the given URL:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function goMoz() {
* window.location = "http://www.mozilla.org";
* }
* 
* // in html: &lt;button onclick="goMoz();"&gt;Mozilla&lt;/button&gt;
* 
* </pre>
* <p>
* Display the properties of the current URL in an alert dialog:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function showLoc()
* {
* var x = window.location;
* var t = ['Property - Typeof - Value',
* 'window.location - ' + (typeof x) + ' - ' + x ];
* for (var prop in x){
* t.push(prop + ' - ' + (typeof x[prop]) + ' - ' +  (x[prop] || 'n/a'));
* }
* alert(t.join('\n'));
* }
* 
* // in html: &lt;button onclick="showLoc();"&gt;Show location properties&lt;/button&gt;
* 
* </pre>
* <p>
* Send a string of data to the server by modifying the <code>search</code> property:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function sendData(dat)
* {
* window.location.search = dat;
* }
* 
* // in html: &lt;button onclick="sendData('Some data');"&gt;Send data&lt;/button&gt;
* 
* </pre>
* <p>The current URL with "?Some%20data" appended is sent to the server (if no action is taken by the server, the current document is reloaded with the modified search string).
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
location: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the <b>locationbar</b> object, whose visibility can be toggled in the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>objRef</i> = window.locationbar
* </pre>
* <h2> <span>Example </span></h2>
* <p>The following complete HTML example shows way that the visible property of the various "bar" objects is used, and also the change to the privileges necessary to write to the visible property of any of the bars on an existing window.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;Various DOM Tests&lt;/title&gt;
* &lt;script&gt;
* // changing bar states on the existing window
* netscape.security.PrivilegeManager.
* enablePrivilege("UniversalBrowserWrite");
* window.locationbar.visible=
*  !window.locationbar.visible;
* &lt;/script&gt;
* &lt;/head&gt;
* &lt;body&gt;
* &lt;p&gt;Various DOM Tests&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>When you load the example page above, the browser displays the following dialog:
* <a href="http://developer.mozilla.org/en/docs/index.php?title=Special:Upload&amp;wpDestFile=window.locationbar_example_dialog.png" shape="rect" title="Image:window.locationbar example dialog.png">Image:window.locationbar example dialog.png</a>
* To toggle the visibility of these bars, you must either sign your scripts or enable the appropriate privileges, as in the example above. Also be aware that dynamically updating the visibilty of the various toolbars can change the size of the window rather dramatically, and may affect the layout of your page.
* See also: <a href="http://developer.mozilla.org/en/docs/window.locationbar" shape="rect" title="window.locationbar">window.locationbar</a>, <a href="http://developer.mozilla.org/en/docs/window.menubar" shape="rect" title="window.menubar">window.menubar</a>, <a href="http://developer.mozilla.org/en/docs/window.personalbar" shape="rect" title="window.personalbar">window.personalbar</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollbars" shape="rect" title="window.scrollbars">window.scrollbars</a>, <a href="http://developer.mozilla.org/en/docs/window.statusbar" shape="rect" title="window.statusbar">window.statusbar</a>, <a href="http://developer.mozilla.org/en/docs/window.toolbar" shape="rect" title="window.toolbar">window.toolbar</a>
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
locationbar: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the <b>menubar</b> object, whose visibility can be toggled in the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>objRef</i> = window.menubar
* </pre>
* <h2> <span>Example </span></h2>
* <p>The following complete HTML example shows way that the visible property of the various "bar" objects is used, and also the change to the privileges necessary to write to the visible property of any of the bars on an existing window.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;Various DOM Tests&lt;/title&gt;
* &lt;script&gt;
* // changing bar states on the existing window
* netscape.security.PrivilegeManager.
* enablePrivilege("UniversalBrowserWrite");
* window.menubar.visible=!window.menubar.visible;
* &lt;/script&gt;
* &lt;/head&gt;
* &lt;body&gt;
* &lt;p&gt;Various DOM Tests&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>When you load the example page above, the browser displays the following dialog:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* Internet Security
* 
* A script from "file://localhost" is requesting enhanced
* abilities that are UNSAFE and could be used to
* compromise your machine or data:
* 
* Modify any open window
* 
* Allow these abilities only if you trust this source to be
* free of visuses or malicious programs.
* 
* [ ] Remember this decision
* 
* &lt;&lt; Deny &gt;&gt;   &lt; Allow &gt;
* </pre>
* <p>To toggle the visibility of these bars, you must either sign your scripts or enable the appropriate privileges, as in the example above. Also be aware that dynamically updating the visibilty of the various toolbars can change the size of the window rather dramatically, and may affect the layout of your page.
* See also: <a href="http://developer.mozilla.org/en/docs/window.locationbar" shape="rect" title="window.locationbar">window.locationbar</a>, <a href="http://developer.mozilla.org/en/docs/window.personalbar" shape="rect" title="window.personalbar">window.personalbar</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollbars" shape="rect" title="window.scrollbars">window.scrollbars</a>, <a href="http://developer.mozilla.org/en/docs/window.statusbar" shape="rect" title="window.statusbar">window.statusbar</a>, <a href="http://developer.mozilla.org/en/docs/window.toolbar" shape="rect" title="window.toolbar">window.toolbar</a>
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
menubar: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Moves the current window by a specified amount.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.moveBy(<i>deltaX</i>, <i>deltaY</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>deltaX</code> is the amount of pixels to move the window horizontally.
* </li><li><code>deltaY</code> is the amount of pixels to move the window vertically.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function budge() {
* moveBy(10, -10);
* }
* </pre>
* <h2> <span>Notes </span></h2>
* <p>You can use negative numbers as parameters for this function. This function makes a relative move while <a href="http://developer.mozilla.org/en/docs/window.moveTo" shape="rect" title="window.moveTo">window.moveTo</a> makes an absolute move.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
moveBy: function(deltaX, deltaY) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Moves the window to the specified coordinates.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.moveTo(<i>x</i>, <i>y</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>x</code> is the horizontal coordinate to be moved to.
* </li><li><code>y</code> is the vertical coordinate to be moved to.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function origin() {
* // moves to top left corner of screen
* window.moveTo(0, 0)
* }
* </pre>
* <h2> <span>Notes </span></h2>
* <p>This function moves the window absolutely while <a href="http://developer.mozilla.org/en/docs/window.moveBy" shape="rect" title="window.moveBy">window.moveBy</a> moves the window relative to its current location.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
moveTo: function(x, y) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Gets/sets the name of the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>string</i> = window.name
* window.name = <i>string</i>
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.name = "lab_view";
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The name of the window is used primarily for setting targets for hyperlinks and forms. Windows do not need to have names.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
name: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a reference to the navigator object, which can be queried for information about the application running the script.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">alert("You're using " + navigator.appName);
* </pre>
* <h2> <span> Properties </span></h2>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.appCodeName" shape="rect" title="navigator.appCodeName">navigator.appCodeName</a> </dt><dd> Returns the internal "code" name of the current browser.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.appName" shape="rect" title="navigator.appName">navigator.appName</a> </dt><dd> Returns the official name of the browser.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.appVersion" shape="rect" title="navigator.appVersion">navigator.appVersion</a> </dt><dd> Returns the version of the browser as a string.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.buildID" shape="rect" title="navigator.buildID">navigator.buildID</a> </dt><dd> Returns the build identifier of the browser (e.g. "2006090803")
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.cookieEnabled" shape="rect" title="navigator.cookieEnabled">navigator.cookieEnabled</a> </dt><dd> Returns a boolean indicating whether cookies are enabled in the browser or not.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.language" shape="rect" title="navigator.language">navigator.language</a> </dt><dd> Returns a string representing the language version of the browser.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.mimeTypes" shape="rect" title="navigator.mimeTypes">navigator.mimeTypes</a> </dt><dd> Returns a list of the MIME types supported by the browser.
* </dd><dt style="font-weight:bold"> <a href="window.navigator.onLine" shape="rect" title="DOM:window.navigator.onLine">navigator.onLine</a> </dt><dd> Returns a boolean indicating whether the browser is working online.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.oscpu" shape="rect" title="navigator.oscpu">navigator.oscpu</a> </dt><dd> Returns a string that represents the current operating system.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.platform" shape="rect" title="navigator.platform">navigator.platform</a> </dt><dd> Returns a string representing the platform of the browser.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.plugins" shape="rect" title="navigator.plugins">navigator.plugins</a> </dt><dd> Returns an array of the plugins installed in the browser.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.product" shape="rect" title="navigator.product">navigator.product</a> </dt><dd> Returns the product name of the current browser. (e.g. "Gecko")
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.productSub" shape="rect" title="navigator.productSub">navigator.productSub</a> </dt><dd> Returns the build number of the current browser (e.g. "20060909")
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.navigator.securityPolicy&amp;action=edit" shape="rect" title="DOM:window.navigator.securityPolicy">navigator.securityPolicy</a> </dt><dd> Returns an empty string.  In Netscape 4.7x, returns "US &amp; CA domestic policy" or "Export policy".
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.userAgent" shape="rect" title="navigator.userAgent">navigator.userAgent</a> </dt><dd> Returns the user agent string for the current browser.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.vendor" shape="rect" title="navigator.vendor">navigator.vendor</a> </dt><dd> Returns the vendor name of the current browser (e.g. "Netscape6")
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.vendorSub" shape="rect" title="navigator.vendorSub">navigator.vendorSub</a> </dt><dd> Returns the vendor version number (e.g. "6.1")
* </dd></dl>
* <h2> <span> Methods </span></h2>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/navigator.javaEnabled" shape="rect" title="navigator.javaEnabled">navigator.javaEnabled</a> </dt><dd> Indicates whether the host browser is Java-enabled or not.
* </dd><dt style="font-weight:bold"> <a href="window.navigator.isLocallyAvailable" shape="rect" title="DOM:window.navigator.isLocallyAvailable">navigator.isLocallyAvailable</a> </dt><dd> Lets code check to see if the document at a given URI is available without using the network.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.navigator.preference&amp;action=edit" shape="rect" title="DOM:window.navigator.preference">navigator.preference</a> </dt><dd> Sets a user preference. This method is <a href="http://www.faqts.com/knowledge_base/view.phtml/aid/1608/fid/125/lang/en" rel="nofollow" shape="rect" title="http://www.faqts.com/knowledge_base/view.phtml/aid/1608/fid/125/lang/en">only available to privileged code</a>, and you should use XPCOM <a href="http://developer.mozilla.org/en/docs/Preferences_API" shape="rect" title="Preferences API">Preferences API</a> instead.
* </dd><dt style="font-weight:bold"> <a href="window.navigator.registerContentHandler" shape="rect" title="DOM:window.navigator.registerContentHandler">navigator.registerContentHandler</a> </dt><dd> Allows web sites to register themselves as a possible handler for a given MIME type.
* </dd><dt style="font-weight:bold"> <a href="window.navigator.registerProtocolHandler" shape="rect" title="DOM:window.navigator.registerProtocolHandler">navigator.registerProtocolHandler</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span> </dt><dd> Allows web sites to register themselves as a possible handler for a given protocol.
* </dd><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.navigator.taintEnabled&amp;action=edit" shape="rect" title="DOM:window.navigator.taintEnabled">navigator.taintEnabled</a> <span style="border: 1px solid #FF9999; background-color: #FFDBDB; font-size: 9px; vertical-align: text-top;">Obsolete</span> </dt><dd> Returns false. JavaScript taint/untaint functions removed in JavaScript 1.2<a href="http://devedge-temp.mozilla.org/library/manuals/2000/javascript/1.3/reference/nav.html#1194117" rel="nofollow" shape="rect" title="http://devedge-temp.mozilla.org/library/manuals/2000/javascript/1.3/reference/nav.html#1194117">[1]</a>
* </dd></dl>
* <h2> <span> See also </span></h2>
* <p><a href="DOM_Client_Object_Cross-Reference:navigator" shape="rect" title="DOM Client Object Cross-Reference:navigator">DOM Client Object Cross-Reference:navigator</a>
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
navigator: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>An event handler for abort events sent to the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onabort = <i>funcRef</i>
* </pre>
* <p><code>funcRef</code> is a reference to a function.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onabort = function() {
* alert("Load aborted.");
* }
* </pre>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:NeedsContent" shape="rect" title="Category:NeedsContent">NeedsContent</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onabort: undefined,
onbeforeunload: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>The <code>onblur</code> property can be used to set the blur handler on the window, which is triggered when the window loses focus.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onblur = funcRef;
* </pre>
* <ul><li> <code>funcRef</code> is a reference to the function to be executed.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onblur test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onblur = blurText;
* 
* function blurText()
* {
* alert("blur event detected!");
* // remove the event to stop an infinite loop!
* window.onblur = '';
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;click on another window
* to fire the blur event for this browser window&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onblur: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for change events sent to the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onchange = funcRef;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onchange = resetThatServerThing
* </pre>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onchange: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for click events sent to the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onclick = <i>funcRef</i>;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onclick = doPopup;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onclick test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onclick = clickPage;
* 
* function clickPage()
* {
* alert("click event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;click and release the LH mouse button on this page.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The click event is raised when the user clicks on the window.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onclick: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for close events sent to the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onclose = funcRef;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onclose = resetThatServerThing
* </pre>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onclose: undefined,
oncontextmenu: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>An event handler for drag and drop events sent to the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><s>window.ondragdrop = funcRef;</s>
* window.addEventListener("dragdrop", funcRef, useCapturing);
* </pre>
* <dl><dt style="font-weight:bold"> funcRef </dt><dd> the event handler function to be registered.
* </dd></dl>
* <p>The <code>window.ondragdrop</code> property and the <code>ondragdrop</code> attribute are not implemented in <a href="http://developer.mozilla.org/en/docs/Gecko" shape="rect" title="Gecko">Gecko</a> (<a href="https://bugzilla.mozilla.org/show_bug.cgi?id=112288" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=112288">bug 112288</a>), you have to use <code>addEventListener</code>. See <a href="element.addEventListener" shape="rect" title="DOM:element.addEventListener">addEventListener</a> for details.
* </p>
* <h2> <span> Example </span></h2>
* <h3> <span> Fire an alert on dragdrop </span></h3>
* <p>In this example, an event listener is added to the window (the event target). If, from an external source, a tab, a link, marked text or a file is dragged and dropped onto this window, the alert is fired. Note how <code>event.stopPropagation();</code> prevents the browser from loading the dropped tab, link or file.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;&lt;title&gt;dragdroptest&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.addEventListener("dragdrop", testfunc, false);
* 
* function testfunc(event) {
* alert("dragdrop!");
* event.stopPropagation();
* }
* &lt;/script&gt;
* 
* &lt;/head&gt;
* &lt;body&gt;
* I am bodytext
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The event.data array available in NN4 seems to be unavailable in <a href="http://developer.mozilla.org/en/docs/Gecko" shape="rect" title="Gecko">Gecko</a>.
* See also <a href="http://forums.mozillazine.org/viewtopic.php?p=863806" rel="nofollow" shape="rect" title="http://forums.mozillazine.org/viewtopic.php?p=863806">[1]</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
ondragdrop: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for error events sent to the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onerror = funcRef;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function. When the function returns <code>true</code>, this prevents the firing of the default event handler. Function parameters:
* <ul><li> Error message (string)
* </li><li> Url where error was raised (string)
* </li><li> Error code (number)
* </li></ul>
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onerror = null;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The error event is raised when an error occurs in the script. The example above prevents error dialogs from displaying-which is the window's normal behavior-by overriding the default event handler for error events that go to the window.
* </p><p>When using the inline html markup (&lt;body onerror="alert('an error occurred')&gt;...), the arguments are not named. They can be referenced by <code>arguments[0]</code> through <code>arguments[2]</code>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onerror: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for focus events sent to the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onfocus = funcRef;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onfocus = startTimer;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The focus event is raised when the user sets focus on the current window.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onfocus: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the keydown event on the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onkeydown = funcRef;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onkeydown = doFunc;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onkeydown test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onkeydown = keydown;
* 
* function keydown()
* {
* alert("keydown event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;press and hold down any key to fire the keydown event.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The keydown event is raised when the user presses any key.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onkeydown: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the keypress event on the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onkeypress = funcRef;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onkeypress = doFunc;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onkeypress test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onkeypress = keypress;
* 
* function keypress()
* {
* alert("keypress event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;press and release any key to fire the keypress event.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The keypress event is raised when the user presses and releases any key on the keyboard.
* </p>
* <ul><li> Under FF 1.0.5.4 this event fires before the keyup event is detected, i.e. it appears to work exactly like onkeydown.
* </li></ul>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onkeypress: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the keyup event on the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onkeyup = <i>funcRef</i>;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onkeyup = doFunc;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onkeyup test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onkeyup = keyup;
* 
* function keyup()
* {
* alert("keyup event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;press and hold any key, then release it to fire the keyup event.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The keyup event is raised when a key that has been pressed is released.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onkeyup: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>An event handler for the load event of a <a href="window" shape="rect" title="DOM:window">window</a>.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onload = <i>funcRef</i>;
* </pre>
* <ul><li> <code>funcRef</code> is the handler function.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onload = function() {
* init();
* doSomethingElse();
* };
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onload test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onload = load;
* 
* function load()
* {
* alert("load event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;The load event fires when the document has finished loading!&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The load event fires at the end of the document loading process. At this point, all of the objects in the document are in the DOM, and all the images and sub-frames have finished loading.
* </p><p>There is also <a href="http://developer.mozilla.org/en/docs/Gecko-Specific_DOM_Events" shape="rect" title="Gecko-Specific DOM Events">Gecko-Specific DOM Events</a> like <code>DOMContentLoaded</code> and <code>DOMFrameContentLoaded</code> events (which can be handled using <code>addEventListener</code>) which are fired after the DOM for the page has been constructed, but don't wait for other resources to finish loading.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onload: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the mousedown event on the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onmousedown = <i>funcRef</i>;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onmousedown = doFunc;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onmousedown test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onmousedown = mousedown;
* 
* function mousedown()
* {
* alert("mousedown event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;click and hold down the LH mouse button&lt;br /&gt;
* on the page to fire the mousedown event.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The mousedown event is raised when the user clicks the left mouse button anywhere in the document.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmousedown: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the mousemove event on the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onmousemove = <i>funcRef</i>;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onmousemove = doFunc;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onmousemove test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onmousemove = mousemoved;
* 
* function mousemoved()
* {
* alert("mousemove event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;move the mouse pointer anywhere on the page&lt;br /&gt;
* to fire the mousemove event.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The mousemove event fires when the user moves the mouse pointer by 1 pixel or more in any direction in the browser window.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmousemove: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the mouseout event on the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onmouseout = funcRef;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onmouseout = doFunc;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onmouseout test&lt;/title&gt;
* 
* &lt;style type="text/css"&gt;
* body { border: 1px solid blue; }
* .my_box { border: 1px solid red; }
* p { border: 1px solid green; }
* &lt;/style&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onmouseout = mouseout;
* 
* function mouseout()
* {
* alert("mouseout event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;div class="my_box"&gt;
* &lt;p&gt;move the mouse pointer away from the element it is on&lt;br /&gt;
* to fire the mouseout event.&lt;/p&gt;
* &lt;/div&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The mouseout event is raised when the mouse leaves the area of the specified element (in this case the window itself).
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmouseout: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the mouseover event on the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onmouseover = <i>funcRef</i>;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onmouseover = doFunc;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onmouseover test&lt;/title&gt;
* 
* &lt;style type="text/css"&gt;
* .my_box { border: 1px solid red; }
* &lt;/style&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onmouseover = mouseover;
* 
* function mouseover()
* {
* alert("mouseover event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;div class="my_box"&gt;
* &lt;p&gt;move the mouse pointer to this div element,&lt;br /&gt;
* or onto the status bar and back into the main window&lt; br /&gt;
* to fire the mouseover event.&lt;/p&gt;
* &lt;/div&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The mouseover event is raised when the mouse pointer moves over the current element (in this case the window itself).
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmouseover: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the mouseup event on the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onmouseup = <i>funcRef</i>;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onmouseup = doFunc;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onmouseup test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onmouseup = mouseup;
* 
* function mouseup()
* {
* alert("mouseup event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;click on the page with the LH mouse button, and hold down for a few
* seconds, then release the button. The mouseup event fires when you
* release the mouse button.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The mouseup event is raised when the user unclicks the left mouse button anywhere in the document.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmouseup: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>An event handler for the paint event on the window. <b>Not working in <a href="http://developer.mozilla.org/en/docs/Gecko" shape="rect" title="Gecko">Gecko</a>-based applications currently, see Notes section!</b>
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onpaint = <i>funcRef</i>;
* </pre>
* <ul><li> <code>funcRef</code> is a handler function.
* </li></ul>
* <h2> <span> Notes </span></h2>
* <p><code>onpaint</code> doesn't work currently, and it is the question whether this event is going to work at all, see <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=239074" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=239074">bug 239074</a>.
* </p><p>The paint event is raised when the window is rendered. This event occurs after the load event for a window, and reoccurs each time the window needs to be rerendered, as when another window obscures it and is then cleared away.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onpaint: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the reset event on the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onreset = funcRef;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">&lt;html&gt;
* &lt;script&gt;
* function reg() {
* window.captureEvents(Event.RESET);
* window.onreset = hit;
* }
* 
* function hit() {
* alert('hit');
* }
* &lt;/script&gt;
* 
* &lt;body onload="reg();"&gt;
* &lt;form&gt;
* &lt;input type="reset" value="reset" /&gt;
* &lt;/form&gt;
* &lt;div id="d"&gt; &lt;/div&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The reset event is raised when the user clicks a reset button in a form (<code>&lt;input type="reset"/&gt;</code>).
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onreset: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the resize event on the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onresize = <i>funcRef</i>;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.onresize = doFunc;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onresize test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onresize = resize;
* 
* function resize()
* {
* alert("resize event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;Resize the browser window to fire the resize event.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The resize event is fired after the window has been resized.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onresize: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Specifies the function to be called when the window is scrolled.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onscroll = <i>funcRef</i>;
* </pre>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onscroll = function (e) {
* // called when the window is scrolled.
* }
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onscroll test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onscroll = scroll;
* 
* function scroll()
* {
* alert("scroll event detected! "+window.pageXOffset+" "+window.pageYOffset);
* // note: you can use window.innerWidth and window.innerHeight to access the width and height of the viewing area
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;Resize the window&lt;/p&gt;
* &lt;p&gt;to a very small size,&lt;/p&gt;
* &lt;p&gt;and use the scrollbars&lt;/p&gt;
* &lt;p&gt;to move around the page content&lt;/p&gt;
* &lt;p&gt;in the window.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p><a href="https://bugzilla.mozilla.org/show_bug.cgi?id=189308" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=189308">bug 189308</a>, in older versions of Gecko caused onscroll to be fired only when dragging the scroll bar, not when using cursor keys or mousewheel. This was fixed in Gecko 1.8/Firefox 1.5.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onscroll: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>An event handler for the select event on the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onselect = <i>funcRef</i>;
* </pre>
* <ul><li> <code>funcRef</code> is a function.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onselect test&lt;/title&gt;
* 
* &lt;style type="text/css"&gt;
* .text1 { border: 2px solid red; }
* &lt;/style&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onselect = selectText;
* 
* function selectText()
* {
* alert("select event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;textarea class="text1" cols="30" rows="3"&gt;
* Highlight some of this text
* with the mouse pointer
* to fire the onselect event.
* &lt;/textarea&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The select event only fires when text inside a text input or textarea is selected. The event is fired <i>after</i> the text has been selected.
* </p>
* <h2> <span> Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onselect: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>An event handler for the submit event on the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onsubmit = <i>funcRef</i>;
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">&lt;html&gt;
* &lt;script&gt;
* function reg() {
* window.captureEvents(Event.SUBMIT);
* window.onsubmit = hit;
* }
* 
* function hit() {
* alert('hit');
* }
* &lt;/script&gt;
* 
* &lt;body onload="reg();"&gt;
* &lt;form&gt;
* &lt;input type="submit" value="submit" /&gt;
* &lt;/form&gt;
* &lt;div id="d"&gt; &lt;/div&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The submit event is raised when the user clicks a submit button in a form (<code>&lt;input type="submit"/&gt;</code>).
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onsubmit: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>An event handler for the unload event on the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.onunload = <i>funcRef</i>;
* </pre>
* <ul><li> <code>funcRef</code> is a reference to a function.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* 
* &lt;title&gt;onunload test&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* window.onunload = unloadPage;
* 
* function unloadPage()
* {
* alert("unload event detected!");
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p&gt;Reload a new page into the browser&lt;br /&gt;
* to fire the unload event for this page.&lt;/p&gt;
* &lt;p&gt;You can also use the back or forward buttons&lt;br /&gt;
* to load a new page and fire this event.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The unload event is raised when the document is unloaded.
* </p><p>Note that using this event handler in your page prevents Firefox 1.5 from caching the page in the in-memory bfcache. See <a href="http://developer.mozilla.org/en/docs/Using_Firefox_1.5_caching" shape="rect" title="Using Firefox 1.5 caching">Using Firefox 1.5 caching</a> for details.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onunload: undefined,
/**
* <h2> <span> Definition </span></h2>
* <p>Creates a new secondary browser window and loads the referenced resource.
* </p>
* <h2> <span> Syntax </span></h2>
* <p><var>WindowObjectReference</var> = window.open(<var>strUrl</var>, <var>strWindowName</var> [, <var>strWindowFeatures</var>]);
* </p>
* <h2> <span> Return value and parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>WindowObjectReference</code> </dt><dd> This is the reference pointing to the newly created browser window. This reference is the return value of the open() method; it will be <code>null</code> if for some reasons the call did not succeed to open the window. A global variable is best used to store such reference. You can then, for example, use it to look for properties of the new window or access its methods, assuming that your main versus secondary window relationship complies with <a href="http://www.mozilla.org/projects/security/components/same-origin.html" rel="nofollow" shape="rect" title="http://www.mozilla.org/projects/security/components/same-origin.html">Same origin policy</a> security requirements.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>strUrl</code> </dt><dd> This is the URL to be loaded in the newly opened window. <var>strUrl</var> can be an HTML document on the web, it can be an image file or any type of file which is supported by the browser.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>strWindowName</code> </dt><dd> This is the string that just names the new window. Such string can be used to be the target of links and forms when the target attribute of an <code style="font-size: 1em;">&lt;a&gt;</code> element or of a <code style="font-size: 1em;">&lt;form&gt;</code> is specified. This string parameter should not contain any blank space. <var>strWindowName</var> does not specify the title of the new window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>strWindowFeatures</code> </dt><dd> Optional parameter. This parameter is the string which lists the requested window features (window functionalities and toolbars) of the new browser window. This string parameter must not contain any blank space. Each requested window feature must be separated by a comma inside the character string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The <code>open()</code> method creates a new secondary browser window, similar to choosing New Window from the File menu. The <var>strUrl</var> parameter specifies the URL to be fetched and loaded in the new window. If <var>strUrl</var> is an empty string, then a new blank, empty window (URL <code>about:blank</code> loaded) is created with the default toolbars of the main window.
* </p><p>Note that remote URLs won't load immediately. When <code>window.open()</code> returns, the window always contains <code>about:blank</code>. The actual fetching of the URL is deferred and starts after the current script block finishes executing. The window creation and the loading of the referenced resource are done asynchronously.
* </p>
* <h3> <span> Examples </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* var WindowObjectReference;
* / * Declaring a global variable which will store
* a reference to the new window to be created * /
* 
* function openRequestedPopup()
* {
* WindowObjectReference = window.open("http://www.cnn.com/",
* "CNN_WindowName",
* "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes");
* }
* &lt;/script&gt;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* var WindowObjectReference; // global variable
* 
* function openRequestedPopup()
* {
* WindowObjectReference = window.open("http://www.domainname.ext/path/ImageFile.png",
* "DescriptiveWindowName",
* "resizable=no,scrollbars=yes,status=no");
* }
* &lt;/script&gt;
* </pre>
* <p>If a window with the name <var>strWindowName</var> already exists, then, instead of opening a new window, <var>strUrl</var> is loaded into the existing window. In this case the return value of the method is the existing window and <var>strWindowFeatures</var> is ignored. Providing an empty string for <var>strUrl</var> is a way to get a reference to an open window by its name without changing the window's location. If you want to open a new window on every call of <code>window.open()</code>, you should use the special value <var>_blank</var> for <var>strWindowName</var>.
* </p><p><var>strWindowFeatures</var> is an optional string containing a comma-separated list of requested features of the new window. After a window is opened, you can not use JavaScript to change the window functionalities and the window toolbars.
* If <var>strWindowName</var> does not specify an existing window and if you do not supply the <var>strWindowFeatures</var> parameter (or if the <var>strWindowFeatures</var> parameter is an empty string), then the new secondary window will render the default toolbars of the main window.
* </p><p>If the <var>strWindowFeatures</var> parameter is used and if no size features are defined, then the new window dimensions will be the same as the dimensions of the most recently rendered window.
* </p><p>If the <var>strWindowFeatures</var> parameter is used and if no position features are defined, then the left and top coordinates of the new window dimension will be 22 pixels off from where the most recently rendered window was. An offset is universally implemented by browser manufacturers (it is 29 pixels in MSIE 6 SP2 with the default theme) and its purpose is to help users to notice new windows opening. If the most recently used window was maximized, then there is no 22 pixels offset: the new, secondary window will be maximized as well.
* </p><p><strong>If you define the <var>strWindowFeatures</var> parameter, then the features that are not listed, requested in the string will be disabled or removed</strong> (except <var>titlebar</var> and <var>close</var> which are by default <var>yes</var>).
* </p>
* <div>
* <p><strong>Tip</strong>: If you use the <var>strWindowFeatures</var> parameter, then only list the features you want to include in the new window, that you want to be enabled or rendered; the others (except <var>titlebar</var> and <var>close</var>) will be disabled, removed.
* </p>
* </div>
* <p style="border: 1px dotted green;"><a href="http://developer.mozilla.org/en/docs/Image:FirefoxChromeToolbarsDescription7a.gif" shape="rect" title="Firefox Chrome Toolbars Illustration"/></p>
* <h3> <span> Position and size features </span></h3>
* <p><a href="DOM:window.open#Note_on_position_and_dimension_error_correction" shape="rect" title="">Note on position and dimension error correction</a>
* </p>
* <div><a href="https://bugzilla.mozilla.org/show_bug.cgi?id=176320" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=176320">Bug 176320: Minimal innerWidth/innerHeight values for popup windows</a></div>
* <p><a href="window.open#Note_on_precedence" shape="rect" title="">Note on precedence</a>
* </p>
* <dl><dt style="font-weight:bold"> left </dt><dd> <span id="left">Specifies the distance</span> the new window is placed from the left side of the work area for applications of the user's operating system to the leftmost border (resizing handle) of the browser window. The new window can not be initially positioned offscreen.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Opera6.gif" shape="rect" title="Opera 6+"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> top </dt><dd> <span id="topS">Specifies the distance</span> the new window is placed from the top side of the work area for applications of the user's operating system to the topmost border (resizing handle) of the browser window. The new window can not be initially positioned offscreen.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Opera6.gif" shape="rect" title="Opera 6+"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> height </dt><dd> <span id="height">Specifies the height</span> of the content area, viewing area of the new secondary window in pixels. The height value includes the height of the horizontal scrollbar if present. The minimum required value is 100.
* </dd><dd> <a href="window.open#Note_on_outerHeight_versus_height" shape="rect" title="">Note on outerHeight versus height (or innerHeight)</a>
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Opera6.gif" shape="rect" title="Opera 6+"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> width </dt><dd> <span id="width">Specifies the width</span> of the content area, viewing area of the new secondary window in pixels. The width value includes the width of the vertical scrollbar if present. The width value does not include the sidebar if it is expanded. The minimum required value is 100.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Opera6.gif" shape="rect" title="Opera 6+"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> screenX </dt><dd> Deprecated. Same as <a href="window.open#left" shape="rect" title="">left</a> but only supported by Netscape and Mozilla-based browsers.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold">screenY </dt><dd> Deprecated. Same as <a href="window.open#topS" shape="rect" title="">top</a> but only supported by Netscape and Mozilla-based browsers.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> centerscreen </dt><dd> Centers the window in relation to its parent's size and position. Requires chrome=yes.
* </dd></dl>
* <dl><dt style="font-weight:bold"> outerHeight </dt><dd> Specifies the height of the whole browser window in pixels. This outerHeight value includes any/all present toolbar, window horizontal scrollbar (if present) and top and bottom window resizing borders. Minimal required value is 100.
* </dd><dd> <strong>Note</strong>: since titlebar is always rendered, then requesting outerHeight=100 will make the innerHeight of the browser window under the minimal 100 pixels.
* </dd><dd> <a href="window.open#Note_on_outerHeight_versus_height" shape="rect" title="">Note on outerHeight versus height (or innerHeight)</a>
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> outerWidth </dt><dd> Specifies the width of the whole browser window in pixels. This outerWidth value includes the window vertical scrollbar (if present) and left and right window resizing borders.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> innerHeight </dt><dd> Same as <a href="window.open#height" shape="rect" title="">height</a> but only supported by Netscape and Mozilla-based browsers. Specifies the height of the content area, viewing area of the new secondary window in pixels. The <var>innerHeight</var> value includes the height of the horizontal scrollbar if present. Minimal required value is 100.
* </dd><dd> <a href="window.open#Note_on_outerHeight_versus_height" shape="rect" title="">Note on outerHeight versus height (or innerHeight)</a>
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> innerWidth </dt><dd> Same as <a href="window.open#width" shape="rect" title="">width</a> but only supported by Netscape and Mozilla-based browsers. Specifies the width of the content area, viewing area of the new secondary window in pixels. The innerWidth value includes the width of the vertical scrollbar if present. The innerWidth value does not include the sidebar if it is expanded. Minimal required value is 100.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <h3> <span> Toolbar and chrome features </span></h3>
* <dl><dt style="font-weight:bold"> menubar </dt><dd> If this feature is set to yes, then the new secondary window renders the menubar.
* </dd><dd> Mozilla and Firefox users can force new windows to always render the menubar by setting <code>dom.disable_window_open_feature.menubar</code> to <var>true</var> in <kbd>about:config</kbd> or in their <a href="http://www.mozilla.org/support/firefox/edit#user" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#user">user.js file</a>.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> toolbar </dt><dd> If this feature is set to <var>yes</var>, then the new secondary window renders the Navigation Toolbar (Back, Forward, Reload, Stop buttons). In addition to the Navigation Toolbar, Mozilla-based browsers will render the Tab Bar if it is visible, present in the parent window.
* </dd><dd> Mozilla and Firefox users can force new windows to always render the Navigation Toolbar by setting <code>dom.disable_window_open_feature.toolbar</code>to <var>true</var> in <kbd>about:config</kbd> or in their <a href="http://www.mozilla.org/support/firefox/edit#user" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#user">user.js file</a>.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> location </dt><dd> If this feature is set to <var>yes</var>, then the new secondary window renders the Location bar in Mozilla-based browsers. MSIE 5+ and Opera 7.x renders the Address Bar.
* </dd><dd> Mozilla and Firefox users can force new windows to always render the location bar by setting <code>dom.disable_window_open_feature.location</code> to <var>true</var> in <kbd>about:config</kbd> or in their <a href="http://www.mozilla.org/support/firefox/edit#user" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#user">user.js file</a>.
* </dd></dl>
* <p>MSIE 7 forces the presence of the Address Bar by default: "We think the address bar is also important for users <b>to see in pop-up windows</b>. A missing address bar creates a chance for a fraudster to forge an address of their own. To help thwart that, <b>IE7 will show the address bar on all internet windows to help users see where they are</b>." coming from <a href="http://blogs.msdn.com/ie/archive/2005/11/21.aspx" rel="nofollow" shape="rect" title="http://blogs.msdn.com/ie/archive/2005/11/21.aspx">Microsoft Internet Explorer Blog, Better Website Identification</a>
* </p><p>Mozilla.org also intends to soon force the presence of the Location Bar in Firefox 3:
* </p>
* <div><a href="https://bugzilla.mozilla.org/show_bug.cgi?id=337344" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=337344">Bug 337344: Change default dom.disable_window_open_feature.location to true</a></div>
* <dl><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Opera6.gif" shape="rect" title="Opera 6+"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> directories </dt><dd> If this feature is set to <var>yes</var>, then the new secondary window renders the Personal Toolbar in Netscape 6.x, Netscape 7.x and Mozilla browser. It renders the Bookmarks Toolbar in Firefox 1.x and, in MSIE 5+, it renders the Links bar. In addition to the Personal Toolbar, Mozilla browser will render the Site Navigation Bar if such toolbar is visible, present in the parent window.
* </dd><dd> Mozilla and Firefox users can force new windows to always render the Personal Toolbar/Bookmarks toolbar by setting <code>dom.disable_window_open_feature.directories</code> to <var>true</var> in <kbd>about:config</kbd> or in their <a href="http://www.mozilla.org/support/firefox/edit#user" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#user">user.js file</a>.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> personalbar </dt><dd> Same as <var>directories</var> but only supported by Netscape and Mozilla-based browsers.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> status </dt><dd> If this feature is set to <var>yes</var>, then the new secondary window has a status bar. Users can force the rendering of status bar in all Mozilla-based browsers, in MSIE 6 SP2 (<a href="window.open#Note_on_security_issues_of_the_status_bar_presence" shape="rect" title="">Note on status bar in XP SP2</a>) and in Opera 6+. The default preference setting in recent Mozilla-based browser releases and in Firefox 1.0 is to force the presence of the status bar.
* </dd><dd> <a href="window.open#Note_on_status_bar" shape="rect" title="">Note on status bar</a>
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <h3> <span> Window functionality features </span></h3>
* <dl><dt style="font-weight:bold"> resizable </dt><dd> If this feature is set to <var>yes</var>, the new secondary window will be resizable.
* </dd><dd> <strong>Note</strong>: Starting with version 1.4, Mozilla-based browsers have a window resizing grippy at the right end of the status bar, this ensures that users can resize the browser window even if the web author requested this secondary window to be non-resizable. In such case, the maximize/restore icon in the window's titlebar will be disabled and the window's borders won't allow resizing but the window will still be resizable via that grippy in the status bar.
* </dd></dl>
* <p>It is expected to see secondary window always resizable starting from Firefox 3 (<a href="https://bugzilla.mozilla.org/show_bug.cgi?id=177838" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=177838">bug 177838</a>)
* </p>
* <div>
* <p><strong>Tip</strong>: For accessibility reasons, it is strongly recommended to set this feature always to <var>yes</var>.
* </p>
* </div>
* <dl><dd> Mozilla and Firefox users can force new windows to be easily resizable by setting <code>dom.disable_window_open_feature.resizable</code> to <var>true</var> in <kbd>about:config</kbd> or in their <a href="http://www.mozilla.org/support/firefox/edit#user" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#user">user.js file</a>.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> scrollbars </dt><dd> If this feature is set to <var>yes</var>, the new secondary window will show horizontal and/or vertical scrollbar(s) if the document doesn't fit into the window's viewport.
* </dd></dl>
* <div>
* <p><strong>Tip</strong>: For accessibility reasons, it is strongly encouraged to set this feature always to <var>yes</var>.
* </p>
* </div>
* <dl><dd> Mozilla and Firefox users can force this option to be always enabled for new windows by setting <code>dom.disable_window_open_feature.scrollbars</code> to <var>true</var> in <kbd>about:config</kbd> or in their <a href="http://www.mozilla.org/support/firefox/edit#user" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#user">user.js file</a>.
* </dd><dd> <a href="window.open#Note_on_scrollbars" shape="rect" title="">Note on scrollbars</a>
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>, <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> dependent </dt><dd> If set to <var>yes</var>, the new window is said to be dependent of its parent window. A dependent window closes when its parent window closes. A dependent window is minimized on the Windows task bar only when its parent window is minimized. On Windows platforms, a dependent window does not show on the task bar. A dependent window also stays in front of the parent window.
* </dd><dd> Dependent windows are not implemented on MacOS X, this option will be ignored.
* </dd><dd> The dependent feature is currently under revision to be removed (<a href="https://bugzilla.mozilla.org/show_bug.cgi?id=214867" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=214867">bug 214867</a>)
* </dd></dl>
* <dl><dd> In MSIE 6, the nearest equivalent to this feature is the <code>showModelessDialog()</code> method.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> dialog </dt><dd> The <code>dialog</code> feature removes all icons (restore, minimize, maximize) from the window's titlebar, leaving only the close button. <span><a href="http://developer.mozilla.org/en/docs/Image:MenuSystemCommands.png" shape="rect" title="Firefox and its command system menu under Windows"/></span>Mozilla 1.2+ and Netscape 7.1 will render the other menu system commands (in FF 1.0 and in NS 7.0x, the command system menu is not identified with the Firefox/NS 7.0x icon on the left end of the titlebar: that's probably a bug. You can access the command system menu with a right-click on the titlebar). Dialog windows are windows which have no minimize system command icon and no maximize/restore down system command icon on the titlebar nor in correspondent menu item in the command system menu. They are said to be dialog because their normal, usual purpose is to only notify info and to be dismissed, closed. On Mac systems, dialog windows have a different window border and they may get turned into a sheet.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> minimizable </dt><dd> This setting can only apply to dialog windows; "minimizable" requires <code>dialog=yes</code>. If <code>minimizable</code> is set to <var>yes</var>, the new dialog window will have a minimize system command icon in the titlebar and it will be minimizable. Any non-dialog window is always minimizable and <code>minimizable=no</code> will be ignored.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> fullscreen </dt><dd> Do not use. Not implemented in Mozilla. There are no plans to implement this feature in Mozilla.
* </dd><dd> This feature no longer works in MSIE 6 SP2 the way it worked in MSIE 5.x. The Windows taskbar, as well as the titlebar and the status bar of the window are not visible, nor accessible when fullscreen is enabled in MSIE 5.x.
* </dd><dd> <code>fullscreen</code> always upsets users with large monitor screen or with dual monitor screen. Forcing <code>fullscreen</code> onto other users is also extremely unpopular and is considered an outright rude attempt to impose web author's viewing preferences onto users.
* </dd><dd> <a href="window.open#Note_on_fullscreen" shape="rect" title="">Note on fullscreen</a>
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:MSIE_ico.png" shape="rect" title="Internet Explorer 5+"/>
* </dd><dd> <code>fullscreen</code> does not really work in MSIE 6 SP2.
* </dd></dl>
* <h3> <span> Features requiring privileges </span></h3>
* <p>The following features require the <code>UniversalBrowserWrite</code> privilege, otherwise they will be ignored. Chrome scripts have this privilege automatically, others have to request it from the <a href="http://developer.mozilla.org/en/docs/index.php?title=PrivilegeManager&amp;action=edit" shape="rect" title="PrivilegeManager">PrivilegeManager</a>.
* </p>
* <dl><dt style="font-weight:bold"> chrome </dt><dd> <strong>Note</strong>: Starting with Mozilla 1.7/Firefox 0.9, this feature requires the <code>UniversalBrowserWrite</code> privilege (<a href="https://bugzilla.mozilla.org/show_bug.cgi?id=244965" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=244965">bug 244965</a>). Without this privilege, it is ignored.
* </dd><dd> If set to <var>yes</var>, the page is loaded as window's only content, without any of the browser's interface elements. There will be no context menu defined by default and none of the standard keyboard shortcuts will work. The page is supposed to provide a user interface of its own, usually this feature is used to open XUL documents (standard dialogs like the JavaScript Console are opened this way).
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> modal </dt><dd> <strong>Note</strong>: Starting with Mozilla 1.2.1, this feature requires the <code>UniversalBrowserWrite</code> privilege (<a href="https://bugzilla.mozilla.org/show_bug.cgi?id=180048" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=180048">bug 180048</a>). Without this privilege, it is ignored.
* </dd><dd> If set to <var>yes</var>, the new window is said to be modal. The user cannot return to the main window until the modal window is closed. A typical modal window is created by the <a href="window.alert" shape="rect" title="DOM:window.alert">alert() function</a>.
* </dd><dd> The exact behavior of modal windows depends on the platform and on the Mozilla release version.
* </dd><dd> The MSIE 6 equivalent to this feature is the <code>showModalDialog()</code> method.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> titlebar </dt><dd> By default, all new secondary windows have a titlebar. If set to <var>no</var>, this feature removes the titlebar from the new secondary window.
* </dd><dd> Mozilla and Firefox users can force new windows to always render the titlebar by setting <code>dom.disable_window_open_feature.titlebar</code> to <var>true</var> in <kbd>about:config</kbd> or in their <a href="http://www.mozilla.org/support/firefox/edit#user" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#user">user.js file</a>.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> alwaysRaised </dt><dd> If set to <var>yes</var>, the new window will always be displayed on top of other browser windows, regardless of whether it is active or not.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> alwaysLowered </dt><dd> If set to <var>yes</var>, the new created window floats below, under its own parent when the parent window is not minimized. alwaysLowered windows are often referred as pop-under windows. The alwaysLowered window can not be on top of the parent but the parent window can be minimized. In NS 6.x, the alwaysLowered window has no minimize system command icon and no restore/maximize system command.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <dl><dt style="font-weight:bold"> z-lock </dt><dd> Same as <code>alwaysLowered</code>.
* </dd></dl>
* <dl><dt style="font-weight:bold"> close </dt><dd> When set to <var>no</var>, this feature removes the system close command icon and system close menu item. It will only work for dialog windows (<code>dialog</code> feature set). <code>close=no</code> will override <code>minimizable=yes</code>.
* </dd><dd> Mozilla and Firefox users can force new windows to always have a close button by setting <code>dom.disable_window_open_feature.close</code> to <var>true</var> in <kbd>about:config</kbd> or in their <a href="http://www.mozilla.org/support/firefox/edit#user" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#user">user.js file</a>.
* </dd><dd> Supported in: <a href="http://developer.mozilla.org/en/docs/Image:ns6.gif" shape="rect" title="Netscape 6.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:NS7_ico4.gif" shape="rect" title="Netscape 7.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:Mozilla1_ico.png" shape="rect" title="Mozilla 1.x"/>, <a href="http://developer.mozilla.org/en/docs/Image:FF1x.png" shape="rect" title="Firefox 1.x"/>
* </dd></dl>
* <p>The position and size feature elements require a number to be set. The toolbars and window functionalities can be set with a <var>yes</var> or <var>no</var>; you can use <var>1</var> instead of <var>yes</var> and <var>0</var> instead of <var>no</var>. The toolbar and functionality feature elements also accept the shorthand form: you can turn a feature on by simply listing the feature name in the <var>strWindowFeatures</var> string. If you supply the <var>strWindowFeatures</var> parameter, then the <code>titlebar</code> and <code>close</code> are still <var>yes</var> by default, but the other features which have a <var>yes</var>/<var>no</var> choice will be <var>no</var> by default and will be turned off.
* </p><p>Example:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* var WindowObjectReference; // global variable
* 
* function openRequestedPopup()
* {
* WindowObjectReference =
* window.open("http://www.domainname.ext/path/ImgFile.png",
* "DescriptiveWindowName",
* "width=420,height=230,resizable,scrollbars=yes,status=1");
* }
* &lt;/script&gt;
* </pre>
* <p>In this example, the window will be resizable, it will render scrollbar(s) if needed, if the content overflows requested window dimensions and it will render the status bar. It will not render the menubar nor the location bar. Since the author knew about the size of the image (400 pixels wide and 200 pixels high), he added the margins applied to the root element in MSIE 6 which is 15 pixels for top margin, 15 pixels for the bottom margin, 10 pixels for the left margin and 10 pixels for the right margin.
* </p>
* <h2> <span> Best practices </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* var WindowObjectReference = null; // global variable
* 
* function openFFPromotionPopup()
* {
* if(WindowObjectReference == null || WindowObjectReference.closed)
* / * if the pointer to the window object in memory does not exist
* or if such pointer exists but the window was closed * /
* 
* {
* WindowObjectReference = window.open("http://www.spreadfirefox.com/",
* "PromoteFirefoxWindowName", "resizable=yes,scrollbars=yes,status=yes");
* / * then create it. The new window will be created and
* will be brought on top of any other window. * /
* }
* else
* {
* WindowObjectReference.focus();
* / * else the window reference must exist and the window
* is not closed; therefore, we can bring it back on top of any other
* window with the focus() method. There would be no need to re-create
* the window or to reload the referenced resource. * /
* };
* }
* &lt;/script&gt;
* 
* (...)
* 
* &lt;p&gt;&lt;a href="http://www.spreadfirefox.com/"
* target="PromoteFirefoxWindowName"
* onclick="openFFPromotionPopup(); return false;"
* title="This link will create a new window or will re-use
* an already opened one"&gt;Promote Firefox adoption&lt;/a&gt;&lt;/p&gt;
* </pre>
* <p>The above code solves a few usability problems related to links opening secondary window. The purpose of the <code>return false</code> in the code is to cancel default action of the link: if the onclick event handler is executed, then there is no need to execute the default action of the link. But if javascript support is disabled or non-existent on the user's browser, then the onclick event handler is ignored and the browser loads the referenced resource in the target frame or window that has the name "PromoteFirefoxWindowName". If no frame nor window has the name "PromoteFirefoxWindowName", then the browser will create a new window and will name it "PromoteFirefoxWindowName".
* </p><p>More reading on the use of the target attribute:
* </p><p><a href="http://www.w3.org/TR/html401/present/frames.html#h-16.3.2" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/frames.html#h-16.3.2">HTML 4.01 Target attribute specifications</a>
* </p><p><a href="http://www.htmlhelp.com/faq/html/links.html#new-window" rel="nofollow" shape="rect" title="http://www.htmlhelp.com/faq/html/links.html#new-window">How do I create a link that opens a new window?</a>
* </p><p>You can also parameterize the function to make it versatile, functional in more situations, therefore re-usable in scripts and webpages:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* var WindowObjectReference = null; // global variable
* 
* function openRequestedPopup(strUrl, strWindowName)
* {
* if(WindowObjectReference == null || WindowObjectReference.closed)
* {
* WindowObjectReference = window.open(strUrl, strWindowName,
* "resizable=yes,scrollbars=yes,status=yes");
* }
* else
* {
* WindowObjectReference.focus();
* };
* }
* &lt;/script"&gt;
* (...)
* 
* &lt;p&gt;&lt;a href="http://www.spreadfirefox.com/" target="PromoteFirefoxWindow"
* onclick="openRequestedPopup(this.href, this.target); return false;"
* title="This link will create a new window or will re-use
* an already opened one"&gt;Promote
* Firefox adoption&lt;/a&gt;&lt;/p&gt;
* </pre>
* <p>You can also make such function able to open only 1 secondary window and to reuse such single secondary window for other links in this manner:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* var WindowObjectReference = null; // global variable
* var PreviousUrl; / * global variable which will store the
* url currently in the secondary window * /
* 
* function openRequestedSinglePopup(strUrl)
* {
* if(WindowObjectReference == null || WindowObjectReference.closed)
* {
* WindowObjectReference = window.open(strUrl, "SingleSecondaryWindowName",
* "resizable=yes,scrollbars=yes,status=yes");
* }
* else if(previousUrl != strUrl)
* {
* WindowObjectReference = window.open(strUrl, "SingleSecondaryWindowName",
* "resizable=yes,scrollbars=yes,status=yes");
* / * if the resource to load is different,
* then we load it in the already opened secondary window and then
* we bring such window back on top/in front of its parent window. * /
* WindowObjectReference.focus();
* }
* else
* {
* WindowObjectReference.focus();
* };
* PreviousUrl = strUrl;
* / * explanation: we store the current url in order to compare url
* in the event of another call of this function. * /
* }
* &lt;/script&gt;
* 
* (...)
* 
* &lt;p&gt;&lt;a href="http://www.spreadfirefox.com/"
* target="SingleSecondaryWindowName"
* onclick="openRequestedSinglePopup(this.href); return false;"
* title="This link will create a new window or will re-use
* an already opened one"&gt;Promote Firefox
* adoption&lt;/a&gt;&lt;/p&gt;
* &lt;p&gt;&lt;a href="http://www.mozilla.org/support/firefox/faq"
* target="SingleSecondaryWindowName"
* onclick="openRequestedSinglePopup(this.href); return false;"
* title="This link will create a new window or will re-use
* an already opened one"&gt;Firefox FAQ&lt;/a&gt;&lt;/p&gt;
* </pre>
* <h2> <span> FAQ </span></h2>
* <dl><dt style="font-weight:bold"> How can I prevent the confirmation message asking the user whether he wants to close the window?
* </dt><dd> You can not. <strong>New windows not opened by javascript can not as a rule be closed by JavaScript.</strong> The JavaScript Console in Mozilla-based browsers will report the warning message: <code>"Scripts may not close windows that were not opened by script."</code> Otherwise the history of URLs visited during the browser session would be lost.
* </dd><dd> <a href="http://developer.mozilla.org/en/docs/window.close" shape="rect" title="window.close">More on the window.close()</a> method
* </dd></dl>
* <dl><dt style="font-weight:bold"> How can I bring back the window if it is minimized or behind another window?
* </dt><dd> First check for the existence of the window object reference of such window and if it exists and if it has not been closed, then use the <a href="window.focus" shape="rect" title="DOM:window.focus">focus()</a> method. There is no other reliable way. You can examine an <a href="window.open#Best_practices" shape="rect" title="">example explaining how to use the focus() method</a>.
* </dd></dl>
* <dl><dt style="font-weight:bold"> How do I force a maximized window?
* </dt><dd> You cannot. All browser manufacturers try to make the opening of new secondary windows noticed by users and noticeable by users to avoid confusion, to avoid disorienting users.
* </dd></dl>
* <dl><dt style="font-weight:bold"> How do I turn off window resizability or remove toolbars?
* </dt><dd> You cannot force this. Users with Mozilla-based browsers have absolute control over window functionalities like resizability, scrollability and toolbars presence via user preferences in <code>about:config</code>. Since your users are the ones who are supposed to use such windows (and not you, being the web author), the best is to avoid interfering with their habits and preferences. We recommend to always set the resizability and scrollbars presence (if needed) to yes to insure accessibility to content and usability of windows. This is in the best interests of both the web author and the users.
* </dd></dl>
* <dl><dt style="font-weight:bold"> How do I resize a window to fit its content?
* </dt><dd> You can not reliably because the users can prevent the window from being resized by unchecking the
* </dd></dl>
* <p><code style="white-space: normal;">Edit/Preferences/Advanced/Scripts &amp; Plug-ins/Allow Scripts to/ Move or resize existing windows</code> checkbox in Mozilla or
* <code style="white-space: normal;">Tools/Options.../Content tab/Enable Javascript/Advanced button/Move or resize existing windows</code> checkbox in Firefox or by setting <code>dom.disable_window_move_resize</code> to <var>true</var> in the <kbd>about:config</kbd> or by editing accordingly their <a href="http://www.mozilla.org/support/firefox/edit#user" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#user">user.js file</a>.
* </p>
* <dl><dd> In general, users usually disable moving and resizing of existing windows because allowing authors' scripts to do so has been abused overwhelmingly in the past and the rare scripts that do not abuse such feature are often wrong, inaccurate when resizing the window. 99% of all those scripts disable window resizability and disable scrollbars when in fact they should enable both of these features to allow a cautious and sane fallback mechanism if their calculations are wrong.
* </dd></dl>
* <dl><dd> The window method <a href="window.sizeToContent" shape="rect" title="DOM:window.sizeToContent">sizeToContent()</a> is also disabled if the user unchecks the preference <code>Move or resize existing windows</code> checkbox. Moving and resizing a window remotely on the user's screen via script will very often annoy the users, will disorient the user, and will be wrong at best. The web author expects to have full control of (and can decide about) every position and size aspects of the users' browser window ... which is simply not true.
* </dd></dl>
* <dl><dt style="font-weight:bold"> How do I open a referenced resource of a link in a new tab? or in a specific tab?
* </dt><dd> Currently, you can not. Only the user can set his advanced preferences to do that. <a href="http://kmeleon.sourceforge.net/" rel="nofollow" shape="rect" title="http://kmeleon.sourceforge.net/">K-meleon 1.1</a>, a Mozilla-based browser, gives complete control and power to the user regarding how links are opened. Some advanced extensions also give Mozilla and Firefox a lot of power over how referenced resources are loaded.
* </dd></dl>
* <dl><dd> In a few years, the <a href="http://www.w3.org/TR/2004/WD-css3-hyperlinks-20040224/#target0" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2004/WD-css3-hyperlinks-20040224/#target0">target property of the CSS3 hyperlink module</a> may be implemented (if CSS3 Hyperlink module as it is right now is approved). And even if and when this happens, you can expect developers of browsers with tab-browsing to give the user entire veto power and full control over how links can open web pages. How to open a link should always be entirely under the control of the user.
* </dd></dl>
* <dl><dt style="font-weight:bold"> How do I know whether a window I opened is still open?
* </dt><dd> You can test for the existence of the window object reference which is the returned value in case of success of the window.open() call and then verify that <var>WindowObjectReference</var>.closed return value is <var>false</var>.
* </dd></dl>
* <dl><dt style="font-weight:bold"> How can I tell when my window was blocked by a popup blocker?
* </dt><dd> With the built-in popup blockers of Mozilla/Firefox and Internet Explorer 6 SP2, you have to check the return value of <code>window.open()</code>: it will be <var>null</var> if the window wasn't allowed to open. However, for most other popup blockers, there is no reliable way.
* </dd></dl>
* <dl><dt style="font-weight:bold"> What is the JavaScript relationship between the main window and the secondary window?
* </dt><dd> The return value of the window.open() method and the <a href="window.opener" shape="rect" title="DOM:window.opener">opener</a> property. The <var>WindowObjectReference</var> links the main (opener) window to the secondary (sub-window) window while the opener keyword will link the secondary window to its main (opener) window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> I can not access the properties of the new secondary window. I always get an  error in the javascript console saying "Error: uncaught exception: Permission denied to get property &lt;property_name or method_name&gt;. Why is that?
* </dt><dd> It is because of the cross-domain script security restriction (also referred as the "Same Origin Policy"). A script loaded in a window (or frame) from a distinct origin (domain name) <strong>cannot get nor set</strong> properties of another window (or frame) or the properties of any of its HTML objects coming from another distinct origin (domain name). Therefore, before executing a script targeting a secondary window, the browser in the main window will verify that the secondary window has the same domain name.
* </dd><dd> More reading on the cross-domain script security restriction: <a href="http://www.mozilla.org/projects/security/components/same-origin.html" rel="nofollow" shape="rect" title="http://www.mozilla.org/projects/security/components/same-origin.html">http://www.mozilla.org/projects/security/components/same-origin.html</a>
* </dd></dl>
* <h2> <span> Usability issues </span></h2>
* <h3> <span> Avoid resorting to <code>window.open()</code> </span></h3>
* <p>Generally speaking, it is preferable to avoid resorting to window.open() for several reasons:
* </p>
* <ul><li> All Mozilla-based browsers offer tab-browsing and this is the preferred mode of opening referenced resources... not just in Mozilla-based browsers but in all other browsers offering tab-browsing. In other words, tab-capable browser users overall prefer opening new tabs than opening new windows in a majority of webpage situations. Tab-capable browsers have rapidly gained support and enthusiasm on internet in the last 3 years; this trend will not revert back. MSIE 7, released in October 2006, has full support for tab browsing.
* </li></ul>
* <ul><li> There are now <a href="https://addons.mozilla.org/seamonkey/browse/type:1/cat:48/sort:updated" rel="nofollow" shape="rect" title="https://addons.mozilla.org/seamonkey/browse/type:1/cat:48/sort:updated">several Mozilla extensions</a> (like Multizilla) and <a href="https://addons.update.mozilla.org/firefox/browse/type:1/cat:14/sort:updated" rel="nofollow" shape="rect" title="https://addons.update.mozilla.org/firefox/browse/type:1/cat:14/sort:updated">Firefox extensions</a> (like <a href="https://addons.mozilla.org/firefox/addon/158" rel="nofollow" shape="rect" title="https://addons.mozilla.org/firefox/addon/158">Tabbrowser preferences</a>), features, settings and advanced preferences based on tab-browsing and based on converting window.open() calls into opening tabs, based on neutralizing window.open() calls, in particular in neutralizing unrequested openings of new windows (often referred as blocking unrequested popups or as blocking script-initiated windows opening automatically). Such features found in extensions include opening a link in a new window or not, in the same window, in a new tab or not, in "background" or not. Coding carelessly to open new windows can no longer be assured of success, can not succeed by force and, if it does, it will annoy a majority of users.
* </li></ul>
* <ul><li> New windows can have menubar missing, scrollbars missing, status bar missing, window resizability disabled, etc.; new tabs can not be missing those functionalities or toolbars (or at least, the toolbars which are present by default). Therefore, tab-browsing is preferred by a lot of users because the normal user-interface of the browser window they prefer is kept intact, remains stable.
* </li></ul>
* <ul><li> Opening new windows, even with reduced features, uses considerably a lot of the user's system resources (cpu, RAM) and involves considerably a lot of coding in the source code (security management, memory management, various code branchings sometimes quite complex, window frame/chrome/toolbars building, window positioning and sizing, etc.). Opening new tabs is less demanding on the user's system resources (and faster to achieve) than opening new windows.
* </li></ul>
* <h3> <span> Offer to open a link in a new window, using these guidelines </span></h3>
* <p>If you want to offer to open a link in a new window, then follow tested and recommendable usability and accessibility guidelines:
* </p>
* <h4> <span> <em>Never</em> use this form of code for links:<br clear="none"/><code>&lt;a href="javascript:window.open(...)" ...&gt;</code> </span></h4>
* <p>"javascript:" links break accessibility and usability of webpages in every browser.
* </p>
* <ul><li> "javascript:" pseudo-links become dysfunctional when javascript support is disabled or inexistent. Several corporations allow their employees to surf on the web but under strict security policies: no javascript enabled, no java, no activeX, no Flash. For various reasons (security, public access, text browsers, etc..), about 5% to 10% of users on the web surf with javascript disabled.
* </li><li> "javascript:" links will interfere with advanced features in tab-capable browsers: eg. middle-click on links, Ctrl+click on links, tab-browsing features in extensions, etc.
* </li><li> "javascript:" links will interfere with the process of indexing webpages by search engines.
* </li><li> "javascript:" links interfere with assistive technologies (e.g. voice browsers) and several web-aware applications (e.g. <abbr title="Personal Digital Assistant">PDAs</abbr> and mobile browsers).
* </li><li> "javascript:" links also interfere with "mouse gestures" features implemented in browsers.
* </li><li> Protocol scheme "javascript:" will be reported as an error by link validators and link checkers.
* </li></ul>
* <p><b>Further reading:</b>
* </p>
* <ul><li> <a href="http://www.useit.com/alertbox/20021223.html" rel="nofollow" shape="rect" title="http://www.useit.com/alertbox/20021223.html">Top Ten Web-Design Mistakes of 2002</a>, 6. JavaScript in Links, Jakob Nielsen, December 2002
* </li><li> <a href="http://www.evolt.org/article/Links_and_JavaScript_Living_Together_in_Harmony/17/20938/" rel="nofollow" shape="rect" title="http://www.evolt.org/article/Links_and_JavaScript_Living_Together_in_Harmony/17/20938/">Links &amp; JavaScript Living Together in Harmony</a>, Jeff Howden, February 2002
* </li><li> <a href="http://jibbering.com/faq/#FAQ4_24" rel="nofollow" shape="rect" title="http://jibbering.com/faq/#FAQ4_24">comp.lang.javascript newsgroup discussion FAQ on "javascript:" links</a>
* </li></ul>
* <h4> <span> Never use <code>&lt;a href="#" onclick="window.open(...);"&gt;</code> </span></h4>
* <p>Such pseudo-link also breaks accessibility of links. <strong>Always use a real URL for the href attribute value</strong> so that if javascript support is disabled or inexistent or if the user agent does not support opening of secondary window (like MS-Web TV, text browsers, etc), then such user agents will still be able to load the referenced resource according to its default mode of opening/handling a referenced resource. This form of code also interferes with advanced features in tab-capable browsers: eg. middle-click on links, Ctrl+click on links, Ctrl+Enter on links, "mouse gestures" features.
* </p>
* <h4> <span> Always identify links which will create (or will re-use) a new, secondary window </span></h4>
* <p>Identify links that will open new windows in a way that helps navigation for users by coding the title attribute of the link, by adding an icon at the end of the link or by coding the cursor accordingly.
* </p><p>The purpose is to warn users in advance of context changes to minimize confusion on the user's part: changing the current window or popping up new windows can be very disorienting to users (Back toolbar button is disabled).
* </p>
* <blockquote>
* <p>"Users often don't notice that a new window has opened, especially if they are using a small monitor where the windows are maximized to fill up the screen. So a user who tries to return to the origin will be confused by a grayed out <i>Back</i> button."
* quote from <a href="http://www.useit.com/alertbox/990530.html" rel="nofollow" shape="rect" title="http://www.useit.com/alertbox/990530.html">The Top Ten <i>New</i> Mistakes of Web Design</a>: 2. Opening New Browser Windows, Jakob Nielsen, May 1999</p>
* </blockquote>
* <p>When extreme changes in context are explicitly identified before they occur, then the users can determine if they wish to proceed or so they can be prepared for the change: not only they will not be confused or feel disoriented, but more experienced users can better decide how to open such links (in a new window or not, in the same window, in a new tab or not, in "background" or not).
* </p><p><b>References</b>
* </p>
* <ul><li> "If your link spawns a new window, or causes another windows to 'pop up' on your display, or move the focus of the system to a new FRAME or Window, then the nice thing to do is to tell the user that something like that will happen." <a href="http://www.w3.org/WAI/wcag-curric/sam77-0.htm" rel="nofollow" shape="rect" title="http://www.w3.org/WAI/wcag-curric/sam77-0.htm">World Wide Web Consortium Accessibility Initiative regarding popups</a>
* </li><li> "Use link titles to provide users with a preview of where each link will take them, before they have clicked on it." <a href="http://www.useit.com/alertbox/991003.html" rel="nofollow" shape="rect" title="http://www.useit.com/alertbox/991003.html">Ten Good Deeds in Web Design</a>, Jakob Nielsen, October 1999
* </li><li> <a href="http://www.useit.com/alertbox/980111.html" rel="nofollow" shape="rect" title="http://www.useit.com/alertbox/980111.html">Using Link Titles to Help Users Predict Where They Are Going</a>, Jakob Nielsen, January 1998
* </li></ul>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" summary="First 3 rows: 12 image icons identifying links opening new window; 4th and last row: 2 cursors identifying links opening new window" width="100%">
* <tr>
* <td colspan="4" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Example "New Window" Icons &amp; Cursors</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1" style="width: 25%;"><a href="http://developer.mozilla.org/en/docs/Image:newwindowYahoo.png" shape="rect" title="New window icon from yahoo.com"/></td>
* <td colspan="1" rowspan="1" style="width: 25%;"><a href="http://developer.mozilla.org/en/docs/Image:newwinMSIE.gif" shape="rect" title="New window icon from microsoft.com"/></td>
* <td colspan="1" rowspan="1" style="width: 25%;"><a href="http://developer.mozilla.org/en/docs/Image:Popup_requested_new_window.gif" shape="rect" title="New window icon from webaim.org"/></td>
* <td colspan="1" rowspan="1" style="width: 25%;"><a href="http://developer.mozilla.org/en/docs/Image:popupImageSun.gif" shape="rect" title="New window icon from sun.com"/></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:opennews_rb.gif" shape="rect" title="New window icon from bbc.co.uk"/></td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:AIS_NewWindowIcon.png" shape="rect" title="New window icon from Accessible Internet Solutions"/></td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:pop-up-launcher.gif" shape="rect" title="New window icon from accessify.com"/></td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:Webstyleguide_com_newwind.gif" shape="rect" title="New window icon from webstyleguide.com"/></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:popicon_1.gif" shape="rect" title="New window icon from an unknown source"/></td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:new.gif" shape="rect" title="New window icon from an unknown source"/></td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:WillCreateOrRecycleNewWindow.gif" shape="rect" title="New window icon from an unknown source"/></td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:OpenRequestedPopup.png" shape="rect" title="New window icon from gtalbot.org"/></td>
* </tr>
* <tr>
* <td colspan="2" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:Cursor_LinkNewWindowTargetBlank.png" shape="rect" title="New window cursor from draig.de"/></td>
* <td colspan="2" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Image:Cursor_newwindowSergeySokoloff.png" shape="rect" title="New window cursor from mithgol.ru"/></td>
* </tr>
* </table>
* <h4> <span> Always use the target attribute </span></h4>
* <p>If javascript support is disabled or non-existent, then the user agent will create a secondary window accordingly or will render the referenced resource according to its handling of the target attribute: e.g. some user agents which can not create new windows, like MS Web TV, will fetch the referenced resource and append it at the end of the current document. The goal and the idea is to try to provide - <strong>not impose</strong> - to the user a way to open the referenced resource, a mode of opening the link. Your code should not interfere with the features of the browser at the disposal of the user and your code should not interfere with the final decision resting with the user.
* </p>
* <h4> <span> Do not use <code>target="_blank"</code> </span></h4>
* <p>Always provide a meaningful name to your target attribute and try to reuse such target attribute in your page so that a click on another link may load the referenced resource in an already created and rendered window (therefore speeding up the process for the user) and therefore justifying the reason (and user system resources, time spent) for creating a secondary window in the first place. Using a single target attribute value and reusing it in links is much more user resources friendly as it only creates one single secondary window which is recycled. On the other hand, using "_blank" as the target attribute value will create several new and unnamed windows on the user's desktop which can not be recycled, reused.
* In any case, if your code is well done, it should not interfere with the user's final choice but rather merely offer him more choices, more ways to open links and more power to the tool he's using (a browser).
* </p>
* <h2> <span> Glossary </span></h2>
* <dl><dt style="font-weight:bold"> Opener window, parent window, main window, first window </dt><dd> Terms often used to describe or to identify the same window. It is the window from which a new window will be created. It is the window on which the user clicked a link which lead to the creation of another, new window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> Sub-window, child window, secondary window, second window </dt><dd> Terms often used to describe or to identify the same window. It is the new window which was created.
* </dd></dl>
* <dl><dt style="font-weight:bold"> Unrequested popup windows</dt><dd> Script-initiated windows opening automatically without the user's consent.
* </dd></dl>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. window.open() is not part of any <abbr title="World Wide Web Consortium">W3C</abbr> specification or technical recommendation.
* </p>
* <h2> <span> Notes </span></h2>
* <h3> <span> Note on precedence </span></h3>
* <p>In cases where <code>left</code> and <code>screenX</code> (and/or <code>top</code> and <code>screenY</code>) have conflicting values, then <code>left</code> and <code>top</code> have precedence over <code>screenX</code> and <code>screenY</code> respectively. If <code>left</code> and <code>screenX</code> (and/or <code>top</code> and <code>screenY</code>) are defined in the <var>strWindowFeatures</var> list, then <code>left</code> (and/or <code>top</code>) will be honored and rendered. In the following example the new window will be positioned at 100 pixels from the left side of the work area for applications of the user's operating system, not at 200 pixels.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* WindowObjectReference = window.open("http://news.bbc.co.uk/",
* "BBCWorldNewsWindowName",
* "left=100,screenX=200,resizable,scrollbars,status");
* &lt;/script&gt;
* </pre>
* <p>If left is set but top has no value and screenY has a value, then left and screenY will be the coordinate positioning values of the secondary window.
* </p><p>outerWidth has precedence over width and width has precedence over innerWidth.
* outerHeight has precedence over height and height has precedence over innerHeight.  In the following example, Mozilla-browsers will create a new window with an outerWidth of 600 pixels wide and will ignore the request of a width of 500 pixels and will also ignore the request of an innerWidth of 400 pixels.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* WindowObjectReference = window.open("http://www.wwf.org/",
* "WWildlifeOrgWindowName",
* "outerWidth=600,width=500,innerWidth=400,resizable,scrollbars,status");
* &lt;/script&gt;
* </pre>
* <h3> <span> Note on position and dimension error correction </span></h3>
* <p>Requested position and requested dimension values in the <var>strWindowFeatures</var> list will not be honored and <strong>will be corrected</strong> if any of such requested value does not allow the entire browser window to be rendered within the work area for applications of the user's operating system. <strong>No part of the new window can be initially positioned offscreen. This is by default in all Mozilla-based browser releases.</strong>
* </p><p><a href="http://msdn2.microsoft.com/en-us/library/ms997645.aspx#xpsp_topic5" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/ms997645.aspx#xpsp_topic5">MSIE 6 SP2 has a similar error correction mechanism</a> but it is not activated by default in all security levels: a security setting can disable such error correction mechanism.
* </p>
* <h3> <span> Note on scrollbars </span></h3>
* <p>When content overflows window viewport dimensions, then scrollbar(s) (or some scrolling mechanism) are necessary to ensure that content can be accessed by users. Content can overflow window dimensions for several reasons which are outside the control of web authors:
* </p>
* <ul><li> user resizes the window
* </li><li> user increases the text size of fonts via View/Text Zoom (%) menuitem in Mozilla or View/Text Size/Increase or Decrease in Firefox
* </li><li> user sets a minimum font size for pages which is bigger than the font-size the web author requested. People over 40 years old or with particular viewing habit or reading preference often set a minimal font size in Mozilla-based browsers.
* </li><li> web author is not aware of default margin (and/or border and/or padding) values applying to root element or body node in various browsers and various browser versions
* </li><li> user uses an user stylesheet (<a href="http://www.mozilla.org/support/firefox/edit#content" rel="nofollow" shape="rect" title="http://www.mozilla.org/support/firefox/edit#content">userContent.css in Mozilla-based browsers</a>) for his viewing habits which increases document box dimensions (margin, padding, default font size)
* </li><li> user can customize individually the size (height or width) of most toolbars via operating system settings. E.g. window resizing borders, height of browser titlebar, menubar, scrollbars, font size are entirely customizable by the user in Windows XP operating system. These toolbars dimensions can also be set via browser themes and skins or by operating system themes
* </li><li> web author is unaware that the user default browser window has custom toolbar(s) for specific purpose(s); e.g.: prefs bar, web developer bar, accessibility toolbar, popup blocking and search toolbar, multi-feature toolbar, etc.
* </li><li> user uses assistive technologies or add-on features which modify the operating system's work area for applications: e.g. MS-Magnifier
* </li><li> user repositions and/or resizes directly or indirectly the operating system's work area for applications: e.g. user resizes the Windows taskbar, user positions the Windows taskbar on the left side (arabic language based) or right side (Hebrew language), user has a permanent MS-Office quick launch toolbar, etc.
* </li><li> some operating system (Mac OS X) forces presence of toolbars which can then fool the web author's anticipations, calculations of the effective dimensions of the browser window
* </li></ul>
* <h3> <span> Note on status bar </span></h3>
* <p>You should assume that a large majority of users' browsers will have the status bar or that a large majority of users will want to force the status bar presence: best is to always set this feature to yes. Also, if you specifically request to remove the status bar, then Firefox users will not be able to view the Site Navigation toolbar if it is installed. In Mozilla and in Firefox, all windows with a status bar have a window resizing grippy at its right-most side. The status bar also provides info on http connection, hypertext resource location, download progress bar, encryption/secure connection info with <abbr title="Secure Socket Layer">SSL</abbr> connection (displaying a yellow padlock icon), internet/security zone icons, privacy policy/cookie icon, etc. <strong>Removing the status bar usually removes a lot of functionality, features and information considered useful (and sometimes vital) by the users.</strong>
* </p>
* <h3> <span> Note on security issues of the status bar presence </span></h3>
* <p>In MSIE 6 for XP SP2: For windows opened using window.open():
* </p>
* <blockquote>
* <p>"For windows opened using window.open():
* Expect the status bar to be present, and code for it. <strong>The status bar will be on by default</strong> and is 20-25 pixels in height. (...)"quote from
* <a href="http://msdn2.microsoft.com/en-us/library/ms997645.aspx#xpsp_topic5" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/ms997645.aspx#xpsp_topic5">Fine-Tune Your Web Site for Windows XP Service Pack 2, Browser Window Restrictions in XP SP2</a></p>
* </blockquote>
* <blockquote>
* <p>"(...) windows that are created using the window.open() method can be called by scripts and used to spoof a user interface or desktop or to hide malicious information or activity by sizing the window so that the status bar is not visible.
* Internet Explorer windows provide visible security information to the user to help them ascertain the source of the Web page and the security of the communication with that page. When these elements are not in view, the user might think they are on a more trusted page or interacting with a system process when they are actually interacting with a malicious host. (...)<strong>Script-initiated windows will be displayed fully, with the Internet Explorer title bar and status bar.</strong> (...)
* Script management of Internet Explorer status bar
* Detailed description
* <strong>Internet Explorer has been modified to not turn off the status bar for any windows. The status bar is always visible for all Internet Explorer windows.</strong> (...) Without this change, windows that are created using the window.open() method can be called by scripts and spoof a user interface or desktop or hide malicious information or activity by hiding important elements of the user interface from the user.
* The status bar is a security feature of Internet Explorer windows that provides Internet Explorer security zone information to the user. This zone cannot be spoofed (...)"
* quote from <a href="http://www.microsoft.com/technet/prodtechnol/winxppro/maintain/sp2brows.mspx#ECAA" rel="nofollow" shape="rect" title="http://www.microsoft.com/technet/prodtechnol/winxppro/maintain/sp2brows.mspx#ECAA">Changes to Functionality in Microsoft Windows XP Service Pack 2, Internet Explorer Window Restrictions</a>
* </p>
* </blockquote>
* <h3> <span> Note on fullscreen </span></h3>
* <p>In MSIE 6 for XP SP2:
* </p>
* <ul><li> "window.open() with fullscreen=yes will now result in a maximized window, not a kiosk mode window."
* </li></ul>
* <ul><li> "The definition of the fullscreen=yes specification is changed to mean 'show the window as maximized,' which will keep the title bar, address bar, and status bar visible."
* </li></ul>
* <p><i>References:</i>
* </p>
* <ul><li><a href="http://msdn2.microsoft.com/en-us/library/ms997645.aspx#xpsp_topic5" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/ms997645.aspx#xpsp_topic5">Fine-Tune Your Web Site for Windows XP Service Pack 2</a>
* </li></ul>
* <ul><li> <a href="http://www.microsoft.com/technet/prodtechnol/winxppro/maintain/sp2brows.mspx#ECAA" rel="nofollow" shape="rect" title="http://www.microsoft.com/technet/prodtechnol/winxppro/maintain/sp2brows.mspx#ECAA">Changes to Functionality in Microsoft Windows XP Service Pack 2, Script sizing of Internet Explorer windows</a>
* </li></ul>
* <h3> <span> Note on outerHeight versus height </span></h3>
* <p><a href="http://developer.mozilla.org/en/docs/Image:FirefoxInnerVsOuterHeight.png" shape="rect" title="innerHeight vs outerHeight illustration"/></p>
* <h2> <span> Tutorials </span></h2>
* <p><a href="http://www.infimum.dk/HTML/JSwindows.html" rel="nofollow" shape="rect" title="http://www.infimum.dk/HTML/JSwindows.html">JavaScript windows (tutorial)</a> by Lasse Reichstein Nielsen
* </p><p><a href="http://www.accessify.com/tutorials/the-perfect-pop-up.asp" rel="nofollow" shape="rect" title="http://www.accessify.com/tutorials/the-perfect-pop-up.asp">The perfect pop-up (tutorial)</a> by Ian Lloyd
* </p><p><a href="http://www.gtalbot.org/FirefoxSection/Popup/PopupAndFirefox.html" rel="nofollow" shape="rect" title="http://www.gtalbot.org/FirefoxSection/Popup/PopupAndFirefox.html">Popup windows and Firefox (interactive demos)</a> by Gérard Talbot
* </p>
* <h2> <span> References </span></h2>
* <p><a href="http://www.cs.tut.fi/~jkorpela/www/links.html" rel="nofollow" shape="rect" title="http://www.cs.tut.fi/~jkorpela/www/links.html">Links Want To Be Links</a> by Jukka K. Korpela
* </p><p><a href="http://www.evolt.org/article/Links_and_JavaScript_Living_Together_in_Harmony/17/20938/" rel="nofollow" shape="rect" title="http://www.evolt.org/article/Links_and_JavaScript_Living_Together_in_Harmony/17/20938/">Links &amp; JavaScript Living Together in Harmony</a> by Jeff Howden
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
open: function(strUrl, strWindowName , strWindowFeatures) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>window.openDialog</code> is an extension to <a href="window.open" shape="rect" title="DOM:window.open">window.open</a>. It behaves the same, except that it can optionally take one or more parameters past <code>windowFeatures</code>, and <code>windowFeatures</code> itself is treated a little differently.
* </p><p>The optional parameters, if present, will be bundled up in a JavaScript Array object and added to the newly created window as a property named <a href="http://developer.mozilla.org/en/docs/index.php?title=window.arguments&amp;action=edit" shape="rect" title="DOM:window.arguments">window.arguments</a>. They may be referenced in the JavaScript of the window at any time, including during the execution of a <code>load</code> handler. These parameters may be used, then, to pass arguments to and from the dialog window.
* </p><p>Note that the call to <code>openDialog()</code> returns immediately. If you want the call to block until the user has closed the dialog, supply <code>modal</code> as a <code>windowFeatures</code> parameter. Note that this also means the user won't be able to interact with the opener window until he closes the modal dialog.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>newWindow</i> = openDialog(<i>url</i>, <i>name</i>, <i>features</i>, <i>arg1</i>, <i>arg2</i>, ...)
* </pre>
* <dl><dt style="font-weight:bold"> newWindow </dt><dd> The opened window
* </dd><dt style="font-weight:bold"> url </dt><dd> The URL to be loaded in the newly opened window.
* </dd><dt style="font-weight:bold"> name </dt><dd> The window name (optional). See <a href="DOM:window.open" shape="rect" title="DOM:window.open">window.open</a> description for detailed information.
* </dd><dt style="font-weight:bold"> features </dt><dd> See <a href="window.open" shape="rect" title="DOM:window.open">window.open</a> description for description.
* </dd><dt style="font-weight:bold"> arg1, arg2, ... </dt><dd> The arguments to be passed to the new window (optional).
* </dd></dl>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var win = openDialog("http://example.tld/zzz.xul", "dlg", "", "pizza", 6.98);
* </pre>
* <h2> <span> Notes </span></h2>
* <h3> <span>New Features</span></h3>
* <p><code>all</code> - Initially activates (or deactivates <code>("all=no")</code>) all chrome (except the behaviour flags <code>chrome</code>, <code>dialog</code> and <code>modal</code>). These can be overridden (so <code>"menubar=no,all"</code> turns on all chrome except the menubar.) This feature is explicitly ignored by <a href="http://developer.mozilla.org/en/docs/window.open" shape="rect" title="window.open">window.open</a>. <code>window.openDialog</code> finds it useful because of its different default assumptions.
* </p>
* <h3> <span>Default behaviour</span></h3>
* <p>The <code>chrome</code> and <code>dialog</code> features are always assumed on, unless explicitly turned off ("<code>chrome=no</code>"). <code>openDialog</code> treats the absence of the features parameter as does <a href="http://developer.mozilla.org/en/docs/window.open" shape="rect" title="window.open">window.open</a>, (that is, an empty string sets all features to off) except <code>chrome</code> and <code>dialog</code>, which default to on. If the <code>features</code> parameter is a zero-length string, or contains only one or more of the behaviour features (<code>chrome</code>, <code>dependent</code>, <code>dialog</code> and <code>modal</code>) the chrome features are assumed "OS' choice." That is, window creation code is not given specific instructions, but is instead allowed to select the chrome that best fits a dialog on that operating system.
* </p>
* <h3> <span>Passing extra parameters to the dialog</span></h3>
* <p>To pass extra parameters into the dialog, you can simply supply them after the <tt>windowFeatures</tt> parameter:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">openDialog("http://example.tld/zzz.xul", "dlg", "", "pizza", 6.98);
* </pre>
* <p>The extra parameters will then get packed into a property named <tt>arguments</tt> of type <a href="http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a>, and this property gets added to the newly opened dialog window.
* </p><p>To access these extra parameters from within dialog code, use the following scheme:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var food  = window.arguments[0];
* var price = window.arguments[1];
* </pre>
* <p>Note that you can access this property from within anywhere in the dialog code. (<a href="http://developer.mozilla.org/en/docs/Code_snippets:Dialogs_and_Prompts#Passing_arguments_and_displaying_a_dialog" shape="rect" title="Code snippets:Dialogs and Prompts">Another example</a>).
* </p>
* <h3> <span>Returning values from the dialog</span></h3>
* <p>Since <tt>window.close()</tt> erases all properties associated with the dialog window (i.e. the variables specified in the JavaScript code which gets loaded from the dialog), it is not possible to pass return values back past the close operation using globals (or any other constructs).
* </p><p>To be able to pass values back to the caller, you have to supply some object via the extra parameters. You can then access this object from within the dialog code and set properties on it, containing the values you want to return or preserve past the <tt>window.close()</tt> operation.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var retVals = { address: null, delivery: null };
* openDialog("http://example.tld/zzz.xul", "dlg", "modal", "pizza", 6.98, retVals);
* </pre>
* <p>If you set the properties of the <tt>retVals</tt> object in the dialog code as described below, you can now access them via the <tt>retVals</tt> array after the <tt>openDialog()</tt> call returns.
* </p><p>Inside the dialog code, you can set the properties as follows:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var retVals = window.arguments[2];
* retVals.address  = enteredAddress;
* retVals.delivery = "immediate";
* </pre>
* <p>See also <a href="http://groups.google.com/group/netscape.public.dev.xul/msg/02075a1736406b40" rel="nofollow" shape="rect" title="http://groups.google.com/group/netscape.public.dev.xul/msg/02075a1736406b40">[1]</a>. (<a href="http://developer.mozilla.org/en/docs/Code_snippets:Dialogs_and_Prompts#Passing_arguments_and_displaying_a_dialog" shape="rect" title="Code snippets:Dialogs and Prompts">Another example</a>).
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
openDialog: function(url, name, features, arg1, arg2) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Returns a reference to the window that opened this current window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>objRef</i> = window.opener
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* if window.opener != indexWin {
* referToTop(window.opener);
* }
* </pre>
* <h2> <span>Notes </span></h2>
* <p>When a window is opened from another window, it maintains a reference to that first window as <b>window.opener</b>. If the current window has no opener, this method returns NULL.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
opener: undefined,
/**
* <h1> <span> window.outerHeight </span></h1>
* <p>Gets the height of the outside of the browser window.
* window.outerHeight represents the height of the whole browser window including toolbars and window chrome.
* </p>
* <h2> <span> Syntax </span></h2>
* <p>var <var>intWindowHeight</var> = window.outerHeight;
* </p>
* <h2> <span> Value </span></h2>
* <p><var>intWindowHeight</var> stores the window.outerHeight property value.
* </p><p>The window.outerHeight property is read-only; it has no default value.
* window.outerHeight property stores an integer representing a number of pixels.
* </p>
* <h2> <span> Notes </span></h2>
* <p>To do
* </p><p>See also <a href="http://developer.mozilla.org/en/docs/window.innerHeight" shape="rect" title="window.innerHeight">window.innerHeight</a>, <a href="http://developer.mozilla.org/en/docs/window.innerWidth" shape="rect" title="window.innerWidth">window.innerWidth</a> and <a href="http://developer.mozilla.org/en/docs/window.outerWidth" shape="rect" title="window.outerWidth">window.outerWidth</a>
* </p>
* <h2> <span> Graphical example </span></h2>
* <p style="margin-left: -32px;"><a href="http://developer.mozilla.org/en/docs/Image:FirefoxInnerVsOuterHeight2.png" shape="rect" title="innerHeight vs outerHeight illustration"/></p>
* <h2> <span> Standards info </span></h2>
* <p>DOM Level 0. Not part of any <abbr title="World Wide Web Consortium">W3C</abbr> technical specification or recommendation.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
outerHeight: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Gets the width of the outside of the browser window.
* window.outerWidth represents the width of the whole browser window including sidebar (if expanded), window chrome and window [re-]sizing borders/handles.
* </p>
* <h2> <span> Syntax </span></h2>
* <p>var <var>intWindowWidth</var> = window.outerWidth;
* </p>
* <h2> <span> Value </span></h2>
* <p><var>intWindowWidth</var> stores the window.outerWidth property value.
* </p><p>The window.outerWidth property is read-only; it has no default value.
* window.outerWidth property stores an integer representing a number of pixels.
* </p>
* <h2> <span> Notes </span></h2>
* <p>To do
* </p><p>See also <a href="http://developer.mozilla.org/en/docs/window.innerHeight" shape="rect" title="window.innerHeight">window.innerHeight</a>, <a href="http://developer.mozilla.org/en/docs/window.innerWidth" shape="rect" title="window.innerWidth">window.innerWidth</a> and <a href="http://developer.mozilla.org/en/docs/window.outerHeight" shape="rect" title="window.outerHeight">window.outerHeight</a>.
* </p>
* <h2> <span> Standards info </span></h2>
* <p>DOM Level 0. Not part of any <abbr title="World Wide Web Consortium">W3C</abbr> technical specification or recommendation.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:NeedsContent" shape="rect" title="Category:NeedsContent">NeedsContent</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
outerWidth: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Gets the amount of left-hand-side content hidden by scrolling to the right.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>hScroll</i> = window.pageXOffset
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>hScroll</code> is the number of pixels scrolled as an integer.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var hScroll = window.pageXOffset;
* var vScroll = window.pageYOffset;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>If the user has scrolled to the right and 200 pixels of the left-hand-side content is hidden by this, then the <b>window.pageXOffset</b> property returns 200.
* See also: <a href="http://developer.mozilla.org/en/docs/window.pageYOffset" shape="rect" title="window.pageYOffset">window.pageYOffset</a>
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
pageXOffset: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Gets the amount of top page content that has been hidden by scrolling down.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>vScroll</i> = window.pageYOffset
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>vScroll</code> is the number of pixels as an integer.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var vScroll = pageYOffset;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>If the user has scrolled down and 400 pixels of the top page content is hidden by this, then the <b>window.pageYOffset</b> property returns 400.
* See also <a href="http://developer.mozilla.org/en/docs/window.pageXOffset" shape="rect" title="window.pageXOffset">window.pageXOffset</a>
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
pageYOffset: undefined,
/**
* <h2> <span> Summary</span></h2>
* <p>Returns a reference to the parent of the current window or subframe.
* </p><p>If a window does not have a parent, its <code>parent</code> property is a reference to itself.
* </p><p>When a window is loaded in an <code>&lt;iframe&gt;</code>, <code>&lt;object&gt;</code>, or <code>&lt;frame&gt;</code>, its parent is the window with the element embedding the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>parentWindow</i> = window.parent
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (window.parent != window.top) {
* // we're deeper than one down
* }
* </pre>
* <h2> <span> See also </span></h2>
* <ul><li> <code><a href="DOM:window.frameElement" shape="rect" title="DOM:window.frameElement">window.frameElement</a></code> returns the specific element (such as <code>&lt;iframe&gt;</code>) the <code>window</code> is embedded into.
* </li><li> <code><a href="window.top" shape="rect" title="DOM:window.top">window.top</a></code> returns a reference to the top-level window.
* </li></ul>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
parent: undefined,
/**
* <h2> <span> Summary</span></h2>
* <p>Returns the <b>personalbar</b> object, whose visibility can be toggled in the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>objRef</i> = <i>window</i>.personalbar
* </pre>
* <h2> <span> Example </span></h2>
* <p>The following complete HTML example shows the way that the visible property of the various "bar" objects is used, and also the change to the privileges necessary to write to the visible property of any of the bars on an existing window.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;Various DOM Tests&lt;/title&gt;
* &lt;script&gt;
* // changing bar states on the existing window
* netscape.security.PrivilegeManager.
* enablePrivilege("UniversalBrowserWrite");
* window.personalbar.visible=
*  !window.personalbar.visible;
* &lt;/script&gt;
* &lt;/head&gt;
* &lt;body&gt;
* &lt;p&gt;Various DOM Tests&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>To toggle the visibility of these bars, you must either sign your scripts or enable the appropriate privileges, as in the example above. Also be aware that dynamically updating the visibilty of the various toolbars can change the size of the window rather dramatically, and may affect the layout of your page.
* </p><p>See also: <a href="http://developer.mozilla.org/en/docs/window.locationbar" shape="rect" title="window.locationbar">window.locationbar</a>, <a href="http://developer.mozilla.org/en/docs/window.menubar" shape="rect" title="window.menubar">window.menubar</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollbars" shape="rect" title="window.scrollbars">window.scrollbars</a>, <a href="http://developer.mozilla.org/en/docs/window.statusbar" shape="rect" title="window.statusbar">window.statusbar</a>, <a href="http://developer.mozilla.org/en/docs/window.toolbar" shape="rect" title="window.toolbar">window.toolbar</a>
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
personalbar: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the bit depth of the screen.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">depth = window.screen.pixelDepth
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// if there is not adequate bit depth
* // choose a simpler color
* if ( window.screen.pixelDepth &gt; 8 ) {
* document.style.color = "#FAEBD7";
* } else {
* document.style.color = "#FFFFFF";
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>See also <a href="DOM:window.screen.colorDepth" shape="rect" title="DOM:window.screen.colorDepth">window.screen.colorDepth</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
pixelDepth: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the <b>pkcs11</b> object , which can be used to install drivers and other software associated with the <a href="http://developer.mozilla.org/en/docs/index.php?title=pkcs11_protocol&amp;action=edit" shape="rect" title="pkcs11 protocol">pkcs11 protocol</a>.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>objRef</i> = window.pkcs11
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.pkcs11.addModule(sMod, secPath, 0, 0);
* </pre>
* <h2> <span>Notes </span></h2>
* <p>See <a href="http://developer.mozilla.org/en/docs/index.php?title=nsIDOMPkcs11&amp;action=edit" shape="rect" title="nsIDOMPkcs11">nsIDOMPkcs11</a> for more information about how to manipulate pkcs11 objects.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
pkcs11: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Opens the Print Dialog to print the current document.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.print()
* </pre>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
print: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Displays a dialog with a message prompting the user to input some text.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>result</i> = window.prompt(<i>text</i>, <i>value</i>);
* </pre>
* <ul><li><code>result</code> is a string containing the text entered by the user, or the value null.
* </li><li><code>text</code> is a string of text to display to the user.
* </li><li><code>value</code> is a string containing the default value displayed in the text input field. It is an optional parameter.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var sign = prompt("What's your sign?");
* if (sign.toLowerCase() == "scorpio")
* alert("Wow! I'm a Scorpio too!");
* </pre>
* <p>When the user clicks the OK button, text entered in the input field is returned. If the user clicks OK without entering any text, an empty string is returned. If the user clicks the Cancel button, this function returns null.
* </p>
* <h2> <span> Notes </span></h2>
* <p>A prompt dialog contains a single-line textbox, a Cancel button, and an OK button, and returns the (possibly empty) text the user inputted into that textbox.
* </p><p>Dialog boxes are modal windows - they prevent the user from accessing the rest of the program's interface until the dialog box is closed. For this reason, you should not overuse any function that creates a dialog box (or modal window).
* </p><p>Chrome users (e.g. extensions) should use methods of <a href="http://developer.mozilla.org/en/docs/nsIPromptService" shape="rect" title="nsIPromptService">nsIPromptService</a> instead.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* <h2> <span> See also </span></h2>
* <p><a href="window.alert" shape="rect" title="DOM:window.alert">alert</a>, <a href="window.confirm" shape="rect" title="DOM:window.confirm">confirm</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
prompt: function(text, value) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary</span></h2>
* <div style="border: 1px solid #FF5151; background-color: #FEBCBC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Obsolete</p></div>
* <p>Releases the window from trapping events of a specific type.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.releaseEvents(<i>eventType</i>)
* </pre>
* <p><code>eventType</code> is a combination of the following values: <code>Event.ABORT</code>, <code>Event.BLUR</code>, <code>Event.CLICK</code>, <code>Event.CHANGE</code>, <code>Event.DBLCLICK</code>, <code>Event.DRAGDDROP</code>, <code>Event.ERROR</code>, <code>Event.FOCUS</code>, <code>Event.KEYDOWN</code>, <code>Event.KEYPRESS</code>, <code>Event.KEYUP</code>, <code>Event.LOAD</code>, <code>Event.MOUSEDOWN</code>, <code>Event.MOUSEMOVE</code>, <code>Event.MOUSEOUT</code>, <code>Event.MOUSEOVER</code>, <code>Event.MOUSEUP</code>, <code>Event.MOVE</code>, <code>Event.RESET</code>, <code>Event.RESIZE</code>, <code>Event.SELECT</code>, <code>Event.SUBMIT</code>, <code>Event.UNLOAD</code>.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.releaseEvents(Event.KEYPRESS)
* </pre>
* <h2> <span> Notes </span></h2>
* <div>
* <p>This method is obsolete as of Gecko 1.9, in favor of W3C DOM Events methods (see <a href="DOM:element.addEventListener" shape="rect" title="DOM:element.addEventListener">addEventListener</a>). The support for this method <a href="http://developer.mozilla.org/en/docs/Gecko_1.9_Changes_affecting_websites" shape="rect" title="Gecko 1.9 Changes affecting websites">has been removed</a> from <a href="http://developer.mozilla.org/en/docs/Gecko" shape="rect" title="Gecko">Gecko</a> 1.9.
* </p>
* </div>
* <p>Note that you can pass a list of events to this method using the following syntax: window.releaseEvents(Event.KEYPRESS | Event.KEYDOWN | Event.KEYUP).
* </p><p>See also <a href="window.captureEvents" shape="rect" title="DOM:window.captureEvents">window.captureEvents</a> (<span style="border: 1px solid #FF9999; background-color: #FFDBDB; font-size: 9px; vertical-align: text-top;">Obsolete</span>).
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
releaseEvents: function(eventType) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p><b>removeEventListener</b> allows the removal of event listeners from the event target.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.removeEventListener(<i>type</i>, <i>listener</i>, <i>useCapture</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <dl><dt style="font-weight:bold"><code>type</code> </dt><dd> A string representing the event type being registered.
* </dd><dt style="font-weight:bold"><code>listener</code> </dt><dd> The listener parameter takes an interface implemented by the user which contains the methods to be called when the event occurs.
* </dd><dt style="font-weight:bold"><code>useCapture</code> </dt><dd> If true, useCapture indicates that the user wishes to initiate capture. After initiating capture, all events of the specified type will be dispatched to the registered EventListener before being dispatched to any EventTargets beneath them in the tree. Events which are bubbling upward through the tree will not trigger an EventListener designated to use capture.
* </dd></dl>
* <h2> <span>Notes </span></h2>
* <p>If an EventListener is removed from an EventTarget while it is processing an event, it will not be triggered by the current actions. EventListeners can never be invoked after being removed.
* Calling removeEventListener with arguments which do not identify any currently registered EventListener on the EventTarget has no effect.
* See also <a href="http://developer.mozilla.org/en/docs/addEventListener" shape="rect" title="addEventListener">addEventListener</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Events-20001113/events.html#Events-EventTarget-removeEventListener" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Events-20001113/events.html#Events-EventTarget-removeEventListener">removeEventListener </a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
removeEventListener: function(type, listener, useCapture) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Resizes the current window by a certain amount.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.resizeBy(<i>xDelta</i>, <i>yDelta</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>xDelta</code> is the number of pixels to grow the window horizontally.
* </li><li><code>yDelta</code> is the number of pixels to grow the window vertically.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // shrink the window
* window.resizeBy(-200, -200);
* </pre>
* <h2> <span>Notes </span></h2>
* <p>This method resizes the window relative to its current size. To resize the window in absolute terms, use <a href="http://developer.mozilla.org/en/docs/window.resizeTo" shape="rect" title="window.resizeTo">window.resizeTo</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
resizeBy: function(xDelta, yDelta) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Dynamically resizes window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.resizeTo(<i>iWidth</i>, <i>iHeight</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>iWidth</code> is an integer representing the new width in pixels.
* </li><li><code>iHeight</code> is an integer value representing the new height in pixels.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // function resizes the window to take up half
* // of the available screen.
* function halve() {
* window.resizeTo(window.screen.availWidth/2,
* window.screen.availHeight/2);
* }
* </pre>
* <h2> <span>Notes </span></h2>
* <p>See also <a href="http://developer.mozilla.org/en/docs/window.resizeBy" shape="rect" title="window.resizeBy">window.resizeBy</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
resizeTo: function(iWidth, iHeight) {
  // This is just a stub for a builtin native JavaScript object.
},
returnValue: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a reference to the screen object associated with the window.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>screenObj</i> = <i>window</i>.screen
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (screen.pixelDepth &lt; 8) {
* // use low-color version of page
* } else {
* // use regular, colorful page
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The <code>screen</code> object is a special object for inspecting properties of the screen on which the current window is being rendered.
* </p><p>This object has the following properties:
* </p>
* <dl><dt style="font-weight:bold"> <a href="DOM:window.screen.availTop" shape="rect" title="DOM:window.screen.availTop">availTop</a>
* </dt><dd> Specifies the y-coordinate of the first pixel that is not allocated to permanent or semipermanent user interface features.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availLeft" shape="rect" title="DOM:window.screen.availLeft">availLeft</a>
* </dt><dd> Returns the first available pixel available from the left side of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availHeight" shape="rect" title="DOM:window.screen.availHeight">availHeight</a>
* </dt><dd> Specifies the height of the screen, in pixels, minus permanent or semipermanent user interface features displayed by the operating system, such as the Taskbar on Windows.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availWidth" shape="rect" title="DOM:window.screen.availWidth">availWidth</a>
* </dt><dd> Returns the amount of horizontal space in pixels available to the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.colorDepth" shape="rect" title="DOM:window.screen.colorDepth">colorDepth</a>
* </dt><dd> Returns the color depth of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.height" shape="rect" title="DOM:window.screen.height">height</a>
* </dt><dd> Returns the height of the screen in pixels.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.left" shape="rect" title="DOM:window.screen.left">left</a>
* </dt><dd> Returns the distance in pixels from the left side of the main screen to the left side of the current screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.pixelDepth" shape="rect" title="DOM:window.screen.pixelDepth">pixelDepth</a>
* </dt><dd> Returns the distance in pixels from the top side of the current screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.top" shape="rect" title="DOM:window.screen.top">top</a>
* </dt><dd> Gets/sets the distance from the top of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.width" shape="rect" title="DOM:window.screen.width">width</a>
* </dt><dd> Returns the width of the screen.
* </dd></dl>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
screen: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the horizontal distance of the left border of the user's browser from the left side of the screen.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>lLoc</i> = window.screenX
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>lLoc</code> is the number of pixels from the left side the screen.
* </li></ul>
* <h2> <span>Notes </span></h2>
* <p>See also <a href="http://developer.mozilla.org/en/docs/window.screenY" shape="rect" title="window.screenY">window.screenY</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
screenX: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the vertical distance of the top border of the user's browser from the top edge of the screen.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>lLoc</i> = window.screenY
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>lLoc</code> is the number of pixels from the top of the screen.
* </li></ul>
* <h2> <span>Notes </span></h2>
* <p>See also <a href="http://developer.mozilla.org/en/docs/window.screenX" shape="rect" title="window.screenX">window.screenX</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
screenY: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Scrolls the window to a particular place in the document.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.scroll(<i>x-coord</i>, <i>y-coord</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>x-coord</code> is the pixel along the horizontal axis of the document that you want displayed in the upper left.
* </li><li><code>y-coord</code> is the pixel along the vertical axis of the document that you want displayed in the upper left.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // put the 100th vertical pixel at the top of the window
* &lt;button onClick="scroll(0, 100);"&gt;click to scroll down 100 pixels&lt;/button&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p><a href="http://developer.mozilla.org/en/docs/window.scrollTo" shape="rect" title="window.scrollTo">window.scrollTo</a> is effectively the same as this method.
* For scrolling a particular distance repeatedly, use the <a href="http://developer.mozilla.org/en/docs/window.scrollBy" shape="rect" title="window.scrollBy">window.scrollBy</a>. Also see <a href="http://developer.mozilla.org/en/docs/window.scrollByLines" shape="rect" title="window.scrollByLines">window.scrollByLines</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollByPages" shape="rect" title="window.scrollByPages">window.scrollByPages</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scroll: function(x_coord, y_coord) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Scrolls the document in the window by the given amount.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.scrollBy(<i>X</i>,<i>Y</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>X</code> is the offset in pixels to scroll horizontally.
* </li><li><code>Y</code> is the offset in pixels to scroll vertically.
* </li></ul>
* <p>Positive co-ordinates will scroll to the right and down the page. Negative values will scroll to the left and up the page.
* </p>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // scroll one page
* window.scrollBy(0, window.innerHeight);
* </pre>
* <h2> <span>Notes </span></h2>
* <p><a href="http://developer.mozilla.org/en/docs/window.scrollBy" shape="rect" title="window.scrollBy">window.scrollBy</a> scrolls by a particular amount where <a href="http://developer.mozilla.org/en/docs/window.scroll" shape="rect" title="window.scroll">window.scroll</a> scrolls to an absolute position in the document.
* See also <a href="http://developer.mozilla.org/en/docs/window.scrollByLines" shape="rect" title="window.scrollByLines">window.scrollByLines</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollByPages" shape="rect" title="window.scrollByPages">window.scrollByPages</a>
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollBy: function(X,Y) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Scrolls the document by the given number of lines.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.scrollByLines(<i>lines</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>lines</code> is the number of lines to scroll the document by.
* </li><li><code>lines</code> may be a positive or negative integer.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// scroll down the document by 5 lines.
* &lt;button onclick="scrollByLines(5);"&gt;down 5 lines&lt;/button&gt;
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// scroll up the document by 5 lines.
* &lt;button onclick="scrollByLines(-5);"&gt;up 5 lines&lt;/button&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>See also <a href="http://developer.mozilla.org/en/docs/window.scrollBy" shape="rect" title="window.scrollBy">window.scrollBy</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollByPages" shape="rect" title="window.scrollByPages">window.scrollByPages</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollByLines: function(lines) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Scrolls the current document by the specified number of pages.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.scrollByPages(<i>pages</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>pages</code> is the number of pages to scroll.
* </li><li><code>pages</code> may be a positive or negative integer.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // scroll down the document by 1 page
* window.scrollByPages(1);
* 
* // scroll up the document by 1 page
* window.scrollByPages(-1);
* </pre>
* <h2> <span>Notes </span></h2>
* <p>See also <a href="http://developer.mozilla.org/en/docs/window.scrollBy" shape="rect" title="window.scrollBy">window.scrollBy</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollByLines" shape="rect" title="window.scrollByLines">window.scrollByLines</a>, <a href="http://developer.mozilla.org/en/docs/window.scroll" shape="rect" title="window.scroll">window.scroll</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollTo" shape="rect" title="window.scrollTo">window.scrollTo</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollByPages: function(pages) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the maximum number of pixels that the document can be scrolled horizontally.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>xpix</i> = window.scrollMaxX
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>xpix</code> is the number of pixels.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // Scroll to most right of the page
* var maxX = window.scrollMaxX;
* window.scrollTo(maxX, 0);
* </pre>
* <h2> <span>Notes </span></h2>
* <p>Use this property to get the total document width, which is equivalent to <a href="window.innerWidth" shape="rect" title="DOM:window.innerWidth">window.innerWidth</a> + window.scrollMaxX.
* </p><p>See also <a href="window.scrollMaxY" shape="rect" title="DOM:window.scrollMaxY">window.scrollMaxY</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollMaxX: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the maximum number of pixels that the document can be scrolled vertically.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>ypix</i> = window.scrollMaxY
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>ypix</code> is the number of pixels.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // Scroll to the bottom of the page
* var maxY = window.scrollMaxY;
* window.scrollTo(0,maxY);
* </pre>
* <h2> <span>Notes </span></h2>
* <p>Use this property to get the total document height, which is equivalent to window.innerHeight + window.scrollMaxY.
* </p><p>See also <a href="window.scrollMaxX" shape="rect" title="DOM:window.scrollMaxX">window.scrollMaxX</a> and <a href="window.scrollTo" shape="rect" title="DOM:window.scrollTo">window.scrollTo</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollMaxY: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Scrolls to a particular set of coordinates in the document.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.scrollTo(<i>x-coord</i>, <i>y-coord</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>x-coord</code> is the pixel along the horizontal axis of the document that you want displayed in the upper left.
* </li><li><code>y-coord</code> is the pixel along the vertical axis of the document that you want displayed in the upper left.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* window.scrollTo(0, 1000);
* </pre>
* <h2> <span>Notes </span></h2>
* <p>This function is effectively the same as <a href="http://developer.mozilla.org/en/docs/window.scroll" shape="rect" title="window.scroll">window.scroll</a>. For relative scrolling, see <a href="http://developer.mozilla.org/en/docs/window.scrollBy" shape="rect" title="window.scrollBy">window.scrollBy</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollByLines" shape="rect" title="window.scrollByLines">window.scrollByLines</a>, and <a href="http://developer.mozilla.org/en/docs/window.scrollByPages" shape="rect" title="window.scrollByPages">window.scrollByPages</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollTo: function(x_coord, y_coord) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the number of pixels that the document has already been scrolled horizontally.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>xpix</i> = window.scrollX
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>xpix</code> is the number of pixels.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // make sure and go over to the second horizontal page
* if (window.scrollX) {
* scroll(0,0); }
* scrollBy(400, 0);
* </pre>
* <h2> <span>Notes </span></h2>
* <p>Use this property to check that the document hasn't already been scrolled some if you are using relative scroll functions such as <a href="http://developer.mozilla.org/en/docs/window.scrollBy" shape="rect" title="window.scrollBy">window.scrollBy</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollByLines" shape="rect" title="window.scrollByLines">window.scrollByLines</a>, or <a href="http://developer.mozilla.org/en/docs/window.scrollByPages" shape="rect" title="window.scrollByPages">window.scrollByPages</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollX: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the number of pixels that the document has already been scrolled vertically.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">ypix = window.scrollY
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>ypix</code> is the number of pixels.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // make sure and go down to the second page
* if (window.scrollY) {
* scroll(0,0); }
* scrollByPages(1);
* 
* </pre>
* <h2> <span>Notes </span></h2>
* <p>Use this property to check that the document hasn't already been scrolled some if you are using relative scroll functions such as <a href="http://developer.mozilla.org/en/docs/window.scrollBy" shape="rect" title="window.scrollBy">window.scrollBy</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollByLines" shape="rect" title="window.scrollByLines">window.scrollByLines</a>, or <a href="http://developer.mozilla.org/en/docs/window.scrollByPages" shape="rect" title="window.scrollByPages">window.scrollByPages</a>
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollY: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the scrollbars object, whose visibility can be toggled in the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>objRef</i> = window.scrollbars
* </pre>
* <h2> <span>Example </span></h2>
* <p>The following complete HTML example shows way that the visible property of the various "bar" objects is used, and also the change to the privileges necessary to write to the visible property of any of the bars on an existing window.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;Various DOM Tests&lt;/title&gt;
* &lt;script&gt;
* // changing bar states on the existing window
* netscape.security.PrivilegeManager.
* enablePrivilege("UniversalBrowserWrite");
* window.menubar.visible=!window.menubar.visible;
* &lt;/script&gt;
* &lt;/head&gt;
* &lt;body&gt;
* &lt;p&gt;Various DOM Tests&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>Note that scrollbars is not an array of the scrollbars. The visibility of these objects can only be controlled as a group.
* To toggle the visibility of these bars, you must either sign your scripts or enable the appropriate privileges, as in the example above. Also be aware that dynamically updating the visibilty of the various toolbars can change the size of the window rather dramatically, and may affect the layout of your page.
* See also: <a href="http://developer.mozilla.org/en/docs/window.locationbar" shape="rect" title="window.locationbar">window.locationbar</a>, <a href="http://developer.mozilla.org/en/docs/window.menubar" shape="rect" title="window.menubar">window.menubar</a>, <a href="http://developer.mozilla.org/en/docs/window.personalbar" shape="rect" title="window.personalbar">window.personalbar</a>, <a href="http://developer.mozilla.org/en/docs/window.statusbar" shape="rect" title="window.statusbar">window.statusbar</a>, <a href="http://developer.mozilla.org/en/docs/window.toolbar" shape="rect" title="window.toolbar">window.toolbar</a>
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollbars: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns an object reference to the window object.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>objRef</i> = window.self
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* if (window.parent.frames[0] != window.self) {
* // this window is not the first frame in the list
* }
* </pre>
* <h2> <span>Notes </span></h2>
* <p><b>window.self</b> is almost always used in comparisons like in the example above, which finds out if the current window is the first subframe in the parent frameset.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
self: undefined,
sessionStorage: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Calls a function repeatedly, with a fixed time delay between each call to that function.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>intervalID</i> = window.setInterval(<i>func</i>, <i>delay</i>[, <i>param1</i>, <i>param2</i>, ...]);
* <i>intervalID</i> = window.setInterval(<i>code</i>, <i>delay</i>);
* </pre>
* <p>where
* </p>
* <ul><li> <code>intervalID</code> is a unique interval ID you can pass to <code><a href="DOM:window.clearInterval" shape="rect" title="DOM:window.clearInterval">clearInterval()</a></code>.
* </li><li> <code>func</code> is the <a href="http://developer.mozilla.org/en/docs/Function" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">function</a> you want to be called repeatedly.
* </li><li> <code>code</code> in the alternate syntax, is a string of code you want to be executed repeatedly.
* </li><li> <code>delay</code> is the number of milliseconds (thousandths of a second) that the <code>setInterval()</code> function should wait before each call to <code>func</code>.
* </li></ul>
* <p>Note that passing additional parameters to the function in the first syntax does not work in Internet Explorer.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var intervalID = window.setInterval(animate, 500);
* </pre>
* <p>The following example will continue to call the <code>flashtext()</code> function once a second, until you clear the <code>intervalID</code> by clicking the Stop button.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;setInterval/clearInterval example&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* var intervalID;
* 
* function changeColor()
* {
* intervalID = setInterval(flashText, 1000);
* }
* 
* function flashText()
* {
* var elem = document.getElementById("my_box");
* if (elem.style.color == 'red')
* {
* elem.style.color = 'blue';
* }
* else
* {
* elem.style.color = 'red';
* }
* }
* 
* function stopTextColor()
* {
* clearInterval(intervalID);
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body onload="changeColor();"&gt;
* &lt;div id="my_box"&gt;
* &lt;p&gt;Hello World&lt;/p&gt;
* &lt;/div&gt;
* &lt;button onclick="stopTextColor();"&gt;Stop&lt;/button&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The <code>setInterval()</code> function is commonly used to set a delay for functions that are executed again and again, such as animations.
* </p><p>You can cancel the interval using <code><a href="window.clearInterval" shape="rect" title="DOM:window.clearInterval">window.clearInterval()</a></code>.
* </p><p>If you wish to have your function called <i>once</i> after the specified delay, use <code><a href="window.setTimeout" shape="rect" title="DOM:window.setTimeout">window.setTimeout()</a></code>.
* </p>
* <h3> <span> The 'this' problem </span></h3>
* <p>When you pass a method to <code>setInterval()</code> (or any other function, for that matter), it will be invoked with a wrong <code>this</code> value. This problem is explained in detail in the <a href="http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Operators:Special_Operators:this_Operator#Method_binding" shape="rect" title="Core JavaScript 1.5 Reference:Operators:Special Operators:this Operator">JavaScript reference</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
setInterval: function(func, delay, param1, param2) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Executes a code snippet or a function after specified delay.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>timeoutID</i> = window.setTimeout(<i>func</i>, <i>delay</i>[, <i>param1</i>, <i>param2</i>, ...]);
* <i>timeoutID</i> = window.setTimeout(<i>code</i>, <i>delay</i>);
* </pre>
* <p>where
* </p>
* <ul><li> <code>timeoutID</code> is the ID of the timeout, which can be used with <a href="DOM:window.clearTimeout" shape="rect" title="DOM:window.clearTimeout">window.clearTimeout</a>.
* </li><li> <code>func</code> is the <a href="http://developer.mozilla.org/en/docs/Function" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">function</a> you want to execute after <code>delay</code> milliseconds.
* </li><li> <code>code</code> in the alternate syntax, is a string of code you want to execute after <code>delay</code> milliseconds.
* </li><li> <code>delay</code> is the number of milliseconds (thousandths of a second) that the function call should be delayed by.
* </li></ul>
* <p>Note that passing additional parameters to the function in the first syntax does not work in Internet Explorer.
* </p>
* <h2> <span> Compatibility </span></h2>
* <p>Introduced with JavaScript 1.0, Netscape 2.0.  Passing a Function object reference was introduced with JavaScript 1.2, Netscape 4.0; supported by the MSHTML DOM since version 5.0.
* </p>
* <h2> <span> Examples </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.setTimeout('window.parent.generateOutput()', 1000);
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function generateOutput(aConcise) {
* if(aConcise)
* parent.generateConciseOutput();
* else
* parent.generateOutput();
* }
* window.setTimeout(generateOutput, 1000, true);
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;setTimeout example&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* function delayedAlert()
* {
* timeoutID = window.setTimeout(slowAlert, 2000);
* }
* 
* function slowAlert()
* {
* alert("That was really slow!");
* }
* 
* function clearAlert()
* {
* window.clearTimeout(timeoutID);
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;button onclick="delayedAlert();"
* &gt;show an alert box after 2 seconds&lt;/button&gt;&lt;br&gt;
* &lt;button onclick="clearAlert();"&gt;Cancel&lt;/button&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <p>See also <a href="window.clearTimeout#Example" shape="rect" title="DOM:window.clearTimeout">clearTimeout() example</a>.
* </p>
* <h2> <span> Notes </span></h2>
* <p>You can cancel the timeout using <code><a href="DOM:window.clearTimeout" shape="rect" title="DOM:window.clearTimeout">window.clearTimeout()</a></code>.
* </p><p>If you wish to have your function called repeatedly (i.e. every N milliseconds), consider using <code><a href="window.setInterval" shape="rect" title="DOM:window.setInterval">window.setInterval()</a></code>.
* </p>
* <h3> <span> The 'this' problem </span></h3>
* <p>Code executed by <code>setTimeout()</code> is run in a separate execution context to the function from which it was called.  As a consequence, the <code>this</code> keyword for the called function will be set to the <code>window</code> (or <code>global</code>) object, it will not be the same as the <code>this</code> value for the function that called <code>setTimeout</code>. This issue is explained in more detail in the <a href="http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Operators:Special_Operators:this_Operator#Method_binding" shape="rect" title="Core JavaScript 1.5 Reference:Operators:Special Operators:this Operator">JavaScript reference</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
setTimeout: function(func, delay, param1, param2) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #818151; background-color: #FFFFE1; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">This article covers features introduced in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Creates and displays a modal dialog box containing a specified HTML document.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>returnVal</i> = window.showModalDialog(<i>uri</i>[, <i>arguments</i>][, <i>options</i>]);
* </pre>
* <p>where
* </p>
* <ul><li> <code>returnVal</code> is a variant, indicating the returnValue property as set by the window of the document specified by <code>uri</code>.
* </li><li> <code>uri</code> is the URI of the document to display in the dialog box.
* </li><li> <code>arguments</code> is an optional variant that contains values that should be passed to the dialog box; these are made available in the <code><a href="window" shape="rect" title="DOM:window">window</a></code> object's <code><a href="http://developer.mozilla.org/en/docs/index.php?title=window.dialogArguments&amp;action=edit" shape="rect" title="DOM:window.dialogArguments">window.dialogArguments</a></code> property.
* </li><li> <code>options</code> an optional string that specifies window ornamentation for the dialog box, using one or more semicolon delimited values:
* </li></ul>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* 
* <tr>
* <th colspan="1" rowspan="1">Syntax
* </th><th colspan="1" rowspan="1">Description
* </th></tr>
* <tr>
* <td colspan="1" rowspan="1"><code>center: {on | off | yes | no | 1 | 0 }</code>
* </td><td colspan="1" rowspan="1">If this argument's value is <code>on</code>, <code>yes</code>, or 1, the dialog window is centered on the desktop; otherwise it's hidden.  The default value is <code>yes</code>.
* </td></tr>
* <tr>
* <td colspan="1" rowspan="1"><code>dialogheight: <i>height</i></code>
* </td><td colspan="1" rowspan="1">Specifies the height of the dialog box; by default, the size is specified in pixels.
* </td></tr>
* <tr>
* <td colspan="1" rowspan="1"><code>dialogleft: <i>left</i></code>
* </td><td colspan="1" rowspan="1">Specifies the horizontal position of the dialog box in relation to the upper-left corner of the desktop.
* </td></tr>
* <tr>
* <td colspan="1" rowspan="1"><code>dialogwidth: <i>width</i></code>
* </td><td colspan="1" rowspan="1">Specifies the width of the dialog box; by default, the size is specified in pixels.
* </td></tr>
* <tr>
* <td colspan="1" rowspan="1"><code>dialogtop: <i>top</i></code>
* </td><td colspan="1" rowspan="1">Specifies the vertical position of the dialog box in relation to the upper-left corner of the desktop.
* </td></tr>
* <tr>
* <td colspan="1" rowspan="1"><code>resizable: {on | off | yes | no | 1 | 0 }</code>
* </td><td colspan="1" rowspan="1">If this argument's value is <code>on</code>, <code>yes</code>, or 1, the dialog window can be resized by the user; otherwise its size is fixed.  The default value is <code>no</code>.
* </td></tr>
* <tr>
* <td colspan="1" rowspan="1"><code>scroll: {on | off | yes | no | 1 | 0 }</code>
* </td><td colspan="1" rowspan="1">If this argument's value is <code>on</code>, <code>yes</code>, or 1, the dialog window has scroll bars; otherwise its size is fixed.  The default value is <code>no</code>.
* </td></tr>
* </table>
* <blockquote><div><b>Note:</b> Firefox does not implement the <code>dialogHide</code>, <code>edge</code>, <code>status</code>, or <code>unadorned</code> arguments.</div></blockquote>
* <h2> <span> Compatibility </span></h2>
* <p>Introduced by Microsoft Internet Explorer 4.  Support added to Firefox in Firefox 3.
* </p>
* <h2> <span> Examples </span></h2>
* <p><a href="http://developer.mozilla.org/samples/domref/showModalDialog.html" rel="nofollow" shape="rect" title="http://developer.mozilla.org/samples/domref/showModalDialog.html">Try out <code>showModalDialog()</code></a>.
* </p>
* <h2> <span> Notes </span></h2>
* <h2> <span> Specification </span></h2>
* <p>Microsoft MSDN: <a href="http://msdn2.microsoft.com/en-us/library/ms536759.aspx" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/ms536759.aspx">showModalDialog</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
showModalDialog: function(uri, arguments, options) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a sidebar object, which contains several methods for registering add-ons with browser.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.sidebar.addPanel("Google", "http://www.google.com/", "");
* </pre>
* <p>Note: the third empty parameter is required!
* </p>
* <h2> <span> Notes </span></h2>
* <p>The sidebar object returned has the following methods:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Method</th>
* <th colspan="1" rowspan="1">Description</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">addPanel(title, contentURL, customizeURL)</td>
* 
* </tr>
* <tr>
* <td colspan="1" rowspan="1">addPersistentPanel(title, contentURL, customizeURL)</td>
* 
* </tr>
* <tr>
* <td colspan="1" rowspan="1">addSearchEngine(engineURL, iconURL, suggestedTitle, suggestedCategory)</td>
* <td colspan="1" rowspan="1">Installs a Sherlock search engine. See <a href="http://developer.mozilla.org/en/docs/Adding_search_engines_from_web_pages" shape="rect" title="Adding search engines from web pages">Adding search engines from web pages</a> for details.</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">addMicrosummaryGenerator(generatorURL)</td>
* <td colspan="1" rowspan="1">Installs a microsummary generator. New in <a href="http://developer.mozilla.org/en/docs/Firefox_2" shape="rect" title="Firefox 2">Firefox 2</a></td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p>Mozilla-specific. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
sidebar: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Sizes the window according to its content.
* </p><p>The DOM content should be loaded when this function is called, for example after the <a href="http://developer.mozilla.org/en/docs/Gecko-Specific_DOM_Events#DOMContentLoaded" shape="rect" title="Gecko-Specific DOM Events">DOMContentLoaded</a> event.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.sizeToContent()
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.sizeToContent();
* </pre>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
sizeToContent: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Sets the text in the status bar at the bottom of the browser or returns the previously set text.
* </p><p>This property does not work in default configuration of Firefox and some other browsers: setting <code>window.status</code> has no effect on the text displayed in the status bar. To allow scripts change the the status bar text, the user must set the <code>dom.disable_window_status_change</code> preference to <code>false</code> in the <tt>about:config</tt> screen.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>string</i> = <i>window</i>.status;
* <i>window</i>.status = <i>string</i>;
* </pre>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
status: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the statusbar object, whose visibility can be toggled in the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>objRef</i> = window.statusbar
* </pre>
* <h2> <span>Example </span></h2>
* <p>The following complete HTML example shows way that the visible property of the various "bar" objects is used, and also the change to the privileges necessary to write to the visible property of any of the bars on an existing window.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;Various DOM Tests&lt;/title&gt;
* &lt;script&gt;
* // changing bar states on the existing window
* netscape.security.PrivilegeManager.
* enablePrivilege("UniversalBrowserWrite");
* window.statusbar.visible=!window.statusbar.visible;
* &lt;/script&gt;
* &lt;/head&gt;
* &lt;body&gt;
* &lt;p&gt;Various DOM Tests&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>When you load the example page above, the browser displays the following dialog:
* </p><p>To toggle the visibility of these bars, you must either sign your scripts or enable the appropriate privileges, as in the example above. Also be aware that dynamically updating the visibilty of the various toolbars can change the size of the window rather dramatically, and may affect the layout of your page.
* See also: <a href="http://developer.mozilla.org/en/docs/window.locationbar" shape="rect" title="window.locationbar">window.locationbar</a>, <a href="http://developer.mozilla.org/en/docs/window.menubar" shape="rect" title="window.menubar">window.menubar</a>, <a href="http://developer.mozilla.org/en/docs/window.personalbar" shape="rect" title="window.personalbar">window.personalbar</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollbars" shape="rect" title="window.scrollbars">window.scrollbars</a>, <a href="http://developer.mozilla.org/en/docs/window.toolbar" shape="rect" title="window.toolbar">window.toolbar</a>
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
statusbar: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>This method stops window loading.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.stop()
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.stop();
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The stop() method is exactly equivalent to clicking the stop button in the browser. Because of the order in which scripts are loaded, the stop() method cannot stop the document in which it is contained from loading, but it will stop the loading of large images, new windows, and other objects whose loading is deferred.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
stop: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the toolbar object, whose visibility can be toggled in the window.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>objRef</i> = window.toolbar
* </pre>
* <h2> <span>Example </span></h2>
* <p>The following complete HTML example shows way that the visible property of the various "bar" objects is used, and also the change to the privileges necessary to write to the visible property of any of the bars on an existing window.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;Various DOM Tests&lt;/title&gt;
* &lt;script&gt;
* // changing bar states on the existing window
* netscape.security.PrivilegeManager.enablePrivilege("UniversalBrowserWrite");
* window.toolbar.visible=!window.toolbar.visible;
* &lt;/script&gt;
* &lt;/head&gt;
* &lt;body&gt;
* &lt;p&gt;Various DOM Tests&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>To toggle the visibility of these bars, you must either sign your scripts or enable the appropriate privileges, as in the example above. Also be aware that dynamically updating the visibilty of the various toolbars can change the size of the window rather dramatically, and may affect the layout of your page.
* See also: <a href="http://developer.mozilla.org/en/docs/window.locationbar" shape="rect" title="window.locationbar">window.locationbar</a>, <a href="http://developer.mozilla.org/en/docs/window.menubar" shape="rect" title="window.menubar">window.menubar</a>, <a href="http://developer.mozilla.org/en/docs/window.personalbar" shape="rect" title="window.personalbar">window.personalbar</a>, <a href="http://developer.mozilla.org/en/docs/window.scrollbars" shape="rect" title="window.scrollbars">window.scrollbars</a>, <a href="http://developer.mozilla.org/en/docs/window.statusbar" shape="rect" title="window.statusbar">window.statusbar</a>
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
toolbar: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a reference to the topmost window in the window hierarchy.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>topWindow</i> = window.top;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Where the <code><a href="window.parent" shape="rect" title="DOM:window.parent">window.parent</a></code> property returns the immediate parent of the current window, <code>window.top</code> returns the topmost window in the hierarchy of window objects.
* </p><p>This property is especially useful when you are dealing with a window that is in a subframe of a parent or parents, and you want to get to the top-level frameset.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
top: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Decodes a value that has been encoded in hexadecimal (e.g., a cookie).
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>regular</i> = window.unescape(<i>escaped</i>)
* </pre>
* <ul><li><code>regular</code> is the decoded string.
* </li><li><code>encoded</code> is the encoded string.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">alert(unescape("%5C")); // displays "\"
* </pre>
* <h2> <span> Notes </span></h2>
* <p>See also <a href="DOM:window.escape" shape="rect" title="DOM:window.escape">escape</a>, <a href="http://developer.mozilla.org/en/docs/decodeURIComponent" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:decodeURIComponent">decodeURIComponent</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard. Mentioned in a non-normative section of ECMA-262.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
unescape: function(escaped) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Updates the state of commands of the current chrome window (UI).
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.updateCommands("sCommandName")
* </pre>
* <h2> <span> Parameters </span></h2>
* <ul><li> <code>sCommandName</code> is a particular string which describes what kind of update event this is (e.g. whether we are in bold right now).
* </li></ul>
* <h2> <span> Notes </span></h2>
* <p>This enables or disables items (setting or clearing the "disabled" attribute on the command node as appropriate), or ensures that the command state reflects the state of the selection by setting current state information in the "state" attribute of the XUL command nodes.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
updateCommands: function(sCommandName) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the width of the screen.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">lWidth = window.screen.width
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// crude way to check that the screen is at 1024x768
* if (window.screen.width &gt; 1000) {
* // resolution is below 10 x 7
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Note that not all of the width given by this property may be available to the window itself. When other widgets occupy space that cannot be used by the <code>window</code> object, there is a difference in <code>window.screen.width</code> and <code>window.screen.availWidth</code>.
* See also <a href="DOM:window.screen.height" shape="rect" title="DOM:window.screen.height">window.screen.height</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
width: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>The <code>window</code> property of a window object points to the window object itself. Thus the following expressions all return the same window object:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">window.window
* window.window.window
* window.window.window.window
* ...
* </pre>
* <p>In web pages, the window object is also a <i>global object</i>. This means that:
* </p>
* <ol>
* <li>global variables of your script are in fact properties of <code>window</code>:
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var global = {data: 0};
* alert(global === window.global); // displays "true"
* </pre></li>
* <li>you can access built-in properties of the window object without having to type <code>window.</code> prefix:
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* setTimeout("alert('Hi!')", 50); // equivalent to using window.setTimeout.
* alert(window === window.window); // displays "true"
* </pre></li></ol>
* <p>The point of having the <code>window</code> property refer to the object itself was (probably) to make it easy to refer to the global object (otherwise you'd have to do a manual <code>var window = this;</code> assignment at the top of your script).
* </p><p>Another reason is that without this property you wouldn't be able to write, for example, "<a href="window.open" shape="rect" title="DOM:window.open">window.open('http://google.com/')</a>" - you'd have to just use "open('http://google.com/')" instead.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
window: undefined,
};

/**
* <p>This section provides a brief reference for all of the methods, properties, and events available through the DOM <code>window</code> object. The <code>window</code> object implements the <code>Window</code> interface, which in turn inherits from the <code><a href="http://www.w3.org/TR/DOM-Level-2-Views/views.html#Views-AbstractView" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Views/views.html#Views-AbstractView">AbstractView</a></code> interface.
* </p><p>The <code>window</code> object represents the window itself. The <code>document</code> property of a <code>window</code> points to the <a href="document" shape="rect" title="DOM:document">DOM document</a> loaded in that window. A window for a given document can be obtained using the <code><a href="document.defaultView" shape="rect" title="DOM:document.defaultView">document.defaultView</a></code> property.
* </p><p>In a tabbed browser, such as Firefox, each tab contains its own <code>window</code> object (and if you're writing an extension, the browser window itself is a separate window too - see <a href="http://developer.mozilla.org/en/docs/Working_with_windows_in_chrome_code#Content_windows" shape="rect" title="Working with windows in chrome code">Working with windows in chrome code</a> for more information). That is, the <code>window</code> object is not shared between tabs in the same window. Some methods, namely <code><a href="window.resizeTo" shape="rect" title="DOM:window.resizeTo">window.resizeTo</a></code> and <code><a href="window.resizeBy" shape="rect" title="DOM:window.resizeBy">window.resizeBy</a></code> apply to the whole window and not to the specific tab the <code>window</code> object belongs to. Generally, anything that can't reasonably pertain to a tab pertains to the window instead.
* </p>
* <h2> <span> Properties </span></h2>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/Components_object" shape="rect" title="Components object">window.Components</a>
* </dt><dd> The entry point to many <a href="http://developer.mozilla.org/en/docs/XPCOM" shape="rect" title="XPCOM">XPCOM</a> features. Some properties, e.g. <a href="http://developer.mozilla.org/en/docs/Components.classes" shape="rect" title="Components.classes">classes</a>, are only available to sufficiently privileged code.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.content" shape="rect" title="DOM:window.content">window.content</a> and window._content
* </dt><dd> Returns a reference to the content element in the current window. The variant with underscore is deprecated.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.closed" shape="rect" title="DOM:window.closed">window.closed</a>
* </dt><dd> This property indicates whether the current window is closed or not.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.controllers" shape="rect" title="DOM:window.controllers">window.controllers</a>
* </dt><dd> Returns the XUL controller objects for the current chrome window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.crypto" shape="rect" title="DOM:window.crypto">window.crypto</a>
* </dt><dd> Returns the browser crypto object.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.defaultStatus" shape="rect" title="DOM:window.defaultStatus">window.defaultStatus</a>
* </dt><dd> Gets/sets the status bar text for the given window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.dialogArguments&amp;action=edit" shape="rect" title="DOM:window.dialogArguments">window.dialogArguments</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> Gets the arguments passed to the window (if it's a dialog box) at the time <code><a href="window.showModalDialog" shape="rect" title="DOM:window.showModalDialog">window.showModalDialog()</a></code> was called.  This is an <code><a href="http://developer.mozilla.org/en/docs/nsIArray" shape="rect" title="nsIArray">nsIArray</a></code>.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.directories" shape="rect" title="DOM:window.directories">window.directories</a>
* </dt><dd> Returns a reference to the directories toolbar in the current chrome.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.document" shape="rect" title="DOM:window.document">window.document</a>
* </dt><dd> Returns a reference to the document that the window contains.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.frameElement" shape="rect" title="DOM:window.frameElement">window.frameElement</a>
* </dt><dd> Returns the element in which the window is embedded, or null if the window is not embedded.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.frames" shape="rect" title="DOM:window.frames">window.frames</a>
* </dt><dd> Returns an array of the subframes in the current window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.fullScreen" shape="rect" title="DOM:window.fullScreen">window.fullScreen</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> This property indicates whether the window is displayed in full screen or not.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="Storage#globalStorage" shape="rect" title="DOM:Storage">window.globalStorage</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_2_for_developers" shape="rect" title="Firefox 2 for developers">Firefox 2</a></span>
* </dt><dd> Multiple storage objects that are used for storing data across multiple pages.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.history" shape="rect" title="DOM:window.history">window.history</a>
* </dt><dd> Returns a reference to the history object.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.innerHeight" shape="rect" title="DOM:window.innerHeight">window.innerHeight</a>
* </dt><dd> Gets the height of the content area of the browser window including, if rendered, the horizontal scrollbar.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.innerWidth" shape="rect" title="DOM:window.innerWidth">window.innerWidth</a>
* </dt><dd> Gets the width of the content area of the browser window including, if rendered, the vertical scrollbar.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.length" shape="rect" title="DOM:window.length">window.length</a>
* </dt><dd> Returns the number of frames in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.location" shape="rect" title="DOM:window.location">window.location</a>
* </dt><dd> Gets/sets the location, or current URL, of the window object.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.locationbar" shape="rect" title="DOM:window.locationbar">window.locationbar</a>
* </dt><dd> Returns the locationbar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.menubar" shape="rect" title="DOM:window.menubar">window.menubar</a>
* </dt><dd> Returns the menubar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.name" shape="rect" title="DOM:window.name">window.name</a>
* </dt><dd> Gets/sets the name of the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.navigator" shape="rect" title="DOM:window.navigator">window.navigator</a>
* </dt><dd> Returns a reference to the navigator object.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.opener" shape="rect" title="DOM:window.opener">window.opener</a>
* </dt><dd> Returns a reference to the window that opened this current window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.outerHeight" shape="rect" title="DOM:window.outerHeight">window.outerHeight</a>
* </dt><dd> Gets the height of the outside of the browser window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.outerWidth" shape="rect" title="DOM:window.outerWidth">window.outerWidth</a>
* </dt><dd> Gets the width of the outside of the browser window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.pageXOffset" shape="rect" title="DOM:window.pageXOffset">window.pageXOffset</a>
* </dt><dd> Gets the amount of content that has been hidden by scrolling to the right.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.pageYOffset" shape="rect" title="DOM:window.pageYOffset">window.pageYOffset</a>
* </dt><dd> Gets the amount of content that has been hidden by scrolling down.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.parent" shape="rect" title="DOM:window.parent">window.parent</a>
* </dt><dd> Returns a reference to the parent of the current window or subframe.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.personalbar" shape="rect" title="DOM:window.personalbar">window.personalbar</a>
* </dt><dd> Returns the personalbar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.pkcs11" shape="rect" title="DOM:window.pkcs11">window.pkcs11</a>
* </dt><dd> Returns the pkcs11 object, which can be used to install drivers and other software associated with the pkcs11 protocol.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.returnValue&amp;action=edit" shape="rect" title="DOM:window.returnValue">window.returnValue</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> The return value to be returned to the function that called <code><a href="window.showModalDialog" shape="rect" title="DOM:window.showModalDialog">window.showModalDialog()</a></code> to display the window as a modal dialog.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen" shape="rect" title="DOM:window.screen">window.screen</a>
* </dt><dd> Returns a reference to the screen object associated with the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availTop" shape="rect" title="DOM:window.screen.availTop">window.screen.availTop</a>
* </dt><dd> Specifies the y-coordinate of the first pixel that is not allocated to permanent or semipermanent user interface features.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availLeft" shape="rect" title="DOM:window.screen.availLeft">window.screen.availLeft</a>
* </dt><dd> Returns the first available pixel available from the left side of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availHeight" shape="rect" title="DOM:window.screen.availHeight">window.screen.availHeight</a>
* </dt><dd> Specifies the height of the screen, in pixels, minus permanent or semipermanent user interface features displayed by the operating system, such as the Taskbar on Windows.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.availWidth" shape="rect" title="DOM:window.screen.availWidth">window.screen.availWidth</a>
* </dt><dd> Returns the amount of horizontal space in pixels available to the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.colorDepth" shape="rect" title="DOM:window.screen.colorDepth">window.screen.colorDepth</a>
* </dt><dd> Returns the color depth of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.height" shape="rect" title="DOM:window.screen.height">window.screen.height</a>
* </dt><dd> Returns the height of the screen in pixels.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.left" shape="rect" title="DOM:window.screen.left">window.screen.left</a>
* </dt><dd> Returns the current distance in pixels from the left side of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.pixelDepth" shape="rect" title="DOM:window.screen.pixelDepth">window.screen.pixelDepth</a>
* </dt><dd> Gets the bit depth of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.top" shape="rect" title="DOM:window.screen.top">window.screen.top</a>
* </dt><dd> Returns the distance from the top of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screen.width" shape="rect" title="DOM:window.screen.width">window.screen.width</a>
* </dt><dd> Returns the width of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screenX" shape="rect" title="DOM:window.screenX">window.screenX</a>
* </dt><dd> Returns the horizontal distance of the left border of the user's browser from the left side of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.screenY" shape="rect" title="DOM:window.screenY">window.screenY</a>
* </dt><dd> Returns the vertical distance of the top border of the user's browser from the top side of the screen.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollbars" shape="rect" title="DOM:window.scrollbars">window.scrollbars</a>
* </dt><dd> Returns the scrollbars object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollMaxX" shape="rect" title="DOM:window.scrollMaxX">window.scrollMaxX</a>
* </dt><dd> The maximum offset that the window can be scrolled to horizontally.
* </dd><dd> (i.e., the document width minus the viewport width)
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollMaxY" shape="rect" title="DOM:window.scrollMaxY">window.scrollMaxY</a>
* </dt><dd> The maximum offset that the window can be scrolled to vertically.
* </dd><dd> (i.e., the document height minus the viewport height)
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollX" shape="rect" title="DOM:window.scrollX">window.scrollX</a>
* </dt><dd> Returns the number of pixels that the document has already been scrolled horizontally.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollY" shape="rect" title="DOM:window.scrollY">window.scrollY</a>
* </dt><dd> Returns the number of pixels that the document has already been scrolled vertically.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.self" shape="rect" title="DOM:window.self">window.self</a>
* </dt><dd> Returns an object reference to the window object itself.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="Storage#sessionStorage" shape="rect" title="DOM:Storage">window.sessionStorage</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> A storage object for storing data within a single page session.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.sidebar" shape="rect" title="DOM:window.sidebar">window.sidebar</a>
* </dt><dd> Returns a reference to the window object of the sidebar.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.status" shape="rect" title="DOM:window.status">window.status</a>
* </dt><dd> Gets/sets the text in the statusbar at the bottom of the browser.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.statusbar" shape="rect" title="DOM:window.statusbar">window.statusbar</a>
* </dt><dd> Returns the statusbar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.toolbar" shape="rect" title="DOM:window.toolbar">window.toolbar</a>
* </dt><dd> Returns the toolbar object, whose visibility can be toggled in the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.top" shape="rect" title="DOM:window.top">window.top</a>
* </dt><dd> Returns a reference to the topmost window in the window hierarchy.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.window" shape="rect" title="DOM:window.window">window.window</a>
* </dt><dd> Returns a reference to the current window.
* </dd></dl>
* <h2> <span> Methods </span></h2>
* <dl><dt style="font-weight:bold"> <a href="DOM:window.alert" shape="rect" title="DOM:window.alert">window.alert</a>
* </dt><dd> Displays an alert dialog.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.addEventListener" shape="rect" title="DOM:element.addEventListener">window.addEventListener</a>
* </dt><dd> Register an event handler to a specific event type on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.atob" shape="rect" title="DOM:window.atob">window.atob</a>
* </dt><dd> Decodes a string of data which has been encoded using base-64 encoding.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.back" shape="rect" title="DOM:window.back">window.back</a>
* </dt><dd> Moves back one in the window history.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.blur" shape="rect" title="DOM:window.blur">window.blur</a>
* </dt><dd> Sets focus away from the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.btoa" shape="rect" title="DOM:window.btoa">window.btoa</a>
* </dt><dd> Creates a base-64 encoded ASCII string from a string of binary data.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.captureEvents" shape="rect" title="DOM:window.captureEvents">window.captureEvents</a> <span style="border: 1px solid #FF9999; background-color: #FFDBDB; font-size: 9px; vertical-align: text-top;">Obsolete</span>
* </dt><dd> Registers the window to capture all events of the specified type.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.clearInterval" shape="rect" title="DOM:window.clearInterval">window.clearInterval</a>
* </dt><dd> Cancels the repeated execution set using <code>setInterval</code>.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.clearTimeout" shape="rect" title="DOM:window.clearTimeout">window.clearTimeout</a>
* </dt><dd> Clears a delay that's been set for a specific function.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.close" shape="rect" title="DOM:window.close">window.close</a>
* </dt><dd> Closes the current window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.confirm" shape="rect" title="DOM:window.confirm">window.confirm</a>
* </dt><dd> Displays a dialog with a message that the user needs to respond to.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.dump" shape="rect" title="DOM:window.dump">window.dump</a>
* </dt><dd> Writes a message to the console.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.escape" shape="rect" title="DOM:window.escape">window.escape</a>
* </dt><dd> Encodes a string.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.find" shape="rect" title="DOM:window.find">window.find</a>
* </dt><dd> Searches for a given string in a window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.focus" shape="rect" title="DOM:window.focus">window.focus</a>
* </dt><dd> Sets focus on the current window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.forward" shape="rect" title="DOM:window.forward">window.forward</a>
* </dt><dd> Moves the window one document forward in the history.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.getAttention" shape="rect" title="DOM:window.getAttention">window.getAttention</a>
* </dt><dd> Flashes the application icon.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.getComputedStyle" shape="rect" title="DOM:window.getComputedStyle">window.getComputedStyle</a>
* </dt><dd> Gets computed style for the specified element. Computed style indicates the computed values of all CSS properties of the element.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.getSelection" shape="rect" title="DOM:window.getSelection">window.getSelection</a>
* </dt><dd> Returns the selection object representing the selected item(s).
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.home" shape="rect" title="DOM:window.home">window.home</a>
* </dt><dd> Returns the browser to the home page.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.moveBy" shape="rect" title="DOM:window.moveBy">window.moveBy</a>
* </dt><dd> Moves the current window by a specified amount.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.moveTo" shape="rect" title="DOM:window.moveTo">window.moveTo</a>
* </dt><dd> Moves the window to the specified coordinates.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.open" shape="rect" title="DOM:window.open">window.open</a>
* </dt><dd> Opens a new window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.openDialog" shape="rect" title="DOM:window.openDialog">window.openDialog</a>
* </dt><dd> Opens a new dialog window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.print" shape="rect" title="DOM:window.print">window.print</a>
* </dt><dd> Opens the Print Dialog to print the current document.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.prompt" shape="rect" title="DOM:window.prompt">window.prompt</a>
* </dt><dd> Returns the text entered by the user in a prompt dialog.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.releaseEvents" shape="rect" title="DOM:window.releaseEvents">window.releaseEvents</a> <span style="border: 1px solid #FF9999; background-color: #FFDBDB; font-size: 9px; vertical-align: text-top;">Obsolete</span>
* </dt><dd> Releases the window from trapping events of a specific type.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.removeEventListener" shape="rect" title="DOM:element.removeEventListener">window.removeEventListener</a>
* </dt><dd> Removes an event listener from the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.resizeBy" shape="rect" title="DOM:window.resizeBy">window.resizeBy</a>
* </dt><dd> Resizes the current window by a certain amount.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.resizeTo" shape="rect" title="DOM:window.resizeTo">window.resizeTo</a>
* </dt><dd> Dynamically resizes window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scroll" shape="rect" title="DOM:window.scroll">window.scroll</a>
* </dt><dd> Scrolls the window to a particular place in the document.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollBy" shape="rect" title="DOM:window.scrollBy">window.scrollBy</a>
* </dt><dd> Scrolls the document in the window by the given amount.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollByLines" shape="rect" title="DOM:window.scrollByLines">window.scrollByLines</a>
* </dt><dd> Scrolls the document by the given number of lines.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollByPages" shape="rect" title="DOM:window.scrollByPages">window.scrollByPages</a>
* </dt><dd> Scrolls the current document by the specified number of pages.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.scrollTo" shape="rect" title="DOM:window.scrollTo">window.scrollTo</a>
* </dt><dd> Scrolls to a particular set of coordinates in the document.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.setInterval" shape="rect" title="DOM:window.setInterval">window.setInterval</a>
* </dt><dd> Execute a function each X milliseconds.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.setTimeout" shape="rect" title="DOM:window.setTimeout">window.setTimeout</a>
* </dt><dd> Sets a delay for executing a function.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.showModalDialog" shape="rect" title="DOM:window.showModalDialog">window.showModalDialog</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span>
* </dt><dd> Displays a modal dialog.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.sizeToContent" shape="rect" title="DOM:window.sizeToContent">window.sizeToContent</a>
* </dt><dd> Sizes the window according to its content.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.stop" shape="rect" title="DOM:window.stop">window.stop</a>
* </dt><dd> This method stops window loading.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.unescape" shape="rect" title="DOM:window.unescape">window.unescape</a>
* </dt><dd> Unencodes a value that has been encoded in hexadecimal (e.g. a cookie).
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.updateCommands" shape="rect" title="DOM:window.updateCommands">window.updateCommands</a>
* </dt><dd> Updates the state of commands of the current chrome window (UI).
* </dd></dl>
* <h2> <span> Event Handlers </span></h2>
* <dl><dt style="font-weight:bold"> <a href="DOM:window.onabort" shape="rect" title="DOM:window.onabort">window.onabort</a>
* </dt><dd> An event handler property for abort events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.onbeforeunload&amp;action=edit" shape="rect" title="DOM:window.onbeforeunload">window.onbeforeunload</a>
* </dt><dd> An event handler property for before-unload events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onblur" shape="rect" title="DOM:window.onblur">window.onblur</a>
* </dt><dd> An event handler property for blur events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onchange" shape="rect" title="DOM:window.onchange">window.onchange</a>
* </dt><dd> An event handler property for change events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onclick" shape="rect" title="DOM:window.onclick">window.onclick</a>
* </dt><dd> An event handler property for click events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onclose" shape="rect" title="DOM:window.onclose">window.onclose</a>
* </dt><dd> An event handler property for handling the window close event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="http://developer.mozilla.org/en/docs/index.php?title=window.oncontextmenu&amp;action=edit" shape="rect" title="DOM:window.oncontextmenu">window.oncontextmenu</a>
* </dt><dd> An event handler property for right-click events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.ondragdrop" shape="rect" title="DOM:window.ondragdrop">window.ondragdrop</a>
* </dt><dd> An event handler property for drag and drop events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onerror" shape="rect" title="DOM:window.onerror">window.onerror</a>
* </dt><dd> An event handler property for errors raised on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onfocus" shape="rect" title="DOM:window.onfocus">window.onfocus</a>
* </dt><dd> An event handler property for focus events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onkeydown" shape="rect" title="DOM:window.onkeydown">window.onkeydown</a>
* </dt><dd> An event handler property for keydown events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onkeypress" shape="rect" title="DOM:window.onkeypress">window.onkeypress</a>
* </dt><dd> An event handler property for keypress events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onkeyup" shape="rect" title="DOM:window.onkeyup">window.onkeyup</a>
* </dt><dd> An event handler property for keyup events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onload" shape="rect" title="DOM:window.onload">window.onload</a>
* </dt><dd> An event handler property for window loading.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmousedown" shape="rect" title="DOM:window.onmousedown">window.onmousedown</a>
* </dt><dd> An event handler property for mousedown events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmousemove" shape="rect" title="DOM:window.onmousemove">window.onmousemove</a>
* </dt><dd> An event handler property for mousemove events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmouseout" shape="rect" title="DOM:window.onmouseout">window.onmouseout</a>
* </dt><dd> An event handler property for mouseout events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmouseover" shape="rect" title="DOM:window.onmouseover">window.onmouseover</a>
* </dt><dd> An event handler property for mouseover events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onmouseup" shape="rect" title="DOM:window.onmouseup">window.onmouseup</a>
* </dt><dd> An event handler property for mouseup events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onpaint" shape="rect" title="DOM:window.onpaint">window.onpaint</a>
* </dt><dd> An event handler property for paint events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onreset" shape="rect" title="DOM:window.onreset">window.onreset</a>
* </dt><dd> An event handler property for reset events on the window.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onresize" shape="rect" title="DOM:window.onresize">window.onresize</a>
* </dt><dd> An event handler property for window resizing.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onscroll" shape="rect" title="DOM:window.onscroll">window.onscroll</a>
* </dt><dd> An event handler property for window scrolling.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onselect" shape="rect" title="DOM:window.onselect">window.onselect</a>
* </dt><dd> An event handler property for window selection.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onsubmit" shape="rect" title="DOM:window.onsubmit">window.onsubmit</a>
* </dt><dd> An event handler property for submits on window forms.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="window.onunload" shape="rect" title="DOM:window.onunload">window.onunload</a>
* </dt><dd> An event handler property for unload events on the window.
* </dd></dl>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var window = new Window();
