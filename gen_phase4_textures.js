const zlib = require('zlib');
const fs = require('fs');
const path = require('path');

function createPNG(r, g, b, a = 255) {
    // PNG signature
    const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

    // IHDR chunk
    const ihdr = Buffer.alloc(13);
    ihdr.writeUInt32BE(16, 0); // width
    ihdr.writeUInt32BE(16, 4); // height
    ihdr[8] = 8;  // bit depth
    ihdr[9] = 6;  // color type (RGBA)
    ihdr[10] = 0; // compression
    ihdr[11] = 0; // filter
    ihdr[12] = 0; // interlace

    // Raw image data: filter byte + RGBA pixels per row
    const rawData = Buffer.alloc(16 * (1 + 16 * 4));
    for (let y = 0; y < 16; y++) {
        const offset = y * (1 + 16 * 4);
        rawData[offset] = 0; // filter: none
        for (let x = 0; x < 16; x++) {
            const px = offset + 1 + x * 4;
            rawData[px] = r;
            rawData[px + 1] = g;
            rawData[px + 2] = b;
            rawData[px + 3] = a;
        }
    }

    const compressed = zlib.deflateSync(rawData);

    function makeChunk(type, data) {
        const typeBuffer = Buffer.from(type);
        const length = Buffer.alloc(4);
        length.writeUInt32BE(data.length, 0);
        const crcData = Buffer.concat([typeBuffer, data]);
        const crc = Buffer.alloc(4);
        crc.writeUInt32BE(crc32(crcData), 0);
        return Buffer.concat([length, typeBuffer, data, crc]);
    }

    function crc32(buf) {
        let crc = 0xFFFFFFFF;
        for (let i = 0; i < buf.length; i++) {
            crc ^= buf[i];
            for (let j = 0; j < 8; j++) {
                crc = (crc >>> 1) ^ (crc & 1 ? 0xEDB88320 : 0);
            }
        }
        return (crc ^ 0xFFFFFFFF) >>> 0;
    }

    const ihdrChunk = makeChunk('IHDR', ihdr);
    const idatChunk = makeChunk('IDAT', compressed);
    const iendChunk = makeChunk('IEND', Buffer.alloc(0));

    return Buffer.concat([signature, ihdrChunk, idatChunk, iendChunk]);
}

const textures = [
    { path: 'textures/block/canvas.png', r: 240, g: 240, b: 240 },
    { path: 'textures/block/canvas_glass.png', r: 200, g: 200, b: 220, a: 180 },
    { path: 'textures/block/paint_can.png', r: 180, g: 50, b: 50 },
    { path: 'textures/block/paint_mixer.png', r: 80, g: 80, b: 80 },
    { path: 'textures/item/golden_eye.png', r: 220, g: 190, b: 50 },
    { path: 'textures/item/luggage.png', r: 140, g: 90, b: 50 },
    { path: 'textures/item/hang_glider.png', r: 100, g: 180, b: 230 },
    { path: 'textures/item/cartographer.png', r: 60, g: 160, b: 80 },
    { path: 'textures/item/mini_me.png', r: 210, g: 180, b: 140 },
    { path: 'textures/item/paint_brush.png', r: 220, g: 150, b: 50 },
    { path: 'textures/item/squeegee.png', r: 50, g: 100, b: 200 },
];

const basePath = 'D:/IntelliJ/IDEA/Projects/OpenBlocks/common/src/main/resources/assets/openblocks';
for (const tex of textures) {
    const fullPath = path.join(basePath, tex.path);
    const dir = path.dirname(fullPath);
    fs.mkdirSync(dir, { recursive: true });
    const png = createPNG(tex.r, tex.g, tex.b, tex.a || 255);
    fs.writeFileSync(fullPath, png);
    console.log('Created: ' + fullPath);
}
