/***************************************************************************
 *
 *          Copyright (c) 2005, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 ***********************************************************************org*netbeanspmodulesawsdlextensions.smtp.validatortpbc.extensions;

 
/**
 *
 *
 * @author       Alexander Fung
 * @version      
 *
 */

package org.netbeans.modules.wsdlextensions.smtp.validator;

public class InvalidMailboxException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Creates a new instance of <code>InvalidMailboxException</code>
     * without detail message.
     */
    public InvalidMailboxException() {
        super();
    }

    /**
     * Constructs an instance of <code>InvalidMailboxException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidMailboxException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>InvalidMailboxException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidMailboxException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}

