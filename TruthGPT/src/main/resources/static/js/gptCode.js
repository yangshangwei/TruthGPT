const decoder = new TextDecoder('utf-8');
const globalSettingDLG = new bootstrap.Modal(document.getElementById('globalSettingDLG'));


function globalSetting() {

    $.ajax({
        type: "GET",
        url: "/gpt/ls",
        dataType: 'json',
        success: function (data) {
            if (data.code === 0) {

                $('#openAI_APIKey').val(data.result.openAIKey)
                globalSettingDLG.show()
                $('#globalSettingDLG .modal-footer button').on('click', function (event) {
                    let $button = $(event.target); // The clicked button

                    let apiKey = $('#openAI_APIKey').val()

                    $(this).closest('.modal').one('hidden.bs.modal', function () {
                        if ($button.hasClass("btn-danger")) {
                            if (apiKey) {
                                $.ajax({
                                    type: "POST",
                                    url: "/gpt/ls/update",
                                    contentType: "application/json",
                                    data: JSON.stringify({
                                        openAIKey: apiKey,
                                    }),
                                    dataType: 'json',
                                    success: function (data) {
                                        if (data.code === 0) {
                                            toastr.info(data.msg)
                                        } else {
                                            toastr.error(data.msg)
                                        }
                                    }, error: function (e) {
                                        toastr.error(e)
                                    }
                                });
                            } else {
                                toastr.error("保存或更新设置，API Key不可以为空!")
                            }
                        }
                    });
                });

            } else {
                toastr.error(data.msg)
            }
        }, error: function (e) {
            toastr.error(e)
        }
    });
}

function authValidation() {
    $.ajax({
        type: "GET",
        url: "/gpt/authValidate",
        dataType: 'json',
        success: function (data) {

        }, error: function (e) {
            toastr.error(e)
        }
    });
}

let last_question = '';

