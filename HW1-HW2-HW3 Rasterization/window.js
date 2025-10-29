const DEBUG_DISABLE_CORRECT_INTERPOLATION = false;
const DEBUG_LOAD_OBJ = true;
const DEBUG_LOAD_TEX = true;
const DEBUG_ENABLE_DEPTH_TEST = true;
const DEBUG_ENABLE_LIGHTING = true;
const DEBUG_LIGHT_VISABLE = false;
const DEBUG_DEPTH_VISABLE = false;

//底层 BEGIN
const mat4 = glMatrix.mat4;
const vec4 = glMatrix.vec4;
const vec3 = glMatrix.vec3;
const vec2 = glMatrix.vec2;

// 计算2D叉积来判断点是否在三角形内部
// 对于2D向量 (a,b) 和 (c,d)，叉积为 a*d - b*c
function cross2D(ax, ay, bx, by) {
    return ax * by - ay * bx;
}

/**
 * 三角形图元
 * @param {vec4} pos1 三角形顶点1
 * @param {vec4} pos2 三角形顶点2
 * @param {vec4} pos3 三角形顶点3
 */
function Primitive(pos1, pos2, pos3, uv1, uv2, uv3) {
    this.calcNormal = function () {
        var v12 = vec4.sub(vec4.create(), this.pos2, this.pos1);
        var v13 = vec4.sub(vec4.create(), this.pos3, this.pos1);
        // 使用vec3.cross计算3D叉积，忽略w分量
        return vec3.cross(vec3.create(), v12, v13);
    }

    /**
     * 判断点是否在三角形内
     * @param {number} x NDC坐标x
     * @param {number} y NDC坐标y
     */
    this.inside = (x, y) => {
        var x1 = this.pos1[0], y1 = this.pos1[1];
        var x2 = this.pos2[0], y2 = this.pos2[1];
        var x3 = this.pos3[0], y3 = this.pos3[1];

        var cross1 = cross2D(x2 - x1, y2 - y1, x - x1, y - y1);
        var cross2 = cross2D(x3 - x2, y3 - y2, x - x2, y - y2);
        var cross3 = cross2D(x1 - x3, y1 - y3, x - x3, y - y3);

        return (cross1 >= 0 && cross2 >= 0 && cross3 >= 0) ||
            (cross1 <= 0 && cross2 <= 0 && cross3 <= 0);
    }

    this.interpolateUV = (ndc_x, ndc_y) => {
        var x1 = this.pos1[0], y1 = this.pos1[1];
        var x2 = this.pos2[0], y2 = this.pos2[1];
        var x3 = this.pos3[0], y3 = this.pos3[1];

        var cross1 = cross2D(x2 - x1, y2 - y1, ndc_x - x1, ndc_y - y1);
        var cross2 = cross2D(x3 - x2, y3 - y2, ndc_x - x2, ndc_y - y2);
        var cross3 = cross2D(x1 - x3, y1 - y3, ndc_x - x3, ndc_y - y3);

        var full_area = cross2D(x2 - x1, y2 - y1, x3 - x1, y3 - y1);
        //对边对角
        var a = cross2 / full_area;
        var b = cross3 / full_area;
        var c = cross1 / full_area;
        var u_ = a * (this.uv1[0] / this.pos1[3]) + b * (this.uv2[0] / this.pos2[3]) + c * (this.uv3[0] / this.pos3[3]);
        var v_ = a * (this.uv1[1] / this.pos1[3]) + b * (this.uv2[1] / this.pos2[3]) + c * (this.uv3[1] / this.pos3[3]);
        var w_ = a * (1 / this.pos1[3]) + b * (1 / this.pos2[3]) + c * (1 / this.pos3[3]);
        if (DEBUG_DISABLE_CORRECT_INTERPOLATION) {
            w_ = 1;
        }
        return vec2.fromValues(u_ / w_, v_ / w_);
    }

    this.interpolateDepth = (ndc_x, ndc_y) => {
        var x1 = this.pos1[0], y1 = this.pos1[1];
        var x2 = this.pos2[0], y2 = this.pos2[1];
        var x3 = this.pos3[0], y3 = this.pos3[1];

        var cross1 = cross2D(x2 - x1, y2 - y1, ndc_x - x1, ndc_y - y1);
        var cross2 = cross2D(x3 - x2, y3 - y2, ndc_x - x2, ndc_y - y2);
        var cross3 = cross2D(x1 - x3, y1 - y3, ndc_x - x3, ndc_y - y3);

        var full_area = cross2D(x2 - x1, y2 - y1, x3 - x1, y3 - y1);
        //对边对角
        var a = cross2 / full_area;
        var b = cross3 / full_area;
        var c = cross1 / full_area;
        var z_ = a * (this.pos1[2] / this.pos1[3]) + b * (this.pos2[2] / this.pos2[3]) + c * (this.pos3[2] / this.pos3[3]);
        var w_ = a * (1 / this.pos1[3]) + b * (1 / this.pos2[3]) + c * (1 / this.pos3[3]);
        return z_ / w_;
    }

    this.computeLighting = () => {
        if (!DEBUG_ENABLE_LIGHTING) {
            return 1;
        }
        //平行光
        return Math.min(Math.max(vec3.dot(this.normal, LIGHT_0) + 0.3, 0), 1);
    }

    this.clone = function () {
        return new Primitive(vec4.clone(this.pos1), vec4.clone(this.pos2), vec4.clone(this.pos3), vec2.clone(this.uv1), vec2.clone(this.uv2), vec2.clone(this.uv3));
    }

    this.pos1 = pos1;
    this.pos2 = pos2;
    this.pos3 = pos3;
    this.normal = this.calcNormal();
    this.uv1 = uv1;
    this.uv2 = uv2;
    this.uv3 = uv3;
}

