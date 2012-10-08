/**
 * 
 Copyright (c) 2010, Jens Kübler All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the <organization> nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.sf.seesea.navigation.ui.listener;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.eclipse.swt.widgets.Display;

import net.sf.seesea.model.core.physx.Time;
import net.sf.seesea.navigation.ui.figures.SingleLinedFigure;
import net.sf.seesea.services.navigation.listener.ITimeListener;

public class TimeFigureListener extends InvalidatingFigureListener<Time> implements ITimeListener {

	private final SingleLinedFigure timeFigure;
	private final SingleLinedFigure timeFigureUTC;
	private final SingleLinedFigure dateFigure;
	private final SimpleDateFormat dateFormat;
	private final SimpleDateFormat dateFormatGmt;

	public TimeFigureListener(SingleLinedFigure timeFigure,
			SingleLinedFigure timeFigureUTC, SingleLinedFigure dateFigure) {
		super(timeFigure, timeFigureUTC, dateFigure);
				this.timeFigure = timeFigure;
				this.timeFigureUTC = timeFigureUTC;
				this.dateFigure = dateFigure;
				dateFormat = new SimpleDateFormat("yyyy-MMM-dd"); //$NON-NLS-1$
				dateFormatGmt = new SimpleDateFormat("HH:mm:ss z"); //$NON-NLS-1$
	}

	@Override
	public void notify(final Time time, String source) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				dateFormatGmt.setTimeZone(TimeZone.getDefault());
				timeFigure.setTime(dateFormatGmt.format(time.getTime()));
				timeFigure.repaint();
				
				dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
				timeFigureUTC.setTime(dateFormatGmt.format(time.getTime()));
				timeFigureUTC.repaint();
				
				String currentDate = dateFormat.format(time.getTime());
				if(!currentDate.equals(dateFigure.getTime())) {
					dateFigure.setTime(currentDate);
					dateFigure.repaint();
				}

			}
		});
	}

}
