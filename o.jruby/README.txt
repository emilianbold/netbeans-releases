I’m  using  JRuby  both  for in‐IDE parsing (to do semantic high‐
lighting etc.) as well as for lexing (to do syntax highlighting).
JRuby’s  lexer  is  advanced (which is why I want to use it ‐ for
example, it distinguishes between "if" being used as a  statement
modifier  (no corresponding end statement) or as a statement ini‐
tiator.  However, it wasn’t written to be used as an  IDE  lexer.
In  particular,  I need to modify it to return whitespace tokens,
comment tokens, and to split up embedded Ruby code strings  (#{})
within  literals.  I also need to modify it to return its current
state (and let me set its current state) such that I can  use  it
for  incremental  lexing (restarting lexing from given boundaries
in the document during editing).

Rather than fork the code I’ve left this as a set of patches such
that I can keep up to date with the rapidly evolving JRuby parser
and lexer. The patches try to make as few  modifications  to  the
source  as possible rather than be as clean as possible.  My next
task is to see what here makes sense to integrate  into  baseline
JRuby.

I both bundle an unmodified version of JRuby (which installs into
the cluster and is used to execute projects, Rails, etc) as  well
as  a  patched  version which is used within the IDE for language
features. These are both built from  a  single  source  zip  file
which  is  unzipped  into  unpatched_source and patched_source. I
then delegate to the ant file  in  unpatched_source  to  build  a
standard  JRuby  distribution.  I copy in patched versions of the
files to build the IDE jar. My original build  files  were  using
the  ant  <patch> task to insert my code modifications. Turns out
this doesn’t work on Windows and has problems on  some  platforms
(since  <patch> isn’t provided directly by ant; it just delegates
to system patch commands).  For that reason, I’m now  hardcopying
in  versions of the patched files in the patched_files directory.
These obviously need to be kept in sync with the  source  bundle,
and can be regenerated from the provided jruby.diff file.

The unpatched version of JRuby (used for execution) is also modi‐
fied in a couple of ways.  First,  the  encryption  related  code
(Bountycastle, OpenSSL) are removed. They can be added again lat‐
er, but have not been included in the netbeans.org  bits  because
of export restrictions on encryption technology, which would have
to go through further legal review.

I also added fork="true" to the  generate‐method‐classes  target,
because  recursive  building  failed without it (the <javac> task
was using the wrong directory).
























































































































































