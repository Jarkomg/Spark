/**
 * $Revision: $
 * $Date: $
 *
 * Copyright (C) 2006 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package org.jivesoftware.sparkimpl.plugin.gateways;

import org.jivesoftware.resource.Res;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.component.RolloverButton;
import org.jivesoftware.spark.component.TitlePanel;
import org.jivesoftware.spark.ui.status.StatusBar;
import org.jivesoftware.spark.util.GraphicUtils;
import org.jivesoftware.spark.util.ModelUtil;
import org.jivesoftware.spark.util.ResourceUtils;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.Transport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.TransportUtils;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


/**
 * Dialog to allow the addition of gateways within Spark.
 *
 * @author Derek DeMoro
 */
public class TransportRegistrationDialog extends JPanel implements ActionListener, KeyListener {

    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private RolloverButton registerButton = new RolloverButton("", null);
    private RolloverButton cancelButton = new RolloverButton("", null);
    private JDialog dialog;
    private String serviceName;
    private Transport transport;

    /**
     * Initiation Dialog with the tranport service name.
     *
     * @param serviceName the name of the transport service.
     */
    public TransportRegistrationDialog(String serviceName) {
        setLayout(new GridBagLayout());

        this.serviceName = serviceName;

        ResourceUtils.resButton(registerButton, Res.getString("button.register"));
        ResourceUtils.resButton(cancelButton, Res.getString("button.cancel"));


        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(registerButton);
        registerButton.requestFocus();
        buttonPanel.add(cancelButton);


        transport = TransportUtils.getTransport(serviceName);

        final TitlePanel titlePanel = new TitlePanel(transport.getTitle(), transport.getInstructions(), transport.getIcon(), true);

        add(titlePanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        final JLabel usernameLabel = new JLabel();
        usernameLabel.setFont(new Font("Dialog", Font.BOLD, 11));
        ResourceUtils.resLabel(usernameLabel, usernameField, Res.getString("label.username") + ":");
        add(usernameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(usernameField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

        final JLabel passwordLabel = new JLabel();
        passwordLabel.setFont(new Font("Dialog", Font.BOLD, 11));
        ResourceUtils.resLabel(passwordLabel, passwordField, Res.getString("label.password") + ":");
        add(passwordLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        add(passwordField, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

        add(buttonPanel, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    }

    /**
     * Invoke the Dialog.
     */
    public void invoke() {
        dialog = new JDialog(SparkManager.getMainWindow(), transport.getTitle(), false);
        dialog.add(this);
        dialog.pack();
        dialog.setSize(400, 200);

        GraphicUtils.centerWindowOnComponent(dialog, SparkManager.getMainWindow());
        dialog.setVisible(true);

        usernameField.requestFocus();

        usernameField.addKeyListener(this);
        passwordField.addKeyListener(this);
        registerButton.addActionListener(this);

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.dispose();
            }
        });
    }

    public String getScreenName() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }


    public void actionPerformed(ActionEvent e) {
        String username = getScreenName();
        String password = getPassword();
        if (!ModelUtil.hasLength(username) || !ModelUtil.hasLength(password)) {
            JOptionPane.showMessageDialog(this, Res.getString("message.username.password.error"), Res.getString("title.registration.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            TransportUtils.registerUser(SparkManager.getConnection(), serviceName, username, password);

            // Send Directed Presence
            final StatusBar statusBar = SparkManager.getWorkspace().getStatusBar();
            Presence presence = statusBar.getPresence();
            presence.setTo(transport.getServiceName());
            SparkManager.getConnection().sendPacket(presence);
        }
        catch (XMPPException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(this, Res.getString("message.registration.transport.failed"), Res.getString("title.registration.error"), JOptionPane.ERROR_MESSAGE);
        }

        dialog.dispose();
    }


    public void keyTyped(KeyEvent keyEvent) {
    }

    public void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
            actionPerformed(null);
        }

        if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dialog.dispose();
        }
    }

    public void keyReleased(KeyEvent keyEvent) {
    }
}