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
                        <a target="_blank" href="https://aboutspacex.com" style="color: #97ff00;text-decoration: none">Here Truth GO!&nbsp;&nbsp;&nbsp;&nbsp;
                            ©2023</a>
                    </div>
                </div>
                <div class="flex-center text-center" style="margin-top: 2vh">
                    <div style="color: #ffffff;padding: 1vw">
                        <button class="btn btn-sm  btn-danger" onclick="alertInfo()"><strong
                                    style="text-align: center;font-size: 0.8rem"><i
                                        class="bi bi-exclamation-diamond-fill"></i>免责声明</strong></button>
                        <div id="alertInfo" style="display: none;margin-top: 10px">
                            <label style="text-align: left;font-size: 0.5rem">本软件的内容由AI生成，若您使用本软件，则表示您默认接受以下声明:<br><br>
                                1、用户在使用本软件时，应遵守本国国家的法律法规，不得利用本软件从事违法犯罪活动，否则，一切后果由用户自行承担。<br><br>
                                2、用户应对本软件中的内容自行加以判断，并承担因使用内容而引起的所有风险，包括因对内容的正确性、完整性或实用性的依赖而产生的风险。本软件无法且不会对因用户行为而导致的任何损失或损害承担责任。<br><br>
                                3、鉴于本软件是调用的第三方服务，因网络及原服务商的调整等因素，本软件不保证服务不会中断！对服务的及时性、安全性、准确性也都不作保证。<br><br>
                                4、本软件有权于任何时间暂时或永久修改或终止本服务（或其任何部分），而无论其通知与否，本软件对用户和任何第三人均无需承担任何责任。<br><br>
                                5、本声明未涉及的问题参见国家有关法律法规，当本声明与国家法律法规冲突时，以国家法律法规为准。</label>
                        </div>
                    </div>
                </div>
            </div>
            <div style="margin-top: 2vh">
                <div class="text-center" style="margin-top: 1vh;color: whitesmoke">
                    <strong style="color: bisque"><i class="bi bi-person-circle"></i>系统范围</strong>
                </div>
                <div style="margin-top: 1vh" class="flex-center">
                    <textarea class="systemTextArea" rows="5" placeholder="您可以在这里为AI指定它要扮演的角色，以便更贴切地回答你的问题"
                              id="chatSystem"></textarea>
                </div>
            </div>
            <div style="margin-top: 2vh;box-shadow: 0px 0px 50px #00ff0d; border-bottom: 5px solid #00ff0d;">
                <div class="text-center" style="padding-top: 2vh;color: whitesmoke">
                    <strong style="color: bisque"><i class="bi bi-coin"></i>会话参数</strong>
                    <label class="form-text"
                           style="font-size: 0.5rem;display: block;padding-left: 2rem;padding-right: 2rem">以下均为默认值，明白含义之后再作适当调整</label>
                </div>
                <div style="margin-top: 2vh">
                    <label for="chatTemperature" class="parameterLabel"><i
                                title="采样温度是用来控制生成文本的随机性和确定性的，它的取值范围是0到2。采样温度越高，比如0.8，生成的文本越随机，可能会出现一些意想不到的结果；采样温度越低，比如0.2，生成的文本越确定，可能会更加符合输入序列的条件概率分布。一般来说，我们建议调整采样温度或者另一个参数叫做top_p，但不要同时调整两个参数"
                                class="bi bi-patch-question-fill"></i>采样温度:&nbsp;&nbsp;<span
                                id="chatTemperature_V" style="float: right;color: #02ff04">0.88</span></label>
                    <input type="range" class="form-range parameterSlider" min="0" max="2" step="0.01" value="0.88"
                           id="chatTemperature">
                </div>
                <div style="margin-top: .5vh">
                    <label for="maxToken" class="parameterLabel"><i
                                title="最大长度是一个可选的整数参数，用于指定在聊天完成中生成的最大的单词数量。输入的单词和生成的单词的总长度不能超过模型的上下文长度。"
                                class="bi bi-patch-question-fill"></i>最大长度:&nbsp;&nbsp;<span
                                id="maxToken_V" style="float: right;color: #02ff04">2800</span></label>
                    <input type="range" class="form-range parameterSlider" min="0" max="4096" step="200" value="2800"
                           id="maxToken">
                </div>
                <div style="margin-top: .5vh;margin-bottom: 2vh">
                    <label for="qaDeep" class="parameterLabel" style="color: #00ffea"><i
                                title="想让GPT模型的API产生上下文的对话效果，需要将历史问题及答案按照先后顺序再次发送给它，对话深度表示将当前对话之前的多少次历史问答整合进最新的问题之中，一般深度越高，对话的上下文效果越高，但其副作用也很明显，会降低最新答案的长度以及过快的消耗流量。"
                                class="bi bi-patch-question-fill"></i>对话深度:&nbsp;&nbsp;<span
                                id="qaDeep_V" style="float: right;color: #02ff04">1</span></label>
                    <input type="range" class="form-range parameterSlider" min="1" max="10" step="1" value="1"
                           id="qaDeep">
                </div>
            </div>
            <div style="margin-top: 3vh;" class="flex-center">
                <button class="btn btn-sm  btn-success" onclick="globalSetting()" style="font-size: 0.5rem"><i
                            class="bi bi-key-fill"></i>系统设置
                </button>
            </div>
            <div style="margin-top: 2vh" class="flex-center">
                <label style="font-size: 0.6rem;color: rgb(245 245 245 / 16%);">软件版本:${_S_V_!}</label>
            </div>
        </nav>

        <div style="position: fixed;top:10vh;right:20px;width: 18%;height: 80vh;transition: opacity 0.5s ease"
             id="historyArea">
            <h6 style="text-align: center;color: whitesmoke"><i class="bi bi-compass"></i>历史会话
            </h6>
            <div id="chatHistoryList">
                <#if CHAT_HISTORY??>
                    <#list CHAT_HISTORY as ch>
                        <div class="flex-center" name="chatHistoryElement" style="background-color: rgba(0,0,0,0.53);margin-top: 5px;margin-left: 5px">
                            <input class="historyBox" onclick="loadHistoryChat(this)" id="${ch.batchId!}" readonly
                                   value="${ch.batchTitle!}">
                            <i style="color: rgba(0,255,0,0.69);cursor: pointer;margin-right: 5px;margin-left: 5px"
                               class="bi bi-pencil"
                               onclick="editBatchTitle(this)"></i>
                            <i style="color: rgba(255,0,153,0.6);cursor: pointer" class="bi bi-trash3"
                               onclick="deleteBatch(this)"></i>
                        </div>
                    </#list>
                </#if>
            </div>
            <div class="flex-center" style="width: 100%;margin-top: 3vh">
                <input onkeyup="searchHisTitles(this)" placeholder="会话标题关键字..." class="form-control historySearch">
            </div>
            <div class="flex-center" style="width: 100%;margin-top: 2vh">
                <button onclick="window.location.reload()" class="btn btn-sm  btn-danger" title="新会话"><i
                            class="bi bi-chat"></i>新会话
                </button>
            </div>
        </div>

        <main role="main" class="col-md-8 ml-sm-auto col-lg-10 px-4" id="mainArea"
              style="margin-left: auto;background-image: url('/img/main_bg.jpg');background-repeat: no-repeat;background-size: cover;background-attachment:fixed;min-height: 100vh">
            <div class="row flex-center">
                <label class="form-text" style="color: greenyellow;" id="systemNotification">
                    <i style="color: yellow" class="bi bi-bell"></i>${INDEX_NOTE!}
                </label>
            </div>

            <div class="row" style="margin-top: 5vh;margin-bottom: 25vh">
                <div class="col-md-1">

                </div>
                <div class="col-md-8" id="questionList">

                </div>
                <div class="col-md-3">

                </div>
            </div>
        </main>
    </div>
