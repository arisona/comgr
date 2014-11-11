package ch.fhnw.i4ds.comgr;

import java.awt.Frame;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.GLBuffers;

public class Ring extends GLCanvas implements GLEventListener {
	private static final long serialVersionUID = -8933329638658421749L;

	private static final int N = 40;
	private static final double R0 = 0.5;
	private static final double R1 = 0.7;

	private int program;
	private final List<Integer> shaders = new ArrayList<>();
	private final int[] VBO = new int[1];
	private final int[] VAO = new int[1];

	public static void main(String[] args) {
		Ring triangle = new Ring();
		Frame frame = new Frame("Comgr Ring");
		frame.add(triangle);
		frame.setSize(triangle.getWidth(), triangle.getHeight());
		frame.setVisible(true);
	}

	public Ring() {
		super(new GLCapabilities(GLProfile.get("GL3")));
		setSize(800, 800);
		addGLEventListener(this);
	}

	@Override
	public void init(GLAutoDrawable glad) {
		GL3 gl3 = glad.getGL().getGL3();

		try {
			shaders.add(GLSLHelpers.createShader(gl3, getClass(), GL3.GL_VERTEX_SHADER, "glsl/simple_vs"));
			shaders.add(GLSLHelpers.createShader(gl3, getClass(), GL3.GL_FRAGMENT_SHADER, "glsl/simple_fs"));
			program = GLSLHelpers.createProgram(gl3, shaders);

			gl3.glGenVertexArrays(1, VAO, 0);
			gl3.glBindVertexArray(VAO[0]);

			gl3.glGenBuffers(1, VBO, 0);
			gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, VBO[0]);
			FloatBuffer buffer = GLBuffers.newDirectFloatBuffer(N * 12);

			for (int i = 0; i < N; i++) {
				double a = (i * Math.PI * 2) / N;
				double b = a + (Math.PI * 2) / N;

				add(buffer, R0, a);
				add(buffer, R0, b);
				add(buffer, R1, a);

				add(buffer, R0, b);
				add(buffer, R1, b);
				add(buffer, R1, a);
			}
			buffer.rewind();

			gl3.glBufferData(GL3.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GL3.GL_STATIC_DRAW);
			gl3.glEnableVertexAttribArray(0);
			gl3.glVertexAttribPointer(0, 2, GL3.GL_FLOAT, false, 0, 0);

			gl3.glBindVertexArray(0);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void dispose(GLAutoDrawable glad) {
		GL3 gl3 = glad.getGL().getGL3();

		gl3.glDeleteBuffers(1, VBO, 0);

		for (int shader : shaders)
			gl3.glDeleteShader(shader);

		gl3.glDeleteProgram(program);
	}

	@Override
	public void display(GLAutoDrawable glad) {
		GL3 gl3 = glad.getGL().getGL3();

		gl3.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl3.glClear(GL3.GL_COLOR_BUFFER_BIT);

		gl3.glUseProgram(program);

		gl3.glBindVertexArray(VAO[0]);
		gl3.glDrawArrays(GL3.GL_TRIANGLES, 0, N * 6);
		gl3.glBindVertexArray(0);

		gl3.glUseProgram(0);
	}

	@Override
	public void reshape(GLAutoDrawable glad, int x, int y, int w, int h) {
		glad.getGL().getGL3().glViewport(x, y, w, h);
	}
	
	private static void add(FloatBuffer buffer, double r, double a) {
		buffer.put((float) (r * Math.sin(a)));
		buffer.put((float) (r * Math.cos(a)));
	}	
}