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
package com.nxp.timedoctor.ui.statistics;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.nxp.timedoctor.core.model.SampleLine;
import com.nxp.timedoctor.core.model.TraceModel;
import com.nxp.timedoctor.core.model.ZoomModel;
import com.nxp.timedoctor.core.model.SampleLine.LineType;
import com.nxp.timedoctor.core.model.statistics.Statistic;
import com.nxp.timedoctor.core.model.statistics.StatisticsTimeModel;
import com.nxp.timedoctor.core.model.statistics.TaskStatistic;

//TODO: button to select between seconds, cycles, or %
public class LineStatViewer implements Observer {
	private TraceModel traceModel;
	private ZoomModel zoomModel;
	private StatisticsTimeModel timeModel;
	
	private StatTimeViewer timeViewer;
	private LineStatTreeViewer treeViewer;
	
	private Observer timeObserver = null;
	
	/**
	 * The constructor.
	 */
	public LineStatViewer(final Composite parent) {
		GridLayout parentLayout = new GridLayout(1, false);
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parentLayout.verticalSpacing = 0;
		parent.setLayout(parentLayout);
		
		timeViewer = new StatTimeViewer(parent);
		timeViewer.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		
		treeViewer = new LineStatTreeViewer(parent);
		treeViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		treeViewer.setInput(null);
	}

	public void setModels(final TraceModel traceModel, 
			final ZoomModel zoomModel,
			final StatisticsTimeModel timeModel) {
		
		// TODO THIS IS A HACK!! 
		// add this as an observer to the zoom model to get updates when the
		// selection has changed.
		// SHOULD BE REMOVED when the TraceEditor provides ISelection events
		// and triggers the selection listener in LineStatView
		// In that case, only listen to the ISelection event and get the selected 
		// trace line and zoom times from there.
		if (this.zoomModel != null) {
			this.zoomModel.deleteObserver(this);
		}
		if (zoomModel != null) {
			zoomModel.addObserver(this);
		}
		// END HACK
		
		this.traceModel = traceModel;
		this.zoomModel = zoomModel;
		this.timeModel = timeModel;
		
		timeViewer.setTimeModel(timeModel);
		
		if ((traceModel != null) && (timeModel != null)) {		
			selectionChanged();
		} else {
			treeViewer.setInput(null);
			treeViewer.refresh();
		}					
	}

	// THIS IS A HACK
	// part of the hack above in setModels
	public void update(Observable o, Object arg) {
		selectionChanged();				
	}
	
	public void selectionChanged() {
		if ((zoomModel == null) || (traceModel == null) || (timeModel == null)) {
			return;
		}
		
		// TODO check ISelection on what has changed, only update accordingly
		// Now, assume the ISelection event is sent when the line has changed, or the selected
		// zoom window has changed
		SampleLine line = zoomModel.getSelectedLine();
		if (line != null) {
			// Update selected line
			sampleLineChanged(line);

			// Update selected time window
			timeModel.setTimes(zoomModel.getStartTime(), zoomModel.getEndTime());
		}
		else {
			treeViewer.setInput(null);
			treeViewer.refresh();
		}
	}
	
	private void sampleLineChanged(final SampleLine line) {
		if (timeObserver != null) {
			timeModel.deleteObserver(timeObserver);
		}

		Statistic input = null;
		LineType type = line.getType();		
		if ((type == LineType.TASKS) || (type == LineType.ISRS) || (type == LineType.AGENTS)) {
			// Task statistics are specific per line, create a new one
			input = createTaskStatistic(line);
		}
		treeViewer.setInput(input);
	}
	
	private TaskStatistic createTaskStatistic(final SampleLine line) {
		final TaskStatistic taskStat = new TaskStatistic(null, traceModel, line);	
		
		// Recalculate when the start and end times change, either by ISelection events,
		// or by local events from the StatTimeViewer
		timeObserver = new Observer() {
			public void update(final Observable o, final Object arg) {
				// Show statistics for the current zoom range
				taskStat.calculate(timeModel.getStartTime(), timeModel
						.getEndTime());										
				treeViewer.refresh();
			}
		};
		timeModel.addObserver(timeObserver);
		
		// Recalculate even when timeModel is not changed, 
		// since the sample line may have changed
		// TODO needs some cleanup, use ISelection information here
		taskStat.calculate(timeModel.getStartTime(), timeModel
				.getEndTime());	
		treeViewer.refresh();
		
		return taskStat;
	}
}