</div>

<div class="modal fade" id="initNotification" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content" style="font-size: .8rem">
            <div class="modal-header" style="background-color: #e93c58;">
                <h6 class="modal-title text-center" id="myModalLabel"><strong>系统警告</strong></h6>
            </div>
            <div class="modal-body">
                <p>由于网络限制，您当前无法访问OpenAI的相关服务！请了解！</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-sm  btn-danger" data-bs-dismiss="modal">关闭</button>
            </div>
        </div>
    </div>
</div>

<div class="row fixed-bottom" style="bottom: 2vh;left: 16.68%">
    <div class="col-md-10">
        <div class="d-flex justify-content-end">
            <div class="container" id="submitArea" style="">
                <div class="row">
                    <div class="col-md-1"></div>
                    <div class="col-md-8 flex-center">
                        <input type="hidden" id="currentSN">
                        <input type="hidden" id="SRType" value="S">
                        <button onclick="SREvent()" style="display: none;font-size: 0.5rem;box-shadow: 0px 0px 50px #00ff0d;border: 1px solid whitesmoke" class="btn btn-success" id="btnRC">停止生成</button>
                    </div>
                    <div class="col-md-3"></div>
                </div>
                <div class="row margin-top-2vh" id="questionArea" >
                    <div class="col-md-1"></div>
                    <div class="col-md-8 input-container flex-center">
                        <div>
                        <textarea id="currentQuestion" class="form-control question-stage"
                                  style="max-height: 9vh; height: 4vh; background-color: rgba(32,32,32,0.5);"
                                  rows="3"
                                  placeholder=""></textarea>
                            <img id="submitButton" src="${request.contextPath}/img/gptButton.png"
                                 style="cursor: pointer"
                                 onclick="submitCode(1)">
                        </div>
                        <label style="text-align: end"><i title="生成图片" class="bi bi-image"
                                                          style="color: #ff029a;"></i></label>
                        <input id="generateImageMode" type="checkbox" class="form-check">
                    </div>
                    <div class="col-md-3"></div>
                </div>
            </div>
        </div>
    </div>