function Pixel(r, g, b) {
    this.r = r;
    this.g = g;
    this.b = b;
}

/**
 * 纹理类
 * @param {number} width 纹理宽度
 * @param {number} height 纹理高度
 * @param {Array|string} data 纹理数据 (RGBA格式) 或 PNG URL
 */
function Texture(width, height, data) {
    this.width = width;
    this.height = height;
    this.data = null;
    this.isLoaded = false;

    // 生成棋盘格图案
    this.generateCheckerboard = function () {
        for (let y = 0; y < this.height; y++) {
            for (let x = 0; x < this.width; x++) {
                const index = (y * this.width + x) * 4;
                const checker = Math.floor(x / 8) + Math.floor(y / 8);
                if (checker % 2 === 0) {
                    this.data[index] = 255;     // R
                    this.data[index + 1] = 255; // G
                    this.data[index + 2] = 255; // B
                    this.data[index + 3] = 255; // A
                } else {
                    this.data[index] = 0;       // R
                    this.data[index + 1] = 50;   // G
                    this.data[index + 2] = 50;   // B
                    this.data[index + 3] = 255; // A
                }
            }
        }
    };


    /**
     * 纹理采样函数（最近邻采样）
     * @param {number} u 纹理坐标u (0-1)
     * @param {number} v 纹理坐标v (0-1)
     */
    this.sample = function (u, v) {
        if (!this.isLoaded || !this.data) {
            // 如果纹理未加载，返回默认颜色
            return { r: 1, g: 0, b: 1, a: 1 }; // 品红色
        }

        // 将纹理坐标转换为像素坐标
        let x = Math.round(u * (this.width - 1));
        let y = Math.round(v * (this.height - 1));
        if (x < 0) x = 0;
        if (x >= this.width) x = 1;
        if (y < 0) y = 0;
        if (y >= this.height) y = 1;
        const index = (y * this.width + x) * 4;

        return {
            r: this.data[index] / 255.0,
            g: this.data[index + 1] / 255.0,
            b: this.data[index + 2] / 255.0,
            a: this.data[index + 3] / 255.0
        };
    };

    // 只支持数组数据
    this.data = data || new Array(width * height * 4);
    this.isLoaded = true;

    // 初始化默认纹理（棋盘格图案）
    if (!data) {
        this.generateCheckerboard();
    }
}


