/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.collab.ui.actions;

import java.awt.event.*;

import org.openide.util.*;
import org.openide.util.actions.SystemAction;

//import com.conga.jni.dispatch.*;

/**
 *
 *
 * 
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
