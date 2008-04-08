This is a forked copy of Rhino (currently, version 1.6R7 but the README may be stale)
which is patched for in-IDE use: In particular, this version of Rhino is suitable
for use by the syntax highlighter (by returning whitespace tokens), and it keeps
position info (for use by the parser). It also attempts more aggressive error recovery
(for use while code completing etc.)