function submitCode(type) {
    let sn = generateUUID()
    if(type === 1) {
        last_question = $('#currentQuestion').val()
    }
    $('#currentQuestion').val('')
    if (!last_question) {
        const inputField = document.getElementById("currentQuestion")
        inputField.classList.add("shake");
        setTimeout(function () {
            inputField.classList.remove("shake");
        }, 500);
        return
    }
    last_question = last_question.replace(/</g, "&lt;");
    last_question = last_question.replace(/>/g, "&gt;");
    let questionList = $('#questionList')
    let newAsk = '<div class="container" style="margin-top:2vh;color: whitesmoke;"><div class="row focus-area"><div class="col-md-1 flex-center"><img style="width: 2rem" src="/img/user.png"></div><div class="col-md-9 flex-left"><pre class="inner-stage" style="color: rgb(241,241,241);font-size: 1rem;width: 100%" id="Q-' + sn + '">' + last_question + '</pre></div><div class="col-md-2 flex-center"></div></div></div>'
    questionList.append(newAsk)

    let characterCount = 0

    let im = 0;
    if (document.getElementById('generateImageMode').checked) {
        im = 1;
    }

    if (1 === im) {

        let newImageH = '<div class="container" style="margin-top:1vh;color: whitesmoke;">' +
            '<div class="row focus-area" id="IGWaiting-' + sn + '">' +
            '<div class="col-md-1 flex-center">' +
            '<img id="BIMG-' + sn + '" style="width: 2rem" src="/img/tto-sml.png"></div></div></div>'

        questionList.append(newImageH)
        let imgId = "#BIMG-" + sn
        $(imgId).addClass("breathing focus-image")

        $('#btnRC').css('display', 'none')
        $('#btnRC').text("停止生成")
        $('#SRType').val("S")

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: "/gpt/ig/" + sn,
            data: JSON.stringify({
                prompt: last_question,
            }),
            dataType: 'json',
            success: function (data) {
                let content = ''
                if (data.code === 0) {
                    content += '<div style="padding: 2vh" class="col-md-5"><img class="focus-image" style="width:100%" src="data:image/png;base64,' + data.result[0] + '"></div>' + '<div style="padding: 2vh" class="col-md-5"><img class="focus-image" style="width:100%" src="data:image/png;base64,' + data.result[1] + '"></div>'
                    let IGWaiting = "#IGWaiting-" + sn
                    $(IGWaiting).append(content);
                } else {
                    toastr.error(data.msg)
                }
                $(imgId).removeClass("breathing focus-image")
            },
            error: function () {
                document.getElementById(sn).innerHTML += "系统异常，请稍后再试"
                $(imgId).removeClass("breathing focus-image")
            }
        })
    } else {

        let newAnswer = '<div class="container" style="margin-top:2vh;color: whitesmoke;">' +
            '<div class="row focus-area flex-center">' +
            '<div class="col-md-1 flex-center">' +
            '<img id="BIMG-' + sn + '" style="width: 2rem" src="/img/tto-sml.png"></div>' +
            '<div class="col-md-9">' +
            '<pre style="color: rgb(241,241,241);" id="Code-' + sn + '"></pre>' +
            '</div><div class="col-md-2">' +
            '<input type="hidden"  value="' + sn + '">' +
            '<div class="row text-center">' +
            '<h5 style="color: #96f6c6" id="Count-' + sn + '"></h5></div>' +
            '<div class="row flex-center" style="margin-top: 1vh">' +
            '<span class="zoom quick-btn" onclick="copyResult(this)" title="复制"><img class="img-100" src="/img/sc_cp.png"></span>' +
            '<span class="zoom quick-btn" onclick="toEnglish(this)" title="翻译成英文"><img class="img-100" src="/img/sc_en.png"></span>' +
            '<span class="zoom quick-btn" onclick="toChinese(this)" title="翻译成中文"><img class="img-100" src="/img/sc_cn.png"></span>' +
            '<span class="zoom quick-btn" onclick="listKeywords(this)" title="提取关键字"><img class="img-100" src="/img/sc_kw.png"></span>' +
            '</div></div>'
        questionList.append(newAnswer)
        let imgId = "#BIMG-" + sn
        $(imgId).addClass("breathing focus-image")
        let chatTemperature = parseFloat($('#chatTemperature').val())
        let maxToken = parseInt($('#maxToken').val())
        let qaDeep = parseInt($('#qaDeep').val())
        let chatSystem = $('#chatSystem').val()

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: "/gpt/tg/" + sn,
            data: JSON.stringify({
                prompt: last_question,
                systemScope: chatSystem
            }),
            dataType: 'json',
            success: function (data) {
                if (0 === data.code) {
                    let source = new EventSource("/gpt/asyn/chat/" + sn + "/" + maxToken + "/" + chatTemperature + "/" + qaDeep);
                    let startTime = null;
                    startAsk(sn);
                    source.addEventListener("message", function (event) {
                        let words = atob(event.data)
                        words = decoder.decode(new Uint8Array(words.split('').map(c => c.charCodeAt(0))));

                        if (words.startsWith("[_T_O_F_]")) {
                            source.close()
                            $(imgId).removeClass("breathing focus-image")
                            highlightWithNewContent(document.getElementById("Code-" + sn), words.substring(9));
                            const duration = new Date() - startTime;
                            toastr.success("耗时[" + duration / 1000 + "]秒,回答还算满意吧～～")
                            $('#SRType').val("R")
                            $('#btnRC').text("重新生成")

                            $.ajax({
                                type: "GET",
                                url: "/gpt/findBatchTitle",
                                dataType: 'json',
                                success: function (data) {
                                    if (data.code === 0) {
                                        if (data.result) {
                                            let newChat = '<div class="flex-center" style="background-color: rgba(0,0,0,0.53);margin-top: 5px;margin-left: 5px">\n' +
                                                '<input class="historyBox" onclick="loadHistoryChat(this)" id="' + data.result.batchId + '" readonly value="' + data.result.batchTitle + '">' +
                                                '                            <i style="color: rgba(0,255,92,0.65);cursor: pointer;margin-right: 5px;margin-left: 5px" class="bi bi-pencil" onClick="editBatchTitle(this)"></i>\n' +
                                                '                            <i style="color: rgba(255,2,154,0.67);cursor: pointer" class="bi bi-trash3" onclick="deleteBatch(this)"></i>\n' +
                                                '                        </div>' +
                                                ''
                                            let newChatDom = $('#chatHistoryList')
                                            newChatDom.css('display', 'none')
                                            newChatDom.prepend(newChat)
                                            newChatDom.fadeIn(2000)
                                        }
                                    } else {
                                    }
                                }
                            })

                        }else if (words.startsWith("[_U_O_F_]")){
                            source.close()
                            $(imgId).removeClass("breathing focus-image")
                            highlightDom(document.getElementById("Code-" + sn));
                        } else {
                            characterCount += 1
                            document.getElementById("Count-" + sn).innerHTML = characterCount
                            document.getElementById("Code-" + sn).innerHTML += words
                        }
                    }, false);
                    source.addEventListener("error", function (error) {
                        $('#SRType').val("R")
                        $('#btnRC').text("重新生成")
                        source.close()
                        $(imgId).removeClass("breathing focus-image")
                        highlightDom(document.getElementById("Code-" + sn));
                    }, false);
                    source.addEventListener("open", function () {
                        startTime = new Date()
                    })
                }else {
                    $(imgId).removeClass("breathing focus-image")
                    toastr.error(data.msg)
                }
            },
            error: function (e) {
                document.getElementById(sn).innerHTML += "系统异常，请稍后再试"
            }
        });
    }
}

