com.sun.rave.insync (InSync Source Modeler) Module

Description
-----------
InSync is responsible for the source modeling, two way and truth-in-source capabilities of Creator.

Module Owners
-------------
Eric Arseneau
   All
Devananda Jayaraman (Deva)
   All
Torbjorn Norbye (Tor)
   com.sun.rave.insync.markup
   com.sun.rave.insync.markup.css

Current Status
--------------
We keep a Twiki page updated with current status of InSync module itself at

http://jupiter.czech.sun.com/wiki/view/Creator/InSyncStatus

Change Process
--------------
Internally to the InSync team, changes are discussed and code is reviewed by all team members prior to commit.  There are exceptions, but these are the exception to the rule rather than the norm.

Changes made by external people should be discussed, and diffs sent once performed, to both Eric and Deva for review.  If we do not respond with a positive or negative response within a reasonable amount of time AND a second prod, then permission is grudgingly given to go ahead and commit changes.

Build Instructions
------------------
- If inside src/insync dir
   - ant
      - will compile modified source and update jar found in ravebuild/rave build directory
   - ant clean
      - will remove all compiled classes, derived resources and jar in ravebuild/rave dir
- raveBuild insync
   - will compile modified source and update jar found in ravebuild/rave build directory
- if inside ravebuild dir
   - ant insync
      - will compile modified source and update jar found in ravebuild/rave build directory

Documentation
-------------
The documents CVS module (cvs co documents from inside your src folder) contains some limited documentation on the design and internals of InSync.

  /src/documents/insync

Active Branches
---------------
- trunk (main)
  - contains code in use by most of the Creator team

- dev-insync
  - contains active work internal to InSync and others to review and test risky on-going work
