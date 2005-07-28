/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.actions;

import org.openide.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.*;


//import com.conga.jni.dispatch.*;

/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class FlashWindowAction extends SystemAction {
    public boolean isEnabled() {
        return true;
    }

    public String getName() {
        return "Flash Window";
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        //		try
        //		{
        //			new Thread(new WindowFlasher()).start();
        //		}
        //		catch (Exception e)
        //		{
        //			Debug.errorManager.notify(e);
        //		}
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    //	public static class WindowFlasher extends Object
    //		implements Runnable
    //	{
    //		public void run()
    //		{
    //	//		typedef struct {
    //	//			UINT cbSize;
    //	//			HWND hwnd;
    //	//			DWORD dwFlags;
    //	//			UINT uCount;
    //	//			DWORD dwTimeout;
    //	//		}
    //
    //			int structSize=
    //				NativePointer.SIZE+
    //				NativePointer.SIZE+
    //				NativePointer.SIZE+
    //				NativePointer.SIZE+
    //				NativePointer.SIZE;
    //			NativeMemory flashInfo=new NativeMemory(structSize);
    //
    //			try
    //			{
    //				Thread.currentThread().sleep(2000);
    //
    //				NativeFunction findWindow=new NativeFunction("user32.dll","FindWindowA",
    //					NativeFunction.STDCALL_CONVENTION);
    //				NativeFunction flashWindow=new NativeFunction("user32.dll","FlashWindowEx",
    //					NativeFunction.STDCALL_CONVENTION);
    //
    //				String title=WindowManager.getDefault().getMainWindow().getTitle();
    //				int windowHandle=findWindow.invokeInt(NativePointer.NULL,
    //					WindowManager.getDefault().getMainWindow().getTitle());
    //
    //				int offset=0;
    //				flashInfo.setInt(offset,structSize);
    //				offset+=NativePointer.SIZE;
    //				flashInfo.setInt(offset,windowHandle);
    //				offset+=NativePointer.SIZE;
    //				flashInfo.setInt(offset,ALL);
    //				offset+=NativePointer.SIZE;
    //				flashInfo.setInt(offset,3);
    //				offset+=NativePointer.SIZE;
    //				flashInfo.setInt(offset,0);
    //				offset+=NativePointer.SIZE;
    //
    //				int state=flashWindow.invokeInt(flashInfo);
    //			}
    //			catch (Exception e)
    //			{
    //				Debug.errorManager.notify(e);
    //			}
    //			finally
    //			{
    //				flashInfo.free();
    //			}
    //		}
    //
    //		public static final int STOP=0;
    //		public static final int CAPTION=1;
    //		public static final int TRAY=2; 
    //		public static final int ALL=3;
    //		public static final int TIMER=4; 
    //		public static final int TIMERNOFG=12; 
    //	}
}
