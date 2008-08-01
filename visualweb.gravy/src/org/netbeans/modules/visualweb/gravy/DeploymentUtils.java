/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.gravy;

import java.io.IOException;

////import com.meterware.httpunit.*;

import junit.framework.TestCase;

/**
 * RemoteDeploymentUtils class
 */

public class DeploymentUtils
{
  /**
   * Check if project was deployed.
   * @param deploy_string String to execute.
   * @return True if project works well, in other way retuirn false.
   */
  public static boolean testProject(String deploy_string)
  {
    try
    {
      Process oasp = Runtime.getRuntime().exec(deploy_string);
      System.out.println(deploy_string+" execute.");
      try
      {
        if (oasp.waitFor() == 0) System.out.println("Project has been deployed successfully!");
        else
        {
            System.out.println("Project has not been deployed!");
            TestCase.fail("Cannot run application.");
        }
      }
      catch (InterruptedException ie)
      {
        TestCase.fail("Exception in waitFor() : " + ie);
        return false;
      }
      oasp.destroy();
    }
    catch (IOException e)
    {
      TestCase.fail("Cannot run application.");
      return false;
    }
    TestUtils.wait(20000);
    return true;
  }

////  /**
////   * Check project with page navigation.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifySimpleNavigation(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      System.out.println("Response text after page commit: " + response.getText());
////      if (!response.getTitle().equals("Page2 Title"))
////      {
////        TestCase.fail("Cannot find Page2 in response");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with web service.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyWebService(String checkURL)
////  {
////    try
////    {
////      String webOutput;
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      webOutput = response.getText();
////      System.out.println("Response text : " + webOutput);
////      if (((response.getText()).indexOf("Kent") == -1) ||
////              ((response.getText()).indexOf("Richard") == -1) ||
////              ((response.getText()).indexOf("Cancel by 6pm local time 24 hours prior to avoid no-show billing.") == -1))
////      {
////        TestCase.fail("Cannot find strings according to chosen WebService in response");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check "Intro" project.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyIntro(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      com.meterware.httpunit.WebForm addContainer = response.getForms()[0];
////      WebRequest request = addContainer.getRequest();
////      System.out.println("Response text : " + response.getText());
////      addContainer.setParameter("form1:dropdown1", "Chen, Larry");
////      response = conversation.getResponse(request);
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      addContainer = response.getForms()[0];
////      System.out.println("Response text after page commit: " + response.getText());
////      if ((response.getText()).indexOf("Hello,  Larry!") == -1)
////      {
////        TestCase.fail("Cannot find \" Hello,  Larry! \" string in response after submit");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with validaton.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyValidation(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////      WebRequest request = addContainer.getRequest();
////      System.out.println("Set textField value = 500, expected result = 932");
////      addContainer.setParameter("form1:textField1", "500");
////      response = conversation.getResponse(request);
////      System.out.println("Response text after 1st page commit: " + response.getText());
////      if ((response.getText()).indexOf("932") == -1)
////      {
////        TestCase.fail("Cannot find value \"932:\" string in response");
////        return false;
////      }
////      TestUtils.wait(5000);
////      addContainer = response.getForms()[0];
////      request = addContainer.getRequest();
////      System.out.println("Set textField value = -1000, expected result = Validation Error");
////      addContainer.setParameter("form1:textField1", "-1000");
////      response = conversation.getResponse(request);
////      System.out.println("Response text after 2st page commit: " + response.getText());
////      if ((response.getText()).indexOf("Validation Error") == -1)
////      {
////        TestCase.fail("Cannot find \"Validation Error:\" string in response");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with dynamic navigation.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyDynamicNavigation(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////      addContainer.setParameter("form1:dropdown1", "Emerald City");
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      System.out.println("Response text after form1 submit: " + response.getText());
////      if (!response.getTitle().equals("Page2 Title"))
////      {
////        TestCase.fail("Wrong webform responce after WebForm1 submit action");
////        return false;
////      }
////      addContainer = response.getForms()[0];
////      btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      System.out.println("Response text after 1 form2 submit: " + response.getText());
////      if (response.getTitle().equals("Page1 Title"))
////      {
////        TestCase.fail("Wrong webform responce after WebForm2 first submit action");
////        return false;
////      }
////      response = addContainer.submit(btn);
////      System.out.println("Response text after 2 Page2 submit: " + response.getText());
////      response = addContainer.submit(btn);
////      System.out.println("Response text after 3 Page2 submit: " + response.getText());
////
////      if (!response.getTitle().equals("Page1 Title"))
////      {
////        TestCase.fail("Wrong page responce after Page2 third submit action");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Excetion in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project which send email.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyEmail(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////      addContainer.setParameter("form1:txtComments", "Hello world!");
////      TestUtils.wait(2000);
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      TestUtils.wait(2000);
////      System.out.println("Response text after page commit: " + response.getText());
////      if ((response.getText()).indexOf("Your comments have been successfully sent") == -1)
////      {
////        TestCase.fail("Cannot find \"Your comments have been successfully sent\" string in response");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      System.out.println("Check your mail settings, it can be cause of failing.");
////      TestCase.fail("Excetion in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project which DB access.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyDBAccess(String checkURL)
////  {
////    try
////    {
////      String webOutput;
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL); //
////      WebForm addContainer = response.getForms()[0];
////      addContainer.setParameter("form1:dropdown1", "2");
////      TestUtils.wait(5000);
////      WebRequest request = addContainer.getRequest();
////      TestUtils.wait(5000);
////      response = conversation.getResponse(request);
////      webOutput = response.getText();
////      System.out.println("Response text : " + webOutput);
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with number converter.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyNumberConverter(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      System.out.println("Set textField value = 125, expected result = class.java.Long");
////      addContainer.setParameter("form1:textField1", "125");
////      response = addContainer.submit(btn);
////      System.out.println("Response text after 1st page commit: " + response.getText());
////      if ((response.getText()).indexOf("java.lang.Long") == -1)
////      {
////        TestCase.fail("Cannot find value \"java.lang.Long\" string in response");
////        return false;
////      }
////      TestUtils.wait(5000);
////      addContainer = response.getForms()[0];
////      btn = addContainer.getSubmitButtons()[0];
////      System.out.println("Set textField value = 125.3, expected result = class.java.Double");
////      addContainer.setParameter("form1:textField1", "125.3");
////      response = addContainer.submit(btn);
////      System.out.println("Response text after 2st page commit: " + response.getText());
////      if ((response.getText()).indexOf("java.lang.Double") == -1)
////      {
////        TestCase.fail("Cannot find \"java.lang.Double\" string in response");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with currency converter.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyCurrencyConverter(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      System.out.println("Set textField value = $1,234.00");
////      addContainer.setParameter("form1:textField1", "$1,234.00");
////      response = addContainer.submit(btn);
////      System.out.println("Response text after 1st page commit: " + response.getText());
////      if (((response.getText()).indexOf("&pound;822.67") == -1) ||
////              ((response.getText()).indexOf("E 1.028,33") == -1))
////      {
////        TestCase.fail("Cannot find value \"&pound;822.67\" or \"E 1.028,33\" string in response");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with date converter.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyDateConverter(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      System.out.println("Response text after 1st page commit: " + response.getText());
////      if (((response.getText()).indexOf("Greenwich Mean Time") == -1) ||
////              ((response.getText()).indexOf("GMT") == -1) ||
////              (((response.getText()).indexOf("Pacific Daylight Time") == -1) &&
////               ((response.getText()).indexOf("Pacific Standard Time") == -1)))
////      {
////        TestCase.fail("Cannot find date time strings in response");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with "List" component.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyList(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      com.meterware.httpunit.WebForm addContainer = response.getForms()[0];
////      System.out.println("Response : " + response.getText());
////      String[] valArray = {"choice1", "choice2"};
////      addContainer.setParameter("form1:multiSelectListbox1", valArray);
////      SubmitButton btm = addContainer.getSubmitButton("form1:button1");
////      response = addContainer.submit(btm);
////      addContainer = response.getForms()[0];
////      System.out.println("Response text after page commit: " + response.getText());
////      if ((response.getText()).indexOf("Values chosen:\nchoice1\nchoice2") == -1)
////      {
////        TestCase.fail("Cannot find result string in response after submit");
////        return false;
////      }
////      TestUtils.wait(5000);
////      btm = addContainer.getSubmitButton("form1:button2");
////      TestUtils.wait(3000);
////      response = addContainer.submit(btm);
////      TestUtils.wait(3000);
////      addContainer = response.getForms()[0];
////      btm = addContainer.getSubmitButton("form1:button2");
////      response = addContainer.submit(btm);
////      TestUtils.wait(3000);
////      addContainer = response.getForms()[0];
////      TestUtils.wait(3000);
////      System.out.println("Response text after 1 page commit: " + response.getText());
////      String[] valArray1 = {"testChoice5", "testChoice6"};
////      addContainer.setParameter("form1:multiSelectListbox1", valArray1);
////      btm = addContainer.getSubmitButton("form1:button1");
////      response = addContainer.submit(btm);
////      addContainer = response.getForms()[0];
////      System.out.println("Response text after 2 page commit: " + response.getText());
////      if ((response.getText()).indexOf("Values chosen:\ntestChoice5\ntestChoice6") == -1)
////      {
////        TestCase.fail("Cannot find result string in response after submit");
////        return false;
////      }
////      TestUtils.wait(5000);
////      btm = addContainer.getSubmitButton("form1:button3");
////      TestUtils.wait(3000);
////      response = addContainer.submit(btm);
////      TestUtils.wait(3000);
////      addContainer = response.getForms()[0];
////      System.out.println("Response text after 3 page commit: " + response.getText());
////      if (((response.getText()).indexOf("testChoice5") != -1) ||
////              ((response.getText()).indexOf("testChoice6") != -1))
////      {
////        TestCase.fail("Wrong list items left after removing in response after submit");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Excetion in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with property binding.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyLinking(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////      WebRequest request = addContainer.getRequest();
////      System.out.println("Response text : " + response.getText());
////      addContainer.setParameter("form1:dropdown1", "Chen, Larry");
////      response = conversation.getResponse(request);
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      addContainer = response.getForms()[0];
////      System.out.println("Response text after page commit: " + response.getText());
////      if ((response.getText()).indexOf("Hello,  Larry!") == -1)
////      {
////        TestCase.fail("Cannot find \" Hello,  Larry! \" string in response after submit");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with ancillary library.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyLibrary(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      com.meterware.httpunit.WebForm addContainer = response.getForms()[0];
////      System.out.println("Response : " + response.getText());
////      if ((response.getText()).indexOf("Hello, world.") == -1)
////      {
////        TestCase.fail("Cannot find \"Hello, world.\" string in response.");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: " + e);
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check project with "Message" component.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyMessage(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = null;
////      response = conversation.getResponse(checkURL);
////      WebForm addContainer = response.getForms()[0];
////      WebRequest request = addContainer.getRequest();
////      System.out.println("Response text : " + response.getText());
////      SubmitButton btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      addContainer = response.getForms()[0];
////      System.out.println("Response text after page commit: " + response.getText());
////      if ((response.getText()).indexOf("Validation Error: Value is required.") == -1 || (response.getText()).indexOf("Validation Error: Value is required.") == -1)
////      {
////        TestCase.fail("Cannot find \"Validation Error: Value is required.\" string in response after submit");
////        return false;
////      }
////      TestUtils.wait(1000);
////      addContainer.setParameter("form1:textField1", "abc");
////      addContainer.setParameter("form1:textField2", "abc");
////      btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      addContainer = response.getForms()[0];
////      System.out.println("Response text after page commit: " + response.getText());
////      if ((response.getText()).indexOf("Conversion error occurred.") == -1)
////      {
////        TestCase.fail("Cannot find \"Conversion error occurred.\" string in response after submit");
////        return false;
////      }
////      TestUtils.wait(1000);
////      addContainer.setParameter("form1:textField1", "10");
////      addContainer.setParameter("form1:textField2", "abc");
////      btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      addContainer = response.getForms()[0];
////      System.out.println("Response text after page commit: " + response.getText());
////      if ((response.getText()).indexOf("Validation Error: Specified attribute is not between the expected values of 1 and 5.") == -1)
////      {
////        TestCase.fail("Cannot find \"Validation Error: Specified attribute is not between the expected values of 1 and 5.\" string in response after submit");
////        return false;
////      }
////      TestUtils.wait(1000);
////      addContainer.setParameter("form1:textField1", "4");
////      addContainer.setParameter("form1:textField2", "abc");
////      btn = addContainer.getSubmitButtons()[0];
////      response = addContainer.submit(btn);
////      addContainer = response.getForms()[0];
////      System.out.println("Response text after page commit: " + response.getText());
////      if ((response.getText()).indexOf("Form Message: Processing Complete.") == -1)
////      {
////        TestCase.fail("Cannot find \"Form Message: Processing Complete.\" string in response after submit");
////        return false;
////      }
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
////
////  /**
////   * Check CRUD project.
////   * @param checkURL Project's URL.
////   * @return True if project works well, in other way retuirn false.
////   */
////  public static boolean httpVerifyCRUD(String checkURL)
////  {
////    try
////    {
////      WebConversation conversation = new WebConversation();
////      WebResponse response = conversation.getResponse(checkURL);
////      System.out.println("Start page: " + response.getText());
////      WebForm form = response.getForms()[0];
////      SubmitButton addBtn=(SubmitButton)form.getButtonWithID("form1:addTrip");
////      response = form.submit(addBtn);
////      System.out.println("Details screen for add = " + response.getText());
////      form = response.getForms()[0];
////      form.setParameter("form1:depDate","16.06.2004 0:23:09");
////      form.setParameter("form1:fromCity","Hobbiton");
////      form.setParameter("form1:toCity","Rivendell");
////      form.setParameter("form1:tripTypeId","3");
////      SubmitButton saveBtn=(SubmitButton)form.getButtonWithID("form1:save");
////      response = form.submit(saveBtn);
////      System.out.println("After adding travel: " + response.getText());
////      form = response.getForms()[0];
////      SubmitButton updateBtn=(SubmitButton)form.getButtonWithID("form1:dataTable1:0:update");
////      response = form.submit(updateBtn);
////      System.out.println("Details screen for update = " + response.getText());
////      form = response.getForms()[0];
////      form.setParameter("form1:depDate","16.07.2004 0:23:09");
////      form.setParameter("form1:fromCity","Rivendell");
////      form.setParameter("form1:toCity","Orodruin");
////      form.setParameter("form1:tripTypeId","4");
////      saveBtn=(SubmitButton)form.getButtonWithID("form1:save");
////      response = form.submit(saveBtn);
////      System.out.println("After updating travel: " + response.getText());
////      form = response.getForms()[0];
////      SubmitButton deleteBtn=(SubmitButton)form.getButtonWithID("form1:dataTable1:0:delete");
////      response = form.submit(deleteBtn);
////    }
////    catch (Exception e)
////    {
////      System.out.println("Exception occured: ");
////      e.printStackTrace();
////      TestCase.fail("Exception in HTTP check : " + e);
////      return false;
////    }
////    return true;
////  }
}
