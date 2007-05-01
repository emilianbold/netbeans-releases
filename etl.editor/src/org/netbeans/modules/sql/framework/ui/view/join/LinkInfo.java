/*
 * LinkInfo.java
 *
 * Created on February 3, 2004, 7:03 PM
 */

package org.netbeans.modules.sql.framework.ui.view.join;

import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;

/**
 * @author radval
 */
public class LinkInfo {
    private SQLCanvasObject sObj;
    private SQLConnectableObject eObj;
    private String sParam;
    private String dParam;

    LinkInfo(SQLCanvasObject srcObj, SQLConnectableObject expObj, String srcParam, String destParam) {
        this.sObj = srcObj;
        this.eObj = expObj;
        this.sParam = srcParam;
        this.dParam = destParam;
    }

    public SQLCanvasObject getSource() {
        return this.sObj;
    }

    public SQLConnectableObject getTarget() {
        return this.eObj;
    }

    public String getSourceParam() {
        return this.sParam;
    }

    public String getTargetParam() {
        return this.dParam;
    }

}

