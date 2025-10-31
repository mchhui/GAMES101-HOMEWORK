var rectColor = "#ff0000";

function slab(minX, minY, maxX, maxY, x, y, dx, dy) {
    if(dx<0){
        var temp=minX;
        minX=maxX;
        maxX=temp;
    }
    if(dy<0){
        var temp=minY;
        minY=maxY;
        maxY=temp;
    }
    var enter = (minX - x) / dx;
    var exit = (maxX - x) / dx;
    var t = (minY - y) / dy;
    enter = (t > enter ? t : enter);
    t = (maxY - y) / dy;
    exit = (t < exit ? t : exit);
    return enter < exit && exit >= 0 && enter <= 1;

}

function main() {
    console.log(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, line.p1.x, line.p1.y, line.p2.x - line.p1.x, line.p2.y - line.p1.y);
    if (slab(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, line.p1.x, line.p1.y, line.p2.x - line.p1.x, line.p2.y - line.p1.y)) {
        rectColor = "#00ff00";
    } else {
        rectColor = "#ff0000";
    }
}