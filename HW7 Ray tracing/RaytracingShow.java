package mchhui.raytracing;

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

import mchhui.booststructure.AABB;
import mchhui.booststructure.BVH;
import mchhui.objloader.Face;
import mchhui.objloader.GroupObject;
import mchhui.objloader.Vertex;
import mchhui.objloader.WavefrontObject;
import mchhui.textureengine.TextureEngine;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RaytracingShow {
	public static ArrayList<Runnable> tasks = new ArrayList<Runnable>();
	public static long windowHandle = 0;

	public static void main(String[] args) {
		Menu.init(args);
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
		windowHandle = window;

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
			tasks.forEach(Runnable::run);
			tasks.clear();
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

	public static void addTask(Runnable run) {
		tasks.add(run);
	}

	public static float yaw = 0;
	public static float pitch = 0;
	public static float distance = 15;
	public static float fov = 70;

	public static void setupCamera(int width, int height) {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		Matrix4f projection = new Matrix4f();
		projection.perspective((float) Math.toRadians(fov), width / (float) height, 0.1f, 10000.0f);
		FloatBuffer matrixBuffer = org.lwjgl.BufferUtils.createFloatBuffer(16);
		projection.get(matrixBuffer);
		GL11.glLoadMatrixf(matrixBuffer);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslated(0, 0, -distance);
		GL11.glRotated(pitch, 1, 0, 0);
		GL11.glRotated(yaw, 0, 1, 0);
	}

	public static Vector4f testStart;
	public static Vector4f testDirection;
	public static Vector4f testResult;
	public static Vector4f hit;

	public static void drawMollerTrumbore() {
		if (testStart == null) {
			return;
		}
		Vector4f start = testStart;
		Vector4f direction = testDirection;

		if (testResult == null || testResult.w > 10) {
			GL11.glLineWidth(5);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glVertex3f(start.x, start.y, start.z);
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glVertex3f(start.x + direction.x * 10, start.y + direction.y * 10, start.z + direction.z * 10);
			GL11.glEnd();
		} else {
			GL11.glLineWidth(5);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor4f(testResult.x, testResult.y, testResult.z, 1);
			GL11.glVertex3f(start.x, start.y, start.z);
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glVertex3f(hit.x, hit.y, hit.z);
			GL11.glEnd();
		}
	}

	public static SimpleEntry<Vector4f, Face> ray(Vector4f start, Vector4f direction) {
		ArrayList<AABB> list = new ArrayList<AABB>();
		bvh.intersect(start, direction, list);
		float minT = Float.MAX_VALUE;
		Face face = null;
		Vector4f result = new Vector4f();
		for (int i = 0; i < list.size(); i++) {
			AABB temp = list.get(i);
			if (temp.intersect(start, direction) >= 0) {
				Face tf = AABBtoFace.get(temp);
				Vector4f mt = mollerTrumbore(tf, start, direction);
				if (mt.x > 0 && mt.y > 0 && mt.z > 0 && mt.w > 0) {
					if (mt.w < minT) {
						minT = mt.w;
						face = tf;
						result = mt;
					}
				}
			}
		}
		return new SimpleEntry<Vector4f, Face>(result, face);
	}

	public static Vector4f rayColor(Vector4f start, Vector4f direction) {
		SimpleEntry<Vector4f, Face> r = ray(start, direction);
		Face face = r.getValue();
		Vector4f result = r.getKey();
		if (face != null) {
			if (face.isLight) {
				result.x = face.color.x;
				result.y = face.color.y;
				result.z = face.color.z;
			} else {
				float u = face.textureCoordinates[0].u * result.x + face.textureCoordinates[1].u * result.y
						+ face.textureCoordinates[2].u * result.z;
				float v = face.textureCoordinates[0].v * result.x + face.textureCoordinates[1].v * result.y
						+ face.textureCoordinates[2].v * result.z;
				int[] color = TextureEngine.sample("./football_ball_basecolor.png", u, v);
				result.x = color[0] / 255f;
				result.y = color[1] / 255f;
				result.z = color[2] / 255f;
			}
		}
		return result;
	}

	public static Vector4f AMBIENT_LIGHT = new Vector4f(0.2f, 0.2f, 0.2f, 0);
//	public static Vector4f AMBIENT_LIGHT = new Vector4f(1f, 1f, 1f, 0);

	private static Random random = new Random();

	public static Vector4f randomReflect(Vector4f normal) {
		Vector4f vec = new Vector4f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, 0);
		while (vec.dot(normal) < 0) {
			vec = new Vector4f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, 0);
		}
		return vec.normalize();
	}

	public static int MAX_REFLECT_DEPTH = 16;

	public static Vector4f rayCast(Vector4f start, Vector4f direction) {
		return rayCast(start, direction, 0);
	}

	public static Vector4f rayCast(Vector4f start, Vector4f direction, int depth) {
		if (depth >= MAX_REFLECT_DEPTH) {
			return AMBIENT_LIGHT;
		}
		SimpleEntry<Vector4f, Face> r = ray(start, direction);
		Face face = r.getValue();
		Vector4f result = r.getKey();
		if (face != null) {
			if (face.isLight) {
				result.x = face.color.x;
				result.y = face.color.y;
				result.z = face.color.z;
				return new Vector4f(result.x, result.y, result.z, 0f);
			} else {
				Vector4f hit = new Vector4f();
				hit.x = face.vertices[0].x * result.x + face.vertices[1].x * result.y + face.vertices[2].x * result.z;
				hit.y = face.vertices[0].y * result.x + face.vertices[1].y * result.y + face.vertices[2].y * result.z;
				hit.z = face.vertices[0].z * result.x + face.vertices[1].z * result.y + face.vertices[2].z * result.z;
				hit.w = 1;
				float u = face.textureCoordinates[0].u * result.x + face.textureCoordinates[1].u * result.y
						+ face.textureCoordinates[2].u * result.z;
				float v = face.textureCoordinates[0].v * result.x + face.textureCoordinates[1].v * result.y
						+ face.textureCoordinates[2].v * result.z;
				float normalX=face.vertexNormals[0].x*result.x+face.vertexNormals[1].x*result.y+face.vertexNormals[2].x*result.z;
				float normalY=face.vertexNormals[0].y*result.x+face.vertexNormals[1].y*result.y+face.vertexNormals[2].y*result.z;
				float normalZ=face.vertexNormals[0].z*result.x+face.vertexNormals[1].z*result.y+face.vertexNormals[2].z*result.z;
				int[] color_ = TextureEngine.sample(face.basecolor, u, v);
//				int[] normal_ = TextureEngine.sample(face.normal, u, v);
//				int[] metallic_ = TextureEngine.sample(face.metallic, u, v);
				result.x = color_[0] / 255f;
				result.y = color_[1] / 255f;
				result.z = color_[2] / 255f;
				Vector4f normal = new Vector4f(normalX,normalY,normalZ,0).normalize();
//				Vector4f metallic = new Vector4f(metallic_[0] / 255f, metallic_[1] / 255f, metallic_[2] / 255f,
//						metallic_[3] / 255f);
				Vector4f reflect = rayCast(hit, randomReflect(normal), depth + 1);
				result.x *= reflect.x;
				result.y *= reflect.y;
				result.z *= reflect.z;
				return result;
			}
		}
		return AMBIENT_LIGHT;
	}

	public static void shootOneRay() {
		addTask(() -> {
			double alpha = System.currentTimeMillis() % 10000 / 10000d;
			float alpha1 = (float) Math.cos(2 * Math.PI * (System.currentTimeMillis() % 5000 / 5000d));
			float alpha2 = (float) Math.sin(2 * Math.PI * (System.currentTimeMillis() % 5000 / 5000d));

			float r = (float) Math.abs(0.6 * Math.sin(Math.PI * alpha));
			Vector4f start = new Vector4f(0, 0, 8, 1);
			Vector4f direction = new Vector4f(r * alpha1, r * alpha2, -1, 0).normalize();
//			Vector4f direction = new Vector4f(0, 1, 0, 0);

			Vector4f[] result = new Vector4f[1];
			float[] minT = new float[] { Float.MAX_VALUE };
			Face[] sampleFace = new Face[1];
			model.ObjGroupObjects.forEach((part) -> {
				part.faces.forEach((face) -> {
					Vector4f temp = mollerTrumbore(face.vertices[0], face.vertices[1], face.vertices[2], start,
							direction);
					if (temp.x > 0 && temp.y > 0 && temp.z > 0) {
						if (temp.w > 0 && temp.w < minT[0]) {
							minT[0] = temp.w;
							result[0] = temp;
							sampleFace[0] = face;
						}
					}
				});
			});
			testStart = start;
			testDirection = direction;
			testResult = result[0];
			hit = new Vector4f();
			if (testResult != null && sampleFace[0] != null) {
				Face face = sampleFace[0];
				float u = face.textureCoordinates[0].u * testResult.x + face.textureCoordinates[1].u * testResult.y
						+ face.textureCoordinates[2].u * testResult.z;
				float v = face.textureCoordinates[0].v * testResult.x + face.textureCoordinates[1].v * testResult.y
						+ face.textureCoordinates[2].v * testResult.z;
				hit.x = face.vertices[0].x * testResult.x + face.vertices[1].x * testResult.y
						+ face.vertices[2].x * testResult.z;
				hit.y = face.vertices[0].y * testResult.x + face.vertices[1].y * testResult.y
						+ face.vertices[2].y * testResult.z;
				hit.z = face.vertices[0].z * testResult.x + face.vertices[1].z * testResult.y
						+ face.vertices[2].z * testResult.z;
				hit.w = 1;
				int[] color = TextureEngine.sample("./football_ball_basecolor.png", u, v);
				testResult.x = color[0] / 255f;
				testResult.y = color[1] / 255f;
				testResult.z = color[2] / 255f;
			}
			System.out.println("shoot one ray:" + testResult + "|" + sampleFace[0]);
		});
	}

	public static Vector4f mollerTrumbore(Face face, Vector4f start, Vector4f direction) {
		return mollerTrumbore(face.vertices[0], face.vertices[1], face.vertices[2], start, direction);
	}

	public static Vector4f mollerTrumbore(Vertex pos1, Vertex pos2, Vertex pos3, Vector4f start, Vector4f direction) {
		return mollerTrumbore(new Vector4f(pos1.x, pos1.y, pos1.z, 1), new Vector4f(pos2.x, pos2.y, pos2.z, 1),
				new Vector4f(pos3.x, pos3.y, pos3.z, 1), start, direction);
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
		GL11.glPushMatrix();
		GL11.glTranslated(0, -5, 0);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(0.5f, 0.5f, 0.5f, 1);
		GL11.glVertex3f(-10, 0, -10);
		GL11.glVertex3f(10, 0, -10);
		GL11.glVertex3f(10, 0, 10);
		GL11.glVertex3f(-10, 0, 10);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glPolygonOffset(-1, -1);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(0.1f, 0.1f, 0.1f, 1);
		GL11.glVertex3f(-9, 0, -9);
		GL11.glVertex3f(9, 0, -9);
		GL11.glVertex3f(9, 0, 9);
		GL11.glVertex3f(-9, 0, 9);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glPopMatrix();
	}

	public static WavefrontObject model = new WavefrontObject("./soccer_ball.obj", () -> {

	});

	public static WavefrontObject modelLight = new WavefrontObject("./light.obj", () -> {

	});

	public static HashMap<AABB, Face> AABBtoFace = new HashMap<AABB, Face>();
	public static BVH bvh;

	// 生成AABB和BVH
	static {
		initAABBandBVH();
	}

	public static void initAABBandBVH() {
		AABBtoFace.clear();
		ArrayList<AABB> list = new ArrayList<AABB>();
		model.ObjGroupObjects.stream().map(obj -> obj.faces).flatMap(ArrayList::stream).forEach(face -> {
			AABB aabb = new AABB(face);
			AABBtoFace.put(aabb, face);
			list.add(aabb);
			face.basecolor = "./football_ball_basecolor.png";
			face.normal = "./football_ball_normal.png";
			face.metallic = "./football_ball_metallic.png";
		});
		modelLight.ObjGroupObjects.stream().map(obj -> obj.faces).flatMap(ArrayList::stream).forEach(face -> {
			AABB aabb = new AABB(face);
			AABBtoFace.put(aabb, face);
			list.add(aabb);
			face.isLight = true;
			face.color = new Vector4f(10,0,10,0);
		});
		bvh = new BVH(list);
	}

	public static void drawFootball() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1, 1, 1, 1);
		TextureEngine.bindTexture("./football_ball_basecolor.png");
		model.renderAll();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	public static void drawLight() {
		GL11.glColor4f(1, 0, 1, 1);
		modelLight.renderAll();
	}

	public static void drawAABB() {
		AABBtoFace.keySet().forEach(AABB::render);
	}

	public static void doRender(int width, int height) {
		GL11.glViewport(0, 0, width, height);
		GL11.glClearColor(1f, 1f, 0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		setupCamera(width, height);
		drawGround();
		drawMollerTrumbore();
		drawFootball();
		drawLight();
		drawAABB();
		RaytracingRender.debugRender();
	}

	public static String PIC_SAVE_PATH = "E:\\workbench\\java\\RayTracingRenderer\\screenshot\\";

	public static void scrrenshot() {
		if (windowHandle == 0)
			return;
		int[] w = new int[1], h = new int[1];
		GLFW.glfwGetWindowSize(windowHandle, w, h);
		// 检查窗口大小是否有效，防止窗口过小时崩溃
		if (w[0] <= 0 || h[0] <= 0) {
			System.err.println("警告: 窗口大小无效，无法截图 (宽度: " + w[0] + ", 高度: " + h[0] + ")");
			return;
		}
		
		// 获取实际视口大小（可能因高DPI而不同）
		int[] viewport = new int[4];
		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		int viewportWidth = viewport[2];
		int viewportHeight = viewport[3];
		
		// 使用视口大小或窗口大小中的较小值，确保安全
		int width = Math.min(w[0], viewportWidth);
		int height = Math.min(h[0], viewportHeight);
		
		if (width <= 0 || height <= 0) {
			System.err.println("警告: 视口大小无效，无法截图 (宽度: " + width + ", 高度: " + height + ")");
			return;
		}
		
		// 设置像素对齐为1字节（避免对齐问题）
		int oldPackAlignment = GL11.glGetInteger(GL11.GL_PACK_ALIGNMENT);
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		
		// 计算所需缓冲区大小（RGB，每个像素3字节）
		int bufferSize = width * height * 3;
		ByteBuffer buffer = org.lwjgl.BufferUtils.createByteBuffer(bufferSize);
		buffer.clear(); // 确保position为0
		
		// 确保所有渲染命令完成
		GL11.glFinish();
		
		// 读取像素数据
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
		
		// 恢复原来的对齐设置
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, oldPackAlignment);
		
		// 检查是否有OpenGL错误
		int error = GL11.glGetError();
		if (error != GL11.GL_NO_ERROR) {
			System.err.println("警告: glReadPixels 返回错误: " + error);
			return;
		}
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		buffer.rewind(); // 重置缓冲区位置
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = (y * width + x) * 3;
				if (i + 2 >= bufferSize) {
					break; // 防止越界
				}
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				img.setRGB(x, height - 1 - y, (r << 16) | (g << 8) | b);
			}
		}
		try {
			ImageIO.write(img, "png", new File(PIC_SAVE_PATH + "screenshot_" + System.currentTimeMillis() + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
