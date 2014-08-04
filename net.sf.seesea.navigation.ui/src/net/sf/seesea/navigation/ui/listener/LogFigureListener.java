package net.sf.seesea.navigation.ui.listener;

import java.text.DecimalFormat;

import net.sf.seesea.model.core.physx.Distance;
import net.sf.seesea.navigation.ui.figures.DescriptiveInstrumentFigure;
import net.sf.seesea.navigation.ui.figures.DoubleRowedDescriptiveInstrumentFigure;
import net.sf.seesea.services.navigation.listener.ITotalLogListener;
import net.sf.seesea.services.navigation.listener.ITripLogListener;

import org.eclipse.swt.widgets.Display;

public class LogFigureListener extends InvalidatingFigureListener<Distance> implements ITotalLogListener, ITripLogListener {

	private final DoubleRowedDescriptiveInstrumentFigure figure;
	private DecimalFormat logDecimalFormat;

	
	public LogFigureListener(DoubleRowedDescriptiveInstrumentFigure figure) {
		super(figure);
		this.figure = figure;
		logDecimalFormat = new DecimalFormat("##0.0"); //$NON-NLS-1$
	}
	
	@Override
	public void notify(final Distance sensorData, String source) {
		if(isSensorUpdateFast()) {
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				String nm = logDecimalFormat.format(sensorData.getValue()) + Messages.getString("LogFigureListener.nauticalMile");
				if(sensorData.getSensorID().equals("GPS")) {
					figure.setValue1(nm); //$NON-NLS-1$
				} else {
					figure.setValue2(nm); //$NON-NLS-1$
				}
				figure.repaint();
			}
		});
	}

}
