package cg_01_bresenham;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.Timer;

public class Bresenham extends WindowAdapter {
	static class FrameBuffer extends Canvas {
		private static final long serialVersionUID = -4862025317626407760L;
		int w;
		int h;
		BufferedImage buffer;

		@Override
		public void update(Graphics g) {
			if (getWidth() != w || getHeight() != h) {
				w = getWidth();
				h = getHeight();
				buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			}
			display();
			g.drawImage(buffer, 0, 0, this);
		}

		private int ix(float x) {
			return (int) ((x + 1f) * w * 0.5f);
		}

		private int iy(float y) {
			return (int) ((-y + 1f) * h * 0.5f);
		}

		public void drawPixel(float x, float y, int rgb) {
			drawPixel(ix(x), iy(y), rgb);
		}

		private void drawPixel(int x, int y, int rgb) {
			if (x < 0 || x >= w || y < 0 || y >= h)
				return;
			buffer.setRGB(x, y, rgb);
		}

		private static final int sign(float v) {
			return v < 0 ? -1 : v > 0 ? 1 : 0;
		}

		public void drawLine1(float x0, float y0, float x1, float y1, int rgb) {
			int n = ix(x1) - ix(x0);
			float dx = 2f / this.w;
			float dy = (y1 - y0) / n;
			for (int i = 0; i <= n; i++) {
				drawPixel(x0, y0, rgb);
				y0 += dy;
				x0 += dx;
			}
		}

		public void drawLine(float x0, float y0, float x1, float y1, int rgb) {
			float fw = x1 - x0;
			float fh = y1 - y0;

			int dx0 = sign(fw);
			int dy0 = sign(fh);
			int dx1 = dx0;
			int dy1 = 0;

			int l = (int) (Math.abs(fw) * w * 0.5);
			int s = (int) (Math.abs(fh) * h * 0.5);
			if (s > l) {
				int t = l;
				l = s;
				s = t;
				dy1 = sign(fh);
				dx1 = 0;
			}

			int ix0 = ix(x0);
			int iy0 = iy(y0);

			int num = l >> 1;
			for (int i = 0; i <= l; i++) {
				drawPixel(ix0, iy0, rgb);
				num += s;
				if (num >= l) {
					num -= l;
					ix0 += dx0;
					iy0 -= dy0;
				} else {
					ix0 += dx1;
					iy0 -= dy1;
				}
			}
		}

		private Random r = new Random();

		void display() {
			int N = 100;
			for (int i = 0; i < N; i++) {
				double a = i * 2.0 * Math.PI / N;
				drawLine1(0f, 0f, (float) Math.sin(a), (float) Math.cos(a),
						r.nextInt(0xFFFFFF));
			}
		}
	}

	public static void main(String[] args) {
		Frame f = new Frame("Brseneham");
		final FrameBuffer fb = new FrameBuffer();
		f.add(fb);
		f.setSize(800, 800);
		f.setVisible(true);
		new Timer(40, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fb.repaint();
			}
		}).start();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
}