function loadModelFromData(objData) {
    return new Promise((resolve, reject) => {
        try {
            console.log('OBJ文件内容长度:', objData.length);
            console.log('OBJ文件前100个字符:', objData.substring(0, 100));

            var mesh = new OBJ.Mesh(objData, {});

            console.log('Mesh创建完成');
            console.log('顶点数量:', mesh.vertices ? mesh.vertices.length : 'undefined');
            console.log('索引数量:', mesh.indices ? mesh.indices.length : 'undefined');

            // 将 OBJ 数据转换为你的 Primitive 格式
            const primitives = [];
            if (mesh.indices && mesh.indices.length > 0) {
                for (let i = 0; i < mesh.indices.length; i += 3) {
                    const idx1 = mesh.indices[i];
                    const idx2 = mesh.indices[i + 1];
                    const idx3 = mesh.indices[i + 2];

                    const pos1 = vec4.fromValues(
                        mesh.vertices[idx1 * 3],
                        mesh.vertices[idx1 * 3 + 1],
                        mesh.vertices[idx1 * 3 + 2],
                        1
                    );
                    const pos2 = vec4.fromValues(
                        mesh.vertices[idx2 * 3],
                        mesh.vertices[idx2 * 3 + 1],
                        mesh.vertices[idx2 * 3 + 2],
                        1
                    );
                    const pos3 = vec4.fromValues(
                        mesh.vertices[idx3 * 3],
                        mesh.vertices[idx3 * 3 + 1],
                        mesh.vertices[idx3 * 3 + 2],
                        1
                    );

                    // 纹理坐标
                    const uv1 = vec2.fromValues(
                        mesh.textures[idx1 * 2] || 0,
                        mesh.textures[idx1 * 2 + 1] || 0
                    );
                    const uv2 = vec2.fromValues(
                        mesh.textures[idx2 * 2] || 0,
                        mesh.textures[idx2 * 2 + 1] || 0
                    );
                    const uv3 = vec2.fromValues(
                        mesh.textures[idx3 * 2] || 0,
                        mesh.textures[idx3 * 2 + 1] || 0
                    );

                    primitives.push(new Primitive(pos1, pos2, pos3, uv1, uv2, uv3));
                }
            } else {
                console.error('没有找到索引数据');
            }

            resolve(primitives);
        } catch (error) {
            console.error('解析失败:', error);
            reject(error);
        }
    });
}


function clearFramebuffer() {
    for (let i = 0; i < WDITH * HEIGHT; i++) {
        colorFramebuffer[i] = new Pixel(0, 0, 0);
        depthFramebuffer[i] = 1;
    }
}
function setPixel(x, y, r, g, b, depth) {
    if (x < 0 || x >= WDITH || y < 0 || y >= HEIGHT) {
        return;
    }
    if (depth === undefined) {
        depth = 1;
    }
    let index = y * WDITH + x;
    //深度测试
    if (DEBUG_ENABLE_DEPTH_TEST) {
        if (depthFramebuffer[index] <= depth) {
            return;
        }
    }
    colorFramebuffer[index] = new Pixel(r, g, b);
    depthFramebuffer[index] = depth;
    // console.log(`setPixel: (${x}, ${y}), r: ${r}, g: ${g}, b: ${b}, depth: ${depth}`);
}
function uploadFramebufferToCanvas() {
    // 获取Canvas元素和2D绘图上下文
    const canvas = document.getElementById('window');
    const ctx = canvas.getContext('2d');

    // 创建ImageData对象来存储像素数据
    const imageData = ctx.createImageData(WDITH, HEIGHT);
    const data = imageData.data;

    // 将帧缓冲区数据复制到ImageData
    for (let y = 0; y < HEIGHT; y++) {
        for (let x = 0; x < WDITH; x++) {
            const index = y * WDITH + x;
            const pixel = colorFramebuffer[index];

            // ImageData的RGBA格式，每个像素4个字节
            const pixelIndex = (y * WDITH + x) * 4;
            data[pixelIndex] = Math.floor(pixel.r * 255);     // Red (浮点数转0-255)
            data[pixelIndex + 1] = Math.floor(pixel.g * 255); // Green (浮点数转0-255)
            data[pixelIndex + 2] = Math.floor(pixel.b * 255); // Blue (浮点数转0-255)
            data[pixelIndex + 3] = 255;                       // Alpha (不透明)
        }
    }

    // 将ImageData绘制到Canvas上
    ctx.putImageData(imageData, 0, 0);
}
function flush() {
    uploadFramebufferToCanvas();
    clearFramebuffer();
}
//底层 END

//全局变量区 BEGIN
var WDITH = 720;
var HEIGHT = 600;
var PIXEL_SIZE = 3;
var FAR_PLANE = 1000;
var colorFramebuffer = [WDITH * HEIGHT * PIXEL_SIZE];
var depthFramebuffer = [WDITH * HEIGHT];
for (let i = 0; i < WDITH * HEIGHT; i++) {
    colorFramebuffer[i] = new Pixel(0, 0, 0);
    depthFramebuffer[i] = 1;
}

