# Website Downloader
Web application for downloading web pages for offline preview.

### Problem
1. Make a  website, hosted locally, using Java.
2. There is one input field where user can type HTML page URL of any website on internet. System checks if that page exists using AJAX requests.
3. There is one submit buttton which launches the process on the server.
4. Server side application reads a whole HTML page and all related files: pictures, css, etc.
5. System then changes, in the main HTML file, all links to other files, to be able to open it locally.
6. Finally it compresses all files into one .zip file and provides possibility of downloading it.

###Solution
1. Main part of the website are: `index.jsp` with two servlets `CheckPage.java`(asynchronous) and `DownloadPage.java`.
2. Input field has function on `oninput` event which tries to make connection to entered website. Status of the request is written as message below input.
3. Button is a form submit type.
4. HTML is read into string. That string is then searched for src or href, and parsed in a way that extracts links with extensions.
5. Accordingly, those same links are changed for local browsing. Duplicates are named example, example(1), example(2).
6. Download is available using the same form submit button. Dialog Box opens and shows only .zip files, suggesting name of the file to be page title.


---
##### This is an example of Java development task for a job interview.
