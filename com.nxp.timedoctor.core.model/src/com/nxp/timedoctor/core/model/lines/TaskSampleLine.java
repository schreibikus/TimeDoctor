/*******************************************************************************
 * Copyright (c) 2006 Royal Philips Electronics NV.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License version 1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Royal Philips Electronics NV. - initial API and implementation
 *******************************************************************************/
package com.nxp.timedoctor.core.model.lines;

import com.nxp.timedoctor.core.model.SampleCPU;
import com.nxp.timedoctor.core.model.SampleLine;
import com.nxp.timedoctor.core.model.Description.DescrType;
import com.nxp.timedoctor.core.model.Sample.SampleType;

/**
 * Sample line for tasks. Overrides much of the SampleLine implementation for
 * type-specific functionality.
 */
public class TaskSampleLine extends SampleLine {

	/**
	 * Variable for use in adding samples, to track the state of the sample
	 * array.
	 */
	private int startCount = 0;
	/**
	 * Number of times the line has been suspended since the last start/resume.
	 */
	private int suspendCount = 0;

	/**
	 * Constructs a task sample line using the given cpu and id, and adds it to
	 * the tasks section of the model (creating it if it does not exist).
	 * 
	 * @param cpu
	 *            the cpu associated with the line
	 * @param id
	 *            the integer id of the line.
	 */
	public TaskSampleLine(final SampleCPU cpu, final int id) {
		super(cpu, id);
		setType(LineType.TASK);
	}

	/**
	 * Implements abstract superclass method addSample with type-specific
	 * behavior. Adds the sample described by the type, time, and value provided
	 * to the samples array.
	 * 
	 * @param type
	 *            the type of sample
	 * @param time
	 *            the time at which it occurred
	 * @param value
	 *            the value associated with the sample
	 */
	public final void addSample(final SampleType type, final double time,
			final double val) {
		if (getCount() == 0 && type == SampleType.STOP) {
			return;
		}
		switch (type) {
		case START:
			if (startCount > 0) {
				return;
			}
			addOneSample(type, time, val);
			startCount = 1;
			break;
		case STOP:
			if (startCount == 0) {
				return;
			}
			if (suspendCount > 0) {
				addOneSample(SampleType.RESUME, time, val);
				suspendCount = 0;
			}
			addOneSample(type, time, val);
			startCount = 0;
			break;
		case SUSPEND:
			if (startCount == 0) {
				return;
			}
			if (suspendCount == 0) {
				addOneSample(type, time, val);
			}
			suspendCount++;
			break;
		case RESUME:
			if (suspendCount == 0) {
				return;
			}
			suspendCount--;
			if (suspendCount == 0) {
				addOneSample(type, time, val);
			}
			break;
		default:
			if (suspendCount > 0) {
				addOneSample(SampleType.RESUME, time, val);
				suspendCount = 0;
			}
			addOneSample(type, time, val);
			break;
		}
	}

	// MR explain what it does!
	/**
	 * Implements the abstract calculate function from superclass.
	 * 
	 * @param endTime
	 *            the time at which to end calculation
	 */
	public final void calculate(final double endTime) {
		int[] clr = new int[getCount() + 1];
		int[] st = new int[getCount() + 1];
		// MR extract (generic) method
		if (getName() == null) {
			setName(String.format("Task 0x%x", getID()));
		}
		
		// MR add comments, extract sub methods
		int n = 0;
		setMaxSampleDuration(0);
		for (int i = 0, ii = 0; i < getCount(); i++) {
			if (getSample(i).type == SampleType.START) {
				clr[n] = -1;
				for (; ii < getDescCount(); ii++) {
					if (getDescription(ii).time >= getSample(i).time) {
						break;
					}
				}
				for (; ii < getDescCount(); ii++) {
					if (getDescription(ii).time == getSample(i).time) {
						if (getDescription(ii).type == DescrType.COLOR) {
							clr[n] = (int) getDescription(ii).value;
						}
					} else {
						break;
					}
				}
				st[n++] = i;
			} else if (getSample(i).type == SampleType.SUSPEND) {
				if (n > 0) {
					clr[n] = clr[n - 1];
				} else {
					clr[n] = -1;
				}
				st[n++] = i;
			} else if (getSample(i).type == SampleType.STOP
					|| getSample(i).type == SampleType.RESUME) {
				int stopColor = -1;
				for (; ii < getDescCount(); ii++) {
					if (getDescription(ii).time >= getSample(i).time) {
						break;
					}
				}
				for (; ii < getDescCount(); ii++) {
					if (getDescription(ii).time == getSample(i).time) {
						if (getDescription(ii).type == DescrType.COLOR) {
							stopColor = (int) getDescription(ii).value;
						}
					} else {
						break;
					}
				}
				if (n > 0) {
					n--;
					int j = st[n];
					getSample(j).val = i;
					if (stopColor >= 0) {
						getSample(i).val = stopColor;
					} else {
						getSample(i).val = clr[n];
					}
					if (getSample(i).type == SampleType.STOP) {
						setMaxSampleDuration(Math.max(getMaxSampleDuration(),
								getSample(i).time - getSample(j).time));
					}
				}
			}
		}
		
		// MR extract method
		/*
		 * Add samples at end time to conclude still open task
		 */
		for (n--; n >= 0; n--) {
			if (getSample(st[n]).type == SampleType.START) {
				addSample(SampleType.STOP, endTime);
			} else {
				while (suspendCount > 0) {
					addSample(SampleType.RESUME, endTime);
				}
			}
			getSample(st[n]).val = getCount() - 1;
		}
		if (getCount() > 0) {
			addSample(SampleType.END, endTime, getSample(getCount() - 1).val);
		} else {
			addSample(SampleType.END, endTime);
		}
	}

	// MR explain what it does for type-specific behavior
	/**
	 * Overrides superclass method for type-specific behavior.
	 * 
	 * @param from
	 *            the beginning of the time window
	 * @param to
	 *            the end of the time window
	 * @return true if there are samples present in the window, false otherwise
	 */
	public final boolean hasSamples(final double from, final double to) {
		int i = binarySearch(from);
		int j = binarySearch(to);
		if (i != j) {
			return true;
		} else if (getCount() <= 1) {
			return false;
		} else if (getSample(i).time > to || getSample(i).time < from) {
			return false;
		} else if (getSample(i).type == SampleType.STOP) { // how can this be
			// removed?
			return false;
		} else {
			return true;
		}
	}
}
