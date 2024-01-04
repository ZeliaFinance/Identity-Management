<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Email Template</title>
    <link href="https://fonts.cdnfonts.com/css/aileron" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@100;300;400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<style>
    * {
        margin: 0;
        font-family: aileron, sans-serif;
        line-height: 20px;
    }

    .mt-6 {
        margin-top: 60px;
    }

    .mb-6 {
        margin-bottom: 60px;
    }

    .pb-6 {
        padding-bottom: 60px;
    }

    .mt-2 {
        margin-top: 20px;
    }

    .mb-2 {
        margin-bottom: 20px;
    }

    .font-inter {
        font-family: Inter;
    }
</style>
<body class="mb-2 mt-6">
<div style="padding: 0 20%">
    <div style="width: 96px; height: 96px;" class="mb-6">
        <img
                style="width: 100%; height: 100%"           src="https://i.imgur.com/p5LYCEH.png" alt="zelia-logo">
    </div>
    <div class="mb-6">
        <h4 class="mb-2">Hello 👋🏽</h4>
        <p class="mb-2">You're almost there! 😊</p>
        <p>To finish setting up your Zelia account, please verify your email address with the code below.</p>
    </div>
    <table width="100%" style="border-radius: 24px;
background: #F9FAFB; padding: 38px" class="mb-6">
        <tbody>
        <tr align="center">
            <td><h5 class="font-inter" style="font-size: 40px; color: #3A60CB">${otp}</h5></td>
        </tr>
        </tbody>
    </table>

    <div class="pb-6 mb-2" style="border-bottom: 2px solid rgba(51, 51, 51, 0.19)">
        <p class="mb-2">Note that this OTP expires in 8 minutes.</p>
        <p class="mb-2">If you have any questions or need assistance, please send an email to <span style="text-decoration: underline; color: #3A60CB;">contact@zeliafinance.com</span> or call us - +234 908 830 8010.</p>
        <div>
            <span>Thank you.</span> <br />
            <h4>Zelia.</h4>
        </div>
    </div>

    <div>
        <div style="color: #121826;" class="mb-2">
            <h4 style=" font-size: 20px;" class="mb-2">Zelia</h4>
            <p style="font-size: 14px; cursor: pointer;" class="mb-2 font-inter">Unsubscribe</p>
            <p style="color: #757575; font-size: 14px;" class="font-inter mb-2">&copy;2023 All rights reserved.</p>
        </div>
    </div>
</div>
</body>
</html>