import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Master extends JFrame
{
	private static final int SCR_W=120;
	private static final int SCR_H=80;

	public Master()
	{
		super("Interos IO v0.1");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0,0,SCR_W,SCR_H);
		this.setLayout(new GridBagLayout());
		this.setBackground(Color.BLACK);

		initUI();

		initHandlers();

		this.requestFocus();
	}

	private void initUI()
	{
		this.add(new JLabel("Switch to this window to send \ninput to the selected client."));

		this.pack();
		this.setLocationRelativeTo(null);
	}

	private void initHandlers()
	{
		this.addKeyListener(new KeyListener()
		{

			@Override
			public void keyPressed(KeyEvent e)
			{

			}

			@Override
			public void keyReleased(KeyEvent e)
			{

			}

			@Override
			public void keyTyped(KeyEvent e)
			{

			}

		});
	}

	public void handleKeyPress(KeyEvent e)
	{
		System.out.println(String.format("Pressed key %s.",e.getKeyCode()));
	}

	public void handleKeyRelease(KeyEvent e)
	{
		System.out.println(String.format("Pressed key %s.",e.getKeyCode()));
	}

	public void handleKeyTyped(KeyEvent e)
	{
		System.out.println(String.format("Pressed key %s.",e.getKeyCode()));
	}

	public static void main(String[] args)
	{
		new Master().setVisible(true);
	}

}
