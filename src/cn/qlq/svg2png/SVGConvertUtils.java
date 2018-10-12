package cn.qlq.svg2png;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;

/**
 * 将svg转换PDF\JPEG\PNG
 * 
 * 
 */
public abstract class SVGConvertUtils {

	/**
	 * 
	 * @param svgCode
	 * @param pngFilePath
	 *            文件路径
	 * @param fileType
	 *            文件名称
	 * @param width
	 *            宽度
	 * @param height
	 *            高度
	 * @throws IOException
	 * @throws TranscoderException
	 */
	public static void convertToPngOrOthers(String svgCode, String pngFilePath, String fileType, int width, int height)
			throws IOException, TranscoderException {

		File file = new File(pngFilePath);
		FileOutputStream outputStream = null;
		try {
			file.createNewFile();
			outputStream = new FileOutputStream(file);
			convertToPngOrOthers(svgCode, outputStream, fileType, width, height);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void convertToPngOrOthers(String svgCode, OutputStream outputStream, String fileType, int width,
			int height) throws TranscoderException, IOException {
		try {
			byte[] bytes = svgCode.getBytes("utf-8");
			Transcoder t = null;
			if ("png".equals(fileType)) {
				t = new PNGTranscoder();
			} else if ("pdf".equals(fileType)) {
				t = new PDFTranscoder();
			} else if ("jpeg".equals(fileType)) {
				t = new JPEGTranscoder();
			}

			TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(bytes));
			TranscoderOutput output = new TranscoderOutput(outputStream);
			// 增加图片的属性设置(单位是像素)---下面是写死了，实际应该是根据SVG的大小动态设置，默认宽高都是400
			t.addTranscodingHint(ImageTranscoder.KEY_WIDTH, new Float(width));
			t.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, new Float(height));
			t.transcode(input, output);
			outputStream.flush();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}