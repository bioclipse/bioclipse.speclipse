package net.bioclipse.nmrshiftdb.wizards;

import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PasswordWizardPage extends WizardPage {
	private Text text2;
	private Text text1;
	private Button selection;
	private SubmitWizard submitWizard;
	private boolean allright=false;
	private Button selectionServer;
	private Combo combo2;

	public boolean isAllright() {
		return allright;
	}

	/**
	 * Constructor for PasswordWizardPage.
	 * 
	 * @param submitWizard The submit wizard this page is part of
	 */
	public PasswordWizardPage(SubmitWizard submitWizard) {
		super("PasswordWizardPage");
		setTitle("Submit to NMRShiftDB wizard");
		setDescription("This wizard allows submission to NMRShiftDB");
		this.submitWizard=submitWizard;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;

		Label label=new Label(container, SWT.NULL);
		label.setText("Enter your NMRShiftDB user name and password here. If you do not yet have one,\r\ngo to http://www.nmrshiftdb.org and register.");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.widthHint=600;
		label.setLayoutData(gridData);
		Label label2=new Label(container, SWT.NULL);
		label2.setText("Username:");
		text1=new Text(container,SWT.WRAP | SWT.BORDER);
		GridData gridData2 = new GridData();
		gridData2.widthHint=600;
		text1.setLayoutData(gridData2);
		text1.setText(submitWizard.getUserPreference());
		text1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkForCompletion(true);
			}
		});
    text1.addTraverseListener(new TraverseListener() {
        public void keyTraversed(TraverseEvent e) {
          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            e.doit = true;
          }
        }
      });
		Label label3=new Label(container, SWT.NULL);
		label3.setText("Password:");
		text2=new Text(container, SWT.PASSWORD | SWT.BORDER);
		text2.setLayoutData(gridData2);	
		text2.setText(submitWizard.getPwPreference());
		text2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkForCompletion(true);
			}
		});
    text2.addTraverseListener(new TraverseListener() {
        public void keyTraversed(TraverseEvent e) {
          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            e.doit = true;
          }
        }
      });
		Label label6=new Label(container,SWT.NULL);
		label6.setText("Check this box if you want to remember your username/password");
		selection = new Button(container, SWT.CHECK);
		selection.setSelection(submitWizard.getNmrshiftdbPreference());
		Label label5=new Label(container, SWT.NULL);
		label5.setText("Choose a server to submit to (internet connection is needed to use this service)");
		label5.setLayoutData(gridData);
		combo2 = new Combo(container,SWT.DROP_DOWN);
		combo2.add(ServerWizardPage.getNmrshiftdbServerPreference());
		combo2.setText(combo2.getItem(0));
		combo2.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
		    	  if(PasswordWizardPage.this.selectionServer!=null){
		    		  if(PasswordWizardPage.this.selectionServer.getSelection()){
		    			  ServerWizardPage.setNmrshiftdbServerPreference(combo2.getText());
		    			  ServerWizardPage.setSaveServerPreference(true);
		    		  }else{
		    			  ServerWizardPage.setNmrshiftdbServerPreference(Bc_nmrshiftdbConstants.server);
		    			  ServerWizardPage.setSaveServerPreference(false);
		    		  }
		    	  }
			}
		});
		combo2.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  if(PasswordWizardPage.this.selectionServer!=null){
		    		  if(PasswordWizardPage.this.selectionServer.getSelection()){
		    			  ServerWizardPage.setNmrshiftdbServerPreference(combo2.getText());
		    			  ServerWizardPage.setSaveServerPreference(true);
		    		  }else{
		    			  ServerWizardPage.setNmrshiftdbServerPreference(Bc_nmrshiftdbConstants.server);
		    			  ServerWizardPage.setSaveServerPreference(false);
		    		  }
		    	  }
		      }
		});
		combo2.setLayoutData(gridData);		
		Label label7=new Label(container,SWT.NULL);
		label7.setText("Check this box if you want to remember the URL entered");
		selectionServer = new Button(container, SWT.CHECK);
		selectionServer.setSelection(ServerWizardPage.getSaveServerPreference());
		selectionServer.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
		    	  if(PasswordWizardPage.this.selectionServer!=null){
		    		  if(PasswordWizardPage.this.selectionServer.getSelection()){
		    			  ServerWizardPage.setNmrshiftdbServerPreference(combo2.getText());
		    			  ServerWizardPage.setSaveServerPreference(true);
		    		  }else{
		    			  ServerWizardPage.setNmrshiftdbServerPreference(Bc_nmrshiftdbConstants.server);
		    			  ServerWizardPage.setSaveServerPreference(false);
		    		  }
		    	  }
			}
		});
		setControl(container);
		checkForCompletion(false);
	}
	
	/**
	 * If page not complete, set error messages
	 */
	protected void checkForCompletion(boolean inoperation) {
		setErrorMessage(null);
		allright=true;
		if (text1.getText() == null || text1.getText().compareTo("") == 0){
			this.setErrorMessage("You need to enter a username!");
			allright=false;
		}
		if (text2.getText() == null || text2.getText().compareTo("") == 0){
			this.setErrorMessage("You need to enter a password!");
			allright=false;
		}
		if(allright){
			this.setPageComplete(true);
		}else{
			this.setPageComplete(false);
		}
		if(inoperation)
			getWizard().getContainer().updateButtons();
	}


	public String getSelectedServer() {
		return combo2.getText();
	}
	
	
	public String getPassword(){
		return text2.getText();
	}
	
	public String getUsername(){
		return text1.getText();
	}

	public Button getSelection() {
		return selection;
	}
}

