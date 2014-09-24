package ch.fhnw.i4ds.cg;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.BitSet;

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.GLBuffers;

public class Ring extends GLCanvas implements GLEventListener {
	private static final long serialVersionUID = -8933329638658421749L;

	private BitSet              shaders = new BitSet();
	private int                 material;
	private final int[]         VBO = new int[1];
	private final int[]         VAO = new int[1];
	private static final int    N   = 40;
	private static final double R0  = 0.5;
	private static final double R1  = 0.7;
	
	public static void main(String[] args) {
		Ring triangle = new Ring();
		Frame frame = new Frame("CG_01 Ring");
		frame.add(triangle);
		frame.setSize(triangle.getWidth(), triangle.getHeight());
		frame.setVisible(true);
	}

	public Ring() {
		super(new GLCapabilities(GLProfile.get("GL3")));
		setSize(800, 800);
		addGLEventListener(this);
	}

	private static void add(FloatBuffer buffer, double r, double a) {
		buffer.put((float)(r * Math.sin(a))); 
		buffer.put((float)(r * Math.cos(a))); 
	}
	
	@Override
	public void init(GLAutoDrawable glad) {
		final GL3 gl3 = glad.getGL().getGL3();
		
		try {
			shaders.set(createShader(gl3, GL3.GL_VERTEX_SHADER,   "simple_vs"));
			shaders.set(createShader(gl3, GL3.GL_FRAGMENT_SHADER, "simple_fs"));
			material = createProgram(gl3, shaders);

			gl3.glGenVertexArrays(1, VAO, 0);
			gl3.glBindVertexArray(VAO[0]);
			
			gl3.glGenBuffers(1, VBO, 0);
			gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, VBO[0]);
			FloatBuffer buffer = GLBuffers.newDirectFloatBuffer(N * 12);
			
			for(int i = 0; i < N; i++) {
				double a = (i * Math.PI * 2) / N;
				double b = a + (Math.PI * 2) / N;

				add(buffer, R0, a);
				add(buffer, R0, b);
				add(buffer, R1, a);
				
				add(buffer, R0, b);
				add(buffer, R1, b);
				add(buffer, R1, a);
			}
			buffer.clear();
			
			gl3.glBufferData(GL3.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GL3.GL_STATIC_DRAW);
			gl3.glEnableVertexAttribArray(0);
			gl3.glVertexAttribPointer(0, 2, GL3.GL_FLOAT, false, 0, 0);
			
			gl3.glBindVertexArray(0);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void dispose(GLAutoDrawable glad) {
		final GL3 gl3 = glad.getGL().getGL3();

		gl3.glDeleteBuffers(1, VBO, 0);

		for (int shader = shaders.nextSetBit(0); shader >= 0; shader = shaders.nextSetBit(shader+1))
			gl3.glDeleteShader(shader);

		gl3.glDeleteProgram(material);
	}

	@Override
	public void display(GLAutoDrawable glad) {
		GL3 gl3 = glad.getGL().getGL3();

		gl3.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl3.glClear(GL3.GL_COLOR_BUFFER_BIT);

		gl3.glUseProgram(material);
		gl3.glBindVertexArray(VAO[0]);
		gl3.glDrawArrays(GL3.GL_TRIANGLES, 0, N * 6);
		gl3.glBindVertexArray(0);
		gl3.glUseProgram(0);
	}

	@Override
	public void reshape(GLAutoDrawable glad, int x, int y, int w, int h) {
		glad.getGL().getGL3().glViewport(x, y, w, h);
	}

private final int createShader(GL3 gl3, int type, String filename) throws IOException {
	BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename + ".glsl")));
	String content = "";
	String line;
	while ((line = in.readLine()) != null)
		content += line + "\n";
	in.close();

	int result = gl3.glCreateShader(type);

	gl3.glShaderSource(result, 1, new String[]{content}, new int[]{content.length()}, 0);
	gl3.glCompileShader(result);

	checkStatus(gl3, result, GL3.GL_COMPILE_STATUS);
	return result;
}

private final int createProgram(GL3 gl3, BitSet shaders) {
	int result = gl3.glCreateProgram();

	for (int shader = shaders.nextSetBit(0); shader >= 0; shader = shaders.nextSetBit(shader+1))
		gl3.glAttachShader(result, shader);

	gl3.glLinkProgram(result);
	checkStatus(gl3, result, GL3.GL_LINK_STATUS);
	gl3.glValidateProgram(result);

	return result;
}

private void checkStatus(GL3 gl3, int object, int status) {
	int[] params = {0};

	if(status == GL3.GL_COMPILE_STATUS)
		gl3.glGetShaderiv(object, status, params, 0);
	else if(status == GL3.GL_LINK_STATUS)
		gl3.glGetProgramiv(object, status, params, 0);

	if (params[0] != 1) {
		System.err.println("status: " + params[0]);
		gl3.glGetShaderiv(object, GL3.GL_INFO_LOG_LENGTH, params, 0);
		byte[] infoLog = new byte[params[0]];
		if(status == GL3.GL_COMPILE_STATUS)
			gl3.glGetShaderInfoLog(object, params[0], params, 0, infoLog, 0);
		else if(status == GL3.GL_LINK_STATUS)
			gl3.glGetProgramInfoLog(object, params[0], params, 0, infoLog, 0);
		System.err.println(new String(infoLog));
		System.exit(-1);
	}
}
}