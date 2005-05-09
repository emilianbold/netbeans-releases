/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 */
package org.netbeans.modules.jmx.runtime;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import java.awt.*;

public class ManagementDialogs {
  private static DialogDisplayer standard = DialogDisplayer.getDefault();
  private static ManagementDialogs defaultInstance;

  private ManagementDialogs () { } // avoid direct instance creation

  public static ManagementDialogs getDefault () {
    if (defaultInstance == null)
      defaultInstance = new ManagementDialogs ();
    return defaultInstance;
  }

  public Object notify(NotifyDescriptor descriptor) {
    return standard.notify(descriptor);
  }

  public Dialog createDialog(DialogDescriptor descriptor) {
    return standard.createDialog(descriptor);
  }


  public static class DNSAMessage extends NotifyDescriptor.Message {
    /**
     * Create an informational report about the results of a command.
     *
     * @param message the message object
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAMessage(String key, Object message) {
      super(message);
    }

    /**
     * Create a report about the results of a command.
     *
     * @param message     the message object
     * @param messageType the type of message to be displayed
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAMessage(String key, Object message, int messageType) {
      super(message, messageType);
    }
  }

  public static class DNSAConfirmation extends NotifyDescriptor.Confirmation {

    /**
     * Create a yes/no/cancel question with default title.
     *
     * @param message the message object
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message) {
      super(message);
    }

    /**
     * Create a yes/no/cancel question.
     *
     * @param message the message object
     * @param title   the dialog title
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, String title) {
      super(message, title);
    }

    /**
     * Create a question with default title.
     *
     * @param message    the message object
     * @param optionType the type of options to display to the user
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, int optionType) {
      super(message, optionType);
    }

    /**
     * Create a question.
     *
     * @param message    the message object
     * @param title      the dialog title
     * @param optionType the type of options to display to the user
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, String title, int optionType) {
      super(message, title, optionType);
    }

    /**
     * Create a confirmation with default title.
     *
     * @param message     the message object
     * @param optionType  the type of options to display to the user
     * @param messageType the type of message to use
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, int optionType, int messageType) {
      super(message, optionType, messageType);
    }

    /**
     * Create a confirmation.
     *
     * @param message     the message object
     * @param title       the dialog title
     * @param optionType  the type of options to display to the user
     * @param messageType the type of message to use
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, String title, int optionType, int messageType) {
      super(message, title, optionType, messageType);
    }
  }
}
