package mchhui.raytracing;

import javax.swing.*;

import org.lwjgl.glfw.GLFW;

import mchhui.booststructure.AABB;
import mchhui.booststructure.BVH;

import java.awt.*;

public class Menu {
    public static void init(String[] args) {
        JFrame frame = new JFrame("菜单");
        frame.setLayout(new BorderLayout());
        
        // 创建主面板，使用垂直布局
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));
        panel.setBackground(new Color(245, 245, 250));
        
        // 创建标题
        JLabel title = new JLabel("调试菜单", JLabel.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(50));
        
        // 创建按钮1
        JButton button1 = createStyledButton("进行一次MT");
        button1.addActionListener(e -> {
            RaytracingShow.shootOneRay();
        });
        panel.add(button1);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮2
        JButton button2 = createStyledButton("构建BVH 最大深度:1");
        button2.addActionListener(e -> {
            RaytracingShow.addTask(()->{
            	BVH.MAX_SPLIT_DEPTH=1;
            	RaytracingShow.initAABBandBVH();
            });
        });
        panel.add(button2);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮3
        JButton button3 = createStyledButton("构建BVH 最大深度:2");
        button3.addActionListener(e -> {
            RaytracingShow.addTask(()->{
            	BVH.MAX_SPLIT_DEPTH=2;
            	RaytracingShow.initAABBandBVH();
            });
        });
        panel.add(button3);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮4
        JButton button4 = createStyledButton("构建BVH 最大深度:4");
        button4.addActionListener(e -> {
            RaytracingShow.addTask(()->{
            	BVH.MAX_SPLIT_DEPTH=4;
            	RaytracingShow.initAABBandBVH();
            });
        });
        panel.add(button4);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮5
        JButton button5 = createStyledButton("构建BVH 最大深度:8");
        button5.addActionListener(e -> {
            RaytracingShow.addTask(()->{
            	BVH.MAX_SPLIT_DEPTH=8;
            	RaytracingShow.initAABBandBVH();
            });
        });
        panel.add(button5);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮6
        JButton button6 = createStyledButton("构建BVH 最大深度:32");
        button6.addActionListener(e -> {
            RaytracingShow.addTask(()->{
            	BVH.MAX_SPLIT_DEPTH=32;
            	RaytracingShow.initAABBandBVH();
            });
        });
        panel.add(button6);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮7
        JButton button7 = createStyledButton("显示AABB");
        button7.addActionListener(e -> {
            RaytracingShow.addTask(()->{
            	AABB.DEBUG_RENDER_AABB=true;
            });
        });
        panel.add(button7);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮8
        JButton button8 = createStyledButton("隐藏AABB");
        button8.addActionListener(e -> {
            RaytracingShow.addTask(()->{
            	AABB.DEBUG_RENDER_AABB=false;
            });
        });
        panel.add(button8);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮9
        JButton button9 = createStyledButton("启用AABB按BVH染色");
        button9.addActionListener(e -> {
            RaytracingShow.addTask(()->{
            	AABB.DEBUG_DYE_AABB=true;
            });
        });
        panel.add(button9);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮10
        JButton button10 = createStyledButton("禁用AABB按BVH染色");
        button10.addActionListener(e -> {
        	RaytracingShow.addTask(()->{
            	AABB.DEBUG_DYE_AABB=false;
            });
        });
        panel.add(button10);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮11
        JButton button11 = createStyledButton("截图");
        button11.addActionListener(e -> {
        	RaytracingShow.addTask(()->{
            	RaytracingShow.scrrenshot();
            });
        });
        panel.add(button11);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮12
        JButton button12 = createStyledButton("光线追踪渲染(基础色采样)");
        button12.addActionListener(e -> {
        	RaytracingShow.addTask(()->{
        		int[] width_ = new int[1];
        		int[] height_ = new int[1];
        		GLFW.glfwGetWindowSize(RaytracingShow.windowHandle, width_, height_);
        		int width = width_[0];
        		int height = height_[0];
                RaytracingRender.sampleBaseColor(width, height);
        	});
        });
        panel.add(button12);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮13
        JButton button13 = createStyledButton("光线追踪渲染(SPP:1)");
        button13.addActionListener(e -> {
        	RaytracingShow.addTask(()->{
        		int[] width_ = new int[1];
        		int[] height_ = new int[1];
        		GLFW.glfwGetWindowSize(RaytracingShow.windowHandle, width_, height_);
        		int width = width_[0];
        		int height = height_[0];
                RaytracingRender.sampleRayCast(width, height, 1);
        	});
        });
        panel.add(button13);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮14
        JButton button14 = createStyledButton("光线追踪渲染(SPP:4)");
        button14.addActionListener(e -> {
        	RaytracingShow.addTask(()->{
        		int[] width_ = new int[1];
        		int[] height_ = new int[1];
        		GLFW.glfwGetWindowSize(RaytracingShow.windowHandle, width_, height_);
        		int width = width_[0];
        		int height = height_[0];
                RaytracingRender.sampleRayCast(width, height, 4);
        	});
        });
        panel.add(button14);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮15
        JButton button15 = createStyledButton("光线追踪渲染(SPP:64)");
        button15.addActionListener(e -> {
        	RaytracingShow.addTask(()->{
        		int[] width_ = new int[1];
        		int[] height_ = new int[1];
        		GLFW.glfwGetWindowSize(RaytracingShow.windowHandle, width_, height_);
        		int width = width_[0];
        		int height = height_[0];
                RaytracingRender.sampleRayCast(width, height, 64);
        	});
        });
        panel.add(button15);
        panel.add(Box.createVerticalStrut(25));
        
        // 创建按钮16
        JButton button16 = createStyledButton("光线追踪渲染(SPP:200)");
        button16.addActionListener(e -> {
        	RaytracingShow.addTask(()->{
        		int[] width_ = new int[1];
        		int[] height_ = new int[1];
        		GLFW.glfwGetWindowSize(RaytracingShow.windowHandle, width_, height_);
        		int width = width_[0];
        		int height = height_[0];
        		System.out.println(200);
                RaytracingRender.sampleRayCast(width, height, 200);
        	});
        });
        panel.add(button16);
        
        // 使用滚动面板以支持更多按钮
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(600, 1000);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    
    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        button.setPreferredSize(new Dimension(300, 60));
        button.setMaximumSize(new Dimension(300, 60));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
