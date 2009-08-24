package net.bioclipse.spectrum;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import spok.guicomponents.SpectrumChartFactory;

public class PeakLabelThresholdPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {


	private Text value;
	private Button check;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		Label checkLabel = new Label(composite, SWT.NULL);
		checkLabel.setText("Show Peak Labels?");
		check = new Button(composite, SWT.CHECK);
		if (net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().getInt(SpectrumChartFactory.LABELTHRESHOLD) != -1) {
			check.setSelection(true);
		}
		check.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				if (!button.getSelection()) {
					if (value != null) {
						value.setText("-1");
						value.setVisible(false);
					}
				}
				else {
					value.setVisible(true);
				}

			}
			
		});
		
		Label setLabel = new Label(composite, SWT.NULL);
		setLabel.setText("Select threshold for displaying Peak Labels (in %): ");
		value = new Text(composite, SWT.BORDER | SWT.SINGLE);
		value.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				Text text = (Text) e.getSource();
				int content = -2;
				try {
					content = new Integer(text.getText()).intValue();
				}
				catch (NumberFormatException nfe) {
					
				}
				if (content == -1) {
					return;
				}
//				else if (content < 0 || content > 100) {
//					System.out.println("Number not in range of 0 - 100");
//				}
			}
			
		});
		int val = net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().getInt(SpectrumChartFactory.LABELTHRESHOLD);
		if (val == -1) {
			value.setText("   ");
		}
		else {
			String valStr = new Integer(val).toString();
			switch (valStr.length()) {
				case (1): valStr = valStr + " ";
				case (2): valStr = valStr + " ";
			}
			value.setText(valStr);
		}
		if (!check.getSelection()) {
			value.setVisible(false);
		}
		else {
			value.setVisible(true);
		}
		return composite;
	}

	public void init(IWorkbench workbench) {
	}
	
	private void checkValues(){
		int retVal;
		if (value.getText().trim().length() != 0) {
			try {
				Integer integer = new Integer(value.getText().trim());
				retVal = integer.intValue();
			}
			catch (NumberFormatException nfe) {
				MessageDialog.openError(getControl().getShell(), "Not a Number", "The entered value is not a Number! Please correct...");
				return;
			}
		}
		else {
			retVal = -1;
		}
		if (retVal != -1 && retVal < 0 || retVal > 100) {
			MessageDialog.openError(getControl().getShell(), "Not a valid Number", "The entered value is not between 0 and 100%! Please correct...");
			return;
		}
		net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().setValue(SpectrumChartFactory.LABELTHRESHOLD,retVal);
	}

	@Override
	protected void performApply() {
		checkValues();
		super.performApply();
	}

	@Override
	public boolean performOk() {
		checkValues();
		return super.performOk();
	}
	
	@Override
    protected void performDefaults() {
		check.setSelection(false);
		value.setText("-1");
		value.setEnabled(false);
    }
}
