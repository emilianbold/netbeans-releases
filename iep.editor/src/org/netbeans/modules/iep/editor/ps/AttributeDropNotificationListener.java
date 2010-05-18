package org.netbeans.modules.iep.editor.ps;

import java.util.EventListener;

public interface AttributeDropNotificationListener extends EventListener {

    void onDropComplete(AttributeDropNotificationEvent evt);
    
}
