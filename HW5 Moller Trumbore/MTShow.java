package mchhui.mollertrumbore;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import java.nio.FloatBuffer;

public class MTShow {
	public static class Primitive {
		public Vector4f pos1;
		public Vector4f pos2;
		public Vector4f pos3;
		public Vector2f uv1;
		public Vector2f uv2;
		public Vector2f uv3;
		public Vector4f color1;
		public Vector4f color2;
		public Vector4f color3;

		public Primitive pos(Vector4f pos1, Vector4f pos2, Vector4f pos3) {
			this.pos1 = pos1;
			this.pos2 = pos2;
			this.pos3 = pos3;
			return this;
		}

		public Primitive uv(Vector2f uv1, Vector2f uv2, Vector2f uv3) {
			this.uv1 = uv1;
			this.uv2 = uv2;
			this.uv3 = uv3;
			return this;
		}

		public Primitive color(Vector4f color1, Vector4f color2, Vector4f color3) {
			this.color1 = color1;
			this.color2 = color2;
			this.color3 = color3;
			return this;
		}
	}

	public static void main(String[] args) {
		GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);
		errorCallback.set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

		long window = GLFW.glfwCreateWindow(1600, 900, "LWJGL Window", 0, 0);
		if (window == 0L) {
			GLFW.glfwTerminate();
			errorCallback.free();
			throw new RuntimeException("Failed to create GLFW window");
		}

		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();
		GLFW.glfwSwapInterval(1); // vsync
		GLFW.glfwShowWindow(window);

		// 设置鼠标回调
		setupMouseCallbacks(window);

