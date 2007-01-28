This module provides API to markup(DOM) and CSS impl used in designer.

Currently the impl is in insync module in packages:
org.netbeans.modules.visualweb.insync.markup
org.netbeans.modules.visualweb.insync.markup.css
There is also the implementation of this API (org.netbeans.modules.visualweb.insync.markup.MarkupServiceImpl).

All those should be extracted into this module.

This module may not be dependent on designer/api or designer. 
The designer module will depend on this module (and others using the markup services,
which should be minimized).
