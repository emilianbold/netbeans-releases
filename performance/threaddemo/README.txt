Demonstration of the effect of different node rendering strategies on display of
large data sets such as massive files.

Configure paths to various libs; see build.xml and create user.build.properties
or just pass -D args to ant. Make sure openide.jar is up-to-date (check CVS).
--> You need openide/src/org/openide/util/Mutex*.java from branch mutex_32439, but rest of openide from trunk
--> You need Looks from the trunk (openide/looks).
--> Only JDK 1.4 is supported.

Default build target builds and runs the app. It creates a data model, called
Phadhail, which is just a thin wrapper over basic aspects of File. XML files
(*.xml) also have a parse-regenerate data model that you can view and use.

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

You probably first want to create some test data. About 500 files is good for
playing with.

Press Show to show the file tree.

Play with time to open a large folder, responsiveness, repainting, etc.

The model supports mutations and events, so you can play with that too. Operations:
- new file
- new folder
- rename
- delete

Also you can Open a file to get its contents in an editor window. To save you need
to select Save from the node's context menu. For XML files, you can expand them,
make text edits to see the reparse, or delete elements to see the regeneration.

You can click View Index in the file tree to see an index of the counts of all XML
elements in all XML files in the tree. This is supposed to updated live as changes
are made. This view can help you test the effect of an app feature that involves a
fair amount of computation and a lot of background read access to data models.

You can click Refactor to replace every <tag-0> with a <tag-1>, every
<tag-1> with a <tag-2>, etc. This action (cancellable and using a
nonmodal dialog) can help you test the effect of an app feature that
involves some computation as well as heavy background write access to
data models.

Currently no actions are supported in the Raw view, so you cannot test these. Raw view
also currently does not listen for changes, so it will not work with the Swung model
which relies on change events even with a read-only underlying model.

-jglick@netbeans.org
