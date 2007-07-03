# Native Ruby doesn't always flush its buffers when running
# under NetBeans (e.g. not in a proper terminal or pty).
# As a workaround, we execute these commands before anything else.
$stdout.sync=true
$stderr.sync=true
