package ch.fhnw.i4ds.comgr;

import java.net.URL;
import java.nio.ByteBuffer;

public final class TextureHelpers {
	public static class TextureBuffer {
		private int width;
		private int height;
		private ByteBuffer buffer;
		private int format;

		public TextureBuffer(URL url, boolean flipVertically) {
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public ByteBuffer getBuffer() {
			return buffer;
		}

		public int getFormat() {
			return format;
		}
	}

}
