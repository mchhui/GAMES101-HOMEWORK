package mchhui.booststructure;

import java.awt.Color;

public class ColorSet {
	private static final float[][] DISTINCT_COLORS = { 
			{ 0.121f, 0.466f, 0.705f }, // 蓝色
			{ 1.0f, 0.498f, 0.054f }, // 橙色
			{ 0.172f, 0.627f, 0.172f }, // 绿色
			{ 0.839f, 0.152f, 0.156f }, // 红色
			{ 0.580f, 0.403f, 0.741f }, // 紫色
			{ 0.549f, 0.337f, 0.294f }, // 棕色
			{ 0.890f, 0.466f, 0.760f }, // 粉色
			{ 0.498f, 0.498f, 0.498f }, // 灰色
			{ 0.737f, 0.741f, 0.133f }, // 黄绿色
			{ 0.090f, 0.745f, 0.811f }, // 青色
			{ 0.682f, 0.780f, 0.909f }, // 浅蓝色
			{ 1.0f, 0.733f, 0.470f }, // 浅橙色
			{ 0.596f, 0.874f, 0.541f }, // 浅绿色
			{ 1.0f, 0.596f, 0.588f }, // 浅红色
			{ 0.772f, 0.690f, 0.835f }, // 浅紫色
			{ 0.768f, 0.611f, 0.580f }, // 浅棕色
			{ 0.968f, 0.713f, 0.823f }, // 浅粉色
			{ 0.780f, 0.780f, 0.780f }, // 浅灰色
			{ 0.858f, 0.858f, 0.552f }, // 浅黄绿色
			{ 0.619f, 0.854f, 0.898f } // 浅青色
	};

	public static Color getColor(int seed) {
		float[] c = DISTINCT_COLORS[seed % DISTINCT_COLORS.length];
		return new Color(c[0], c[1], c[2]);
	}
}