var LIGHT_0 = vec4.fromValues(1, 0, 0, 0);

//全局变量区 END

//业务 BEGIN
var modelViewMatrix = mat4.create();
var projectionMatrix = mat4.create();
mat4.identity(projectionMatrix);
mat4.perspective(projectionMatrix, 70 / 180 * Math.PI, WDITH / HEIGHT, 0.1, FAR_PLANE);

var modelData = [
    new Primitive(vec4.fromValues(0, 0, 0, 1), vec4.fromValues(1, 0, 0, 1), vec4.fromValues(0, 1, 0, 1), vec2.fromValues(0, 0), vec2.fromValues(1, 0), vec2.fromValues(0, 1)),
    new Primitive(vec4.fromValues(1, 1, 0, 1), vec4.fromValues(1, 0, 0, 1), vec4.fromValues(0, 1, 0, 1), vec2.fromValues(1, 1), vec2.fromValues(1, 0), vec2.fromValues(0, 1))
]
var texture = new Texture(64,64);
if(DEBUG_LOAD_TEX){
    texture = new Texture(512, 384, imageData);
}

if (DEBUG_LOAD_OBJ) {
    // 异步加载模型
    loadModelFromData(rawModel)
        .then(primitives => {
            modelData = primitives;
            console.log(`模型加载完成，包含 ${modelData.length} 个三角形`);

            // for(let i=0;i<modelData.length;i++){
            //     console.log(modelData[i].pos1,modelData[i].pos2,modelData[i].pos3);
            // }
        })
        .catch(error => {
            console.error('模型加载失败:', error);
            // 使用默认模型
            modelData = [
                new Primitive(vec4.fromValues(0, 0, 0, 1), vec4.fromValues(100, 0, 0, 1), vec4.fromValues(0, 100, 0, 1), vec2.fromValues(0, 0), vec2.fromValues(1, 0), vec2.fromValues(0, 1)),
                new Primitive(vec4.fromValues(100, 100, 0, 1), vec4.fromValues(100, 0, 0, 1), vec4.fromValues(0, 100, 0, 1), vec2.fromValues(1, 1), vec2.fromValues(1, 0), vec2.fromValues(0, 1))
            ];
        });
}

function windowLoop() {
    flush();
    renderModel(modelData);

    var texture = new Texture(64, 64);
    // rasterize(function (x, y, ndc_x, ndc_y) {
    //     return true;
    // }, function (x, y, ndc_x, ndc_y) {
    //     var uv = vec2.fromValues(x/WDITH, y/HEIGHT);
    //     var texColor = texture.sample(uv[0], uv[1]);
    //     setPixel(x, y, texColor.r, texColor.g, texColor.b);
    // });
}

function rasterize(checkingFunc, shadingFunc) {
    for (let y = 0; y < HEIGHT; y++) {
        for (let x = 0; x < WDITH; x++) {
            let ndc_x = (x - WDITH / 2) / (WDITH / 2);
            let ndc_y = -(y - HEIGHT / 2) / (HEIGHT / 2);
            if (checkingFunc(ndc_x, ndc_y)) {
                shadingFunc(x, y, ndc_x, ndc_y);
            }
        }
    }
}

function handlePixelClick(x, y) {

}

function transform(model) {
    //pose
    mat4.identity(modelViewMatrix);
    mat4.translate(modelViewMatrix, modelViewMatrix, vec4.fromValues(0, 0, -5, 0));
    time = new Date().getTime();
    // time = 6080;
    mat4.rotateY(modelViewMatrix, modelViewMatrix, Math.PI * 2 * (time % 15000) / 15000);
    mat4.rotateX(modelViewMatrix, modelViewMatrix, Math.PI / 8 * (Math.sin(time / 10000) + 1));
    // mat4.rotateY(modelViewMatrix, modelViewMatrix, Math.PI * 2 * (time % 15000) / 15000);

    LIGHT_0 = vec4.fromValues(1, 0, 0, 0);
    vec4.transformMat4(LIGHT_0, LIGHT_0, modelViewMatrix);
    vec4.transformMat4(LIGHT_0, LIGHT_0, projectionMatrix);

    //apply
    for (let i = 0; i < model.length; i++) {
        var primitive = model[i];
        vec4.transformMat4(primitive.pos1, primitive.pos1, modelViewMatrix);
        vec4.transformMat4(primitive.pos2, primitive.pos2, modelViewMatrix);
        vec4.transformMat4(primitive.pos3, primitive.pos3, modelViewMatrix);
        vec4.transformMat4(primitive.pos1, primitive.pos1, projectionMatrix);
        vec4.transformMat4(primitive.pos2, primitive.pos2, projectionMatrix);
        vec4.transformMat4(primitive.pos3, primitive.pos3, projectionMatrix);
    }
}

