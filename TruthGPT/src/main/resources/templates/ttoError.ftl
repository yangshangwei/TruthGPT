<!DOCTYPE html>
<html lang="zh-classical" style="height: 100%;">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>TruthGPT</title>
    <link href="${request.contextPath}/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link href="${request.contextPath}/bootstrap/bootstrap-icons.css" rel="stylesheet">
    <link href="${request.contextPath}/github-markdown-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/highlight/styles/night-owl.min.css" rel="stylesheet">
    <link href="${request.contextPath}/toastr/toastr.min.css" rel="stylesheet">
    <link href="${request.contextPath}/css/common.css" rel="stylesheet">
    <link href="${request.contextPath}/css/gpt.css" rel="stylesheet">
</head>
<body style="height: 100%;background-color: #444654">
<div class="container-fluid h-100">
    <div class="row">
        <nav class="col-md-2 d-none d-md-block sidebar"
             style="background-color: #202123;box-shadow: 0px 0px 50px #00ff0d;position: fixed;height: 100vh;padding-right: 0;padding-left: 0">
            <div class="sidebar-sticky" style="box-shadow: 0px 0px 50px #00ff0d;border-bottom: 5px solid #00ff0d">
                <a class="navbar-brand text-center" href="https://aboutspacex.com" target="_blank">
                    <img class="image-gradient" src="${request.contextPath}/img/logo.png"
                         style="width: 60%;margin-top: 2vh" alt="ElonNetTech">
                </a>

                <div class="flex-center text-center"
                     style="color: #a6a6a6;font-size: smaller;margin-top: 2vh">
                    <div>
                        <a target="_blank" href="https://aboutspacex.com" style="color: #c462ff;text-decoration: none">科技品鉴官&nbsp;&nbsp;&nbsp;&nbsp;
                            ©2023</a>
                        <label style="color: #00e1ff" class="form-text">本服务由OpenAI的GPT-${INDEX_VERSION!}
                            模型驱动！</label>
                    </div>
                </div>
                <div class="flex-center text-center" style="margin-top: 2vh">
                    <div style="color: #ffffff;padding: 1vw">
                        <button class="btn btn-sm btn-danger" onclick="alertInfo()"><strong
                                    style="text-align: center;font-size: 0.8rem"><i
                                        class="bi bi-exclamation-diamond-fill"></i>免责声明</strong></button>
                        <div id="alertInfo" style="margin-top: 10px">
                            <label style="text-align: left;font-size: 0.5rem">本软件由本软件基于OpenAI的GPT
                                模型API开发，若您使用本软件，则表示您默认接受以下声明！<br><br>
                                1、用户在使用本软件时，应遵守中华人民共和国的法律法规，不得利用本软件从事违法犯罪活动，否则，一切后果由用户自行承担。<br><br>
                                2、用户应对本软件中的内容自行加以判断，并承担因使用内容而引起的所有风险，包括因对内容的正确性、完整性或实用性的依赖而产生的风险。本软件无法且不会对因用户行为而导致的任何损失或损害承担责任。<br><br>
                                3、鉴于本软件是调用的第三方服务，因网络及原服务商的调整等因素，本软件不保证服务不会中断！对服务的及时性、安全性、准确性也都不作保证。<br><br>
                                4、本软件有权于任何时间暂时或永久修改或终止本服务（或其任何部分），而无论其通知与否，本软件对用户和任何第三人均无需承担任何责任。<br><br>
                                5、本声明未涉及的问题参见国家有关法律法规，当本声明与国家法律法规冲突时，以国家法律法规为准。</label>
                        </div>
                    </div>
                </div>
            </div>
        </nav>


        <main role="main" class="col-md-8 ml-sm-auto col-lg-10 px-4" id="mainArea"
              style="margin-left: auto;background-image: url('/img/main_bg.jpg');background-repeat: no-repeat;background-size: cover;background-attachment:fixed;min-height: 100vh">

            <div class="row" style="margin-top: 5vh;margin-bottom: 15vh">
                <div class="col-md-1">

                </div>
                <div class="col-md-8 flex-center" id="questionList">
                    <h2 style="color: #f4a62a">${ERROR!}</h2>
                </div>
                <div class="col-md-3">

                </div>
            </div>
        </main>
    </div>
</div>

<!-- Link to Bootstrap JS -->
<script src="${request.contextPath}/jquery.min.js"></script>
<script src="${request.contextPath}/bootstrap/js/bootstrap.min.js"></script>
<script src="${request.contextPath}/marked.min.js"></script>
<script src="${request.contextPath}/highlight/highlight.min.js"></script>
<script src="${request.contextPath}/toastr/toastr.min.js"></script>
<script src="${request.contextPath}/js/gptCode.js"></script>


<script>

</script>
</body>
</html>
