/*
Copyright (c) 2013 Max Lungarella <cybrmx@gmail.com>

This file is part of AmiKoDesk for Windows.

AmiKoDesk is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.maxl.java.amikodesk;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JComponent;

public class ProgressIndicator extends JComponent {

	private Color[] colors = new Color[12];
	private SpinLoop spinLoop = new SpinLoop();
	private volatile int hour;
	private volatile boolean spinning;
	private volatile boolean progress;


	public ProgressIndicator(int width) {
		Dimension d = new Dimension(width, width);
		super.setMinimumSize(d);
		super.setMaximumSize(d);
		super.setPreferredSize(d);
		super.setSize(d);

		int i = 0;
		for (; i<6; i++) {
			colors[i] = new Color(0xC0666666, true);
		}
		colors[i++] = new Color(0xC0505050, true);
		colors[i++] = new Color(0xCC404040, true);
		colors[i++] = new Color(0xD0303030, true);
		colors[i++] = new Color(0xDD202020, true);
		colors[i++] = new Color(0xE0101010, true);
		colors[i++] = new Color(0xEE000000, true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (spinning) {
			((Graphics2D)g).setStroke(new BasicStroke(2.0f));
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			double innerR = (getWidth() - 2) / 4.0;
			double outerR = (getWidth() - 2) / 2.0;
			double centerX = getWidth() / 2.0;
			double centerY = getWidth() / 2.0;
			for (int i=0; i<12; i++) {
				g.setColor(colors[(i - hour + 12) % 12]);
				double angle = Math.PI * ((double)i)/6.0;
				double cos = Math.cos(angle);
				double sin = Math.sin(angle);
				double innerX = (centerX + cos * innerR);
				double innerY = (centerY + sin * innerR);
				double outerX = (centerX + cos * outerR);
				double outerY = (centerY + sin * outerR);
				((Graphics2D)g).draw(new Line2D.Double(innerX, innerY, outerX, outerY));
			}
		}
//		g.setColor(Color.RED);
//		g.drawRect(0,0,getWidth()-1, getHeight()-1);
	}

	public void set(int hour) {
		this.hour = hour % 12;
		this.repaint();
	}

	public void increment() {
		hour = (hour + 1) % 12;
		this.repaint();
	}

	/**
	 * since this starts up a thread, be careful to call setSpinning(false)
	 * @param spinning
	 */
	public void setSpinning(boolean spinning) {
		this.spinning = spinning;		
		if (spinning) {
			Thread thread = new Thread(spinLoop);
			thread.start();
		}		
		repaint();
	}

	public void spinOnProgress() {
		this.spinning = true;
		Thread thread = new Thread(new SpinOnProgressLoop());
		thread.start();
		repaint();
	}

	public void setProgress(boolean progress) {
		this.progress = progress;
	}
	
	private class SpinOnProgressLoop implements Runnable {
		int MILLIS_PER_CYCLE = 100;
		long cycleTime;
		public void run() {
			cycleTime = System.currentTimeMillis();
			
			while (spinning) {
				if (progress) {
					progress = false;
					increment();
				}
				sync();
			}
		}
		private void sync() {
			cycleTime = cycleTime + MILLIS_PER_CYCLE;
			long difference = cycleTime - System.currentTimeMillis();
			try {
				Thread.sleep(Math.max(0, difference));
			}
			catch(InterruptedException e) {
			}
		}
	}

	private class SpinLoop implements Runnable {
		int MILLIS_PER_CYCLE = 100;
		long cycleTime;
		public void run() {
			cycleTime = System.currentTimeMillis();
			
			while (spinning) {
				increment();
				sync();
			}
		}
		private void sync() {
			cycleTime = cycleTime + MILLIS_PER_CYCLE;
			long difference = cycleTime - System.currentTimeMillis();
			try {
				Thread.sleep(Math.max(0, difference));
			}
			catch(InterruptedException e) {
			}
		}
	}

}
