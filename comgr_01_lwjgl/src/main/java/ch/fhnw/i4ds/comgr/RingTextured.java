package ch.fhnw.i4ds.comgr;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class RingTextured {

	private static final int N = 40;
	private static final double R0 = 0.5;
	private static final double R1 = 0.7;

	private GLFWErrorCallback errorCallback;

	private long window;

	private int program;
	private final List<Integer> shaders = new ArrayList<>();

	private int vao;
	private int vboPosition;
	private int vboTexCoord;

	private int positionAttribLocation;
	private int texCoordAttribLocation;

	private int aspectUniformLocation;
	private float aspect = 1;

	private int textureUniformLocation;
	private int texture;

	public static void main(String[] args) {
		new RingTextured().run();
	}

	private void run() {
		System.out.println("Texture Ring - LWJGL " + Version.getVersion());

		try {
			GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
			if (!GLFW.glfwInit())
				throw new IllegalStateException("Unable to initialize GLFW");

			initWindow();
			initScene();
			loop();
			destroyScene();

			Callbacks.glfwFreeCallbacks(window);
		} finally {
			GLFW.glfwTerminate();
			errorCallback.free();
		}
		
	}

	private void initWindow() {
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);

		int width = 300;
		int height = 300;

		window = GLFW.glfwCreateWindow(width, height, "Hello World!", MemoryUtil.NULL, MemoryUtil.NULL);
		if (window == MemoryUtil.NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
					GLFW.glfwSetWindowShouldClose(window, true);
			}
		});
		
		GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {		
			@Override
			public void invoke(long window, int width, int height) {
				reshape(width, height);
			}
		});

		GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		GLFW.glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);

		GLFW.glfwShowWindow(window);

		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
	}
	
	private void initScene() {
		try {
			// ---- core profile requires a vao
			vao = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vao);

			// ---- setup glsl program and uniforms
			shaders.add(GLSLHelpers.createShader(getClass(), GL20.GL_VERTEX_SHADER, "glsl/textured_vs"));
			shaders.add(GLSLHelpers.createShader(getClass(), GL20.GL_FRAGMENT_SHADER, "glsl/textured_fs"));
			program = GLSLHelpers.createProgram(shaders);

			positionAttribLocation = GL20.glGetAttribLocation(program, "position");
			texCoordAttribLocation = GL20.glGetAttribLocation(program, "texCoord");

			aspectUniformLocation = GL20.glGetUniformLocation(program, "aspect");
			textureUniformLocation = GL20.glGetUniformLocation(program, "colorMap");

			// ---- setup vertex buffers (two buffers, non-interleaved)
			FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(N * 12);
			FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(N * 12);

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

			vboPosition = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPosition);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(positionAttribLocation, 2, GL11.GL_FLOAT, false, 0, 0);
			GL20.glEnableVertexAttribArray(positionAttribLocation);

			vboTexCoord = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTexCoord);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(texCoordAttribLocation, 2, GL11.GL_FLOAT, false, 0, 0);
			GL20.glEnableVertexAttribArray(texCoordAttribLocation);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			// ---- setup texture
			// TextureBuffer tb = new
			// TextureBuffer(getClass().getResource("assets/pluto.jpg"), true);
			//
			// texture = GL11.glGenTextures();
			// GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			// GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
			// GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			// GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
			// GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			// GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
			// GL11.GL_REPEAT);
			// GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
			// GL11.GL_REPEAT);
			//
			// GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
			// tb.getBuffer().rewind();
			// GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
			// tb.getWidth(), tb.getHeight(), 0, tb.getFormat(),
			// GL11.GL_UNSIGNED_BYTE, tb.getBuffer());
			// GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			// GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);			

			
			GL30.glBindVertexArray(0);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("scene initialized");
	}
	
	private void destroyScene() {
		GL15.glDeleteBuffers(vboPosition);
		GL15.glDeleteBuffers(vboTexCoord);

		for (int shader : shaders)
			GL20.glDeleteShader(shader);

		GL20.glDeleteProgram(program);		
	}
	

	private void loop() {
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		while (GLFW.glfwWindowShouldClose(window) == false) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			GL20.glUseProgram(program);

			GL20.glUniform1f(aspectUniformLocation, aspect);

			GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			GL20.glUniform1i(textureUniformLocation, 0);

			GL30.glBindVertexArray(vao);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, N * 6);
			GL30.glBindVertexArray(0);

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);

			GL20.glUseProgram(0);

			
			GLFW.glfwSwapBuffers(window);

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			GLFW.glfwPollEvents();
		}
	}

	public void reshape(int w, int h) {
		aspect = (float) w / (float) h;
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
