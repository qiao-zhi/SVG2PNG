package cn.qlq.Servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import cn.qlq.svg2png.SVGConvertUtils;

/**
 * Servlet implementation class ExportSVG2Excel
 */
@WebServlet("/ExportSVG2Excel")
public class ExportSVG2Excel extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ExportSVG2Excel() {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 1.接受前台的SVGCode参数
		String svgCode = request.getParameter("svgCode");
		if (StringUtils.isEmpty(svgCode)) {
			return;
		}
		// 2.采用JSoup处理code(加上xmlns属性，并且获取宽度和高度属性)
		svgCode = disposeSvgCode(svgCode);
		// 3.后台生成图片(暂时不做异常处理)
		Document document = Jsoup.parse(svgCode);
		Element element = document.select("svg").get(0);
		String style = element.attr("style");
		String width = extractCssAttr(style, "width");// 提取style的宽度属性
		String height = extractCssAttr(style, "height");// 提取style的宽度属性
		String pngFileName = "e:/amchartspng";
		try {
			// 最终的SVGCode必须根元素是是svg,而且带xmlns属性，而且必须将clippath替换为clipPath，否则识别不了会报错
			String finallySvgCode = element.outerHtml().replace("clippath", "clipPath");
			SVGConvertUtils.convertToPngOrOthers(finallySvgCode, pngFileName, "png", Integer.valueOf(width),
					Integer.valueOf(height));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (TranscoderException e) {
			e.printStackTrace();
		}
		String excelName = "e:/amchartsexcel";
		// 3.图片写入Excel
		writePng2Excel(pngFileName, excelName);
		// 4.excel提供下载
		// 若名字为中文名字，需要用URL编码
		response.setHeader("content-disposition",
				"attachment;filename=" + URLEncoder.encode(excelName + ".xls", "UTF-8"));
		InputStream in = in = new FileInputStream(excelName);
		OutputStream out = null;
		int len = 0;
		byte buffer[] = new byte[1024];
		out = response.getOutputStream();
		while ((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}
		in.close();
	}

	private void writePng2Excel(String pngFileName, String excelName) {
		FileOutputStream fileOut = null;
		BufferedImage bufferImg = null;
		try {
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			// 加载图片
			bufferImg = ImageIO.read(new File(pngFileName));
			ImageIO.write(bufferImg, "png", byteArrayOut);
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet1 = wb.createSheet("sheet1");
			HSSFPatriarch patriarch = sheet1.createDrawingPatriarch();
			/**
			 * dx1 - the x coordinate within the first
			 * cell.//定义了图片在第一个cell内的偏移x坐标，既左上角所在cell的偏移x坐标，一般可设0 dy1 - the y
			 * coordinate within the first
			 * cell.//定义了图片在第一个cell的偏移y坐标，既左上角所在cell的偏移y坐标，一般可设0 dx2 - the x
			 * coordinate within the second
			 * cell.//定义了图片在第二个cell的偏移x坐标，既右下角所在cell的偏移x坐标，一般可设0 dy2 - the y
			 * coordinate within the second
			 * cell.//定义了图片在第二个cell的偏移y坐标，既右下角所在cell的偏移y坐标，一般可设0 col1 - the
			 * column (0 based) of the first cell.//第一个cell所在列，既图片左上角所在列 row1 -
			 * the row (0 based) of the first cell.//图片左上角所在行 col2 - the column
			 * (0 based) of the second cell.//图片右下角所在列 row2 - the row (0 based)
			 * of the second cell.//图片右下角所在行
			 */
			HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0, (short) 2, 2, (short) 5, 8);
			// 插入图片
			patriarch.createPicture(anchor, wb.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
			// 输出文件
			fileOut = new FileOutputStream(excelName);
			wb.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// excel转换单位(未用上)
	private int parsrUnit(String value, String unit) {
		int v = 0;
		if (StringUtils.isBlank(unit) || unit.equals("px")) {
			v = Math.round(Float.parseFloat(value) * 37F);
		} else if ("em".endsWith(unit)) {
			v = Math.round(Float.parseFloat(value) * 267.5F);
		}
		return v;
	}

	private String extractCssAttr(String style, String extract) {
		if (style.contains(extract)) {
			style = style.substring(style.indexOf(extract));
			style = style.substring(0, style.indexOf(";"));
			String attr = style.substring(style.indexOf(":") + 2);
			return attr.substring(0, attr.indexOf("px"));
		}
		return "";
	}

	private String disposeSvgCode(String svgCode) {
		Document document = Jsoup.parseBodyFragment(svgCode);
		Element element = document.select("svg").get(0);
		element.attr("xmlns", "http://www.w3.org/2000/svg");// 添加属性
		return document.html();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