</div>

<#include "globalSetting.ftl">
<!-- Link to Bootstrap JS -->
<script src="${request.contextPath}/jquery.min.js"></script>
<script src="${request.contextPath}/bootstrap/js/bootstrap.min.js"></script>
<script src="${request.contextPath}/marked.min.js"></script>
<script src="${request.contextPath}/highlight/highlight.min.js"></script>
<script src="${request.contextPath}/toastr/toastr.min.js"></script>
<script src="${request.contextPath}/js/gptCode.js"></script>


<script>
    document.addEventListener("keypress", function (event) {
        if (event.key === "Enter") {
            let button = document.getElementById("submitButton")
            button.click()
            event.preventDefault()
        }
    });
    toastr.options.positionClass = 'toast-bottom-right';
    const currentQuestion = document.getElementById("currentQuestion");
    currentQuestion.addEventListener("focus", () => {
        currentQuestion.style.color = "white";
    });

    currentQuestion.addEventListener("blur", () => {
        currentQuestion.style.color = "yellow";
    });

    let userScroll = false
    document.addEventListener('wheel', function (event) {
        userScroll = true;
    });

    const divElement = document.getElementById('questionList');
    const observer = new MutationObserver(() => {
        if (!userScroll) {
            window.scrollTo(0, document.body.scrollHeight);
        }
    });
    const config = {attributes: true, childList: true, subtree: true};
    observer.observe(divElement, config);

    function alertInfo() {
        $("#alertInfo").slideToggle();
    };

    $('#chatTemperature').on('input', function () {
        let value = $(this).val();
        $('#chatTemperature_V').text(value);
    });

    $('#maxToken').on('input', function () {
        let value = $(this).val();
        $('#maxToken_V').text(value);
    });

    $('#topP').on('input', function () {
        let value = $(this).val();
        $('#topP_V').text(value);
    });

    $('#preP').on('input', function () {
        let value = $(this).val();
        $('#preP_V').text(value);
    });

    $('#freP').on('input', function () {
        let value = $(this).val();
        $('#freP_V').text(value);
    });

    $('#qaDeep').on('input', function () {
        let value = $(this).val();
        $('#qaDeep_V').text(value);
    });


    let historyAreaJQ = $('#historyArea')
    historyAreaJQ.css('display', 'none')
    historyAreaJQ.fadeIn(3000)

    let historyArea = document.getElementById("historyArea");

    historyArea.onmouseover = function () {
        historyArea.style.opacity = 1;
    };

    historyArea.onmouseout = function () {
        historyArea.style.opacity = 0.3;
    };

    setTimeout(function () {
        $('#systemNotification').fadeOut(1000)
    }, 12000);

    <#if NET_AVAILABLE?? && false == NET_AVAILABLE>
    const dialogImageSelector = new bootstrap.Modal(document.getElementById('initNotification'));
    dialogImageSelector.show()
    setTimeout(function () {
        dialogImageSelector.hide()
    }, 20000);
    </#if>
</script>
</body>
</html>