function stupidClip(model) {
    //先试试直接裁剪
    for (let i = 0; i < model.length; i++) {
        var primitive = model[i];
        if (primitive.pos1[2] < 0 || primitive.pos2[2] < 0 || primitive.pos3[2] < 0) {
            model.splice(i, 1);
            i--;
        }
    }
}

//有点懒了 只做近平面的笨蛋剔除吧
function clip(model) {
    stupidClip(model);
}

function perspectiveDivide(model) {
    for (let i = 0; i < model.length; i++) {
        var primitive = model[i];

        // console.log("Before perspective divide - pos2:", primitive.pos2);
        primitive.pos1[0] /= primitive.pos1[3];
        primitive.pos1[1] /= primitive.pos1[3];
        primitive.pos1[2] /= FAR_PLANE;
        primitive.pos1[3] = primitive.pos1[3];
        if (DEBUG_DISABLE_CORRECT_INTERPOLATION) {
            primitive.pos1[3] = 1;
        }

        primitive.pos2[0] /= primitive.pos2[3];
        primitive.pos2[1] /= primitive.pos2[3];
        primitive.pos2[2] /= FAR_PLANE;
        primitive.pos2[3] = primitive.pos2[3];
        if (DEBUG_DISABLE_CORRECT_INTERPOLATION) {
            primitive.pos2[3] = 1;
        }

        primitive.pos3[0] /= primitive.pos3[3];
        primitive.pos3[1] /= primitive.pos3[3];
        primitive.pos3[2] /= FAR_PLANE;
        primitive.pos3[3] = primitive.pos3[3];
        if (DEBUG_DISABLE_CORRECT_INTERPOLATION) {
            primitive.pos3[3] = 1;
        }

        // console.log("After perspective divide - pos1(" + primitive.pos1[0] + "," + primitive.pos1[1] + ")" + "pos2(" + primitive.pos2[0] + "," + primitive.pos2[1] + ")" + "pos3(" + primitive.pos3[0] + "," + primitive.pos3[1] + ")");
    }
}

function renderModel(modelData) {
    // 如果模型还没加载完成，跳过渲染
    var time = new Date().getTime();
    if (modelData.length === 0) {
        return;
    }
    var model = [];
    for (let i = 0; i < modelData.length; i++) {
        model.push(modelData[i].clone());
    }
    var log1 = new Date().getTime() - time;
    time = new Date().getTime();
    transform(model);
    clip(model);
    perspectiveDivide(model);
    var log2 = new Date().getTime() - time;
    time = new Date().getTime();
    for (let i = 0; i < model.length; i++) {
        rasterize(model[i].inside, function (x, y, ndc_x, ndc_y) {
            var uv = model[i].interpolateUV(ndc_x, ndc_y);
            var texColor = texture.sample(uv[0], uv[1]);
            var light = model[i].computeLighting();
            if (DEBUG_DEPTH_VISABLE) {
                var depth=model[i].interpolateDepth(ndc_x, ndc_y)*100;
                setPixel(x, y, depth, 0, 0, model[i].interpolateDepth(ndc_x, ndc_y));
            } else if (DEBUG_LIGHT_VISABLE) {
                setPixel(x, y, light, light, light, model[i].interpolateDepth(ndc_x, ndc_y));
            } else {
                setPixel(x, y, texColor.r * light, texColor.g * light, texColor.b * light, model[i].interpolateDepth(ndc_x, ndc_y));
            }
            // console.log("test:",light);
            // setPixel(x, y, model[i].interpolateDepth(ndc_x, ndc_y)*100, 0, 0, model[i].interpolateDepth(ndc_x, ndc_y));
        });
    }
    var log3 = new Date().getTime() - time;
    // 更新HTML元素显示性能信息
    var performanceElement = document.getElementById('performance-info');
    if (performanceElement) {
        performanceElement.innerHTML = `加载模型: ${log1}ms<br>变换: ${log2}ms<br>光栅化: ${log3}ms`;
    }
}
//业务 END