function startAsk(sn) {
    $('#blinkCaret').css('display', 'none')
    $('#btnRC').css('display', '')
    $('#btnRC').text("停止生成")
    $('#SRType').val("S")
    userScroll = false;
    $('#currentSN').val(sn)
}

function SREvent() {

    let sn = $('#currentSN').val()
    if (!sn) {
        return
    }

    let sr = $('#SRType').val()
    if("S" === sr){
        stop(sn)
    }else if ("R" === sr){
        restart()
    }else {

    }
}

function restart(){
    $('#SRType').val("S")
    $('#btnRC').text("停止生成")

    submitCode(0)
}
function stop(sn){
    $('#SRType').val("R")
    $('#btnRC').text("重新生成")
    $.ajax({
        type: "GET",
        url: "/gpt/tg/stop/" + sn,
        dataType: 'json',
        success: function () {

        },
        error: function (data) {

        }
    })
}

function searchHisTitles(ipt) {
    let filter = $(ipt).val()

    let ches = $('[name=chatHistoryElement]');
    for(let i = 0;i < ches.length;i++){
        let tt = $(ches[i]).find('input').val()
        if (tt.indexOf(filter) > -1) {
            $(ches[i]).css('display','')
        } else {
            $(ches[i]).css('display','none')
        }
    }
}

function deleteBatch(his) {
    const batchId = $(his).parent().find('input').attr('id')

    if ($(his).hasClass('bi-trash3')) {
        $(his).removeClass('bi-trash3').addClass('bi-check-circle-fill')
    } else if ($(his).hasClass('bi-check-circle-fill')) {
        $.ajax({
            type: "GET",
            url: "/gpt/delChatHis/" + batchId,
            dataType: 'json',
            success: function (data) {
                if (data.code === 0) {
                    if (data.result) {
                        $("#questionList").empty()
                        $("#questionList").css('display', 'none')
                        $("#questionList").fadeIn(1000)
                    }
                    $(his).parent().remove()
                } else {
                    toastr.error(data.msg)
                }
            },
            error: function (data) {

            }
        })
    }
}

function editBatchTitle(his) {

    const batchId = $(his).parent().find('input').attr('id')
    let input = $('#' + batchId)

    if ($(his).hasClass('bi-pencil')) {
        input.prop('readonly', false);
        input.focus();
        let inputVal = input.val();
        input.val('').val(inputVal);

        $(his).removeClass('bi-pencil').addClass('bi-check-circle-fill')
    } else {
        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: "/gpt/updateChatHis",
            data: JSON.stringify({
                batchId: batchId,
                batchTitle: input.val()
            }),
            dataType: 'json',
            success: function (data) {
                if (data.code === 0) {
                    input.prop('readonly', true);
                    $(his).removeClass('bi-check-circle-fill').addClass('bi-pencil')
                } else {
                    toastr.error(data.msg)
                }
            },
            error: function (data) {

            }
        })
    }
}

