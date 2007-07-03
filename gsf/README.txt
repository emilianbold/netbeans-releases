-----------------------------------------------------------------
GSF - Generic Scripting Framework
-----------------------------------------------------------------

The  GSF  module stands for "Generic Scripting Framework".  It is
intended to make providing support for scripting  languages  bet-
ter.  Essentially,  it contains a lot of the infastructure neces-
sary for language integration, such as

* Tracking file changes, scheduling and cancelling parsing jobs

* Handling interactions with the various NetBeans APIs for things
like editor highlights, code completion, etc.

*  Managing  persistent  storage and interactions with the Lucene
search engine

The idea is that each language plugin (Ruby, Groovy,  etc.)  only
needs  to  provide specific feature implementations particular to
its own language, not IDE infrastructure.

As an example, the Ruby plugin (which is the only client of  this
infrastructure  today)  registers a parser (JRuby) which provides
an AST back to the IDE.  The infrastructure  does  not  interpret
this AST in any way, but it will hand it back to the various fea-
ture plugins from the Ruby module, such as  the  code  completion
support,  the  semantic  syntax  highlighting support, and so on.
The Ruby IDE plugin therefore gets to parse and walk its own ASTs
but  otherwise  does  not  worry  about scheduling, how to create
highlights etc. In the specific case  of  semantic  syntax  high-
lighting  for  example,  it  only  returns  a set of regions that
should be highlighted and GSF does the rest.

Even though there are "API" packages in GSF, these are very imma-
ture  and  will  probably change a lot as we experiment more with
more features, more languages, etc.  For  that  reason,  the  API
module has NO public packages.

Secondly, the GSF is based very heavily on the Java support. Many
classes were copied directly, and most were copied and then modi-
fied slightly to rip out the Java specific parts and insert redi-
rects to the language plugins where appropriate. The Java support
is  still  being developed, and I really want to benefit from bug
fixes and enhancements that are still being  performed  on  them.
For  that  reason,  I've tried to leave the source files in their
original state - leaving names and formatting in place as much as
possible.  I've  also  in  many cases annotated my own deliberate
changes to the files, to make it easier to  resolve  diffs  as  I
gradually integrate changes made to the Java support files.  This
hopefully explains why these files contain some features that are
not  yet  used  (because  I  plan to incorporate support for them
soon, such as scanning of jars during indexing, etc).

Even  though GSF stands for -Scripting- framework, it is not par-
ticularly -scripting- specific. GSF started out as a more generic
language framework. However, doing that introduced more complexi-
ty in many areas. Now that we  have  Schliemann  in  NetBeans  to
serve  as  a general language integration framework, GSF tries to
deep-dive in editing support for a much  smaller  class  of  lan-
guages, currently scripting languages. It might also fit well for
languages that may fall outside of this narrow  definition,  such
as Scala, but it would be wrong to try to apply GSF to a language
like XML, for example.



