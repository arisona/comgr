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

import ch.fhnw.i4ds.comgr.TextureHelpers.TextureBuffer;

import com.jogamp.opengl.util.GLBuffers;

public class RingTextured extends GLCanvas implements GLEventListener {
	private static final long serialVersionUID = -8933329638658421749L;

	private static final int N = 40;
	private static final double R0 = 0.5;
	private static final double R1 = 0.7;

	private int program;
	private final List<Integer> shaders = new ArrayList<>();

	private final int[] vao = new int[1];
	private final int[] vbo = new int[2];
	
	private int positionAttribLocation;
	private int texCoordAttribLocation;
	
	private int aspectUniformLocation;
	private float aspect = 1;
	
	private int textureUniformLocation;
	private final int[] texture = new int[1];
	

	public static void main(String[] args) {
		RingTextured triangle = new RingTextured();
		Frame frame = new Frame("Comgr Ring Textured");
		frame.add(triangle);
		frame.setSize(triangle.getWidth(), triangle.getHeight());
		frame.setVisible(true);
	}

	public RingTextured() {
		super(new GLCapabilities(GLProfile.get("GL3")));
		setSize(800, 800);
		addGLEventListener(this);		
	}

	@Override
	public void init(GLAutoDrawable glad) {
		final GL3 gl3 = glad.getGL().getGL3();

		try {
			//---- setup glsl program and uniforms
			shaders.add(GLSLHelpers.createShader(gl3, getClass(), GL3.GL_VERTEX_SHADER, "glsl/textured_vs"));
			shaders.add(GLSLHelpers.createShader(gl3, getClass(), GL3.GL_FRAGMENT_SHADER, "glsl/textured_fs"));
			program = GLSLHelpers.createProgram(gl3, shaders);
			
			positionAttribLocation = gl3.glGetAttribLocation(program, "position");
			texCoordAttribLocation = gl3.glGetAttribLocation(program, "texCoord");

			aspectUniformLocation = gl3.glGetUniformLocation(program, "aspect");
			textureUniformLocation = gl3.glGetUniformLocation(program, "colorMap");
	
			
			//---- setup vertex buffers (two buffers, non-interleaved)
			FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(N * 12);
			FloatBuffer texCoordBuffer = GLBuffers.newDirectFloatBuffer(N * 12);

			for (int i = 0; i < N; i++) {
				double a = (i * Math.PI * 2) / N;
				double b = a + (Math.PI * 2) / N;

				addPosition(positionBuffer, R0, a);
				addPosition(positionBuffer, R0, b);
				addTexCoord(texCoordBuffer, R0, a);
				addTexCoord(texCoordBuffer, R0, b);
				
				addPosition(positionBuffer, R1, a);
				addPosition(positionBuffer, R0, b);
				addTexCoord(texCoordBuffer, R1, a);
				addTexCoord(texCoordBuffer, R0, b);

				addPosition(positionBuffer, R1, b);
				addPosition(positionBuffer, R1, a);
				addTexCoord(texCoordBuffer, R1, b);
				addTexCoord(texCoordBuffer, R1, a);
			}
			positionBuffer.rewind();
			texCoordBuffer.rewind();

			gl3.glGenVertexArrays(1, vao, 0);
			gl3.glBindVertexArray(vao[0]);

			gl3.glGenBuffers(2, vbo, 0);

			gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo[0]);
			gl3.glBufferData(GL3.GL_ARRAY_BUFFER, positionBuffer.capacity() * 4, positionBuffer, GL3.GL_STATIC_DRAW);
			gl3.glVertexAttribPointer(positionAttribLocation, 2, GL3.GL_FLOAT, false, 0, 0);
			gl3.glEnableVertexAttribArray(positionAttribLocation);

			gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo[1]);
			gl3.glBufferData(GL3.GL_ARRAY_BUFFER, texCoordBuffer.capacity() * 4, texCoordBuffer, GL3.GL_STATIC_DRAW);
			gl3.glVertexAttribPointer(texCoordAttribLocation, 2, GL3.GL_FLOAT, false, 0, 0);
			gl3.glEnableVertexAttribArray(texCoordAttribLocation);
			
			gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
			gl3.glBindVertexArray(0);
			
			
			//---- setup texture
			TextureBuffer tb = new TextureBuffer(getClass().getResource("assets/philae.png"), true);

			gl3.glGenTextures(1, texture, 0);
			gl3.glBindTexture(GL3.GL_TEXTURE_2D, texture[0]);
			gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
			gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR_MIPMAP_LINEAR);
			gl3.glTexParameterf(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT);
			gl3.glTexParameterf(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT);

			gl3.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);
			tb.getBuffer().rewind();
			gl3.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, tb.getWidth(), tb.getHeight(), 0, tb.getFormat(), GL3.GL_UNSIGNED_BYTE, tb.getBuffer());
			gl3.glGenerateMipmap(GL3.GL_TEXTURE_2D);
			gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void dispose(GLAutoDrawable glad) {
		final GL3 gl3 = glad.getGL().getGL3();

		gl3.glDeleteBuffers(2, vbo, 0);

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
		
		gl3.glUniform1f(aspectUniformLocation, aspect);
		
		gl3.glActiveTexture(GL3.GL_TEXTURE0 + 0);
		gl3.glBindTexture(GL3.GL_TEXTURE_2D, texture[0]);
		gl3.glUniform1i(textureUniformLocation, 0);

		gl3.glBindVertexArray(vao[0]);
		gl3.glDrawArrays(GL3.GL_TRIANGLES, 0, N * 6);
		gl3.glBindVertexArray(0);

		gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);
		gl3.glActiveTexture(GL3.GL_TEXTURE0);
		
		gl3.glUseProgram(0);
	}

	@Override
	public void reshape(GLAutoDrawable glad, int x, int y, int w, int h) {
		glad.getGL().getGL3().glViewport(x, y, w, h);
		aspect = (float)w / (float)h;
	}
	
	private static void addPosition(FloatBuffer buffer, double r, double a) {
		buffer.put((float) (r * Math.sin(a)));
		buffer.put((float) (r * Math.cos(a)));
	}	

	private static void addTexCoord(FloatBuffer buffer, double r, double a) {
		buffer.put((float) (0.5 + 0.5 * r * Math.sin(a)));
		buffer.put((float) (0.5 + 0.5 * r * Math.cos(a)));
	}	
}