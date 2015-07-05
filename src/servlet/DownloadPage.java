package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet("/downloadPage")
public class DownloadPage extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String pageUrl = request.getParameter("url");
		String html = getHtmlPageAsString(pageUrl);
		JFileChooser fileChooser = new JFileChooser();

		int userSelection = openDialogBox(html, fileChooser);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			String path = fileToSave.toString();
			String fileName = fileToSave.getName();

			savePageToFile(pageUrl, html, path, fileName);
		}

		response.sendRedirect("");
	}

	private void savePageToFile(String pageUrl, String html, String path, String fileName) throws IOException {
		if (fileName.toLowerCase().endsWith(".zip")) {
			fileName = fileName.substring(0, fileName.length() - 4);
			path = path.substring(0, path.length() - 4);
		}

		byte[] buffer = new byte[1024];

		FileOutputStream fileOutputStream = new FileOutputStream(path + ".zip");
		ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

		StringBuffer resultHtml = new StringBuffer(html.length());

		List<String> sourceUrlList = new ArrayList<String>();
		List<String> filesList = new ArrayList<String>();

		String protocol = pageUrl.substring(0, pageUrl.indexOf(":") + 1);

		Matcher matcher = getSourcesMatcher(html);
		while (matcher.find()) {
			String sourceUrl = matcher.group(3);
			if (sourceUrlList.contains(sourceUrl)) {
				continue;
			}

			String[] sourceUrlParts = sourceUrl.split("/");

			String downloadedFileName = sourceUrlParts[sourceUrlParts.length - 1];
			if (filesList.contains(downloadedFileName)) {
				downloadedFileName = fixDuplicateFileName(downloadedFileName, filesList);
			}

			URL url = new URL(getAbsoluteSourceUrl(pageUrl, sourceUrl, sourceUrlParts.length, protocol));
			try {
				InputStream inputStream = url.openStream();
				writeToOutputStream(buffer, zipOutputStream, inputStream, fileName + "_files\\" + downloadedFileName);
				inputStream.close();
				matcher.appendReplacement(resultHtml, matcher.group(1) + matcher.group(2) + fileName + "_files/" + downloadedFileName + matcher.group(4));
			} catch (IOException e) {
				e.printStackTrace();
			}

			sourceUrlList.add(sourceUrl);
			filesList.add(downloadedFileName);
		}
		matcher.appendTail(resultHtml);
		html = resultHtml.toString();

		byte b[] = html.getBytes();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(b);
		writeToOutputStream(buffer, zipOutputStream, inputStream, fileName + ".html");

		zipOutputStream.closeEntry();
		zipOutputStream.close();
	}

	private void writeToOutputStream(byte[] buffer, ZipOutputStream zipOutputStream, InputStream inputStream, String fileName) throws IOException {
		ZipEntry ze = new ZipEntry(fileName);
		zipOutputStream.putNextEntry(ze);

		int length;
		while ((length = inputStream.read(buffer)) > 0) {
			zipOutputStream.write(buffer, 0, length);
		}
	}

	private String fixDuplicateFileName(String fileName, List<String> filesList) {
		int newNumber = 0;
		for (String file : filesList) {
			if (file.startsWith(fileName + "(")) {
				String fileNumber = file.substring(fileName.length(), fileName.indexOf(")"));
				int number = Integer.parseInt(fileNumber);
				if (number > newNumber) {
					newNumber = number;
				}
			}
		}
		return fileName.substring(0, fileName.lastIndexOf("."))
				+ "(" + newNumber + ")"
				+ fileName.substring(fileName.lastIndexOf("."));
	}

	private String getAbsoluteSourceUrl(String pageUrl, String sourceUrl, int sourceUrlPartsLength, String protocol) {
		if (sourceUrl.startsWith("//")) {
			return protocol + sourceUrl;
		}
		if (sourceUrl.startsWith("/")) {
			return pageUrl + sourceUrl;
		}
		if (sourceUrlPartsLength < 2) {
			return pageUrl.substring(0, pageUrl.lastIndexOf("/")) + "/" + sourceUrl;
		}
		if (sourceUrl.startsWith("../")) {
			String getUrl = sourceUrl.substring(3);
			int i = 1;
			while (getUrl.startsWith("../")) {
				getUrl = getUrl.substring(3);
				i++;
			}
			String[] pageUrlParts = pageUrl.split("/");
			for (int j = pageUrlParts.length - 2 - i; j >= 0; j--) {
				getUrl = pageUrlParts[j] + "/" + getUrl;
			}

			return getUrl;
		}
		return sourceUrl;
	}

	private Matcher getSourcesMatcher(String html) {
		Pattern pattern = Pattern.compile("\\b(href|src)(\\s*=\\s*['\"]+?\\s*)(\\S+\\.+?[^/\\s]*)(\\s*['\"]+?)");
		return pattern.matcher(html);
	}

	private int openDialogBox(String html, JFileChooser fileChooser) {
		JFrame parentFrame = new JFrame();

		fileChooser.setDialogTitle("Save as");

		fileChooser.setFileFilter(new ExtensionFileFilter("Compressed Zip Archive (*.zip)", "zip"));
		fileChooser.setAcceptAllFileFilterUsed(false);

		Pattern patternTitle = Pattern.compile("<title.*?>(.+?)</title>");
		Matcher matcherTitle = patternTitle.matcher(html);

		String title = (matcherTitle.find() ? matcherTitle.group(1) : "WebPage");

		fileChooser.setSelectedFile(new File(title + ".zip"));

		parentFrame.setVisible(true);
		parentFrame.setAlwaysOnTop(true);
		parentFrame.toFront();
		int userSelection = fileChooser.showSaveDialog(parentFrame);
		parentFrame.setVisible(false);

		return userSelection;
	}

	private String getHtmlPageAsString(String pageUrl) throws IOException {
		URL url = new URL(pageUrl);
		InputStream in = url.openStream();
		in = new BufferedInputStream(in);
		Reader r = new InputStreamReader(in);

		int c;
		String html = "";
		while ((c = r.read()) != -1) {
			html += ((char) c);
		}
		return html;
	}

	class ExtensionFileFilter extends FileFilter {
		String description;

		String extensions[];

		public ExtensionFileFilter(String description, String extension) {
			this.extensions = toLower(new String[]{extension});
			if (description == null) {
				this.description = extensions[0];
			} else {
				this.description = description;
			}
			toLower(this.extensions);
		}

		private String[] toLower(String array[]) {
			for (int i = 0; i < array.length; i++) {
				array[i] = array[i].toLowerCase();
			}
			return array;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			} else {
				String path = file.getAbsolutePath().toLowerCase();
				for (String extension : extensions) {
					if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
						return true;
					}
				}
			}
			return false;
		}
	}
}