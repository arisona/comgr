package ch.fhnw.i4ds.comgr;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.GLBuffers;

public class Triangle extends GLCanvas implements GLEventListener {
	private static final long serialVersionUID = -8933329638658421749L;

	private static final float[] TRIANGLE = { 0.0f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f };

	private List<Integer> shaders = new ArrayList<>();
	private int program;
	private final int[] VBO = new int[1];
	private final int[] VAO = new int[1];

	public static void main(String[] args) {
		Triangle triangle = new Triangle();
		JFrame frame = new JFrame("Comgr Triangle");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(triangle);
		frame.setSize(triangle.getWidth(), triangle.getHeight());
		frame.setVisible(true);
	}

	public Triangle() {
		super(new GLCapabilities(GLProfile.get(GLProfile.GL3)));
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
			FloatBuffer buffer = GLBuffers.newDirectFloatBuffer(TRIANGLE);
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

		gl3.glDeleteVertexArrays(1, VAO, 0);
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
		gl3.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);
		gl3.glBindVertexArray(0);
		
		gl3.glUseProgram(0);
	}

	@Override
	public void reshape(GLAutoDrawable glad, int x, int y, int w, int h) {
		glad.getGL().getGL3().glViewport(x, y, w, h);
	}
}