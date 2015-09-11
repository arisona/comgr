package ch.fhnw.i4ds.comgr;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GL;

public final class TextureHelpers {
	public static class TextureBuffer {
		private static final ColorModel GL_SRGBA_MODEL = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 }, true, false,
				ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
		private static final ColorModel GL_SRGB_MODEL = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 0 }, false, false,
				ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

		private int width;
		private int height;
		private Buffer buffer;
		private int format;

		public TextureBuffer(URL url, boolean flipVertically) {
			try {
				BufferedImage image = ImageIO.read(url);
				this.width = image.getWidth();
				this.height = image.getHeight();
				this.buffer = convertImage(image, flipVertically);
				this.format = image.getColorModel().hasAlpha() ? GL.GL_RGBA : GL.GL_RGB;
			} catch (Exception e) {
				throw new IllegalArgumentException("can't load image " + url);
			}
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public Buffer getBuffer() {
			return buffer;
		}

		public int getFormat() {
			return format;
		}

		private Buffer convertImage(BufferedImage image, boolean flipVertically) {
			int w = image.getWidth();
			int h = image.getHeight();
			boolean alpha = image.getColorModel().hasAlpha();
			boolean premult = image.getColorModel().isAlphaPremultiplied();

			BufferedImage tex;
			if (alpha) {
				WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, w, h, 4, null);
				tex = new BufferedImage(GL_SRGBA_MODEL, raster, premult, new Hashtable<>());
			} else {
				WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, w, h, 3, null);
				tex = new BufferedImage(GL_SRGB_MODEL, raster, premult, new Hashtable<>());
			}

			Graphics2D g = tex.createGraphics();
			g.setComposite(AlphaComposite.Src);
			if (flipVertically) {
				AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
				tx.translate(0, -image.getHeight(null));
				g.setTransform(tx);
			}
			g.drawImage(image, 0, 0, null);

			byte[] data = ((DataBufferByte) tex.getRaster().getDataBuffer()).getData();

			ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
			buffer.order(ByteOrder.nativeOrder());
			buffer.put(data, 0, data.length);

			return buffer;
		}
	}

}
