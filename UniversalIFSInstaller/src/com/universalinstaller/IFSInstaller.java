package com.universalinstaller;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class IFSInstaller extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4123464448377571392L;
	public static IFSInstaller gui = null;
	static JFrame frame;
	JPanel insPanel = new JPanel();
	JLabel m_SystemLabel = new JLabel();
	JLabel m_UserLabel = new JLabel();
	JLabel m_PasswordLabel = new JLabel();
	JFormattedTextField m_SystemBox = new JFormattedTextField();
	JTextField m_UserBox = new JTextField();
	JPasswordField m_PasswordBox = new JPasswordField();
	JButton m_AcceptButton = new JButton();
	JButton m_RemoveButton = new JButton();
	JLabel m_title = new JLabel();
	JLabel m_status = new JLabel();
	JProgressBar m_progress = new JProgressBar();

	public IFSInstaller()
	{
		setFocusTraversalPolicyProvider(true);
		initializePanel();
		gui = this;
	}

	public static void main(String[] args)
	{
		frame = new JFrame();
		frame.setDefaultCloseOperation(3);
		frame.setMinimumSize(new Dimension(260, 230));
		frame.setSize(300, 250);
		frame.setPreferredSize(new Dimension(300, 250));
		frame.setLocationRelativeTo(null);
		frame.getContentPane().add(new IFSInstaller());
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent evt)
			{
				System.exit(0);
			}
		});
	}

	void addFillComponents(Container panel, int[] cols, int[] rows)
	{
		Dimension filler = new Dimension(10, 10);

		boolean filled_cell_11 = false;
		CellConstraints cc = new CellConstraints();
		if ((cols.length > 0) && (rows.length > 0)) {
			if ((cols[0] == 1) && (rows[0] == 1))
			{
				panel.add(Box.createRigidArea(filler), cc.xy(1, 1));
				filled_cell_11 = true;
			}
		}
		for (int index = 0; index < cols.length; index++) {
			if ((cols[index] != 1) || (!filled_cell_11)) {
				panel.add(Box.createRigidArea(filler), cc.xy(cols[index], 1));
			}
		}
		for (int index = 0; index < rows.length; index++) {
			if ((rows[index] != 1) || (!filled_cell_11)) {
				panel.add(Box.createRigidArea(filler), cc.xy(1, rows[index]));
			}
		}
	}

	public JPanel createPanel()
	{
		FormLayout formlayout1 = new FormLayout("FILL:10PX:NONE,FILL:DEFAULT:NONE,FILL:4PX:NONE,FILL:100PX:GROW,FILL:10PX:NONE", "FILL:10PX:NONE,CENTER:DEFAULT:GROW,FILL:8PX:NONE,CENTER:DEFAULT:GROW,FILL:4PX:NONE,CENTER:DEFAULT:GROW,FILL:4PX:NONE,CENTER:DEFAULT:GROW,FILL:8PX:NONE,CENTER:DEFAULT:GROW,FILL:8PX:NONE,CENTER:DEFAULT:GROW,FILL:4PX:NONE,CENTER:DEFAULT:GROW,FILL:10PX:NONE");
		this.insPanel.setLayout(formlayout1);

		frame.setTitle("Universal IFS Installer");

		this.m_title.setName("title");
		this.m_title.setText("Universal IFS Installer");
		this.m_title.setHorizontalAlignment(0);
		this.insPanel.add(this.m_title, new CellConstraints(2, 2, 3, 1, CellConstraints.FILL, CellConstraints.FILL));

		this.m_SystemLabel.setName("SystemLabel");
		this.m_SystemLabel.setText("System Host/IP");
		this.insPanel.add(this.m_SystemLabel, new CellConstraints(2, 4, 1, 1, CellConstraints.RIGHT, CellConstraints.FILL));

		this.m_UserLabel.setName("UserLabel");
		this.m_UserLabel.setText("User ID");
		this.m_UserLabel.setHorizontalAlignment(4);
		this.insPanel.add(this.m_UserLabel, new CellConstraints(2, 6, 1, 1, CellConstraints.RIGHT, CellConstraints.FILL));

		this.m_PasswordLabel.setName("PasswordLabel");
		this.m_PasswordLabel.setText("Password");
		this.m_PasswordLabel.setHorizontalAlignment(4);
		this.insPanel.add(this.m_PasswordLabel, "2, 8, right, fill");

		this.m_SystemBox.setColumns(1);
		this.m_SystemBox.setName("SystemBox");
		this.m_SystemBox.setToolTipText("IP Address or Hostname of System i");
		this.insPanel.add(this.m_SystemBox, "4, 4, fill, fill");

		this.m_UserBox.setName("UserBox");
		this.m_UserBox.setToolTipText("User Name that will be used to install software package, this user should be a QSECOFR account");
		this.insPanel.add(this.m_UserBox, "4, 6, fill, fill");

		this.m_PasswordBox.setName("PasswordBox");
		this.m_PasswordBox.setToolTipText("Password for System i Account");
		this.insPanel.add(this.m_PasswordBox, "4, 8, fill, fill");

		this.m_status.setName("status");
		this.m_status.setText("Ready");
		this.m_status.setHorizontalAlignment(2);
		this.insPanel.add(this.m_status, new CellConstraints(2, 12, 3, 1, CellConstraints.FILL, CellConstraints.FILL));

		this.m_progress.setName("progressbar");
		this.m_progress.setString("0 Bytes Copied");
		this.m_progress.setStringPainted(true);
		this.insPanel.add(this.m_progress, "2, 14, 3, 1, fill, fill");

		this.m_AcceptButton.setActionCommand("Install");
		this.m_AcceptButton.setName("InstallButton");
		this.m_AcceptButton.setText("Install");
		this.m_AcceptButton.setToolTipText("Install Software Package(s)");
		this.insPanel.add(this.m_AcceptButton, "4, 10, default, fill");
		this.m_AcceptButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				new Thread(new Runnable()
				{
					public void run()
					{
						try
						{
							@SuppressWarnings("deprecation")
							IFSUnpacker extractor = new IFSUnpacker(IFSUnpackerFile.load(), IFSInstaller.this.m_SystemBox.getText(), IFSInstaller.this.m_UserBox.getText(), IFSInstaller.this.m_PasswordBox.getText());
							extractor.addStatusLabel(IFSInstaller.this.m_status);
							extractor.addProgressBar(IFSInstaller.this.m_progress);
							extractor.runInstall();

							IFSInstaller.this.m_status.setText("Installation Complete");
							JOptionPane.showMessageDialog(new JFrame(), "Install Complete", "Installation Complete", -1);
						}
						catch (IFSUnpackerException e)
						{
							JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error", 0);
							IFSInstaller.this.m_status.setText("Installation Failed");
						}
					}
				})

				.start();
			}
		});
		this.m_RemoveButton.setActionCommand("Remove");
		this.m_RemoveButton.setName("RemoveButton");
		this.m_RemoveButton.setText("Uninstall");
		this.m_RemoveButton.setToolTipText("Remove Software Package(s)");
		this.insPanel.add(this.m_RemoveButton, "2, 10, default, fill");
		this.m_RemoveButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				new Thread(new Runnable()
				{
					public void run()
					{
						try
						{
							@SuppressWarnings("deprecation")
							IFSUnpacker extractor = new IFSUnpacker(IFSUnpackerFile.load(), IFSInstaller.this.m_SystemBox.getText(), IFSInstaller.this.m_UserBox.getText(), IFSInstaller.this.m_PasswordBox.getText());
							IFSInstaller.this.m_progress.setString("");
							extractor.addStatusLabel(IFSInstaller.this.m_status);
							extractor.addProgressBar(IFSInstaller.this.m_progress);
							extractor.runRemove();

							IFSInstaller.this.m_status.setText("Removal Complete");
							JOptionPane.showMessageDialog(new JFrame(), "Removal Complete", "Removal Complete", -1);
						}
						catch (IFSUnpackerException e)
						{
							JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error", 0);
							IFSInstaller.this.m_status.setText("Removal Failed");
						}
					}
				})

				.start();
			}
		});
		addFillComponents(this.insPanel, new int[] { 1, 2, 3, 4, 5 }, new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
		return this.insPanel;
	}

	protected void initializePanel()
	{
		setLayout(new BorderLayout());
		add(createPanel(), "Center");
	}
}