function loadHistoryChat(his) {
    if($(his).parent().find('input').prop('readonly')) {

        $('#blinkCaret').css('display', 'none')
        $('#btnRC').css('display', 'none')
        $('#btnRC').text("停止生成")
        $('#SRType').val("S")
        userScroll = false;

        const batchId = $(his).parent().find('input').attr('id')
        $.ajax({
            type: "GET",
            url: "/gpt/chatHis/" + batchId,
            dataType: 'json',
            success: function (data) {
                if (data.code === 0) {
                    $('#chatSystem').empty()
                    $('#chatSystem').val(data.result.batchInfo.systemScope)
                    $("#questionList").empty()
                    $("#questionList").css('display', 'none')
                    for (let i = 0; i < data.result.chatRecordList.length; i++) {
                        let chat = data.result.chatRecordList[i]
                        let question = chat.question
                        let answer = chat.answer
                        let sn = chat.tmpSN

                        let newAsk = '<div class="container" style="margin-top:2vh;color: whitesmoke;"><div class="row focus-area"><div class="col-md-1 flex-center"><img class="img-50" src="/img/user.png"></div><div class="col-md-9 flex-left"><pre class="inner-stage" style="color: rgb(241,241,241);font-size: 1rem;width: 100%" id="Q-' + sn + '">' + question + '</pre></div><div class="col-md-2 flex-center"></div></div></div>'
                        $("#questionList").append(newAsk)
                        let newAnswer = '<div class="container" style="margin-top:2vh;color: whitesmoke;">' +
                            '<div class="row focus-area flex-center">' +
                            '<div class="col-md-1 flex-center">' +
                            '<img id="BIMG-' + sn + '" style="width: 2rem" src="/img/tto-sml.png"></div>' +
                            '<div class="col-md-9">' +
                            '<pre style="color: rgb(241,241,241);" id="Code-' + sn + '">' + answer + '</pre>' +
                            '</div><div class="col-md-2">' +
                            '<input type="hidden"  value="' + sn + '">' +
                            '<div class="row flex-center">' +
                            '<span class="zoom quick-btn" onclick="copyResult(this)" title="复制"><img class="img-100" src="/img/sc_cp.png"></span>' +
                            '<span class="zoom quick-btn" onclick="toEnglish(this)" title="翻译成英文"><img class="img-100" src="/img/sc_en.png"></span>' +
                            '<span class="zoom quick-btn" onclick="toChinese(this)" title="翻译成中文"><img class="img-100" src="/img/sc_cn.png"></span>' +
                            '<span class="zoom quick-btn" onclick="listKeywords(this)" title="提取关键字"><img class="img-100" src="/img/sc_kw.png"></span>' +
                            '</div></div>'
                        $("#questionList").append(newAnswer)
                        highlightWithNewContent(document.getElementById("Code-" + sn), answer);
                    }
                    $("#questionList").fadeIn(1000)
                } else {
                    toastr.error(data.msg)
                }
            },
            error: function (data) {

            }
        })
    }
}

function copyResult(btn) {
    let result = $(btn).parent().parent().parent().find('pre').text()
    navigator.clipboard.writeText(result).then(function () {
        toastr.info("已成功复制到剪切板")
    }, function (err) {
        console.error('复制到剪切板失败', err);
    });
}

function toEnglish(btn) {
    shortCommand(btn, "2en")
}

function toChinese(btn) {
    shortCommand(btn, "2cn")
}

function listKeywords(btn) {
    shortCommand(btn, "kws")
}


function shortCommand(btn, cmd) {

    let characterCount = 0
    let osn = $(btn).parent().parent().find('input').val()
    let sn = generateUUID()

    let newAnswer = '<div class="row focus-area" style="margin-top:1vh;color: whitesmoke;">' +
        '<div class="col-md-1 flex-center">' +
        '<img id="BIMG-' + sn + '" style="width: 2rem" src="/img/tto-sml.png"></div>' +
        '<div class="col-md-9 flex-center">' +
        '<pre style="color: rgb(171,255,2);width: 100%" id="Code-' + sn + '"></pre>' +
        '</div><div class="col-md-2">' +
        '<div class="row text-center">' +
        '<h5 style="color: #9efad1" id="SCCount-' + sn + '"></h5></div>' +
        '<div class="row flex-center">' +
        '<span class="zoom quick-btn" onclick="copyResult(this)" title="复制"><img class="img-100" src="/img/sc_cp.png"></span>' +
        '</div></div></div>'

    $(newAnswer).insertAfter($(btn).parent().parent().parent())

    let imgId = "#BIMG-" + sn
    $(imgId).addClass("breathing focus-image")

    let source = new EventSource("/gpt/asyn/sc/" + osn + "/" + cmd);
    $('#blinkCaret').css('display', 'none')
    userScroll = false;
    source.addEventListener("message", function (event) {

        let words = atob(event.data)
        words = decoder.decode(new Uint8Array(words.split('').map(c => c.charCodeAt(0))));

        if (words.startsWith("[_T_O_F_]")) {
            source.close()
            $(imgId).removeClass("breathing focus-image")
            highlightWithNewContent(document.getElementById("Code-" + sn), words.substring(9));
            toastr.info("回答结束，还算满意吧～～")
        } else {
            characterCount += 1
            document.getElementById("SCCount-" + sn).innerHTML = characterCount
            document.getElementById("Code-" + sn).innerHTML += words
        }
    }, false);

    source.addEventListener("error", function (error) {
        source.close()
        $(imgId).removeClass("breathing focus-image")
        highlightDom(document.getElementById("Code-" + sn));
    }, false);
}


function highlightDom(div) {
    const code = div.innerText
    div.innerHTML = marked.parse(code)
    hljs.highlightAll();
}

function highlightWithNewContent(div, cnt) {
    div.innerHTML = marked.parse(cnt)
    hljs.highlightAll();
}

function generateUUID() {
    let d = new Date().getTime();
    if (typeof performance !== 'undefined' && typeof performance.now === 'function') {
        d += performance.now(); //use high-precision timer if available
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}
