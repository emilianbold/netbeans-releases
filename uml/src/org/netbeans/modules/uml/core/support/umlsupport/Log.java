/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * File         : Log.java
 * Version      : 2.0
 * Description  : Provides basic logging services for Describe IDE integrations.
 * Author       : Sumitabh Kansal
 */
package org.netbeans.modules.uml.core.support.umlsupport;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.ui.support.ProductHelper;

/**
 *  Provides basic logging services for Describe IDE integrations.
 *
 * @author Sumitabh Kansal
 */
public class Log
{
    //kris richards - all LoggingInformation prefs have been expunged and set to empty or false.
    // Therefore, most methods do nothing effectual (no writing).
    /**
    *  An autoflushing writer to the logfile.
    */
    private static PrintWriter output        = null;
   
    private static boolean initialized       = false;

    private static final String LOG_PATH    = "LoggingInformation";
    private static final String PREF_YES    = "PSK_YES";

    // bit masks used to reveal the MDR event type being passed in
    public final static int MDR_EVENT_MASK_UNKNOWN     = 0;   // 00000000
    public final static int MDR_EVENT_MASK_GENERAL     = 7;   // 00000111 
    public final static int MDR_EVENT_MASK_SPECIFIC    = 248; // 11111000

    // bit masks for the 3 general types of MDR events
    public final static int MDR_EVENT_MASK_PLANNED     = 1;   // 00000001
    public final static int MDR_EVENT_MASK_CANCELED    = 2;   // 00000010
    public final static int MDR_EVENT_MASK_CHANGE      = 4;   // 00000100
    
    // bit masks for the 5 specific types of event
    public final static int MDR_EVENT_MASK_ATTRIBUTE   = 8;   // 00001000
    public final static int MDR_EVENT_MASK_ASSOCIATE   = 16;  // 00010000
    public final static int MDR_EVENT_MASK_EXTENT      = 32;  // 00100000
    public final static int MDR_EVENT_MASK_INSTANCE    = 64;  // 01000000
    public final static int MDR_EVENT_MASK_TRANSACTION = 128; // 10000000
    
    public final static int MDR_EVENT_FLAG_RESET       = 0;
    // bitwise flags indicating the 5 specific MDR events being logged
    // for each of the 3 general event types
    public static int mdrPlannedEventLogFlag           = 0;
    public static int mdrCanceledEventLogFlag          = 0;
    public static int mdrChangeEventLogFlag            = 0;
    
    
   /**
    *  Forces Log to reinitialize when the next Log request is made.
    */
   public static void reset()
   {
      mdrPlannedEventLogFlag = MDR_EVENT_FLAG_RESET;
      mdrCanceledEventLogFlag = MDR_EVENT_FLAG_RESET;
      mdrChangeEventLogFlag = MDR_EVENT_FLAG_RESET;
   }
   
   /**
    *  Initializes Log.
    */
   public static void init()
   {
      setPropertyFlags();
   }
   
   private static boolean initialized()
   {
      if (initialized) return true;
      init();
      return initialized;
   }
   
   /**
    *  Writes a string to the logfile, with an ending newline.
    */
   public static void write(String s)
   {
       write(s, true);
   }
   
   /**
    *  Writes a string to the logfile, with an ending newline.
    */
   public static void write(String s, boolean newline)
   {
       try
       {
           if (!initialized)
               init();
           
           if (output != null)
           {
               if (newline)
                   output.println(s);
               else
                   output.print(s);
           }
       }
       
       catch (Exception e)
       {
           e.printStackTrace();
       }
   }
   
   /**
    *  Writes a string to the logfile, prefixed by the current date and a
    * [Debug] tag, and terminated by a newline. Suitable for debugging log
    * messages.
    * @param s The String to write.
    */
   public static void out(String s)
   {
      if (!initialized)
         init();
   }
   
   /**
    * Writes a string to the logfile, boxed by banner lines. Use sparingly...
    * @param s The String to write.
    * @deprecated This method is extremely noisy in the logfile and should not
    *             be used in production code.
    */
   public static void banner(String s)
   {
   }
   
   /**
    * Dumps a stack trace of the current thread to the logfile.
    * @param s The String (or null) that will be used to describe the stack
    *          trace.
    * @deprecated This method is extremely noisy in the logfile, and should not
    *             be used in production code.
    */
   public static void dumpStack(String s)
   {
      Log.stackTrace(new Throwable(s).fillInStackTrace());
   }
   
