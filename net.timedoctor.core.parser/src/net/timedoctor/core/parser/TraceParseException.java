/*******************************************************************************
 * Copyright (c) 2007 NXP Semiconductors B.V.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License version 1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     NXP Semiconductors B.V. - initial API and implementation
 *******************************************************************************/
package net.timedoctor.core.parser;

/**
 * Exception thrown, when parsing of the trace file fails
 */
public class TraceParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7689132135855970026L;
	
	public TraceParseException() {
		super();
	}
	
	public TraceParseException(String message) {
		super(message);
	}
}