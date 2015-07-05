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

        function submitEvent() {
            document.getElementById("overlay").style.visibility = 'visible';
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

        @keyframes spinner {
            to {
                transform: rotate(360deg);
            }
        }

        @-webkit-keyframes spinner {
            to {
                -webkit-transform: rotate(360deg);
            }
        }

        #overlay {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 10;
            background-color: rgba(0, 0, 0, 0.3);
            visibility: hidden;
        }

        .spinner {
            border-radius: 50%;
            border: 1px solid rgba(255, 255, 255, 1);
            border-top-color: #03ade0;
            animation: spinner 1.3s linear infinite;
            -webkit-animation: spinner 1.3s linear infinite;
            position: absolute;
            top: 50%;
            left: 50%;
            width: 25px;
            height: 25px;
        }
    </style>
</head>
<body>
<div class="container">
    <form action="downloadPage" method="post" onsubmit="submitEvent()">
        <label for="url">Enter URL</label>
        <br/> <input type="text" id="url" name="url" oninput="checkURL()">
        <br/> <span id="status"></span>
        <br/><br/> <input type="submit" id="button" value="Download Page" disabled="disabled"/>
    </form>
</div>
<div id="overlay">
    <div class="spinner"></div>
</div>
</body>
</html>