   /**
    *  Writes a string to the logfile, prefixed by the current date and an
    * [Entry] tag, and terminated by a newline. Suitable for function entry
    * logs.
    *
    * @param s The String to write.
    */
   public static void entry(String s)
   {
      if (!initialized)
         init();
      
   }
   
   /**
    *  Writes a string to the logfile, prefixed by the current date and an
    * [Exit] tag, and terminated by a newline. Suitable for function exit
    * logs.
    *
    * @param s The String to write.
    */
   public static void exit(String s)
   {

   }
   
   /**
    *  Writes a string to the logfile, prefixed by the current date and an
    * [Error] tag, and terminated by a newline. Suitable for error
    * notifications.
    *
    * @param s The String to write.
    */
   public static void err(String s)
   {
      if (!initialized)
         init();

   }
   
   /**
    * Writes a stack trace to the logfile.
    * @param t The Throwable object that holds the stack trace.
    */
   public static void stackTrace(Throwable t)
   {
      if (!initialized)
         init();

   }
   
   /**
    * Writes a stack trace to the log file, with the error message
    * given. Suitable for logging conditions that should never occur.
    *
    * @param mess The error message.
    */
   public static void impossible(String mess)
   {
      dumpStack(mess);
   }
   
   /**
    *  Writes a string to the logfile, prefixed by a [DescribeLog] tag.
    *
    * @param s The String to write.
    */
   public static void writeDescribeLogs(String s)
   {
      try
      {
         if (!initialized)
            init();
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   
   /**
    *  Writes a string to the logfile, prefixed by the current date and an
    * [Error] tag, and terminated by a newline. Suitable for error
    * notifications.
    *
    * @param s The String to write.
    */
   public static void mdrEventTrace(int mdrEventID, String s, boolean newline)
   {
      if (!initialized)
         init();
      
      if (isMDREventEnabled(mdrEventID))
      {
         // actual miliseconds are more useful than formatted 
         // date/time for event logging (the precision is desired)
         write(System.currentTimeMillis() + " [MDR Event] " + s, newline);
//         write(new Date(System.currentTimeMillis()) 
//            + " [MDR Event] " + s, newline);
      }
   }
   
   
   
   /**
    *  Sets Log properties by asking Describe's preference manager. Aborts
    * if Describe is not connected.
    */
   private static void setPropertyFlags()
   {
      IPreferenceManager2 prefMan = ProductHelper.getPreferenceManager();
      if (prefMan == null)
         return;
      
      String mdrLogPath = LOG_PATH + "|LogMDREvents|LogMDRPlannedChangeEvents";
      String prefLogMDRPlannedAssocEvent    = prefMan.getPreferenceValue(mdrLogPath,"LogMDRAssociationEvents");
      String prefLogMDRPlannedAttrEvent     = prefMan.getPreferenceValue(mdrLogPath,"LogMDRAttributeEvents");
      String prefLogMDRPlannedExtentEvent   = prefMan.getPreferenceValue(mdrLogPath,"LogMDRExtentEvents");
      String prefLogMDRPlannedInstanceEvent = prefMan.getPreferenceValue(mdrLogPath,"LogMDRInstanceEvents");
      String prefLogMDRPlannedTransEvent    = prefMan.getPreferenceValue(mdrLogPath,"LogMDRTransactionEvents");

      mdrLogPath = LOG_PATH + "|LogMDREvents|LogMDRCanceledChangeEvents";
      String prefLogMDRCanceledAssocEvent    = prefMan.getPreferenceValue(mdrLogPath,"LogMDRAssociationEvents");
      String prefLogMDRCanceledAttrEvent     = prefMan.getPreferenceValue(mdrLogPath,"LogMDRAttributeEvents");
      String prefLogMDRCanceledExtentEvent   = prefMan.getPreferenceValue(mdrLogPath,"LogMDRExtentEvents");
      String prefLogMDRCanceledInstanceEvent = prefMan.getPreferenceValue(mdrLogPath,"LogMDRInstanceEvents");
      String prefLogMDRCanceledTransEvent    = prefMan.getPreferenceValue(mdrLogPath,"LogMDRTransactionEvents");

      mdrLogPath = LOG_PATH + "|LogMDREvents|LogMDRChangeEvents";
      String prefLogMDRChangeAssocEvent    = prefMan.getPreferenceValue(mdrLogPath,"LogMDRAssociationEvents");
      String prefLogMDRChangeAttrEvent     = prefMan.getPreferenceValue(mdrLogPath,"LogMDRAttributeEvents");
      String prefLogMDRChangeExtentEvent   = prefMan.getPreferenceValue(mdrLogPath,"LogMDRExtentEvents");
      String prefLogMDRChangeInstanceEvent = prefMan.getPreferenceValue(mdrLogPath,"LogMDRInstanceEvents");
      String prefLogMDRChangeTransEvent    = prefMan.getPreferenceValue(mdrLogPath,"LogMDRTransactionEvents");

      if (PREF_YES.equals(prefLogMDRPlannedAssocEvent))
            mdrPlannedEventLogFlag |= MDR_EVENT_MASK_ASSOCIATE;
      if (PREF_YES.equals(prefLogMDRPlannedAttrEvent))
            mdrPlannedEventLogFlag |= MDR_EVENT_MASK_ATTRIBUTE;
      if (PREF_YES.equals(prefLogMDRPlannedExtentEvent))
            mdrPlannedEventLogFlag |= MDR_EVENT_MASK_EXTENT;
      if (PREF_YES.equals(prefLogMDRPlannedInstanceEvent))
            mdrPlannedEventLogFlag |= MDR_EVENT_MASK_INSTANCE;
      if (PREF_YES.equals(prefLogMDRPlannedTransEvent))
            mdrPlannedEventLogFlag |= MDR_EVENT_MASK_TRANSACTION;

      if (PREF_YES.equals(prefLogMDRCanceledAssocEvent))
            mdrCanceledEventLogFlag |= MDR_EVENT_MASK_ASSOCIATE;
      if (PREF_YES.equals(prefLogMDRCanceledAttrEvent))
            mdrCanceledEventLogFlag |= MDR_EVENT_MASK_ATTRIBUTE;
      if (PREF_YES.equals(prefLogMDRCanceledExtentEvent))
            mdrCanceledEventLogFlag |= MDR_EVENT_MASK_EXTENT;
      if (PREF_YES.equals(prefLogMDRCanceledInstanceEvent))
            mdrCanceledEventLogFlag |= MDR_EVENT_MASK_INSTANCE;
      if (PREF_YES.equals(prefLogMDRCanceledTransEvent))
            mdrCanceledEventLogFlag |= MDR_EVENT_MASK_TRANSACTION;

      if (PREF_YES.equals(prefLogMDRChangeAssocEvent))
            mdrChangeEventLogFlag |= MDR_EVENT_MASK_ASSOCIATE;
      if (PREF_YES.equals(prefLogMDRChangeAttrEvent))
            mdrChangeEventLogFlag |= MDR_EVENT_MASK_ATTRIBUTE;
      if (PREF_YES.equals(prefLogMDRChangeExtentEvent))
            mdrChangeEventLogFlag |= MDR_EVENT_MASK_EXTENT;
      if (PREF_YES.equals(prefLogMDRChangeInstanceEvent))
            mdrChangeEventLogFlag |= MDR_EVENT_MASK_INSTANCE;
      if (PREF_YES.equals(prefLogMDRChangeTransEvent))
            mdrChangeEventLogFlag |= MDR_EVENT_MASK_TRANSACTION;
    
         ETSystem.out.println("Log file not specified");
         
         if (output != null)
         {
            output.close();
            output = null;
         }
      
   }

    private static boolean isMDREventEnabled(int mdrEventID)
    {
        int generalType = mdrEventID & MDR_EVENT_MASK_GENERAL;
        int specificType = (mdrEventID & MDR_EVENT_MASK_SPECIFIC);

        if (generalType == MDR_EVENT_MASK_PLANNED)
        {
            if ((mdrPlannedEventLogFlag & specificType) > 0)
                return true;
        }
        
        else if (generalType == MDR_EVENT_MASK_CANCELED)
        {
            if ((mdrCanceledEventLogFlag & specificType) > 0)
                return true;
        }
        
        else if (generalType == MDR_EVENT_MASK_CHANGE)
        {
            if ((mdrChangeEventLogFlag & specificType) > 0)
                return true;
        }
        
        return false;
    }
}