		int[] width = new int[1];
		int[] height = new int[1];
		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwGetWindowSize(window, width, height);
			doRender(width[0], height[0]);
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}

		// 释放鼠标回调
		if (mouseButtonCallback != null) {
			mouseButtonCallback.free();
		}
		if (cursorPosCallback != null) {
			cursorPosCallback.free();
		}
		if (scrollCallback != null) {
			scrollCallback.free();
		}

		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
		errorCallback.free();
	}

	public static float yaw = 0;
	public static float pitch = 0;
	public static float distance = 5;

	public static void setupCamera(int width, int height) {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		Matrix4f projection = new Matrix4f();
		projection.perspective((float) Math.toRadians(70.0f), width / (float) height, 0.1f, 10000.0f);
		FloatBuffer matrixBuffer = org.lwjgl.BufferUtils.createFloatBuffer(16);
		projection.get(matrixBuffer);
		GL11.glLoadMatrixf(matrixBuffer);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslated(0, 0, -distance);
		GL11.glRotated(pitch, 1, 0, 0);
		GL11.glRotated(yaw, 0, 1, 0);
	}

	public static Primitive primitive = new Primitive();
	static {
		Vector4f pos1 = new Vector4f(0, 0, 0, 1);
		Vector4f pos2 = new Vector4f(3, 0, 0, 1);
		Vector4f pos3 = new Vector4f(0, 3, 0, 1);
		Vector2f uv1 = new Vector2f(0, 0);
		Vector2f uv2 = new Vector2f(1, 0);
		Vector2f uv3 = new Vector2f(0, 1);
		Vector4f color1 = new Vector4f(1, 0, 0, 1);
		Vector4f color2 = new Vector4f(0, 1, 0, 1);
		Vector4f color3 = new Vector4f(0, 0, 1, 1);
		primitive.pos(pos1, pos2, pos3).uv(uv1, uv2, uv3).color(color1, color2, color3);
	}

	public static void drawMollerTrumbore() {
		double alpha = System.currentTimeMillis() % 10000 / 10000d;
		float alpha1 = (float) Math.cos(2 * Math.PI * (System.currentTimeMillis() % 5000 / 5000d));
		float alpha2 = (float) Math.sin(2 * Math.PI * (System.currentTimeMillis() % 5000 / 5000d));
		Vector4f pos1 = new Vector4f(primitive.pos1.x, primitive.pos1.y, primitive.pos1.z, primitive.pos1.w);
		Vector4f pos2 = new Vector4f(primitive.pos2.x, primitive.pos2.y, primitive.pos2.z, primitive.pos2.w);
		Vector4f pos3 = new Vector4f(primitive.pos3.x, primitive.pos3.y, primitive.pos3.z, primitive.pos3.w);
		Matrix4f transform = new Matrix4f();
		transform.translate(-1f, -1f, -2);
		transform.translate(-0.1f*alpha1,-0.1f*alpha1, 0);
		transform.translate(0.1f*alpha2,0.1f*alpha2, 0);
		transform.rotate((float) Math.toRadians(30 * (float) (1 * Math.sin(Math.toRadians((360 * alpha))))), 0, 1, 0);
		pos1.mul(transform);
		pos2.mul(transform);
		pos3.mul(transform);

		GL11.glBegin(GL11.GL_TRIANGLES);
		GL11.glColor4f(primitive.color1.x, primitive.color1.y, primitive.color1.z, primitive.color1.w);
		GL11.glVertex3f(pos1.x, pos1.y, pos1.z);
		GL11.glColor4f(primitive.color2.x, primitive.color2.y, primitive.color2.z, primitive.color2.w);
		GL11.glVertex3f(pos2.x, pos2.y, pos2.z);
		GL11.glColor4f(primitive.color3.x, primitive.color3.y, primitive.color3.z, primitive.color3.w);
		GL11.glVertex3f(pos3.x, pos3.y, pos3.z);
		GL11.glEnd();

		float r=(float)Math.abs(0.6*Math.sin(Math.PI*alpha));
		Vector4f start = new Vector4f(0, 0, 0, 1);
		Vector4f direction = new Vector4f(r * alpha1, r * alpha2, -1, 0).normalize();
		Vector4f result = mollerTrumbore(pos1, pos2, pos3, start, direction);
		Vector4f hit = new Vector4f();
		hit.add(pos1.mul(result.x));
		hit.add(pos2.mul(result.y));
		hit.add(pos3.mul(result.z));
		boolean hited = result.x >= 0 && result.y >= 0 && result.z >= 0 && result.w >= 0;
		Vector4f color = new Vector4f(0, 0, 0, 0);
		if (hited) {
			Vector4f temp = new Vector4f();
			color.add(primitive.color1.mul(result.x, temp));
			color.add(primitive.color2.mul(result.y, temp));
			color.add(primitive.color3.mul(result.z, temp));
//			System.out.println(color);
		}
		GL11.glLineWidth(5);
		if (hited) {
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor4f(color.x, color.y, color.z, color.w);
			GL11.glVertex3f(start.x, start.y, start.z);
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glVertex3f(hit.x, hit.y, hit.z);
			GL11.glEnd();
		} else {
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glVertex3f(start.x, start.y, start.z);
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glVertex3f(start.x + direction.x * 10, start.y + direction.y * 10, start.z + direction.z * 10);
			GL11.glEnd();
		}
	}

	public static Vector4f mollerTrumbore(Vector4f pos1, Vector4f pos2, Vector4f pos3, Vector4f start,
			Vector4f direction) {
		Vector4f E1 = new Vector4f();
		Vector4f E2 = new Vector4f();
		Vector4f S = new Vector4f();
		Vector4f S1 = new Vector4f();
		Vector4f S2 = new Vector4f();
		pos2.sub(pos1, E1);
		pos3.sub(pos1, E2);
		start.sub(pos1, S);
		Vector3f temp1 = new Vector3f();
		Vector3f temp2 = new Vector3f();
		direction.xyz(temp1);
		E2.xyz(temp2);
		S1.set(temp1.cross(temp2), 0);
		S.xyz(temp1);
		E1.xyz(temp2);
		S2.set(temp1.cross(temp2), 0);
		Vector4f result = new Vector4f(S2.dot(E2), S1.dot(S), S2.dot(direction), 0);
		result.div(S1.dot(E1));
		return result.set(1 - result.y - result.z, result.y, result.z, result.x);
	}

	public static void drawGround() {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(0.5f, 0.5f, 0.5f, 1);
		GL11.glVertex3f(-10, -3, -10);
		GL11.glVertex3f(10, -3, -10);
		GL11.glVertex3f(10, -3, 10);
		GL11.glVertex3f(-10, -3, 10);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glPolygonOffset(-1, -1);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(0.1f, 0.1f, 0.1f, 1);
		GL11.glVertex3f(-9, -3, -9);
		GL11.glVertex3f(9, -3, -9);
		GL11.glVertex3f(9, -3, 9);
		GL11.glVertex3f(-9, -3, 9);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
	}

	public static void doRender(int width, int height) {
		GL11.glViewport(0, 0, width, height);
		GL11.glClearColor(0f, 0f, 0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		setupCamera(width, height);
		drawGround();
		drawMollerTrumbore();
	}

	// 鼠标控制相关变量
	private static boolean isRightMousePressed = false;
	private static double lastMouseX = 0;
	private static double lastMouseY = 0;
	private static boolean firstMouse = true;
	private static final float MOUSE_SENSITIVITY = 0.1f;
	private static final float SCROLL_SENSITIVITY = 0.5f;

	// 鼠标回调对象（需要保存引用防止被GC）
	private static GLFWMouseButtonCallback mouseButtonCallback;
	private static GLFWCursorPosCallback cursorPosCallback;
	private static GLFWScrollCallback scrollCallback;

	private static void setupMouseCallbacks(long window) {
		// 鼠标按钮回调（右键控制旋转）
		mouseButtonCallback = GLFWMouseButtonCallback.create((w, button, action, mods) -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
				if (action == GLFW.GLFW_PRESS) {
					isRightMousePressed = true;
					GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
				} else if (action == GLFW.GLFW_RELEASE) {
					isRightMousePressed = false;
					GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
					firstMouse = true;
				}
			}
		});

		// 鼠标移动回调（旋转相机）
		cursorPosCallback = GLFWCursorPosCallback.create((w, xpos, ypos) -> {
			if (isRightMousePressed) {
				if (firstMouse) {
					lastMouseX = xpos;
					lastMouseY = ypos;
					firstMouse = false;
				}

				double xoffset = xpos - lastMouseX;
				double yoffset = lastMouseY - ypos; // 反转Y轴

				lastMouseX = xpos;
				lastMouseY = ypos;

				yaw += (float) (xoffset * MOUSE_SENSITIVITY);
				pitch -= (float) (yoffset * MOUSE_SENSITIVITY);

				// 限制pitch角度，防止翻转
				if (pitch > 89.0f) {
					pitch = 89.0f;
				}
				if (pitch < -89.0f) {
					pitch = -89.0f;
				}
			}
		});

		// 鼠标滚轮回调（缩放距离）
		scrollCallback = GLFWScrollCallback.create((w, xoffset, yoffset) -> {
			distance -= (float) (yoffset * SCROLL_SENSITIVITY);
			if (distance < 0.1f) {
				distance = 0.1f;
			}
			if (distance > 1000.0f) {
				distance = 1000.0f;
			}
		});

		GLFW.glfwSetMouseButtonCallback(window, mouseButtonCallback);
		GLFW.glfwSetCursorPosCallback(window, cursorPosCallback);
		GLFW.glfwSetScrollCallback(window, scrollCallback);
	}
}
