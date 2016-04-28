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
var Window = {}

var window = new Window();
