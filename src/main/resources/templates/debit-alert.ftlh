<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Invite</title>
    <link href="https://fonts.cdnfonts.com/css/aileron" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@100;300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://unicons.iconscout.com/release/v4.0.8/css/line.css">
    <link rel="preload" href="https://res.cloudinary.com/de7eypejp/image/upload/v1703801996/Logo_jsulke.png" as="image">
</head>
<style>
    * {
        margin: 0;
        font-family: aileron, sans-serif;
        line-height: 1.5rem;
        box-sizing: border-box;
    }

    h1 {
        font-size: 3rem;
        line-height: 1;
    }

    p {
        font-size: 1rem;
    }

    .container {
        background-color:#ffff;
        padding: 8rem 4rem;
    }

    .email-content {
        background-color: #fff;
        padding: 5rem 2rem 2rem 2rem;
        border-radius: 1rem;

    }

    .email-content-text-wrapper {
        margin-top: 5rem;
    }

    .header-text {
        font-size: 1.5rem;
    }

    .table-wrapper {
        width: 80%;
        background-color: #F5F5F5;
        padding: 2rem 3rem;
        display: flex;
        flex-direction: column;
        align-items: flex-start;
    }

    .table-col {
        width: 100%;
        padding: 1rem 0.8rem;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #D7D4D4;
    }

    .table-col:last-of-type {
        border: none;
    }

    .footer {
        padding-top: 3rem;
        padding-bottom: 5rem;
        border-top: 3px solid #333333
    }

    .social-links-wrapper {
        display: flex;
        align-items: center;
        gap: 1rem;
    }

    .social-link {
        border: 1px solid #121826;
        border-radius: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        width: 3rem;
        height: 3rem;
        font-size: 2rem;
        text-decoration: none;
        color: #121826;
        cursor: pointer;
    }



    a {
        text-decoration: underline;
        color: #407BFF;
    }

    .mt-6 {
        margin-top: 60px;
    }

    .mb-6 {
        margin-bottom: 60px;
    }

    .mb-4 {
        margin-bottom: 40px;
    }

    .mb-3 {
        margin-bottom: 30px;
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

    @media screen and (max-width: 900px) {
        .table-wrapper {
            width: 100%;
        }
    }
</style>
<body class="mb-2 mt-6">
<div style="padding: 0 20%">
    <div class="container">
        <div class="email-content">
            <div style="width: 96px; height: 96px;" class="mb-2">
                <img style="width: 100%; height: 100%" src="https://res.cloudinary.com/de7eypejp/image/upload/v1703801996/Logo_jsulke.png" alt="Zelia logo">
            </div>
            <div class="email-content-text-wrapper">
                <div class="pb-6">
                    <p class="mb-4">Hello  <strong>*Username*</strong>,</p>
                    <p class="header-text mb-4"><strong>Your wallet has been debited successfully! 💸</strong></p>
                    <p class="mb-4">Find the details of your transaction below: </p>
                    <div class="table-wrapper mb-4">
                        <div class="table-col">
                            <p><strong>Transaction Reference</strong></p>
                            <p>0836te53544</p>
                        </div>
                        <div class="table-col">
                            <p><strong>Transaction Type</strong></p>
                            <p>Debit</p>
                        </div>
                        <div class="table-col">
                            <p><strong>Amount</strong></p>
                            <p>NGN 200,000.00</p>
                        </div>
                        <div class="table-col">
                            <p><strong>Date</strong></p>
                            <p>12/02/2023</p>
                        </div>
                        <div class="table-col">
                            <p><strong>Account Balance</strong></p>
                            <p>NGN 50,000.00</p>
                        </div>
                    </div>
                    <div class="mb-4">
                        If you didn&apos;t initiate this transaction, please immediately  reach out to us via email <a href="mailto:contact@zeliafinance.com">contact@zeliafinance.com</a> or call us - <span style="color: #407BFF;">+234 908 830 8010</span>
                    </div>
                </div>

                <footer class="footer">
                    <div style="color: #121826;" class="mb-6">
                        <h2 class="mb-2">Zelia</h2>
                        <p style="font-size: 0.9rem; cursor: pointer;" class="mb-2 font-inter"><strong>Unsubscribe</strong></p>
                        <p style="color: #757575; font-size: 14px;" class="font-inter mb-2">&copy;2023 All rights reserved.</p>
                    </div>
                    <div class="social-links-wrapper mb-6">
                        <a href="https://www.facebook.com/profile.php?id=100093570991545" target="_blank" class="social-link">
                            <i class="uil uil-facebook-f"></i>
                        </a>
                        <a href="https://instagram.com/bankwithzelia" target="_blank" class="social-link">
                            <i class="uil uil-instagram"></i>
                        </a>
                        <a href="https://tiktok.com/@bankwithzelia" target="_blank" class="social-link">
                            <svg width="25" height="25" viewBox="0 0 15 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M14.7043 4.46384V7.29238C14.21 7.2441 13.5677 7.13179 12.8571 6.87151C11.9293 6.53145 11.2387 6.06649 10.7864 5.70649V11.4234L10.7748 11.4056C10.7822 11.5189 10.7864 11.6344 10.7864 11.7509C10.7864 14.5899 8.47631 16.901 5.63623 16.901C2.79615 16.901 0.486084 14.5899 0.486084 11.7509C0.486084 8.91183 2.79615 6.59966 5.63623 6.59966C5.91436 6.59966 6.18725 6.62172 6.45383 6.66475V9.45233C6.19774 9.36102 5.92276 9.31171 5.63623 9.31171C4.29175 9.31171 3.19707 10.4053 3.19707 11.7509C3.19707 13.0964 4.29175 14.19 5.63623 14.19C6.9807 14.19 8.07538 13.0953 8.07538 11.7509C8.07538 11.7005 8.07434 11.6501 8.07119 11.5997V0.490234H10.8976C10.9081 0.729532 10.9176 0.97094 10.9281 1.21024C10.947 1.68149 11.1149 2.13382 11.4077 2.50432C11.7509 2.93988 12.2578 3.44577 12.9694 3.84985C13.6359 4.22663 14.2614 4.3893 14.7043 4.46592V4.46384Z" fill="#121826"/>
                            </svg>
                        </a>
                        <a href="https://twitter.com/bankwithzelia" target="_blank" class="social-link">
                            <i class="uil uil-twitter-alt"></i>
                        </a>
                        <a href="https://www.youtube.com/channel/UCHTLPN58nZPQElq-NZ4jmKg" target="_blank"  class="social-link">
                            <i class="uil uil-youtube"></i>
                        </a>
                        <a href="https://www.linkedin.com/company/zelia-finance/" target="_blank" class="social-link">
                            <i class="uil uil-linkedin-alt"></i>
                        </a>
                    </div>
                </footer>
            </div>
        </div>
    </div>
</div>
</body>
</html>