package cn.qlq.svg2png;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;

/**
 * 将svg转换为png格式的图片
 * 
 * 
 */
public abstract class SVG2PNGUtils {

	/**
	 * 将svg字符串转换为png
	 * 
	 * @param svgCode
	 *            svg代码
	 * @param pngFilePath
	 *            保存的路径
	 * @throws TranscoderException
	 *             svg代码异常
	 * @throws IOException
	 *             io错误
	 */
	public static void convertToPng(String svgCode, String pngFilePath) throws IOException, TranscoderException {

		File file = new File(pngFilePath);
		FileOutputStream outputStream = null;
		try {
			file.createNewFile();
			outputStream = new FileOutputStream(file);
			convertToPng(svgCode, outputStream);
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

	/**
	 * 将svgCode转换成png文件，直接输出到流中
	 * 
	 * @param svgCode
	 *            svg代码
	 * @param outputStream
	 *            输出流
	 * @throws TranscoderException
	 *             异常
	 * @throws IOException
	 *             io异常
	 */
	public static void convertToPng(String svgCode, OutputStream outputStream) throws TranscoderException, IOException {
		try {
			byte[] bytes = svgCode.getBytes("utf-8");
			PNGTranscoder t = new PNGTranscoder();
			TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(bytes));
			TranscoderOutput output = new TranscoderOutput(outputStream);
			// 增加图片的属性设置(单位是像素)---下面是写死了，实际应该是根据SVG的大小动态设置，默认宽高都是400
			t.addTranscodingHint(ImageTranscoder.KEY_WIDTH, new Float(941));
			t.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, new Float(800));
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

	public static void main(String[] args) throws IOException, TranscoderException {
		ClassLoader classLoader = SVG2PNGUtils.class.getClassLoader();
		String filePath = classLoader.getResource("cn/qlq/svg2png/svgtest2.svg").getPath();
		String svgCode = FileUtils.readFileToString(new File(filePath), "UTF-8");
		svgCode = svgCode.replace("clippath", "clipPath");
		convertToPng(svgCode, "e:/test.png");
	}
}