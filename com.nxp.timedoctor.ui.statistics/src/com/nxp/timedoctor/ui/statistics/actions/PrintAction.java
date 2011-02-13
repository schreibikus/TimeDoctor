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
package com.nxp.timedoctor.ui.statistics.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.nxp.timedoctor.ui.ITimeDoctorUIConstants;
import com.nxp.timedoctor.ui.statistics.IStatisticsViewPage;

public class PrintAction extends Action {
	private IStatisticsViewPage statisticsViewPage;
	
	public PrintAction(final IStatisticsViewPage statisticsViewPage) {
		this.statisticsViewPage = statisticsViewPage;
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(ITimeDoctorUIConstants.TD_UI_PLUGIN, 
				ITimeDoctorUIConstants.LOCAL_TOOLBAR_ENABLED_IMG_PATH + "print.gif"));
		setToolTipText("Print");
	}

	@Override
	public void run() {
		statisticsViewPage.print();
	}
}