function Vec2(x, y) {
    this.x = x;
    this.y = y;
}

function Pixel(r, g, b) {
    this.r = r;
    this.g = g;
    this.b = b;
}


function clearFramebuffer() {
    for (let i = 0; i < WDITH * HEIGHT; i++) {
        colorFramebuffer[i] = new Pixel(0, 0, 0);
        depthFramebuffer[i] = 1;
    }
}
function setPixel(x, y, r, g, b, depth) {
    x=Math.floor(x);
    y=Math.floor(y);
    if (x < 0 || x >= WDITH || y < 0 || y >= HEIGHT) {
        return;
    }
    if (depth === undefined) {
        depth = 1;
    }
    let index = y * WDITH + x;
    colorFramebuffer[index] = new Pixel(r, g, b);
    depthFramebuffer[index] = depth;
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
    // clearFramebuffer();
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

//全局变量区 END

//业务 BEGIN
function windowLoop() {
    flush();
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

var cache=[];
var drawed=false;

function handlePixelClick(x, y) {
    if(drawed){
        return;
    }
    for(let i=-5;i<=5;i++){
        for(let j=-5;j<=5;j++){
            setPixel(x+i,y+j,0,1,0,1);
        }
    }
    cache.push(new Vec2(x,y));
    if(cache.length>=4){
        drawed=true;
        for(let t=0;t<=1;t+=0.001){
            var point = deCasteljau(t,cache);
            for(let i=-2;i<=2;i++){
                for(let j=-2;j<=2;j++){
                    setPixel(point.x+i,point.y+j,1,0,0,1);
                }
            }
            // console.log(point.x,point.y);
        }
        console.log("draw");
    }
}

function deCasteljau(t,points){
    if(points.length == 1){
        return points[0];
    }
    var arr=[];
    for(let i=0;i<points.length-1;i++){
        arr.push(new Vec2(points[i].x * (1-t) + points[i+1].x * t, points[i].y * (1-t) + points[i+1].y * t));
    }
    return deCasteljau(t,arr);
}
//业务 END