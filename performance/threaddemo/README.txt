Demonstration of the effect of different node rendering strategies on display of
large data sets such as massive files.

Configure paths to various libs; see build.xml and create user.build.properties
or just pass -D args to ant. Make sure openide.jar is up-to-date (check CVS).
--> You need openide/src/org/openide/util/Mutex*.java from branch mutex_32439, but rest of openide from trunk
--> You need Looks from the trunk (openide/looks).

Default build target builds and runs the app. It creates a data model, called
Phadhail, which is just a thin wrapper over basic aspects of File. You probably
first want to do something like this:

$ mkdir /tmp/testbigstuff
$ mkdir /tmp/testbigstuff/biggish
$ cd /tmp/testbigstuff/biggish
$ perl -e 'for ("aaa" .. "ezz") {open TOUCH, ">$_"; close TOUCH}'
$ mkdir /tmp/testbigstuff/whopper
$ cd /tmp/testbigstuff/whopper
$ perl -e 'for ("aaa" .. "zzz") {open TOUCH, ">$_"; close TOUCH}'

That creates dirs containing 3.4k and 17.6k files, respectively. In your
user.build.properties, set testdir=/tmp/testbigstuff for easy access.

Start the app. First select a threading model to use.

1. Synchronous - view will call model directly.

2. Locked - same, but will use a read mutex for every display operation and a
   write mutex for every modification.

3. Event-hybrid-locked - same as the locked model, but using a special mutex
   that only permits writes in AWT (reads can be in any thread).

4. Spun - uses Spin (spin.sf.net) to make the access asynch. FoxTrot would be
   similar, I think.

5. Swung - uses a technique similar to SwingWorker
   (http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html)
   to make the access fully asynch.

(In all cases, there is some internal buffering using weak references. Every time
you open a new view, though, System.gc() should clear it.)

And select a view type:

1. Raw. The data model is displayed directly in a JTree, using a more or less
   efficient TableModel, and using "big model" mode.

2. Node. The Nodes API is used with a regular Explorer tree view.

3. Look Node. Uses Looks to represent the data, then presents it with Nodes and
   an Explorer tree view.

4. Look. The Looks API is used to represent the data but this is displayed
   directly in a JTree without using the Nodes or Explorer APIs at all.

Play with time to open a large folder, responsiveness, repainting, etc.

The model supports mutations and events, so you can play with that too. Operations:
- new file
- new folder
- rename
- delete

Also you can Open a file to get its contents in an editor window. To save you need
to select Save from the node's context menu (currently broken in Look view).

Currently no actions are supported in the Raw view, so you cannot test these. Raw view
also currently does not listen for changes, so it will not work with the Swung model
which relies on change events even with a read-only underlying model.

-jglick@netbeans.org
