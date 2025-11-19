package mchhui.booststructure;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import java.nio.FloatBuffer;

public class AABBShow {
	public static void main(String[] args) {
		GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);
		errorCallback.set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

		long window = GLFW.glfwCreateWindow(1600, 900, "AABB Intersection Demo", 0, 0);
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

	// 创建一个AABB对象
	public static AABB aabb = new AABB(-1, -1, -1, 1, 1, 1);

	public static void drawAABB() {
		// 绘制AABB线框
		GL11.glLineWidth(2.0f);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor4f(0, 1, 0, 1); // 绿色

		// 前面
		GL11.glVertex3f(aabb.minX, aabb.minY, aabb.maxZ);
		GL11.glVertex3f(aabb.maxX, aabb.minY, aabb.maxZ);
		GL11.glVertex3f(aabb.maxX, aabb.minY, aabb.maxZ);
		GL11.glVertex3f(aabb.maxX, aabb.maxY, aabb.maxZ);
		GL11.glVertex3f(aabb.maxX, aabb.maxY, aabb.maxZ);
		GL11.glVertex3f(aabb.minX, aabb.maxY, aabb.maxZ);
		GL11.glVertex3f(aabb.minX, aabb.maxY, aabb.maxZ);
		GL11.glVertex3f(aabb.minX, aabb.minY, aabb.maxZ);

		// 后面
		GL11.glVertex3f(aabb.minX, aabb.minY, aabb.minZ);
		GL11.glVertex3f(aabb.maxX, aabb.minY, aabb.minZ);
		GL11.glVertex3f(aabb.maxX, aabb.minY, aabb.minZ);
		GL11.glVertex3f(aabb.maxX, aabb.maxY, aabb.minZ);
		GL11.glVertex3f(aabb.maxX, aabb.maxY, aabb.minZ);
		GL11.glVertex3f(aabb.minX, aabb.maxY, aabb.minZ);
		GL11.glVertex3f(aabb.minX, aabb.maxY, aabb.minZ);
		GL11.glVertex3f(aabb.minX, aabb.minY, aabb.minZ);

		// 连接前后
		GL11.glVertex3f(aabb.minX, aabb.minY, aabb.minZ);
		GL11.glVertex3f(aabb.minX, aabb.minY, aabb.maxZ);
		GL11.glVertex3f(aabb.maxX, aabb.minY, aabb.minZ);
		GL11.glVertex3f(aabb.maxX, aabb.minY, aabb.maxZ);
		GL11.glVertex3f(aabb.minX, aabb.maxY, aabb.minZ);
		GL11.glVertex3f(aabb.minX, aabb.maxY, aabb.maxZ);
		GL11.glVertex3f(aabb.maxX, aabb.maxY, aabb.minZ);
		GL11.glVertex3f(aabb.maxX, aabb.maxY, aabb.maxZ);

		GL11.glEnd();
	}

	// 辅助方法：绘制单条射线
	private static void drawSingleRay(Vector4f start, Vector4f direction) {
		// 使用AABB的intersect方法检测相交
		float t = aabb.intersect(start, direction);
		boolean hited = t < Float.MAX_VALUE;

		// 计算交点
		Vector4f hit = new Vector4f();
		if (hited) {
			hit.set(start.x + direction.x * t, start.y + direction.y * t, start.z + direction.z * t, 1);
		}

		// 绘制射线
		if (hited) {
			// 相交时，绘制从起点到交点的线（红色）
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor4f(1, 0, 0, 1); // 红色
			GL11.glVertex3f(start.x, start.y, start.z);
			GL11.glColor4f(1, 1, 0, 1); // 黄色（交点）
			GL11.glVertex3f(hit.x, hit.y, hit.z);
			GL11.glEnd();

			// 绘制交点（小球）
			GL11.glPointSize(10);
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glColor4f(1, 1, 0, 1); // 黄色
			GL11.glVertex3f(hit.x, hit.y, hit.z);
			GL11.glEnd();
		} else {
			// 不相交时，绘制完整的射线（白色）
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor4f(1, 1, 1, 1); // 白色
			GL11.glVertex3f(start.x, start.y, start.z);
			GL11.glColor4f(0.5f, 0.5f, 0.5f, 1); // 灰色
			GL11.glVertex3f(start.x + direction.x * 10, start.y + direction.y * 10, start.z + direction.z * 10);
			GL11.glEnd();
		}
	}

