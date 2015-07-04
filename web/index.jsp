<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Website Downloader</title>
    <script type="text/javascript">
        var request;
        var requestFinishedState = 4;

        function toggleButton(enabled) {
            document.getElementById("button").disabled = !enabled;
        }

        function setStatus(message, color) {
            document.getElementById("status").style.color = color;
            document.getElementById("status").textContent = message;
        }

        function handleStateChange() {
            if (request.readyState == requestFinishedState) {
                if (request.responseText == "200") {
                    setStatus("Page is OK.", "green");
                    toggleButton(true);
                } else {
                    setStatus("Site not reachable!", "red");
                }
            } else {
                setStatus("Loading...", "blue");
            }
        }

        function checkURL() {
            toggleButton(false);
            var urlString = document.getElementById("url").value;
            if (urlString == "") {
                setStatus("", "");
            } else {
                var url = "checkPage?url=" + urlString;
                if (window.XMLHttpRequest) {
                    request = new XMLHttpRequest();
                } else {
                    request = new ActiveXObject("Microsoft.XMLHTTP");
                }
                request.onreadystatechange = handleStateChange;
                request.open("POST", url);
                request.send();
            }
        }
    </script>

    <style>
        input {
            width: 250px;
        }

        .container {
            position: relative;
            top: 120px;
            background-color: #dedede;
            text-align: center;
            padding: 20px;
        }
    </style>
</head>
<body>
<div class="container">
    <form action="downloadPage" method="post">
        <label for="url">Enter URL</label>
        <br/> <input type="text" id="url" name="url" oninput="checkURL()">
        <br/> <span id="status"></span>
        <br/><br/> <input type="submit" id="button" value="Download Page" disabled="disabled"/>
    </form>
</div>
</body>
</html>
