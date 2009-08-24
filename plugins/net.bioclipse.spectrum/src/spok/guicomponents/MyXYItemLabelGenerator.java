/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package spok.guicomponents;

import java.text.DecimalFormat;

import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;

public class MyXYItemLabelGenerator extends AbstractXYItemLabelGenerator implements XYItemLabelGenerator {

	private static final long serialVersionUID = 5834288072518398864L;

	public MyXYItemLabelGenerator(String string, DecimalFormat format, DecimalFormat format2) {
		super(string, format, format2);
	}

	public String generateLabel(XYDataset dataset, int series, int item) {
		Number num = dataset.getX(series, item);
		return num.toString();
	}

}
