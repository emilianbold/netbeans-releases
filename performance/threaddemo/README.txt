Demonstration of the effect of different node rendering strategies on display of
large data sets such as massive files.

Configure paths to various libs; see build.xml and create user.build.properties
or just pass -D args to ant.

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

2. Locked - same, but will use a read mutex for every access.

3. Spun - uses Spin (spin.sf.net) to make the access asynch.

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

Note that none of the models currently support any listening, so this is not a
completely fair test. If listening were added, it would be possible to add a
fourth threading model: asynch with callbacks, like Swing Workers. Note that this
means using SwingUtilities.invokeLater and is thus quite different internally
from Spin, which uses a private event queue. (Foxtrot is similar to Spin in this
respect, though the user API looks quite different because it does not use Proxy.)

-jglick@netbeans.org
