package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.border.Border;

/**
 * @author David Kaspar
 */
public final class VMDFactory {

    private static final Border BORDER_NODE = new VMDNodeBorder ();

    private VMDFactory () {
    }

    public static Border createVMDNodeBorder () {
        return BORDER_NODE;
    }

}
