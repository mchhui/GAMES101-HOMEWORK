package mchhui.raytracing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class RaytracingRender {

//	public static Vector4f debugBall = new Vector4f();
//	public static Vector4f debugRay = new Vector4f();

	public static ExecutorService executor = new ForkJoinPool(24);

	public static void sampleBaseColor(int width, int height) {
		long time=System.currentTimeMillis();
		float halfWidth = width / 2f;
		float halfHeight = height / 2f;
		float fov = RaytracingShow.fov;
		float yaw = RaytracingShow.yaw;
		float pitch = RaytracingShow.pitch;
		float distance = RaytracingShow.distance;
		float zPlane = halfHeight / Math.tan(Math.toRadians(fov / 2));
		Vector4f start = new Vector4f();
		Matrix4f transform = new Matrix4f();
		transform.rotate(Math.toRadians(yaw), 0, -1, 0);
		transform.rotate(Math.toRadians(pitch), -1, 0, 0);
		transform.translate(0, 0, distance);
		start.mul(transform);
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		AtomicInteger count = new AtomicInteger(0);
		int[] pixels = new int[width * height];
		
		int tileSize = 32;
		int tileCountX = (width + tileSize - 1) / tileSize;
		int tileCountY = (height + tileSize - 1) / tileSize;
		
		for (int tileX = 0; tileX < tileCountX; tileX++) {
			for (int tileY = 0; tileY < tileCountY; tileY++) {
				int tx = tileX;
				int ty = tileY;
				executor.execute(() -> {
					Random localRandom = new Random();
					int startX = tx * tileSize;
					int startY = ty * tileSize;
					int endX = Math.min(startX + tileSize, width);
					int endY = Math.min(startY + tileSize, height);
					
					for (int px = startX; px < endX; px++) {
						for (int py = startY; py < endY; py++) {
							Vector4f direction = new Vector4f(px + localRandom.nextFloat() - halfWidth,
									py + localRandom.nextFloat() - halfHeight, -zPlane, 0).normalize();
							direction.mul(transform);
							Vector4f sample = RaytracingShow.rayColor(start, direction);
							int r = Math.round(sample.x * 255);
							int g = Math.round(sample.y * 255);
							int b = Math.round(sample.z * 255);
							pixels[py * width + px] = (r << 16) | (g << 8) | b;
						}
					}
					count.getAndAdd((endX - startX) * (endY - startY));
				});
			}
		}
		String tip = "";
		String lastTip = "";
		while (count.get() != pixels.length) {
			tip = String.format("finished:%.2f%%", count.get() * 100f / (pixels.length));
			if (!tip.equals(lastTip)) {
				System.out.println(tip);
			}
			lastTip = tip;
			try {
				Thread.sleep(10); // 避免CPU空转
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				img.setRGB(x, height - 1 - y, pixels[y * width + x]);
			}
		}
		try {
			ImageIO.write(img, "png",
					new File(RaytracingShow.PIC_SAVE_PATH + "screenshot_" + System.currentTimeMillis() + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("time: " + (System.currentTimeMillis() - time) + "ms");
	}

	public static void sampleRayCast(int width, int height, int SPP) {
		long time=System.currentTimeMillis();
		float halfWidth = width / 2f;
		float halfHeight = height / 2f;
		float fov = RaytracingShow.fov;
		float yaw = RaytracingShow.yaw;
		float pitch = RaytracingShow.pitch;
		float distance = RaytracingShow.distance;
		float zPlane = halfHeight / Math.tan(Math.toRadians(fov / 2));
		Vector4f start = new Vector4f();
		Matrix4f transform = new Matrix4f();
		transform.rotate(Math.toRadians(yaw), 0, -1, 0);
		transform.rotate(Math.toRadians(pitch), -1, 0, 0);
		transform.translate(0, 0, distance);
		start.mul(transform);
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		AtomicInteger count = new AtomicInteger(0);
		int[] pixels = new int[width * height];
		
		int tileSize = 32;
		int tileCountX = (width + tileSize - 1) / tileSize;
		int tileCountY = (height + tileSize - 1) / tileSize;
		
		for (int tileX = 0; tileX < tileCountX; tileX++) {
			for (int tileY = 0; tileY < tileCountY; tileY++) {
				int tx = tileX;
				int ty = tileY;
				executor.execute(() -> {
					Random localRandom = new Random();
					int startX = tx * tileSize;
					int startY = ty * tileSize;
					int endX = Math.min(startX + tileSize, width);
					int endY = Math.min(startY + tileSize, height);
					
					for (int px = startX; px < endX; px++) {
						for (int py = startY; py < endY; py++) {
							Vector4f sample = new Vector4f();
							for (int i = 0; i < SPP; i++) {
								Vector4f direction = new Vector4f(px + localRandom.nextFloat() - halfWidth,
										py + localRandom.nextFloat() - halfHeight, -zPlane, 0).normalize();
								direction.mul(transform);
								sample.add(RaytracingShow.rayCast(start, direction));
							}
							sample.div(SPP);
							int r = Math.round(sample.x * 255);
							int g = Math.round(sample.y * 255);
							int b = Math.round(sample.z * 255);
							pixels[py * width + px] = (r << 16) | (g << 8) | b;
						}
					}
					count.getAndAdd((endX - startX) * (endY - startY));
				});
			}
		}
		String tip = "";
		String lastTip = "";
		while (count.get() != pixels.length) {
			tip = String.format("finished:%.2f%%", count.get() * 100f / (pixels.length));
			if (!tip.equals(lastTip)) {
				System.out.println(tip);
			}
			lastTip = tip;
			try {
				Thread.sleep(10); // 避免CPU空转
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				img.setRGB(x, height - 1 - y, pixels[y * width + x]);
			}
		}
		try {
			ImageIO.write(img, "png",
					new File(RaytracingShow.PIC_SAVE_PATH + "screenshot_" + System.currentTimeMillis() + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("time: " + (System.currentTimeMillis() - time) + "ms");
	}

	public static void debugRender() {
//		float radius = 0.1f;
//		GL11.glPushMatrix();
//		GL11.glTranslatef(debugBall.x, debugBall.y, debugBall.z);
//		GL11.glColor4f(1, 0, 0, 1);
//		GL11.glBegin(GL11.GL_TRIANGLES);
//		for (int i = 0; i < 8; i++) {
//			float a1 = (float) (i * java.lang.Math.PI * 2 / 8);
//			float a2 = (float) ((i + 1) * java.lang.Math.PI * 2 / 8);
//			GL11.glVertex3f(0, radius, 0);
//			GL11.glVertex3f((float) (radius * java.lang.Math.cos(a1)), 0, (float) (radius * java.lang.Math.sin(a1)));
//			GL11.glVertex3f((float) (radius * java.lang.Math.cos(a2)), 0, (float) (radius * java.lang.Math.sin(a2)));
//			GL11.glVertex3f(0, -radius, 0);
//			GL11.glVertex3f((float) (radius * java.lang.Math.cos(a2)), 0, (float) (radius * java.lang.Math.sin(a2)));
//			GL11.glVertex3f((float) (radius * java.lang.Math.cos(a1)), 0, (float) (radius * java.lang.Math.sin(a1)));
//		}
//		GL11.glEnd();
//		GL11.glPopMatrix();
//		GL11.glLineWidth(3);
//		GL11.glBegin(GL11.GL_LINES);
//		GL11.glColor4f(1, 0, 0, 1);
//		GL11.glVertex3f(debugBall.x, debugBall.y, debugBall.z);
//		GL11.glVertex3f(debugBall.x + debugRay.x * 10000, debugBall.y + debugRay.y * 10000, debugBall.z + debugRay.z * 10000);
//		GL11.glEnd();
	}
}
