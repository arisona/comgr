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
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class Ring {

	private static final int N = 100;
	private static final double R0 = 0.5;
	private static final double R1 = 0.7;

	private long window;

	private int program;
	private final List<Integer> shaders = new ArrayList<>();

	private int vao;
	private int vboPosition;
	private int vboColor;

	private int positionAttribLocation;
	private int colorAttribLocation;

	private int aspectUniformLocation;
	private int width;
	private int height;
	private float aspect = 1;

	public static void main(String[] args) {
		new Ring().run();
	}

	private void run() {
		System.out.println("Ring - LWJGL " + Version.getVersion());

		try (GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err)) {
			GLFW.glfwSetErrorCallback(errorCallback);
			if (!GLFW.glfwInit())
				throw new IllegalStateException("Unable to initialize GLFW");

			initWindow();
			initScene();
			loop();
			destroyScene();

			Callbacks.glfwFreeCallbacks(window);
		} finally {
			GLFW.glfwTerminate();
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

		window = GLFW.glfwCreateWindow(16, 16, "OpenGL Ring", MemoryUtil.NULL, MemoryUtil.NULL);
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
		GLFW.glfwSetWindowSize(window, width, height);
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
			shaders.add(GLSLHelpers.createShader(getClass(), GL20.GL_VERTEX_SHADER, "/shaders/colored_vs"));
			shaders.add(GLSLHelpers.createShader(getClass(), GL20.GL_FRAGMENT_SHADER, "/shaders/colored_fs"));
			program = GLSLHelpers.createProgram(shaders);

			positionAttribLocation = GL20.glGetAttribLocation(program, "position");
			colorAttribLocation = GL20.glGetAttribLocation(program, "color");

			aspectUniformLocation = GL20.glGetUniformLocation(program, "aspect");

			// ---- setup vertex buffers (two buffers, non-interleaved)
			FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(N * 12);
			FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(N * 24);

			for (int i = 0; i < N; i++) {
				double a = (i * Math.PI * 2) / N;
				double b = a + (Math.PI * 2) / N;

				addPosition(positionBuffer, R0, a);
				addPosition(positionBuffer, R0, b);
				addColor(colorBuffer, R0, a);
				addColor(colorBuffer, R0, b);

				addPosition(positionBuffer, R1, a);
				addPosition(positionBuffer, R0, b);
				addColor(colorBuffer, R1, a);
				addColor(colorBuffer, R0, b);

				addPosition(positionBuffer, R1, b);
				addPosition(positionBuffer, R1, a);
				addColor(colorBuffer, R1, b);
				addColor(colorBuffer, R1, a);
			}
			positionBuffer.rewind();
			colorBuffer.rewind();

			vboPosition = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPosition);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(positionAttribLocation, 2, GL11.GL_FLOAT, false, 0, 0);
			GL20.glEnableVertexAttribArray(positionAttribLocation);

			vboColor = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColor);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(colorAttribLocation, 2, GL11.GL_FLOAT, false, 0, 0);
			GL20.glEnableVertexAttribArray(colorAttribLocation);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			GL30.glBindVertexArray(0);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("scene initialized");
	}
	
	private void destroyScene() {
		GL15.glDeleteBuffers(vboPosition);
		GL15.glDeleteBuffers(vboColor);

		for (int shader : shaders)
			GL20.glDeleteShader(shader);

		GL20.glDeleteProgram(program);		
	}

	private void loop() {
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		while (GLFW.glfwWindowShouldClose(window) == false) {
			GL11.glViewport(0, 0, width, height);
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			GL20.glUseProgram(program);

			GL20.glUniform1f(aspectUniformLocation, aspect);

			GL30.glBindVertexArray(vao);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, N * 6);
			GL30.glBindVertexArray(0);

			GL20.glUseProgram(0);
			
			GLFW.glfwSwapBuffers(window);

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			GLFW.glfwPollEvents();
		}
	}

	public void reshape(int w, int h) {
		width = w;
		height = h;
		aspect = (float) w / (float) h;
	}

	private static void addPosition(FloatBuffer buffer, double r, double a) {
		buffer.put((float) (r * Math.sin(a)));
		buffer.put((float) (r * Math.cos(a)));
	}

	private static void addColor(FloatBuffer buffer, double r, double a) {
		buffer.put((float) (0.5 + 0.5 * r * Math.sin(a)));
		buffer.put((float) (0.5 + 0.5 * r * Math.cos(a)));
		buffer.put((float) (0.5 + 0.5 * r * Math.sin(a)));
		buffer.put((float) (0.5 + 0.5 * r * Math.cos(a)));
	}
}
