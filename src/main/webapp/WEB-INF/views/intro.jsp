<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>19세 이상 확인</title>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">
    <!-- jQuery -->
    <script type="text/javascript" src="https://code.jquery.com/jquery-1.12.4.min.js" ></script>
    <!-- iamport.payment.js -->
    <script src="https://cdn.iamport.kr/v1/iamport.js"></script>
    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            padding: 0;
            font-family: 'Noto Sans KR', sans-serif;
            background-color: white;
            color: #333;
        }

        .container {
            width: 100%;
            max-width: 360px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .section {
            margin-bottom: 48px;
            text-align: center;
        }

        .section img {
            width: 100px;
            margin-bottom: 16px;
        }

        .section p {
            font-size: 14px;
            line-height: 1.6;
            margin: 0 0 12px;
        }

        .section b {
            color: #e74c3c;
        }

        .section a.button {
            display: inline-block;
            margin-top: 10px;
            padding: 10px 0;
            width: 100%;
            background-color: #444;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            font-size: 14px;
            transition: background-color 0.2s;
        }

        .section a.button:hover {
            background-color: #222;
        }

        h3 {
            text-align: left;
            font-size: 15px;
            margin-bottom: 15px;
            border-left: 4px solid #888;
            padding-left: 10px;
        }

        form input[type="text"],
        form input[type="password"],
        form input[type="submit"] {
            width: 100%;
            padding: 10px;
            margin-bottom: 10px;
            font-size: 14px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }

        form input[type="submit"] {
            background-color: #444;
            color: white;
            font-weight: bold;
            cursor: pointer;
        }

        form input[type="submit"]:hover {
            background-color: #222;
        }

        .social-buttons button {
            width: 100%;
            margin-bottom: 10px;
            padding: 10px;
            border: none;
            border-radius: 4px;
            font-weight: bold;
            font-size: 14px;
            background-color: #f0f0f0;
            color: #333;
            cursor: pointer;
        }

        .social-buttons button:hover {
            background-color: #ddd;
        }

        .divider {
            height: 1px;
            background-color: #ddd;
            margin: 40px 0;
        }

        .certify input[type="button"] {
            width: 100%;
            padding: 10px;
            background-color: #f0f0f0;
            border: none;
            color: #333;
            border-radius: 4px;
            font-weight: bold;
            cursor: pointer;
            margin-bottom: 10px;
        }

        .certify input[type="button"]:hover {
            background-color: #ddd;
        }

        .certify p {
            font-size: 13px;
            color: #666;
            line-height: 1.5;
            margin-top: 12px;
            text-align: left;
        }
    </style>
</head>
<body>

<div class="container">

    <!-- 경고 안내 영역 -->
    <div class="section">
        <img src="${pageContext.request.contextPath}/images/intro_he01.gif" alt="19세 이상">
        <p>
            이 정보내용은 청소년 유해매체물로서 정보통신망 이용촉진법 및<br>
            정보보호 등에 관한 법률 및 청소년 보호법의 규정에 의하여<br>
            <b>19세 미만의 청소년</b>은 사용할 수 없습니다.
        </p>
        <a href="javascript:history.back()" class="button">19세 미만 나가기</a>
    </div>

    <!-- 로그인 영역 -->
    <div class="section">
        <h3>회원 로그인</h3>
        <form id="loginForm" action="/login" method="get">
            <input type="text" id="memberId" name="username" placeholder="아이디 입력" required>
            <input type="password" id="memberPw" name="password" placeholder="비밀번호 입력" required>
            <input type="submit" value="로그인">
        </form>


        <div class="social-buttons">
            <button>네이버 로그인</button>
            <button>카카오 로그인</button>
            <button>구글 로그인</button>
        </div>
    </div>

    <!-- 구분선 -->
    <div class="divider"></div>

    <!-- 비회원 성인인증 -->
    <div class="section certify">
        <h3>비회원 성인인증</h3>
        <input type="button" value="휴대폰 인증">
        <input type="button" value="간편 인증" onclick="requestCertification()">
        <p>
            본인 명의의 휴대폰 또는 간편 인증 수단으로 본인인증을 진행합니다.<br>
            명의자의 나이 정보로 19세 미만 여부를 확인합니다.
        </p>
    </div>
</div>

<script>
    // 콘솔에 IMP 객체 확인
    IMP.init("imp70328114");
    function requestCertification() {
        IMP.certification(
            {
                // param
                channelKey: "channel-key-fc3f00c8-38e1-4a2b-a3e3-35b444cacab8",
                merchant_uid: "mid_" + new Date().getTime(), // 주문 번호
                m_redirect_url: "{리디렉션 될 URL}", // 모바일환경에서 popup:false(기본값) 인 경우 필수, 예: https://www.myservice.com/payments/complete/mobile
                popup: false, // PC환경에서는 popup 파라미터가 무시되고 항상 true 로 적용됨
            },
            function (rsp) {
                if (rsp.success) {
                    $.ajax({
                        url: "/verify-cert",
                        method: "POST",
                        contentType: "application/json",
                        data: JSON.stringify({ imp_uid: rsp.imp_uid }),
                        success: function (res) {
                            alert(res.message);
                            if (res.adult === true) {
                                // POST 방식으로 /main 페이지로 이동
                                const form = document.createElement("form");
                                form.method = "get";
                                form.action = "/main";

                                // 필요한 경우 서버로 보낼 값이 있으면 input 추가 가능
                                // 예: 사용자 이름, 전화번호 등 인증 결과 일부를 넘기고 싶다면 여기에 추가

                                document.body.appendChild(form);
                                form.submit();
                            } else {
                                alert("19세 미만은 접근할 수 없습니다.");
                            }
                        },
                        error: function (xhr, status, error) {
                            console.error("서버 오류:", xhr.responseText);
                            alert("서버 오류가 발생했습니다.");
                        }
                    });
                } else {
                    alert("인증 실패: " + rsp.error_msg);
                }
            }
        );
    }

    $(document).ready(function() {
        $("#loginForm").submit(function(event) {
            event.preventDefault();  // 기본 폼 제출 막기

            const memberId = $("#memberId").val().trim();
            const memberPw = $("#memberPw").val().trim();

            if (!memberId || !memberPw) {
                alert("아이디와 비밀번호를 모두 입력해주세요.");
                return;
            }

            $.ajax({
                url: "/auth/loginResult",
                method: "POST",
                contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
                data: {
                    memberId: memberId,
                    memberPw: memberPw
                },
                success: function(res) {
                    if (res.loginResult === true) {
                        window.location.href = '/loading';
                    } else {
                        alert("아이디 또는 비밀번호를 확인해주세요.");
                    }
                },
                error: function(xhr, status, error) {
                    console.error("로그인 요청 중 오류 발생:", error);
                    alert("서버 오류가 발생했습니다. 다시 시도해주세요.");
                }
            });
        });
    });


</script>

</body>
</html>
