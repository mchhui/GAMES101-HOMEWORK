package mchhui.booststructure;

import java.awt.Color;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import mchhui.objloader.Face;

public class AABB {
	public float minX;
	public float minY;
	public float minZ;
	public float maxX;
	public float maxY;
	public float maxZ;

	public transient Color color = new Color(1, 1, 1);

	public AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public AABB(Face face) {
		this.minX = Math.min(face.vertices[0].x, Math.min(face.vertices[1].x, face.vertices[2].x));
		this.minY = Math.min(face.vertices[0].y, Math.min(face.vertices[1].y, face.vertices[2].y));
		this.minZ = Math.min(face.vertices[0].z, Math.min(face.vertices[1].z, face.vertices[2].z));
		this.maxX = Math.max(face.vertices[0].x, Math.max(face.vertices[1].x, face.vertices[2].x));
		this.maxY = Math.max(face.vertices[0].y, Math.max(face.vertices[1].y, face.vertices[2].y));
		this.maxZ = Math.max(face.vertices[0].z, Math.max(face.vertices[1].z, face.vertices[2].z));
	}

	public float intersect(Vector4f start, Vector4f direction) {
		// 进入时间
		float tmin = Float.NEGATIVE_INFINITY;
		// 离开时间
		float tmax = Float.POSITIVE_INFINITY;

		// X轴
		if (Math.abs(direction.x) < 1e-6f) {
			// 射线与X轴平行，检查起点是否在X范围内
			if (start.x < minX || start.x > maxX) {
				return Float.MAX_VALUE;
			}
		} else {
			float invD = 1.0f / direction.x;
			float t1 = (minX - start.x) * invD;
			float t2 = (maxX - start.x) * invD;
			if (t1 > t2) {
				float temp = t1;
				t1 = t2;
				t2 = temp;
			}
			tmin = Math.max(tmin, t1);
			tmax = Math.min(tmax, t2);
		}

		// Y轴
		if (Math.abs(direction.y) < 1e-6f) {
			if (start.y < minY || start.y > maxY) {
				return Float.MAX_VALUE;
			}
		} else {
			float invD = 1.0f / direction.y;
			float t1 = (minY - start.y) * invD;
			float t2 = (maxY - start.y) * invD;
			if (t1 > t2) {
				float temp = t1;
				t1 = t2;
				t2 = temp;
			}
			tmin = Math.max(tmin, t1);
			tmax = Math.min(tmax, t2);
		}

		// Z轴
		if (Math.abs(direction.z) < 1e-6f) {
			if (start.z < minZ || start.z > maxZ) {
				return Float.MAX_VALUE;
			}
		} else {
			float invD = 1.0f / direction.z;
			float t1 = (minZ - start.z) * invD;
			float t2 = (maxZ - start.z) * invD;
			if (t1 > t2) {
				float temp = t1;
				t1 = t2;
				t2 = temp;
			}
			tmin = Math.max(tmin, t1);
			tmax = Math.min(tmax, t2);
		}

		// 检查是否相交
		if (tmax < 0) {
			return Float.MAX_VALUE; // AABB在射线后方
		}
		if (tmin > tmax) {
			return Float.MAX_VALUE; // 不相交
		}

		// 如果tmin < 0，说明起点在AABB内部，返回0
		return tmin < 0 ? 0 : tmin;
	}

	public static boolean DEBUG_RENDER_AABB = false;
	public static boolean DEBUG_DYE_AABB = false;

	public void render() {
		if(!DEBUG_RENDER_AABB) {
			return;
		}
		GL11.glLineWidth(1);
		GL11.glBegin(GL11.GL_LINES);
		if(DEBUG_DYE_AABB) {
			GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);	
		}else {
			GL11.glColor4f(0, 1, 0, 1);
		}
		GL11.glVertex3f(minX, minY, minZ);
		GL11.glVertex3f(maxX, minY, minZ);
		GL11.glVertex3f(maxX, minY, minZ);
		GL11.glVertex3f(maxX, maxY, minZ);
		GL11.glVertex3f(maxX, maxY, minZ);
		GL11.glVertex3f(minX, maxY, minZ);
		GL11.glVertex3f(minX, maxY, minZ);
		GL11.glVertex3f(minX, minY, minZ);
		GL11.glVertex3f(minX, minY, maxZ);
		GL11.glVertex3f(maxX, minY, maxZ);
		GL11.glVertex3f(maxX, minY, maxZ);
		GL11.glVertex3f(maxX, maxY, maxZ);
		GL11.glVertex3f(maxX, maxY, maxZ);
		GL11.glVertex3f(minX, maxY, maxZ);
		GL11.glVertex3f(minX, maxY, maxZ);
		GL11.glVertex3f(minX, minY, maxZ);
		GL11.glVertex3f(minX, minY, minZ);
		GL11.glVertex3f(minX, minY, maxZ);
		GL11.glVertex3f(maxX, minY, minZ);
		GL11.glVertex3f(maxX, minY, maxZ);
		GL11.glVertex3f(maxX, maxY, minZ);
		GL11.glVertex3f(maxX, maxY, maxZ);
		GL11.glVertex3f(minX, maxY, minZ);
		GL11.glVertex3f(minX, maxY, maxZ);
		GL11.glEnd();
	}

	public static enum Axis {
		X, Y, Z;
	}

	public Axis getLongestAxis() {
		if (maxX - minX > maxY - minY && maxX - minX > maxZ - minZ) {
			return Axis.X;
		}
		if (maxY - minY > maxX - minX && maxY - minY > maxZ - minZ) {
			return Axis.Y;
		}
		return Axis.Z;
	}

	public AABB[] split(Axis axis) {
		float mid = getMid(axis);
		switch (axis) {
		case X:
			return new AABB[] { new AABB(minX, minY, minZ, mid, maxY, maxZ),
					new AABB(mid, minY, minZ, maxX, maxY, maxZ) };
		case Y:
			return new AABB[] { new AABB(minX, minY, minZ, maxX, mid, maxZ),
					new AABB(minX, mid, minZ, maxX, maxY, maxZ) };
		case Z:
			return new AABB[] { new AABB(minX, minY, minZ, maxX, maxY, mid),
					new AABB(minX, minY, mid, maxX, maxY, maxZ) };
		default:
			throw new IllegalArgumentException();
		}
	}

	public float getMid(Axis axis) {
		switch (axis) {
		case X:
			return (maxX + minX) / 2;
		case Y:
			return (maxY + minY) / 2;
		case Z:
			return (maxZ + minZ) / 2;
		default:
			throw new IllegalArgumentException();
		}
	}

	public float getMax(Axis axis) {
		switch (axis) {
		case X:
			return maxX;
		case Y:
			return maxY;
		case Z:
			return maxZ;
		default:
			throw new IllegalArgumentException();
		}
	}

	public float getMin(Axis axis) {
		switch (axis) {
		case X:
			return minX;
		case Y:
			return minY;
		case Z:
			return minZ;
		default:
			throw new IllegalArgumentException();
		}
	}
}
