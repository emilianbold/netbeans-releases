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
package org.netbeans.modules.collab.provider.im;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabMessage;
import com.sun.collablet.CollabMessagePart;
import com.sun.collablet.CollabPrincipal;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.lib.collab.*;

import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class IMCollabMessage extends IMCollabMessagePart implements CollabMessage {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Message message;
    private CollabPrincipal originator;

    //	private Contact[] recipients;
    private CollabMessagePart[] messageParts;

    /**
     * NOTE: The message's originator must have already been set when this
     * object is constructed (if necessary)
     *
     */
    protected IMCollabMessage(CollabPrincipal originator, Message message) {
        super(message);
        this.originator = originator;
        this.message = message;
    }

    /**
     *
     *
     */
    protected Message getMessage() {
        return message;
    }

    /**
     *
     *
     */
    public String getID() {
        return getMessage().getMessageId();
    }

    /**
     *
     *
     */
    public synchronized CollabPrincipal getOriginator() {
        //		if (originator==null)
        //			originator=new IMCollabPrincipal(message.getOriginator(),true);
        return originator;
    }

    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	public synchronized void setOriginator(CollabPrincipal value)
    //		throws CollabException
    //	{
    //		originator=value;
    //
    //		try
    //		{
    //			getMessage().setOriginator(originator.getIdentifier());
    //		}
    //		catch (CollaborationException e)
    //		{
    //			throw new CollabException(e,e.getMessage());
    //		}
    //	}
    //	/**
    //	 *
    //	 *
    //	 */
    //	public synchronized Contact[] getRecipients()
    //		throws CollabException
    //	{
    //		if (recipients==null)
    //		{
    //			try
    //			{
    //				String[] recipientsArray=getMessage().getRecipients();
    //				recipients=new Contact[recipientsArray.length];
    //				for (int i=0; i<recipientsArray.length; i++)
    //					recipients[i]=new IMContact(recipientsArray[i],true);
    //			}
    //			catch (CollaborationException e)
    //			{
    //				throw new CollabException(e,e.getMessage());
    //			}
    //		}
    //
    //		return recipients;
    //	}
    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	public synchronized void addRecipient(Contact contact)
    //		throws CollabException
    //	{
    //		recipients=null;
    //		try
    //		{
    //			getMessage().addRecipient(contact.getIdentifier());
    //		}
    //		catch (CollaborationException e)
    //		{
    //			throw new CollabException(e,e.getMessage());
    //		}
    //	}
    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	public synchronized void removeRecipient(Contact contact)
    //		throws CollabException
    //	{
    //		recipients=null;
    //		try
    //		{
    //			getMessage().removeRecipient(contact.getIdentifier());
    //		}
    //		catch (CollaborationException e)
    //		{
    //			throw new CollabException(e,e.getMessage());
    //		}
    //	}

    /**
     *
     *
     */
    public String getHeader(String header) {
        return getMessage().getHeader(header);
    }

    /**
     *
     *
     */
    public void setHeader(String header, String value)
    throws CollabException {
        try {
            getMessage().setHeader(header, value);
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public synchronized CollabMessagePart[] getParts() {
        if (messageParts == null) {
            MessagePart[] messagePartsArray = getMessage().getParts();
            messageParts = new CollabMessagePart[messagePartsArray.length];

            for (int i = 0; i < messagePartsArray.length; i++)
                messageParts[i] = new IMCollabMessagePart(messagePartsArray[i]);
        }

        return messageParts;
    }

    /**
     *
     *
     */
    public CollabMessagePart newPart() throws CollabException {
        try {
            return new IMCollabMessagePart(getMessage().newPart());
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public synchronized void addPart(CollabMessagePart part)
    throws CollabException {
        messageParts = null;

        try {
            getMessage().addPart(((IMCollabMessagePart) part).getMessagePart());
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public synchronized void removePart(CollabMessagePart part)
    throws CollabException {
        messageParts = null;

        try {
            getMessage().removePart(((IMCollabMessagePart) part).getMessagePart());
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    public void sendStatus(int status) throws CollabException {
        if (((IMCollabSession) originator.getCollabSession()).skipSend()) {
            Debug.out.println("Skipping send status: " + status);

            return;
        }

        try {
            if (status == CollabMessage.TYPING_ON) {
                getMessage().sendStatus(MessageStatus.TYPING_ON);
            } else if (status == CollabMessage.TYPING_OFF) {
                getMessage().sendStatus(MessageStatus.TYPING_OFF);
            }
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }
}