	public static void drawRayIntersection() {
		GL11.glLineWidth(5);

		// 计算AABB的中心点，用于确定射线起点位置
		float centerX = (aabb.minX + aabb.maxX) / 2.0f;
		float centerY = (aabb.minY + aabb.maxY) / 2.0f;
		float centerZ = (aabb.minZ + aabb.maxZ) / 2.0f;
		float size = 4.0f; // 射线起点距离AABB的距离

		// 获取时间，用于动画（加快速度）
		long time = System.currentTimeMillis();
		double time1 = (time % 3000) / 3000.0; // 加快到3秒周期
		double time2 = ((time + 500) % 3000) / 3000.0; // 偏移1/6周期
		double time3 = ((time + 1000) % 3000) / 3000.0; // 偏移2/6周期
		double time4 = ((time + 1500) % 3000) / 3000.0; // 偏移3/6周期
		double time5 = ((time + 2000) % 3000) / 3000.0; // 偏移4/6周期
		double time6 = ((time + 2500) % 3000) / 3000.0; // 偏移5/6周期

		// 旋转角度（使用不同的周期和相位）
		float angle1 = (float) (2 * Math.PI * time1);
		float angle2 = (float) (2 * Math.PI * time2);
		float angle3 = (float) (2 * Math.PI * time3);
		float angle4 = (float) (2 * Math.PI * time4);
		float angle5 = (float) (2 * Math.PI * time5);
		float angle6 = (float) (2 * Math.PI * time6);

		// 旋转半径（增大半径，让射线更容易错过AABB）
		float radius = 20f;

		// 1. 前面（+Z方向）：从前方射向AABB，围绕Z轴旋转
		float offsetX1 = (float) (radius * Math.cos(angle1));
		float offsetY1 = (float) (radius * Math.sin(angle1));
		Vector4f start1 = new Vector4f(centerX + offsetX1, centerY + offsetY1, centerZ + size, 1);
		Vector4f direction1 = new Vector4f(centerX - start1.x, centerY - start1.y, centerZ - start1.z, 0).normalize();
		drawSingleRay(start1, direction1);

		// 2. 后面（-Z方向）：从后方射向AABB，围绕Z轴旋转
		float offsetX2 = (float) (radius * Math.cos(angle2));
		float offsetY2 = (float) (radius * Math.sin(angle2));
		Vector4f start2 = new Vector4f(centerX + offsetX2, centerY + offsetY2, centerZ - size, 1);
		Vector4f direction2 = new Vector4f(centerX - start2.x, centerY - start2.y, centerZ - start2.z, 0).normalize();
		drawSingleRay(start2, direction2);

		// 3. 右面（+X方向）：从右侧射向AABB，围绕X轴旋转
		float offsetY3 = (float) (radius * Math.cos(angle3));
		float offsetZ3 = (float) (radius * Math.sin(angle3));
		Vector4f start3 = new Vector4f(centerX + size, centerY + offsetY3, centerZ + offsetZ3, 1);
		Vector4f direction3 = new Vector4f(centerX - start3.x, centerY - start3.y, centerZ - start3.z, 0).normalize();
		drawSingleRay(start3, direction3);

		// 4. 左面（-X方向）：从左侧射向AABB，围绕X轴旋转
		float offsetY4 = (float) (radius * Math.cos(angle4));
		float offsetZ4 = (float) (radius * Math.sin(angle4));
		Vector4f start4 = new Vector4f(centerX - size, centerY + offsetY4, centerZ + offsetZ4, 1);
		Vector4f direction4 = new Vector4f(centerX - start4.x, centerY - start4.y, centerZ - start4.z, 0).normalize();
		drawSingleRay(start4, direction4);

		// 5. 上面（+Y方向）：从上方射向AABB，围绕Y轴旋转
		float offsetX5 = (float) (radius * Math.cos(angle5));
		float offsetZ5 = (float) (radius * Math.sin(angle5));
		Vector4f start5 = new Vector4f(centerX + offsetX5, centerY + size, centerZ + offsetZ5, 1);
		Vector4f direction5 = new Vector4f(centerX - start5.x, centerY - start5.y, centerZ - start5.z, 0).normalize();
		drawSingleRay(start5, direction5);

		// 6. 下面（-Y方向）：从下方射向AABB，围绕Y轴旋转
		float offsetX6 = (float) (radius * Math.cos(angle6));
		float offsetZ6 = (float) (radius * Math.sin(angle6));
		Vector4f start6 = new Vector4f(centerX + offsetX6, centerY - size, centerZ + offsetZ6, 1);
		Vector4f direction6 = new Vector4f(centerX - start6.x, centerY - start6.y, centerZ - start6.z, 0).normalize();
		drawSingleRay(start6, direction6);
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
		drawAABB();
		drawRayIntersection();
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
